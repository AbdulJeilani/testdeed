package com.heapbrain.core.testdeed.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import com.heapbrain.core.testdeed.executor.TestDeedController
import scala.collection.JavaConverters._
import scala.collection.Map

class TestDeedScenario { 

	var httpTestDeedLookup = scenario(""); 
	var scalaHeader = (TestDeedController.serviceMethodObject.getHeaderObj()).asScala.toMap

			println(scalaHeader)
			println(TestDeedController.serviceMethodObject.getExecuteService())
			println(TestDeedController.serviceMethodObject.getRequestBody())
			try {
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
							.exec(flushHttpCache)
							.exec(_.set("sessionAttribute", """{
  "name" : "string",
  "empId" : "string",
  "testTOSub" : [ {
    "name" : "string"
  } ]
}"""))
							.exec(httpTestDeedService)
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
								.formUpload(TestDeedController.serviceMethodObject.getMultiPart1(),TestDeedController.serviceMethodObject.getMultiPart2())
								.check(status is TestDeedController.gatlingConfiguration.getStatus())
					}  

					httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName())
							.exec(flushHttpCache)
							.exec(httpTestDeedService)
				}

				if((TestDeedController.serviceMethodObject.getMethod())=="GET"){
					var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
							+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
							.get(TestDeedController.serviceMethodObject.getExecuteService())
							.headers(scalaHeader)
							.check(status is TestDeedController.gatlingConfiguration.getStatus())

							httpTestDeedLookup = scenario(TestDeedController.serviceMethodObject.getTestDeedName()) 
							.exec(flushHttpCache)
							.exec(httpTestDeedService)
				}
			} catch {
			case e: Exception => println(e)
			}
}