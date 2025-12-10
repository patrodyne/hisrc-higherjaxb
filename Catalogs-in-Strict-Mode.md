### Catalogs in Strict Mode

There is a potential conflict when the **XJC** binding compiler is configured to use both catalog files and its default strict validation mode simultaneously. 

+ **XJC** (Jakarta XML Binding / JAXB Binding Compiler) is a tool that generates Java classes from XML schemas (XSDs).
+ By default, **XJC** performs a strict validation of the source schema.
+ Catalog files are used to map external entity references (like DTDs or imported schemas) to local files, helping resolve them offline or in specific environments.
+ **XJC** now uses new XML Catalog API to support the Organization for the Advancement of Structured Information Standards (OASIS) introduced in Java SE 9.

#### The Conflict

When **XJC** is run with the default strict mode and a catalog, the underlying XML parser might still try to access the original schema location over the network before using the catalog mapping, which can cause failures (e.g., `FileNotFoundException`) if the network location is unreachable.

#### The Resolution

To avoid this issue, you can modify the **XJC** behavior to use a less strict validation or configure the environment to better use the catalog. A common solution in build tools like the `hisrc-higherjaxb-maven-plugin` is to explicitly set the `<strict>` parameter to `false`. 

**Example Maven `pom.xml` snippet to resolve the warning:**

~~~
xml

<configuration>
    <!-- ... other configurations ... -->
    <strict>false</strict>
    <!-- ... -->
</configuration>
~~~

Alternatively, you can use the `-nv` command-line option, which disables _strict_ schema validation, allowing the catalog resolver to function more smoothly. Note that this does not disable all validation, just the most strict rules. 
