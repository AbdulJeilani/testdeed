package com.heapbrain.core.testdeed.executor;

/**
 * @author AbdulJeilani
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.heapbrain.core.testdeed.annotations.TestDeedApi;
import com.heapbrain.core.testdeed.common.Constant;
import com.heapbrain.core.testdeed.engine.TestDeedReportGenerateEngine;
import com.heapbrain.core.testdeed.engine.TestDeedServiceGenerateEngine;
import com.heapbrain.core.testdeed.exception.TestDeedValidationException;
import com.heapbrain.core.testdeed.to.ApplicationInfo;
import com.heapbrain.core.testdeed.to.GatlingConfiguration;
import com.heapbrain.core.testdeed.to.ServiceMethodObject;
import com.heapbrain.core.testdeed.utility.TestDeedUtility;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;

@RestController
@RequestMapping("/")
public class TestDeedController {

	@Autowired
	public TestDeedServiceGenerateEngine testDeedServiceGenerateEngine;

	@Autowired
	public TestDeedUtility testDeedUtility;

	public static String basePackage = "";
	public static List<String> serverHosts = new ArrayList<>();
	public static String prHost = "";
	public static String reportPath="";
	public static boolean isProdEnabled = false;
	public static ServiceMethodObject serviceMethodObject = new ServiceMethodObject();
	public static Map<String, ServiceMethodObject> serviceMethodObjectMap = new HashMap<>();
	public static GatlingConfiguration gatlingConfiguration = new GatlingConfiguration();
	public static String simulationClass = "";

	@RequestMapping(value = "/testdeed.html", method = RequestMethod.GET)
	public String loadServicePage(HttpServletRequest request) throws Exception {

		List<String> controllerClasses = new ArrayList<String>();
		ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(true);

		simulationClass = loadUserDefinedSimulationClass();

		ApplicationInfo applicationInfo = new ApplicationInfo();
		String currentUrl = request.getScheme()+"://"+request.getServerName();
		if(-1 != request.getServerPort()) {
			currentUrl += ":"+request.getServerPort();
			applicationInfo.setServerLocalUrl(currentUrl);
		}

		boolean isControllerPresent = false;
		for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
			Class<?> cl = Class.forName(bd.getBeanClassName());
			if(isTestDeedConfigClass(cl)) {
				testDeedUtility.loadClassConfig(cl, applicationInfo);
				testDeedUtility.loadMethodConfig(cl, applicationInfo);
				controllerClasses.add(cl.getSimpleName());
				isControllerPresent = true;
			}
		}
		if(isControllerPresent) {
			return testDeedServiceGenerateEngine.generateHomePage(applicationInfo)
					.replace("~controllerClasses~", controllerClasses.toString().replaceAll("\\[|\\]", ""));
		} else {
			return testDeedUtility.getErrorResponse("No controller or TestDeed config not found in application.");
		}
	}

	private boolean isTestDeedConfigClass(Class<?> classInput) {
		if(null != classInput.getDeclaredAnnotation(Controller.class) || 
				null != classInput.getDeclaredAnnotation(RestController.class) || 
				null != classInput.getDeclaredAnnotation(SpringBootApplication.class) || 
				null != classInput.getDeclaredAnnotation(TestDeedApi.class)) {
			return true;
		}
		return false;
	}

	@RequestMapping(value = "/loadrunner.html", method = RequestMethod.POST)
	public synchronized String loadPerformanceResult(HttpServletRequest request) throws IOException {
		try {
			String[] requestMethod = request.getParameter("executeService").split("~");
			if(null != serviceMethodObjectMap.get(requestMethod[0])) {
				serviceMethodObject = serviceMethodObjectMap.get(requestMethod[0]);
				serviceMethodObject.setBaseURL(request.getParameter("baseURL"));
				serviceMethodObject.setExecuteService(frameURL(requestMethod[0], request));
				serviceMethodObject.setMethod(requestMethod[1].toUpperCase());
				serviceMethodObject.setAcceptHeader(request.getParameter("serviceConsume"));
				serviceMethodObject.setServiceName(request.getParameter("applicationservicename"));

				Map<String, String> headerObj = serviceMethodObject.getHeaderObj();
				headerObj.put("Content-Type", request.getParameter("serviceConsume"));

				if(null != request.getParameter("requestHeader")) {
					String[] allHeaders = (request.getParameter("requestHeader").replaceAll("\\[|\\]", "")).split(",");
					for(String header : allHeaders) {
						headerObj.put(header, request.getParameter(header));
					}
				}
				serviceMethodObject.setHeaderObj(headerObj);

				if(requestMethod[1].equalsIgnoreCase("POST") || 
						requestMethod[1].equalsIgnoreCase("PUT")) {
					if(requestMethod.length > 2 && null != requestMethod[2]) {
						if(!requestMethod[2].startsWith("MultipartFile")) {
							serviceMethodObject.setRequestBody(request.getParameter(requestMethod[2]));
						}
					}
				}
			}

			loadGatlingUserConfiguration(request);
			GatlingPropertiesBuilder props = new GatlingPropertiesBuilder();

			if(!request.getParameter("simulationclass").equals("")) {
				simulationClass = request.getParameter("simulationclass");
			} else {
				simulationClass = "com.heapbrain.core.testdeed.gatling.TestDeedSimulation";
			}
			props.simulationClass(simulationClass);
			props.resultsDirectory(reportPath);
			return syncRunner(props);
		} catch (Exception e) {
			return testDeedUtility.getErrorResponse("Gatling configuration error : "
					+e.getMessage() +" Solution : go back and refresh page");
		}
	}

	private String syncRunner(GatlingPropertiesBuilder props) throws IOException {
		Gatling.fromMap(props.build());
		TestDeedReportGenerateEngine singleReport = new TestDeedReportGenerateEngine();
		String response = singleReport.generateReportFromGatling();
		FileUtils.deleteDirectory(new File(reportPath));
		return response;
	}

	private String frameURL(String inputURL, HttpServletRequest request) {
		Pattern MY_PATTERN = Pattern.compile("(\\{)(.*?)(\\})");
		if(!"".equals(request.getParameter("updatedURL"))) {
			inputURL =  inputURL + request.getParameter("updatedURL");
		}
		Matcher m = MY_PATTERN.matcher(inputURL);
		while(m.find()) {
			if(null != request.getParameter(m.group(2))) {
				inputURL = inputURL.replace("{"+m.group(2)+"}", request.getParameter(m.group(2)));
				serviceMethodObject.setMultiPart1(m.group(2));
				serviceMethodObject.setMultiPart2(request.getParameter(m.group(2)));
			}
		}
		return inputURL;
	}

	private String isEmpty(String field, String requestParameter) {
		if(requestParameter.equals("")) {
			return "0";
		} else {
			return requestParameter;
		}
	}

	private void loadGatlingUserConfiguration(HttpServletRequest request) {
		gatlingConfiguration.setConstantUsersPerSec(new Double(isEmpty("constantUsersPerSec",request.getParameter("constantUsersPerSec"))));
		gatlingConfiguration.setDuration(isEmpty("duration",request.getParameter("duration")+" "+request.getParameter("duration_select")));
		gatlingConfiguration.setMaxDuration(isEmpty("maxDuration",request.getParameter("maxDuration")+" "+request.getParameter("maxDuration_select")));
		gatlingConfiguration.setAtOnceUsers(Integer.parseInt(isEmpty("atOnceUsers",request.getParameter("atOnceUsers"))));
		gatlingConfiguration.setRampUser(Integer.parseInt(isEmpty("rampUser",request.getParameter("rampUser"))));
		gatlingConfiguration.setRampUserOver(isEmpty("rampUserOver",request.getParameter("rampUserOver"))+" "+isEmpty("rampUserOver_select",request.getParameter("rampUserOver_select")));
		gatlingConfiguration.setRampUsersPerSec(new Double(isEmpty("rampUsersPerSec",request.getParameter("rampUsersPerSec"))));
		gatlingConfiguration.setRampUsersPerSecTo(new Double(isEmpty("rampUsersPerSecTo",request.getParameter("rampUsersPerSecTo"))));
		gatlingConfiguration.setRampUsersPerSecDuring(isEmpty("rampUsersPerSecDuring",request.getParameter("rampUsersPerSecDuring"))+" "+isEmpty("rampUsersPerSecDuring_select",request.getParameter("rampUsersPerSecDuring_select")));
		gatlingConfiguration.setNothingFor(isEmpty("nothingFor",request.getParameter("nothingFor")+" "+request.getParameter("nothingFor_select")));
		gatlingConfiguration.setStatus(Integer.parseInt(isEmpty("status",request.getParameter("status"))));
		gatlingConfiguration.setMaxResponseTime(isEmpty("maxResponseTime",request.getParameter("maxResponseTime")));
	}

	private String loadUserDefinedSimulationClass() {
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
				throw new TestDeedValidationException(Constant.CONFIGURATION_ERROR +" User defined simulation class does not exist ");
			}
		});
		return returnValue.toString().replace("package ","").replace(".scala", "");
	}

}
