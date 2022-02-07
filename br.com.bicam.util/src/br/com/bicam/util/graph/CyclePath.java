package br.com.bicam.util.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class CyclePath {
	private boolean[] marked; 
	private HashSet<Integer> visited;
//	private HashSet<Integer> cycle;
	private ArrayDeque<Integer> onePath;
//	private ArrayList<ArrayDeque<Integer>> paths;
	private ArrayList<Integer[]> paths;


	private int[] edgeTo;
	
	BasicGraph graph;
	
	private int source;
	private int target;
	
	public CyclePath(BasicGraph _graph, int _source){
		this.graph = _graph;
		this.source = _source;
		onePath = new ArrayDeque<>(); 
		paths = new ArrayList<>();
		
		visited = new HashSet<>();

		
		marked = new boolean[_graph.getNumVertices()];
		edgeTo = new int[_graph.getNumVertices()];
		
		dfs(_graph,_source); 
	}
	
	private void dfs(BasicGraph _graph, int v){
		marked[v] = true;
		for(int adjacent : _graph.getAdj()[v]){
			if(!marked[adjacent]){
				dfs(_graph, adjacent);
			}
		}
	}
	
	public boolean hasPathTo(int v){
		return marked[v];
	}
	
//	public ArrayList<ArrayDeque<Integer>> pathTo(int _v){
	public ArrayList<Integer[]> pathTo(int _v){
//		if(!hasPathTo(_v)) return new ArrayList<ArrayDeque<Integer>>();
		if(!hasPathTo(_v)) return new ArrayList<>();

		paths.clear();
        target = _v;
		dfsPath(graph.getReverse(), _v); // Em graph.getReverse(), target se torna source
		return paths;
	}	

    private void dfsPath(BasicGraph _graph, int _v) {
    	if(visited.contains(_v)){
    		return;
    	}
    	visited.add(_v);
    	onePath.push(_v);
//    	cycle.add(_v);
    	
    	for(int adjacent : _graph.getAdj()[_v]){
    		if(adjacent == source){
    			onePath.push(adjacent);
//    	    	paths.add(onePath);
				paths.add(onePath.toArray( new Integer[onePath.size()]));
				onePath.pop();
    			break;
    		}
    		dfsPath(_graph, adjacent);
    	}
    	visited.remove(_v);
    	onePath.pop();
//    	cycle.remove(_v);
    }
    
/*    private void incluiPath(int _adjacent){
    	for(int i = _adjacent; edgeTo[i] != target;  i = edgeTo[i]){
    		onePath.push(i);
    	}
    	onePath.push(target);
    	paths.add(onePath);
    }*/

//    public ArrayList<ArrayDeque<Integer>> getPaths(){
        public ArrayList<Integer[]> getPaths(){
    	return paths;
    }
    
	public static void main(String[] args) throws FileNotFoundException {
		BasicGraph graph = new BasicGraph(5);
		File file = new File("C:\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_NewGraph");
		Scanner scanner = new Scanner(file);
		String line = "";
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(" ");
			graph.addEdge(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
		scanner.close();
		System.err.println(graph.toString());
		System.err.println(graph.getReverse().toString());
		
		CyclePath paths = new CyclePath(graph, 0);
//		System.err.println(paths.hasPathTo(3));
		
		ArrayList<Integer[]> ppaths = paths.pathTo(3);
		System.err.println(ppaths.size() + " paths");
		for(Integer[] ppath : ppaths){
			StringBuffer p = new StringBuffer();
			for(Integer item : ppath){
				if(p.length() > 0) p.append(" -> ");
				p.append(item);
			}
			System.err.println(p);
		}
	}	    
    
}