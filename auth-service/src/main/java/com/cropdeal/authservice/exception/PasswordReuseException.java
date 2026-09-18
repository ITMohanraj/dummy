package com.cropdeal.authservice.exception;

public class PasswordReuseException extends RuntimeException {
    public PasswordReuseException(String message) {
        super(message);
    }
}
