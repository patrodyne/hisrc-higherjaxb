# Sample: Workday

This is a Maven project to demonstrate the unmarshalling (and marshaling) of XML files containing a _no namespace_ `root` element but with an import of child elements in a specific namespace. An XML Schema is used to generate the JAXB classes.

## Execution

This is a stand-alone Maven project. After unzipping it to your local folder, you can run the test using:

~~~
mvn -Ptest clean test
~~~

Or execute the sample application using:

~~~
mvn -Pexec clean compile exec:java
~~~

This [output][1] shows the test results.

### Problem (from StackOverflow [#77961018](https://stackoverflow.com/questions/77961018))

Use JAXB to add a namespace to the second level. More specifically, add the namespace `urn:com.workday/bsvc` to the second element in [this][24] XML instance. The `root` element should have _no namespace_; but, the child elements should use the given namespace. *How should the JAXB classes be annotated?*

### Solution

This [download (zip)][3] contains a stand-alone Maven project to unmarshal the given XML [workday-01.xml][24] file into JAXB objects and then marshal the objects back into XML, like this:

~~~
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<root xmlns:wd="urn:com.workday/bsvo">
    <wd:Put_Absence_Input_Request wd:version="v39.2">
        <wd:Absence_Input_Data>
            <wd:Batch_ID>Import 15022023</wd:Batch_ID>
            <wd:Worker_Reference>
                <wd:ID wd:type="Employee_ID">1741538</wd:ID>
            </wd:Worker_Reference>
            <wd:Absence_Component_Reference>
                <wd:ID wd:type="Accrual_Code">FRA_Shadow_Accrual_for_Time_Offs_Impacting_Accrual_of_Paid_Annual_Leave_CP</wd:ID>
            </wd:Absence_Component_Reference>
            <wd:Start_Date>2023-01-01</wd:Start_Date>
            <wd:End_Date>2023-01-31</wd:End_Date>
            <wd:Reference_Date>2023-01-15</wd:Reference_Date>
            <wd:Hours>2.08</wd:Hours>
            <wd:Adjustment>false</wd:Adjustment>
        </wd:Absence_Input_Data>
    </wd:Put_Absence_Input_Request>
</root>

~~~

The `root` element does not declare a namespace for itself but it does declare a namespace for the child elements.

#### Here's How

The configuration of the `"urn:com.workday/bsvo"` namespace is declared in the JAXB context using:

**`com/workday/bsvo/package-info.java`**
~~~
@jakarta.xml.bind.annotation.XmlSchema
(
    namespace = "urn:com.workday/bsvo",
    elementFormDefault = jakarta.xml.bind.annotation.XmlNsForm.QUALIFIED,
    xmlns =
    {
        @jakarta.xml.bind.annotation.XmlNs
        (
            prefix = "wd",
            namespaceURI = "urn:com.workday/bsvo")
    }
)
package com.workday.bsvo;
~~~

**Additionally**, the namespace prefix is optionally configured to be `wd:` to override the generic `ns1` prefix.

The Maven project generates the Java (JAXB) classes from these XML schemas and a binding file:

+ [src/main/resource/root.xjb][11]
+ [src/main/resource/root.xsd][12]
+ [src/main/resource/wd.xsd][13]

The [HiSrc HigherJAXB Maven Plugin][4] is used to generate the JAXB classes:

> **Note:** This is a "schema first" design pattern; but, you can use the generated Java classes as a model for a "code first" approach.

~~~
target
└── generated-sources
    └── xjc
        ├── com
        │   └── workday
        │       ├── bsvo
        │       │   ├── AbsenceComponentReference.java
        │       │   ├── AbsenceInputData.java
        │       │   ├── ID.java
        │       │   ├── ObjectFactory.java
        │       │   ├── package-info.java
        │       │   ├── PutAbsenceInputRequest.java
        │       │   └── WorkerReference.java
        │       ├── ObjectFactory.java
        │       ├── package-info.java
        │       └── Root.java
        ├── JAXBDebug.java
        └── META-INF
            └── sun-jaxb.episode
~~~

The [root.xjb][11] binding file customizes the Java package path for the namespace-less `root` classes and a child path for the `workday` classes to keep things orderly. The [wd.xsd][13] declares its own target namespace and the [root.xsd][12] intentionally omits a target namespace.

Also, the [root.xjb][11] binding file customizes the workday level `package-info.java` to use the more friendly prefix, `wd:`. The [HiSrc HyperJAXB Annox XJC Plugin][5] is used to customize the prefix. This is optional. If this customization is omitted, JAXB will declare the `"urn:com.workday/bsvo"` namespace, as desired, but the prefix will be arbitrary (i.e. `ns1`).

Also, the [HiSrc BasicJAXB XJC Plugin][6] is used to generate value specific methods for `hashCode`, `equals` and `toString`. The generation of these methods is optional but it provides a human-readable `toString` output for the sample [Main.java][10] application.

<!-- References -->

[1]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/OUTPUT.txt
[2]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/project-pom.xml
[3]: https://github.com/patrodyne/hisrc-higherjaxb/releases/download/2.2.0/hisrc-higherjaxb-sample-workday-2.2.0-mvn-src.zip
[4]: https://github.com/patrodyne/hisrc-higherjaxb#readme
[5]: https://github.com/patrodyne/hisrc-hyperjaxb-annox#readme
[6]: https://github.com/patrodyne/hisrc-basicjaxb#readme
[10]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/main/java/com/workday/bsvo/Main.java
[11]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/main/resources/root.xjb
[12]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/main/resources/root.xsd
[13]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/main/resources/wd.xsd
[20]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/test/java/com/workday/bsvo/WorkdayTest.java
[21]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/test/resources/jvmsystem.arguments
[22]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/test/resources/jvmsystem.properties
[23]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/test/resources/simplelogger.properties
[24]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/workday/src/test/samples/workday-01.xml
