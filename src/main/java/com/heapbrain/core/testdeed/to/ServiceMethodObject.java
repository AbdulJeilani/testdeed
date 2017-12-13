package com.heapbrain.core.testdeed.to;

/**
 * @author AbdulJeilani
 */

public class ServiceMethodObject {
	String method;
	String executeService;
	String testDeedName;
	String baseURL;
	String acceptHeader;
	String requestBody;
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getExecuteService() {
		return executeService;
	}
	public void setExecuteService(String executeService) {
		this.executeService = executeService;
	}
	public String getTestDeedName() {
		return testDeedName;
	}
	public void setTestDeedName(String testDeedName) {
		this.testDeedName = testDeedName;
	}
	public String getBaseURL() {
		return baseURL;
	}
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	public String getAcceptHeader() {
		return acceptHeader;
	}
	public void setAcceptHeader(String acceptHeader) {
		this.acceptHeader = acceptHeader;
	}
	public String getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}	
}
