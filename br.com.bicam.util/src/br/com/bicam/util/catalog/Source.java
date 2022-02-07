package br.com.bicam.util.catalog;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import br.com.bicam.util.PropertyListSerializable;

@XmlSeeAlso({ Vb6ProjectSerializable.class, 
	          Vb6FormSer.class,
	          Vb6ClassSer.class,
	          Vb6ModuleSer.class,})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
abstract public class Source {
	String name;
	PropertyListSerializable properties;
	List<SourceVersion> versions;
	
	public Source() {}
	
	public Source(String _name) {
		this.name = _name;
		properties = new PropertyListSerializable();
		versions = new ArrayList<SourceVersion>();
	}
	
	public String getName() {
		return this.name;
	}
	
	@XmlElement
	public void setName(String _name) {
		this.name = _name;
	}
	
	public void addProperty(String key, String value) {
		getProperties().addProperty(key, value);
	}
	
	public PropertyListSerializable getProperties() {
		return properties;
	}
	
	@XmlElement
	public void setProperties(PropertyListSerializable _properties) {
		this.properties = _properties;
	}
	
	public List<SourceVersion> getVersions() {
		return versions;
	}
	
	@XmlElement
	public void setVersions(List<SourceVersion> _versions) {
		this.versions = _versions;
	}
	
	public void addVersion(SourceVersion _version) {
		versions.add(_version);
	}
}