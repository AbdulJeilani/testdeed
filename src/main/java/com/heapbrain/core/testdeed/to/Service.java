package com.heapbrain.core.testdeed.to;

/**
 * @author AbdulJeilani
 */

import java.util.ArrayList;

/**
 * @author AbdulJeilani
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service implements Cloneable {
	String serviceName="";
	String serviceMethodName="";
	String requestMappingClassLevel="";
	String requestMapping="";
	String requestMethod="";
	String description="";
	List<String> consume=new ArrayList<>();
	Map<String, Object> parameters = new HashMap<>();
	
	public String getRequestMappingClassLevel() {
		return requestMappingClassLevel;
	}
	public void setRequestMappingClassLevel(String requestMappingClassLevel) {
		this.requestMappingClassLevel = requestMappingClassLevel;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceMethodName() {
		return serviceMethodName;
	}
	public void setServiceMethodName(String serviceMethodName) {
		this.serviceMethodName = serviceMethodName;
	}
	public String getRequestMapping() {
		return requestMapping;
	}
	public void setRequestMapping(String requestMapping) {
		this.requestMapping = requestMapping;
	}
	public String getRequestMethod() {
		return requestMethod;
	}
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getConsume() {
		return consume;
	}
	public void setConsume(List<String> consume) {
		this.consume = consume;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
