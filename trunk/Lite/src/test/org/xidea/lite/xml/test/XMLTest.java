package org.xidea.lite.xml.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.JSIText;
import org.xidea.jsi.impl.RuntimeSupport;
import org.xidea.lite.impl.ParseUtil;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;

public class XMLTest {
	JSIRuntime jsr = RuntimeSupport.create();
	@Test
	public void test() throws Exception, IOException{
		String source = ParseUtil.loadTextAndClose(new FileInputStream("D:\\workspace\\FireSite\\web\\index.xhtml"), "utf-8");
		Document d1 = ParseUtil.loadXMLBySource(source, "");
		source = ParseUtil.normalize(source, "");
		jsr.eval("$import('org.xidea.lite.nodejs:DOMParser')");
		Object d2 = jsr.invoke(jsr.eval("new DOMParser()"), "parseFromString", source);

		Object r1 = jsr.invoke(jsr.eval("new DOMParser()"), "test", d1);
		Object r2 = jsr.invoke(jsr.eval("new DOMParser()"), "test", d2);
		System.out.println(r1);
		System.out.println(r2);
		
		r1 = jsr.invoke(jsr.eval("new DOMParser()"), "test", d1);
		r2 = jsr.invoke(jsr.eval("new DOMParser()"), "test", d2);

		System.out.println(r1);
		System.out.println(r2);
	}
	@Test
	public void testDomHandler() throws Exception, IOException{
		File file = new File("D:\\workspace\\FireSite\\web\\index.xhtml");
		String source = ParseUtil.loadTextAndClose(new FileInputStream(file), "utf-8");
		Document d1 = ParseUtil.loadXMLBySource(source, "");
		source = ParseUtil.normalize(source, "");
		jsr.eval("$import('org.xidea.lite.util.xml:DOMHandler')");
		Object domhandler = jsr.eval("new DOMHandler()");
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		InputSource is = new InputSource(file.toURI().toASCIIString());   
		is.setCharacterStream(new StringReader(source));
		XMLReader reader = parser.getXMLReader();
        reader.setContentHandler(jsr.wrapToJava(domhandler,ContentHandler.class));
        reader.setEntityResolver(jsr.wrapToJava(domhandler,EntityResolver2.class));
        reader.setErrorHandler(jsr.wrapToJava(domhandler,ErrorHandler.class));
        reader.setDTDHandler(jsr.wrapToJava(domhandler,DTDHandler.class));
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", jsr.wrapToJava(domhandler,LexicalHandler.class));
        reader.parse(is);
        
        jsr.eval("$import('org.xidea.lite.nodejs:DOMParser')");
        Object r1 = jsr.invoke(jsr.eval("new DOMParser()"), "test", d1);
		Object r2 = jsr.invoke(jsr.eval("new DOMParser()"), "test", domhandler);
		System.out.println(r1);
		System.out.println(r2);

		r1 = jsr.invoke(jsr.eval("new DOMParser()"), "test", d1);
		r2 = jsr.invoke(jsr.eval("new DOMParser()"), "test", domhandler);

		System.out.println(r1);
		System.out.println(r2);
		
		
		long n1 = System.nanoTime();
		r1 = jsr.invoke(jsr.eval("new DOMParser()"), "test", d1);
		long n2 = System.nanoTime();
		r2 = jsr.invoke(jsr.eval("new DOMParser()"), "test", domhandler);
		long n3 = System.nanoTime();
		System.out.println((n2-n1)/100 +"/"+(n3-n2)/100);

		System.out.println(r1);
		System.out.println(r2);
        
	}

}