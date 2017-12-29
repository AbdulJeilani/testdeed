package com.heapbrain.core.testdeed.executor;

/**
 * @author AbdulJeilani
 */

import java.io.BufferedOutputStream;

/**
 * @author AbdulJeilani
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.heapbrain.core.testdeed.annotations.TestDeedApi;
import com.heapbrain.core.testdeed.annotations.TestDeedApplication;
import com.heapbrain.core.testdeed.engine.TestDeedReportGenerateEngine;
import com.heapbrain.core.testdeed.engine.TestDeedServiceGenerateEngine;
import com.heapbrain.core.testdeed.exception.TestDeedValidationException;
import com.heapbrain.core.testdeed.to.ApplicationInfo;
import com.heapbrain.core.testdeed.to.GatlingConfiguration;
import com.heapbrain.core.testdeed.to.ServiceMethodObject;
import com.heapbrain.core.testdeed.utility.TestDeedSupportUtil;
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
				if(null == cl.getDeclaredAnnotation(SpringBootApplication.class)) {
					controllerClasses.add(cl.getSimpleName());
				}
				if(null != cl.getDeclaredAnnotation(SpringBootApplication.class) 
						&& null == cl.getDeclaredAnnotation(TestDeedApplication.class)) {
					String htmlString = IOUtils.toString(testDeedUtility.getHtmlFile("testdeedexception.html"), 
							Charset.forName("UTF-8"));
					return htmlString.replace("~testdeedexception~", "Configuration Error : Testdeed configurations missing in springboot.")
							.replace("~printstacktrace~", "");
				}
				isControllerPresent = true;
			}
		}
		if(isControllerPresent) {
			return testDeedServiceGenerateEngine.generateHomePage(applicationInfo)
					.replace("~controllerClasses~", controllerClasses.toString().replaceAll("\\[|\\]", ""));
		} else {
			throw new TestDeedValidationException("Configuration Error : Controller configurations missing.");
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

			if(!request.getParameter("simulationclass").equals("")) {
				simulationClass = request.getParameter("simulationclass");
			} else {
				String[] requestMethod = request.getParameter("executeService").split("~");

				Part bodyFeeder = request.getPart("bodyFeeder");
				Part multipartfile = request.getPart("multipartfile");

				if(null != serviceMethodObjectMap.get(requestMethod[0])) {
					serviceMethodObject = serviceMethodObjectMap.get(requestMethod[0]);
					serviceMethodObject.setBaseURL(request.getParameter("baseURL"));
					serviceMethodObject.setExecuteService(frameURL(requestMethod[0], request));
					serviceMethodObject.setMethod(requestMethod[1].toUpperCase());
					serviceMethodObject.setAcceptHeader(request.getParameter("serviceConsume"));
					serviceMethodObject.setServiceName(request.getParameter("applicationservicename"));

					if(null != bodyFeeder && !bodyFeeder.getSubmittedFileName().equals("")) {
						byte[] bytes = IOUtils.toByteArray(bodyFeeder.getInputStream());
						String feederInputJson = new String(bytes);
						String isValid = TestDeedSupportUtil.isValidJSON(feederInputJson);
						if(!isValid.equals("yes")) {
							return isValid; 
						}
						ObjectMapper mapper = new ObjectMapper();
						JsonParser jsonParser = new JsonFactory().createParser(feederInputJson);
						ArrayNode json = mapper.readTree(jsonParser);
						serviceMethodObject.setFeederRuleObj(json);
					}

					if(null != multipartfile && !multipartfile.getSubmittedFileName().equals("")) {
						byte[] bytes = IOUtils.toByteArray(multipartfile.getInputStream());
						String path = System.getProperty("user.dir")+"/target/performance/upload/"+multipartfile.getSubmittedFileName();
						FileUtils.touch(new File(path));
						BufferedOutputStream stream = new BufferedOutputStream(
								new FileOutputStream(new File(path)));
						stream.write(bytes);
						stream.close();
						serviceMethodObject.setMultiPart1(request.getParameter("multipartfile_object"));
						serviceMethodObject.setMultiPart2(path);
					}

					Map<String, String> headerObj = serviceMethodObject.getHeaderObj();
					headerObj.put("Content-Type", request.getParameter("serviceConsume"));

					if(null != request.getParameter("requestHeader")) {
						String header = (request.getParameter("requestHeader").replaceAll("\\[|\\]", ""));
						headerObj.put(header, request.getParameter(header));
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
				if(null != bodyFeeder && !bodyFeeder.getSubmittedFileName().equals("")){
					simulationClass = "com.heapbrain.core.testdeed.gatling.TestDeedFeederSimulation";
				} else {
					simulationClass = "com.heapbrain.core.testdeed.gatling.TestDeedSimulation";
				}
				loadGatlingUserConfiguration(request);
			}

			GatlingPropertiesBuilder props = new GatlingPropertiesBuilder();
			props.simulationClass(simulationClass);
			props.resultsDirectory(reportPath);
			return syncRunner(props);
		} catch (Exception e) {
			throw new TestDeedValidationException("Gatling configuration error " + e.getMessage(), e);
		}
	}

	private String syncRunner(GatlingPropertiesBuilder props) {
		try {
			FileUtils.deleteDirectory(new File(reportPath));
			Gatling.fromMap(props.build());
			TestDeedReportGenerateEngine singleReport = new TestDeedReportGenerateEngine();
			String response = singleReport.generateReportFromGatling();

			return response;
		} catch(Exception e) {
			throw new TestDeedValidationException(TestDeedSupportUtil.getErrorResponse("Gatling configuration error ",e.getMessage(),e.getStackTrace()));
		}
	}

	private String frameURL(String inputURL, HttpServletRequest request) {
		try {
			Pattern MY_PATTERN = Pattern.compile("(\\{)(.*?)(\\})");
			if(!"".equals(request.getParameter("updatedURL"))) {
				inputURL =  inputURL + request.getParameter("updatedURL");
			}
			Matcher m = MY_PATTERN.matcher(inputURL);
			while(m.find()) {
				if(null != request.getParameter(m.group(2))) {
					inputURL = inputURL.replace("{"+m.group(2)+"}", URLEncoder.encode(request.getParameter(m.group(2)), "UTF-8"));
				}
			}
			return inputURL;
		} catch(UnsupportedEncodingException uee) {
			throw new TestDeedValidationException("Validation Error : your service input not supported",uee);
		}
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
				throw new TestDeedValidationException("Configuration Error : User defined simulation class does not exist ",e);
			}
		});
		return returnValue.toString().replace("package ","").replace(".scala", "");
	}

}
