package com.heapbrain.core.testdeed.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    String validationExceptionHandler(ValidationException e) {
        return HttpStatus.BAD_REQUEST.toString();
    }

    @ExceptionHandler(Exception.class)
    String genericExceptionHandler(Exception e) {
        return HttpStatus.INTERNAL_SERVER_ERROR.toString();
    }
}