# Java class generation from XSD: cannot resolve an entity.

## Problem

See [Stack Overflow #79616463](https://stackoverflow.com/questions/79616463/)

## Solution

Try adding `<strict>false</strict>` to your jaxb plugin's configuration. This affects the SAX parser by allowing it to continue to other resolvers when the first attempt fails.

In any case, this [demo (zip)][1] includes your XSD files and generates JAXB v2.3 classes using `javax.xml.bind` imports. This is a stand-alone Maven project. After downloading and unzipping it to your local folder, you can run the test using:

~~~
mvn -Ptest clean test
~~~

Or execute the sample application using:

~~~
mvn -Pexec clean compile exec:java
~~~

> _Disclaimer:_ I am the maintainer for the HiSrc projects.

<!-- References -->

[1]: https://github.com/patrodyne/hisrc-higherjaxb/releases/download/2.2.1/hisrc-higherjaxb-sample-visitation-2.2.1-mvn-src.zip
