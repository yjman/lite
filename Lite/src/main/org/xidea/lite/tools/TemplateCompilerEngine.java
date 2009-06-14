package org.xidea.lite.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xidea.el.json.JSONEncoder;
import org.xidea.lite.Template;
import org.xidea.lite.TemplateEngine;
import org.xidea.lite.parser.NodeParser;
import org.xidea.lite.parser.ParseContext;
import org.xidea.lite.parser.impl.ParseContextImpl;

public class TemplateCompilerEngine extends TemplateEngine {

	private Map<String, List<Object>> itemsMap = new HashMap<String, List<Object>>();
	private Map<String, File[]> filesMap = new HashMap<String, File[]>();
	private NodeParser[] parsers;

	@SuppressWarnings("unchecked")
	public TemplateCompilerEngine(File root, String[] parserClasses) {
		super(root);
		if (parserClasses != null) {
			try {
				NodeParser[] parsers = new NodeParser[parserClasses.length];
				for (int i = 0; i < parsers.length; i++) {
					parsers[i] = (NodeParser) Class.forName(parserClasses[i])
							.newInstance();
				}
				this.parsers = parsers;
			} catch (Exception e) {
			}
		}

	}

	@Override
	protected ParseContext createParseContext() {
		ParseContext context = super.createParseContext();
		if(this.parsers != null){
			for (NodeParser parser : this.parsers) {
				context.addNodeParser(parser);
			}
		}
		return context;
	}

	public String getCacheCode(String path) {
		String root = super.webRoot.getAbsolutePath();
		if (root.endsWith("/") || root.endsWith("\\")) {
			root = root.substring(0, root.length() - 1);
		}
		ArrayList<String> filesList = new ArrayList<String>();
		for (File file : filesMap.get(path)) {
			String item = file.getAbsolutePath();
			if (item.startsWith(root)) {
				filesList.add(item);
			}
		}
		return JSONEncoder
				.encode(new Object[] { filesList, itemsMap.get(path) });
	}

	protected Template createTemplate(String path, ParseContext context)
			throws IOException {
		Template template = super.createTemplate(path, context);
		this.itemsMap.put(path, context.toList());
		this.filesMap.put(path, this.getAssociatedFiles(context));
		return template;
	}

}
