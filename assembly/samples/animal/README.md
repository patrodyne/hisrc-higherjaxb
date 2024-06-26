# Sample: Animal

This is a Maven project to demonstrate the unmarshalling of XML files using an XML Schema to generate the JAXB classes.

## Execution

This is a stand-alone Maven project. You can run the test using:

~~~
mvn -Ptest clean test
~~~

The [output][6] shows the test results.

### Problem

Use the *HiSrc HigherJAXB Maven Plugin* to generate JAXB classes from an XML Schema.

### Solution

Create a Maven project named **Animal** using the standard file layout. This project includes:

+ An XML Schema file [animal.xsd][2] for the `Animal` model
+ A JAXB *binding* file [animal.xjb][3] for customizations
+ A JUnit test class to demonstrate (un)marshalling
+ Sample XML files with `Animal` data.
+ The Maven POM file with `hisrc-higherjaxb-maven-plugin`

~~~
Animal
    src
        main
            java
            resources
                animal.xjb
                animal.xsd
        test
            java
                org/example/animal/AnimalTest.java
            resources
                simplelogger.properties
            samples
                Animal101.xml
                Animal102.xml
    pom.xml
~~~

The JAXB classes are generated by this plugin in this project's [pom.xml][1]

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
        </execution>
    </executions>
</plugin>
~~~

and are generated to:

~~~
target/generated-sources/xjc/
    org.example.animal
        Animal.java
        Species.java
        ObjectFactory.java
~~~

#### Testing

The JUnit test class, [AnimalTest][4], scans for the sample files and invokes the method `checkSample(File sample)` to provide each file to the tester. For this project, a `JAXBContext` is created and each file in the [samples][5] path is *unmarshaled* to an `animal` object. When successful, the `animal` object is *marshaled* for logging and your review.

<!-- References -->

[1]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/animal/project-pom.xml
[2]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/animal/src/main/resources/animal.xsd
[3]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/animal/src/main/resources/animal.xjb
[4]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/animal/src/test/java/org/example/animal/AnimalTest.java
[5]: https://github.com/patrodyne/hisrc-higherjaxb/tree/master/assembly/samples/animal/src/test/samples
[6]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/animal/OUTPUT.txt


