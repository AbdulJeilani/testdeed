package com.heapbrain.core.testdeed.utility;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.heapbrain.core.testdeed.executor.TestDeedController;
import com.heapbrain.core.testdeed.to.Service;

public class TestDeedServiceUtil {

	@Autowired
	TestDeedUtility testDeedUtility;
	
	public static String loadParameters(int serviceCount, String key, String parametersDesign, String baseMap, String consumes, Service service) throws Exception {
		Map<String, Object> parameters = service.getParameters();
		parametersDesign = parametersDesign.replace("~id~", key+"_divshowhide");
		parametersDesign = parametersDesign.replace("~application.service.name~", service.getServiceName());

		parametersDesign = parametersDesign.replace("~service.method.name~", service.getServiceMethodName());
		parametersDesign = parametersDesign.replace("~service.description~", service.getDescription());

		if(service.getRequestMethod().equalsIgnoreCase("GET")) {
			parametersDesign = parametersDesign.replace("~feeder4get~",
					"Feeder : <input type=\"file\" name=\"getFeeder\" id=\"getFeeder\"/>");
		} else {
			parametersDesign = parametersDesign.replace("~feeder4get~", "<input type=\"file\" name=\"getFeeder\" id=\"getFeeder\" style=\"display:none;\"/>");
		}

		if(null != parameters.get("RequestBody")) {
			Class<?> classTemp = parameters.get("RequestBody").getClass();
			if(!TestDeedConverter.declaredVariableType.contains(classTemp.getSimpleName())) {
				parametersDesign = parametersDesign.replace("~buttonmapid~", baseMap+service.getRequestMapping()+"~"+service.getRequestMethod()+"~"+classTemp.getSimpleName());
				parametersDesign = parametersDesign.replace("_~serviceCount~_", String.valueOf(serviceCount));
			}
		} else {
			parametersDesign = parametersDesign.replace("~buttonmapid~", baseMap+service.getRequestMapping()+"~"+service.getRequestMethod()+"~");
			parametersDesign = parametersDesign.replace("_~serviceCount~_", String.valueOf(serviceCount));
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
