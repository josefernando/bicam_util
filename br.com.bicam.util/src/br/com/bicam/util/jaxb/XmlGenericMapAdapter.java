package br.com.bicam.util.jaxb;
 
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;
 
/**
 *
 * @author John Yeary
 * @version 1.0
 * http://javaevangelist.blogspot.com/2011/12/java-tip-of-day-generic-jaxb-map-v.html
 */
public class XmlGenericMapAdapter<K, V> extends XmlAdapter<MapType<K, V>, Map<K, V>> {
 
    @Override
    public Map<K, V> unmarshal(MapType<K, V> v) throws Exception {
        HashMap<K, V> map = new HashMap<K, V>();
 
        for (MapEntryType<K, V> mapEntryType : v.getEntry()) {
            map.put(mapEntryType.getKey(), mapEntryType.getValue());
        }
        return map;
    }
 
    @Override
    public MapType<K, V> marshal(Map<K, V> v) throws Exception {
        MapType<K, V> mapType = new MapType<K, V>();
 
        for (Map.Entry<K, V> entry : v.entrySet()) {
            MapEntryType<K, V> mapEntryType = new MapEntryType<K, V>();
            mapEntryType.setKey(entry.getKey());
            mapEntryType.setValue(entry.getValue());
            mapType.getEntry().add(mapEntryType);
        }
        return mapType;
    }
}