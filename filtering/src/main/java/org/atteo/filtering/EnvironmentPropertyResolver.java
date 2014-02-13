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

/**
 * Resolves any properties prefixed with 'env.' as {@link System#getenv(String) environment
 * variables}.
 * <p>
 * For instance {@code ${env.PATH}} will be resolved into the value of the '{@code PATH}' environment variable.
 * </p>
 * <p>
 * Due to security concerns it does not recursively resolve properties by default.
 * </p>
 */
public class EnvironmentPropertyResolver extends SimplePropertyResolver {
	private final static String prefix = "env.";

	public EnvironmentPropertyResolver() {
		filterResult = false;
	}

	@Override
	public String getProperty(String name) throws PropertyNotFoundException {
		if (!name.startsWith(prefix)) {
			throw new PropertyNotFoundException(name);
		}
		name = name.substring(prefix.length());
		return System.getenv(name);
	}
}
