package com.cropdeal.cropservice.exception;

public class InsufficientCropQuantityException extends RuntimeException {
    public InsufficientCropQuantityException(String message) {
        super(message);
    }
}
