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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.atteo.dollarbrace.spi.Tokenizer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Dollar-brace expressions filtering engine.
 */
public class DollarBrace {
	/**
	 * Property filter which throws {@link CircularPropertyResolutionException} when loop occurs.
	 */
	private final static class LoopCheckPropertyFilter implements PropertyFilter {
		private final Set<String> inProgress = new HashSet<>();
		private final PropertyResolver resolver;

		private LoopCheckPropertyFilter(PropertyResolver resolver) {
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
		public String filter(String value) throws PropertyNotFoundException {
			List<Tokenizer.Token> parts = Tokenizer.splitIntoTokens(value);
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

		@Override
		public void filterFile(Path source, Path destination) throws PropertyNotFoundException, IOException {
			String content = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
			String result = filter(content);

			Files.write(destination, result.getBytes(StandardCharsets.UTF_8));
		}
	}

	private static class PublicPropertyFilter implements PropertyFilter {
		private final PropertyResolver resolver;

		public PublicPropertyFilter(PropertyResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public String filter(String value) throws PropertyNotFoundException {
			return new LoopCheckPropertyFilter(resolver).filter(value);
		}

		@Override
		public String getProperty(String name) throws PropertyNotFoundException {
			return new LoopCheckPropertyFilter(resolver).getProperty(name);
		}

		@Override
		public void filter(Element element) throws PropertyNotFoundException {
			new LoopCheckPropertyFilter(resolver).filter(element);
		}

		@Override
		public void filterFile(Path source, Path destination) throws PropertyNotFoundException, IOException {
			new LoopCheckPropertyFilter(resolver).filterFile(source, destination);
		}
	}

	/**
	 * Returns property filter which resolves properties using provided resolvers.
	 * <p>
	 * The returned property filter is thread-safe.
	 * </p>
	 * @param resolvers property resolvers
	 * @return property filter
	 */
	public static PropertyFilter getFilter(PropertyResolver... resolvers) {
		if (resolvers.length == 1) {
			return new PublicPropertyFilter(resolvers[0]);
		}
		return new PublicPropertyFilter(new CompoundPropertyResolver(resolvers));
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
