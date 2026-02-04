package com.example.custom_authentication.controller;

import com.example.custom_authentication.dto.ApiResponse;
import com.example.custom_authentication.dto.LoginRequest;
import com.example.custom_authentication.dto.RegisterRequest;
import com.example.custom_authentication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegisterRequest request) {
        var authResponse = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse.getMessage()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {
        var authResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(authResponse.getMessage())
                .data(java.util.Map.of("token", authResponse.getToken()))
                .build());
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse> verifyUser(@RequestParam("token") String token) {
        String message = userService.verifyUser(token);
        boolean success = message.contains("successfully");
        return ResponseEntity.ok(ApiResponse.builder()
                .success(success)
                .message(message)
                .build());
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(@RequestParam("email") String email) {
        String message = userService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
