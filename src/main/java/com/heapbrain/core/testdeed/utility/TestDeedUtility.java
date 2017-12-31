package com.heapbrain.core.testdeed.utility;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.SpringBootApplication;
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

	public String loadClassConfig(Class<?> annotatedClass, ApplicationInfo applicationInfo) {

		String requestMappingClassLevel = "";
		
		TestDeedApi testDeedApi = annotatedClass.getDeclaredAnnotation(TestDeedApi.class);
		TestDeedApplication testDeedApplication = 
				annotatedClass.getDeclaredAnnotation(TestDeedApplication.class);

		if(null != testDeedApi) {
			RequestMapping requestMapping = annotatedClass.getDeclaredAnnotation(RequestMapping.class);
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
					requestMappingClassLevel = baseUrl;
				}
			}
		}
		if(null != testDeedApplication) {
			applicationInfo.setApplicationName(testDeedApplication.name());
		}
		return requestMappingClassLevel;
	}

	public void loadMethodConfig(Class<?> annotatedClass, ApplicationInfo applicationInfo, String requestMappingClassLevel) throws Exception {
		Service service = new Service();
		if(null == annotatedClass.getAnnotation(SpringBootApplication.class)) {
			for (Method method : annotatedClass.getDeclaredMethods()) {
				service.setServiceName(applicationInfo.getTestDeedApi());
				RequestMapping requestMapping = method.getDeclaredAnnotation(RequestMapping.class);
				TestDeedApiOperation testDeedApiOperation = method.getDeclaredAnnotation(TestDeedApiOperation.class);
				if(null != requestMapping) {
					ApiOperation apiOperation = method.getDeclaredAnnotation(ApiOperation.class);

					Map<String, Object> parameters = service.getParameters();
					parameters.put("RequestBodyType", "");
					service.setConsume(Arrays.asList("application/json","application/xml","multipart/form-data"));

					Annotation[][] annotations_params = method.getParameterAnnotations();
					List<String> pathVariableList = new ArrayList<>();
					List<String> requestParam = new ArrayList<>();
					List<String> headerParam = new ArrayList<>();
					int parameterCount = 0;
					Paranamer info = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));
					for(Annotation[] annotation_params : annotations_params) {
						for(Annotation annotation_param : annotation_params) {
							if(annotation_param.annotationType().equals(PathVariable.class)) {
								requestParam.add(TestDeedSupportUtil.getGenericType(
										method.getParameters()[parameterCount].toString().split(" ")[0]
												+" "+info.lookupParameterNames(method)[parameterCount]));
								parameterCount++;
							} else if(annotation_param.annotationType().equals(RequestParam.class)) {
								requestParam.add(TestDeedSupportUtil.getGenericType(
										method.getParameters()[parameterCount].toString().split(" ")[0]
												+" "+info.lookupParameterNames(method)[parameterCount]));
								parameterCount++;
							} else if(annotation_param.annotationType().equals(RequestHeader.class)) {
								headerParam.add(TestDeedSupportUtil.getGenericType(
										method.getParameters()[parameterCount].toString().split(" ")[0]
												+" "+info.lookupParameterNames(method)[parameterCount]));
								parameterCount++;
							} else if(annotation_param.annotationType().equals(RequestBody.class)) {
								String requestBody = method.getParameters()[parameterCount].toString().split(" ")[0];//method.getParameterTypes()[parameterCount].getName();
								String collectionType = "";
								if(requestBody.contains("<")) {
									Pattern pattern = Pattern.compile("([.a-zA-z]+)*\\<([^\\}]+)*\\>");
									Matcher matcher = pattern.matcher(requestBody);
									while(matcher.find()) {
										collectionType = matcher.group(1).substring(matcher.group(1).lastIndexOf(".")+1, matcher.group(1).length());
										requestBody = matcher.group(2);
									}
									if(TestDeedConverter.collectionClass.stream().anyMatch(collectionType::equalsIgnoreCase) ||
											TestDeedConverter.pairCollectionClass.stream().anyMatch(collectionType::equalsIgnoreCase)) {
										parameters.put("RequestBodyType", collectionType);
									}
								}
									parameters.put("RequestBody", TestDeedConverter.getClassObject(requestBody));
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
					} else if(null == testDeedApiOperation) {
						TestDeedController.serviceMethodObject.setTestDeedName(method.getName());
						service.setServiceMethodName(method.getName());
						service.setDescription(method.getName()+" description unavailable");
					}
					service.setRequestMappingClassLevel(requestMappingClassLevel);
					allServices.put(service.getRequestMapping()+"~"+service.getRequestMethod()+"::"+annotatedClass.getName(), service);
					TestDeedController.serviceMethodObjectMap.put(requestMappingClassLevel+service.getRequestMapping(), TestDeedController.serviceMethodObject);
					TestDeedController.serviceMethodObject = new ServiceMethodObject();
					service = new Service();
				}
			}
			applicationInfo.setServices(allServices);
		}
	}

	public InputStream getHtmlFile(String fileName) {
		ClassLoader classLoader = TestDeedUtility.class.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}

}
