# Sample: PurchaseOrder

This is a Maven project to demonstrate the unmarshalling of XML files using an XML Schema to generate the **JAXB** classes. Further, it includes examples of XJC plugins to add custom `hashCode`, `equals`, `toString` and `copyable` implentations to each generated JAXB class.

## Execution

This is a stand-alone Maven project. You can run the test using:

~~~
mvn clean test
mvn compile exec:java
~~~

The [output][2] shows the test results.

### Problem ( from [StackOverflow](https://stackoverflow.com/questions/66580186/) )

+ Is there any maven plugin for JAXB 3.0 (Jakarta EE 9)?
+ Do you know if it's possible and how to use *jaxb2-basics*?

### Solution

+ [hisrc-higherjaxb-maven-plugin][9] - Maven plugin for JAXB 4.x.
+ [hisrc-basicjaxb-plugins][10] - JAXB plugins forked from *jaxb2-basics*.

> **Note:** The source/target (release) compatibility for the above is at Java 11, up from Java 8. And, JDK 17 is used for the build. JAXB dependencies are at version 4.x.

The **JAXB** classes are generated by this plugin using this project's [pom.xml][3]

~~~
<plugin>
    <groupId>org.patrodyne.jvnet</groupId>
    <artifactId>hisrc-higherjaxb-maven-plugin</artifactId>
    <version>${hisrc-higherjaxb-maven-plugin.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <args>
                    <arg>-no-header</arg>
                    <arg>-XhashCode</arg>
                    <arg>-Xequals</arg>
                    <arg>-XtoString</arg>
                    <arg>-Xcopyable</arg>
                </args>
                <plugins>
                    <plugin>
                        <groupId>org.patrodyne.jvnet</groupId>
                        <artifactId>hisrc-basicjaxb-plugins</artifactId>
                        <version>${hisrc-basicjaxb.version}</version>
                    </plugin>
                </plugins>
            </configuration>
        </execution>
    </executions>
</plugin>
~~~

and are generated to:

~~~
target/generated-sources/xjc/
    org
    └── example
        └── po
            ├── Item.java
            ├── Items.java
            ├── ObjectFactory.java
            ├── package-info.java
            ├── PurchaseOrder.java
            └── USAddress.java
~~~

Use **JAXB** to generate Java classes from XML Schema file(s) using its **XJC** compiler. This solution uses a Maven project to invoke **XJC** using the [hisrc-higherjaxb-maven-plugin][9].

[Here][1] is a Maven project named **PurchaseOrder** using the standard file layout. This project includes:

+ An XML Schema file [PurchaseOrder.xsd][4] for the `Invoice` model
+ An XML Sample file [po.xml][5] for unmarshalling
+ A JAXB *binding* file [PurchaseOrder.xjb][6] for configuration
+ A JUnit test class [PurchaseOrderTest.java][7] to verify (un)marshalling
+ Sample XML [file(s)][8] with `purchaseOrder` data.
+ The Maven POM file with `hisrc-higherjaxb-maven-plugin`

~~~
PurchaseOrder
    src
        main
            java
            resources
                PurchaseOrder.xsd
                PurchaseOrder.xjb
                simplelogger.properties
        test
            java
                org/example/po/PurchaseOrderTest.java
            resources
                jvmsystem.arguments
                jvmsystem.properties
            samples
                po.xml
    pom.xml
~~~

#### Execution

This is a stand-alone Maven project. You can run the test using:

~~~
mvn clean test
mvn compile exec:java
~~~

##### Approach

The [hisrc-higherjaxb-maven-plugin][9] is configured to generate **JAXB** classes using the provided [PurchaseOrder.xsd][4] schema. The schema provides the namespace `"http://org.example/po"` which **JAXB** uses to create the Java `package` name using its own naming convention.

As an option, a more advanced implementation of Java's built-in `Object` methods are generated using these **XJC** [hisrc-basicjaxb-plugins][10]. In particular, the sample project uses the `toString` plugin to display *human-readable* representations of the unmarshaled `PurchaseOrder` objects.

**hisrc-basicjaxb-plugins**
~~~
<args>
    <arg>-no-header</arg>
    <arg>-XhashCode</arg>
    <arg>-Xequals</arg>
    <arg>-XtoString</arg>
    <arg>-Xcopyable</arg>
</args>
~~~

> *Note:* When the **XJC** [hisrc-basicjaxb-plugins][10] are used, the [hisrc-basicjaxb-runtime][10] dependency is required on the run-time class path.

##### Testing

The JUnit test class, [PurchaseOrderTest.java][7], scans for the sample files and invokes the method `checkSample(File sample)` to provide each file to the tester. For this project, a `JAXBContext` is created and each file in the [samples][8] path is *unmarshaled* to a `PurchaseOrder` object. When successful, each object is *marshaled* for logging and your [review][2].

##### Demonstration

A Java standard engine application with a `main(...)` method is at [`org.example.po.Main`][11]. This application is executed using:

~~~
mvn compile exec:java "src/test/samples/po.xml"
~~~

<!-- References -->

[1]: https://github.com/patrodyne/hisrc-higherjaxb/releases/download/2.1.0/hisrc-higherjaxb-sample-jaxbplugins-2.1.0-mvn-src.zip
[2]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/jaxbplugins/OUTPUT.txt
[3]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/jaxbplugins/project-pom.xml
[4]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/jaxbplugins/src/main/resources/PurchaseOrder.xsd
[5]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/jaxbplugins/src/test/samples/PurchaseOrder01-1.xml
[6]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/jaxbplugins/src/main/resources/PurchaseOrder.xjb
[7]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/jaxbplugins/src/test/java/namespace/my/jaxbplugins/PurchaseOrderTest.java
[8]: https://github.com/patrodyne/hisrc-higherjaxb/tree/master/assembly/samples/jaxbplugins/src/test/samples
[9]: https://github.com/patrodyne/hisrc-higherjaxb#readme
[10]: https://github.com/patrodyne/hisrc-basicjaxb#readme
[11]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/jaxbplugins/src/main/java/namespace/my/jaxbplugins/Aufgabe4.java
