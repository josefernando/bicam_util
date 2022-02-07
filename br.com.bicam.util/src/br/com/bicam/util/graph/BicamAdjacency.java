package br.com.bicam.util.graph;

import br.com.bicam.util.PropertyList;

public class BicamAdjacency implements Comparable<BicamAdjacency>{ //Adjacency é a (b) ponta da flecha em a -> b
	BicamNode node;
	PropertyList properties;
	
	public BicamAdjacency(BicamNode _node){
			this.node = _node;	
			properties = new PropertyList();
	}

	public BicamNode getNode() {
		return node;
	}

	public Double getWeight() {
		return getProperty("WEIGHT") ==  null ? null : (Double)getProperty("WEIGHT");
	}

	public String getLabel() {
		return getProperty("LABEL") ==  null ? null : (String)getProperty("LABEL");
	}
	
	public String getLabel1() {
		return getProperty("LABEL1") ==  null ? null : (String)getProperty("LABEL1");
	}

	public void setWeight(Double _weight) {
		properties.addProperty("WEIGHT", _weight);
	}
	
	public Object getProperty(String _key) {
		return properties.getProperty(_key);
	}
	
	public void setProperty(String _key, Object _value) {
		properties.addProperty(_key, _value);
	}
	
	public PropertyList getProperties() {
		return properties;
	}
	
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof BicamAdjacency)) {
            return false;
        }
        if(this.getNode() == getNode()) {
        	return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
    	return node.getId().hashCode();
    }
    
	public String toString() {
		return "node=" + "{" + node.id + "}, weight=" + getWeight() + ", properties=" + properties;
	}

	@Override
	public int compareTo(BicamAdjacency o) {
		if(this.getWeight() < o.getWeight()) return -1;
		if(this.getWeight() > o.getWeight()) return 1;
		return 0;
	}
}