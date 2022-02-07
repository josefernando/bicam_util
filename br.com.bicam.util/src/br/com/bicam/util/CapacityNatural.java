package br.com.bicam.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.bicam.parser.constant.DescriptionProperty;
//import br.com.bicam.parser.util.Capacity;

public class CapacityNatural extends Capacity{

	public CapacityNatural(String _srcLanguage, String _format) {
		super(_srcLanguage, _format);
	}
	
	public void setDefinedLength(){
		String dataFormat = stripParen();
	}
	
	public void setDefinedLength(String _dataFormat){
		
		format = stripParen();
//System.err.println("format: " + format);		
		
 //System.err.println("format.substring(0, 0): " + format.substring(0, 1));
		if(format.substring(0, 1).equalsIgnoreCase("A")){
			setDefLenAlphaNumeric(format);	
		}
	}
	
	public void setDefLenAlphaNumeric(String _dataFormat){
		this.setProperty(DescriptionProperty.LENGTH_DEFINED, Integer.parseInt(_dataFormat.substring(1)));
//     System.err.println("Integer: " + Integer.parseInt(_dataFormat.substring(1)));
	}
	
	public Integer getDefinedLength() {
		return (Integer) getProperty(DescriptionProperty.LENGTH_DEFINED);
	}	
    
    protected String stripParen() {
    	Pattern p = Pattern.compile("([ABCDFILNPTU]|[0-9])+",Pattern.CASE_INSENSITIVE);    	
    	Matcher m = p.matcher(format.substring(1));
    	m.find();
    	return m.group();
    }
}
