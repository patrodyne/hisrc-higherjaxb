# HiSrc HigherJAXB Ex001 Trade DTD

This project is the first exploration of the HiSrc HigherJAXB library. It includes a Swing application named _Explorer_ to demonstrate two features of the HiSrc Higher plug-ins. This _Explorer_ application is designed to present a narrative lesson together with dynamic output for real-time experimentation. Feel free to modify the _Explorer.java_ source file to add or modify the action methods with your own investigative code. The `Explorer` class is an extension of `AbstractExplorer` which contains the more boring mechanics of this implementation. Feel free to create an `Explorer` class in your own projects to help explain the purpose of your work too.

> **About AbstractExplorer:** Projects create their own custom Explorer by extending `AbstractExplorer` and providing an HTML lesson page plus JMenuItem(s) to trigger exploratory code.

> An `AbstractExplorer` implementation (like the one you see here) displays three panels: an HTML lesson, a print console and an error console. The lesson file is read as a resource relative to the implementation (i.e. `Explorer`) class. Text is sent to the print console by calling `println(text)` and error messages are sent to the error console by calling `errorln(msg)`. Additionally, 'standard out' / 'standard error' streams are sent to their respective consoles.

## More

+ [Generate Xml Schema From String](!generateXmlSchemaFromString)
+ [Generate Xml Schema From DOM](!generateXmlSchemaFromDom)
+ [Generate Xml Schema Validator From DOM](!generateXmlSchemaValidatorFromDom)
+ [Marshal Trade 1](!marshalTrade1)
+ [Marshal Trade 2](!marshalTrade2)
+ [Marshal Trade 3](!marshalTrade3)
+ [Marshal Trades](!marshalTrades)
+ [Marshal Transfers](!marshalTransfers)
+ [Compare HashCodes](!compareHashCodes)
+ [Compare Equality](!compareEquality)
+ [Compare ToString](!compareToString)
+ [Roundtrip Valid](!roundtripValid)
+ [Roundtrip Invalid](!roundtripInvalid)
+ [Search Trades](!searchTrades)
+ [Search Transfers](!searchTransfers)

## End of this Exploration

<!-- References -->

[1]: https://github.com/patrodyne/hisrc-higherjaxb/blob/master/explore/Ex001-TradeDTD/src/test/java/org/patrodyne/jvnet/higherjaxb/ex001/Explorer.java?ts=4
[2]: https://raw.githubusercontent.com/patrodyne/hisrc-higherjaxb/master/explore/Ex001-TradeDTD/src/main/resources/TradeDTD.svg
