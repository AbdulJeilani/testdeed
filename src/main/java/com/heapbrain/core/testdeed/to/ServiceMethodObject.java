package com.heapbrain.core.testdeed.to;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author AbdulJeilani
 */

public class ServiceMethodObject {
	String serviceName = "";
	String method="";
	String executeService="";
	String testDeedName="";
	String baseURL="";
	String acceptHeader="";
	String requestBody="";
	Map<String, String> headerObj = new HashMap<String, String>();
	ArrayNode feederRuleObj;
	List<String> feederRuleXMLObj = new ArrayList<>();
	List<String> feederInputURL = new ArrayList<>();

	public List<String> getFeederInputURL() {
		return feederInputURL;
	}

	public void setFeederInputURL(List<String> feederInputURL) {
		this.feederInputURL = feederInputURL;
	}

	public List<String> getFeederRuleXMLObj() {
		return feederRuleXMLObj;
	}
	public void setFeederRuleXMLObj(List<String> feederRuleXMLObj) {
		this.feederRuleXMLObj = feederRuleXMLObj;
	}
	public ArrayNode getFeederRuleObj() {
		return feederRuleObj;
	}
	public void setFeederRuleObj(ArrayNode feederRuleObj) {
		this.feederRuleObj = feederRuleObj;
	}
	String multiPart1 = "";
	String multiPart2 = "";
	
	public String getMultiPart1() {
		return multiPart1;
	}
	public void setMultiPart1(String multiPart1) {
		this.multiPart1 = multiPart1;
	}
	public String getMultiPart2() {
		return multiPart2;
	}
	public void setMultiPart2(String multiPart2) {
		this.multiPart2 = multiPart2;
	}
	public Map<String, String> getHeaderObj() {
		return headerObj;
	}
	public void setHeaderObj(Map<String, String> headerObj) {
		this.headerObj = headerObj;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
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
