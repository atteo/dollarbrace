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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Properties filtering engine.
 */
public class Filtering {
	private final static Logger logger = LoggerFactory.getLogger(Filtering.class);

	private final static class LoopCheckerResolver implements PropertyResolver {
		private final Set<String> inProgress = new HashSet<String>();
		private final PropertyResolver resolver;

		private LoopCheckerResolver(PropertyResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public String resolveProperty(String name, PropertyResolver ignore) throws PropertyNotFoundException {
			if (inProgress.contains(name)) {
				throw new CircularPropertyResolutionException(name);
			}
			logger.debug("Resolving property: " + name);
			inProgress.add(name);

			String value = resolver.resolveProperty(name, this);

			inProgress.remove(name);

			return value;
		}
	}

	public static final class Token {
		private final String value;
		private final boolean property;

		public Token(String value, boolean property) {
			this.value = value;
			this.property = property;
		}

		public String getValue() {
			return value;
		}

		public boolean isProperty() {
			return property;
		}
	}

	public static String getProperty(String value, PropertyResolver resolver) throws PropertyNotFoundException {
		// optimization: we don't need another one
		if (resolver instanceof LoopCheckerResolver) {
			return resolver.resolveProperty(value, null);
		}
		return new LoopCheckerResolver(resolver).resolveProperty(value, resolver);
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the value using given property resolver.
	 * @param value the value to filter the properties into
	 * @param propertyResolver resolver for the property values
	 * @return filtered value
	 * @throws PropertyNotFoundException when some property cannot be found
	 */
	public static String filter(String value, PropertyResolver propertyResolver)
			throws PropertyNotFoundException {
		List<Token> parts = splitIntoTokens(value);
		StringBuilder result = new StringBuilder();

		for (Token part : parts) {
			if (part.isProperty()) {
				String propertyValue = getProperty(part.getValue(), propertyResolver);
				if (propertyValue == null) {
					throw new PropertyNotFoundException(part.getValue());
					//result.append(value.subSequence(position, endposition + 1));
					//break;
				}
				result.append(propertyValue);
			} else {
				result.append(part.getValue());
			}
		}
		return result.toString();
	}

	/**
	 * Splits given string into {@link Token tokens}.
	 * <p>
	 * Token is ordinary text or property placeholder: <code>${name}</code>.
	 * For instance the string: "abc${abc}abc" will be split
	 * into three tokens: text "abc", property "abc" and text "abc".
	 * </p>
	 * @param input input string to split into tokens
	 * @return list of tokens
	 */
	public static List<Token> splitIntoTokens(String input) {
		List<Token> parts = new ArrayList<Token>();
		int index = 0;

		while (true) {
			int startPosition = input.indexOf("${", index);
			if (startPosition == -1) {
				break;
			}
			// find '${' and '}' pair, correctly handle nested pairs
			boolean lastDollar = false;
			int count = 1;
			int countBrace = 0;
			int endposition;
			for (endposition = startPosition + 2; endposition < input.length(); endposition++) {
				if (input.charAt(endposition) == '$') {
					lastDollar = true;
					continue;
				}
				if (input.charAt(endposition) == '{') {
					if (lastDollar) {
						count++;
					} else {
						countBrace++;
					}
				} else if (input.charAt(endposition) == '}') {
					if (countBrace > 0) {
						countBrace--;
					} else {
						count--;
						if (count == 0) {
							break;
						}
					}
				}
				lastDollar = false;
			}

			if (count > 0) {
				break;
			}

			if (index != startPosition) {
				parts.add(new Token(input.substring(index, startPosition), false));
			}

			String propertyName = input.substring(startPosition + 2, endposition);
			index = endposition + 1;

			parts.add(new Token(propertyName, true));
		}

		if (index != input.length()) {
			parts.add(new Token(input.substring(index), false));
		}
		return parts;
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the value using given properties.
	 * @param value the value to filter the properties into
	 * @param properties properties to filter into the value
	 * @return filtered value
	 * @throws PropertyNotFoundException when some property cannot be found
	 */
	public static String filter(String value, final Properties properties)
			throws PropertyNotFoundException {

		return filter(value, new PropertiesPropertyResolver(properties));
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the XML element.
	 *
	 * <p>
	 * The structure of the XML document is not changed. Each attribute and element text is filtered
	 * separately.
	 * </p>
	 * @param element
	 * @param propertyResolver
	 * @throws PropertyNotFoundException
	 */
	public static void filter(Element element, PropertyResolver propertyResolver)
			throws PropertyNotFoundException {
		XmlFiltering filtering = new XmlFiltering(propertyResolver);
		filtering.filterElement(element);
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the XML element.
	 *
	 * @see #filter(Element, PropertyResolver)
	 */
	public static void filter(Element element, Properties properties)
			throws PropertyNotFoundException {
		filter(element, new PropertiesPropertyResolver(properties));
	}

	private static class XmlFiltering {
		private final PropertyResolver propertyResolver;

		private XmlFiltering(PropertyResolver propertyResolver) {
			this.propertyResolver = propertyResolver;
		}

		private void filterElement(Element element) throws PropertyNotFoundException {
			NamedNodeMap attributes = element.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node node = attributes.item(i);
				filterAttribute((Attr) node);
			}

			NodeList nodes = element.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				switch (node.getNodeType()) {
					case Node.ELEMENT_NODE:
						filterElement((Element) node);
						break;
					case Node.TEXT_NODE:
						filterText((Text) node);
						break;
				}
			}
		}

		private void filterAttribute(Attr attribute) throws PropertyNotFoundException {
			attribute.setValue(filterString(attribute.getValue()));
		}

		private void filterText(Text text) throws PropertyNotFoundException {
			text.setTextContent(filterString(text.getTextContent()));
		}

		private String filterString(String value) throws PropertyNotFoundException {
			return Filtering.filter(value, propertyResolver);
		}
	}
}
