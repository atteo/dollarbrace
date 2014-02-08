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
package org.atteo.evo.janino;

import java.util.Properties;

import org.atteo.evo.filtering.CompoundPropertyResolver;
import org.atteo.evo.filtering.Filtering;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyNotFoundException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class JaninoPropertyResolverTest {
	@Test
	public void simple() throws PropertyNotFoundException {
		JaninoPropertyResolver resolver = new JaninoPropertyResolver();
		String result = Filtering.getProperty("java:new java.util.Date()", resolver);
		assertNotNull(result);

		result = Filtering.getProperty("java:3+3", resolver);
		assertEquals("6", result);
	}

	@Test
	public void noPrefix() throws PropertyNotFoundException {
		JaninoPropertyResolver resolver = new JaninoPropertyResolver();

		String result = Filtering.getProperty("3+3", resolver);
		assertEquals(null, result);
	}

	@Test(expected = RuntimeException.class)
	public void forced() throws PropertyNotFoundException {
		JaninoPropertyResolver resolver = new JaninoPropertyResolver();
		Filtering.getProperty("java: asdf", resolver);
	}

	@Test
	public void compound() throws PropertyNotFoundException {
		Properties properties = new Properties();
		properties.setProperty("test1", "${java:3+3}");
		properties.setProperty("test2", "${test${java:2-1}}");
		CompoundPropertyResolver resolver = new CompoundPropertyResolver(
				new JaninoPropertyResolver(),
				new PropertiesPropertyResolver(properties));
		assertEquals("6", Filtering.getProperty("test2", resolver));
	}

	@Test(expected = PropertyNotFoundException.class)
	public void notFound() throws PropertyNotFoundException {
		Properties properties = new Properties();
		properties.setProperty("test2", "${test${java:2-1}}");
		CompoundPropertyResolver resolver = new CompoundPropertyResolver(
				new JaninoPropertyResolver(),
				new PropertiesPropertyResolver(properties));
		Filtering.getProperty("test2", resolver);
	}

	@Test
	public void executingFunctionThrowingCheckedException() throws PropertyNotFoundException {
		Filtering.getProperty("java: java.lang.String.class.getResource(\"/java/lang/String.class\")"
				+ ".toURI().toString()", new JaninoPropertyResolver());
	}
}
