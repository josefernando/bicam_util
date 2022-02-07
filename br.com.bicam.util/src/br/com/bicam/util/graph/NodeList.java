package br.com.bicam.util.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import br.com.bicam.util.PropertyList;

public class NodeList implements Iterable<BicamNode>{
	private Set<BicamNode> nodeSet;
	private Map<String,BicamNode> nodeMap;
	
	public NodeList() {
		nodeSet = new LinkedHashSet<BicamNode>();
		nodeMap = new HashMap<String,BicamNode>();
	}
	
	public boolean add(BicamNode e) {
		if(nodeSet.add(e)) {
			nodeMap.put(e.getId(), e);
			return true;
		}
		return false;
	}
	
	public BicamNode get(String s) {
		return nodeMap.get(s);
	}
	
	public boolean exist(String s) {
		return get(s) == null ? false : true;
	}
	
	public BicamNode create(String s) {
		BicamNode node = get(s);
		if(node != null) return node;
		node = new BicamNode(s);
		add(node);
		return node;
	}
	
	public void appendNodeList(NodeList _other, boolean ..._replace) {
		for(BicamNode node :_other.nodeSet) {
			if(nodeSet.add(node)){
				nodeMap.put(node.getId(), node);
			}
/*			else {
				if(_replace.length > 0) {
					if(_replace[0] == true) {
						nodeMap.remove(node.getId());
						nodeMap.put(node.getId(), node);
					}
				}
			}*/
		}
	}

	@Override
	public Iterator<BicamNode> iterator() {
	    return nodeSet.iterator();
	}
	
	public Set<BicamNode> getNodes(){
		return nodeSet;
	}
	
	public String toString() {
		Set<String> nodes = new LinkedHashSet<String>();
		for(BicamNode node : nodeSet) {
			nodes.add(node.id);
		}
		return nodes.toString();
	}
	
	public String inputGraphviz(PropertyList _properties) {
		StringBuffer sb = new StringBuffer();
		String  graphName = "G";
		Double  ranksep=.75; 
		String  size = "7.5->7.5"; 
		boolean splines=true;
		boolean concentrate=true;
		
		sb.append("digraph " + graphName + "{" + System.lineSeparator());
		sb.append("ranksep=" + ranksep + System.lineSeparator());
		sb.append("size=" + "\"" + size + "\"" + System.lineSeparator());
		sb.append("splines=" + splines + System.lineSeparator());
		sb.append("concentrate=" + concentrate + System.lineSeparator());	
		
		for(BicamNode node : nodeSet) {
			String tail = "\"" + node.getId() + "\"";
			for(BicamAdjacency adj : node.getAdjacencyOut()) {
				String head = "\"" + adj.node.getId() + "\"";
				sb.append(tail + " -> " + head + System.lineSeparator());
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	public String inputSymbolGraph(PropertyList... _properties) {
		StringBuffer sb = new StringBuffer();
		
		for(BicamNode node : nodeSet) {
			String tail = node.getId();
			for(BicamAdjacency adj : node.getAdjacencyOut()) {
				String head = adj.node.getId();
				sb.append(tail + "/" + head + System.lineSeparator());
			}
		}
		return sb.toString();
	}	
}