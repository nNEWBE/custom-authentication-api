package com.example.custom_authentication.listener;

import com.example.custom_authentication.event.UserRegisteredEvent;
import com.example.custom_authentication.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UserRegistrationListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationListener.class);
    private final EmailService emailService;

    public UserRegistrationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling user registration event for {}", event.email());
        emailService.sendVerificationEmail(event.email(), event.verificationToken());
    }
}
