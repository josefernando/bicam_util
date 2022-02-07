package br.com.bicam.util.graph;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import br.com.bicam.util.BicamSystem;

public class WeightedGraph {
	private int numVertices;
	private int numEdges;
	private String separator;
	private ArrayList<Adjacency>[] adj;
	private Set<String> vertices;
	
	WeightedGraph reverse;
	
	public WeightedGraph(WeightedGraph _nonReverseGraph) {
		numVertices = _nonReverseGraph.getNumVertices();
		vertices = new HashSet<>();
		adj = new ArrayList[numVertices];
		for (int v = 0 ; v < numVertices; v++){
			adj[v] = new ArrayList<Adjacency>();
		}
	}
	
	public WeightedGraph(int _numVertices){
		this.numVertices = _numVertices;
		this.numEdges = 0;
		vertices = new HashSet<>();

		adj = new ArrayList[numVertices];
		
		for (int v = 0 ; v < numVertices; v++){
			adj[v] = new ArrayList<Adjacency>();
		}
	}
	
	public WeightedGraph(File _file, String..._separator){
		if(_separator.length > 0) {
			separator = _separator[0];
		}
		else {
			separator = " ";
		}
		vertices = new HashSet<>();

		this.numVertices = countVertices(_file,separator);
		
		adj = new ArrayList[numVertices];
		
		for (int v = 0 ; v < numVertices; v++){
			adj[v] = new ArrayList<Adjacency>();
		}
		
		addEdges(_file);
	}
	
	public void addEdge(int _vertex, Adjacency _adj){
//		int adjVertex = _adj.getVertice();
		for(Adjacency a : adj[_vertex]) {
			if(a.getVertice() == _adj.getVertice()) {
				System.err.println("*** WARNING - Loops Arestas paralelas não são permitidas");
				return; // aresta paralelas não são permitidas
			}
		}
		adj[_vertex].add(_adj);
		numEdges++;
	}

	private void addEdges(File _file){
		try {
		Scanner scanner = new Scanner(_file);
		String line = "";
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(separator);
			if(parts.length > 2) {        // has weight
				addEdge(Integer.parseInt(parts[0]), new Adjacency(Integer.parseInt(parts[1]),Double.parseDouble(parts[2])));
			}
			else if(parts.length == 2){   
				addEdge(Integer.parseInt(parts[0]), new Adjacency(Integer.parseInt(parts[1])));
			}
		}
		scanner.close();
		} catch( FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public int getNumEdges() {
		return numEdges;
	}

	public ArrayList<Adjacency>[] getAdj() {
		return adj;
	}

	public int getNumVertices() {
		return numVertices;
	}
	
	public WeightedGraph getReverse(){
		if(reverse != null) return reverse;
		reverse = new WeightedGraph(this);
		for(int v= 0; v < getNumVertices(); v++)
			for(Adjacency adjacent : adj[v])
				reverse.addEdge(adjacent.getVertice(), new Adjacency(v,adjacent.getWeight()));
		return reverse;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int v = 0; v < getNumVertices(); v++ ){
			sb.append("[" + v + "]: ");
			for(Adjacency  adjacent : adj[v]){
				sb.append(adjacent.getVertice() + "/" + adjacent.getWeight() + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private int countVertices(File _file, String _separator) {
		String line = null;
		try {
		Scanner	scanner = new Scanner(_file);

		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(_separator);
			if(parts[0].length() == 0) continue; // linha em branco
			if(parts.length > 3 ) {
				BicamSystem.printLog("WARNING", "INVALID INPUT VERTICES. DISCARDED." + line);
				continue;
			}
			for(int i = 0; i < parts.length && i < 2; i++) {
				vertices.add(parts[i]);
			}
		}
		scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return vertices.size();
	}
	
	public static void main(String[] args) throws FileNotFoundException {
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicGraph_New");
		File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotas.txt");
		WeightedGraph graph = new WeightedGraph(file,"/");
		System.err.println(graph.toString());
		System.err.println(graph.getReverse().toString());
	}	
}
