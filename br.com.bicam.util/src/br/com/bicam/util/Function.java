package br.com.bicam.util;

import java.util.HashMap;

import org.antlr.v4.runtime.ParserRuleContext;

public class Function {

	String name;
	HashMap<Integer,String> parameters;
	
	public Function(String _name, ParserRuleContext _ctx){
		this.name = _name;
		parameters = new HashMap<Integer,String>();
	}
	
	public String getName(){
		return name;
	}
	
	public void addParameter(int _seq, String _parm){
		parameters.put(_seq, _parm);
	}
	
	public String getParameterByIndex(Integer _seq){
		return parameters.get(_seq);
	}
	
	public HashMap<Integer,String> getParameters(){
		return parameters;
	}
}
