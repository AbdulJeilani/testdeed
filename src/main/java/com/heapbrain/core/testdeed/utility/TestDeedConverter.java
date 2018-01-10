package com.heapbrain.core.testdeed.utility;

/**
 * @author AbdulJeilani
 */

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
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
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.heapbrain.core.testdeed.engine.TestDeedServiceGenerateEngine;
import com.heapbrain.core.testdeed.exception.TestDeedValidationException;

public class TestDeedConverter {

	public static List<String> declaredVariableType = Arrays.asList("String","BigDecimal","Byte","Character","Integer","Float","Double","Long","Short","int","char");
	public static List<String> collectionClass = Arrays.asList("List","Set","Queue","Collection","ArrayList","HashSet","LinkedList");
	public static List<String> pairCollectionClass = Arrays.asList("Map","HashMap");
	Map<String, Object> mapper4variable = new HashMap<String, Object>();

	@Autowired
	TestDeedUtility testDeedUtility;

	String requestAttributes = "";

	public TestDeedConverter() {
		mapper4variable.put("String", "string");
		mapper4variable.put("BigDecimal", new BigDecimal("0.0"));
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
		mapper4variable.put("boolean", false);
		mapper4variable.put("short", (short) 0 );
		mapper4variable.put("Short", (short) 0);

		mapper4variable.put("StringObj", new String(""));
		mapper4variable.put("BigDecimalObj", new BigDecimal(0));
		mapper4variable.put("ByteObj", new Byte((byte)0));
		mapper4variable.put("CharacterObj", new Character('c'));
		mapper4variable.put("IntegerObj", new Integer(0));
		mapper4variable.put("FloatObj", new Float(0.0f));
		mapper4variable.put("DoubleObj", new Double(0.0d));
		mapper4variable.put("LongObj", new Long(0l));
		mapper4variable.put("ShortObj", new Short((short) 0));
	}

	public String getParmeters(String serviceName, Map<String, Object> params, String consumes, String requestMethod) throws Exception {
		requestAttributes = "";
		List<String> requestHeader = new ArrayList<String>();
		String requestBodyType = params.get("RequestBodyType").toString();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if(entry.getKey().equals("RequestBody")) {
				Class<?> classTemp = entry.getValue().getClass();
				String jsonValue = convertObjectToString(getClassObject(classTemp.getName()),consumes,requestBodyType);
				String className = classTemp.getSimpleName();
				if(requestBodyType.equals("List") || 
						requestBodyType.equals("ArrayList")) {
					className = requestBodyType+"&lt;"+classTemp.getSimpleName()+"&gt;";
				} else {
					className = classTemp.getSimpleName();
				}
				if(!declaredVariableType.contains(classTemp.getSimpleName())) {
					requestAttributes += "<tr><td valign=\"top\"><font color=\"#3c495a\">Feeder (choose file)<br/>Body"
							+ "("+className+")</font></td>"+
							"<td><input type=\"file\" id=\"bodyFeeder\" name=\"bodyFeeder\"/>"
							+ "<br/>"
							+ "<textarea style=\"width:240px;\" class=\"text-container\" id=\""+classTemp.getSimpleName()+"\" name=\""+classTemp.getSimpleName()+"\">"+
							jsonValue
							+"</textarea></td><td><font color=\"#3c495a\">"+entry.getKey()+"</font></td><td><font color=\"#3c495a\">"+
							className+"</font></td></tr>";
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
						String isRequired = "";
						if(!" ".equals(param[2])) {
							isRequired = "required";
						}

						if(entry.getKey().equals("RequestParam")) {
							if(!param[0].startsWith("MultipartFile")) {
								updateURLParam(param[1]);
							}
						}
						if(entry.getKey().equals("RequestHeader")) {
							requestHeader.add(param[1]);
							requestAttributes += "<tr><td valign=\"top\"><font color=\"#3c495a\">"
									+param[1]+ "</font></td>"+
									"<td><input size=\"35\" type=\"text\" id=\""+param[1]+"\" name=\""+param[1]+"\"" +
									" placeholder=\""+param[2]+"\" "+isRequired+" />"
									+"</td><td><font color=\"#3c495a\">"+entry.getKey()+"</font></td>"
									+ "<td><font color=\"#3c495a\">"+param[0]+"</font></td></tr>";
						} else {
							String textField = "";
							if(param[0].startsWith("MultipartFile")) {
								textField = "<input size=\"35\" type=\"file\" id=\"multipartfile\" name=\"multipartfile\""+
										" placeholder=\""+param[2]+"\" "+isRequired+" />";
								requestAttributes += "<input type=\"hidden\" id=\"multipartfile_object\" name=\"multipartfile_object\" value=\""+param[1]+"\"/>";
							} else {
								textField = "<input size=\"35\" type=\"text\" id=\""+param[1]+"\" name=\""+param[1]+"\"" +
										" placeholder=\""+param[2]+"\" "+isRequired+" />";
							}
							
							if(paramsName.contains("&") && collectionClass.contains(param[0].split("&")[0]) &&
									!requestMethod.equalsIgnoreCase("GET")) {
								if(!declaredVariableType.contains((param[0].split("&")[1]).replace("lt;", ""))) {
									textField = "<textarea style=\"width:240px;\" class=\"text-container\" id=\""
											+param[1]+"\" name=\""+param[1]+"\"></textarea>";
								}
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

	private String convertObjectToString(Object object, String consumes, String requestBodyType) {

		try {
			for(PropertyDescriptor propertyDescriptor : 
				Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors()) {
				Method m = propertyDescriptor.getWriteMethod();

				if(null != m) {
					String type = m.getGenericParameterTypes()[0].getTypeName();
					String type_name = type.substring(type.lastIndexOf(".")+1,type.length());
					String genericType = "";
					String checkDeclaredVariableType = "";
					if(type.endsWith(">")) {
						type_name = type.substring(0, type.lastIndexOf('<'));
						type_name = type_name.substring(type_name.lastIndexOf(".")+1, type_name.length());
						genericType = type.substring(type.lastIndexOf('<')+1, type.length()-1);
						checkDeclaredVariableType = genericType.substring(genericType.lastIndexOf('.')+1, genericType.length());
					}
					if(declaredVariableType.stream().anyMatch(type_name::equalsIgnoreCase) || 
							declaredVariableType.stream().anyMatch(checkDeclaredVariableType::equalsIgnoreCase)) {
						if(mapper4variable.containsKey(type_name) || mapper4variable.containsKey(checkDeclaredVariableType)) {
							if(null == mapper4variable.get(type_name)) {
								if(pairCollectionClass.stream().anyMatch(type_name::equalsIgnoreCase)) {
									Object genericObject = getClassObject((genericType.split(",")[1]).trim());
									Map<Object, Object> map = new HashMap<>();
									m.invoke(object,map);
									convertObjectToString(genericObject,consumes,requestBodyType);
								} else {
									loadCollectionObject(m, object, mapper4variable.get(checkDeclaredVariableType+"Obj"), type_name);
								}
							} else {
								m.invoke(object, mapper4variable.get(type_name));
							}
						} else {
							m.invoke(object, mapper4variable.get(type_name));
						}
					} else if(collectionClass.stream().anyMatch(type_name::equalsIgnoreCase)) {
						Object genericObject = getClassObject(genericType);
						loadCollectionObject(m, object, genericObject, type_name);
						convertObjectToString(genericObject,consumes,requestBodyType);
					} else if(pairCollectionClass.stream().anyMatch(type_name::equalsIgnoreCase)) {
						Object genericObject = getClassObject((genericType.split(",")[1]).trim());
						Map<Object, Object> map = new HashMap<>();
						m.invoke(object,map);
						convertObjectToString(genericObject,consumes,requestBodyType);
					}

				}
			}

			if(!"".equals(requestBodyType)) {
				object = TestDeedSupportUtil.loadCollectionObject(object, requestBodyType);
			}
						
			if(consumes.endsWith("xml") && !object.equals("")) {
				JacksonXmlModule xmlModule = new JacksonXmlModule();
				xmlModule.setDefaultUseWrapper(false);
				ObjectMapper mapper = new XmlMapper(xmlModule);
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				return mapper.writeValueAsString(object);
			} else if(!object.equals("")){
				ObjectMapper mapper = new ObjectMapper();
				mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
				mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
			}
		} catch (Exception e) {
			throw new TestDeedValidationException("TestDeed error : Unable to convert sample JSON object", e);
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
