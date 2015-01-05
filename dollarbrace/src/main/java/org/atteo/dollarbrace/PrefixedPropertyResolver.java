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
 * Property resolver which handles properties with given prefix.
 */
public interface PrefixedPropertyResolver extends PropertyResolver {
	/**
	 * Prefix which this property resolver handles.
	 * {@link CompoundPropertyResolver} will not execute this resolver if prefix does not match
	 * property name. If at least one resolver matches the prefix, CompoundPropertyResolver
	 * will not execute any of the non-matching {@link PropertyResolver Resolvers}.
	 * @return prefix, or null if resolver is not prefixed
	 */
	String getPrefix();
}
