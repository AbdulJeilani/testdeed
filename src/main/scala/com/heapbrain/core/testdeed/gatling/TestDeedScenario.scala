package com.heapbrain.core.testdeed.gatling

import scala.collection.JavaConverters.mapAsScalaMapConverter

import com.heapbrain.core.testdeed.executor.TestDeedController

import io.gatling.core.Predef.StringBody
import io.gatling.core.Predef.checkBuilder2Check
import io.gatling.core.Predef.findCheckBuilder2ValidatorCheckBuilder
import io.gatling.core.Predef.rawFileBodies
import io.gatling.core.Predef.scenario
import io.gatling.core.Predef.stringToExpression
import io.gatling.core.Predef.value2Expression
import io.gatling.http.Predef.RawFileBodyPart
import io.gatling.http.Predef.StringBodyPart
import io.gatling.http.Predef.flushHttpCache
import io.gatling.http.Predef.flushCookieJar
import io.gatling.http.Predef.flushSessionCookies
import io.gatling.http.Predef.http
import io.gatling.http.Predef.status

@throws(classOf[Exception])
class TestDeedScenario { 

  var httpTestDeedLookup = scenario(""); 
   
  try {
	var scalaHeader = (TestDeedController.serviceMethodObject.getHeaderObj()).asScala.toMap

	println("Method : "+TestDeedController.serviceMethodObject.getMethod());
	println("Service Name : "+TestDeedController.serviceMethodObject.getServiceName());
	println("RequestBody : "+TestDeedController.serviceMethodObject.getRequestBody());
	println("ExecuteService : " + Environment.baseURL+TestDeedController.serviceMethodObject.getExecuteService())
	println("Header : " + scalaHeader)
	println("Scenario Name : " + TestDeedController.serviceMethodObject.getTestDeedName())

	if((TestDeedController.serviceMethodObject.getMethod())=="POST"){
		var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
				+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.post(TestDeedController.serviceMethodObject.getExecuteService())
				.headers(scalaHeader)
				.body(StringBody(TestDeedController.serviceMethodObject.getRequestBody())).asJSON
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

				if(TestDeedController.serviceMethodObject.getAcceptHeader()=="application/xml") {
					httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
							+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
							.post(TestDeedController.serviceMethodObject.getExecuteService())
							.headers(scalaHeader)
							.body(StringBody(TestDeedController.serviceMethodObject.getRequestBody())).asXML
							.check(status is TestDeedController.gatlingConfiguration.getStatus())
				}

		if(TestDeedController.serviceMethodObject.getAcceptHeader()=="multipart/form-data") {
			httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
					+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
					.post(TestDeedController.serviceMethodObject.getExecuteService())
					.headers(scalaHeader)
					.formUpload(TestDeedController.serviceMethodObject.getMultiPart1(),TestDeedController.serviceMethodObject.getMultiPart2())
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
				.body(StringBody(TestDeedController.serviceMethodObject.getRequestBody())).asJSON
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

				if(TestDeedController.serviceMethodObject.getAcceptHeader()=="application/xml") {
					httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
							+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
							.put(TestDeedController.serviceMethodObject.getExecuteService())
							.headers(scalaHeader)
							.body(StringBody(TestDeedController.serviceMethodObject.getRequestBody())).asXML
							.check(status is TestDeedController.gatlingConfiguration.getStatus())
				}

		if(TestDeedController.serviceMethodObject.getAcceptHeader()=="multipart/form-data") {
			httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
					+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
					.put(TestDeedController.serviceMethodObject.getExecuteService())
					.headers(scalaHeader)
					.bodyPart(RawFileBodyPart(TestDeedController.serviceMethodObject.getMultiPart1(),TestDeedController.serviceMethodObject.getMultiPart2()))
					.check(status is TestDeedController.gatlingConfiguration.getStatus())
		}  

		httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName())
				.exec(httpTestDeedService)
				.exec(flushHttpCache)
				.exec(flushCookieJar)
				.exec(flushSessionCookies)
	}

	if((TestDeedController.serviceMethodObject.getMethod())=="GET"){
		var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
				+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.get(TestDeedController.serviceMethodObject.getExecuteService())
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

				println("Gatling service : "+httpTestDeedService)
				
				httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName()) 
				.exec(httpTestDeedService)
				.exec(flushHttpCache)
				.exec(flushCookieJar)
				.exec(flushSessionCookies)
	}
  } catch {
	  case e : Exception => throw new Exception("Gatling performance Issue " + e)
	}
	
}