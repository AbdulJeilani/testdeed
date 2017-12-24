package com.heapbrain.core.testdeed.utility;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
				for(Annotation[] annotation_params : annotations_params) {
					for(Annotation annotation_param : annotation_params) {
						if(annotation_param.annotationType().equals(PathVariable.class)) {
							if(!"".equals(((PathVariable) annotation_param).value())) {
								pathVariableList.add(getGenericType(method, method.getParameterTypes()[parameterCount].getSimpleName())+"~"
										+((PathVariable) annotation_param).value());
								parameterCount++;
							}
						} else if(annotation_param.annotationType().equals(RequestParam.class)) {
							if(!"".equals(((RequestParam) annotation_param).value())) {
								requestParam.add(getGenericType(method, method.getParameterTypes()[parameterCount].getSimpleName())+"~"
										+((RequestParam) annotation_param).value());
								parameterCount++;
							}
						} else if(annotation_param.annotationType().equals(RequestHeader.class)) {
							if(!"".equals(((RequestHeader) annotation_param).value())) {
								headerParam.add(getGenericType(method, method.getParameterTypes()[parameterCount].getSimpleName())+"~"
										+((RequestHeader) annotation_param).value());
								parameterCount++;
							}
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

	private String getGenericType(Method method, String variableName) {
		if(TestDeedConverter.collectionClass.contains(variableName)) {
			Type returnType = method.getGenericReturnType();
			if(null != returnType) {
				Class<?> typeArgClass = (Class<?>) returnType;
				variableName += "&lt;"+typeArgClass.getSimpleName()+"&gt;";
			}
		}
		return variableName;
	}
	
	public InputStream getHtmlFile(String fileName) {
		ClassLoader classLoader = TestDeedUtility.class.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}

	public String getErrorResponse(String errorDescription) {
		try {
			return IOUtils.toString(getHtmlFile("testdeedexception.html"), 
					Charset.forName("UTF-8")).replace("~testdeedexception~", errorDescription);
		} catch (IOException e) {
			return "Unavailable error response";
		}
	}
}
