package com.example.smartlearn.controller.auth;

import com.example.smartlearn.dto.request.LoginRequest;
import com.example.smartlearn.dto.request.UserRegistrationRequest;
import com.example.smartlearn.dto.response.LoginResponse;
import com.example.smartlearn.exception.AccountAlreadyExistsException;
import com.example.smartlearn.exception.InvalidRoleException;
import com.example.smartlearn.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest registrationRequest) {
        try {
            authService.register(registrationRequest);
            return ResponseEntity.ok("注册成功");
        } catch (AccountAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (InvalidRoleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("注册失败：" + e.getMessage());
        }
    }
}