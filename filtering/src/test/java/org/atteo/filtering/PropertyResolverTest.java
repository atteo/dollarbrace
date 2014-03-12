/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.filtering;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PropertyResolverTest {
	@Test
	public void system() throws PropertyNotFoundException {
		System.setProperty("testKey", "testValue");

		assertEquals("system: testValue", Filtering.filter("system: ${testKey}",
				new SystemPropertyResolver()));
	}

	@Test
	public void env() throws PropertyNotFoundException {
		if (System.getenv("PATH") != null) {
			System.out.println("PATH not defined, environment based test skipped");
		}
		Filtering.filter("env: ${env.PATH}", new EnvironmentPropertyResolver());
	}

	@Test(expected = PropertyNotFoundException.class)
	public void envNotFound() throws PropertyNotFoundException {
		Filtering.filter("env: ${env.ASDFASICSAPWOECM_123}", new EnvironmentPropertyResolver());
	}

	@Test
	public void recursion() throws PropertyNotFoundException {
		System.setProperty("first", "value");
		System.setProperty("second", "${first} ${first}");
		System.setProperty("third", "${first} ${second}");

		PropertyResolver resolver = new SystemPropertyResolver();
		assertEquals("value value value", Filtering.filter("${third}", resolver));
	}

	@Test
	public void isRecursionSafe() throws PropertyNotFoundException {
		Properties properties = new Properties();
		properties.setProperty("first", "${");
		properties.setProperty("second", "test}");
		properties.setProperty("compound", "${first}${second}");
		PropertyResolver resolver = new PropertiesPropertyResolver(properties);
		assertEquals("${test}", Filtering.getProperty("compound", resolver));
	}

	@Test(expected = CircularPropertyResolutionException.class)
	public void circularRecursion() throws PropertyNotFoundException {
		System.setProperty("first", "${third}");
		System.setProperty("second", "${first} ${first}");
		System.setProperty("third", "${first} ${second}");

		PropertyResolver resolver = new SystemPropertyResolver();
		Filtering.filter("${third}", resolver);
	}

	@Test(expected = PropertyNotFoundException.class)
	public void shouldNotReportCircularRecursion() throws PropertyNotFoundException {
		PropertyResolver resolver = new CompoundPropertyResolver(new OneOfPropertyResolver());
		Filtering.filter("${oneof:${notfound},${notfound}}", resolver);
	}

	@Test
	public void raw() throws PropertyNotFoundException {
		PropertyResolver resolver = new RawPropertyResolver();
		assertEquals("${abc${abc}}abc", Filtering.getProperty("raw:${abc${abc}}abc", resolver));
	}

	@Test
	public void oneof() throws PropertyNotFoundException {
		Properties properties = new Properties();
		properties.setProperty("second", "value");
		PropertyResolver propertiesResolver = new PropertiesPropertyResolver(properties);
		PropertyResolver oneOfResolver = new OneOfPropertyResolver();
		PropertyResolver resolver = new CompoundPropertyResolver(propertiesResolver, oneOfResolver);
		assertEquals("value1", Filtering.filter("${oneof:1${abc}2,${second}1}", resolver));
		assertEquals("value", Filtering.filter("${oneof:${second}}", resolver));
		assertEquals("xx", Filtering.filter("${oneof:${abc},xx,yy,${cde}}", resolver));
	}

	@Test
	public void xml() throws ParserConfigurationException, SAXException, IOException, PropertyNotFoundException {
		String xml = "<config>"
				+ "<a value='test'/>"
				+ "<b>test2</b>"
				+ "<c><d>test3</d></c>"
				+ "<e><f>test4</f><f>test5</f></e>"
				+ "<g.h><i>test6</i></g.h>"
				+ "</config>";

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

		PropertyResolver resolver = new XmlPropertyResolver(document.getDocumentElement(), true);
		assertEquals("test", Filtering.getProperty("config.a.value", resolver));
		assertEquals("test2", Filtering.getProperty("config.b", resolver));
		assertEquals("test3", Filtering.getProperty("config.c.d", resolver));
		assertEquals("test6", Filtering.getProperty("config.g.h.i", resolver));

		resolver = new XmlPropertyResolver(document.getDocumentElement(), false);
		assertEquals("test", Filtering.getProperty("a.value", resolver));
		assertEquals("test6", Filtering.getProperty("g.h.i", resolver));
	}

	@Test(expected = PropertyNotFoundException.class)
	public void shouldThrowWhenPropertyIsNotFound() throws PropertyNotFoundException {
		Filtering.getProperty("a", new PropertyResolver() {
			@Override
			public String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException {
				throw new PropertyNotFoundException(name);
			}
		});
	}
}
