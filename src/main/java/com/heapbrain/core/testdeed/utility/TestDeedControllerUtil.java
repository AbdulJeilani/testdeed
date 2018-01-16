package com.heapbrain.core.testdeed.utility;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.heapbrain.core.testdeed.exception.TestDeedValidationException;

/**
 * @author AbdulJeilani
 */

public class TestDeedControllerUtil {

	public static boolean isTestDeedConfigClass(Class<?> classInput) {
		if(null != classInput.getDeclaredAnnotation(Controller.class) || 
				null != classInput.getDeclaredAnnotation(RestController.class) || 
				null != classInput.getDeclaredAnnotation(SpringBootApplication.class)) {
			return true;
		}
		return false;
	}
	
	public static String frameURL(String inputURL, HttpServletRequest request) {
		try {
			Pattern MY_PATTERN = Pattern.compile("(\\{)(.*?)(\\})");
			if(!"".equals(request.getParameter("updatedURL"))) {
				inputURL =  inputURL + request.getParameter("updatedURL");
			}
			Matcher m = MY_PATTERN.matcher(inputURL);
			while(m.find()) {
				if(null != request.getParameter(m.group(2))) {
					inputURL = inputURL.replace("{"+m.group(2)+"}",
							URLEncoder.encode(request.getParameter(m.group(2)), "UTF-8"));
				}
			}
			return inputURL;
		} catch(UnsupportedEncodingException uee) {
			throw new TestDeedValidationException("Validation Error : your service input not supported",uee);
		}
	}

	public static String isEmpty(String field, String requestParameter) {
		if(requestParameter.equals("")) {
			return "0";
		} else {
			return requestParameter;
		}
	}
	
	public static String loadUserDefinedSimulationClass() {
		final String[] SUFFIX = {"scala"};
		StringBuilder returnValue=new StringBuilder();
		FileUtils.listFiles(new File(System.getProperty("user.dir")+"/src"), SUFFIX, true).stream().forEach(f -> {
			try {
				String fileContent = FileUtils.readFileToString(f,Charset.forName("UTF-8"));
				if(fileContent.contains(" extends Simulation")) {
					returnValue.append(fileContent.substring(fileContent.indexOf("package "), fileContent.indexOf("\n"))
							+"."+f.getName());
				}
			} catch (Exception e) {
				throw new TestDeedValidationException("Configuration Error : User defined simulation class does not exist ",e);
			}
		});
		return returnValue.toString().replace("package ","").replace(".scala", "");
	}
}
