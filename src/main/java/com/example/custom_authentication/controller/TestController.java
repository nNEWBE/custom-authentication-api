package com.example.custom_authentication.controller;

import com.example.custom_authentication.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<ApiResponse> testEndpoint(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "Anonymous";
        return ResponseEntity.ok(ApiResponse.success(
                "You have accessed a protected endpoint successfully!",
                java.util.Map.of("authenticatedUser", email)));
    }
}
