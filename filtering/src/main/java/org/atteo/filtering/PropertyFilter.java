/*
 * Copyright 2014 Atteo.
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

import org.w3c.dom.Element;

/**
 * Property filtering operations.
 */
public interface PropertyFilter {
	/**
	 * Filters string by replacing properties denoted by the ${...} delimeters with their resolved values.
	 * @param value string which is parsed.
	 * @return filtered value
	 * @throws PropertyNotFoundException when some property could not be resolved
	 */
	String filter(String value) throws PropertyNotFoundException;

	/**
	 * Returns value for property with given name.
	 * @param name name of the property
	 * @return value of the property
	 * @throws PropertyNotFoundException when property is not found
	 */
	String getProperty(String name) throws PropertyNotFoundException;

	/**
	 * Filters XML tree replacing properties denoted by the ${} found in attribute values or tag content.
	 * @param element XML element to filter
	 * @throws PropertyNotFoundException when some property could not be resolved
	 */
	void filter(Element element) throws PropertyNotFoundException;
}
