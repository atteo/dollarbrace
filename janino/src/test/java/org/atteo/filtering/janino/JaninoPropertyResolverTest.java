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
package org.atteo.filtering.janino;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.filtering.Filtering;
import org.atteo.filtering.PropertiesPropertyResolver;
import org.atteo.filtering.PropertyFilter;
import org.atteo.filtering.PropertyNotFoundException;
import org.junit.Test;

public class JaninoPropertyResolverTest {
	@Test
	public void simple() throws PropertyNotFoundException {
		// given
		PropertyFilter filter = Filtering.getFilter(new JaninoPropertyResolver());

		// when
		String result = filter.getProperty("java:new java.util.Date()");
		String result2 = filter.getProperty("java:3+3");

		// then
		assertThat(result).isNotNull();
		assertThat(result2).isEqualTo("6");
	}

	@Test(expected = PropertyNotFoundException.class)
	public void noPrefix() throws PropertyNotFoundException {
		// given
		PropertyFilter filter = Filtering.getFilter(new JaninoPropertyResolver());

		// when
		filter.getProperty("3+3");
	}

	@Test(expected = RuntimeException.class)
	public void forced() throws PropertyNotFoundException {
		// given
		PropertyFilter filter = Filtering.getFilter(new JaninoPropertyResolver());

		// when
		filter.getProperty("java: asdf");
	}

	@Test
	public void compound() throws PropertyNotFoundException {
		// given
		Properties properties = new Properties();
		properties.setProperty("test1", "${java:3+3}");
		properties.setProperty("test2", "${test${java:2-1}}");
		PropertyFilter filter = Filtering.getFilter(
				new JaninoPropertyResolver(),
				new PropertiesPropertyResolver(properties));

		// when
		String result = filter.getProperty("test2");

		// then
		assertThat(result).isEqualTo("6");
	}

	@Test(expected = PropertyNotFoundException.class)
	public void notFound() throws PropertyNotFoundException {
		// given
		Properties properties = new Properties();
		properties.setProperty("test2", "${test${java:2-1}}");
		PropertyFilter filter = Filtering.getFilter(new JaninoPropertyResolver(),
				new PropertiesPropertyResolver(properties));

		// when
		filter.getProperty("test2");
	}

	@Test
	public void executingFunctionThrowingCheckedException() throws PropertyNotFoundException {
		// given
		PropertyFilter filter = Filtering.getFilter(new JaninoPropertyResolver());

		// when
		filter.getProperty("java: java.lang.String.class.getResource(\"/java/lang/String.class\")"
				+ ".toURI().toString()");
	}
}
