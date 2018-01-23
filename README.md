www.heapbrain.com
# testdeed

Requirement : JDK 1.8

1. Install scala 2.12.3 / and Scala nature IDE 

1. add dependancy
https://mvnrepository.com/artifact/com.heapbrain/testdeed
<!-- https://mvnrepository.com/artifact/com.heapbrain/testdeed -->
<dependency>
    <groupId>com.heapbrain</groupId>
    <artifactId>testdeed</artifactId>
    <version>1.3.8</version>
</dependency>

2. Clear your warnings from pom.xml (ex. managed version higher)
   Maven update and "run clean package"

4. add testdeed.properties in src/resource folder with configuration of qahost, qphost, prhost URL
   add /webapp/performance folder under /src/main - This will help to access your report from server.

5. In springboot application class
	a. @TestDeedApplication(name="Your application name")
	b. @ComponentScan(basePackages= {"com.heapbrain.core.testdeed","your_package"})
	c. In main method - TestDeedApp.load(YourSpringBootApplication.class);
  
   In controller
	a. In service - @TestDeedApi(name="Your Service name", isProdEnabled=false) - Name should be unique
	b. In method (Optional)- @TestDeedApiOperation(name="testGet", description="Return employee Name")

6. Start your server..
For performance url : <http://your_server_url>/testdeed.html

Happy stress loading :)