package com.heapbrain.core.testdeed.exception;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = -1372259933391317509L;

    public ValidationException(String errorMessage) {
        super(errorMessage);
    }
}