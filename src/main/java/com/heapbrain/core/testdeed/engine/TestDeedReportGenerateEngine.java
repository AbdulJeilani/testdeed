package com.heapbrain.core.testdeed.engine;

/**
 * @author AbdulJeilani
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.heapbrain.core.testdeed.utility.TestDeedSupportUtil;
import com.heapbrain.core.testdeed.utility.TestDeedUtility;

public class TestDeedReportGenerateEngine {
	
	@Autowired
	public TestDeedUtility testDeedUtility;
	
	private String loadJS(File file) {
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

	private String loadStyle(File file) {
		StringBuilder styleStringLoader = new StringBuilder();
		styleStringLoader.append("<meta charset=\"UTF-8\"><style type=\"text/css\">");
		styleStringLoader.append("html * {font-size: 12px !important;" + 
				"	font-family: \"Trebuchet MS\", Helvetica, sans-serif !important;\n" + 
				"	font-style: normal !important;\n" + 
				"	-webkit-font-smoothing: antialiased !important;\n" + 
				"}");
		styleStringLoader.append("   #overlay {\n" + 
				"    position: fixed;\n" + 
				"    -webkit-transition: all .4s cubic-bezier(.25, .8, .25, 1);\n" + 
				"    transition: all .4s cubic-bezier(.25, .8, .25, 1);\n" + 
				"    display: none;\n" + 
				"    width: 100%;\n" + 
				"    height: 100%;\n" + 
				"    top: 0;\n" + 
				"    left: 0;\n" + 
				"    right: 0;\n" + 
				"    bottom: 0;\n" + 
				"    background-color: rgba(255,255,255,1);\n" + 
				"    z-index: 2;\n" + 
				"    overflow: scroll;\n" + 
				"    cursor: pointer;\n" + 
				"}\n" + 
				"\n" + 
				"#text{\n" + 
				"    position: absolute;\n" + 
				"     \n" + 
				"    \n" + 
				"    font-size: 20px;\n" + 
				"      height: 100%;\n" + 
				"    color: white;\n" + 
				" \n" + 
				"        box-sizing: border-box;\n" + 
				"    padding: 0 20px 20px;\n" + 
				"    color: #fff;\n" + 
				"}");
		final String[] SUFFIX = {"css"};
		FileUtils.listFiles(file, SUFFIX, true).stream().forEach(f -> {
			try {
				styleStringLoader.append(FileUtils.readFileToString(f,Charset.forName("UTF-8")));
			} catch (IOException e) {
				styleStringLoader.setLength(0);
				styleStringLoader.append(new StringBuilder(TestDeedSupportUtil.getErrorResponse("Gatling report generation error",e.getMessage(), e.getStackTrace())));
			}
		});
		styleStringLoader.append("</style>");
		return styleStringLoader.toString();
	}

	private String loadIndexHtml(File file) {
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

		htmlStringLoader.replace(htmlStringLoader.indexOf("<link "), htmlStringLoader.indexOf("<title>"), loadJS(file));
		return htmlStringLoader.toString().replace("setDetailsMenu();","//setDetailsMenu();");
	}

	private String loadDetailsReport(File file) {
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

	public String generateReportFromGatling() {
		
		File file = new File(System.getProperty("user.dir")+"/target/performance/reports");

		String convertGatlingToTestDeed = loadIndexHtml(file).
				replaceAll("<meta charset=\"UTF-8\">", loadStyle(file)).
				replaceAll("<div class=\"content-in\">","~AddSubReport~").
				replace("<a href=\"index.html\">GLOBAL","<a href=\"javascript:off();\">GLOBAL").
				replaceAll("<div class=\"item \"><a id=\"details_link\" href=\"#\">DETAILS</a></div>",
						"<div class=\"item \"><a href=\"javascript:on();\">DETAILS</a></div>");

		convertGatlingToTestDeed = convertGatlingToTestDeed.replaceAll("#E37400", "#3C495A").
				replaceAll("#d16b00", "#cdd3dd").
				replaceAll("#ff9916", "#3C495A").
				replaceAll("#FF9916", "#3C495A").
				replaceAll("#CF6900", "#cdd3dd");

		convertGatlingToTestDeed = 	convertGatlingToTestDeed.replace("~AddSubReport~",loadDetailsReport(file)).
				replaceAll("<div class=\"container1 details\">", "<div class=\"container details\">").
				replaceAll("<div class=\"nav\">","<div class=\"nav1\">");	

		convertGatlingToTestDeed = convertGatlingToTestDeed.replaceAll("<img alt=\"Gatling\" src=\"style/logo.png\"/>", 
				"<font>Gatling Report (Save - report [File->save])</font>").
				replaceAll("<img alt=\"Gatling\" src=\"style/logo-gatling.jpg\"/>","<font>@Gatling</font>");

		return convertGatlingToTestDeed;
	}

}