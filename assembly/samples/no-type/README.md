# Sample: No-Type on Element
## Can Element be bound to String?

This is a Maven project to demonstrate how JAXB handles an XML schema file containing attributes and elements with no XML type specified.

This project generates Java classes from XML Schema file(s) (xsd) using the [HiSrc HigherJAXB Maven Plugin][12]. The generated code includes [JAXB][2] annotations to support XML marshaling/unmarshaling. Further, it uses [HiSrc HyperJAXB Annox XJC Plugins][13] to add custom annotations to selected classes.

### Problem ( from [StackOverflow][1] )

Generate JAXB Classes with XJC and a Maven plugin. If there is *no type* specified on the **element** in the XSD then XJC uses **Object** for the Java Type on the generated field/property. Is it possible to generate a property that uses **String** instead of **Object**?

### Solution

The short answer is no, it is not possible to bind an **element** with "no type" (i.e. `xs:anyType`) to `java.lang.String` using `<jxb:javaType .../>`. *However*, it is possible to wrap the generated Java property in a *code injected* property that performs the desired conversion. Here's why and how...

#### Attribute versus Element

For XJC, the default XML type for an **attribute** is `xs:anySimpleType` and that is mapped to the Java **String** type unless customized. And, the **attribute** can be customized in the binding file using `<jxb:javaType .../>`. For example the approach shown here, binds the `version` attribute to Java's `BigDecimal` type. It demonstrates how `<jxb:javaType .../>` is configured for and accepted by XJC:

**[XSD_IN_QUESTION.xjb][35]**
~~~
<jxb:bindings node="//xs:attribute[@name='Version']">
    <jxb:property name="VersionAttribute">
        <jxb:baseType>
            <jxb:javaType name="java.math.BigDecimal"
                parseMethod="jakarta.xml.bind.DatatypeConverter.parseDecimal"
                printMethod="jakarta.xml.bind.DatatypeConverter.printDecimal"
            />
        </jxb:baseType>
    </jxb:property>
</jxb:bindings>
~~~

> **Note:** ([JAXB v4.0, §7.8.2.1][3]]) This customization converts the default base type’s value for a *simple type definition* to the Java class specified by `<javaType>` name.

For XJC, the default XML type for an **element** is `xs:anyType` and that is mapped to the Java **Object** type. The `version` **element** cannot be customized using `<javaType ...>` because it is not a *simple type definition*! Any attempt to use the customization will throw this exception:

**Customization `anyType` with `javaType` will fail**
~~~
com.sun.istack.SAXParseException2:
    compiler was unable to honor this javaType customization.
    It is attached to a wrong place, or its inconsistent with other bindings.
~~~

> **Note:** It is inconsistent with `xs:anyType` used by an **element** when no XML type is defined because `xs:anyType` is a *complex type definition*.

#### Demonstration

This [demo (zip)][20] can be downloaded, unpacked and run "as-is". It is a stand-alone Maven project that uses the *HiSrc HigherJAXB Maven Plugin* to generate JAXB classes from an XML Schema when the schema contains elements without a specified XML type.

You can run the demo and/or test(s) using:

~~~
mvn -Pexec clean compile exec:java -Dexec.args="src/test/samples/ParentElement.xml"
mvn -Ptest clean test
~~~

The [output][22] shows the test(s) result.

The demonstration project use the standard Maven layout plus optional `bash` scripts.

**hisrc-higherjaxb-sample-notype**
~~~
├── build-cfg.sh
├── build-inc.sh
├── build.sh
├── pom.xml
├── README.md
├── run.sh
└── src
    ├── main
    │   ├── java
    │   │   └── org
    │   │       └── example
    │   │           ├── model
    │   │           │   └── Version.java
    │   │           └── notype
    │   │               └── Main.java
    │   └── resources
    │       ├── org
    │       │   └── example
    │       │       └── model
    │       │           └── jaxb.index
    │       ├── SOME_OTHER_XSD.xsd
    │       ├── XSD_IN_QUESTION.xjb
    │       └── XSD_IN_QUESTION.xsd
    └── test
        ├── java
        │   └── org
        │       └── example
        │           └── notype
        │               └── NoTypeTest.java
        ├── resources
        │   ├── jvmsystem.arguments
        │   ├── jvmsystem.properties
        │   └── simplelogger.properties
        └── samples
            ├── ParentElement.xml
            └── SomeOtherElement.xml
~~~

The [pom.xml][23] configures [hisrc-higherjaxb-maven-plugin][12] to use XJC extensions and an XJC [hisrc-hyperjaxb-annox-plugin][13] to customize the code generation. The customization includes:

+ Adding `@XmlRootElement` to `ParentElement.java` and `SomeOtherElement.java`
+ Injecting `getVersion / setVersion` methods to implement the `String` conversion.

**pom.xml**
~~~
...
<!-- mvn hisrc-higherjaxb:help -Ddetail=true -->
<!-- mvn hisrc-higherjaxb:generate -->
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
                <extension>true</extension>
                <args>
                    <arg>-Xannotate</arg>
                    <arg>-Xinject-code</arg>
                </args>
                <plugins>
                    <plugin>
                        <groupId>org.patrodyne.jvnet</groupId>
                        <artifactId>hisrc-hyperjaxb-annox-plugin</artifactId>
                        <version>${hisrc-hyperjaxb-annox-plugin.version}</version>
                    </plugin>
                </plugins>
            </configuration>
        </execution>
    </executions>
</plugin>
...
~~~

The JAXB binding file customized the [XSD_IN_QUESTION.xsd][33] schema by:

+ Using [hisrc-hyperjaxb-annox-plugin][13] to add `@XmlRootElement`
* Renaming the class per Java convention
+ Renaming the property to `VersionElement` so it can be nicely wrapped
+ Injecting code for the `Version` wrapper to get/set `java.lang.String`

[XSD_IN_QUESTION.xjb][35]
~~~
...
<jxb:bindings schemaLocation="XSD_IN_QUESTION.xsd">
    <jxb:bindings node="//xs:element[@name='PARENT_ELEMENT']/xs:complexType">
        <anx:annotate>@jakarta.xml.bind.annotation.XmlRootElement(name = "PARENT_ELEMENT")</anx:annotate>
        <jxb:class name="ParentElement"/>
        <jxb:bindings node="//xs:element[@name='Version']">
            <jxb:property name="VersionElement"/>
        </jxb:bindings>
        <ci:code>
public String getVersion() { return (getVersionElement() != null) ? getVersionElement().toString() : null; }
public void setVersion(String version) { setVersionElement(new org.example.model.Version(version)); }
        </ci:code>
    </jxb:bindings>
</jxb:bindings>
...
~~~

For testing, an XML instance of `PARENT_ELEMENT` provides a non-trivial representation of the `Version` element to emphasize that it is an `xs:anyType`. In fact, it can be an arbitrarily complex type, which provides some insight as to why the spec limits the `xs:javaType` binding to *simple type definition*, see [JAXB v4.0, §7.8.2.1][3].

[ParentElement.xml][53]
~~~
<?xml version="1.0" encoding="UTF-8"?>
<PARENT_ELEMENT xmlns:ns1="http://example.org/notype">
    <ns1:Version xsi:type="version" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <major>10</major>
        <minor>2</minor>
    </ns1:Version>
</PARENT_ELEMENT>
~~~

When JAXB unmarshals `PARENT_ELEMENT`, it uses a custom [Version.java][40] implemented by the project and provided to the JAXB context. This class has its own namespace and demonstrates the flexibility of `xs:anyType`.

> **Note:** A [jaxb.index][32] resource file lists the non-schema-derived classes, namely the java to schema binding, in a package to register with `JAXBContext`. 

The custom class implements a constructor to parse a version using the standard format (i.e. "10.2") into its major and minor parts. And a `toString()` method to return the major/minor parts as a version string.

[Version.java][40]
~~~
...
public Version(String version)
{
    if ( version != null )
    {
        String[] parts = version.split("\\.");
        setMajor(parts[0]);
        if ( parts.length > 1 )
            setMinor(parts[1]);
    }
}
...
public String toString()
{
    return format("%s.%s",
        ((getMajor() != null) ? getMajor() : "0"),
        ((getMinor() != null) ? getMinor() : "0")
    );
}
...
~~~

XJC reads the binding file [XSD_IN_QUESTION.xjb][35] and the XML schema [XSD_IN_QUESTION.xsd][33] to generate this class (and other classes):

**ParentElement.java**
~~~
package org.example.notype;

import java.io.Serializable;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "versionElement" })
@XmlRootElement(name = "PARENT_ELEMENT")
public class ParentElement implements Serializable
{
    private static final long serialVersionUID = 20230701L;

    @XmlElement(name = "Version", namespace = "http://example.org/notype", required = true)
    protected Object versionElement;

    public Object getVersionElement() { return versionElement; }
    public void setVersionElement(Object value) { this.versionElement = value; }

    public String getVersion() { return (getVersionElement() != null) ? getVersionElement().toString() : null; }
    public void setVersion(String version) { setVersionElement(new org.example.model.Version(version)); }
}
~~~

The `ParentElement` class is bound to the root "PARENT_ELEMENT" and provides an `java.lang.Object` field to represent the "Version" sub-element. The sub-element is bound to the get/set `VersionElement` property which was custom named in the binding file. 

Finally, the `ParentElement` class provides an addition get/set `Version` property to wrap the JAXB bound property and perform the Object to String conversion.

For usage, see [`org.example.notype.Main`][42] and [`org.example.notype.NoTypeTest`][62].

<!-- References -->

[1]: https://stackoverflow.com/questions/76787970
[2]: https://jakarta.ee/specifications/xml-binding/4.0/jakarta-xml-binding-spec-4.0.html
[3]: https://jakarta.ee/specifications/xml-binding/4.0/jakarta-xml-binding-spec-4.0.html#conversion-using-child-element-javatype
[10]: https://github.com/patrodyne/hisrc-basicjaxb#readme
[11]: https://github.com/patrodyne/hisrc-basicjaxb-annox#readme
[12]: https://github.com/patrodyne/hisrc-higherjaxb#readme
[13]: https://github.com/patrodyne/hisrc-hyperjaxb-annox#readme
[20]: https://github.com/patrodyne/hisrc-higherjaxb/releases/download/2.1.0/hisrc-higherjaxb-sample-notype-2.1.0-mvn-src.zip
[21]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/README.md
[22]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/OUTPUT.txt
[23]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/project-pom.xml
[24]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/bin
[25]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/bin/run.sh
[26]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src
[27]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main
[28]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources
[29]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources/org
[30]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources/org/example
[31]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources/org/example/model
[32]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources/org/example/model/jaxb.index
[33]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources/XSD_IN_QUESTION.xsd
[34]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources/SOME_OTHER_XSD.xsd
[35]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/resources/XSD_IN_QUESTION.xjb
[36]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/java
[37]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/java/org
[38]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/java/org/example
[39]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/java/org/example/model
[40]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/java/org/example/model/Version.java
[41]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/java/org/example/notype
[42]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/main/java/org/example/notype/Main.java
[50]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test
[51]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/samples
[52]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/samples/SomeOtherElement.xml
[53]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/samples/ParentElement.xml
[54]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/resources
[55]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/resources/jvmsystem.arguments
[56]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/resources/jvmsystem.properties
[57]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/resources/simplelogger.properties
[58]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/java
[59]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/java/org
[60]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/java/org/example
[61]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/java/org/example/notype
[62]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/src/test/java/org/example/notype/NoTypeTest.java

