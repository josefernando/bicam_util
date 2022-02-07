package br.com.bicam.util;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class PropertyListSerializable {

	HashMap<String, Object> properties;

	public PropertyListSerializable() {
		properties = new HashMap<String, Object>();
	}

	public HashMap<String, Object> getProperties() {
		return properties;
	}
	
	@XmlJavaTypeAdapter(MapAdapter.class)
	@XmlElement(name="PROPERTY_LIST")
	public void setProperties(HashMap<String, Object> _properties) {
		this.properties = _properties;
	}

	public Set<String> getKeys() {
		return properties.keySet();
	}

	public Collection<Object> getValues() {
		return properties.values();
	}

	public void addProperty(String _key, Object _value) {
		if( !String.class.isInstance(_value) 
		 && !PropertyListSerializable.class.isInstance(_value)
		 && !List.class.isInstance(_value)
		 && !Serializable.class.isInstance(_value)) {
			    try {
			    	throw new Exception();
			    }
			    catch(Exception e) {
			    	e.printStackTrace();
					BicamSystem.printLog("ERROR", "property is not String, PropertyListSerializable or ArrayList");
			    }
		}
		properties.put(_key, _value);
	}

	public Object getProperty(String _key) {
		return properties.get(_key);
	}

	public boolean hasProperty(String _key) {
		return properties.keySet().contains(_key);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (String key : properties.keySet()) {
			if (!sb.toString().equals("{")) {
				sb.append(", ");
			}
			if (String.class.isInstance(properties.get(key))) {
				sb.append(key + "=" + (String) properties.get(key));
			}
			if (properties.get(key) instanceof PropertyListSerializable) {
				sb.append(key + "=" + properties.get(key.toString()));
			}
			if (properties.get(key) instanceof ArrayList) {
				sb.append(key + "=" + properties.get(key.toString()));
			}
/*			else {
				try {
					throw new Exception();

				} catch (Exception e) {
					BicamSystem.printLog("DEBUG", properties.get(key).getClass().getName() + " - instance not is String or  PropertyListSerializable", e);
					e.printStackTrace();
				}
			}*/
		}
		return sb.append("}").toString();
	}
}

class MapAdapter extends XmlAdapter<MapElements[], Map<String, Object>> {
	
	 final String separator = "::";
	 
	 ArrayDeque<Object> stack = new ArrayDeque<Object>();
	 
	 String toUnMarshall = "";
	
	public MapElements[] marshal(Map<String, Object> arg0) throws Exception {
		MapElements[] mapElements; 

		List<MapElements> mapElementList = new ArrayList<MapElements>();
		
		for (Map.Entry<String, Object> entry : arg0.entrySet()) {
				mapElementList.add(new MapElements(entry.getKey(), marshallValue(entry.getValue())));
		}
		
		mapElements = new MapElements[mapElementList.size()];
		int i = 0;
		for(MapElements m : mapElementList) {
			mapElements[i++] = m;
		}
		return mapElements;
	}

	public Map<String, Object> unmarshal(MapElements[] arg0) throws Exception {
		Map<String, Object> r = new HashMap<String, Object>();
		for (MapElements mapelement : arg0) {
				toUnMarshall = mapelement.value;
				r.put(mapelement.key, unMarshallValue());
		}
		return r;
	}
	
	private String marshallValue(Object obj) {
		String ret = "";
		if(String.class.isInstance(obj)) {
			ret = (String)obj;
		}
		else if(PropertyListSerializable.class.isInstance(obj)) {
			Set<Entry<String, Object>> entries = ((PropertyListSerializable)obj).getProperties().entrySet();
			ret = "PROPERTY_LIST";
			for(Map.Entry<String, Object> entry : entries){
				ret =  ret + separator + "ENTRY_PROPERTY_KEY" + separator + entry.getKey() + 
						     separator + "ENTRY_PROPERTY_VAL" + separator + marshallValue(entry.getValue());
			}
		}
		else if(List.class.isInstance(obj)) {
			List<Object> entries = ((ArrayList)obj);
			ret = "ARRAY_LIST";
			for(Object entry : entries){
				ret =  ret + separator + "ENTRY" + separator + marshallValue(entry);
			}
		}
		else {
			try {
				throw new Exception();
			}catch(Exception e) {
				BicamSystem.printLog("ERROR", "INVALID VALUE CLASS: " + obj.getClass().getName(), e);
			}
		}
		
		return ret;
	}
	 
	private Object unMarshallValue() {
		Object ret = null;
		if(toUnMarshall.startsWith("ARRAY_LIST::")) {
			List<Object> ar = new ArrayList<Object>();
			stack.push(ar);
			toUnMarshall = toUnMarshall.substring("ARRAY_LIST::".length());
			unMarshallValue();
			return stack.pop();
		}
		if(toUnMarshall.startsWith("ENTRY::")) {
			while(toUnMarshall.startsWith("ENTRY::")) {
				toUnMarshall = toUnMarshall.substring("ENTRY::".length());
				((ArrayList)stack.peek()).add(unMarshallValue());
			}
			
			return null;
		}
		if(toUnMarshall.startsWith("PROPERTY_LIST::")) {
			PropertyListSerializable propSer = new PropertyListSerializable();
			stack.push(propSer);
			toUnMarshall = toUnMarshall.substring("PROPERTY_LIST::".length());
			unMarshallValue();
//			stack.pop();
			return stack.pop();
		}
		if(toUnMarshall.startsWith("ENTRY_PROPERTY_KEY::")) {
			while(toUnMarshall.startsWith("ENTRY_PROPERTY_KEY::")) {
				PropertyListSerializable propSer = (PropertyListSerializable) stack.peek();
				toUnMarshall = toUnMarshall.substring("ENTRY_PROPERTY_KEY::".length());
				int i = toUnMarshall.indexOf("::ENTRY_PROPERTY_VAL::");
				String key = toUnMarshall.substring(0,i);
				toUnMarshall = toUnMarshall.substring(i);
				toUnMarshall = toUnMarshall.substring("::ENTRY_PROPERTY_VAL::".length());
				i = toUnMarshall.indexOf("::");
				Object value = null;
				if(i == -1) {
					value = toUnMarshall;
					toUnMarshall = "";
				}
				else {
					value = toUnMarshall.substring(0,i);
					toUnMarshall = toUnMarshall.substring(i + "::".length());
				}
				propSer.addProperty(key, value);
			}	
		}
		return toUnMarshall;
	}

	
/*	private List<MapElements> complexElement(String _key , PropertyListSerializable _value){
		List<MapElements> mapElementList = new ArrayList();
		for(Map.Entry<String, Object> entry : _value.getProperties().entrySet()) {
			String plusKey = _key + separator + entry.getKey();
			if(String.class.isInstance(entry.getValue())){
				mapElementList.add(new MapElements(plusKey,(String)entry.getValue()));
			}
			else { // is PropertyList
				mapElementList.addAll(complexElement(plusKey, (PropertyListSerializable)entry.getValue()));
			}
		}
		return mapElementList;
	}*/
}

/*@XmlSeeAlso({ MapElementsString2PropertyList.class,
	          MapElementsString2ArrayList.class})*/
//@XmlSeeAlso({ MapElementsString2PropertyList.class})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
class MapElements {
	public String key;
	public String value;

	public MapElements() {} // Required by JAXB

	public MapElements(String key, String value) {
		this.key   = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String _key) {
		key = _key;
	}	
	
	public String getValue() {
		return (String)value;
	}
	
	public void setValue(String _value) {
		value = _value;
	}
	
	public String toString() {
		return "key=" + key + ", " + "value=" + value;
	}
}