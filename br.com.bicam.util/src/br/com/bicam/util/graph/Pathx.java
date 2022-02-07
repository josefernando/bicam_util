package br.com.bicam.util.graph;

import java.util.ArrayDeque;

public class Pathx<T>  implements Comparable<Pathx<T>>{
	ArrayDeque<Node<T>> lnkNode; // = new ArrayDeque<Node>();
	double weight;
	
	public Pathx() {
		lnkNode = new ArrayDeque<Node<T>>();
	}
	
	public void add(Node<T> _node){
		lnkNode.add(_node);
	}
	
	public ArrayDeque<Node<T>> getNodes(){
		return lnkNode;
	}
	
/*	public double getWeightByNode(Node<T> _node){
		if(weight !=0) return weight;
		ArrayList<Node<T>> nodes = new ArrayList<Node<T>>(lnkNode);
		for(Node<T> node : nodes){
			weight += node.getWeightByNode(node);
		}
		return weight;
	}*/

	@Override
	public int compareTo(Pathx<T> _other) {
		if(this.weight < _other.weight) return -1;
		if(this.weight > _other.weight) return +1;
		return 0;
	}
}
