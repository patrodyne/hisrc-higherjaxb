# Sample: Any Attribute

This is a Maven project to demonstrate the unmarshalling of an *any attribute*. It is in response to a question posed on 
StackOverflow: [Using XJC to Add Additional Attribute to JAXB Class](https://stackoverflow.com/questions/77995165/)

### Problem

Given a `simpleType` definition in an XSD like the following:

~~~
<xsd:simpleType name="MyType" abc:reference="https://example.com/valid_values.xml">
    <xsd:restriction base="xsd:string"/>
</xsd:simpleType>
~~~

Where the `simpleType` has an additional attribute `abc:reference` with the given URL. 

> Note: In this use case, imagine the URL resolves to an XML with all the valid values for the `MyType` string.

Then use XJC to generate a JAXB class to access this `abc:reference` attribute and value (a URL) from the generated class at runtime.

### Solution

This [download (zip)][2] contains a stand-alone Maven project to demonstrate the unmarshalling of an [anyAttribute][7]. This type of attribute is used to allow attributes from other namespaces to be added to an XML *document* defined by *user* schemas. This example shows that it can also be used within the XML *schema*, itself, to declare attributes from other namespaces.

#### Execution

After unzipping the download to a local folder, you can run the test using:

~~~
mvn -Ptest clean test
~~~

Or execute the sample application using:

~~~
mvn -Pexec clean compile exec:java
~~~

This [output][9] shows the execution results.

#### Explanation

Given this XSD below with a `simpleType`, named **`MyType`**, that declares an *other* attribute (`abc:reference`) from a non-schema namespace, we can use the pre-compiled JAXB classes from [`org.patrodyne.jvnet:hisrc-higherjaxb-w3c-xmlschema-v1_0:jar`][8] to unmarshal the XSD as an *XML Schema Instance* (i.e. an XML document), at runtime. Like any other unmarshaled object, the attribute becomes a field/property named `reference` that can be [accessed programmatically][15].

[**MyTypeWrapper.xsd**][12]
~~~
<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema
    targetNamespace="urn:my.company:v2"
    xmlns="urn:my.company:v2"
    xmlns:abc="http://www.mycompany.no/xsd"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    attributeFormDefault="unqualified"
    elementFormDefault="qualified"
>
    <xsd:simpleType name="MyType"
        abc:reference="https://example.com/valid_values.xml"
    >
        <xsd:restriction base="xsd:string"/>
    </xsd:simpleType>

    <xsd:complexType name="MyTypeWrapper">
        <xsd:sequence>
            <xsd:element name="mt" type="MyType"/>
        </xsd:sequence>
    </xsd:complexType>

</xsd:schema>
~~~

[**MyTypeWrapper.xml**][17]
~~~
<MyTypeWrapper>
    <mt>SomeValidValue</mt>
</MyTypeWrapper>
~~~

#### Discussion

In an XML Schema, the [`<anyAttribute>`][7] tag enables us to extend the XML document with attributes *not specified* by the schema. The `<anyAttribute>` element is used to make EXTENSIBLE documents. It allows a document to contain additional elements that are not declared in the main XML schema.

The [`MyTypeWrapper.xsd`][12] file used to define `MyTypeWrapper` is *itself* an XML file with its own schema defined by `"http://www.w3.org/2001/XMLSchema"`.

In fact, `XMLSchema.xsd` is a [self-describing](https://www.w3.org/TR/NOTE-xml-schema-req#Principles) schema. It uses the same tags (`<schema\>`, `<element/>`, `<complexType/>`, etc.) used by the *user* schema `MyTypeWrapper.xsd` to define its contents. The difference being that the contents of `XMLSchema.xsd` are reflexively `<schema\>`, `<element/>`, `<complexType/>`, etc. and the contents of `MyTypeWrapper.xsd` are user defined: `MyTypeWrapper`, `MyType` etc.

> Note: Because `XMLSchema` is a [self-describing](https://www.w3.org/TR/NOTE-xml-schema-req#Principles) schema, every tag designed to support a *user* schema becomes a supported tag of `XMLSchema`.

Specific to this discussion, `XMLSchema.xsd` defines a `complexType` named **`openAttrs`** that is extended by almost all schema types to allow attributes from other namespaces to be added to user schemas.

[**OpenAttrs from XMLSchema.xsd**](https://github.com/patrodyne/hisrc-higherjaxb/blob/75a6dad594c7078664b234d9b57588047978a239/w3c/schemas/src/main/resources/w3c/2001/XMLSchema.xsd#L100)
~~~
<?xml version='1.0' encoding='UTF-8'?>
<!-- XML Schema schema for XML Schemas: Part 1: Structures -->
<xs:schema version="1.0" xml:lang="EN"
    targetNamespace="http://www.w3.org/2001/XMLSchema"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:hfp="http://www.w3.org/2001/XMLSchema-hasFacetAndProperty"
    elementFormDefault="qualified" blockDefault="#all"
>
...
<xs:complexType name="openAttrs">
    <xs:annotation>
        <xs:documentation>
            This type is extended by almost all schema types
            to allow attributes from other namespaces to be
            added to user schemas.
        </xs:documentation>
    </xs:annotation>
    <xs:complexContent>
        <xs:restriction base="xs:anyType">
            <xs:anyAttribute namespace="##other" processContents="lax"/>
        </xs:restriction>
    </xs:complexContent>
</xs:complexType>
...
</xs:schema>
~~~

It is this `openAttrs` definition that allows the `abc:reference` to be a valid attribute of `xsd:simpleType` in the `MyTypeWrapper.xsd` schema.

**MyType as a `simpleType` from MyTypeWrapper.xsd**
~~~
<xsd:simpleType name="MyType" abc:reference="https://example.com/valid_values.xml">
    <xsd:restriction base="xsd:string"/>
</xsd:simpleType>
~~~

The important thing to note, in this definition, is that `abc:reference` is an extension of `XMLSchema.xsd` and it is not an extension of the user schema `MyTypeWrapper.xsd`

> Note: *Normally*, a schema designer would declare a `<xsd:anyAttribute .../>` in the element/types of a user schema like `MyTypeWrapper.xsd` and the actual use case would then be in an XML document like `MyTypeWrapper.xml`. This is the original intent of `openAttrs` in the `XMLSchema.xsd` schema. But because `XMLSchema.xsd`
*is* reflexive, it supports extended attributes on its own types because it is *both* an XML schema and an XML document!

<!-- References -->

[1]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/OUTPUT.txt
[2]: https://github.com/patrodyne/hisrc-higherjaxb/releases/download/2.2.1/hisrc-higherjaxb-sample-any-attribute-2.2.1-mvn-src.zip
[4]: https://github.com/patrodyne/hisrc-higherjaxb#readme
[5]: https://github.com/patrodyne/hisrc-hyperjaxb-annox#readme
[6]: https://github.com/patrodyne/hisrc-basicjaxb#readme
[7]: https://www.w3schools.com/xml/schema_complex_anyattribute.asp
[8]: https://central.sonatype.com/artifact/org.patrodyne.jvnet/hisrc-higherjaxb-w3c-xmlschema-v1_0
[9]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/OUTPUT.txt
[10]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/README.md
[11]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/project-pom.xml
[12]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/main/resources/MyTypeWrapper.xsd
[13]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/main/resources/MyTypeWrapper.xjb
[14]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/main/java/my/company/XsJaxbContext.java
[15]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/main/java/my/company/Main.java
[16]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/main/java/my/company/MtJaxbContext.java
[17]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/test/samples/MyTypeWrapper.xml
[18]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/test/resources/jvmsystem.arguments
[19]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/test/resources/jvmsystem.properties
[20]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/test/resources/simplelogger.properties
[21]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-attribute/src/test/java/my/company/v2/MyTypeWrapperTest.java

