package com.heapbrain.core.testdeed.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import com.heapbrain.core.testdeed.executor.TestDeedController
import scala.collection.JavaConverters._
import scala.collection.Map
import scala.util.Random

class TestDeedFeederScenario { 

	var httpTestDeedLookup = scenario(""); 
	var scalaHeader = (TestDeedController.serviceMethodObject.getHeaderObj()).asScala.toMap

			println("Method : "+TestDeedController.serviceMethodObject.getMethod())
			println("Service Name : "+TestDeedController.serviceMethodObject.getServiceName())
			println("RequestBody : "+TestDeedController.serviceMethodObject.getRequestBody())
			println("ExecuteService : " + TestDeedController.serviceMethodObject.getExecuteService())
			val listOfInputs = (TestDeedController.serviceMethodObject.getFeederRuleObj()).asScala.toList
			println("Feeder Config : "+listOfInputs)
			def pickRandomInput() = {
					listOfInputs(Random.nextInt(listOfInputs.size))
	}

	if((TestDeedController.serviceMethodObject.getMethod())=="POST"){
		var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
				+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.post(TestDeedController.serviceMethodObject.getExecuteService())
				.headers(scalaHeader)
				.body(StringBody(session => s"""${pickRandomInput()}""")).asJSON
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

				if(TestDeedController.serviceMethodObject.getAcceptHeader()=="application/xml") {
					httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
							+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
							.post(TestDeedController.serviceMethodObject.getExecuteService())
							.headers(scalaHeader)
							.body(StringBody(session => s"""${pickRandomInput()}""")).asXML
							.check(status is TestDeedController.gatlingConfiguration.getStatus())
				}

		if(TestDeedController.serviceMethodObject.getAcceptHeader()=="multipart/form-data") {
			httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
					+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
					.post(TestDeedController.serviceMethodObject.getExecuteService())
					.headers(scalaHeader)
					.body(StringBody(session => s"""${pickRandomInput()}""")).asJSON
					.check(status is TestDeedController.gatlingConfiguration.getStatus())
		}

		httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName())
				.exec(httpTestDeedService)
				.exec(flushHttpCache)
				.exec(flushCookieJar)
				.exec(flushSessionCookies)
	}

	if((TestDeedController.serviceMethodObject.getMethod())=="PUT"){
		var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
				+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.put(TestDeedController.serviceMethodObject.getExecuteService())
				.headers(scalaHeader)
				.body(StringBody(session => s"""${pickRandomInput()}""")).asJSON
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

				if(TestDeedController.serviceMethodObject.getAcceptHeader()=="application/xml") {
					httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
							+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
							.put(TestDeedController.serviceMethodObject.getExecuteService())
							.headers(scalaHeader)
							.body(StringBody(session => s"""${pickRandomInput()}""")).asXML
							.check(status is TestDeedController.gatlingConfiguration.getStatus())
				}

		if(TestDeedController.serviceMethodObject.getAcceptHeader()=="multipart/form-data") {
			httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
					+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
					.put(TestDeedController.serviceMethodObject.getExecuteService())
					.headers(scalaHeader)
					.body(StringBody(session => s"""${pickRandomInput()}""")).asJSON
					.check(status is TestDeedController.gatlingConfiguration.getStatus())
		}

		httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName())
				.exec(httpTestDeedService)
				.exec(flushHttpCache)
				.exec(flushCookieJar)
				.exec(flushSessionCookies)
	}
}
