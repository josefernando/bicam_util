package br.com.bicam.util;

import java.util.HashMap;

public interface PropertaryOLD {
	public void addProperty(String name, Object value);
	public Object getProperty(String name);
	public void removeProperty(String name);
	public boolean hasProperty(String name, String value);
	public HashMap<String,Object> getProperties();
	public void addNestedProperty(String nestedName, String name, Object value);
	public Object getNestedProperty(String propertaryName, String name);
}
