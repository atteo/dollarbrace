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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLIdentical;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlFilteringTest {
	@Test
	public void noFiltering() throws IOException, ParserConfigurationException,
			PropertyNotFoundException, SAXException, TransformerException{
		String xml = ""
				+ "<outer>"
				+ "  <inner attribute='value1'>"
				+ "    value2"
				+ "  </inner>"
				+ "</outer>";
		Properties properties = new Properties();

		assertXMLIdentical(new Diff(xml, filter(properties, xml)), true);
	}


	@Test
	public void simple() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException, PropertyNotFoundException {
		String xml = ""
				+ "<outer>"
				+ "  <inner attribute='${value1}'>"
				+ "    ${value2}"
				+ "  </inner>"
				+ "</outer>";
		Properties properties = new Properties();
		properties.setProperty("value1", "result1");
		properties.setProperty("value2", "result2");
		String result = ""
				+ "<outer>"
				+ "  <inner attribute='result1'>"
				+ "    result2"
				+ "  </inner>"
				+ "</outer>";

		assertXMLIdentical(new Diff(result, filter(properties, xml)), true);
	}

	private static String filter(Properties properties, String xml) throws IOException,
			ParserConfigurationException, SAXException, TransformerConfigurationException,
			TransformerException, PropertyNotFoundException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

		Filtering.filter(document.getDocumentElement(), properties);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(document), new StreamResult(writer));
		return writer.toString();
	}
}
