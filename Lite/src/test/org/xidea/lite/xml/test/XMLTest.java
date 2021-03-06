package org.xidea.lite.xml.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.v3.RuntimeSupport;
import org.xidea.lite.impl.ParseUtil;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;

public class XMLTest {
	JSIRuntime jsr = RuntimeSupport.create();

	public static void main(String[] args) throws IOException, Exception {
		new XMLTest().testDomHandler();
	}


	@Test
	public void testDomHandler() throws Exception, IOException {
		File file = new File("D:\\workspace\\FireSite\\web\\index.xhtml");
		String source = ParseUtil.loadTextAndClose(new FileInputStream(file),
				"utf-8");
		source = ParseUtil.normalize(source, "");
		final Document d1 = ParseUtil.loadXMLBySource(source,"");//"<xml a='1' xmlns:c='2' c:__='3'/>");
		d1.normalize();
		
		jsr.eval("$import('org.xidea.lite.util.xml:*')");
		Object domhandler = jsr.eval("new DOMHandler()");
//		source = "<a href='123' xmlns:c='1' c:x='2'/>";

		if (true) {
			Object fn = jsr.eval("(function testSax(source,handler){var sax = new XMLReader();" +
					"sax.contentHandler = handler;" +
					"sax.lexicalHandler = handler;sax.parse(source,handler)})");
			jsr.invoke(fn, fn, source,domhandler);

		} else {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputSource is = new InputSource(file.toURI().toASCIIString());
			is.setCharacterStream(new StringReader(source));
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(jsr.wrapToJava(domhandler,
					ContentHandler.class));
			reader.setEntityResolver(jsr.wrapToJava(domhandler,
					EntityResolver2.class));
			reader.setErrorHandler(jsr.wrapToJava(domhandler,
					ErrorHandler.class));
			reader.setDTDHandler(jsr.wrapToJava(domhandler, DTDHandler.class));
			reader.setProperty("http://xml.org/sax/properties/lexical-handler",
					jsr.wrapToJava(domhandler, LexicalHandler.class));
			reader.parse(is);
		}
		Object d2 = jsr.invoke(null,jsr.eval("(function(h){return h.document})"),domhandler);

		jsr.eval("$import('org.xidea.lite.xml.test:*')");
		Object r1 = jsr.invoke(null,jsr.eval("testWalk"), d1);
		Object r2 = jsr.invoke(null,jsr.eval("testWalk"), d2);
//		System.out.println(r1);
//		System.out.println(r2);
//
//		r1 = jsr.invoke(null,jsr.eval("testWalk"),d1);
//		r2 = jsr.invoke(null,jsr.eval("testWalk"),d2);
//
//		System.out.println(r1);
//		System.out.println(r2);
//
//		long n1 = System.nanoTime();
//		r1 = jsr.invoke(null,jsr.eval("testWalk"), d1);
//		long n2 = System.nanoTime();
//		r2 = jsr.invoke(null,jsr.eval("testWalk"),d2);
//		long n3 = System.nanoTime();
//		System.out.println((n2 - n1) / 100 + "/" + (n3 - n2) / 100);
//
//		System.out.println(r1);
//		System.out.println(r2);
		Object s = jsr.eval("new XMLSerializer()");
		String s1 =(String) jsr.invoke(s, "serializeToString", d1);
		String s2 =(String) jsr.invoke(s, "serializeToString", d2);

		//System.out.println(source);
//		System.out.println(s2);
		r2 = jsr.invoke(null,jsr.eval("compare"),  d1,d2);

		System.out.println();
		System.out.println();
		

//		System.out.println(s1);
//		System.out.println(s2);
	}

}
