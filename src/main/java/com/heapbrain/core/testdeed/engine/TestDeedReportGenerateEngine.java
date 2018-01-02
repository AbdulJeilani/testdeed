package com.heapbrain.core.testdeed.engine;

/**
 * @author AbdulJeilani
 */

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import com.heapbrain.core.testdeed.utility.TestDeedReportUtil;
import com.heapbrain.core.testdeed.utility.TestDeedUtility;

public class TestDeedReportGenerateEngine {
	
	@Autowired
	public TestDeedUtility testDeedUtility;

	public String generateReportFromGatling() {
		
		File file = new File(System.getProperty("user.dir")+"/src/main/webapp/performance/reports");
		return TestDeedReportUtil.loadIndexHtml(file);
	}

}