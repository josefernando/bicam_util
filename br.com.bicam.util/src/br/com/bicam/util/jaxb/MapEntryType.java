package br.com.bicam.util.jaxb;
 
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
 
/**
 *
 * @author John Yeary 
 * @version 1.0
 * http://javaevangelist.blogspot.com/2011/12/java-tip-of-day-generic-jaxb-map-v.html
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement
public class MapEntryType<K, V> {
 
    private K key;
    private V value;
 
    public MapEntryType() {
    }
 
    public MapEntryType(Map.Entry<K, V> e) {
        key = e.getKey();
        value = e.getValue();
    }
 
    public K getKey() {
        return key;
    }
 
    public void setKey(K key) {
        this.key = key;
    }
 
    public V getValue() {
        return value;
    }
 
    public void setValue(V value) {
        this.value = value;
    }
}