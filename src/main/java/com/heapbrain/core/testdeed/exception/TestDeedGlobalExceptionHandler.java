package com.heapbrain.core.testdeed.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;;

@ControllerAdvice
public class TestDeedGlobalExceptionHandler {

	@ExceptionHandler(TestDeedValidationException.class)
	String validationExceptionHandler(TestDeedValidationException e) {
		return e.getMessage();
	}

	@ExceptionHandler(Exception.class)
	String genericExceptionHandler(Exception e) {
		return e.getMessage();
	}
}