www.heapbrain.com
# testdeed

Requirement : JDK 1.8

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
	a. In service - @TestDeedApi(name="Your Service name", isProdEnabled=false) - Name should be unique
	b. In method (Optional)- @TestDeedApiOperation(name="testGet", description="Return employee Name")

5. Start your server..
For performance url : <http://your_server_url>/testdeed.html
Your report : <http://your_server_url>/loadrunner.html

You can save your report using browser save function. It will save complete report in single html file.

Happy stress loading :)