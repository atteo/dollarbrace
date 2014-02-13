Overview
========

Atteo Filtering provides mechanisms to filter strings, files and XML trees to perform property resolution.

Changes
=======
1.0 (2014-02-13)
	Splitted from evo-config
	Separated filtering (PropertyFilter) from property resolution (PropertyResolver)

Basic usage
===========
To resolve any ${name} in given string from given Properties object just execute:

```java
Properties properties;
String contentToFilter;

Filtering.filter(contentToFilter, new  PropertiesPropertyResolve(properties));
```

In-depth description
====================

Filtering.filter(String, PropertyResolver) method allows you to filter the string by replacing any ${name}
with the value produced by the provided resolver.

Each resolver is required to implement PropertyResolver interface:

```java
public interface PropertyResolver {
	String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException;
}
```

For instance here is the resolver which always returns current date and time:

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

It is pretty easy to implement the resolver, but the library already provides a number of ready to use resolvers:

* EnvironmentPropertyResolver - resolves ${env.NAME} with the value of environment variable named NAME.
* SystemPropertyResolver - resolves ${NAME} from the Java system property provided with -DNAME="value"
* PropertiesPropertyResolver - resolves property from provided Properties object.
* XmlPropertyResolver - takes XML DOM tree as a parameter and resolves any ${NAME} as a dot-separated path to the XML element.
  For instance, given document:
```xml
	<a>
		<b>test</b>
	</a>
```

	${a.b} will resolve to "test".
* OneOfPropertyResolver - resolves any name prefixed with 'oneof:' and formatted as comma separated list of values - 'oneof:value1,value2,value3' and returns first value which is correctly defined, for instance '${oneof:${env.HOME},~/}' returns the value of the environment variable 'HOME' or '~/' if the 'HOME' variable is undefined
* RawPropertyResolver - resolves any name prefixed with 'raw:' and returns the same string without 'raw:' prefix. This is handy in case some special characters would otherwise trigger recursive filtering, for instance '${raw:${a}}' returns '${a}' without trying to resolve '${a}'.
* JaninoPropertyResolver - resolves any name prefixed with 'java:', treats the following string as Java expression which is executed to obtain the value, if the returned value is not a string 'toString()' method is automatically executed. For instance '${java:2+2}' will return '4', '${java:new java.util.Date()'} will return current date and time. To use this resolver you need to add additional Maven dependency:

```xml
<dependency>
    <groupId>org.atteo.filtering</groupId>
    <artifactId>janino</artifactId>
    <version>1.0</version>
</dependency>
```

* CompoundPropertyResolver - allows to combine a number of other property resolvers which are executed in turn till any returns some value.
  Example:

```java
new CompoundPropertyResolver(new EnvironmentPropertyResolver(), new SystemPropertyResolver());
```

This will create property resolver which supports both environment and system properties.

