package org.moin.moneytransfer.service;

import org.moin.moneytransfer.dto.UserLoginRequest;
import org.moin.moneytransfer.dto.UserSignupRequest;
import org.moin.moneytransfer.entity.User;
import org.moin.moneytransfer.repository.UserRepository;
import org.moin.moneytransfer.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 회원 가입 처리 메서드
    public User signup(UserSignupRequest request) throws Exception {
        // 1. 입력 값 유효성 검사
        validateUserRequest(request);

        // 2. 패스워드 및 민감 정보 암호화
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        String encryptedIdValue = encryptIdValue(request.getIdValue());

        // 3. 사용자 객체 생성 및 저장
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(encryptedPassword);
        user.setName(request.getName());
        user.setIdType(request.getIdType());
        user.setIdValue(encryptedIdValue);

        userRepository.save(user);

        return user;
    }

    // 로그인 처리 메서드
    public Map<String, Object> login(UserLoginRequest request) throws Exception {
        // 1. 사용자 조회
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new Exception("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new Exception("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. 토큰 생성
        String token = jwtUtil.generateToken(user.getUserId());

        // 4. 응답 데이터 구성
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("name", user.getName());
        data.put("userType", user.getIdType().equals("REG_NO") ? "PERSONAL" : "BUSINESS");

        return data;
    }

    // 유효성 검사 메서드
    private void validateUserRequest(UserSignupRequest request) throws Exception {
        if (!isValidEmail(request.getUserId())) {
            throw new Exception("유효한 이메일을 입력해주세요.");
        }

        if (!isValidPassword(request.getPassword())) {
            throw new Exception("패스워드 정책에 맞지 않습니다.");
        }

        if (!request.getIdType().equals("REG_NO") && !request.getIdType().equals("BUSINESS_NO")) {
            throw new Exception("idType은 REG_NO 또는 BUSINESS_NO만 가능합니다.");
        }

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new Exception("이미 사용 중인 이메일입니다.");
        }

        // 추가로 idValue에 대한 유효성 검사를 진행할 수 있습니다.
    }

    // 이메일 유효성 검사
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    // 패스워드 유효성 검사
    private boolean isValidPassword(String password) {
        // 패스워드 정책 구현 (예: 최소 8자, 숫자, 특수문자 포함)
        if (password.length() < 8) {
            return false;
        }
        // 숫자와 특수문자 포함 여부 검사
        String pattern = "^(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$";
        return Pattern.matches(pattern, password);
    }

    // 민감 정보 암호화
    private String encryptIdValue(String idValue) throws UnsupportedEncodingException {
        // 실제 서비스에서는 안전한 알고리즘과 키 관리를 사용해야 합니다.
        return Base64.getEncoder().encodeToString(idValue.getBytes("UTF-8"));
    }
}
