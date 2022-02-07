package br.com.bicam.util.graph;

public class Adjacency implements Comparable<Adjacency>{ //Adjacency é a (b) ponta da flecha em a -> b
	Integer vertice;
	Double  weight;
	
	public Adjacency(Integer _vertice){
		this.vertice = _vertice;
		this.weight = 0.0;
	}
	
	public Adjacency(Integer _vertice, Double _weight){
		this.vertice = _vertice;
		if(_weight == null) {
			this.weight = 999.0;
		}
		else {
			this.weight = _weight;
		}
	}

	public Integer getVertice() {
		return vertice;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	@Override
	public int compareTo(Adjacency o) {
		if(this.weight < o.weight) return -1;
		if(this.weight > o.weight) return 1;
		return 0;
	}
}
