package com.heapbrain.core.testdeed.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import com.heapbrain.core.testdeed.executor.TestDeedController

import scala.collection.JavaConverters._
import scala.collection.Map
import scala.util.Random
import com.fasterxml.jackson.databind.JsonNode
import com.heapbrain.core.testdeed.exception.TestDeedValidationException

@throws(classOf[Exception])
class TestDeedFeederScenario { 
  var httpTestDeedLookup = scenario(""); 
	try {
		val scalaHeader = (TestDeedController.serviceMethodObject.getHeaderObj()).asScala.toMap

				println("Method : "+TestDeedController.serviceMethodObject.getMethod())
				println("Service Name : "+TestDeedController.serviceMethodObject.getServiceName())
				println("RequestBody : "+TestDeedController.serviceMethodObject.getRequestBody())
				println("ExecuteService : " + Environment.baseURL+TestDeedController.serviceMethodObject.getExecuteService())
				var listOfInputs = List[JsonNode]()
				var listOfInputsXml = List[String]()
				var listOfInputURL = List[String]()

				if( null != TestDeedController.serviceMethodObject.getFeederRuleObj()) {
					listOfInputs = (TestDeedController.serviceMethodObject.getFeederRuleObj()).asScala.toList
				}
				if(null != TestDeedController.serviceMethodObject.getFeederRuleXMLObj()) {
					listOfInputsXml = (TestDeedController.serviceMethodObject.getFeederRuleXMLObj()).asScala.toList
				}
				if(null != TestDeedController.serviceMethodObject.getFeederInputURL()) {
					listOfInputURL = (TestDeedController.serviceMethodObject.getFeederInputURL()).asScala.toList
				}

		println("Feeder Config (JSON): "+listOfInputs)
		println("Feeder Config (XML): "+listOfInputsXml)
		println("Feeder Config (URL): "+listOfInputsXml)

		def pickRandomInput() = {
				listOfInputs(Random.nextInt(listOfInputs.size))
		}   
		def pickRandomInputXML() = {
				listOfInputsXml(Random.nextInt(listOfInputsXml.size))
		}
		def pickRandomInputURL() = {
			  listOfInputURL(Random.nextInt(listOfInputURL.size))
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
								.body(StringBody(session => s"""${pickRandomInputXML()}""")).asXML
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

		if((TestDeedController.serviceMethodObject.getMethod())=="GET"){
			var httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
				+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
				.get(StringBody(session => s"""${pickRandomInputURL()}"""))
				.headers(scalaHeader)
				.check(status is TestDeedController.gatlingConfiguration.getStatus())

			if(TestDeedController.serviceMethodObject.getAcceptHeader()=="application/xml") {
				httpTestDeedService = http(TestDeedController.serviceMethodObject.getServiceName()
					+"["+TestDeedController.serviceMethodObject.getTestDeedName()+"]")
					.get(StringBody(session => s"""${pickRandomInputURL()}"""))
					.headers(scalaHeader)
					.check(status is TestDeedController.gatlingConfiguration.getStatus())
			}

			if(TestDeedController.serviceMethodObject.getAcceptHeader()=="multipart/form-data") {
				throw new TestDeedValidationException("Gatling performance Issue : GET method not supported for multipartfile")
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
								.body(StringBody(session => s"""${pickRandomInputXML()}""")).asXML
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
	} catch {
		case e : Exception => {
			println("From Gatling : " + e.fillInStackTrace())
			throw new TestDeedValidationException("Gatling performance Issue " + e)
		}
	}
}
