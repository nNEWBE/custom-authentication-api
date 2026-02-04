package com.example.custom_authentication.exception;

public class EmailCooldownException extends RuntimeException {
    public EmailCooldownException(String message) {
        super(message);
    }
}
