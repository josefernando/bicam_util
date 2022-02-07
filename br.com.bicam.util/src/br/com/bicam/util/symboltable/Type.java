package br.com.bicam.util.symboltable;

import br.com.bicam.util.PropertyList;

public interface Type {
	public String getName();
	public void addProperty(String propertyDescriptionP, Object valueP);
	
	public void removeProperty(String propertyDescriptionP);
	
	public Object getProperty(String propertyDescriptionP) ;
	
	public boolean hasProperty(String _key, String _val);

	public PropertyList getProperties();
}
