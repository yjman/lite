package org.xidea.lite.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xidea.el.Expression;
import org.xidea.el.ExpressionFactory;
import org.xidea.el.json.JSONEncoder;
import org.xidea.lite.Template;

public class CoreXMLNodeParser implements NodeParser {
	private static Log log = LogFactory.getLog(CoreXMLNodeParser.class);
	private static final Pattern TEMPLATE_NAMESPACE_CORE = Pattern
			.compile("^http:\\/\\/www.xidea.org\\/ns\\/(?:template|lite)(?:\\/core)?\\/?$");
	private ClientJSBuilder jsbuilder = new ClientJSBuilder();
	private ExpressionFactory clientExpressionFactory = new ExpressionFactory() {
		public Expression create(Object el) {
			throw new UnsupportedOperationException();
		}

		public Object parse(String expression) {
			return expression;
		}

	};
	private XMLParser parser;

	public static boolean isCoreNS(String prefix, String url) {
		return ("c".equals(prefix) && ("#".equals(url) || "#core".equals(url)))
				|| TEMPLATE_NAMESPACE_CORE.matcher(url).find();
	}

	public CoreXMLNodeParser(XMLParser parser) {
		this.parser = parser;
	}

	public Node parseNode(final Node node, ParseContext context) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			final Element el = (Element) node;
			String prefix = el.getPrefix();
			String namespaceURI = el.getNamespaceURI();
			if (namespaceURI != null && isCoreNS(prefix, namespaceURI)) {
				String name = el.getLocalName();
				if ("include".equals(name)) {
					return parseIncludeTag(el, context);
				} else if ("client".equals(name)) {
					return parseClientTag(el, context);
				} else if ("group".equals(name) || "context".equals(name)) {
					return parseContextTag(el, context);
				} else if ("json".equals(name)) {
					return parseJSONTag(el, context);
				} else if ("choose".equals(name)) {
					return parseChooseTag(el, context);
				} else if ("elseif".equals(name) || "else-if".equals(name)
						|| "elif".equals(name)) {
					return parseElseIfTag(el, context, true);
				} else if ("else".equals(name)) {
					return parseElseIfTag(el, context, false);
				} else if ("if".equals(name)) {
					return parseIfTag(el, context);
				} else if ("out".equals(name)) {
					return parseOutTag(el, context);
				} else if ("for".equals(name) || "forEach".equals(name)
						|| "for-each".equals(name)) {
					return parseForTag(el, context);
				} else if ("var".equals(name)) {
					return parseVarTag(el, context);
				}
			}
		}
		return node;
	}

	public Node parseIncludeTag(final Element el, ParseContext context) {
		String var = getAttribute(el, "var");
		String path = getAttribute(el, "path");
		String xpath = getAttribute(el, "xpath");
		String xslt = getAttribute(el, "xslt");
		String name = getAttribute(el, "name");
		Node doc = el.getOwnerDocument();
		URL parentURL = context.getCurrentURL();
		try {
			if (name != null) {
				DocumentFragment cachedNode = parser.toDocumentFragment(el, el
						.getChildNodes());
				context.setAttribute("#" + name, cachedNode);
			}
			if (var != null) {
				Node next = el.getFirstChild();
				context.appendCaptrue(var);
				if (next != null) {
					do {
						this.parser.parseNode(next, context);
					} while ((next = next.getNextSibling()) != null);
				}
				context.appendEnd();
			}
			if (path != null) {
				if (path.startsWith("#")) {
					doc = (Node) context.getAttribute(path);
					String uri;
					if (doc instanceof Document) {
						uri = ((Document) doc).getDocumentURI();
					} else {
						uri = doc.getOwnerDocument().getDocumentURI();
					}
					if (uri != null) {
						try {
							context.setCurrentURL(context.createURL(null, uri));
						} catch (Exception e) {
						}
					}
				} else {
					doc = this.parser.loadXML(context
							.createURL(parentURL, path), context);
				}
			}

			if (xpath != null) {
				doc = this.parser.selectNodes(xpath, doc);
			}
			if (xslt != null) {
				Source xsltSource;
				if (xslt.startsWith("#")) {
					Node node1 = ((Node) context.getAttribute(xslt));
					Transformer transformer = javax.xml.transform.TransformerFactory
							.newInstance().newTransformer();
					DOMResult result = new DOMResult();
					if (node1.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
						node1 = node1.getFirstChild();
						while (node1.getNodeType() != Node.ELEMENT_NODE) {
							node1 = node1.getNextSibling();
						}
					}
					transformer.transform(new DOMSource(node1), result);
					xsltSource = new javax.xml.transform.dom.DOMSource(result
							.getNode());
				} else {
					xsltSource = new javax.xml.transform.stream.StreamSource(
							context.createURL(parentURL, xslt).openStream());
				}

				// create an instance of TransformerFactory
				Transformer transformer = javax.xml.transform.TransformerFactory
						.newInstance().newTransformer(xsltSource);
				// javax.xml.transform.TransformerFactory
				// .newInstance().set
				// transformer.setNamespaceContext(parser.createNamespaceContext(doc.getOwnerDocument()));

				Source xmlSource;
				if (doc.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
					Element root = doc.getOwnerDocument().createElement("root");
					root.appendChild(doc);
					xmlSource = new DOMSource(root);
				} else {
					xmlSource = new DOMSource(doc);
				}
				DOMResult result = new DOMResult();

				transformer.transform(xmlSource, result);
				doc = result.getNode();
			}
			this.parser.parseNode(doc, context);
			return null;
		} catch (Exception e) {
			log.error(e);
			return null;
		} finally {
			context.setCurrentURL(parentURL);
		}
	}

	Node parseIfTag(Element el, ParseContext context) {
		Object test = getAttributeEL(context, el, "test");
		context.appendIf(test);
		parseChild(el.getFirstChild(),context);
		context.appendEnd();
		return null;
	}

	Node parseElseIfTag(Element el, ParseContext context, boolean reqiiredTest) {
		context.removeLastEnd();
		if (((Element) el).hasAttribute("test")) {
			Object test = getAttributeEL(context, el, "test");
			context.appendElse(test);
		} else if (reqiiredTest) {
			throw new IllegalArgumentException("@test is required");
		} else {
			context.appendElse(null);
		}
		parseChild(el.getFirstChild(),context);
		context.appendEnd();
		return null;
	}

	Node parseElseTag(Element el, ParseContext context) {
		context.removeLastEnd();
		context.appendElse(null);
		parseChild(el.getFirstChild(),context);
		context.appendEnd();
		return null;
	}

	Node parseChooseTag(final Element el2, ParseContext context) {
		boolean first = true;
		String whenTag = "when";
		String elseTag = "otherwise";
		Node next = el2.getFirstChild();
		while (next != null) {
			if (next instanceof Element) {
				// ignore namespace check
				if (whenTag.equals(next.getLocalName())) {
					if (first) {
						first = false;
						parseIfTag((Element) next, context);
					} else {
						parseElseIfTag((Element) next, context, true);
					}
				} else if (next.getLocalName().equals(elseTag)) {
					parseElseTag((Element) next, context);
				} else {
					throw new RuntimeException("choose 只接受when，otherwise 节点");
				}
			}
			next = next.getNextSibling();
		}
		return null;
	}

	Node parseForTag(Element el, ParseContext context) {
		Object items = getAttributeEL(context, el, "items");
		String var = getAttribute(el, "var");
		String status = getAttribute(el, "status");
		context.appendFor(var, items, status);
		parseChild(el.getFirstChild(), context);
		context.appendEnd();
		return null;
	}

	Node parseVarTag(Element el, ParseContext context) {
		String name = getAttribute(el, "name");
		String value = getAttribute(el, "value");
		if (value == null) {
			context.appendCaptrue(name);
			parseChild(el.getFirstChild(), context);
			context.appendEnd();
		} else {
			int mark = context.mark();
			this.parser.parseText(context, value, Template.EL_TYPE);
			List<Object> temp = context.reset(mark);
			if (temp.size() == 1) {
				Object[] item = (Object[]) temp.get(0);
				context.appendVar(name, item[1]);
			} else {
				context.appendCaptrue(name);
				context.appendAll(temp);
				context.appendEnd();
			}
		}
		return null;
	}

	Node parseClientTag(Element el, ParseContext context) {
		Node next = el.getFirstChild();
		if (next != null) {
			ParseContext context2 = new ParseContextImpl(context
					.getCurrentURL());
			// 前端直接压缩吧？反正保留那些空白也没有调试价值
			// context2.setCompress(context.isCompress());
			context2.setCompress(true);
			context2.setExpressionFactory(clientExpressionFactory);
			do {
				this.parser.parseNode(next, context2);
			} while ((next = next.getNextSibling()) != null);
			List<Object> result = context2.toResultTree();
			String js = jsbuilder.buildJS(el.getAttribute("id"), result);
			if (context.isCompress() && !context.isReserveSpace()) {
				js = jsbuilder.compress(js);
			}
			boolean needScript = needScript(el);
			if(needScript){
				context.append("<script>/*<![CDATA[*/" + js + "/*]]>*/</script>");
			}else{
				context.append("/*<![CDATA[*/" + js + "/*]]>*/");
			}
			
		}
		return null;
	}

	private boolean needScript(Element el) {
		return true;
	}

	Node parseContextTag(Element el, ParseContext context) {
		parseChild(el.getFirstChild(), context);
		return null;
	}

	Node parseJSONTag(final Element el, ParseContext context) {
		String var = getAttribute(el, "var");
		String file = getAttribute(el, "file");
		String encoding = getAttribute(el, "encoding", "charset");
		String content = getAttribute(el, "content");
		if (file != null) {
			try {
				URL url = context.createURL(null, file);
				InputStream in = url.openStream();
				InputStreamReader reader = new InputStreamReader(in,
						encoding == null ? "utf-8" : encoding);
				StringBuilder sbuf = new StringBuilder();
				char[] cbuf = new char[1024];
				int c;
				while ((c = reader.read(cbuf)) >= 0) {
					sbuf.append(cbuf, 0, c);
				}
				content = sbuf.toString();
			} catch (Exception e) {
				if (log.isWarnEnabled()) {
					log.warn("json文件读取失败", e);
				}
			}
		}
		if (content == null) {
			// Node next = node.getFirstChild();
			// context.append(new Object[] { VAR_TYPE, var, null });
			// if (next != null) {
			// do {
			// this.parser.parseNode(next, context);
			// } while ((next = next.getNextSibling()) != null);
			// }
			// context.appendEnd();
			content = el.getTextContent();
		}
		context.appendVar(var, context.optimizeEL(content));
		return null;
	}

	Node parseOutTag(Element el, ParseContext context) {
		String value = getAttribute(el, "value");
		this.parser.parseText(context, value, Template.EL_TYPE);
		return null;
	}

	private void parseChild(Node child, ParseContext context) {
		while (child != null) {
			this.parser.parseNode(child, context);
			child = child.getNextSibling();
		}
	}
	private String getAttribute(Element el, String... keys) {
		for (String key : keys) {
			if (el.hasAttribute(key)) {
				return el.getAttribute(key);
			}
		}
		return null;
	}

	private Object toEL(ParseContext context, String value) {
		value = value.trim();
		if (value.startsWith("${") && value.endsWith("}")) {
			value = value.substring(2, value.length() - 1);
		} else {
			log.warn("输入的不是有效el，系统将字符串转换成el");
			value = JSONEncoder.encode(value);
		}
		return context.optimizeEL(value);
	}

	private Object getAttributeEL(ParseContext context, Element el, String key) {
		String value = getAttribute(el, key);
		return toEL(context, value);

	}
}
