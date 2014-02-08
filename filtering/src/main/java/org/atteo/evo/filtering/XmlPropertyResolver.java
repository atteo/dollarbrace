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

import javax.annotation.Nullable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Get property value from XML by taking text content of a node pointed by property name.
 *
 * <p>
 * The resolver tries to search for an element with a given property name. If none is found
 * it tries to interpret dots (".") as separator between parent and children element names.
 * For instance for document:
 * <pre>
 * {@code
 * <a>
 *     <b>
 *         <c>test</c>
 *     </b>
 *     <e><f>test3</f></e>
 *     <e.f>test2</e.f>
 * </a>
 * }
 * </pre>
 * property {@code ${a.b.c}} will return 'test' value and property {@code ${a.e.f}} will return 'test2' value.
 * </p>
 */
public class XmlPropertyResolver extends SimplePropertyResolver {
	private Element rootElement;
	private boolean matchRoot;

	/**
	 * Create new property resolver based on XML tree.
	 * @param rootElement root element of an XML tree to search for property value
	 * @param matchRoot whether root element should match, or matching should start from rootElement children
	 */
	public XmlPropertyResolver(@Nullable Element rootElement, boolean matchRoot) {
		this.rootElement = rootElement;
		this.matchRoot = matchRoot;
	}

	@Override
	public String getProperty(String name) throws PropertyNotFoundException {
		String value = getValue(name);
		if (value == null) {
			return null;
		}
		return value;
	}

	private String getValue(String name) {
		if (rootElement == null) {
			return null;
		}
		int position = 0;
		ArrayList<Integer> dots = new ArrayList<Integer>();
		while (true) {
			int index = name.indexOf(".", position);
			if (index == -1) {
				break;
			}
			dots.add(index);
			position = index + 1;
		}
		dots.add(name.length());

		Element element = rootElement;
		int dotIndex;

		if (matchRoot) {
			if (!rootElement.getNodeName().equals(name.substring(0, dots.get(0)))) {
				return null;
			}

			position = dots.get(0) + 1;
			dotIndex = 0;
		} else {
			position = 0;
			dotIndex = -1;
		}

		outer: while (position < name.length()) {
			String key = name.substring(position);
			if (element.hasAttribute(key)) {
				return element.getAttribute(key);
			}
			for (int i = dots.size() - 1; i > dotIndex; i--) {
				key = name.substring(position, dots.get(i));
				NodeList list = element.getElementsByTagName(key);
				if (list.getLength() == 1) {
					element = (Element) list.item(0);
					position = dots.get(i) + 1;
					dotIndex = i;
					continue outer;
				}
			}
			return null;
		}
		// getTextContext() returns text content of all the children
		// should we return only direct Text nodes elements content?
		return element.getTextContent();
	}

}
