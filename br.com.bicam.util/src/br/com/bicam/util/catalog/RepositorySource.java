package br.com.bicam.util.catalog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import br.com.bicam.model.util.PropertyListSerializable;

@XmlSeeAlso({ PropertyListSerializable.class,
	          ExecutionUnit.class,
	          Vb6ProjectSerializable.class})
@XmlType(propOrder = {"name"})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
abstract public class RepositorySource {
	@XmlElement (name = "name")
	String name;
    
//	@XmlElement 
//	PropertyListSerializable properties;
//	
	RepositorySource(){}
	
	RepositorySource(String _name){
		this.name = _name;
	}

	public String getName() {
		return name;
	}
	
/*	public void setProperties(PropertyListSerializable _properties) {
		this.properties = _properties;
	}*/
	
	public String toString() {
		return "name=" + getName() + ", properties=" ;//+ properties;
	}
}
