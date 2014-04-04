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
package org.atteo.filtering;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.atteo.filtering.spi.Tokenizer;
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
	private final static class LoopCheckerPropertyFilter implements PropertyFilter {
		private final Set<String> inProgress = new HashSet<>();
		private final PropertyResolver resolver;

		private LoopCheckerPropertyFilter(PropertyResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public String getProperty(String name) throws PropertyNotFoundException {
			if (inProgress.contains(name)) {
				throw new CircularPropertyResolutionException(name);
			}
			inProgress.add(name);

			try {
				return resolver.resolveProperty(name, this);
			} finally {
				inProgress.remove(name);
			}
		}

		@Override
		public String filter(String name) throws PropertyNotFoundException {
			List<Tokenizer.Token> parts = Tokenizer.splitIntoTokens(name);
			StringBuilder result = new StringBuilder();

			for (Tokenizer.Token part : parts) {
				if (part.isProperty()) {
					String propertyValue = getProperty(part.getValue());
					if (propertyValue == null) {
						throw new PropertyNotFoundException(part.getValue());
					}
					result.append(propertyValue);
				} else {
					result.append(part.getValue());
				}
			}
			return result.toString();
		}

		@Override
		public void filter(Element element) throws PropertyNotFoundException {
			new XmlFiltering(this).filterElement(element);
		}
	}

	/**
	 * Returns property filter which resolves properties using provided resolvers.
	 * @param resolvers property resolvers
	 * @return property filter
	 */
	public static PropertyFilter getFilter(PropertyResolver... resolvers) {
		if (resolvers.length == 1) {
			return new LoopCheckerPropertyFilter(resolvers[0]);
		}
		return new LoopCheckerPropertyFilter(new CompoundPropertyResolver(resolvers));
	}

	/**
	 * Returns property filter which resolver properties using provided properties.
	 * <p>
	 * This is equivalent to {@link #getFilter(PropertyResolver...)} with {@link PropertiesPropertyResolver}
	 * as the sole parameter.
	 * </p>
	 * @param properties properties used for resolution
	 * @return property filter
	 */
	public static PropertyFilter getFilter(Properties properties) {
		return getFilter(new PropertiesPropertyResolver(properties));
	}

	/**
	 * Resolves property with the given property resolver.
	 * @param name property name to resolve
	 * @param resolver property resolver
	 * @return property value resolved with given resolver
	 * @throws PropertyNotFoundException  when property is not found
	 * @deprecated use {@link #getFilter(PropertyResolver...)}.{@link PropertyFilter#getProperty(String) getProperty(String)}
	 */
	@Deprecated
	public static String getProperty(String name, PropertyResolver resolver) throws PropertyNotFoundException {
		return new LoopCheckerPropertyFilter(resolver).getProperty(name);
	}

	/**
	 * Filters string by replacing properties denoted by the <code>${...}</code> delimeters with their resolved values.
	 * @param value the value to filter the properties into
	 * @param resolver resolver for the property values
	 * @return filtered value
	 * @throws PropertyNotFoundException when some property cannot be found
	 * @deprecated use {@link #getFilter(PropertyResolver...)}.{@link PropertyFilter#filter(String) filter(String)}
	 */
	@Deprecated
	public static String filter(String value, PropertyResolver resolver) throws PropertyNotFoundException {
		return new LoopCheckerPropertyFilter(resolver).filter(value);
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the value using given properties.
	 * @param value the value to filter the properties into
	 * @param properties properties to filter into the value
	 * @return filtered value
	 * @throws PropertyNotFoundException when some property cannot be found
	 * @deprecated use {@link #getFilter(Properties)}.{@link PropertyFilter#filter(String) filter(String)}
	 */
	@Deprecated
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
	 * @param element XML element to filter
	 * @param resolver property resolver
	 * @throws PropertyNotFoundException when some property could not be resolved
	 * @deprecated use {@link #getFilter(PropertyResolver...)}.{@link PropertyFilter#filter(Element) filter(Element)}
	 */
	@Deprecated
	public static void filter(Element element, PropertyResolver resolver) throws PropertyNotFoundException {
		getFilter(resolver).filter(element);
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the XML element.
	 *
	 * @param element XML element to filter
	 * @param properties properties to filter
	 * @throws PropertyNotFoundException when some property could not be resolved
	 * @deprecated use {@link #getFilter(Properties)}.{@link PropertyFilter#filter(Element) filter(Element)}
	 */
	@Deprecated
	public static void filter(Element element, Properties properties) throws PropertyNotFoundException {
		filter(element, new PropertiesPropertyResolver(properties));
	}

	private static class XmlFiltering {
		private final PropertyFilter propertyFilter;

		private XmlFiltering(PropertyFilter propertyResolver) {
			this.propertyFilter = propertyResolver;
		}

		public void filterElement(Element element) throws PropertyNotFoundException {
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
			return propertyFilter.filter(value);
		}
	}
}
