package br.com.bicam.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.bicam.parser.constant.ParameterList;

public class ParameterNatural implements ParameterList {
	
	protected static String description;
	protected static String name;
	
	static {}

	public static boolean isParameter(String _param){
		return parameterDescription.containsKey(_param);
	}
	
	public static String getDescription(String _param){
		return parameterDescription.get(_param);
	}
	
//    public static void main(String[] args){
//    	String s = "(EM=ZZZZ AD=1 BP=AAA)";
//    	System.err.println("Parameter Description: " + getDescription(s) + " " + isParameter(s));
//    	System.err.println("Source Parameter: " + stripParen(s));
//    	System.err.println("key/Value: " + splitParam(stripParen(s)));
 //   	System.err.println("HashMap: " + getParameters(s));    	
 //   }
    
    
    public static HashMap getParameters(String _paren){
    	List ar = splitParam(stripParen(_paren));
    	LinkedHashMap<String,String> parms = new LinkedHashMap();
    	for(int i=0; i< ar.size(); ++i){
    		parms.put((String)ar.get(i), (String)ar.get(++i));
    	}
    	return parms;
    }
    
    public static List splitParam(String _params){
    	String regex = "[= ]";
    	String[] params = _params.split(regex);
    	return  Arrays.asList(params);
    }
    
    public static String stripParen(String _paren) {
    	// retira parenteses "(" e ")" de _paren
    	
    	Pattern p = Pattern.compile("(.[^)(])+",Pattern.CASE_INSENSITIVE);
    	Matcher m;
      	  m = p.matcher(_paren.substring(1));
    	m.find();
    	return m.group();
    }
}
