package br.com.bicam.util.datamodel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
import br.com.bicam.util.jaxb.MapCatalogItemAdapter;
import br.com.bicam.util.jaxb.SetCatalogItemAdapter;

//@XmlType(propOrder = {"id", "properties","parent", "item", "itens", "invertedList"})
@XmlType(propOrder = {"id", "properties","parent", "item", "itens"})

@XmlSeeAlso({ Catalog.class, 
	Repository.class,
	Source.class,
	Version.class,})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public abstract class CatalogItem {
	String id;
	String timestamp;
	CatalogItem parent;
	PropertyList properties;
	@XmlTransient
	Map<String,CatalogItem> item;
	@XmlElement(required = true)
	HashSet<CatalogItem> itens;
//	@XmlElement(required = true)
//	HashSet<String> invertedList;

	public CatalogItem() {}
	
	public CatalogItem(String _id) {
		this.id = _id;
		properties = new PropertyList();
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	    this.timestamp = dateFormat.format(new Date(System.currentTimeMillis()));
		properties.addProperty("DT_CREATED", dateFormat.format(new Date(System.currentTimeMillis())));
		properties.addProperty("TYPE", getClass().getSimpleName());
		itens = new LinkedHashSet<CatalogItem>();
		item  = new HashMap<String,CatalogItem>();
//		invertedList = new HashSet<String>();
	}

	public String getId() {
		return id;
	}
	
	@XmlID
	public void setId(String _id) {
		this.id = _id;
	}

	public CatalogItem getParent() {
		return parent;
	}
	
	@XmlIDREF
	public void setParent(CatalogItem _parent) {
		this.parent = _parent;
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
	
   public Map<String,CatalogItem> getItem(){
		return item;
	}
	

	@XmlJavaTypeAdapter(MapCatalogItemAdapter.class)
	@XmlElement(name="ITEM_LIST")
	public void setItem(Map<String,CatalogItem> _item){
		item = _item;
	}	
		
	public boolean addItem(CatalogItem _item) {
		if(itens.add(_item)) {
			item.put(_item.getId(), _item);
			_item.setParent(this);
//			addInvertedList(_item.getId());
			return true;
		}
		return false;
	}

	public List<CatalogItem> getItemList(PropertyList _properties){
		return null;
	}
	
	public CatalogItem getItem(PropertyList _properties){
		String id = (String)_properties.getProperty("ID");
		return item.get(id);
	}
	
	public CatalogItem getMoreRecentItem(){
		List<CatalogItem> itensSorted = getItens().stream().collect(Collectors.toList());
	    ListIterator<CatalogItem> it = itensSorted.listIterator(itensSorted.size());
	    while(it.hasPrevious()) return it.previous();
	    return null;
	}

    public Set<CatalogItem> getItens() {
    	return itens;
    }

	@XmlJavaTypeAdapter(SetCatalogItemAdapter.class)
	@XmlIDREF
	@XmlElement(name="ITENS_LIST")
    public void setItens(HashSet<CatalogItem> _itens) {
    	itens = _itens;
    }    
    
/*    public Set<String> getInvertedList(){
    	return invertedList;
    }*/
    
    public Repository getRepository(PropertyList _properties) {
    	if(parent instanceof Repository) return (Repository)parent;
    	if(parent != null) return parent.getRepository(_properties);
    	return null;
    }
    
/*	@XmlJavaTypeAdapter(SetStringAdapter.class)
	@XmlElement(name="INVERTED_LIST")
    public void setInvertedList(HashSet<String> _invertedList){
    	invertedList = _invertedList;
    }*/
    
/*    public void addInvertedList(String _id) {
    	if(invertedList.add(getId() + ":" + _id)){
	    	if(parent != null) {
	    		parent.addInvertedList(getId() + ":" + _id);
	    	}
    	}
    }*/

    public String toString() {
    	return getId();
    }
    
    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof CatalogItem)) {
            return false;
        }
        return ((CatalogItem)o).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }	
}