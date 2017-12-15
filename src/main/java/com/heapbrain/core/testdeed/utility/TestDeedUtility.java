package com.heapbrain.core.testdeed.utility;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heapbrain.core.testdeed.annotations.TestDeedApi;
import com.heapbrain.core.testdeed.annotations.TestDeedApiOperation;
import com.heapbrain.core.testdeed.annotations.TestDeedApplication;
import com.heapbrain.core.testdeed.executor.TestDeedController;
import com.heapbrain.core.testdeed.to.ApplicationInfo;
import com.heapbrain.core.testdeed.to.Service;
import com.heapbrain.core.testdeed.to.ServiceMethodObject;

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
		if(null != testDeedApi) {
			isSwagger = testDeedApi.isSwaggerEnabled();
			TestDeedController.isProdEnabled = testDeedApi.isProdEnabled();
			applicationInfo.setTestDeedApi(testDeedApi.name());
			TestDeedController.testDeedControllerName = testDeedApi.name();
			applicationInfo.setDescription(testDeedApi.description());
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

	public void loadMethodConfig(Class<?> annotatedClass, ApplicationInfo applicationInfo) {
		Service service = new Service();

		for (Method method : annotatedClass.getDeclaredMethods()) {
			RequestMapping requestMapping = method.getDeclaredAnnotation(RequestMapping.class);
			if(requestMapping != null) {
				TestDeedApiOperation testDeedApiOperation = method.getDeclaredAnnotation(TestDeedApiOperation.class);
				ApiOperation apiOperation = method.getDeclaredAnnotation(ApiOperation.class);

				int parameterCount =  0;

				Map<String, Object> parameters = service.getParameters();
				service.setConsume(Arrays.asList("application/xml","applicaton/json"));

				Annotation[][] annotations_params = method.getParameterAnnotations();
				List<String> pathVariableList = new ArrayList<>();
				List<String> requestParam = new ArrayList<>();

				for(Annotation[] annotation_params : annotations_params) {
					for(Annotation annotation_param : annotation_params) {
						if(annotation_param.annotationType().equals(PathVariable.class)) {
							pathVariableList.add(method.getParameterTypes()[parameterCount].getSimpleName()+"~"
									+((PathVariable) annotation_param).value());
							parameterCount++;
						}
						if(annotation_param.annotationType().equals(RequestParam.class)) {
							requestParam.add(method.getParameterTypes()[parameterCount].getSimpleName()+"~"
									+((RequestParam) annotation_param).value());
							parameterCount++;
						}
						if(annotation_param.annotationType().equals(RequestBody.class)) {
							parameters.put("RequestBody", method.getParameterTypes());
						}
					}
				}
				parameters.put("PathVariable", pathVariableList);
				parameters.put("RequestParam", requestParam);

				if(pathVariableList.isEmpty() && requestParam.isEmpty()) {
					Parameter[] nonAnnotationParameters = method.getParameters();
					List<String> commonParams = new ArrayList<>();
					for (Parameter parameter : nonAnnotationParameters) {
						commonParams.add(parameter.getParameterizedType()+"~"+parameter.getName());
					}
					parameters.put("NoParameterType", commonParams);
				}

				service.setServiceName(method.getName());
				if(!isSwagger && null != requestMapping) {
					if(requestMapping.consumes()!=null && requestMapping.consumes().length != 0) {
						service.setConsume(Arrays.asList(requestMapping.consumes()));
					}
				} else if(null != apiOperation) {
					if(null!=apiOperation.consumes() && !apiOperation.consumes().isEmpty()) {
						service.setConsume(Arrays.asList(apiOperation.consumes()));
					} if(null!=apiOperation.produces() && !apiOperation.produces().isEmpty()) {
						service.setConsume(Arrays.asList(apiOperation.produces()));
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
					service.setServiceName(testDeedApiOperation.name());
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

		contentType.append("<p style=\"margin:10px;\"><font color=\"#39495c\">Content Type</font>");
		contentType.append("<select name=\"service"+regards+"\">");
		contentType.append("<option value=\""+regards+"\"><font color=\"#39495c\">"+regards+"</font></option>");
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

	public InputStream getHtmlFile(String fileName) {
		ClassLoader classLoader = TestDeedUtility.class.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}
}
