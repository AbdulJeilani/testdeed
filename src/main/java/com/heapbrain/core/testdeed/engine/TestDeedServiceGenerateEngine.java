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
import com.heapbrain.core.testdeed.utility.TestDeedServiceUtil;
import com.heapbrain.core.testdeed.utility.TestDeedSupportUtil;
import com.heapbrain.core.testdeed.utility.TestDeedUtility;

@Component
public class TestDeedServiceGenerateEngine {

	@Autowired
	TestDeedUtility testDeedUtility;
	
	public static String updatedURL = "";
	TestDeedConverter testDeedConverter = new TestDeedConverter();

	public String generateHomePage(ApplicationInfo applicationInfo) throws Exception {
		if(isPermission(applicationInfo.isProdEnabled(), applicationInfo.getServerLocalUrl())) {
			String htmlString = IOUtils.toString(testDeedUtility.getHtmlFile("index.html"), 
					Charset.forName("UTF-8"));
			htmlString = htmlString.replace("~application.name~", applicationInfo.getApplicationName());
			htmlString = htmlString.replace("~addshowhidescript~", TestDeedSupportUtil.loadShowHideScript(applicationInfo.getServices()));
			htmlString = htmlString.replace("~servicedetails~", loadServiceDetails(applicationInfo.getServices()));
			htmlString = htmlString.replaceAll("~listofservers~", TestDeedServiceUtil.loadHostDetails(applicationInfo.getServerLocalUrl()));
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

	private String loadServiceDetails(Map<String, Service> services) throws Exception {
		String response = "";
		String temp = "";
		String serviceDesign = IOUtils.toString(testDeedUtility.getHtmlFile("servicedetails.html"), 
				Charset.forName("UTF-8"));
		int serviceCount=0;
		for(Map.Entry<String, Service> entry : services.entrySet()) {
			String key = entry.getKey();//.split("::")[0];;
			Service service = entry.getValue();
			String baseMap = service.getRequestMappingClassLevel();
			if(!key.equals("~")) {
				temp = serviceDesign;
				temp = temp.replace("~serviceshowhideopen~", "<a href=\"javascript:showServices"+serviceCount+"();\">");
				temp = temp.replace("~request.method~", service.getRequestMethod());
				temp = temp.replace("~request.mapping~", baseMap+service.getRequestMapping());
				temp = temp.replace("~service.method.name~", service.getServiceMethodName());
				temp = temp.replace("~service.description~", service.getDescription());
				temp = temp.replace("~application.service.name~", service.getServiceName());
				String consumes="";
				if(null == service.getConsume()) {
					temp = temp.replace("~contenttype_consume~", TestDeedSupportUtil.getContentType(baseMap+service.getRequestMapping(),
							Arrays.asList("application/xml","application/json","multipart/form-data"),"Consume",false));
				} else {
					consumes=service.getConsume().get(0);
					temp = temp.replace("~contenttype_consume~", TestDeedSupportUtil.getContentType(baseMap+service.getRequestMapping(),
							service.getConsume(), "Consume",true));
				}

				response += temp + loadParameters(key, baseMap, consumes, service);
				serviceCount++;
			}
		}
		return response;
	}

	private String loadParameters(String key, String baseMap, String consumes, Service service) throws Exception {
		
		String parametersDesign = TestDeedServiceUtil.loadParameters(key, IOUtils.toString(testDeedUtility.getHtmlFile("parameters.html"), 
				Charset.forName("UTF-8")), baseMap, consumes, service);

		parametersDesign = parametersDesign.replace("~loadparameter~", 
				testDeedConverter.getParmeters(baseMap+service.getRequestMapping(), service.getParameters(), consumes, service.getRequestMethod()));

		parametersDesign = parametersDesign.replace("~updatedURL~", updatedURL);
		updatedURL = "";
		return parametersDesign;

	}
}