package br.com.bicam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.model.util.PropertyListSerializable;
import br.com.bicam.util.symboltable.IScope;
import br.com.bicam.util.symboltable.SymbolTable_b;

public class PropertyList_OLD {
	private HashMap<String,Object> properties;
	
	public PropertyList_OLD(){
		properties = new HashMap<String,Object>();
	}
	
	public void setContext(ParserRuleContext prc){
		addProperty("CONTEXT", prc);
	}
	
	public void addProperty(String propertyDescriptionP, Object valueP) {
		properties.put(propertyDescriptionP, valueP);
		if(valueP instanceof ParserRuleContext){
			PropertyList prop = new PropertyList();
			ParserRuleContext prc = (ParserRuleContext)valueP;
			prop.addProperty("LINE", Integer.toString(prc.start.getLine()));
			prop.addProperty("START_INDEX", Integer.toString(prc.start.getStartIndex()));
			properties.put("CONTEXT_TOSTRING", prop);
		}
	}
	
	public void removeProperty(String propertyDescriptionP) {
		properties.remove(propertyDescriptionP);
	}	
	
	public Object getProperty(String propertyDescriptionP) {
		return properties.get(propertyDescriptionP);
	}
	
	public boolean hasProperty(String _keyProp, String _valProp, boolean ..._ignoreCase ) {
		if(getProperty(_keyProp) != null && getProperty(_keyProp).getClass().getSimpleName().equalsIgnoreCase("String")){
			if(((String)getProperty(_keyProp)).equals(_valProp))
				return true;
			if(_ignoreCase.length > 0){
				if(_ignoreCase[0]  == true){
					if(((String)getProperty(_keyProp)).equalsIgnoreCase(_valProp))
						return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasProperty(String _keyProp) {
		for(String key : properties.keySet()){
			if(_keyProp.equals(key)) return true;
		}
		return false;
	}	
	
	public Object getProperty(String _keyProp, String _valProp ) {
		if (!hasProperty(_keyProp,_valProp)) return null;
		return getProperty(_keyProp);
	}	

	public HashMap<String, Object> getProperties() {
		return properties;
	}
	
	public PropertyList addNestedProperty(String _keyProp){
		PropertyList nestedProperties = (getProperty(_keyProp) instanceof PropertyList 
						? (PropertyList)getProperty(_keyProp) 
						: null);
		if(nestedProperties == null){
			nestedProperties = new PropertyList();
			addProperty(_keyProp, nestedProperties);
		}
		else {
			throw new RuntimeException("Error : Nested Property already definned: " 
					+ _keyProp);
		}
		return nestedProperties;
	}
	
	/*
	 * 1  = property is nestedProperty
	 * 0  = property is not nestedProperty
	 * -1 = property not exist
	 */
	public int hasNestedProperty(String _keyProp){
		if(getProperty(_keyProp) == null){
			return  -1;
		}
		else if(getProperty(_keyProp) instanceof PropertyList){
			return 1;
		}
		else return 0;
	}
	
	public PropertyListSerializable getPropertyListSerializable() {
		PropertyListSerializable propListSer = new PropertyListSerializable();
		    for (Map.Entry<String, Object> entry : this.getProperties().entrySet()) {
		    	if(entry.getValue() instanceof String) {
		    		propListSer.getProperties().put(entry.getKey(), (String)entry.getValue());
		    	}
		    }
			return propListSer;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for(String key : properties.keySet()){
			if(!sb.toString().equals("{")){
				sb.append(", ");
			}
			if(properties.get(key) instanceof String){
				sb.append(key + "=" + (String)properties.get(key));
			}
			else if (properties.get(key) instanceof ArrayList){
				sb.append( key + "=" + ((ArrayList)properties.get(key)));
			}
			else if (properties.get(key) instanceof PropertyList){
				sb.append(key + "=" + ((PropertyList)properties.get(key)).getProperties());
			}
			else {
				if(properties.get(key) != null)
					sb.append("=" + properties.get(key).getClass() != null ?
						key + "=" + properties.get(key).getClass().getSimpleName() : "NULL");
						else{
							sb.append(key + "=" + "NULL");
						}
			}
		}
		return sb.append("}").toString();
	}
	
	public  PropertyList getCopy(){
		PropertyList prop = new PropertyList();
		for(String key : this.getProperties().keySet()){
			prop.addProperty(key, this.getProperty(key));
		}
		return prop;
	}
	
// Definido para substituir o método getPropertyListDefault
	public static PropertyList getDefault(SymbolTable_b _st, ParserRuleContext _ctx, IScope _scope){
		PropertyList prop = new PropertyList();
		prop.addProperty("CONTEXT", _ctx);
		prop.addProperty("SYMBOLTABLE", _st);
		prop.addProperty("VISITED", new ArrayList<ParserRuleContext>());
		return prop;
	}
	
	public static PropertyList getDefault_b(SymbolTable_b _st, ParserRuleContext _ctx, IScope _scope){
		PropertyList prop = new PropertyList();
		prop.addProperty("CONTEXT", _ctx);
		prop.addProperty("SYMBOLTABLE", _st);
		prop.addProperty("VISITED", new ArrayList<ParserRuleContext>());
		return prop;
	}	
}