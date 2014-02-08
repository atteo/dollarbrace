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

import java.util.List;

import org.atteo.evo.filtering.spi.Tokenizer;

import com.google.common.base.Splitter;

public class OneOfPropertyResolver implements PrefixedPropertyResolver {
	private static final String prefix = "oneof:";

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public String resolveProperty(String name, PropertyFilter resolver) throws PropertyNotFoundException {
		if (!name.startsWith(prefix)) {
			return null;
		}
		name = name.substring(prefix.length());
		List<Tokenizer.Token> tokens = Tokenizer.splitIntoTokens(name);

		StringBuilder result = new StringBuilder();
		boolean skip = false;

		for (Tokenizer.Token token : tokens) {
			if (token.isProperty()) {
				if (!skip) {
					try {
						result.append(resolver.getProperty(token.getValue()));
					} catch (PropertyNotFoundException e) {
						skip = true;
					}
				}
			} else {
				boolean first = true;
				for (String p : Splitter.on(',').split(token.getValue())) {
					if (!first) {
						if (!skip) {
							return result.toString();
						} else {
							result = new StringBuilder();
							skip = false;
						}
					}
					if (!skip) {
						result.append(p);
					}
					first = false;
				}
			}
		}
		if (skip) {
			throw new PropertyNotFoundException(name);
		}

		return result.toString();
	}
}
