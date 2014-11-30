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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PropertyFilterTest {
	@Test
	public void system() throws PropertyNotFoundException {
		// given
		System.setProperty("testKey", "testValue");

		// when
		String result = Filtering.getFilter(new SystemPropertyResolver()).filter("system: ${testKey}");

		// then
		assertThat(result).isEqualTo("system: testValue");
	}

	@Test
	public void env() throws PropertyNotFoundException {
		// given
		if (System.getenv("PATH") != null) {
			System.out.println("PATH not defined, environment based test skipped");
			return;
		}

		// when
		String result = Filtering.getFilter(new EnvironmentPropertyResolver()).filter("env: ${env.PATH}");

		// then
		assertThat(result).isNotNull();
	}

	@Test(expected = PropertyNotFoundException.class)
	public void envNotFound() throws PropertyNotFoundException {
		// when
		Filtering.getFilter(new EnvironmentPropertyResolver()).filter("env: ${env.ASDFASICSAPWOECM_123}");
	}

	@Test
	public void recursion() throws PropertyNotFoundException {
		// given
		System.setProperty("first", "value");
		System.setProperty("second", "${first} ${first}");
		System.setProperty("third", "${first} ${second}");

		// when
		String result = Filtering.getFilter(new SystemPropertyResolver()).filter("${third}");

		// then
		assertThat(result).isEqualTo("value value value");
	}

	@Test
	public void isRecursionSafe() throws PropertyNotFoundException {
		// given
		Properties properties = new Properties();
		properties.setProperty("first", "${");
		properties.setProperty("second", "test}");
		properties.setProperty("compound", "${first}${second}");

		// when
		String result = Filtering.getFilter(new PropertiesPropertyResolver(properties)).getProperty("compound");

		// then
		assertThat(result).isEqualTo("${test}");
	}

	@Test(expected = CircularPropertyResolutionException.class)
	public void circularRecursion() throws PropertyNotFoundException {
		// given
		System.setProperty("first", "${third}");
		System.setProperty("second", "${first} ${first}");
		System.setProperty("third", "${first} ${second}");

		// when
		Filtering.getFilter(new SystemPropertyResolver()).filter("${third}");
	}

	@Test(expected = PropertyNotFoundException.class)
	public void shouldNotReportCircularRecursion() throws PropertyNotFoundException {
		// given
		PropertyFilter filter = Filtering.getFilter(new CompoundPropertyResolver(new OneOfPropertyResolver()));

		// when
		String result = filter.filter("${oneof:${notfound},${notfound}}");
	}

	@Test
	public void raw() throws PropertyNotFoundException {
		// given
		PropertyFilter filter = Filtering.getFilter(new RawPropertyResolver());

		// when
		String result = filter.getProperty("raw:${abc${abc}}abc");

		// then
		assertThat(result).isEqualTo("${abc${abc}}abc");
	}

	@Test
	public void oneof() throws PropertyNotFoundException {
		// given
		Properties properties = new Properties();
		properties.setProperty("second", "value");
		PropertyFilter filter = Filtering.getFilter(new OneOfPropertyResolver(),
				new PropertiesPropertyResolver(properties));

		// then
		assertThat(filter.filter("${oneof:1${abc}2,${second}1}")).isEqualTo("value1");
		assertThat(filter.filter("${oneof:${second}}")).isEqualTo("value");
		assertThat(filter.filter("${oneof:${abc},xx,yy,${cde}}")).isEqualTo("xx");
	}

	@Test
	public void xml() throws ParserConfigurationException, SAXException, IOException, PropertyNotFoundException {
		// given
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

		PropertyFilter filter = Filtering.getFilter(new XmlPropertyResolver(document.getDocumentElement(), true));
		PropertyFilter rootFilter = Filtering.getFilter(new XmlPropertyResolver(document.getDocumentElement(), false));

		// then
		assertThat(filter.getProperty("config.a.value")).isEqualTo("test");
		assertThat(filter.getProperty("config.b")).isEqualTo("test2");
		assertThat(filter.getProperty("config.c.d")).isEqualTo("test3");
		assertThat(filter.getProperty("config.g.h.i")).isEqualTo("test6");

		assertThat(rootFilter.getProperty("a.value")).isEqualTo("test");
		assertThat(rootFilter.getProperty("g.h.i")).isEqualTo("test6");
	}

	@Test(expected = PropertyNotFoundException.class)
	public void shouldThrowWhenPropertyIsNotFound() throws PropertyNotFoundException {
		// given
		PropertyFilter filter = Filtering.getFilter(new PropertyResolver() {
			@Override
			public String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException {
				throw new PropertyNotFoundException(name);
			}
		});

		// then
		filter.getProperty("a");
	}

	@Test
	public void shouldFilterFile() throws IOException, PropertyNotFoundException {
		// given
		Path source = Paths.get("target", "source");
		Path destination = Paths.get("target", "destination");
		Files.write(source, "key: ${key}".getBytes(StandardCharsets.UTF_8));
		Properties properties = new Properties();
		properties.setProperty("key", "value");
		PropertyFilter filter = Filtering.getFilter(new PropertiesPropertyResolver(properties));

		// when
		filter.filterFile(source, destination);

		// then
		assertThat(destination.toFile()).usingCharset(StandardCharsets.UTF_8).hasContent("key: value");
	}

	@Test
	public void shouldFilterConcurrently() throws InterruptedException {
		final PropertyFilter filter = Filtering.getFilter(new PropertyResolver() {
			@Override
			public String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException {
				try {
					Thread.currentThread().sleep(2000l);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return "result";
			}
		});

		class SleepThread extends Thread {
			@Override
			public void run() {
				try {
					filter.getProperty("test");
				} catch (PropertyNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}

		final List<Throwable> exceptions = new ArrayList<>();

		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				exceptions.add(e);
			}
		};

		Thread thread1 = new SleepThread();
		Thread thread2 = new SleepThread();
		thread1.setUncaughtExceptionHandler(handler);
		thread2.setUncaughtExceptionHandler(handler);

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		assertThat(exceptions).isEmpty();
	}
}
