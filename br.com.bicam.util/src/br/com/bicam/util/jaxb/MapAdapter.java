package br.com.bicam.util.jaxb;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;

public class MapAdapter extends XmlAdapter<MapElements[], Map<String, Object>> {
	
	public MapAdapter() { // Unit test Only
		
	}
	
	 final String separator = "::";
	 
	 ArrayDeque<Object> stack = new ArrayDeque<Object>();
	 
	 String toUnMarshall = "";
	
	public MapElements[] marshal(Map<String, Object> arg0) throws Exception {
		MapElements[] mapElements; 

		List<MapElements> mapElementList = new ArrayList<MapElements>();
		
		for (Map.Entry<String, Object> entry : arg0.entrySet()) {
			
			    if(entry.getKey() != null && entry.getValue() != null) {
				mapElementList.add(new MapElements(entry.getKey(), marshallValue(entry.getValue())));
			    }
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
				r.put(mapelement.key, unMarshallValue(getMainQueue(toUnMarshall)));
		}
		return r;
	}
	
	private String marshallValue(Object obj) {
		String ret = "";
		
		if(obj == null) {
			ret = "null";
		}
		else if(String.class.isInstance(obj)) {
			ret = (String)obj;
		}
		else if(PropertyList.class.isInstance(obj)) {
			Set<Entry<String, Object>> entries = ((PropertyList)obj).getProperties().entrySet();
			ret = "PROPERTY_LIST";
			for(Map.Entry<String, Object> entry : entries){
				if(String.class.isInstance(entry.getKey()) 
						&& String.class.isInstance(marshallValue(entry.getValue()))){
					ret =  ret + separator + "ENTRY_PROPERTY_KEY" + separator + entry.getKey() + 
						     separator + "ENTRY_PROPERTY_VAL" + separator + marshallValue(entry.getValue());
				}
			}
		}
		else if(List.class.isInstance(obj)) {
			List<Object> entries = ((ArrayList)obj);
			ret = "ARRAY_LIST";
			for(Object entry : entries){
					ret =  ret + separator + "ENTRY_LIST" + separator + marshallValue(entry);
			}
		}
		else if(Set.class.isInstance(obj)) {
			Set<Object> entriesSet = (Set<Object>)obj;
			ret = "SET_LIST";
			for(Object entry : entriesSet){
					ret =  ret + separator + "ENTRY_SET" + separator + marshallValue(entry);
			}
		}		
		else {
			try {
				throw new Exception();
			}catch(Exception e) {
				BicamSystem.printLog("WARNING", "INVALID VALUE CLASS: " + obj.getClass().getName(), e);
				ret = "??" + obj.getClass().getName() + "??";
			}
		}
		return ret;
	}
	
	public Object unMarshallValue(Deque<String> marshallQueue){
		Deque<Object> mqTemp = new ArrayDeque<Object>();
		Deque<Object> unMarshallValue = new ArrayDeque<Object>();
		PropertyList properties = null;

		String token = null;
		while(!marshallQueue.isEmpty()) {
			token = "ARRAY_LIST";
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				if(unMarshallValue.peek() instanceof ArrayList) {
					marshallQueue.pop();
					continue;
				}
				else {
					BicamSystem.printLog("ERROR", "VALUE DEVERIA SER ArrayList");
				}				
			}
			
			else token = "ENTRY_LIST"; 
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				marshallQueue.pop();
				Object value = unMarshallValue.pop();
				if(!(unMarshallValue.peek() instanceof ArrayList)) {
					if(!(unMarshallValue.peek() instanceof ArrayList)) unMarshallValue.push(new ArrayList());	
				}
				((ArrayList)unMarshallValue.peek()).add(value);
				continue;
			}
			
			else token = "SET_LIST"; 
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				if(unMarshallValue.peek() instanceof HashSet) {
					marshallQueue.pop();
					continue;
				}
				else {
					BicamSystem.printLog("ERROR", "VALUE DEVERIA SER HashSet");
				}		
			}
			else token = "ENTRY_SET"; 
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				marshallQueue.pop();
				Object value = unMarshallValue.pop();
				if(!(unMarshallValue.peek() instanceof ArrayList)) {
					if(!(unMarshallValue.peek() instanceof HashSet)) unMarshallValue.push(new HashSet());	
				}
				((HashSet)unMarshallValue.peek()).add(value);
				continue;
			}
			
			else token = "PROPERTY_LIST"; 
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				if(unMarshallValue.peek() instanceof PropertyList) {
					marshallQueue.pop();
					continue;
				}
				else {
					BicamSystem.printLog("ERROR", "VALUE DEVERIA SER PropertyList");
				}
			}
			
			else token = "ENTRY_PROPERTY_KEY"; 
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				continue;
			}
			else token = "ENTRY_PROPERTY_VAL";
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				Object value = unMarshallValue.pop();
				marshallQueue.pop(); // elimina token "ENTRY_PROPERTY_VAL"
				String key = marshallQueue.pop(); 
				if(marshallQueue.peek().equalsIgnoreCase("ENTRY_PROPERTY_KEY")) {
					marshallQueue.pop();
				}
				else {
					BicamSystem.printLog("ERROR", "INVALID ENTRY IN MARSHALLED STRING VALUE");
				}
				
				if(!(unMarshallValue.peek() instanceof PropertyList)) unMarshallValue.push(new PropertyList());
				
				((PropertyList)unMarshallValue.peek()).addProperty(key, value);
				continue;
			}
			else token = null;
			if(marshallQueue.peek().equalsIgnoreCase(token)) {
				unMarshallValue.push(null);
				continue;
			}
			else {
				unMarshallValue.push(marshallQueue.pop());
				continue;
			}
		}
		return unMarshallValue.peek();
	}

	
	public Deque<String> getMainQueue(String _toUnMarshall){
		Deque<String> mq = new ArrayDeque<String>();
		String token = null;
		
		while(_toUnMarshall.length() > 0) {
			token = "ARRAY_LIST" + separator;
			if(_toUnMarshall.startsWith(token)) {
				_toUnMarshall = _toUnMarshall.replaceFirst(token, "");
				mq.push(token.replace(separator, ""));
				continue;
			}
			else token = "ENTRY_LIST" + separator; 
			if(_toUnMarshall.startsWith(token)) {
				token = "ENTRY_LIST" + separator;
				_toUnMarshall = _toUnMarshall.replaceFirst(token, "");
				mq.push(token.replace(separator, ""));
				continue;
			}		
			else token = "SET_LIST" + separator;
			if(_toUnMarshall.startsWith(token)) {
				_toUnMarshall = _toUnMarshall.replaceFirst(token, "");
				mq.push(token.replace(separator, ""));
				continue;
			}
			else token = "ENTRY_SET" + separator;
			if(_toUnMarshall.startsWith(token)) {
				_toUnMarshall = _toUnMarshall.replaceFirst(token, "");
				mq.push(token.replace(separator, ""));
				continue;
			}		
			else token = "PROPERTY_LIST" + separator;
			if(_toUnMarshall.startsWith(token)) {
				_toUnMarshall = _toUnMarshall.replaceFirst(token, "");
				mq.push(token.replace(separator, ""));
				continue;
			}		
			else token = "ENTRY_PROPERTY_KEY" + separator;
			if(_toUnMarshall.startsWith(token)) {
				_toUnMarshall = _toUnMarshall.replaceFirst(token, "");
				mq.push(token.replace(separator, ""));
				continue;
			}
			else token = "ENTRY_PROPERTY_VAL" + separator;
			if(_toUnMarshall.startsWith(token)) {
				_toUnMarshall = _toUnMarshall.replaceFirst(token, "");
				mq.push(token.replace(separator, ""));
				continue;
			}
			else {
				int ix = _toUnMarshall.indexOf("::");
				if(ix > 0) {
					mq.push(_toUnMarshall.substring(0,ix));
					_toUnMarshall = _toUnMarshall.substring(ix+2);
				}
				else {
					mq.push(_toUnMarshall.substring(0));
					_toUnMarshall = "";
				}
				continue;
			}
		}
		return mq;
	}
	
	public static void main(String args[]) {
		String s = 
 "ARRAY_LIST::ENTRY_LIST::PROPERTY_LIST::ENTRY_PROPERTY_KEY::FILE_NAME::ENTRY_PROPERTY_VAL::TIFMA001.FRM::ENTRY_PROPERTY_KEY::NAME::ENTRY_PROPERTY_VAL::TIFMA001::ENTRY_LIST::PROPERTY_LIST::ENTRY_PROPERTY_KEY::FILE_NAME::ENTRY_PROPERTY_VAL::TIFMA002.FRM::ENTRY_PROPERTY_KEY::NAME::ENTRY_PROPERTY_VAL::TIFMA002"; 
		MapAdapter ma = new MapAdapter();
		Deque ad = ma.getMainQueue(s);
		System.err.println(ma.unMarshallValue(ad));
	}
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