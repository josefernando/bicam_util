package br.com.bicam.util.datamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.jaxb.XmlSetAdapter;
import br.com.bicam.util.symboltable.Symbol_New;

//@XmlType(propOrder = {"id", "properties", "component", "components", "invertedList"})
@XmlType(propOrder = {"id", "properties", "component", "components"})

@XmlSeeAlso({ Vb6Application.class ,
	Vb6Form.class,
	Vb6Module.class,
	Vb6Class.class,
	Vb6Reference.class,
	Vb6Object.class,
	SqlTransactSybaseStoredProcedure.class
})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public abstract class ApplicationComponent {
	String id;
	@XmlTransient
	Symbol_New symbol;
	PropertyList properties;
	Map<String,ApplicationComponent> component;
	@XmlElement(required = true)
	HashSet<ApplicationComponent> components;
//	@XmlElement(required = true)
//	HashSet<String> invertedList;
	
	public ApplicationComponent() {} // Required by Jaxb
	
	public ApplicationComponent(String _id) {
		this.id    = _id;
		properties = new PropertyList();
		components = new HashSet<ApplicationComponent>();
		component  = new HashMap<String,ApplicationComponent>();
//		invertedList = new HashSet<String>();
	} 
	
	public ApplicationComponent(Symbol_New _symbol) {
		this.id    = _symbol.getId();
		symbol = _symbol;
		properties = _symbol.getProperties();
		properties.addProperty("CREATED_IN", new Long(System.currentTimeMillis()).toString());
		components = new HashSet<ApplicationComponent>();
		component  = new HashMap<String,ApplicationComponent>();
//		invertedList = new HashSet<String>();
		bindSymbol();
	}

	public String getId() {
		return id;
	}
	
	@XmlID
	public void setId(String _id) {
		this.id = _id;
	}

	public PropertyList getProperties() {
		return properties;
	}
	
	public void setProperties(PropertyList _properties) {
		this.properties = _properties;
	}
	
	public void addProperties(PropertyList _properties) {
		for(Entry<String, Object> entry : _properties.getProperties().entrySet()) {
			getProperties().addProperty(entry.getKey(), entry.getValue());
		}
	}
	
	public Map<String,ApplicationComponent> getComponent(){
		return component;
	}
	
//	@XmlJavaTypeAdapter(MapApplicationComponentAdapter.class)
//	@XmlElement(name="ITEM_LIST")
	public void setComponent(Map<String,ApplicationComponent> _component){
		component = _component;
	}	
		
	public boolean addComponent(ApplicationComponent _component) {
		components = components == null ? new HashSet<ApplicationComponent>() : components;
		if(components.add(_component)) {
//			component.put(_component.getId(), _component);
//			addInvertedList(_component.getId());
			return true;
		}
		return false;
	}
	
	public boolean addComponents(Set<ApplicationComponent> _components) {
		if(components.addAll(_components)) {
			return true;
		}
		return false;
	}

	public List<ApplicationComponent> getComponentList(PropertyList _properties){
		return null;
	}
	
	public ApplicationComponent getComponent(PropertyList _properties){
		String id = (String)_properties.getProperty("ID");
		return component.get(id);
	}

    public Set<ApplicationComponent> getComponents() {
    	return components == null ? new HashSet<ApplicationComponent>() : components;
    }
    
    public Set<ApplicationComponent> dependesOn() {
    	return components == null ? new HashSet<ApplicationComponent>() : components;
    }

	@XmlJavaTypeAdapter(XmlSetAdapter.class)
	@XmlIDREF
	@XmlElement(name="COMPONENTS_LIST")
    public void setComponents(HashSet<ApplicationComponent> _components) {
    	components = _components;
    }    
    
/*    public Set<String> getInvertedList(){
    	return invertedList;
    }*/
   
/*	@XmlJavaTypeAdapter(SetStringAdapter.class)
	@XmlElement(name="INVERTED_LIST")
    public void setInvertedList(HashSet<String> _invertedList){
    	invertedList = _invertedList;
    }*/
    
/*    public void addInvertedList(String _id) {
    	if(invertedList.add(getId() + ":" + _id)){
    	}
    }*/
    
    public Symbol_New getSymbol() {
    	return symbol;
    }
    
    public abstract void bindSymbol();

    public String toString() {
    	return getId();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ApplicationComponent)) {
            return false;
        }
        return ((ApplicationComponent)o).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }	
}