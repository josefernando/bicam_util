package br.com.bicam.util.graph;

public class EdgeOld<T> {
	
	private Node<T>  from;
	private Node<T>  to;
    private double weight;
	public Node<T> getFrom() {
		return from;
	}
	public void setFrom(Node<T> from) {
		this.from = from;
	}
	public Node<T> getTo() {
		return to;
	}
	public void setTo(Node<T> to) {
		this.to = to;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	} 
}
