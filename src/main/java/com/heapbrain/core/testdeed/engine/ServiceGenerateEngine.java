package com.heapbrain.core.testdeed.engine;

/**
 * @author AbdulJeilani
 */

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.heapbrain.core.testdeed.executor.TestDeedController;
import com.heapbrain.core.testdeed.to.ApplicationInfo;
import com.heapbrain.core.testdeed.to.Service;
import com.heapbrain.core.testdeed.utility.TestDeedConverter;
import com.heapbrain.core.testdeed.utility.TestDeedUtility;

@Component
public class ServiceGenerateEngine {

	@Autowired
	TestDeedUtility testDeedUtility;
	public static String updatedURL = "";
	TestDeedConverter testDeedConverter = new TestDeedConverter();

	public String generateHomePage(ApplicationInfo applicationInfo) throws Exception {
		if(isPermission(applicationInfo.isProdEnabled(), applicationInfo.getServerLocalUrl())) {
			String htmlString = IOUtils.toString(testDeedUtility.getHtmlFile("index.html"), 
					Charset.forName("UTF-8"));
			htmlString = htmlString.replace("~application.name~", applicationInfo.getApplicationName());
			htmlString = htmlString.replace("~addshowhidescript~", loadShowHideScript(applicationInfo.getServices()));
			htmlString = htmlString.replace("~servicedetails~", loadServiceDetails(applicationInfo.getMapping(), applicationInfo.getServices()));
			htmlString = htmlString.replaceAll("~listofservers~", loadHostDetails(applicationInfo.getServerLocalUrl()));
			String userDefined = "";
			if(TestDeedController.simulationClass.equals("")) {
				userDefined = "<select style=\"width:300px;\" id=\"simulationclass\" name=\"simulationclass\"><option value=\"\"></option></select>";
			} else {
				userDefined = "<select style=\"width:300px;\" id=\"simulationclass\" name=\"simulationclass\"><option value=\"\"></option>"
						+ "<option value=\""+TestDeedController.simulationClass+"\">"+TestDeedController.simulationClass+"</option></select>";
			}
			htmlString = htmlString.replace("~userdefinedsimulation~", userDefined);

			return htmlString;
		} else {
			return IOUtils.toString(testDeedUtility.getHtmlFile("testdeedexception.html"), 
					Charset.forName("UTF-8")).replace("~testdeedexception~", "Performance test - Permission denied in production");
		}
	}

	private boolean isPermission(boolean isProdEnabled, String localURL) {
		if(!isProdEnabled) {
			if(!localURL.equals(TestDeedController.prHost)) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	private String loadShowHideScript(Map<String, Service> services) {
		String script = "<script language=\"javascript\">";
		int serviceCount=0;
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
		}
		script += "</script>";
		return script;
	}

	private String loadServiceDetails(String baseMap, Map<String, Service> services) throws Exception {
		String response = "";
		String temp = "";
		String serviceDesign = IOUtils.toString(testDeedUtility.getHtmlFile("servicedetails.html"), 
				Charset.forName("UTF-8"));
		int serviceCount=0;
		for(Map.Entry<String, Service> entry : services.entrySet()) {
			Service service = entry.getValue();
			if(!entry.getKey().equals("~")) {
				temp = serviceDesign;
				temp = temp.replace("~serviceshowhideopen~", "<a href=\"javascript:showServices"+serviceCount+"();\">");
				temp = temp.replace("~request.method~", service.getRequestMethod());
				temp = temp.replace("~request.mapping~", baseMap+service.getRequestMapping());
				temp = temp.replace("~service.method.name~", service.getServiceMethodName());
				temp = temp.replace("~service.description~", service.getDescription());
				temp = temp.replace("~application.service.name~", service.getServiceName());
				String consumes="";
				if(null == service.getConsume()) {
					temp = temp.replace("~contenttype_consume~", testDeedUtility.getContentType(baseMap+service.getRequestMapping(),
							Arrays.asList("application/xml","application/json","multipart/form-data"),"Consume",false));
				} else {
					consumes=service.getConsume().get(0);
					temp = temp.replace("~contenttype_consume~", testDeedUtility.getContentType(baseMap+service.getRequestMapping(),
							service.getConsume(), "Consume",true));
				}

				response += temp + loadParameters(baseMap, service.getRequestMapping(), 
						service.getParameters(), service.getRequestMethod(), consumes, service.getServiceName());
				serviceCount++;
			}

		}
		return response;
	}

	private String loadHostDetails(String localHost) {
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

	private String loadParameters(String baseMap, String requestMapping, Map<String, Object> parameters, 
			String requestMethod, String consumes, String serviceName) throws Exception {

		String parametersDesign = IOUtils.toString(testDeedUtility.getHtmlFile("parameters.html"), 
				Charset.forName("UTF-8")); 
		parametersDesign = parametersDesign.replace("~id~", requestMapping+"~"+requestMethod+"_divshowhide");
		parametersDesign = parametersDesign.replace("~application.service.name~", serviceName);

		if(null != parameters.get("RequestBody")) {
			Class<?> classTemp = parameters.get("RequestBody").getClass();
			if(!TestDeedConverter.declaredVariableType.contains(classTemp.getSimpleName())) {
				parametersDesign = parametersDesign.replace("~buttonmapid~", baseMap+requestMapping+"~"+requestMethod+"~"+classTemp.getSimpleName());
			}
		} else {
			parametersDesign = parametersDesign.replace("~buttonmapid~", baseMap+requestMapping+"~"+requestMethod+"~");
		}

		parametersDesign = parametersDesign.replace("~loadparameter~", 
				testDeedConverter.getParmeters(baseMap+requestMapping, parameters, consumes));

		parametersDesign = parametersDesign.replace("~updatedURL~", updatedURL);
		updatedURL = "";
		return parametersDesign;

	}
}
