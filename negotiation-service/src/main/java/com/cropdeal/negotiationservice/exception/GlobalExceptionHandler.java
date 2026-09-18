package com.cropdeal.negotiationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("timestamp", LocalDateTime.now());
        err.put("status", HttpStatus.BAD_REQUEST.value());
        err.put("error", "Bad Request");
        err.put("message", ex.getMessage());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("timestamp", LocalDateTime.now());
        err.put("status", HttpStatus.CONFLICT.value());
        err.put("error", "Conflict / Invalid State");
        err.put("message", ex.getMessage());
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("timestamp", LocalDateTime.now());
        err.put("status", HttpStatus.BAD_REQUEST.value());
        err.put("error", "Validation Error");
        err.put("message", ex.getBindingResult().getFieldError() != null ? ex.getBindingResult().getFieldError().getDefaultMessage() : "Invalid input");
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("timestamp", LocalDateTime.now());
        err.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.put("error", "Internal Server Error");
        err.put("message", ex.getMessage());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}