package com.heapbrain.core.testdeed.utility;

/**
 * @author AbdulJeilani
 */

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heapbrain.core.testdeed.annotations.TestDeedApi;
import com.heapbrain.core.testdeed.annotations.TestDeedApiOperation;
import com.heapbrain.core.testdeed.annotations.TestDeedApplication;
import com.heapbrain.core.testdeed.executor.TestDeedController;
import com.heapbrain.core.testdeed.to.ApplicationInfo;
import com.heapbrain.core.testdeed.to.Service;
import com.heapbrain.core.testdeed.to.ServiceMethodObject;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
public class TestDeedUtility {

	public Map<String, Service> allServices = new HashMap<>();
	boolean isSwagger = false;

	public void loadClassConfig(Class<?> annotatedClass, ApplicationInfo applicationInfo) {

		RequestMapping requestMapping = annotatedClass.getDeclaredAnnotation(RequestMapping.class);
		TestDeedApi testDeedApi = annotatedClass.getDeclaredAnnotation(TestDeedApi.class);

		TestDeedApplication testDeedApplication = 
				annotatedClass.getDeclaredAnnotation(TestDeedApplication.class);

		Api api = annotatedClass.getDeclaredAnnotation(Api.class);

		if(null!= api) {
			isSwagger = true;
		}

		if(null != testDeedApi) {
			TestDeedController.isProdEnabled = testDeedApi.isProdEnabled();
			applicationInfo.setTestDeedApi(testDeedApi.name());
			applicationInfo.setSwagger(isSwagger);
			applicationInfo.setProdEnabled(testDeedApi.isProdEnabled());
		}
		if(null != requestMapping) {
			for(String baseUrl : requestMapping.value()) {
				applicationInfo.setMapping(baseUrl);
			}
		}
		if(null != testDeedApplication) {
			applicationInfo.setApplicationName(testDeedApplication.name());
		}		

	}

	public void loadMethodConfig(Class<?> annotatedClass, ApplicationInfo applicationInfo) throws Exception {
		Service service = new Service();
		for (Method method : annotatedClass.getDeclaredMethods()) {
			service.setServiceName(applicationInfo.getTestDeedApi());
			RequestMapping requestMapping = method.getDeclaredAnnotation(RequestMapping.class);
			TestDeedApiOperation testDeedApiOperation = method.getDeclaredAnnotation(TestDeedApiOperation.class);
			if(null != requestMapping && null != testDeedApiOperation) {
				ApiOperation apiOperation = method.getDeclaredAnnotation(ApiOperation.class);

				Map<String, Object> parameters = service.getParameters();
				service.setConsume(Arrays.asList("applicaton/json","application/xml","multipart/form-data"));

				Annotation[][] annotations_params = method.getParameterAnnotations();
				List<String> pathVariableList = new ArrayList<>();
				List<String> requestParam = new ArrayList<>();
				List<String> headerParam = new ArrayList<>();
				int parameterCount = 0;
				Paranamer info = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));
				for(Annotation[] annotation_params : annotations_params) {
					for(Annotation annotation_param : annotation_params) {
						if(annotation_param.annotationType().equals(PathVariable.class)) {
							requestParam.add(getGenericType(
									method.getParameters()[parameterCount].toString().split(" ")[0]
									+" "+info.lookupParameterNames(method)[parameterCount]));
							parameterCount++;
						} else if(annotation_param.annotationType().equals(RequestParam.class)) {
							requestParam.add(getGenericType(
									method.getParameters()[parameterCount].toString().split(" ")[0]
									+" "+info.lookupParameterNames(method)[parameterCount]));
							parameterCount++;
						} else if(annotation_param.annotationType().equals(RequestHeader.class)) {
							headerParam.add(getGenericType(
									method.getParameters()[parameterCount].toString().split(" ")[0]
									+" "+info.lookupParameterNames(method)[parameterCount]));
							parameterCount++;
						} else if(annotation_param.annotationType().equals(RequestBody.class)) {
							parameters.put("RequestBody", TestDeedConverter.getClassObject(method.getParameterTypes()[parameterCount].getName()));
							parameterCount++;
						} else if(annotation_param.annotationType().equals(ModelAttribute.class)) {
							parameters.put("ModelAttribute", TestDeedConverter.getClassObject(method.getParameterTypes()[parameterCount].getName()));
							parameterCount++;
						}
					}
				}
				parameters.put("PathVariable", pathVariableList);
				parameters.put("RequestParam", requestParam);
				parameters.put("RequestHeader", headerParam);

				service.setServiceMethodName(method.getName());
				if(!isSwagger && null != requestMapping) {
					if(requestMapping.consumes()!=null && requestMapping.consumes().length != 0) {
						service.setConsume(Arrays.asList(requestMapping.consumes()));
					}
				} else if(null != apiOperation) {
					if(null!=apiOperation.consumes() && !apiOperation.consumes().isEmpty()) {
						service.setConsume(Arrays.asList(apiOperation.consumes()));
					}
				} 

				if(null != requestMapping) {
					if(0 != requestMapping.value().length) {
						service.setRequestMapping(requestMapping.value()[0]);
					} if(0 != requestMapping.method().length) {
						service.setRequestMethod(requestMapping.method()[0].name());
					}
				} 

				if(null != testDeedApiOperation) {
					TestDeedController.serviceMethodObject.setTestDeedName(testDeedApiOperation.name());
					service.setServiceMethodName(testDeedApiOperation.name());
					service.setDescription(testDeedApiOperation.description());
				}
				allServices.put(service.getRequestMapping()+"~"+service.getRequestMethod(), service);
				TestDeedController.serviceMethodObjectMap.put(applicationInfo.getMapping()+service.getRequestMapping(), TestDeedController.serviceMethodObject);
				TestDeedController.serviceMethodObject = new ServiceMethodObject();
				service = new Service();
			}
		}
		applicationInfo.setServices(allServices);
	}

	public String getContentType(String serviceURL, List<String> contentTypes, String regards, boolean isPresent) {
		StringBuffer contentType = new StringBuffer();

		contentType.append("<p style=\"margin:10px;\"><font color=\"#3c495a\">Consumes&nbsp;&nbsp;</font>");
		contentType.append("<select style=\"width: 130px;\" name=\"service"+regards+"\">");
		for(String content : contentTypes) {
			if(isPresent) {
				contentType.append("<option selected value=\""+content+"\"><font color=\"#39495c\">"+content+"</font></option>");
				isPresent=false;
			} else {
				contentType.append("<option value=\""+content+"\"><font color=\"#39495c\">"+content+"</font></option>");
			}
		}
		contentType.append("</select></p>");

		return contentType.toString();
	}

	private String getGenericType(String generateValues) {
		String result = "";
		String[] generateValue = generateValues.split(" ");
		if(generateValue[0].contains("<")) {
			String[] dataTypes = generateValue[0].split("<");
			result = dataTypes[0].substring(dataTypes[0].lastIndexOf(".")+1, dataTypes[0].length())+"<";
			result += dataTypes[1].substring(dataTypes[1].lastIndexOf(".")+1, dataTypes[1].indexOf(">"))+">";
		} else {
			result = generateValue[0].substring(generateValue[0].lastIndexOf(".")+1, generateValue[0].length());
		}
		result += "~"+generateValue[1];
		return StringEscapeUtils.escapeXml(result);
	}

	public InputStream getHtmlFile(String fileName) {
		ClassLoader classLoader = TestDeedUtility.class.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}

}
