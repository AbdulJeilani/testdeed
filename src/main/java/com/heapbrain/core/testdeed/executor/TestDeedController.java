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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.heapbrain.core.testdeed.utility.TestDeedReportUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
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
import com.heapbrain.core.testdeed.utility.TestDeedControllerUtil;
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

		simulationClass = TestDeedControllerUtil.loadUserDefinedSimulationClass();

		ApplicationInfo applicationInfo = new ApplicationInfo();
		String currentUrl = request.getScheme()+"://"+request.getServerName();
		if(-1 != request.getServerPort()) {
			currentUrl += ":"+request.getServerPort();
			applicationInfo.setServerLocalUrl(currentUrl);
		}

		boolean isControllerPresent = false;
		for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
			Class<?> cl = Class.forName(bd.getBeanClassName());
			if(TestDeedControllerUtil.isTestDeedConfigClass(cl)) {
				if(null != cl.getDeclaredAnnotation(TestDeedApi.class) || 
						null != cl.getDeclaredAnnotation(TestDeedApplication.class)) {
					testDeedUtility.loadMethodConfig(cl, applicationInfo, testDeedUtility.loadClassConfig(cl, applicationInfo));
					if(null == cl.getDeclaredAnnotation(SpringBootApplication.class)) {
						controllerClasses.add(cl.getSimpleName());
					}
					isControllerPresent = true;
				}
			}
		}
		if(isControllerPresent) {
			return testDeedServiceGenerateEngine.generateHomePage(applicationInfo)
					.replace("~controllerClasses~", controllerClasses.toString().replaceAll("\\[|\\]", ""));
		} else {
			String htmlString = IOUtils.toString(testDeedUtility.getHtmlFile("testdeedexception.html"), 
					Charset.forName("UTF-8"));
			return htmlString.replace("~testdeedexception~", "Configuration Error : Testdeed configurations missing in springboot.")
					.replace("~printstacktrace~", "");
		}
	}

	@RequestMapping(value = "/showreport", method = RequestMethod.POST)
	public synchronized void showreport(HttpServletResponse response) throws IOException {
		File file = new File(System.getProperty("user.dir")+"/src/main/webapp/performance/reports");
		String redirectURL = TestDeedReportUtil.loadIndexHtml(file);
		if(null == redirectURL || redirectURL.equals("")) {
            throw new TestDeedValidationException("No reports found");
        } else {
            response.sendRedirect(redirectURL);
        }
	}

	@RequestMapping(value = "/loadrunner.html", method = RequestMethod.POST)
	public synchronized void loadPerformanceResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
					serviceMethodObject.setExecuteService(TestDeedControllerUtil.frameURL(requestMethod[0], request));
					serviceMethodObject.setMethod(requestMethod[1].toUpperCase());
					serviceMethodObject.setAcceptHeader(request.getParameter("serviceConsume"));
					serviceMethodObject.setServiceName(request.getParameter("applicationservicename"));

					if(null != bodyFeeder && !bodyFeeder.getSubmittedFileName().equals("")) {
						byte[] bytes = IOUtils.toByteArray(bodyFeeder.getInputStream());
						String feederInput = new String(bytes);
						if(serviceMethodObject.getAcceptHeader().equals("application/json")) {
							String isValid = TestDeedSupportUtil.isValidJSON(feederInput);
							if(!isValid.equals("yes")) {
								response.sendRedirect("/testdeed.html");
							}
							ObjectMapper mapper = new ObjectMapper();
							JsonParser jsonParser = new JsonFactory().createParser(feederInput);
							ArrayNode json = mapper.readTree(jsonParser);
							serviceMethodObject.setFeederRuleObj(json);
						} else if(serviceMethodObject.getAcceptHeader().equals("application/xml")){
							Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}*");
							Matcher matcher = pattern.matcher(feederInput);
							List<String> feederInputXml = new ArrayList<>();
							while(matcher.find()) {
								feederInputXml.add(matcher.group(1));
							}
							serviceMethodObject.setFeederRuleXMLObj(feederInputXml);
						}
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
					if(!requestMethod[1].equalsIgnoreCase("GET")) {
						headerObj.put("Content-Type", request.getParameter("serviceConsume"));
					}

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
			String redirectURL = syncRunner(props);
            if(null == redirectURL || redirectURL.equals("")) {
				throw new TestDeedValidationException("Gatling error. Check your request and configurations");
            } else {
                response.sendRedirect(redirectURL);
            }
		} catch (Exception e) {
			throw new TestDeedValidationException("Gatling configuration error " + e.getMessage(), e);
		}
	}

	private String syncRunner(GatlingPropertiesBuilder props) {
		try {
			FileUtils.deleteDirectory(new File(reportPath));
			Gatling.fromMap(props.build());
			TestDeedReportGenerateEngine singleReport = new TestDeedReportGenerateEngine();
			return singleReport.generateReportFromGatling();
		} catch(Exception e) {
			throw new TestDeedValidationException(TestDeedSupportUtil.getErrorResponse("Gatling configuration error ",e.getMessage(),e.getStackTrace()));
		}
	}

	private void loadGatlingUserConfiguration(HttpServletRequest request) {
		gatlingConfiguration.setConstantUsersPerSec(new Double(TestDeedControllerUtil.isEmpty("constantUsersPerSec",request.getParameter("constantUsersPerSec"))));
		gatlingConfiguration.setDuration(TestDeedControllerUtil.isEmpty("duration",request.getParameter("duration")+" "+request.getParameter("duration_select")));
		gatlingConfiguration.setMaxDuration(TestDeedControllerUtil.isEmpty("maxDuration",request.getParameter("maxDuration")+" "+request.getParameter("maxDuration_select")));
		gatlingConfiguration.setAtOnceUsers(Integer.parseInt(TestDeedControllerUtil.isEmpty("atOnceUsers",request.getParameter("atOnceUsers"))));
		gatlingConfiguration.setRampUser(Integer.parseInt(TestDeedControllerUtil.isEmpty("rampUser",request.getParameter("rampUser"))));
		gatlingConfiguration.setRampUserOver(TestDeedControllerUtil.isEmpty("rampUserOver",request.getParameter("rampUserOver"))+" "+TestDeedControllerUtil.isEmpty("rampUserOver_select",request.getParameter("rampUserOver_select")));
		gatlingConfiguration.setRampUsersPerSec(new Double(TestDeedControllerUtil.isEmpty("rampUsersPerSec",request.getParameter("rampUsersPerSec"))));
		gatlingConfiguration.setRampUsersPerSecTo(new Double(TestDeedControllerUtil.isEmpty("rampUsersPerSecTo",request.getParameter("rampUsersPerSecTo"))));
		gatlingConfiguration.setRampUsersPerSecDuring(TestDeedControllerUtil.isEmpty("rampUsersPerSecDuring",request.getParameter("rampUsersPerSecDuring"))+" "+TestDeedControllerUtil.isEmpty("rampUsersPerSecDuring_select",request.getParameter("rampUsersPerSecDuring_select")));
		gatlingConfiguration.setNothingFor(TestDeedControllerUtil.isEmpty("nothingFor",request.getParameter("nothingFor")+" "+request.getParameter("nothingFor_select")));
		gatlingConfiguration.setStatus(Integer.parseInt(TestDeedControllerUtil.isEmpty("status",request.getParameter("status"))));
		gatlingConfiguration.setMaxResponseTime(TestDeedControllerUtil.isEmpty("maxResponseTime",request.getParameter("maxResponseTime")));
	}

}
