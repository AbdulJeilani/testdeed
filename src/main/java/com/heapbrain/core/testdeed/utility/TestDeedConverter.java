package com.heapbrain.core.testdeed.utility;

/**
 * @author AbdulJeilani
 */

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
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

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.heapbrain.core.testdeed.engine.TestDeedServiceGenerateEngine;

public class TestDeedConverter {

	public static List<String> declaredVariableType = Arrays.asList("String","Byte","Character","Integer","Float","Double","Long","Short","int","char");
	public static List<String> collectionClass = Arrays.asList("List","Set","Queue","Collection");
	List<String> pairCollectionClass = Arrays.asList("Map");
	Map<String, Object> mapper4variable = new HashMap<String, Object>();

	@Autowired
	TestDeedUtility testDeedUtility;
	
	String requestAttributes = "";

	public TestDeedConverter() {
		mapper4variable.put("String", "string");
		mapper4variable.put("Byte", (byte)0);
		mapper4variable.put("byte", (byte)0);
		mapper4variable.put("Character",'c');
		mapper4variable.put("char", 'c');
		mapper4variable.put("Integer", 0);
		mapper4variable.put("int",0);
		mapper4variable.put("Float", 0.0f);
		mapper4variable.put("float", 0.0f);
		mapper4variable.put("Double", 0.0d);
		mapper4variable.put("double", 0.0d);
		mapper4variable.put("Long", 0l);
		mapper4variable.put("long", 0l);
	}

	public String getParmeters(String serviceName, Map<String, Object> params, String consumes) throws Exception {
		requestAttributes = "";
		List<String> requestHeader = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if(entry.getKey().equals("RequestBody")) {
				Class<?> classTemp = entry.getValue().getClass();
				String jsonValue = convertObjectToString(getClassObject(classTemp.getName()),consumes);
				if(!declaredVariableType.contains(classTemp.getSimpleName())) {
					requestAttributes += "<tr><td valign=\"top\"><font color=\"#3c495a\">Feeder (choose file)<br/>Body"
							+ "("+classTemp.getSimpleName()+")</font></td>"+
							"<td><input type=\"file\" id=\"bodyFeeder\" name=\"bodyFeeder\"/>"
							+ "<br/>"
							+ "<textarea style=\"width:240px;\" class=\"text-container\" id=\""+classTemp.getSimpleName()+"\" name=\""+classTemp.getSimpleName()+"\">"+
							jsonValue
							+"</textarea></td><td><font color=\"#3c495a\">"+entry.getKey()+"</font></td><td><font color=\"#3c495a\">"+
							classTemp.getSimpleName()+"</font></td></tr>";
				}
			} else if(entry.getKey().equals("ModelAttribute")) {
				Class<?> classTemp = entry.getValue().getClass();
				if(!declaredVariableType.contains(classTemp.getSimpleName())) {
					Field[] fields = classTemp.getDeclaredFields();
					for(Field field : fields) {
						updateURLParam(field.getName());
						requestAttributes += "<tr><td valign=\"top\"><font color=\"#3c495a\">"
								+field.getName()+ "</font></td>"+
								"<td><input size=\"35\" type=\"text\" id=\""+field.getName()+"\" name=\""+field.getName()+"\"/>"
								+"</td><td><font color=\"#3c495a\">Query(Model)</font></td>"
								+ "<td><font color=\"#3c495a\">"+field.getType().getSimpleName()+"</font></td></tr>";
					}
				}
			} else if(entry.getKey().equals("PathVariable") || entry.getKey().equals("RequestParam")
					|| entry.getKey().equals("RequestHeader")){
				if(entry.getValue() instanceof List) {
					@SuppressWarnings("unchecked")
					List<String> parametersList = (List<String>)entry.getValue();
					for(String paramsName : parametersList) {
						String[] param = paramsName.split("~");
						if(entry.getKey().equals("RequestParam")) {
							if(!param[0].startsWith("MultipartFile")) {
								updateURLParam(param[1]);
							}
						}
						if(entry.getKey().equals("RequestHeader")) {
							requestHeader.add(param[1]);
							requestAttributes += "<tr><td valign=\"top\"><font color=\"#3c495a\">"
									+param[1]+ "</font></td>"+
									"<td><input size=\"35\" type=\"text\" id=\""+param[1]+"\" name=\""+param[1]+"\"/>"
									+"</td><td><font color=\"#3c495a\">"+entry.getKey()+"</font></td>"
									+ "<td><font color=\"#3c495a\">"+param[0]+"</font></td></tr>";
						} else {
							String textField = "";
							if(param[0].startsWith("MultipartFile")) {
								textField = "<input size=\"35\" type=\"file\" id=\"multipartfile\" name=\"multipartfile\"/>";
								requestAttributes += "<input type=\"hidden\" id=\"multipartfile_object\" name=\"multipartfile_object\" value=\""+param[1]+"\"/>";
							} else {
								textField = "<input size=\"35\" type=\"text\" id=\""+param[1]+"\" name=\""+param[1]+"\"/>";
							}
							
							if(collectionClass.contains(param[0].split("&")[0])) {
								textField = "<textarea style=\"width:240px;\" class=\"text-container\" id=\""
										+param[1]+"\" name=\""+param[1]+"\"></textarea>";
							}
							requestAttributes += "<tr><td valign=\"top\"><font color=\"#3c495a\">"
									+param[1]+ "</font></td>"+
									"<td>"+textField
									+"</td><td><font color=\"#3c495a\">"+entry.getKey()+"</font></td>"
									+ "<td><font color=\"#3c495a\">"+param[0]+"</font></td></tr>";
						}
					}
				}
			}
		}
		if(!requestHeader.isEmpty()) {
			requestAttributes += "<input type=\"hidden\" id=\"requestHeader\" name=\"requestHeader\" value=\""+requestHeader+"\"/>";
		}
		return requestAttributes;
	}

	private String convertObjectToString(Object object, String consumes) {

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
						convertObjectToString(genericObject,consumes);
					} else if(pairCollectionClass.stream().anyMatch(type_name::equalsIgnoreCase)) {
						Object genericObject = getClassObject((genericType.split(",")[1]).trim());
						Map<Object, Object> map = new HashMap<>();
						m.invoke(object,map);
						convertObjectToString(genericObject,consumes);
					}

				}
			}

			if(consumes.endsWith("xml") && !object.equals("")) {
				XmlMapper mapper = new XmlMapper();
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				return mapper.writeValueAsString(object);
			} else if(!object.equals("")){
				ObjectMapper mapper = new ObjectMapper();
				mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
				mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
			}
		} catch (Exception e) {
			testDeedUtility.getErrorResponse("TestDeed error : Unable to convert object to JSON/XML"+e.getMessage());
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

	public static Object getClassObject(String className) throws Exception {
		Class<?> clazz = Class.forName(className);
		return clazz.getConstructor().newInstance(new Object[] {});
	}

	private void updateURLParam(String parameter) {
		if(TestDeedServiceGenerateEngine.updatedURL.equals("")) {
			TestDeedServiceGenerateEngine.updatedURL += "?"+parameter+"={"+parameter+"}";
		} else {
			TestDeedServiceGenerateEngine.updatedURL += "&"+parameter+"={"+parameter+"}";
		}
	}
}
