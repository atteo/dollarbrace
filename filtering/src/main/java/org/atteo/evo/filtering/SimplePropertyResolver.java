/*
 * Copyright 2012 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.filtering;

/**
 * Simple property resolver which provides the value for some name.
 */
public abstract class SimplePropertyResolver implements PropertyResolver {
	protected boolean filterResult = true;

	/**
	 * If true, will filter the value returned by {@link #getProperty(String)}.
	 */
	public void setFilterResult(boolean filterResult) {
		this.filterResult = filterResult;
	}

	@Override
	public String resolveProperty(String name, PropertyResolver resolver) throws PropertyNotFoundException {
		name = Filtering.filter(name, resolver);
		String value = getProperty(name);
		if (value == null) {
			return null;
		}
		if (filterResult) {
			return Filtering.filter(value, resolver);
		} else {
			return value;
		}
	}

	/**
	 * Returns property value which will be optionally filtered for any properties.
	 * Otherwise should behave the same as {@link PropertyResolver#resolveProperty(String, PropertyResolver)}.
	 */
	public abstract String getProperty(String name) throws PropertyNotFoundException;
}
