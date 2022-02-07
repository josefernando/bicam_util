package br.com.bicam.util;

import java.util.HashMap;

public class Proprietary {
	private HashMap<String,Object> properties;

	public Proprietary(){
		properties = new HashMap<String,Object>();
	}
	
	public void addProperty(String name, Object value) {
		getProperties().put(name, value);
	}
	
	public Object getProperty(String name) {
		return getProperties().get(name);
	}
	
	public void removeProperty(String name) {
		getProperties().remove(name);		
	}

	
	public boolean hasProperty(String name, String value) {
		return getProperties().get(name) == null ? false : true;
	}
	
	public void addNestedProperty(String _nestedName, String _name, Object _value) {
		Object  prop = getProperties().get(_nestedName);
		Proprietary pp;
		if(prop == null){
			pp = new Proprietary();
			addProperty(_nestedName, pp);
		}
		else {
			pp = (Proprietary)prop;
		}
/*		prop.ifPresent(t -> {
				Proprietary pp = (Proprietary)t;
		});*/
		
//		Proprietary pp = (Proprietary)prop.orElse(new Proprietary());
		
		pp.addProperty(_name, _value);
	}

/*	
	public Object getNestedProperty(String propertaryName, String name) {
		return ((Propertary_OLD)getProperty(propertaryName)).getProperty(name);
	}*/
	
	public HashMap<String, Object> getProperties() {
		return properties;
	}
}
