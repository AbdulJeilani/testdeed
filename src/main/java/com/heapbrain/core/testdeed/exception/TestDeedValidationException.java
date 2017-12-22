package com.heapbrain.core.testdeed.exception;

public class TestDeedValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TestDeedValidationException(String errorMessage) {
        super(errorMessage);
    }
}