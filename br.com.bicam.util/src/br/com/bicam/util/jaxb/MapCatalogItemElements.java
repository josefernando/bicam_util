package br.com.bicam.util.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import br.com.bicam.util.datamodel.CatalogItem;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class MapCatalogItemElements {
	public String key;
	public CatalogItem value;

	public MapCatalogItemElements() {} // Required by JAXB

	public MapCatalogItemElements(String key, CatalogItem value) {
		this.key   = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String _key) {
		key = _key;
	}	
	
	public CatalogItem getValue() {
		return value;
	}
	
	public void setValue(CatalogItem _value) {
		value = _value;
	}
	
	public String toString() {
		return "key=" + key + ", " + "value=" + value;
	}
}