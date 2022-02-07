package br.com.bicam.util.graph;

import java.util.HashSet;
import java.util.Set;

import br.com.bicam.util.PropertyList;

public class Path {
	
	SymbolWeightedGraph graph;
//	CompilationUnit compUnit;
	String from;
	String to;
	String type;  // s=shortest , a=all, w=shortest weight
	
	Set<Integer> shortestPath;
	
	public Path(PropertyList _properties) {
//		this.compUnit = (CompilationUnit) _properties.getProperty("COMPILATION_UNIT");
//		this.graph = (SymbolWeightedGraph) compUnit.getProperties().getProperty("COMP_UNIT_GRAPH");
		this.graph = (SymbolWeightedGraph)_properties.getProperty("COMP_UNIT_GRAPH");
		this.shortestPath = new HashSet<Integer>();
	}

	public SymbolWeightedGraph getGraph() {
		return graph;
	}

	public void setGraph(SymbolWeightedGraph graph) {
		this.graph = graph;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String _from) {
		this.from = _from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String _to) {
		this.to = _to;
	}

	public String getType() {
		return type;
	}

	public void setType(String _type) {
		this.type = _type;
	}
	
	public Set<Integer> getShortestPath() {
		return shortestPath;
	}
	
	public void run() {
		System.err.println("SHORTEST PATH WEIGHT");
		ShortestPathByWeightDijkstra paths = new ShortestPathByWeightDijkstra(graph.getGraph(), graph.vertexIndex(from));
		Integer[] ppath  = paths.pathTo(graph.vertexIndex(to));
		StringBuffer p = new StringBuffer();
		p = new StringBuffer();
		for(Integer item : ppath){
			if(p.length() > 0) p.append(" -> ");
			p.append(graph.vertexName(item));
			String xx = graph.vertexName(item).split(":")[1];
			shortestPath.add(new Integer(xx));
		}

		System.err.println(p);		
	}
}