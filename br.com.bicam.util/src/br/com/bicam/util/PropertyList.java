package br.com.bicam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.jaxb.MapAdapter;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class PropertyList {
	private HashMap<String,Object> properties;
	
	public PropertyList(){
		properties = new HashMap<String,Object>();
	}
	
	public PropertyList(PropertyList pList){
		properties = new HashMap<String,Object>();
		for ( Entry<String,Object> e   : pList.getProperties().entrySet()) {
			properties.put(e.getKey(), e.getValue());
		}
	}
	
	@XmlTransient
	public void setContext(ParserRuleContext prc){
		addProperty("CONTEXT", prc);
	}
	
	public void addProperty(String propertykey, Object propertyValue, Boolean ...append) {
        if(append.length > 0 && append[0] == true)
        	appendProperty(propertykey,propertyValue);
        else
		properties.put(propertykey, propertyValue);
	}
	
	public void addProperty(PropertyList _properties, String propertykey, Object propertyValue) {
		if(String.class.isInstance(propertyValue)) {
			_properties.addProperty(propertykey, propertyValue);
		}
		else {
		}
	}
	
	public int appendProperty(String propertyKey, Object propertyValue) {
		if(!properties.containsKey(propertyKey)) {
			properties.put(propertyKey, propertyValue);
			if(propertyValue instanceof ParserRuleContext){
				PropertyList prop = new PropertyList();
				ParserRuleContext prc = (ParserRuleContext)propertyValue;
				prop.addProperty("LINE", Integer.toString(prc.start.getLine()));
				prop.addProperty("START_INDEX", Integer.toString(prc.start.getStartIndex()));
				properties.put("CONTEXT_TOSTRING", prop);
			}			
			return 1;
		}
		else {
			if((properties.get(propertyKey) instanceof List)) {
				((ArrayList<PropertyList>)properties.get(propertyKey)).add((PropertyList) propertyValue);
			}
			else {
				ArrayList<PropertyList> list = new ArrayList<PropertyList>();
				list.add((PropertyList) getProperty(propertyKey));
				addProperty(propertyKey, list);
			}
		}
		return ((ArrayList)properties.get(propertyKey)).size();
	}
	
	public void removeProperty(String propertyDescriptionP) {
		properties.remove(propertyDescriptionP);
	}	
	
	public Object getProperty(String propertyDescriptionP) {
		return properties.get(propertyDescriptionP);
	}
	
	public PropertyList getNestedProperty(String nestedProperty) {
		return (PropertyList) getProperty(nestedProperty);
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
	
	@XmlJavaTypeAdapter(MapAdapter.class)
	@XmlElement(name="PROPERTY_LIST")
	public void setProperties(HashMap<String, Object> _prop) {
		this.properties = _prop;
	}
	
	public void setProperties(PropertyList pList) {
		for ( Entry<String,Object> e   : pList.getProperties().entrySet()) {
			properties.put(e.getKey(), e.getValue());
		}
	}
	
	public void clear() {
		properties.clear();
	}
	
	public PropertyList addNestedProperty(String _keyProp, Boolean ..._append){
		PropertyList nestedProperties = (getProperty(_keyProp) instanceof PropertyList 
						? (PropertyList)getProperty(_keyProp) 
						: null);
		
		if(nestedProperties == null){
			nestedProperties = new PropertyList();
			if(_append.length > 0 && _append[0]==true)
				addProperty(_keyProp, nestedProperties,_append[0]);
			else
				addProperty(_keyProp, nestedProperties);
		}
		else if(_append.length > 0 && _append[0]==true) {
			//none
		}
		else {
			throw new RuntimeException("Error : Nested Property already defined: " 
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
		    	else if(entry.getValue() instanceof ArrayList) {
		    		ArrayList<PropertyListSerializable> ar = new ArrayList<PropertyListSerializable>();
		//    		propListSer.getProperties().put(entry.getKey(), ((PropertyList)entry.getValue()).getPropertyListSerializable());
		    		ArrayList<PropertyList> arProp = (ArrayList)entry.getValue();
		    		propListSer.addProperty(entry.getKey(), ar);
		    		for(PropertyList prop : arProp) {
		    			ar.add(prop.getPropertyListSerializable());
		    		}
		    	}
		    	else if(entry.getValue() instanceof PropertyList) {
		    		PropertyList propw = (PropertyList)entry.getValue();
		    		propListSer.getProperties().put(entry.getKey(), propw.getPropertyListSerializable());
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
			else if ((properties.get(key) != null) &&   properties.get(key).getClass().getSuperclass().getSimpleName().endsWith("List")){
				sb.append( key + "=" + ((ArrayList)properties.get(key)));
			}			
			else if ((properties.get(key) != null) && properties.get(key).getClass().getSuperclass().getSimpleName().endsWith("Set")){
				sb.append( key + "=" + ((Set)properties.get(key)));
			}
			else if (properties.get(key) instanceof PropertyList){
				sb.append(key + "=" + ((PropertyList)properties.get(key)).getProperties());
			}
			else {
				if(properties.get(key) != null) {
					sb.append("=" + properties.get(key).getClass() != null ?
//						key + "=" + properties.get(key).getClass().getSimpleName() : "NULL");
					key + "=" + properties.get(key).getClass().getName() : "NULL");
				}
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
	
	public static void main(String args[]) {
		// testaXML();
		testaNestedProperty();
		
	}
	
	private static void testaNestedProperty() {
		PropertyList properties = new PropertyList();
		properties.addNestedProperty("PROPERTIES");
		properties.getNestedProperty("PROPERTIES").addProperty("Prop01","VALUE01");
		properties.getNestedProperty("PROPERTIES").addProperty("Prop02","VALUE02");
		System.err.println(properties);
	}
	
	private static void testaXML() {
		PropertyList properties = new PropertyList();
		properties.addProperty("STRING1", "STRING2");
		List<String> listString = new ArrayList<String>() {{
			add("list1");
			add("list2");
		}};
		properties.addProperty("LIST", listString);
		
		Set<String> listSet = new HashSet<String>() {{
			add("set1");
			add("set2");
			add(null);
		}};		
		properties.addProperty("LIST1", listSet);	
		
		properties.addProperty("NULL1", null);	
		properties.addProperty("Integer", new Integer(1));
		ParseTreeWalker ss = new ParseTreeWalker();
		properties.addProperty("CONTEXT", ss);

		
		System.err.println(properties);

		try {
			BicamSystem.xml(properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}