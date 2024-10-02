package org.moin.moneytransfer.controller;

import org.moin.moneytransfer.dto.UserLoginRequest;
import org.moin.moneytransfer.dto.UserSignupRequest;
import org.moin.moneytransfer.entity.User;
import org.moin.moneytransfer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignupRequest request) {
        try {
            // 서비스 클래스를 통해 회원 가입 처리
            User user = userService.signup(request);

            // 응답 생성
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("name", user.getName());
            data.put("userType", user.getIdType().equals("REG_NO") ? "PERSONAL" : "BUSINESS");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "회원 가입이 완료되었습니다.");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 에러 처리
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAIL");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            // 서비스 클래스를 통해 로그인 처리
            Map<String, Object> data = userService.login(request);

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "로그인이 완료되었습니다.");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 에러 처리
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAIL");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
