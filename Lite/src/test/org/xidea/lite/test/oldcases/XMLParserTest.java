package org.xidea.lite.test.oldcases;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xidea.el.json.JSONEncoder;
import org.xidea.lite.impl.ParseUtil;
import org.xidea.lite.impl.dtd.DefaultEntityResolver;


public class XMLParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testSelect() throws Exception {
		URI url = this.getClass().getResource("include-test.xml").toURI();
		org.w3c.dom.Document doc = ParseUtil.loadXMLBySource(
				ParseUtil.loadTextAndClose(ParseUtil.openStream(url), null)
				,url.toASCIIString());
		
		
		NodeList node = ParseUtil.selectByXPath(doc,"//xhtml:body");
		Assert.assertTrue(node.getLength()==1);
	}
	

	@Test
	public void testText() throws Exception {

		Pattern trim = Pattern
				.compile("^\\s*([\\r\\n])\\s*|\\s*([\\r\\n])\\s*$|^(\\s)+|(\\s)+$");

		System.out.println(JSONEncoder.encode("<"
				+ (trim.matcher("\r\n\r\n\r\n sdsdsd\r\n")
						.replaceAll("$1$2$3$4")) + ">"));
		Object s = LiteTestUtil.parse(
				"<xml>\r\n\r\n\r\n\t\t\t\t\r\n\r\n</xml>").get(0);
		Assert.assertEquals(JSONEncoder.encode("<xml>\n</xml>"), JSONEncoder.encode(s));
		System.out.println(JSONEncoder.encode("<"
				+ (trim.matcher("\r\n\r\n\r\n \t\t \t  \r\n")
						.replaceAll("$1$2$3$4")) + ">"));
	}

	@Test
	public void testXMLParser() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		// factory.setExpandEntityReferences(false);
		factory.setCoalescing(false);
		// factory.setFeature("http://apache.org/xml/features/validation/default-attribute-values",
		// false);
		// System.out.println(factory.getFeature(XMLConstants.XML_DTD_NS_URI));
		// factory.setXIncludeAware(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		documentBuilder.setEntityResolver(new DefaultEntityResolver());
		Document doc = documentBuilder
				.parse(new ByteArrayInputStream(
						"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><pre>---&nbsp;---</pre>"
								.getBytes()));
		CDATASection cdata = doc.createCDATASection("<]]->");
		System.out.println(cdata);
		System.out.println(cdata.getData());
		System.out.println(cdata);
		doc.getDocumentElement().appendChild(cdata);
		javax.xml.transform.TransformerFactory.newInstance().newTransformer()
				.transform(new DOMSource(doc), new StreamResult(System.out));

		System.out
				.println(doc.getDocumentElement().getChildNodes().getLength());
		System.out
				.println(doc.createElement("pre").getAttributes().getLength());
		System.out
				.println(doc.getDocumentElement().getAttributes().getLength());
		System.out.println(doc.getDocumentElement().getFirstChild()
				.getNodeValue());
	}
}
