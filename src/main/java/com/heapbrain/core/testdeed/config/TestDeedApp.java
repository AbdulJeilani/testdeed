package com.heapbrain.core.testdeed.config;

/**
 * @author AbdulJeilani
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.heapbrain.core.testdeed.common.Constant;
import com.heapbrain.core.testdeed.exception.TestDeedValidationException;
import com.heapbrain.core.testdeed.executor.TestDeedController;

@Configuration
@ComponentScan(basePackages = {"com.heapbrain.core.testdeed"})
public class TestDeedApp {

	public final static List<String> serverDetails = Arrays.asList("qahost","qphost","prhost");

	public static void load(Class<?> superClass) {
		TestDeedController.basePackage = superClass.getPackage().getName();
		List<String> listOfServers = new ArrayList<>();
		try {
			Properties prop = new Properties();
			InputStream in = superClass.getClass().getResourceAsStream("/testdeed.properties");

			prop.load(in);
			for(String s : serverDetails) {
				if(null!=s && !s.equals("")) {
					listOfServers.add(prop.getProperty(s));
					if(s.equals("prhost")) {
						TestDeedController.prHost = prop.getProperty(s);
					}
				}
			}
			in.close();
		}  catch (IOException | NullPointerException e) {
			throw new TestDeedValidationException(Constant.CONFIGURATION_ERROR +" testdeed.properties configuration file missing.", e);
		}
		TestDeedController.serverHosts = listOfServers;	
		TestDeedController.reportPath = System.getProperty("user.dir")+"/target/performance/reports";
	}
}