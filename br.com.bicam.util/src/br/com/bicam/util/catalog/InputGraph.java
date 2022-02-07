package br.com.bicam.util.catalog;

public class InputGraph {
	String tailNode ;
	String headNode ;
	Double weight;
	
	
	public InputGraph(String _tail) {
		this.tailNode = _tail;
	}
	
	public InputGraph(String _tail, String _head) {
		this(_tail);
		this.headNode = _head;
	}
	
	public InputGraph(String _tail, String _head, Double _weight) {
		this(_tail, _head);
		this.weight = _weight;
	}

	public String getTailNode() {
		return tailNode;
	}

	public void setTailNode(String tailNode) {
		this.tailNode = tailNode;
	}

	public String getHeadNode() {
		return headNode;
	}

	public void setHeadNode(String headNode) {
		this.headNode = headNode;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}	
} 