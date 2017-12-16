package com.heapbrain.core.testdeed.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import com.heapbrain.core.testdeed.executor.TestDeedController

class TestDeedScenario { 

	var httpTestDeedLookup = scenario(""); 

	if((TestDeedController.serviceMethodObject.getMethod())=="POST"){
		var httpTestDeedService = http(TestDeedController.testDeedControllerName
				+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.post(TestDeedController.serviceMethodObject.getExecuteService())
				.body(StringBody(TestDeedController.serviceMethodObject.getRequestBody())).asJSON
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

				if(TestDeedController.serviceMethodObject.getAcceptHeader()=="application/xml") {
					httpTestDeedService = http(TestDeedController.testDeedControllerName
							+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
							.post(TestDeedController.serviceMethodObject.getExecuteService())
							.body(StringBody(TestDeedController.serviceMethodObject.getRequestBody())).asXML
							.check(status is TestDeedController.gatlingConfiguration.getStatus())
				}

		httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName())
				.exec(flushHttpCache)
				.exec(httpTestDeedService)
	} 
	if((TestDeedController.serviceMethodObject.getMethod())=="GET"){
		var httpTestDeedService = http(TestDeedController.testDeedControllerName
				+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.get(TestDeedController.serviceMethodObject.getExecuteService())
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

				httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName()) 
				.exec(flushHttpCache)
				.exec(httpTestDeedService)
	}

}