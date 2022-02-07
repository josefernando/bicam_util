package br.com.bicam.util.graph;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class NewGraph {
	private final int numVertices;
	private int numEdges;
	private ArrayList<Integer>[] adj;
	
	NewGraph reverse;
	
	public NewGraph(int _numVertices){
		this.numVertices = _numVertices;
		this.numEdges = 0;
		
		adj = new ArrayList[numVertices];
		
		for (int v = 0 ; v < numVertices; v++){
			adj[v] = new ArrayList<Integer>();
		}
	}
	
	public void addEdge(int vertice, int adjacent){
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
	
	public NewGraph getReverse(){
		if(reverse != null) return reverse;
		reverse = new NewGraph(getNumVertices());
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
		NewGraph graph = new NewGraph(6);
		File file = new File("C:\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_NewGraph");
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
