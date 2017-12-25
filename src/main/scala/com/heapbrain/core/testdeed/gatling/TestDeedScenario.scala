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
import io.gatling.http.Predef.http
import io.gatling.http.Predef.status

class TestDeedScenario { 

	var httpTestDeedLookup = scenario(""); 
	var scalaHeader = (TestDeedController.serviceMethodObject.getHeaderObj()).asScala.toMap
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
								.bodyPart(RawFileBodyPart(TestDeedController.serviceMethodObject.getMultiPart1(),TestDeedController.serviceMethodObject.getMultiPart2()))
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