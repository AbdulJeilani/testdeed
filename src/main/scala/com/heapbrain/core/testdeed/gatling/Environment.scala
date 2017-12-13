package com.heapbrain.core.testdeed.gatling

import com.heapbrain.core.testdeed.executor.TestDeedController

object Environment {
	val baseURL = scala.util.Properties.envOrElse("baseURL", TestDeedController.serviceMethodObject.getBaseURL) 
}