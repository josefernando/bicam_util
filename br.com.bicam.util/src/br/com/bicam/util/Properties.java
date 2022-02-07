package br.com.bicam.util;

import java.util.HashMap;

public class Properties {
	private HashMap<String,Object> properties;
		
	public Properties(){
		properties = new HashMap<String,Object>();
	}
	
	public void addProperty(String propertyDescriptionP, Object valueP) {
		properties.put(propertyDescriptionP, valueP);
	}	
	
	public Object getProperty(String propertyDescriptionP) {
		return properties.get(propertyDescriptionP);
	}

	public HashMap<String, Object> getProperties() {
		return properties;
	}
	
	public void addNestedProperty(String _keyProp, String _keyNested, Object _NestedProperty){
		Properties nestedProperties = (getProperty(_keyProp) instanceof Properties 
						? (Properties)getProperty(_keyProp) 
						: null);
		if(nestedProperties == null){
			nestedProperties = new Properties();
			addProperty(_keyProp, nestedProperties);
		}
		nestedProperties.addProperty(_keyNested, _NestedProperty);
	}
	
	public boolean isNestedProperty(String _key){
		return (getProperty(_key) instanceof Properties 
				? true: null);		
	}
	
	public String toString(){
		String s = "{";
		for(String key : properties.keySet()){
			if(!s.equals("{")){
				s += ", ";
			}
			s += key + "=" + (String)properties.get(key);
		}
		return s += "}";
	}
}
