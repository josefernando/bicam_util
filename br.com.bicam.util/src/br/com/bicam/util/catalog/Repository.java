package br.com.bicam.util.catalog;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import br.com.bicam.util.PropertyListSerializable;

@XmlType(propOrder = {"name", "properties", "sources"})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Repository {
	String name;
	
	PropertyListSerializable propriedades;
	
	Set<Source>   sources;		
	
	public Repository() {}
	
	public Repository(String _name) {
		this.name = _name;
		this.propriedades = new PropertyListSerializable();
		this.sources = new HashSet<Source>();
	}
	
	public String getName() {
		return this.name;
	}	
	
	@XmlElement
	public void setName(String _name) {
		this.name = _name;
	}
	
	public PropertyListSerializable getProperties(){
		return propriedades;
	}
	
	@XmlElement
	public void setProperties(PropertyListSerializable _properties){
		this.propriedades = _properties;
	}
	
	public Set<Source> getSources(){
		return sources;
	}

	@XmlElementWrapper(name = "SourceList")
	@XmlElement(name="source")
	public void setSources(Set<Source> _sources){		
		 sources = _sources;
	}	
	
	public void addSource(Source _source){
		getSources().add(_source);
	}
	
	public Source getSource(String _name){
		for(Source source : sources) {
			if(source.getName().equalsIgnoreCase(_name)) return source;
		}
		return null;
	}
	
	public SourceVersion getSourceVersion(String _id){
		for(Source source : sources) {
			for(SourceVersion sv : source.getVersions()) {
				if(sv.getId().equals(_id)) return sv;
			}
		}
		return null;
	}	

	public void addProperty(String _key, Object _value) {
		getProperties().addProperty(_key, _value);
	}
	
    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Repository)) {
            return false;
        }
        return ((Repository)o).getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
	
	public String toString() {
		return getName();
	}
}