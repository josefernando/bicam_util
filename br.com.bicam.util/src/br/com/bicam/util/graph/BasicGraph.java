package br.com.bicam.util.graph;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.com.bicam.model.util.ArrayOfListAdapter;

//@XmlAccessorType (XmlAccessType.PROPERTY) 
//@XmlRootElement
public class BasicGraph {
	
//	@XmlElement
//	private final int numVertices;
	private int numVertices;

	
//	@XmlElement
	private int numEdges;
	
//	@XmlJavaTypeAdapter(ArrayOfListAdapter.class)
	private ArrayList<Integer>[] adj;
	
//	@XmlElement
	BasicGraph reverse;
	
	public BasicGraph(){}  // for Jaxb
	
	public BasicGraph(int _numVertices){
		this.numVertices = _numVertices;
		this.numEdges = 0;
		
		adj = new ArrayList[numVertices];
		
		for (int v = 0 ; v < numVertices; v++){
			adj[v] = new ArrayList<Integer>();
		}
	}
	
	public void addEdge(int vertice, int adjacent){
		if(adj[vertice].contains(adjacent)) return; // Elimina edge paralelo
		adj[vertice].add(adjacent);
		numEdges++;
	}

	public int getNumEdges() {
		return numEdges;
	}

	public ArrayList<Integer>[] getAdj() {
		return adj;
	}

	public int getNumVertices() {
		return numVertices;
	}
	
	public BasicGraph getReverse(){
		if(reverse != null) return reverse;
		reverse = new BasicGraph(getNumVertices());
		for(int v= 0; v < getNumVertices(); v++)
			for(int adjacent : adj[v])
				reverse.addEdge(adjacent, v);
		return reverse;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int v = 0; v < getNumVertices(); v++ ){
			sb.append("[" + v + "]: ");
			for(int i : adj[v]){
				sb.append(i + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		BasicGraph graph = new BasicGraph(9);
		File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicGraph");
		Scanner scanner = new Scanner(file);
		String line = "";
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(" ");
			graph.addEdge(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
		System.err.println(graph.toString());
		System.err.println(graph.getReverse().toString());
	}
}
