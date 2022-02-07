package br.com.bicam.util.jaxb;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.com.bicam.util.datamodel.CatalogItem;

public class MapCatalogItemAdapter extends XmlAdapter<MapCatalogItemElements[], Map<String, CatalogItem>> {
	
	 final String separator = "::";
	 
	 ArrayDeque<CatalogItem> stack = new ArrayDeque<CatalogItem>();
	 
	 String toUnMarshall = "";
	
	public MapCatalogItemElements[] marshal(Map<String, CatalogItem> arg0) throws Exception {
		MapCatalogItemElements[] mapElements; 

		List<MapCatalogItemElements> mapElementList = new ArrayList<MapCatalogItemElements>();
		
		for (Map.Entry<String, CatalogItem> entry : arg0.entrySet()) {
		    if(entry.getKey() != null && entry.getValue() != null) {
		    	mapElementList.add(new MapCatalogItemElements(entry.getKey(), entry.getValue()));
		    }
		}
		
		mapElements = new MapCatalogItemElements[mapElementList.size()];
		int i = 0;
		for(MapCatalogItemElements m : mapElementList) {
			mapElements[i++] = m;
		}
		return mapElements;
	}

	public Map<String, CatalogItem> unmarshal(MapCatalogItemElements[] arg0) throws Exception {
		Map<String, CatalogItem> r = new HashMap<String, CatalogItem>();
		for (MapCatalogItemElements mapelement : arg0) {
				r.put(mapelement.key, mapelement.value);
		}
		return r;
	}
}