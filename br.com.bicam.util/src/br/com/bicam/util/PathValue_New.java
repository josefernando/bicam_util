package br.com.bicam.util;

import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.v4.runtime.ParserRuleContext;

public class PathValue_New {
	ParserRuleContext id;
	Deque<String> partValues;
	String valueType; 
	
	PropertyList properties;
	public PathValue_New(ParserRuleContext _ctx){
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
		setText(getText());
	}
	
	public ParserRuleContext getId(){
		return id;
	}
	
	public void setValue(String _value){
		partValues.add(_value);
	}
	
	public String getValue(){
		StringBuffer sb = new StringBuffer();
		for(String part : partValues) {
			sb.append(part);
		}
		return sb.toString();
	}
	
	public void setValueType(String _type){
		valueType = _type;
	}
	
	public String getValueType(){
		return valueType;
	}	
	
	public void addPartValues(String _part){
		if(_part == null) {
			try {
				throw new Exception();
			}catch (Exception e) {
				e.printStackTrace();
				System.err.println("*** ERROR: Null value is not allowed.");
			}
		}
		partValues.add(_part);
	}
	
	public void addProperty(String _key, Object _value){
		getProperties().addProperty(_key, _value);
	}
	
	public void setText(String _text){
		getProperties().addProperty("TEXT", _text);
	}
	
	public String getText(){
		return (String)getProperties().getProperty("TEXT");
	}
	
	public Object getProperty(String _key){
		return properties.getProperty(_key);
	}
	
	public PropertyList getProperties(){
		return properties;
	}
	
	public void getTogetherValueParts(){
		StringBuffer sb = new StringBuffer();
		for(String s : partValues){
			sb.append(s);
		}
		setValue(sb.toString());
	}
}