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
package org.atteo.dollarbrace;

import javax.annotation.Nonnull;

/**
 * Resolves property name to its value possibly resolving recursively any placeholders.
 */
public interface PropertyResolver {
	/**
	 * Get value for the given property.
	 * @param name name of the property to resolve
	 * @param filter property filter to use when resolving recursively
	 * @return value associated for the property
	 * @throws PropertyNotFoundException when property cannot be resolved
	 */
	@Nonnull
	String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException;
}
