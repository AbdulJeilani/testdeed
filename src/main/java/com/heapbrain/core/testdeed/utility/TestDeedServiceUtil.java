package com.heapbrain.core.testdeed.utility;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.heapbrain.core.testdeed.executor.TestDeedController;

public class TestDeedServiceUtil {

	@Autowired
	TestDeedUtility testDeedUtility;
	
	public static String loadParameters(String key, String parametersDesign, String baseMap, String requestMapping, Map<String, Object> parameters, 
			String requestMethod, String consumes, String serviceName, String methodName, String methodDescription) throws Exception {

		parametersDesign = parametersDesign.replace("~id~", key+"_divshowhide");
		parametersDesign = parametersDesign.replace("~application.service.name~", serviceName);

		parametersDesign = parametersDesign.replace("~service.method.name~", methodName);
		parametersDesign = parametersDesign.replace("~service.description~", methodDescription);
		
		if(null != parameters.get("RequestBody")) {
			Class<?> classTemp = parameters.get("RequestBody").getClass();
			if(!TestDeedConverter.declaredVariableType.contains(classTemp.getSimpleName())) {
				parametersDesign = parametersDesign.replace("~buttonmapid~", baseMap+requestMapping+"~"+requestMethod+"~"+classTemp.getSimpleName());
			}
		} else {
			parametersDesign = parametersDesign.replace("~buttonmapid~", baseMap+requestMapping+"~"+requestMethod+"~");
		}

		return parametersDesign;
	}
	
	public static String loadHostDetails(String localHost) {
		StringBuffer loadHostDetails = new StringBuffer();
		loadHostDetails.append("<select name=\"baseURL\">");
		loadHostDetails.append("<option value=\""+localHost+"\">"+localHost+"</option>");
		for(String host : TestDeedController.serverHosts) {

			if(host !=null) {
				if(host.equals(TestDeedController.prHost) && TestDeedController.isProdEnabled) {
					loadHostDetails.append("<option value=\""+host+"\">"+host+"</option>");
				} else if(!host.equals(TestDeedController.prHost)) {
					loadHostDetails.append("<option value=\""+host+"\">"+host+"</option>");
				}
			}

		}
		loadHostDetails.append("</select>");
		return loadHostDetails.toString();
	}
}
