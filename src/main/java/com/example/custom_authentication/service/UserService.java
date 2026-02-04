package com.example.custom_authentication.service;

import com.example.custom_authentication.dto.AuthResponse;
import com.example.custom_authentication.dto.LoginRequest;
import com.example.custom_authentication.dto.RegisterRequest;
import com.example.custom_authentication.entity.User;
import com.example.custom_authentication.event.UserRegisteredEvent;
import com.example.custom_authentication.exception.EmailCooldownException;
import com.example.custom_authentication.exception.UserNotVerifiedException;
import com.example.custom_authentication.repository.UserRepository;
import com.example.custom_authentication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher; // Added import for ApplicationEventPublisher
import org.springframework.security.authentication.AuthenticationManager; // Added import for AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException; // Added import for DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Added import for UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager; // Added AuthenticationManager
    private final ApplicationEventPublisher eventPublisher; // Replaced EmailService with ApplicationEventPublisher

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Constructor-based injection
    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .build();

        generateTokenAndPublishEvent(user); // Changed to publish event
        userRepository.save(user);

        return AuthResponse.builder()
                .message("User registered successfully. Please check your email for verification.")
                .build();
    }

    public String verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return "Token expired. Please login to receive a new verification email.";
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return "Account verified successfully. You can now login.";
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            resendVerification(request.getEmail()); // Call the public method
            throw new RuntimeException("Account not verified. Verification email sent.");
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }

        String token = jwtUtil.generateToken(request.getEmail());
        return AuthResponse.builder()
                .token(token)
                .message("Login successful")
                .build();
    }

    public String resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            return "Account is already verified.";
        }

        // Check if we can resend email (5 minutes cooldown)
        if (user.getLastEmailSentTime() != null &&
                user.getLastEmailSentTime().plusMinutes(5).isAfter(LocalDateTime.now())) {
            long remainingMinutes = java.time.Duration.between(
                    LocalDateTime.now(),
                    user.getLastEmailSentTime().plusMinutes(5)).toMinutes() + 1;
            throw new EmailCooldownException(
                    "Please wait " + remainingMinutes + " minute(s) before requesting a new verification link.");
        }

        generateTokenAndPublishEvent(user);
        userRepository.save(user);
        return "Verification email sent successfully.";
    }

    private void generateTokenAndPublishEvent(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(10)); // 10 minutes expiry
        user.setLastEmailSentTime(LocalDateTime.now());

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getEmail(), token));
    }
}
