package com.heapbrain.core.testdeed.utility;

/**
 * @author AbdulJeilani
 */

import java.io.File;

import org.apache.commons.io.FileUtils;

public class TestDeedReportUtil {

	public static String loadIndexHtml(File file) {
		final String[] SUFFIX = {"html"};
		StringBuilder htmlStringLoader = new StringBuilder();
		FileUtils.listFiles(file, SUFFIX, true).stream().forEach(f -> {
			try {
				if(f.getName().equals("index.html")) {
					htmlStringLoader.append("performance"+f.getAbsolutePath().split("performance")[1]);
				}
			} catch (Exception e) {
				htmlStringLoader.setLength(0);
				htmlStringLoader.append(new StringBuilder(TestDeedSupportUtil.getErrorResponse("Gatling report generation error",e.getMessage(), e.getStackTrace())));
			}
		});

		return htmlStringLoader.toString();
	}
}
