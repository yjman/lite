/**
 * @see extension.js
 */

function ExtensionParser(){
	this.packageMap = {};
	this.addExtension("http://www.xidea.org/lite/core",Core);
	
}

function formatName(el){
	var tagName = el.localName|| el.nodeName ||''
	tagName = tagName.replace(/[\-]|^\w+\:/g,"");
	return tagName.toLowerCase();
}

function copyParserMap(mapClazz,p,p2,key){
	var map = p[key];
	if(map){
		var result = mapClazz.newInstance();
		p2[key] = result;
		for(var n in map){
			result[n]= map[n];
		}
	}
}

function loadExtObject(source){
	var p = /\b(?:document|xmlns|(?:on|parse|before|seek)\w*)\b/g;
	var fn = new Function(source+"\n return function(){return eval(arguments[0])}");
	var m,o;
	var objectMap = {};
	try{
		fn = fn();
	}catch(e){
		$log.error("扩展脚本装载失败：",source);
	}
	while(m = p.exec(source)){
		try{
			o = fn(m[0]);
			if(o instanceof Function){
				objectMap[m[0]] = o;
			}
		}catch(e){
		}
	}
	return objectMap;
}

/**
 * 
	public boolean parseElement(Element el, ParseContext context,
			ParseChain chain, String name);
	public boolean parseDocument(Document node, ParseContext context,ParseChain chain);
	public boolean parseNamespace(Attr node, ParseContext context, ParseChain chain);
	public boolean parseAttribute(Attr attr, ParseContext context, ParseChain chain);
	public boolean parseBefore(Attr attr, ParseContext context,
			ParseChain previousChain, String name);
 */
ExtensionParser.prototype = {
	mapJava:function(mapClazz){
		var result = mapClazz.newInstance();
		for(var n in this.packageMap){
			var p = this.packageMap[n];
			var p2 = mapClazz.newInstance();
			result[n]=p2;
			p.documentParser && (p2.documentParser = p.documentParser);
			p.namespaceParser && (p2.namespaceParser = p.namespaceParser);
			copyParserMap(mapClazz,p,p2,"beforeMap")
			copyParserMap(mapClazz,p,p2,"onMap")
			copyParserMap(mapClazz,p,p2,"parserMap")
			copyParserMap(mapClazz,p,p2,"seekMap")
		}
		return result
	},
	parseDocument:function(node,context,chain){
		for(var p in this.packageMap){
			p = this.packageMap[p];
			if(p.documentParser){
				p.documentParser.call(chain,node);
				return true;
			}
		}
		return false;
	},
	parseNamespace:function(attr,context,chain){
		if(/^xmlns(?:\:\w+)?/.test(attr.name)){
			var v = attr.value;
			var fp = this.packageMap[v||''];
			if(fp && fp.namespaceParser){
				fp.namespaceParser.call(chain,attr);
				return true;
			}
			//$log.error(v,fp.namespaceParser);
		}
		return false;
	},
	parseElement:function(el, context,chain,ns, name){
//		context.setAttribute(CURRENT_NODE_KEY,el)
		var nns = el.namespaceURI;
		var attrs = el.attributes;
		var len = attrs.length;
		var exclusiveMap = {};
		try{
			var es = 0;
			for (var i =  len- 1; i >= 0; i--) {
				var attr = attrs.item(i);
				var ans = attr.namespaceURI;
				var ext = this.packageMap[ans || ''];
				var an = formatName(attr);
				es = 2
				if (ext && ext.beforeMap) {
					var fn = ext.beforeMap[an];
					if(fn && an in ext.beforeMap){
						es = 2.1
						try{
							el.removeAttributeNode(attr);
							fn.call(chain,attr);
							es =2.2
						}finally{
							
						}
						return;
					}else{
						an+='$';
						if(an in ext.beforeMap){
							exclusiveMap[an] = attr;
						}else{
							$log.error("未支持属性：",el.name,context.currentURI)
						}
					}
				}
			}
			es = 4;
			for(an in exclusiveMap){
				var attr = exclusiveMap[an];
				var ans = attr.namespaceURI;
				var ext = this.packageMap[ans || ''];
				try{
					el.removeAttributeNode(attr);
					ext.beforeMap[an].call(chain,attr);
				}finally{
					
				}
				
				return;
			}
		}catch(e){
			$log.error("元素扩展解析异常",es,attr.xml,ans,an,e)
			throw e;
		}finally{
			
		}
		var ext = this.packageMap[nns||''];
		var nn = formatName(el);
		if(ext && ext.parserMap){
			var fn = ext.parserMap[nn];
			if(fn && (nn in ext.parserMap)
				 || (fn = ext.parserMap[''])){
				fn.call(chain,el);
				return true;
			}else if(nns && nns != 'http://www.w3.org/1999/xhtml'){
				$log.error("未支持标签：",el.tagName,context.currentURI)
			}
		}
	},
	parse:function(node,context,chain){
		try{
			var es = 0;
			var type = node.nodeType;
			if(type == 9){
				if(this.parseDocument(node,context,chain)){
					return ;
				}
			}else if(type == 2){
				try{
					if(this.parseNamespace(node,context,chain)){
						return;
					}
					es = 3;
					var el = node.ownerElement;
					//ie bug.no ownerElement
					var ns = el && el.namespaceURI||'';
					var ext = this.packageMap[ns];
					es=4;
					if(ext && ext.onMap){
						if(fn in ext.onMap){
							var fn = ext.onMap[fn];
							fn.call(chain,node);
							return true;
						}
					}
				}catch(e){
					$log.error("属性扩展解析异常：",node.xml,el==null,es,e)
				}
			}else if(type === 1){
				if(this.parseElement(node,context,chain)){
					return ;
				}
			} 
			es += 10;
			chain.next(node)
		}catch(e){
			$log.error("扩展解析异常：",node.xml,es,e)
		}
	},
	parseText:function(text,start,context){
		var text2 = text.substring(start+1);
		var match = text2.match(/^(?:(\w*)\:)?([\w!#]*)[\$\{]/);
		try{
			var es = 0;
			if(match){
				var matchLength = match[0].length;
				var currentNode = context.getCurrentNode();//getAttribute(CURRENT_NODE_KEY)
				var prefix = match[1];
				var fn = match[2]
				if(prefix == null){
					var ns = ""
				}else{
					es = 1;
					if(currentNode && currentNode.lookupNamespaceURI){
						var ns = currentNode.lookupNamespaceURI(prefix);
						if (ns == null) {
							var doc = currentNode.getOwnerDocument();
							ns = doc && doc.documentElement.lookupNamespaceURI(prefix);
						}
					}
					es =2
				}
				
				if(!ns && (prefix == 'c' || !prefix)){
					ns = "http://www.xidea.org/lite/core"
				}
				if(ns == null){
					$log.warn("文本解析时,查找名称空间失败,请检查是否缺少XML名称空间申明：[code:$"+match[0]+",prefix:"+prefix+",document:"+context.currentURI+"]")
				}else{
					var fp = this.packageMap[ns||''];
					if(fp){
						//{开始的位置，el内容
						var text3 = text2.substring(matchLength-1);
						var p = fp.seek(text3,fn,context);
						if(p>=0){
							return start+matchLength+p+1
						}
					}else{
						$log.warn("文本解析时,名称空间未注册实现程序,请检查lite.xml是否缺少语言扩展定义：[code:$"+match[0]+",namespace:"+ns+",prefix:"+prefix+",document:"+context.currentURI+"]")
					}
				}
			}
		}catch(e){
			$log.error("文本解析异常：",es,e)
		}
		//seek
		return -1;
	},
	/**
	 * 查找EL或者模板指令的开始位置
	 * @param text
	 * @param start 开始查询的位置
	 * @param otherStart 其他的指令解析器找到的指令开始位置（以后必须出现在更前面，否则无效）
	 * @return 返回EL起始位置($位置)
	 */
	findStart:function(text,start,otherStart){
		var begin = start;
		while(true){
			begin = text.indexOf('$',begin);
			if(begin<0 || otherStart <= begin){
				return -1;
			}
			var text2 = text.substring(begin+1);
			var match = text2.match(/^(?:\w*\:)?[\w#!]*[\$\{]/);
			if(match){
				return begin;
			}
			begin++;
		}
	},
	addExtension:function(namespace,packageName){
		if(typeof packageName == 'string'){
			if(/^[\w\.]+$/.test(packageName)){
				var objectMap = {};
				var packageObject = $import(packageName+':');
				for(var n in packageObject.objectScriptMap){
					var match = n.match(/^(?:document|xmlns|on|parse|before|seek).*/);
					if(match){
						var o = $import(packageObject.name+':'+n,objectMap);
					}
				}
			}else{
				objectMap = loadExtObject(packageName)
			}
		}else{
			objectMap = packageName;
		}
		var ext = this.packageMap[namespace||''];
		if(ext == null){
			ext = this.packageMap[namespace||''] = new Extension();
		}
		ext.initialize(objectMap);
	},
	priority:2,
	getPriority:function() {
		//${ =>2
		//$!{ =>3
		//$end$ =>5
		return this.priority;
	}
}