package br.com.bicam.util;

import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.v4.runtime.ParserRuleContext;

public class TerminalValue {
	ParserRuleContext id;
	Deque<String> partValues;
	Object value;
	String valueType; // Enum: STRING, NUMERIC, BOOLEAN E REFERENCE
/*	String expression;        // Ex: split(<identifier>,0) ou split(A,0)
	String coreExpression;  // ... do exemplo expression: <identifier> ou "A"
*/	
	
	PropertyList properties;
	public TerminalValue(ParserRuleContext _ctx){
		if(_ctx == null){
			try{
				throw new IllegalArgumentException();
			} catch (Exception e){
				System.err.println("** Error - null context not Allowed");
				e.printStackTrace();
			}
		}
		partValues = new ArrayDeque<String>();
		this.id = _ctx;
		properties = new PropertyList();
	}
	
	public ParserRuleContext getId(){
		return id;
	}
	
	public void setValue(Object _value){
		value = _value;
	}
	
	public Object getValue(){
		return value;
	}
	
	public void setValueType(String _type){
		valueType = _type;
	}
	
	public String getValueType(){
		return valueType;
	}	
	
	public void addtPartValues(String _part){
		partValues.add(_part);
	}
	
	public void addProperty(String _key, Object _value){
		getProperties().addProperty(_key, _value);
	}
	
	public Object getProperty(String _key){
		return properties.getProperty(_key);
	}
	
	public PropertyList getProperties(){
		return properties;
	}
	
	public void computeValueParts(){
		StringBuffer sb = new StringBuffer();
		for(String s : partValues){
			sb.append(s);
		}
		setValue(sb.toString());
	}
}
