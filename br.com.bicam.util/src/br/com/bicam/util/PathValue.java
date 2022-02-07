package br.com.bicam.util;

import org.antlr.v4.runtime.ParserRuleContext;

public class PathValue {
	ParserRuleContext id;
	Object value;
	String valueType; // Enum: STRING, NUMERIC, BOOLEAN E REFERENCE
/*	String expression;        // Ex: split(<identifier>,0) ou split(A,0)
	String coreExpression;  // ... do exemplo expression: <identifier> ou "A"
*/	
	
	PropertyList properties;
	public PathValue(ParserRuleContext _idCtx){
		this.id = _idCtx;
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
	
/*	public void addtPartValues(Object _object){
		partValues.add(_object);
	}*/
	
	public void addProperty(String _key, Object _value){
		getProperties().addProperty(_key, _value);
	}
	
	public Object getProperty(String _key){
		return properties.getProperty(_key);
	}
	
	public PropertyList getProperties(){
		return properties;
	}
	
	
}
