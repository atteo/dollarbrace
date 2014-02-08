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
package org.atteo.evo.filtering;

import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Property resolver based on predefined {@link Properties properties}.
 */
public class PropertiesPropertyResolver extends SimplePropertyResolver {
	private final Properties properties;

	public PropertiesPropertyResolver(@Nonnull Properties properties) {
		this.properties = properties;
	}

	@Override
	public String getProperty(String name) throws PropertyNotFoundException {
		return properties.getProperty(name);
	}
}
