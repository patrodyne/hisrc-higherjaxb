# Sample: Any Content

This is a Maven project to demonstrate the unmarshalling of XML files with ambiguous content. It is in response to a question posed on [StackOverflow](https://stackoverflow.com/questions/75696006/jaxb-xml-to-pojo-using-inheritance-how-to).

## Execution

This is a standalone Maven project. You can run the test using:

~~~
mvn clean test
~~~

### Problem

XML data files are received without an XML Schema and contain an element named `CONTENT` that may contain multiple content such as Address content or Salary content. The goal is to unmarshal the content into Java JAXB objects.

### Solution

Create an XML Schema by inspecting the XML to identify this structure:

~~~
CONTENT: anySimpleType
CONTENTAddress: complexType
CONTENTSalary: complexType
RESPONSE: complexType
    INFO: complexType
    CONTENT: ref
~~~

The key is to define the `CONTENT` as `anySimpleType` and as a top level element. As such, JAXB is able to unmarshal the ambiguous content (address, salary, etc.) into a general DOM `Element`. Further, as a top-level element JAXB treats it as its own root element that can be marshaled into an XML fragment.

The trick is to pragmatically detect the actual fragment type and modify the fragment to use the correct `CONTENT` tag: CONTENTAddress, CONTENTSalary, etc. The to unmarshal the modified XML to instantiate the final objects.
