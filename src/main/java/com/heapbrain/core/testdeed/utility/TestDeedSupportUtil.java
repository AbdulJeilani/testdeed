package com.heapbrain.core.testdeed.utility;

/**
 * @author AbdulJeilani
 */

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heapbrain.core.testdeed.exception.TestDeedValidationException;

public class TestDeedSupportUtil {

	public static String isValidJSON(String json) {
		String valid = "";
		try {
			final JsonParser parser = new ObjectMapper().getFactory()
					.createParser(json);
			while (parser.nextToken() != null) {
			}
			valid = "yes";
		} catch (Exception jpe) {
			throw new TestDeedValidationException(getErrorResponse("TestDeed error : Unable to convert object to JSON/XML",jpe.getMessage(), jpe.getStackTrace()));
		}
		return valid;
	}

	private static InputStream getHtmlFile(String fileName) {
		ClassLoader classLoader = TestDeedUtility.class.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}
	
	public static String getErrorResponse(String errorDescription, String error, StackTraceElement[] stackTraces) {
		try {
		StringBuilder sb = new StringBuilder();
		sb.append(error);
		if(null != stackTraces) {
			for(StackTraceElement element : stackTraces) {
				sb.append("<br/>"+element.getClassName()+"."+element.getMethodName()+"("+element.getFileName()+":"+element.getLineNumber()+")");
			}
		}
		return IOUtils.toString(getHtmlFile("testdeedexception.html"), 
				Charset.forName("UTF-8")).replace("~testdeedexception~", errorDescription)
				.replace("~printstacktrace~", sb);
		} catch (Exception e) {
			throw new TestDeedValidationException("Unavailable error code");
		}
	}
}
