package org.moin.moneytransfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moin.moneytransfer.dto.UserLoginRequest;
import org.moin.moneytransfer.dto.UserSignupRequest;
import org.moin.moneytransfer.entity.User;
import org.moin.moneytransfer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        // 매 테스트 실행 전 데이터베이스를 초기화합니다.
        userRepository.deleteAll();
    }

    @Test
    public void testSignupSuccess() throws Exception {
        // Given
        UserSignupRequest request = new UserSignupRequest();
        request.setUserId("test@example.com");
        request.setPassword("Password123!");
        request.setName("홍길동");
        request.setIdType("REG_NO");
        request.setIdValue("900101-1234567");

        // When
        User user = userService.signup(request);

        // Then
        assertNotNull(user);
        assertEquals("test@example.com", user.getUserId());
        assertEquals("홍길동", user.getName());
        assertEquals("REG_NO", user.getIdType());
        // 암호화된 패스워드와 idValue는 원본과 다릅니다.
        assertNotEquals("Password123!", user.getPassword());
        assertNotEquals("900101-1234567", user.getIdValue());
    }

    @Test
    public void testSignupWithExistingEmail() {
        // Given
        User existingUser = new User();
        existingUser.setUserId("test@example.com");
        existingUser.setPassword("encryptedPassword");
        existingUser.setName("Existing User");
        existingUser.setIdType("REG_NO");
        existingUser.setIdValue("encryptedIdValue");
        userRepository.save(existingUser);

        UserSignupRequest request = new UserSignupRequest();
        request.setUserId("test@example.com"); // 이미 존재하는 이메일
        request.setPassword("Password123!");
        request.setName("홍길동");
        request.setIdType("REG_NO");
        request.setIdValue("900101-1234567");

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            userService.signup(request);
        });

        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
    }

    @Test
    public void testSignupWithInvalidEmail() {
        // Given
        UserSignupRequest request = new UserSignupRequest();
        request.setUserId("invalid-email"); // 유효하지 않은 이메일
        request.setPassword("Password123!");
        request.setName("홍길동");
        request.setIdType("REG_NO");
        request.setIdValue("900101-1234567");

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            userService.signup(request);
        });

        assertEquals("유효한 이메일을 입력해주세요.", exception.getMessage());
    }

    @Test
    public void testSignupWithWeakPassword() {
        // Given
        UserSignupRequest request = new UserSignupRequest();
        request.setUserId("test2@example.com");
        request.setPassword("weak"); // 약한 패스워드
        request.setName("홍길동");
        request.setIdType("REG_NO");
        request.setIdValue("900101-1234567");

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            userService.signup(request);
        });

        assertEquals("패스워드 정책에 맞지 않습니다.", exception.getMessage());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // Given: 회원 가입된 사용자 생성
        UserSignupRequest signupRequest = new UserSignupRequest();
        signupRequest.setUserId("test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setName("홍길동");
        signupRequest.setIdType("REG_NO");
        signupRequest.setIdValue("900101-1234567");
        userService.signup(signupRequest);

        // When: 올바른 아이디와 비밀번호로 로그인 시도
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUserId("test@example.com");
        loginRequest.setPassword("Password123!");

        Map<String, Object> result = userService.login(loginRequest);

        // Then: 로그인 성공 및 토큰 발급 확인
        assertNotNull(result);
        assertNotNull(result.get("token"));
        assertEquals("test@example.com", result.get("userId"));
        assertEquals("홍길동", result.get("name"));
        assertEquals("PERSONAL", result.get("userType"));
    }

}
