package br.com.bicam.util;

import java.util.HashMap;

import br.com.bicam.parser.constant.DescriptionProperty;
//import br.com.bicam.parser.constant.ProgrammingLanguage;
import br.com.bicam.parser.constant.ProgrammingLanguage;

public abstract class Capacity {
	public String format;
	public ProgrammingLanguage srcLanguage;
	public Integer definedLen;
	public Integer storageLen;
	public Integer intPartLen;      // quantidade de dígitos da parte interira de um numero
	public Integer decimalParLent;  // quantiade de dígitos da parte decimal de um número  
	public Integer minValue;
	public Integer maxValue;
	
	protected HashMap<String, Object> properties;
	
	public Capacity(String _srcLanguage, String _format){
		this.format = _format;
		properties = new HashMap<String, Object>();
	}
	
	
	public Capacity(ProgrammingLanguage _language, String _format){
		this.format = _format;
		properties = new HashMap<String, Object>();
	}
	
	public HashMap getProperties(){
		return properties;
	}
	
	public Object getProperty(String _propertyDsc){
		return properties.get(_propertyDsc);
	}
	
	protected void setProperty(String _propertyDsc, Object property){
		properties.put(_propertyDsc, property );
	}
	
	protected Integer getDefinedLength() {
		return (Integer) getProperty(DescriptionProperty.LENGTH_DEFINED);
	}	
    
    protected  abstract String stripParen();
}
