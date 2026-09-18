package com.cropdeal.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedOperationException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied: insufficient permissions");
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOtp(InvalidOtpException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_OTP", ex.getMessage());
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleOtpExpired(OtpExpiredException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(TooManyOtpAttemptsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyOtpAttempts(TooManyOtpAttemptsException ex) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "MAX_OTP_ATTEMPTS_EXCEEDED", ex.getMessage());
    }

    @ExceptionHandler(OtpRateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleOtpRateLimit(OtpRateLimitException ex) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "OTP_RATE_LIMITED", ex.getMessage());
    }

    @ExceptionHandler(PasswordResetTokenException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordResetToken(PasswordResetTokenException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN", ex.getMessage());
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCurrentPassword(InvalidCurrentPasswordException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD", ex.getMessage());
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordMismatch(PasswordMismatchException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", ex.getMessage());
    }

    @ExceptionHandler(PasswordReuseException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordReuse(PasswordReuseException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "PASSWORD_REUSE", ex.getMessage());
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleWeakPassword(WeakPasswordException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", ex.getMessage());
    }

    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<Map<String, Object>> handleTokenRevoked(TokenRevokedException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "TOKEN_REVOKED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_FAILED");
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
