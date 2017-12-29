package com.heapbrain.core.testdeed.utility;

/**
 * @author AbdulJeilani
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

public class TestDeedReportUtil {

	public static String loadIndexHtml(File file) {
		final String[] SUFFIX = {"html"};
		StringBuilder htmlStringLoader = new StringBuilder();
		FileUtils.listFiles(file, SUFFIX, true).stream().forEach(f -> {
			try {
				if(f.getName().equals("index.html")) {
					htmlStringLoader.append(FileUtils.readFileToString(f,Charset.forName("UTF-8")));
				}
			} catch (IOException e) {
				htmlStringLoader.setLength(0);
				htmlStringLoader.append(new StringBuilder(TestDeedSupportUtil.getErrorResponse("Gatling report generation error",e.getMessage(), e.getStackTrace())));
			}
		});

		htmlStringLoader.replace(htmlStringLoader.indexOf("<link "), htmlStringLoader.indexOf("<title>"), TestDeedReportUtil.loadJS(file));
		return htmlStringLoader.toString().replace("setDetailsMenu();","//setDetailsMenu();");
	}
	
	public static String loadJS(File file) {
		StringBuilder jsStringLoader = new StringBuilder();
		final String[] SUFFIX = {"js"};
		FileUtils.listFiles(file, SUFFIX, true).stream().forEach(f -> {
			try {
				jsStringLoader.append("<script>");
				jsStringLoader.append(FileUtils.readFileToString(f,Charset.forName("UTF-8")));
				jsStringLoader.append("</script>");
			} catch (IOException e) {
				jsStringLoader.setLength(0);
				jsStringLoader.append(new StringBuilder(TestDeedSupportUtil.getErrorResponse("Gatling report generation error",e.getMessage(), e.getStackTrace())));
			}
		});
		return jsStringLoader.toString();
	}
	
	public static String loadDetailsReport(File file) {
		StringBuilder detailedReport = new StringBuilder();
		detailedReport.append("<div class=\"content-in\">");
		detailedReport.append("<script>function on() {document.getElementById(\"overlay\").style.display = \"block\";" + 
				"}function off() { document.getElementById(\"overlay\").style.display = \"none\";}" + 
				"</script><div id=\"overlay\" onclick=\"off()\">");
		final String[] SUFFIX = {"html"};
		FileUtils.listFiles(file, SUFFIX, true).stream().forEach(f -> {
			try {
				if(!f.getName().equals("index.html")) {
					detailedReport.append(FileUtils.readFileToString(f,Charset.forName("UTF-8")));
					detailedReport.replace(detailedReport.indexOf("<link "), detailedReport.indexOf("<title>"), "");
				}
			} catch (IOException e) {
				detailedReport.setLength(0);
				detailedReport.append(new StringBuilder(TestDeedSupportUtil.getErrorResponse("Gatling report generation error",e.getMessage(), 
						e.getStackTrace())));
			}
		});
		detailedReport.append("</div></div><div class=\"content-in\">");
		return detailedReport.toString().replaceAll("container_indicators", "container_indicators1").
				replace("<a href=\"index.html\">GLOBAL","<a href=\"javascript:off();\">GLOBAL").
				replace("<div class=\"item ouvert\"><a id=\"details_link\" href=\"#\">DETAILS</a></div>",
						"<div class=\"item ouvert\"><a href=\"javascript:off();\">DETAILS</a></div>").
				replaceAll("container_distrib","container_distrib1").
				replaceAll("container","container1").
				replaceAll("container_requests","container_requests1").
				replaceAll("container_responses","container_responses1").
				replace("fillStats(pageStats);", "//fillStats(pageStats);").
				replaceAll("container_response_time_dispersion","container_response_time_dispersion1");
	}
}
