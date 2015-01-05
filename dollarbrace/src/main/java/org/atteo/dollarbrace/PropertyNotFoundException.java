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

/**
 * Returned when property with the given name is not found.
 */
@SuppressWarnings("serial")
public class PropertyNotFoundException extends Exception {
	public PropertyNotFoundException(String propertyName) {
		super("Property not found: '" + propertyName + "'");
	}

	public PropertyNotFoundException(String propertyName, Throwable cause) {
		super("Property not found: '" + propertyName + "': " + cause.getMessage(), cause);
	}
}
