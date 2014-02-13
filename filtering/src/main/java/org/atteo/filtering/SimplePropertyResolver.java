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
package org.atteo.filtering;

import javax.annotation.Nonnull;

/**
 * Simple property resolver which provides the value for some name.
 */
public abstract class SimplePropertyResolver implements PropertyResolver {
	protected boolean filterResult = true;

	/**
	 * Specifies if the value returned by {@link #getProperty(String)} should be filtered.
	 * @param filterResult when true, the result of {@link #getProperty(String)} will be recursively filtered
	 */
	public void setFilterResult(boolean filterResult) {
		this.filterResult = filterResult;
	}

	@Override
	@Nonnull
	public String resolveProperty(String name, PropertyFilter resolver) throws PropertyNotFoundException {
		name = resolver.filter(name);
		String value = getProperty(name);
		if (filterResult) {
			return resolver.filter(value);
		} else {
			return value;
		}
	}

	/**
	 * Returns the value for the property with given name.
	 * If {@link #setFilterResult(boolean)} was set to true, the returned value will be filtered.
	 * @param name name of the property
	 * @return property value
	 * @throws PropertyNotFoundException when property cannot be found
	 */
	@Nonnull
	public abstract String getProperty(String name) throws PropertyNotFoundException;
}
