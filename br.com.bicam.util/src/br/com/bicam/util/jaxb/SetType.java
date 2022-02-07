package br.com.bicam.util.jaxb;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement
public class SetType<T> {
    private List<T> entry = new ArrayList<T>();
 
    public SetType() {}
 
    public SetType(Set<T> set) {
        for (T e : set) {
            entry.add(e);
        }
    }
 
    public List<T> getEntry() {
        return entry;
    }
 
    public void setEntry(List<T> entry) {
        this.entry = entry;
    }
    
    public void addEntry(T e) {
        this.entry.add(e);
    }  
 
}