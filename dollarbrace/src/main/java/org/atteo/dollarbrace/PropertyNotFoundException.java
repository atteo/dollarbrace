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

	private final String propertyName;

	public PropertyNotFoundException(String propertyName) {
		this.propertyName = propertyName;
	}

	public PropertyNotFoundException(String propertyName, Throwable cause) {
		super(cause);
		this.propertyName = propertyName;
	}

	public PropertyNotFoundException(String propertyName, PropertyNotFoundException cause) {
		this.propertyName = propertyName;
		if ( cause != null && !propertyName.equals(cause.propertyName)) {
			initCause(cause);
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public String getMessage() {

		StringBuilder sb = new StringBuilder();
		sb.append("'").append(propertyName).append("'");

		Throwable cause = getCause();
		String lastProperty = null;

		while (cause != null) {
			if (cause instanceof PropertyNotFoundException) {
				lastProperty =((PropertyNotFoundException) cause).propertyName;
				sb.append(" -> '").append(lastProperty).append("'");
			}
			cause = cause.getCause();
		}
		if (lastProperty == null) {
			sb.insert(0, "Property not found: ");
		} else {
			sb.insert(0, "Property not found: '" + lastProperty + "' [");
			sb.append("]");
		}
		return sb.toString();
	}
}
