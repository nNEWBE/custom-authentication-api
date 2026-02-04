package com.example.custom_authentication.event;

public record UserRegisteredEvent(String email, String verificationToken) {
}
