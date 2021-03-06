//{{if test}}....{{/endif}}
var $if = {
	parse : function(text,start,context){
		var end = text.indexOf("}}",start);
		var el = text.substring(start+5,end);
		context.appendIf(el);
		return end+2;
	},
	findStart : function(text,start,otherStart){
		return text.indexOf("{{if",start);
	}
};
//{{for item:items}}....{{/endfor}}
var $for = {
	parse : function(text,start,context){
		var end = text.indexOf("}}",start);
		var el = text.substring(start+6,end).replace(/^\s+/,'');
		var split = el.indexOf(':')
		var varName = el.substring(0,split).replace(/\s+$/,'');
		var items = el.substring(split+1);
		context.appendFor(varName,items,null);
		return end+2;
	},
	findStart : function(text,start,otherStart){
		return text.indexOf("{{for",start);
	}
};
//{{if test}}....{{else test2}}{{/endelse}}
var $else = {
	parse : function(text,start,context){
		var end = text.indexOf("}}",start);
		var el = text.substring(start+6,end);
		if(/^\s*$/.test(el)){
			context.appendElse(null);
		}else{
			context.appendElse(el);
		}
		return end+2;
	},
	findStart : function(text,start,otherStart){
		return text.indexOf("{{else",start);
	}
};
//{{var name= value}}
//{{var name}}...{{/var}}
var $var = {
	parse : function(text,start,context){
		var end = text.indexOf("}}",start);
		var el = text.substring(start+5,end);
		el = el.replace(/^\s+|\s+$/,'');
		var split = el.indexOf('=')
		if(split>0){
			var value = el.substring(split+1);
			var varName = el.substring(0,split).replace(/\s+$/,'');
			context.appendVar(varName,value);
		}else{
			var varName = el;
			context.appendCaptrue(varName);
		}
		return end+2;
	},
	findStart : function(text,start,otherStart){
		return text.indexOf("{{var",start);
	}
};
var $end = {
	parse : function(text,start,context){
		context.appendEnd();
		return text.indexOf("}}",start)+2;
	},
	findStart : function(text,start,otherStart){
		var begin = text.indexOf("{{/",start);
		if(begin>0){
		    var end = text.indexOf("}}",begin);
		    switch(text.substring(begin+3,end)){
		    	case 'endif':
		    	case 'endfor':
		    	case 'endelse':
		    	case 'endvar':
		    	case 'end':
		    	return begin;
		    }
		}
		return -1;
	}
}
//{{client fn}}
//{{/client}}
$client = {
}
var $el = {
	parse : function(text,start,context){
		var end = text.indexOf("}}",start);
		var el = text.substring(start+2,end);
		context.appendEL(el);
		return end+2;
	},
	findStart : function(text,start,otherStart){
		return text.indexOf("{{",start);
	}
}
context.addTextParser($if);
context.addTextParser($for);
context.addTextParser($else);
context.addTextParser($var);
context.addTextParser($end);
//示例嘛，还是有待完善的，这个必须放在后面：（
context.addTextParser($el);