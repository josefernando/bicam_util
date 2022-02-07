package br.com.bicam.util.graph;

import java.util.HashSet;
import java.util.Set;

import br.com.bicam.util.PropertyList;

public class BicamNode {
	String id;
	Integer vertex;
	Set<BicamAdjacency> adjacencyIn;
	Set<BicamAdjacency> adjacencyOut;

	PropertyList properties;
	
	public BicamNode(String _id, Integer _vertex){
		this.id = _id;
		this.vertex = _vertex;
		adjacencyIn = new HashSet<BicamAdjacency>();
		adjacencyOut = new HashSet<BicamAdjacency>();

		properties = new PropertyList();
	}
	
	public BicamNode(String _id){
		this.id = _id;
		adjacencyIn = new HashSet<BicamAdjacency>();
		adjacencyOut = new HashSet<BicamAdjacency>();

		properties = new PropertyList();
	}
	
	public PropertyList getProperties(){
		return properties;
	}
	
	public String getId(){
		return id;
	}
	
	public Integer getVertex(){
		return vertex;
	}

	public Object getProperty(String _key) {
		return properties.getProperty(_key);
	}
	
	public Set<BicamAdjacency> getAdjacencyIn(){
		return adjacencyIn;
	}
	
	public void addAdjacencyIn(BicamAdjacency _adjacency){
		adjacencyIn.add(_adjacency);
	}	
	
	public Set<BicamAdjacency> getAdjacencyOut(){
		return adjacencyOut;
	}
	
	public void addAdjacency(BicamAdjacency _adjacency){
		adjacencyOut.add(_adjacency);
	}	
	
	public void addAdjacencyOut(BicamAdjacency _adjacency){
		adjacencyOut.add(_adjacency);
	}
	
	public BicamAdjacency getBicamAdjacencyOut(String _id) {
		for(BicamAdjacency out : adjacencyOut) {
			if(out.getNode().getId().equals(_id));
			return out;
		}
		return null;
	}
	
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof BicamNode)) {
            return false;
        }
        if(this.getId().equals(((BicamNode)other).getId())) {
        	return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }
	
	private Object getProperty(PropertyList _properties, String _keyProperty) {
		return _properties.getProperty(_keyProperty);
	}
	
	public Object getBicamAdjacencyOutProperty(String _nodeId, String _keyProperty) {
		for(BicamAdjacency out : adjacencyOut) {
			if(out.getNode().getId().equals(_nodeId)) {
				return getProperty(out.getProperties(), _keyProperty);
			}
		}
		return null;
	}
	
	public String toString() {
		return "id: " + getId() + ", vertex: " + getVertex() + ", Adjacency In: " + adjacencyIn + ", Adjacency Out: " + adjacencyOut +", properties: " + getProperties();
	}
}
