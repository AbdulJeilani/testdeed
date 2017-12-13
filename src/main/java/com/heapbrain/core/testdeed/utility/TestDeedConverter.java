package com.heapbrain.core.testdeed.utility;

/**
 * @author AbdulJeilani
 */

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestDeedConverter {

	public static List<String> declaredVariableType = new ArrayList<String>();
	List<String> collectionClass = new ArrayList<String>();
	List<String> pairCollectionClass = new ArrayList<String>();
	Map<String, Object> mapper4variable = new HashMap<String, Object>();

	String requestAttributes = "";

	public TestDeedConverter() {
		declaredVariableType = Arrays.asList("String","Byte","Character","Integer","Float","Double","Long","Short","int","char");
		collectionClass = Arrays.asList("List","Set","Queue","Collection");
		pairCollectionClass = Arrays.asList("Map");

		mapper4variable.put("String", "string");
		mapper4variable.put("Byte", (byte)0);
		mapper4variable.put("byte", (byte)0);
		mapper4variable.put("Character",'c');
		mapper4variable.put("char", 'c');
		mapper4variable.put("Integer", 0);
		mapper4variable.put("int",0);
		mapper4variable.put("Float", 0.0);
		mapper4variable.put("float", 0.0);
		mapper4variable.put("Double", 0);
		mapper4variable.put("Double", 0);
		mapper4variable.put("Long", 0l);
		mapper4variable.put("long", 0l);
	}

	public String getParmeters(String serviceName, Map<String, Object> params) throws Exception {
		requestAttributes = "";
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if(entry.getKey().equals("RequestBody")) {
				Class<?> [] inputValues = (Class<?>[]) entry.getValue();
				for(Class<?> classTemp : inputValues) {
					if(!declaredVariableType.contains(classTemp.getSimpleName())) {
						requestAttributes += "<tr><td width=\"15%%\" height=\"20px\" valign=\"top\">Body"
								+ "("+classTemp.getSimpleName()+")</td>"+
		                "<td width=\"85%\" height=\"20px\"><textarea class=\"text-container\" id=\""+classTemp.getSimpleName()+"\" name=\""+classTemp.getSimpleName()+"\">"+
		                convertObjectToJson(getClassObject(classTemp.getName()))
	                    +"</textarea></td></tr>";
					}
				}
			} else {
				if(entry.getValue() instanceof List) {
					@SuppressWarnings("unchecked")
					List<String> parametersList = (List<String>)entry.getValue();
					for(String paramsName : parametersList) {
						requestAttributes += "<tr><td width=\"15%\" height=\"20px\" valign=\"top\">"
								+paramsName+ "</td>"+
		                "<td width=\"85%\" height=\"20px\"><input size=\"35\" type=\"text\" id=\""+paramsName+"\" name=\""+paramsName+"\"></td></tr>";
					}
				}
				
				
				
			}
		}
		return requestAttributes;
	}

	private String convertObjectToJson(Object object) {

		try {

			for(PropertyDescriptor propertyDescriptor : 
				Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors()) {
				Method m = propertyDescriptor.getWriteMethod();

				if(null != m) {
					String type = m.getGenericParameterTypes()[0].getTypeName();
					String type_name = type.substring(type.lastIndexOf(".")+1,type.length());
					String genericType = "";
					if(type.endsWith(">")) {
						type_name = type.substring(0, type.lastIndexOf('<'));
						type_name = type_name.substring(type_name.lastIndexOf(".")+1, type_name.length());
						genericType = type.substring(type.lastIndexOf('<')+1, type.length()-1);
					}
					if(declaredVariableType.stream().anyMatch(type_name::equalsIgnoreCase)) {
						if(mapper4variable.containsKey(type_name)) {
							m.invoke(object, mapper4variable.get(type_name));
						} else {
							m.invoke(object, 0);
						}
					} else if(collectionClass.stream().anyMatch(type_name::equalsIgnoreCase)) {
						Object genericObject = getClassObject(genericType);
						loadCollectionObject(m, object, genericObject, type_name);
						convertObjectToJson(genericObject);
					} else if(pairCollectionClass.stream().anyMatch(type_name::equalsIgnoreCase)) {
						Object genericObject = getClassObject((genericType.split(",")[1]).trim());
						String key = (genericType.split(",")[0]).toLowerCase();
						Map<Object, Object> map = new HashMap<>();
						map.put(key.substring(key.lastIndexOf(".")+1, key.length()), genericObject);
						m.invoke(object,map);
						convertObjectToJson(genericObject);
					}

				}
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private void loadCollectionObject(Method m, Object object, Object genericObject, String type_name) throws Exception {
		switch(type_name) {
		case "List":
			List<Object> list = new ArrayList<>();
			list.add(genericObject);
			m.invoke(object,list);
			break;
		case "Set":
			Set<Object> set = new HashSet<>();
			set.add(genericObject);
			m.invoke(object,set);
			break;
		case "Queue":
			Queue<Object> queue = new LinkedList<>();
			queue.add(genericObject);
			m.invoke(object,queue);
			break;
		case "Collection":
			Collection<Object> collection = new ArrayList<>();
			collection.add(genericObject);
			m.invoke(object,collection);
			break;
		}
	}

	private Object getClassObject(String className) throws Exception {
		Class<?> clazz = Class.forName(className);
		return clazz.getConstructor().newInstance(new Object[] {});
	}

}
