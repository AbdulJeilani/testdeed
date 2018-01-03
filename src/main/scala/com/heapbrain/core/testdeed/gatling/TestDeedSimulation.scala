package com.heapbrain.core.testdeed.gatling

import com.heapbrain.core.testdeed.executor.TestDeedController
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

@throws(classOf[Exception])
class TestDeedSimulation extends Simulation {
	try {
		val httpConf = http.baseURL(Environment.baseURL).disableWarmUp
				var testDeedScenario =  new TestDeedScenario()

				var maxResponseTime = scala.util.Properties.envOrElse("maxResponseTime", 
						TestDeedController.gatlingConfiguration.getMaxResponseTime)

				var testDeedSimulation = List(
						testDeedScenario.httpTestDeedLookup.inject(
								nothingFor(Duration.apply(TestDeedController.gatlingConfiguration.getNothingFor()).asInstanceOf[FiniteDuration]),
								atOnceUsers(TestDeedController.gatlingConfiguration.getAtOnceUsers()),
								rampUsers(TestDeedController.gatlingConfiguration.getRampUser()) over (
										Duration.apply(TestDeedController.gatlingConfiguration.getRampUserOver()).asInstanceOf[FiniteDuration]),
								rampUsersPerSec(TestDeedController.gatlingConfiguration.getRampUsersPerSec()) 
								to TestDeedController.gatlingConfiguration.getRampUsersPerSecTo() during (
										Duration.apply(TestDeedController.gatlingConfiguration.getRampUsersPerSecDuring()).asInstanceOf[FiniteDuration]),
								constantUsersPerSec(TestDeedController.gatlingConfiguration.getConstantUsersPerSec()) during (
										Duration.apply(TestDeedController.gatlingConfiguration.getDuration()).asInstanceOf[FiniteDuration]
										)
								))
				setUp(testDeedSimulation)
				.maxDuration(Duration.apply(TestDeedController.gatlingConfiguration.getMaxDuration()).asInstanceOf[FiniteDuration])
				.protocols(httpConf)
				.assertions(global.responseTime.percentile3.lte(maxResponseTime.toInt))
	} catch {
	case e : Exception => throw new Exception("Gatling performance Issue " + e)
	}
}