package com.heapbrain.core.testdeed.utility;

import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestDeedSupportUtil {

	@Autowired
	TestDeedUtility testDeedUtility;
	
	public static boolean isValidJSON(String json) {
		boolean valid = false;
		try {
			final JsonParser parser = new ObjectMapper().getFactory()
					.createParser(json);
			while (parser.nextToken() != null) {
			}
			valid = true;
		} catch (Exception jpe) {
			throw new ValidationException();
		}
		return valid;
	}
	
}
