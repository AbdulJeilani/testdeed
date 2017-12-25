package com.heapbrain.core.testdeed.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import com.heapbrain.core.testdeed.executor.TestDeedController
import scala.collection.JavaConverters._
import scala.collection.Map
import scala.util.Random
import io.gatling.core.feeder.FeederSupport
import io.gatling.core.feeder.FeederBuilder

class TestDeedFeederScenario { 

	var httpTestDeedLookup = scenario(""); 
	var scalaHeader = (TestDeedController.serviceMethodObject.getHeaderObj()).asScala.toMap
	val randomElementFeeder = jsonFile("feeder.json").circular.random
	println(randomElementFeeder);
	println(TestDeedController.serviceMethodObject.getMultiPart1())
	println(TestDeedController.serviceMethodObject.getMultiPart2())
	
	if((TestDeedController.serviceMethodObject.getMethod())=="POST"){
	var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
			+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
			.post(TestDeedController.serviceMethodObject.getExecuteService())
			.headers(scalaHeader)
			.body(StringBody(TestDeedController.serviceMethodObject.getFeederRuleObj())).asJSON
			.check(status is TestDeedController.gatlingConfiguration.getStatus())

			if(TestDeedController.serviceMethodObject.getAcceptHeader()=="multipart/form-data") {
				http(TestDeedController.serviceMethodObject.getServiceName()
						+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.post(TestDeedController.serviceMethodObject.getExecuteService())
				.headers(scalaHeader)
				.body(StringBody(TestDeedController.serviceMethodObject.getFeederRuleObj())).asJSON
				.check(status is TestDeedController.gatlingConfiguration.getStatus())
			}

	httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName())
			.feed(randomElementFeeder)
			.exec(flushHttpCache)
			.exec(httpTestDeedService)
	}
	
	if((TestDeedController.serviceMethodObject.getMethod())=="PUT"){
	  var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
			+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
			.put(TestDeedController.serviceMethodObject.getExecuteService())
			.headers(scalaHeader)
			.body(StringBody(TestDeedController.serviceMethodObject.getFeederRuleObj())).asJSON
			.check(status is TestDeedController.gatlingConfiguration.getStatus())

			if(TestDeedController.serviceMethodObject.getAcceptHeader()=="multipart/form-data") {
				http(TestDeedController.serviceMethodObject.getServiceName()
						+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.put(TestDeedController.serviceMethodObject.getExecuteService())
				.headers(scalaHeader)
				.body(StringBody(TestDeedController.serviceMethodObject.getFeederRuleObj())).asJSON
				.check(status is TestDeedController.gatlingConfiguration.getStatus())
			}

	httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName())
			.feed(randomElementFeeder)
			.exec(flushHttpCache)
			.exec(httpTestDeedService)
	}
}
