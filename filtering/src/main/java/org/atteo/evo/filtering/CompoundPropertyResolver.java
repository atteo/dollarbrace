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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Resolves properties by trying a number of underlying {@link PropertyResolver property resolvers}.
 * <p>
 * Underlying property resolver can be an instance of {@link PrefixedPropertyResolver}.
 * In this case it will be executed only if its prefix matches property name.
 * If at least one prefixed resolver matches given prefix then only prefixed resolvers
 * matching this prefix will be executed.
 * </p>
 */
public class CompoundPropertyResolver implements PropertyResolver {
	private final List<PropertyResolver> resolvers = Lists.newArrayList();
	private final Multimap<String, PrefixedPropertyResolver> prefixedResolvers = ArrayListMultimap.create();

	public CompoundPropertyResolver(PropertyResolver... resolvers) {
		for (PropertyResolver resolver : resolvers) {
			addPropertyResolver(resolver);
		}
	}

	public final void addPropertyResolver(PropertyResolver resolver) {
		if (resolver instanceof PrefixedPropertyResolver) {
			PrefixedPropertyResolver prefixedResolver = (PrefixedPropertyResolver) resolver;
			String prefix = prefixedResolver.getPrefix();
			if (prefix != null) {
				prefixedResolvers.put(prefixedResolver.getPrefix(), prefixedResolver);
				return;
			}
		}
		resolvers.add(resolver);
	}

	@Override
	public String resolveProperty(String name, PropertyFilter recurse) throws PropertyNotFoundException {
		for (Entry<String, Collection<PrefixedPropertyResolver>> entry : prefixedResolvers.asMap().entrySet()) {
			if (name.startsWith(entry.getKey())) {
				for (PrefixedPropertyResolver resolver : entry.getValue()) {
					try {
						return resolver.resolveProperty(name, recurse);
					} catch (PropertyNotFoundException e) {
						// ok, try next one
					}
				}
				throw new PropertyNotFoundException(name);
			}
		}

		for (PropertyResolver resolver : resolvers) {
			try {
				return resolver.resolveProperty(name, recurse);
			} catch (PropertyNotFoundException e) {
				// ok, try next one
			}
		}
		throw new PropertyNotFoundException(name);
	}
}
