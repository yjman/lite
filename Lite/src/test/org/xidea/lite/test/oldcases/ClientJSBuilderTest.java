package org.xidea.lite.test.oldcases;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.v3.RuntimeSupport;
import org.xidea.lite.parse.ParseContext;
import org.xml.sax.SAXException;

public class ClientJSBuilderTest {
	@Before
	public void setUp() throws Exception {

	}
	RuntimeSupport proxy = (RuntimeSupport) RuntimeSupport.create();



	@Test
	public void testBuildJS() throws SAXException, IOException, URISyntaxException {
		URI url = this.getClass().getResource("format-test.xhtml").toURI();
		ParseContext context2 = LiteTestUtil.buildParseContext(url);
		// 前端直接压缩吧？反正保留那些空白也没有调试价值
		// context2.setCompress(context.isCompress());
		context2.parse(context2.loadXML(url));
		JSIRuntime rt = RuntimeSupport.create();
		proxy.eval("$import('org.xidea.lite.impl.js:JSTranslator')");
		Object ts = proxy.eval("new JSTranslator('t1')");
		String result = (String)proxy.invoke(ts, "translate", context2.toList());

		System.out.println("==JS Code==");
		System.out.println(result);
		boolean isError = Pattern.compile("[\r\n]alert", Pattern.MULTILINE)
				.matcher(result).find();
		Assert.assertTrue("生成失败" + result, !isError);
	}


	@Test
	public void testClient() throws SAXException, IOException, URISyntaxException {
		URI url = this.getClass().getResource("asciitable-client.xhtml").toURI();
		ParseContext context2 = LiteTestUtil.buildParseContext(url);
		// 前端直接压缩吧？反正保留那些空白也没有调试价值
		// context2.setCompress(context.isCompress());
		context2.parse(context2.loadXML(url));

		List<Object> clientLiteCode = context2.toList();
		System.out.println("==JS Code==");
		System.out.println(clientLiteCode.toString());
		String result = clientLiteCode.toString();
		boolean isError = Pattern.compile("[\r\n]alert", Pattern.MULTILINE)
		.matcher(result).find() || !Pattern.compile("\\.push\\(", Pattern.MULTILINE)
		.matcher(result).find();
		Assert.assertTrue("生成失败" + result, !isError);
	}


	public static String loadText(Reader in) {
		// Reader in = new InputStreamReader(sin, "utf-8");
		StringWriter out = new StringWriter();
		int count;
		char[] cbuf = new char[1024];
		try {
			while ((count = in.read(cbuf)) > -1) {
				out.write(cbuf, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}
}
