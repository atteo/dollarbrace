[![Build Status](https://travis-ci.org/atteo/dollarbrace.svg)](https://travis-ci.org/atteo/dollarbrace)
[![Coverage Status](https://img.shields.io/coveralls/atteo/dollarbrace.svg)](https://coveralls.io/r/atteo/dollarbrace)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.atteo/dollarbrace/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.atteo.dollarbrace/dollarbrace)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Overview
========

DollarBrace library provides a mechanisms to filter strings, files and XML trees to perform property resolution
for a common dollar-brace expressions. For instance, given document

```xml
<name>${name}</name>
```

and properties file

```
name=Stephan
```

The filtering result will be
```xml
<name>Stephan</name>
```

Changes
=======
1.3 (2014-11-30)
	fix: PropertyFilter returned from getFilter was not thread-safe

1.2 (2014-04-06)
    Added file filtering

1.1 (2014-03-12)
	fix: incorrectly returning circular dependencies for missing properties

1.0 (2014-02-13)
	Splitted from evo-config
	Separated filtering (PropertyFilter) from property resolution (PropertyResolver)

Usage
=====
First add the following dependency to your POM file:
```xml
<dependency>
    <groupId>org.atteo.dollarbrace</groupId>
    <artifactId>dollarbrace</artifactId>
    <version>2.0</version>
</dependency>
```

Then create a property resolver instance:
```java
Properties properties = new Properties();
properties.putProperty("name", "Stephan");
PropertyResolver propertyResolver = new PropertiesPropertyResolver(properties);
```

Finally create a property filter and use it to interpolate dollar-brace expression within a string:
```java
PropertyFilter propertyFilter = DollarBrace.getFilter(propertyResolver);

// will print 'His name is Stephan'
System.out.println(propertyFilter.filter("His name is ${name}"));
```

Property filter
===============

DollarBrace.getFilter(...) returns a PropertyFilter instance. It contains several methods which allow you to interpolate dollar-brace expressions:

* filter(String) - filters given string interpolating dollar-brace expressions inside
* filter(Element) - filters given XML subtree interpolating tag content and attribute values
* filter(Path source, Path destination) - filters source file and stores the result in the destination file
* getProperty(String) - returns the value of the given property


Property resolvers
==================

DollarBrace.getFilter(resolver1, resolver2, ...) method takes a number of property resolvers. Each resolver will be tried in turn to get the values for dollar-brace expressions.

There are several PropertyResolvers already available for your convenience:

EnvironmentPropertyResolver
---------------------------

Resolves ${env.NAME} with the value of environment variable named NAME.

SystemPropertyResolver
----------------------
Resolves ${NAME} from the Java system property provided with -DNAME="value"

PropertiesPropertyResolver
--------------------------
Resolves property from the provided Properties object.

XmlPropertyResolver
-------------------
Takes XML DOM tree as an input and resolves any ${tag.tag.tag} as a dot-separated path to the XML element.
  For instance, given document:
```xml
<a>
	<b>test</b>
</a>
```

"${a.b}" will resolve to "test".

OneOfPropertyResolver
---------------------
Matches any name prefixed with 'oneof:' and formatted as comma separated list of values - 'oneof:value1,value2,value3'. It returns first of the values which is correctly defined.

For instance

    '${oneof:${env.HOME},~/}'

will return the value of the environment variable 'HOME' or '~/' if the 'HOME' variable is undefined

RawPropertyResolver
-------------------
Matches any name prefixed with 'raw:' and returns the same string without 'raw:' prefix. This is handy in case some special characters would otherwise trigger recursive filtering.

For instance

    '${raw:${a}}'

will return exactly '${a}' (without trying to resolve dollar brace inside it).

JaninoPropertyResolver
----------------------
Matches any name prefixed with 'java:'. It treats the following string as Java expression which is executed to obtain the value.

For instance
    '${java:2+2}'

will return '4' and

    '${java:new java.util.Date()'}

will return current date and time.

If the returned Java object is not a String type, 'toString()' method is automatically executed.

To use this resolver you need to add additional Maven dependency:
```xml
<dependency>
    <groupId>org.atteo.dollarbrace</groupId>
    <artifactId>janino</artifactId>
    <version>1.3</version>
</dependency>
```

Custom property resolver
========================

Basics
------
You can easily prepare your own property resolvers by implementing PropertyResolver interface.

```java
public interface PropertyResolver {
	String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException;
}
```

For instance here is the resolver which matches on a "date" string and always returns current date and time:

```java
public class DatePropertyResolver implements PropertyResolver {
	String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException() {
		if ("date".equals(name)) {
			return new Date().toString();
		}
		throw new PropertyNotFoundException(name);
	}
}
```

Recursive resolution
--------------------

PropertyResolvers can support recursive resolution. That is one dollar-brace expression can be itself built using several other dollar-brace expressions like this:

```
properties.putProperty("name1", "John");
properties.putProperty("number", "1");
PropertyFilter propertyFilter = DollarBrace.getFilter(new PropertiesPropertyResolver(properties));

System.out.println(propertyFilter.filter("My name is ${name${number}}"));
```

To support recursive resolution your custom property resolver must execute extra step to filter property name using the provided PropertyFilter. Here is the example:

```java
public class DatePropertyResolver implements PropertyResolver {
	String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException() {
		// filter the name to support recursive resolution
		name = filter.filter(name);

		if ("date".equals(name)) {
			return new Date().toString();
		}
		throw new PropertyNotFoundException(name);
	}
}
```

