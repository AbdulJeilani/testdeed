package com.heapbrain.core.testdeed.exception;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.heapbrain.core.testdeed.utility.TestDeedUtility;;

@ControllerAdvice
public class TestDeedGlobalExceptionHandler {

	@Autowired
	TestDeedUtility testDeedUtility;

	@ExceptionHandler(TestDeedValidationException.class)
	String validationExceptionHandler(TestDeedValidationException e) throws IOException {
		return IOUtils.toString(testDeedUtility.getHtmlFile("testdeedexception.html"), 
				Charset.forName("UTF-8"))
				.replace("~testdeedexception~", HttpStatus.BAD_REQUEST.toString()+" - "+e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	String genericExceptionHandler(Exception e) throws IOException {
		return IOUtils.toString(testDeedUtility.getHtmlFile("testdeedexception.html"), 
				Charset.forName("UTF-8"))
				.replace("~testdeedexception~", 
						HttpStatus.INTERNAL_SERVER_ERROR.toString()+" - "+e.getMessage());
	}
}