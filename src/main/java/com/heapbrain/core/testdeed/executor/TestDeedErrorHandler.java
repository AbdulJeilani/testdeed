package com.heapbrain.core.testdeed.executor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletRequestAttributes;


@RestController
public class TestDeedErrorHandler implements ErrorController {

    private static final String PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = PATH)
    String error(HttpServletRequest request, HttpServletResponse response) {
    		String errorResponse = errorAttributes.getErrorAttributes(new ServletRequestAttributes(request),false).toString();
    		errorResponse = errorResponse.replace(errorResponse.substring(errorResponse.indexOf("{timestamp="), 
    				errorResponse.indexOf(" status=")), "<font color=\"#3c495a\">");
    		errorResponse = errorResponse.replace("<!DOCTYPE html>","</font><!DOCTYPE html>");
    		errorResponse = errorResponse.replace(errorResponse.substring(errorResponse.indexOf("</html>,"), errorResponse.length()),"</html>");
        return errorResponse;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

}