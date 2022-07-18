# HiSrc HigherJAXB Ex001 Trade DTD

This project is the first exploration of the HiSrc HigherJAXB library. It includes a Swing application named *Explorer* to demonstrate features of the HiSrc Higher plug-ins. This *Explorer* application is designed to present a narrative lesson together with dynamic output for real-time experimentation. Feel free to modify your copy of the [Explorer.java][4] source file by adding or modifying the action methods with your own investigative code. The `Explorer` class is an extension of `AbstractExplorer` which contains the more boring mechanics of this implementation. Feel free to create an `Explorer` class in your own projects to help explain the purpose of your work too.

> **About AbstractExplorer:** Projects can create their own custom Explorer by extending `AbstractExplorer` and providing an HTML lesson page plus `JMenuItem`(s) to trigger exploratory code.

> An `AbstractExplorer` implementation (like the one you see here) displays three panels: an HTML lesson, a print console and an error console. The lesson file is read as a resource relative to the implementation (i.e. `Explorer`) class. Text is sent to the print console by calling `println(text)` and error messages are sent to the error console by calling `errorln(msg)`. Additionally, 'standard out' / 'standard error' streams are sent to their respective consoles.

## Document Type Definition (DTD)

An XML document need not follow any rules beyond the well-formedness criteria laid out in the [XML 1.0 specification][1]. To exchange documents in a meaningful way, however, requires that their structure and content be described and constrained so that the various parties involved will interpret them correctly and consistently. This can be accomplished through the use of a *schema*.

When the [early draft of the JAXB specification][2] was written in 2001, schemas written in the *document-type definition* language (DTD) were by far the most common type. As schemas go, however, DTDs are fairly weak. They support the definition of simple constraints on structure and content, but provide no real facility for expressing data-types or complex structural relationships.

Nevertheless, the *HiSrc HigherJAXB* Maven plug-in supports DTD schemas and there is merit in the simplicity of a DTD.

In [this exploration][4], you will [model][5] a simple stock trade with a [DTD schema][6] and its related [XJC binding file][7]. Your [POM][3] configures the `hisrc-higherjaxb-maven-plugin` to use `<schemaLanguage>dtd</schemaLanguage>`.

> **Note:** [XJC][10] uses different binding files based on the schema language being used, for DTD schemas the binding file is known as an *xml-java-binding-schema* binding and it has its own DTD schema, [xjs.dtd][8]. The XJC compiler is available in several forms: as a CLI tool, a Ant task, a Maven plug-in and a library API. Your POM invokes the XJC compiler as a Maven plug-in. Although this exploration highlights the historic DTD schema-language, you should consider the XML schema-language for modern use.

Your [Trade.dtd][6] uses these DTD building blocks:

+ `ELEMENT` - the name and content for an XML tag, where content can be:
    + a list of child element names
    + simple property values
+ `ATTLIST` - the XML tag name and its list of attribute definitions
+ `PCDATA` - value type for parsed character data
+ `CDATA` - value type for character data

> **Note:** Another DTD building block is `ENTITY`; it can be used for text substitution. An `ENTITY` provides a *name* for some "text" which can be expanded elsewhere in the DTD using `%name;`.

The *HiSrc HigherJAXB* Maven plug-in configures and invokes the XJC compiler. The XJC compiler reads your [Trade.dtd][6] and generates Java source code. The generated classes include JAXB annotations based on your schema definitions. The DTD value types are very general, and because of their simplicity, the XJC binds simple elements and attributes to one type, `java.lang.String`.

Another weakness of the DTD schema is the lack of namespace(s). The namespace idea was introduced after DTDs were conceived. This limits the complexity of a data model because element names must be globally unique.

### DTD: Simple Elements

As stated, you will use [Trade.dtd][6] to define your data [model][5]. Let's review this DTD:

A stock trade is an order to buy or sell shares for a company on a stock exchange. The order includes a unique symbol to identify the company, a quantity and price options.

+ **symbol** - a unique series of letters assigned to a security for trading purposes
+ **quantity** - the number of stock shares to be bought or sold
+ **stopPrice** - the price that triggers the creation of a market order.
+ **limitPrice** - the maximum price to pay (buy) or the minimum price to accept (sell)
+ **completed** - the time when the transaction is finished

Your DTD schema begins with declarations of the simple elements used in your *Trade* model. A simple element contains a single simple value: text, number, date, etc. You declare each element with a name and value type. Typically, the DTD value types are either `PCDATA` for elements or `CDATA` for attributes. The XJC compiler maps these simple values to `String` *unless customized by a binding instruction*.

~~~
...
<!ELEMENT symbol (#PCDATA) >
<!ELEMENT quantity (#PCDATA) >
<!ELEMENT stopPrice (#PCDATA) >
<!ELEMENT limitPrice (#PCDATA) >
<!ELEMENT completed (#PCDATA) >
...
~~~

As the following notes begin to show, the DTD generates many *Adapter* classes.  The more modern, default XML Schema generator handles type declarations more efficiently. You will explore the XML schema generator in a future exploration.

> **Note 1:** When a simple element is first declared, the DTD generator will create a Java class containing a single field named `value`. This field is annotated with `@XmlValue` which is a mapping to an XML Schema complex type with a simpleContent or a XML Schema simple type.

> **Note 2:** However, when the simple element is listed as a child-element of any parent element then the DTD generator omits the 'value' class and creates a `String` field for the element in the parent class.

> **Note 3:** However again, when a custom binding is used to convert the initial `String` type to another type, say `BigDecimal`, then the DTD generator restores the wrapper class (regardless of parent-child declarations) and `@XmlValue` annotation then adds an `@XmlJavaTypeAdapter` and generates an `AdapterN` class to do the conversion.

> **Note 4:** If the simple value is modeled as an XML attribute of some parent element, then the DTD generator creates only the field in the parent class using a built in adapter for `String` or generates a custom adapter for other types.

> **Hint:** If your model includes the same non-string attribute for several parent elements then consider re-designing your model to use a simple element instead of the attribute. Both designs require an adapter to convert the non-string value to another type but the child-element design will re-use the same generated wrapper and adapter classes. To see this, look at the `created` attribute versus the `completed` child-element in your [model][5].

### DTD: Complex Element, trade

The primary element in your model is a *trade*. It is a complex element with attributes and child elements. In a DTD, a complex element declares other elements (simple or complex) as children. It may also declare a list of attributes where each attribute is a simple name-value pair. Your *trade* model adds these attributes:

+ **account:** - a unique number to identify a business user
+ **action:** - an enumeration of the desired trade transaction
+ **duration:** - an enumeration of the period when the order remains valid
+ **created:** - the time when the trade order is placed.

~~~
...
<!ELEMENT trade ( symbol, quantity, stopPrice?, limitPrice?, completed ) >
<!ATTLIST trade
          account CDATA #REQUIRED
          action ( buy | buy-to-cover | sell | sell-short ) #REQUIRED
          duration ( immediate | day | good-til-canceled ) "day" 
          created CDATA #REQUIRED >
...
~~~

The [trade][6] element declares a sequence of child elements separated by commas, this sequence determines the appearance order of these children in the document. In a full declaration, the children must also be declared, and the children can also have children. Two children in this sequence end in a `?` to declare they are optional, each can appear zero or one times. This and other such declarations are:

Cardinality, the child element can appear:

+ `?`: zero or one times
+ `+`: one or one times
+ `*`: zero or many times

The attribute list for your *trade* element declares a name, type and value for:

+ **account:** - simple CDATA and a required value
+ **action:** - enumerated list and a required value
+ **duration:** - enumerated list with the default of "day"
+ **created:** - simple CDATA with a required value

The XJC generator maps these simple values to `String` *unless customized by a binding instruction*. Although the DTD provides the enumeration for *action* and *duration*, the XJC compiler does not generate enumerations by default. Also, the DTD is vague about the exact data types of *account* and *created*; thus, you must declare the intended types in your binding file, later.

### DTD: Complex Element, transfer

Your model includes a *transfer* element to move shares from one account to another. Your *transfer* model adds these attributes:

+ **account:** - a unique number to identify a business user
+ **to-account:** - a unique number to identify another business user
+ **created:** - the time when the trade order is placed.

~~~
...
<!ELEMENT transfer ( symbol, quantity, completed ) >
<!ATTLIST transfer
          account CDATA #REQUIRED
          to-account CDATA #REQUIRED
          created CDATA #REQUIRED >
...
~~~

The [transfer][6] element declares a sequence of child elements separated by commas. You declared the child elements at the start and used these children here and in the *trade* element. 

The attribute list for your *transfer* element declares a name, type and value for:

+ **account:** - simple CDATA and a required value
+ **to-account:** - simple CDATA and a required value
+ **created:** - simple CDATA with a required value

The XJC generator maps these CDATA types to `String`. Soon, you will customize the types using XJS bindings. Attributes with customized bindings cause XJC to generate an `XmlAdapter` for each field in each parent. The *trade* and *transfer* elements share attributes for *account* and *created* which will generate multiple adapters. Changing these attributes to child elements will create wrappers that reuse matching adapters. The impact is greater when your model contains many elements with the same attributes.

### DTD: Complex Element, transaction-batch

Often overlooked when designing a model, is a root element to represent a batch of other top-level elements. Since XML elements always have a start and end tag, you need a container, if you need to export or import a batch of them. The *transaction-batch* element servers this need in your model.

~~~
...
<!ELEMENT transaction-batch ( trade | transfer )+ >
...
~~~

The [transaction-batch][6] element declares a choice of child elements for *trade* or *transfer*. The batch is declared to contain at least one but then any number of such elements. From here, XJC generates this Java code to implement your container:

~~~
@XmlElements({
    @XmlElement(name = "trade", required = true, type = Trade.class),
    @XmlElement(name = "transfer", required = true, type = Transfer.class)
})
protected List<Serializable> tradeOrTransfer;
~~~

## DTD and Xml Java (binding) Schema (XJS)

Using the DTD alone, generates only `String` values for your simple elements and no enumerations. Fortunately, XJC looks for an additional file that can be used to customize the DTD schema. The additional file is known as a *binding* file. Each schema language has it own XJC binding file format. For the DTD language, the format is recognizable by its XML tag, `<xml-java-binding-schema>`.

The binding file is *also* an XML file and has its own DTD, [xjs.dtd][8].

![Schema Relationships][9]

### Root: xml-java-binding-schema

This exploration provides a custom binding file named [Trade.xjs][7] that is used to strengthen your [Trade.dtd][6] contract. Next, let's break down your binding file.

Your XJS binding file is an XML file conforming to [xjs.dtd][8] and described in [this][2] early specification.

~~~
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xml-java-binding-schema SYSTEM "../../../doc/xjs.dtd">
<xml-java-binding-schema version="1.0ea2" xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc">
    ...
</xml-java-binding-schema>
~~~

### Tag: Options

DTDs do not support XML namespace(s) but you can configure XJC to generate your code using the `<options.../>` tag in your binding file. This configuration specifies the Java package for the generated classes for your explorer project.

~~~
    ...
    <options package="org.patrodyne.jvnet.higherjaxb.ex001.model" />
    ...
~~~

### Tag: enumeration

In the XJS, an *enumeration* declaration defines an enumeration class, which represents a set of named values and a bijective mapping between strings and these values; it also defines an implicit conversion for that class.

Although the attribute type for a DTD attribute can be an enumerated list, the XJC does not generate a Java enumeration unless you configure the binding file like this:

~~~
    ...
    <enumeration name="Action" members="buy buy-to-cover sell sell-short"/>
    <enumeration name="Duration" members="immediate day good-til-canceled"/>
    ...
~~~

### Tag: conversion

In the XJS, a *conversion* declaration defines a named conversion that may be specified in an element-value or attribute declaration in order to convert element content or attribute values to values of types other than `java.lang.String`.

~~~
    ...
    <conversion name="price" type="java.math.BigDecimal"/>
    <conversion name="timestamp" type="javax.xml.datatype.XMLGregorianCalendar"
        parse="Bind.parseXGC" print="Bind.print" />
    ...
~~~

The *parse* method, as specified, describes how to convert a string into a value of the target type. It must be the token **new** or be of the form *ClassName.methodName*, where *ClassName* is a legal Java class name, possibly including a package prefix, and *methodName* is a legal Java method name.

The *print* method, as specified, describes how to convert a value of the target type back into a string. It must be of the form *methodName* or of the form *ClassName.methodName*, where *ClassName* and *methodName* are constrained as for the parse method.

Your project provides a hand-coded [Bind.java][11] class to implement the *parse* and *print* methods for your *timestamp* conversion.

### Tag: element / convert

In the XJS, if a binding-conversion name (i.e. `"price"`, `"timestamp"`, etc.) is specified then it must name a conversion defined at the top level of the binding schema or defined as a built-in conversion. 

***Built-in conversions*** A built-in binding conversion is defined for the following Java primitive types. In each case the name of the conversion is the name of the type.

+ For `boolean`, the built-in conversion maps the string `"true"` to `true` and the string `"false"` to `false`; for all other input strings it is undefined.

+ For each of the numeric types `byte`, `short`, `int`, `long`, `float`, and `double`, the built-in conversion uses the static `parse`*Type*`(String)` method of the corresponding `java.lang` wrapper class to convert strings to values, and the static `toString(`*type*`)` method of the same class to convert values to strings.

Your [Trade.xjs][7] decalres four top-level bindings to convert strings to specific data types:

~~~
    ...
    <element name="quantity" type="class" convert="int">
        <constructor properties="value"/>
    </element>
    
    <element name="stopPrice" type="class" convert="price">
        <constructor properties="value"/>
    </element>
    
    <element name="limitPrice" type="class" convert="price">
        <constructor properties="value"/>
    </element>
    
    <element name="completed" type="class" convert="timestamp">
        <constructor properties="value"/>
    </element>
    ...
~~~

The *quantity*, *stopPrice*, *limitPrice* and *completed* types declared in the [Trade.dtd][6] are simple types that are meant to represent a single value. When they are declared with a conversion name in the binding file, the XJC generates wrapper classes with one field, `value`, plus its getter/setter methods. For convenience, a `constructor` is declared within each of your element-class declarations.

> Note: A constructor declaration has the form `<constructor properties="p1, p2, ..., pN"/>` and may appear only within an element-class declaration. The property names `pN`, of which there must be at least one, must be valid XML name(s) and must be unique. Each property name must be identical to the name of a property defined in the enclosing element class.

The *stopPrice* and *limitPrice* element types are meant to contain prices expressed as decimal fractions of dollars. It would not be a good idea to use the built-in conversions for the `float` or `double` primitive numeric types, since these types are inherently approximate. Hence we define a new `"price"` conversion that represents prices as instances of the class `java.math.BigDecimal`, which can represent decimal fractions precisely. With that conversion, a string will be converted into a `BigDecimal` instance during unmarshalling by invoking the `BigDecimal(String)` constructor; such an instance will be converted back into a string during marshaling by invoking the `toString` method of that class. This conversion is used above in the element-value declarations for the *stopPrice* and *limitPrice* element types.

The *completed* element type is meant to represent the time when the trade is executed. It is best practice to use an [ISO-8601][12] formatted timestamp for XML instances because that format is precise and includes an unambiguous time zone offset. With that conversion, a string will be converted to a `javax.xml.datatype.XMLGregorianCalendar` during unmarshalling using the `parseXGC(String)` method of your [Bind][11] helper class; such an instance will be converted back into a string during marshaling by invoking the `print` method of said class.

### Tag: Trade element with attribute(s)

A *trade* element must contain a *symbol* element, followed by a *quantity* element, optionally followed by a *stopPrice* element or a *limitPrice* element or both, followed by a *completed* element, the *account*, *action* and *created* attributes are required, and the *duration* attribute defaults to "day".

Your XJS declares bindings for the *trade* element to generate a constructor for the *account*, *action*, *symbol* and *quantity* properties. The constructor can programatically set the *created* property to the current time.

Attribute bindings declare each Java property name and convert the XML string values to precise Java types and back.

~~~
    ...
    <element name="trade" type="class" root="true">

        <constructor properties="account action symbol quantity"/>

        <attribute name="account"  property="Account"  convert="int"/>
        <attribute name="action"   property="Action"   convert="Action"/>
        <attribute name="duration" property="Duration" convert="Duration"/>
        <attribute name="created"  property="Created"  convert="timestamp"/>

    </element>
    ...
~~~

### Tag: Transfer element with attribute(s)

The *transfer* element is similar to *trade* except that it has an additional required attribute, *to-account*, to represent the destination account, and it does not allow *stopPrice* or *limitPrice* elements in its content.

Your XJS declares bindings for the *transfer* element to generate a constructor for the *account*, *toAccount*, *symbol* and *quantity* properties. The constructor can programatically set the *created* property to the current time.

Attribute bindings declare each Java property name and convert the XML string values to precise Java types and back.

~~~
    ...
    <element name="transfer" type="class" root="true">

        <constructor properties="account toAccount symbol quantity"/>

        <attribute name="account"     property="Account"    convert="int"/>
        <attribute name="to-account"  property="ToAccount"  convert="int"/>
        <attribute name="created"     property="Created"    convert="timestamp"/>

    </element>
    ...
~~~

### Tag: xjc:serializable

Many important applications of XML data binding, make heavy use of the Java platform’s object-serialization facility to cache or transfer data.

This project uses [xjs.dtd][8] to specify the syntax for [Trade.xjs][7]; however, the XJC library relies on a (mostly) equivalent specification, named [bindingfile.xsd][13]. This project is DTD focused; thus, it uses the DTD specification. The actual XSD specification allows the XJC library to include the *xjc* namespace in its configuration.

> **Note:** Since DTD syntax predates XML namespace, qualified names such as `xjc:serializable` and `xjc:superClass` are simply cases of legal XML Names that happen to contain colons. From the perspective of the DTD, there is no such thing as a namespace declaration or namespace use.

Your [Trade.xjs][7] includes a binding to set the Java `serialVersionUID`, to a `long` value of your choosing. Using a value of the form `YYYYMMDD` satisfies the serialization contract and you can easily update it to a newer future value, when needed. (Most coders set it once and forget about it). More to the point, this binding adds the `Serializable` interface to each generated class.

~~~
    ...
    <xjc:serializable uid="20220501" />
    ...
~~~

### Tag: xjc:superClass

It is possible to extend your generated classes from a common super-class; but, it creates a runtime issue.

~~~
IllegalAnnotationsException: 4 counts of IllegalAnnotationExceptions
@XmlValue is not allowed on a class that derives another class.
~~~

Unfortunately, the `Quantity`, `StopPrice`, `LimitPrice` and `Completed` wrappers use `@XmlValue` to map each class to a simpleContent or a XML Schema simple type. To avoid the runtime exception, you must comment out this binding.

~~~
    ...
    <!-- <xjc:superClass name="Stageable" /> -->
    ...
~~~

The more sophisticated XML schema language binding reduce or eliminate the need for these kind of wrappers and support declaration of super-classes for individual elements. This will be demonstrated in a future exploration.

## Experimentation

Let's experiment with your [Explorer.java][4], as follows. Feel free to modify your copy to initiate your own explorations.

### Regenerate Xml Schema

This project generates several Java classes to implement the model declared in [Trade.dtd][6] and [Trade.xjs][7]. You can create a `JAXBContext` instance from the generated classes and use it in several ways.

~~~
private JAXBContext createJAXBContext() throws JAXBException
{
    ...
    Class<?>[] classesToBeBound = { ObjectFactory.class, Trade.class, Transfer.class,
        TransactionBatch.class };
    jaxbContext = JAXBContext.newInstance(classesToBeBound);
    ...
}
~~~

> **Note:** The new context will recognize all the classes specified, but it will also recognize any classes that are directly/indirectly referenced statically from the specified classes. Normally, binding to `ObjectFactory.class` is sufficient.

The `JAXBContext` class provides the application's entry point to the JAXB API. It provides an abstraction for managing the XML/Java binding information necessary to implement the JAXB binding framework operations: *unmarshal*, *marshal* and *validate*. For display purposes, we use JAXB's `SchemaOutputResolver` to generate a textual representation in two ways:

+ [Regenerate Xml Schema From String](!generateXmlSchemaFromString)
+ [Regenerate Xml Schema From Dom](!generateXmlSchemaFromDom)

The first way (above) outputs the XML Schema to a `String` backed `SchemaOutputResolver`. The second way outputs the XML Schema to a `DOMSource` backed resolver. The output from both approaches are similar but the second way enables us to generate a *schema validator object* that can be wired into the marshaller and unmarshaller objects.

> **Hint:** You can use a DTD schema to prototype your model then generate the XML Schema from the `JAXBContext` instance to advance to an XML schema based model.

+ [Generate Xml Schema Validator From Dom](!generateXmlSchemaValidatorFromDom)

> **Note:** This exploration does not wire in the *schema validator object* automatically. This allows you explore the effect of unmarshalling an invalid schema, as described below. If you generate the validator, it will be active for the current session. Toggle the validator and explore the other actions using the tool bar option.

### Marshal Trade Instances to XML

For this demo, `Trade` is a JAXB annotated class with fields for stock market trading. The demo is initialized with three instances: 

+ [Trade1](!marshalTrade1): [1001, BUY, DAY, ...]
+ [Trade2](!marshalTrade2): [1001, BUY, DAY, ...]
+ [Trade3](!marshalTrade3): [1002, SELL, DAY, ...]

All three instances are distinct Java objects. The first two objects refer to the same trade and the third is a separate trade.

### Marshal Transaction Batches to XML

A batch contains several, often many, instances of top-level elements when you need to transmit data. Your model declares a **transaction-batch** element to generate a `TransactionBatch` type for marshaling / unmarshaling a list of **trade** / **transfer** elements.

+ [Marshal Trades](!marshalTrades)
+ [Marshal Transfers](!marshalTransfers)

### Compare Objects

The HiSrc framework provides an `ObjectLocator` and `HashCodeStrategy2` to generate a hash code that is based on the object values. In your model, the hash code and equality is based on the instance's (i.e. **trade**, **transfer**) field values. 

#### Compare Hash Codes

Select [Compare Hash Codes](!compareHashCodes) to show the HiSrc hash and the System hash for the three `Trade` instances. The HiSrc hash codes for `Trade` #1 & #2 are the same because these two objects both identify a trade with the same field values while the hash code for `Trade` #3 is different from the others because that object identifies a separate *SELL* order. However, the system hash codes (**identityHashCode**) for all three objects are unique because they are individual JVM objects.

Here's how the `Trade` class uses the HiSrc interfaces for the `hashCode()` method. See [Explorer.java][4] for the field level details.

~~~
public int hashCode() {
    ObjectLocator theLocator = null;
    final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.getInstance();
    if (strategy.isTraceEnabled()) {
        theLocator = new DefaultRootObjectLocator(this);
    }
    return this.hashCode(theLocator, strategy);
}
~~~

#### Compare Equality

Select [Compare Equality](!compareEquality) to compare (A) **Trade1** with **Trade2** and (B) **Trade2** with **Trade3**.

Case (A) compares two individual objects for the same **BUY** trade order and finds that they are equal.

Case (B) compares a **BUY** order with a **SELL** order and finds that they are **not** equal. Further, case (B) provides a trace log to report the first non-equal field.

Here's how the `Trade` class uses the HiSrc interfaces for the `equals(Object that)` method. See [Explorer.java][4] for the field level details.

~~~
public boolean equals(Object object) {
    ObjectLocator thisLocator = null;
    ObjectLocator thatLocator = null;
    final EqualsStrategy2 strategy = JAXBEqualsStrategy.getInstance();
    if (strategy.isTraceEnabled()) {
        thisLocator = new DefaultRootObjectLocator(this);
        thatLocator = new DefaultRootObjectLocator(object);
    }
    return equals(thisLocator, thatLocator, object, strategy);
}
~~~

The `ObjectLocator` parameters are optional; they can be `null`. When they are `null`, the field level locator utility will use basic location tracing. When the root level locators are used, the field level locators create a nested path that is used for trace level logging. HiSrc uses [SLF4J][14] as its logging framework and that framework is usually configured to skip trace level messaging.

> **Important:** The HiSrc `JAXBEqualsStrategy` extends `DefaultEqualsStrategy` and both include logic to differentiate objects that use *default* vs *set* values. In JAXB, an effort is made to track how an element or attribute receives its value. If the XML Schema defines a *default* value for an element or attribute and the XML instance does not set the value *explicitly* then the JAXB generated class will return the default value from the accessor while keeping the field value `null`. This may surprise a maintainer when reviewing the Java code but it is how JAXB is designed to work! This allows JAXB to detect when a property is a *set value* versus a *default value* and can help preserve round tripping.

#### Compare ToString

Select [Compare ToString](!compareToString) to compare **Trade1**, **Trade2** and **Trade3**.

~~~
public String toString() {
    ObjectLocator theLocator = null;
    final ToStringStrategy2 strategy = JAXBToStringStrategy.getInstance();
    if (strategy.isTraceEnabled()) {
        theLocator = new DefaultRootObjectLocator(this);
    }
    final StringBuilder buffer = new StringBuilder();
    append(theLocator, buffer, strategy);
    return buffer.toString();
}
~~~

> **Hint:** The HiSrc `JAXBToStringStrategy` extends `DefaultToStringStrategy` which includes logic to format the *toString()* output in increasing detail with respect to the `INFO`, `DEBUG` and `TRACE` log levels.

### Roundtrip

In JAXB, a *round trip* is an *xml-to-object-to-xml* or an *object-to-xml-to-object* sequence. Ideally, the initial XML and final XML instance will be the same. There are mechanisms, such as `JAXBElement`, that can be used to preserve the fine details and are often used. However, most projects do not require an exact reproduction of the XML and are satisfied with being `equal-by-value`.

#### Valid Roundtrip

The `JAXBEqualsStrategy` can be used to verify deep `equal-by-value`. Select [Roundtrip Valid](!roundtripValid) to marshal the **Trade1** object into its XML representation then unmarshal the XML back into a second object; finally, this test will use the object's equal method to verify equal by value.

#### Invalid Roundtrip

Select [Roundtrip Invalid](!roundtripInvalid) to create a bit of chaos. This exploration intentionally corrupts the XML by replacing the *symbol* tag with a misspelled *simbol* tag. Because of this, the corrupted XML will not have a *symbol* value; but instead, it will have unknown tag from the XML Schema point of view.

If you toggle the Schema Validation flag to **off (red)** then JAXB will attempt to complete the roundtrip but the final XML will omit the `<symbol>...</symbol>` value.

To detect this chaos programatically, use [Generate Xml Schema Validator From Dom](!generateXmlSchemaValidatorFromDom) then execute [Roundtrip Invalid](!roundtripInvalid). This time, JAXB validation will throw an exception:

~~~
javax.xml.bind.UnmarshalException
[org.xml.sax.SAXParseException; lineNumber: 3; columnNumber: 13; cvc-complex-type.2.4.a:
Invalid content was found starting with element 'simbol'. One of '{symbol}' is expected.]
~~~

### Searching

A benefit of implementing custom *hashCode()* and *equals()* methods is found when using a Java `List` to find the index of a `Trade` or `Transfer` instance.

The *indexOf()* method returns the index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element. More formally, returns the lowest index i such that `(o==null ? get(i)==null : o.equals(get(i)))`, or -1 if there is no such index.

The benefit comes when your *equals()* method is used because it compares that actual field value of your `Trade` or `Transfer` instance. This allows you to search for duplicate instances when the objects are different but their values are the same.

Many of the Java Collection Framework classes benefit from custom *hashCode()* and *equals()* methods.

These two experiments, select a random item from a list of trades or transfers, respectively. The selected item is marshaled / unmarshaled into a different object with the same values. The list is searched for the new object by equating all field values.

+ [Search Trades](!searchTrades)
+ [Search Transfers](!searchTransfers)

When you configure your logger to trace level for the equals strategy, your log will contain the detailed location where each non-matching comparison ocured. This is verbose but useful when trouble-shooting. The level should be set higher in production.

~~~
...DefaultEqualsStrategy=TRACE
~~~

# End of this Exploration

-30-

<!-- References -->

[1]: https://www.w3.org/TR/xml/
[2]: http://xml.coverpages.org/jaxb0530spec.pdf
[3]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/explore/Ex001-TradeDTD/project-pom.xml?ts=4
[4]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/explore/Ex001-TradeDTD/src/test/java/org/patrodyne/jvnet/higherjaxb/ex001/Explorer.java?ts=4
[5]: https://raw.githubusercontent.com/patrodyne/hisrc-higherjaxb/master/explore/Ex001-TradeDTD/src/test/resources/TradeDTD.svg
[6]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/explore/Ex001-TradeDTD/src/main/resources/Trade.dtd?ts=4
[7]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/explore/Ex001-TradeDTD/src/main/resources/Trade.xjs?ts=4
[8]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/explore/Ex001-TradeDTD/doc/xjs.dtd?ts=4
[9]: file:src/test/resources/schema-relations.png
[10]: https://eclipse-ee4j.github.io/jaxb-ri/
[11]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/explore/Ex001-TradeDTD/src/main/java/org/patrodyne/jvnet/higherjaxb/ex001/model/Bind.java?ts=4
[12]: https://en.wikipedia.org/wiki/ISO_8601
[13]: https://github.com/eclipse-ee4j/jaxb-ri/blob/master/jaxb-ri/xjc/src/main/schemas/com/sun/tools/xjc/reader/dtd/bindinfo/bindingfile.xsd
[14]: https://www.slf4j.org/
