package com.heapbrain.core.testdeed.utility;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heapbrain.core.testdeed.exception.TestDeedValidationException;
import com.heapbrain.core.testdeed.to.Service;

public class TestDeedSupportUtil {

	@Autowired
	static TestDeedUtility testDeedUtility;
	
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

	public static String getErrorResponse(String errorDescription, String error, StackTraceElement[] stackTraces) {
		try {
		StringBuilder sb = new StringBuilder();
		sb.append(error);
		if(null != stackTraces) {
			for(StackTraceElement element : stackTraces) {
				sb.append("<br/>"+element.getClassName()+"."+element.getMethodName()+"("+element.getFileName()+":"+element.getLineNumber()+")");
			}
		}
		return IOUtils.toString(testDeedUtility.getHtmlFile("testdeedexception.html"), 
				Charset.forName("UTF-8")).replace("~testdeedexception~", errorDescription)
				.replace("~printstacktrace~", sb);
		} catch (Exception e) {
			throw new TestDeedValidationException("Unavailable error code");
		}
	}
	
	public static String getGenericType(String generateValues) {
		String result = "";
		String[] generateValue = generateValues.split(" ");
		if(generateValue[0].contains("<")) {
			String[] dataTypes = generateValue[0].split("<");
			result = dataTypes[0].substring(dataTypes[0].lastIndexOf(".")+1, dataTypes[0].length())+"<";
			result += dataTypes[1].substring(dataTypes[1].lastIndexOf(".")+1, dataTypes[1].indexOf(">"))+">";
		} else {
			result = generateValue[0].substring(generateValue[0].lastIndexOf(".")+1, generateValue[0].length());
		}
		result += "~"+generateValue[1];
		return StringEscapeUtils.escapeXml(result);
	}
	
	public static String getContentType(String serviceURL, List<String> contentTypes, String regards, boolean isPresent) {
		StringBuffer contentType = new StringBuffer();

		contentType.append("<p style=\"margin:10px;\"><font color=\"#3c495a\">Consumes&nbsp;&nbsp;</font>");
		contentType.append("<select style=\"width: 130px;\" name=\"service"+regards+"\">");
		for(String content : contentTypes) {
			if(isPresent) {
				contentType.append("<option selected value=\""+content+"\"><font color=\"#39495c\">"+content+"</font></option>");
				isPresent=false;
			} else {
				contentType.append("<option value=\""+content+"\"><font color=\"#39495c\">"+content+"</font></option>");
			}
		}
		contentType.append("</select></p>");

		return contentType.toString();
	}
	
	public static String loadShowHideScript(Map<String, Service> services) {
		String script = "<script language=\"javascript\">";
		int serviceCount=0;
		StringBuilder hideAllService = new StringBuilder();
		StringBuilder showAllService = new StringBuilder();
		for(Map.Entry<String, Service> entry : services.entrySet()) {
			script += "function showServices"+serviceCount+"() {";
			String variable = "x"+(serviceCount);
			script += "var "+variable+" = document.getElementById(\""+entry.getKey()+"_divshowhide\");";
			script += "if ("+variable+".style.display == \"none\") {";
			script += variable+".style.display = \"block\";";
			script += "} else {";
			script += variable+".style.display = \"none\";";
			script += "}}";
			serviceCount++;
			hideAllService.append("document.getElementById(\""+entry.getKey()+"_divshowhide\").style.display = \"none\";");
			showAllService.append("document.getElementById(\""+entry.getKey()+"_divshowhide\").style.display = \"block\";");
		}
		
		script += "function hideAllService(){"+hideAllService+"}";
		script += "function showAllService() {"+showAllService+"}";
		
		script += "</script>";
		return script;
	}

}
