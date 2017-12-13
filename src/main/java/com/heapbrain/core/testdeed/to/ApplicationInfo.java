package com.heapbrain.core.testdeed.to;

/**
 * @author AbdulJeilani
 */

import java.util.HashMap;
import java.util.Map;

public class ApplicationInfo {
	String serverLocalUrl;
	String applicationName;
	String description;
	String mapping;
	String testDeedApi;
	boolean isSwagger;
	boolean isProdEnabled;
	Map<String, Service> services = new HashMap<>();
	
	public String getServerLocalUrl() {
		return serverLocalUrl;
	}
	public void setServerLocalUrl(String serverLocalUrl) {
		this.serverLocalUrl = serverLocalUrl;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMapping() {
		return mapping;
	}
	public void setMapping(String mapping) {
		this.mapping = mapping;
	}
	public String getTestDeedApi() {
		return testDeedApi;
	}
	public void setTestDeedApi(String testDeedApi) {
		this.testDeedApi = testDeedApi;
	}
	public boolean isSwagger() {
		return isSwagger;
	}
	public void setSwagger(boolean isSwagger) {
		this.isSwagger = isSwagger;
	}
	public boolean isProdEnabled() {
		return isProdEnabled;
	}
	public void setProdEnabled(boolean isProdEnabled) {
		this.isProdEnabled = isProdEnabled;
	}
	public Map<String, Service> getServices() {
		return services;
	}
	public void setServices(Map<String, Service> services) {
		this.services = services;
	}
}
