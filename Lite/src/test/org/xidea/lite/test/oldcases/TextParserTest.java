package org.xidea.lite.test.oldcases;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.lite.LiteTemplate;
import org.xidea.lite.Template;
import org.xidea.lite.impl.TextNodeParser;
import org.xidea.lite.parse.ParseContext;

public class TextParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws Exception {
		
		test("${1+1}", "2");
		
		HashMap<String, String> testCase = new LinkedHashMap<String, String>();
		
		
		testCase.put("${1}:${1}", "1:1");
		testCase.put("${1+1}", "2");
		testCase.put("${}", "${}");
		testCase.put("${''}", "");
		

		for (String key : testCase.keySet()) {
			test(key, testCase.get(key));
			test("${" + key, "${" + testCase.get(key));
			test(key + "${", testCase.get(key) + "${");
			test("${" + key + "${", "${" + testCase.get(key) + "${");
		}
	}

	public void test(String text, String result) throws Exception {
		TextNodeParser p = new TextNodeParser();
		ParseContext context = LiteTestUtil.buildParseContext(new URI("http://localhost:8080/"));
		p.parse(text, context, null);
		List<Object> insts = context.toList();
		Template t = new LiteTemplate(insts,context.getFeatureMap());
		Writer out = new StringWriter();
		t.render(new HashMap<Object, Object>(), out);
		Assert.assertEquals(result, out.toString());
	}

}
