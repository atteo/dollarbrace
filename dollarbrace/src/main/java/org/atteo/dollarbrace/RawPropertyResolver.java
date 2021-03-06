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
package org.atteo.dollarbrace;

/**
 * Resolver which returns the same string as provided.
 */
public class RawPropertyResolver implements PrefixedPropertyResolver {
	private static final String prefix = "raw:";

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public String resolveProperty(String name, PropertyFilter resolver) throws PropertyNotFoundException {
		if (!name.startsWith(prefix)) {
			throw new PropertyNotFoundException(name);
		}
		return name.substring(prefix.length());
	}
}
