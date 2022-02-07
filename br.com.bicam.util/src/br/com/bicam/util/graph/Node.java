package br.com.bicam.util.graph;

import java.util.Iterator;
import java.util.LinkedHashSet;

import br.com.bicam.util.PropertyList;

public class Node<T> {
	String id;
	T item;
	PropertyList properties;
	LinkedHashSet<Node<T>> inEdge;
	LinkedHashSet<Node<T>> outEdge;
	
//	IdentityHashMap<Node<T>, Double> weight;
	
	public Node(String _s, T _item){
		if(_s == null){
			throw new IllegalArgumentException("Invalid null argument");
		}
		this.id = _s;
		this.item = _item;
		properties = new PropertyList();
		inEdge = new LinkedHashSet<Node<T>>();
		outEdge = new LinkedHashSet<Node<T>>();
//		weight = new IdentityHashMap<Node<T>, Double>();
	}
	
	public PropertyList getProperties(){
		return properties;
	}
	
	public String getId(){
		return id;
	}
	
	public T getItem(){
		return item;
	}
	
	public String getText(){
		Object text = getProperty("TEXT");
		if(text != null) return (String)text;
		return  null;
	}
	
	public void setItem(T _item){
		this.item = _item;
	}	
	
	public void addInEdge(Node<T> _in){
		inEdge.add(_in);
	}
	
	public LinkedHashSet<Node<T>> getInEdge(){
		return inEdge;
	}
	
	public void addOutEdge(Node<T> _out){
		outEdge.add(_out);
	}
	
	public LinkedHashSet<Node<T>> getOutEdge(){
		return outEdge;
	}
	
	public LinkedHashSet<Node<T>> getAdjacentList(){
		return getOutEdge();
	}
	
	public void addProperty(String _Key, Object _value){
		properties.addProperty(_Key, _value);
	}
	
	public Object getProperty(String _key){
		return properties.getProperty(_key);
	}
	
	public Object getGeneric(){
		return item;
	}
	
	public Node<T> getCopy(boolean _order){ // true sameOrder or false reverseOrder
		Node<T> node= new Node<T>(this.getId(), (T)this.getGeneric());
		node.properties =  this.properties;
		if(_order){
			node.inEdge = this.inEdge;
			node.outEdge = this.outEdge;				
		}
		else {
			node.inEdge = this.outEdge;
			node.outEdge = this.inEdge;			
		}
		return node;
	}
	
	public String toString(){
    	StringBuffer s = new StringBuffer();
    	s.append("");
    	s.append("\n" + getId() + ": ");
    	Iterator<Node<T>> it = getAdjacentList().iterator();
    		String comma = "";
    		while(it.hasNext()){
    			s.append(comma + it.next().getId());
    			comma = ", ";
    		}
    		comma = "";
    	return s.toString();
	}
	
    public String toString(String _desc, String... _line){
    	StringBuffer s = new StringBuffer();
    	s.append("");
    		s.append("\n" + (_line.length == 0 ? getId() : (String)getProperty(_line[0])) 
    		+ "-"  + (String)getProperty(_desc) + ": ");
    		Iterator<Node<T>> it = getAdjacentList().iterator();
    		String comma = "";
    		while(it.hasNext()){
    			Node<T> n = it.next();
    			s.append(comma + (_line.length == 0 ? n.getId() : (String)n.getProperty(_line[0]))
    			+ "-"  + (String)n.getProperty(_desc));
    			comma = ", ";
    		}
    		comma = "";
    	return s.toString();
    }	
}
