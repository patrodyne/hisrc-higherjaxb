# HiSrc HigherJAXB

Maven plugin to generated Java source from XML Schema.

## Description

This repository is a fork of [maven-jaxb2-plugin][23]. The original project was
developed by the admirable Alexey Valikov (a.k.a. [Highsource][2]). This
repository contains Java projects to build Maven artifact(s) related to the
Java Architecture for XML Binding (JAXB) framework. It is one of a family of
repositories forked from [Highsource][2] that provide tools for JAXB and JPA
processing. Repo and artifact names have been changed to reflect the familial
connection between the repositories and to fix a conformance issue with the
original `maven-jaxb2-plugin` name.

> **Hint:** You can manage the number of spaces a tab is equal to for your
> GitHub personal account (i.e. 1 tab = 4 spaces) when viewing files in this
> repository. [Here's how][3].

### List of repositories in this family

| Patrodyne                   | Highsource                  | Purpose                                                |
| --------------------------- | --------------------------- | ------------------------------------------------------ |
| [hisrc-basicjaxb-annox][11] | [annox][21]                 | Parse XML Schema to find Java annotation declarations. |
| [hisrc-basicjaxb][12]       | [jaxb2-basics][22]          | A library of XJC plugins and tools to extend JAXB.     |
| [hisrc-higherjaxb][13]      | [maven-jaxb2-plugin][23]    | Maven plugin to generated Java source from XML Schema. |
| [hisrc-hyperjaxb-annox][14] | [jaxb2-annotate-plugin][24] | XJC plugin to add arbitrary Java annotations to JAXB.  |
| [hisrc-hyperjaxb][15]       | [hyperjaxb3][25]            | Maven and XJC plugins to add JPA annotations to JAXB.  |

### Graph of repository relationships

![Patrodyne-Highsource Graph][1]

### Releases

#### GitHub Releases, Demonstrations

* [HiSrc HigherJAXB v2.1.1, Explorations][34]
* [HiSrc HigherJAXB v2.1.0, Explorations][33]
* [HiSrc HigherJAXB v2.0.0, Explorations][32]
* [HiSrc HigherJAXB v0.14.2, Explorations][31]

#### Maven Repositories

* Search
	* [MVN Repository](https://mvnrepository.com/artifact/org.patrodyne.jvnet?sort=popular)
	* [Central Repository](https://central.sonatype.com/search?q=org.patrodyne.jvnet+hisrc-higherjaxb&sort=name)
* Index
	* [Central Repository](https://repo1.maven.org/maven2/org/patrodyne/jvnet/)

#### JavaDoc

* [hisrc-higherjaxb23-maven-plugin][40]
* [hisrc-higherjaxb30-maven-plugin][41]
* [hisrc-higherjaxb40-maven-plugin][42]
* [hisrc-higherjaxb-maven-plugin][43]
* [hisrc-higherjaxb-maven-plugin-core][44]
* [hisrc-higherjaxb-maven-plugin-testing][45]

### Goals

New goals for the next release are:

* Review in progress.

### Status

* Review in progress.

#### Completed

* Review in progress.

### Fork History

#### Version 2.2.0

* Replaced old 'maven-compat' layer with Maven Resolver/Aether.
* Update Maven plugin and dependency versions.
* Build with JDK 21 and Java 11 compatibility.

#### Version 2.1.1

* Added sample project: [any-content](https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/any-content/README.md).
* Added sample project: [animal](https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/animal/README.md).
* Added sample project: [element](https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/element/README.md).
* Added sample project: [publisher](https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/publisher/README.md).
* Added sample project: [invoices](https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/invoices/README.md).
* Added sample project: [base-extend](https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/base-extend/README.md).
* Added sample project: [no-type](https://github.com/patrodyne/hisrc-higherjaxb/blob/master/assembly/samples/no-type/README.md).
* Update plugin and dependency versions.
* Resolved deprecated method(s).

#### Version 2.1.0

* Clean up Java compiler _lint_ warnings.
* Replace deprecated XJC plugin strategies v1 with v2.
* Recheck Dependency Management for newer versions.
* Configure menu, log and Maven options from build-CFG.sh.
* Include JVM system arguments from 'src/test/resources/jvmsystem.arguments'.
* Moved 'explore' and 'samples' folder to new 'assembly' folder and restored int. testing.
* Set log levels for basic strategies in simplelogger.properties.
* Compile sources and generate classes for Java release v11 using JDK 17.
* Add Maven plugin for JAXB 4.x, default.
* Replaced `maven.compiler.source/target="11"` with `maven.compiler.release="11"`.

#### Version 2.0.0

* Update dependencies with newer versions *including* the Jakarta namespace.
* Replace 'eclipse-only' lifecycle profile with 'm2e' XML directive.
* Convert DOS line endings to Unix newlines.
* Update JUnit v4 to JUnit v5.
* Rename packages:
    * BasicJAXB Annotations
        * `OLD: org.jvnet.annox`
        * `NEW: org.jvnet.basicjaxb_annox`
    * BasicJAXB XJC Plugin
        * `OLD: org.jvnet.jaxb2_commons`
        * `NEW: org.jvnet.basicjaxb`
    * HigherJAXB Maven Plugin
        * `OLD: org.jvnet.mjiip`
        * `NEW: org.jvnet.higherjaxb`
    * HigherJAXB Maven Mojo
        * `OLD: org.jvnet.jaxb2.maven2`
        * `NEW: org.jvnet.higherjaxb.mojo`
    * HyperJAXB Persistence
        * `OLD: org.jvnet.hyperjaxb[23]`
        * `NEW: org.jvnet.hyperjaxb`
* Rename namespaces:
    * BasicJAXB XJC Annotations
        * `OLD: http://annox.dev.java.net`
        * `NEW: http://jvnet.org/basicjaxb/xjc/annox`
    * BasicJAXB XJC Plugin
        * `OLD: http://jaxb2-commons.dev.java.net/basic`
        * `NEW: http://jvnet.org/basicjaxb/xjc`
    * HyperJAXB Persistence
        * `OLD: http://hyperjaxb3.jvnet.org/ejb/schemas/customizations`
        * `NEW: http://jvnet.org/hyperjaxb/jpa`
* Rename parameter property prefix:
    * `OLD: maven.xjc2`
    * `NEW: org.jvnet.higherjaxb.mojo.xjc`
* Update version to 2.0.0 due to jakarta and other name changes.

#### Version 0.14.2

* Added sample projects to GitHub releases.
* Updated dependencies with newer versions *excluding* the Jakarta namespace.

#### Version 0.14.1

* Obsolete build scripts have been removed.
* New build scripts have been added.
* POMs have been refactored with renamed artifacts.
* POMs have been updated to reduce warnings and errors.
* Configured SLF4J with SimpleLogger as the log framework.
* Updated Java sources to resolve warnings/errors.
* Verification of unit and integration tests.

<!-- References -->

  [1]: https://raw.githubusercontent.com/patrodyne/hisrc-hyperjaxb/master/etc/hisrc-repositories.svg
  [2]: https://github.com/highsource
  [3]: https://docs.github.com/en/account-and-profile/setting-up-and-managing-your-personal-account-on-github/managing-user-account-settings/managing-your-tab-size-rendering-preference
  [11]: https://github.com/patrodyne/hisrc-basicjaxb-annox#readme
  [12]: https://github.com/patrodyne/hisrc-basicjaxb#readme
  [13]: https://github.com/patrodyne/hisrc-higherjaxb#readme
  [14]: https://github.com/patrodyne/hisrc-hyperjaxb-annox#readme
  [15]: https://github.com/patrodyne/hisrc-hyperjaxb#readme
  [21]: https://github.com/highsource/annox/tree/1.0.2#readme
  [22]: https://github.com/highsource/jaxb2-basics/tree/0.12.0#readme
  [23]: https://github.com/highsource/maven-jaxb2-plugin/tree/0.14.0#readme
  [24]: https://github.com/highsource/jaxb2-annotate-plugin/tree/1.1.0#readme
  [25]: https://github.com/highsource/hyperjaxb3/tree/0.6.2#readme
  [31]: https://github.com/patrodyne/hisrc-higherjaxb/releases/tag/0.14.2
  [32]: https://github.com/patrodyne/hisrc-higherjaxb/releases/tag/2.0.0
  [33]: https://github.com/patrodyne/hisrc-higherjaxb/releases/tag/2.1.0
  [34]: https://github.com/patrodyne/hisrc-higherjaxb/releases/tag/2.1.1
  [40]: https://javadoc.io/doc/org.patrodyne.jvnet/hisrc-higherjaxb23-maven-plugin/latest/index.html
  [41]: https://javadoc.io/doc/org.patrodyne.jvnet/hisrc-higherjaxb30-maven-plugin/latest/index.html
  [42]: https://javadoc.io/doc/org.patrodyne.jvnet/hisrc-higherjaxb40-maven-plugin/latest/index.html
  [43]: https://javadoc.io/doc/org.patrodyne.jvnet/hisrc-higherjaxb-maven-plugin/latest/index.html
  [44]: https://javadoc.io/doc/org.patrodyne.jvnet/hisrc-higherjaxb-maven-plugin-core/latest/index.html
  [45]: https://javadoc.io/doc/org.patrodyne.jvnet/hisrc-higherjaxb-maven-plugin-testing/latest/index.html
