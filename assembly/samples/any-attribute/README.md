# Sample: Any Attribute

This is a Maven project to demonstrate the unmarshalling of an *any attribute*. It is in response to a question posed on 
StackOverflow: [Using XJC to Add Additional Attribute to JAXB Class](https://stackoverflow.com/questions/77995165/)

## Execution

This is a standalone Maven project. You can run the test using:

~~~
mvn -Ptest clean test
~~~

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

Given this XSD below with a `simpleType` that declares an *other* attribute (`abc:reference`) from a non-schema namespace, we can use the pre-compiled JAXB classes from `org.patrodyne.jvnet:hisrc-higherjaxb-w3c-xmlschema-v1_0:jar` to unmarshal this XSD as an *XML Schema Instance*, at runtime. Like any other unmarshaled object, the attribute becomes a field/property named `reference` that can be accessed programmatically, at runtime.

**MyTypeWrapper.xsd**
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

**MyTypeWrapper.xml**
~~~
<MyTypeWrapper>
    <mt>SomeValidValue</mt>
</MyTypeWrapper>
~~~

#### Discussion

In an XML Schema, the `<anyAttribute>` tag enables us to extend the XML document with attributes *not specified* by the schema. The `<anyAttribute>` element is used to make EXTENSIBLE documents. It allows a document to contain additional elements that are not declared in the main XML schema.

The `MyTypeWrapper.xsd` file used to define `MyTypeWrapper` is *itself* an XML file with its own schema defined by `"http://www.w3.org/2001/XMLSchema"`.

In fact, `XMLSchema.xsd` is a [self-describing](https://www.w3.org/TR/NOTE-xml-schema-req#Principles) schema. It uses the same tags (`<schema\>`, `<element/>`, `<complexType/>`, etc.) as `MyTypeWrapper.xsd` to define its contents. The difference being that the contents of `XMLSchema.xsd` are reflexively `<schema\>`, `<element/>`, `<complexType/>`, etc. and the contents of `MyTypeWrapper.xsd` are just `MyTypeWrapper`, `MyType` etc.

> Note: Because `XMLSchema` is a [self-describing](https://www.w3.org/TR/NOTE-xml-schema-req#Principles), every tag designed to support a *user* schema becomes a supported tag of `XMLSchema`.

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

> Note: *Normally*, a schema designer would declare a `<xsd:anyAttribute .../>` in the element/types of a user schema like `MyTypeWrapper.xsd` and the actual use case would then be in an XML instance like `MyTypeWrapper.xml`. This is the original intent of `openAttrs` in the `XMLSchema.xsd` schema. But because `XMLSchema.xsd`
*is* reflexive, it supports extended attributes on its own types, too!


