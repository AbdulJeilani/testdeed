www.heapbrain.com
# testdeed

1. add dependancy
https://mvnrepository.com/artifact/com.heapbrain/testdeed
<!-- https://mvnrepository.com/artifact/com.heapbrain/testdeed -->
<dependency>
    <groupId>com.heapbrain</groupId>
    <artifactId>testdeed</artifactId>
    <version>1.2.3</version>
</dependency>

2. add testdeed.properties in your src/resource folder to add qahost, qphost, prhost

3. In springboot class
  a. @TestDeedApplication(name="Your application name")
  b. @ComponentScan(basePackages= {"com.heapbrain.core.testdeed","your_package"})
  c. In main method - TestDeedApp.load(YourSpringBootApplication.class);

4. In controller
	a. In service - @TestDeedApi(name="Your Service name", isProdEnabled=false)
	b. In method - @TestDeedApiOperation(name="testGet", description="Return paruticular employee Name")

5. Every annotation should have value. For example
@ReqeuestParam(value="******")
@PathVariable(value="*****")
@RequestBody
@RequestHeader(value="*****") - etc...

6. Start your server..
For performance url : <http://your_server_url>/testdeed.html
Your report : <http://your_server_url>/loadrunner.html

You can save your report using brower save function. It will save complete report in single html file.

Happy testing :)