package com.heapbrain.core.testdeed.exception;

/**
 * @author AbdulJeilani
 */

public class TestDeedValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TestDeedValidationException(String errorMessage) {
        super(errorMessage);
    }
	
	public TestDeedValidationException(String errorMessage, Exception e) {
        super(errorMessage, e);
    }
}