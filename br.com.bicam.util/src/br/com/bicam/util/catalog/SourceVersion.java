package br.com.bicam.util.catalog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import br.com.bicam.util.InputRepository;
import br.com.bicam.util.PropertyListSerializable;

@XmlType(propOrder = {"id", "sourceName", "properties"})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class SourceVersion {
	String id;
	String sourceName;
	PropertyListSerializable properties;
	
	public SourceVersion() {}
	
	public SourceVersion(InputRepository _input, String _sourceName ) {
		this.id = _input.getHash();
		this.sourceName = _sourceName;
		properties = new PropertyListSerializable();
		properties.addProperty("INPUT_TYPE", _input.getProperties().getProperty("TYPE"));
		properties.addProperty("INPUT_LOCATION", _input.getProperties().getProperty("LOCATION"));
	}

	public String getId() {
		return id;
	}
	
	@XmlElement
	public void setId(String _id) {
		this.id = _id;
	}
	

	public String getSourceName() {
		return sourceName;
	}
	
	@XmlElement
	public void setSourceName(String _sourceName) {
		this.sourceName = _sourceName;
	}	

	public PropertyListSerializable getProperties() {
		return properties;
	}

	@XmlElement
	public void setProperties(PropertyListSerializable _properties) {
		this.properties = _properties;
	}	
}