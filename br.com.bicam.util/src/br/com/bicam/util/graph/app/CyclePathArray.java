package br.com.bicam.util.graph.app;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

import br.com.bicam.util.graph.BasicGraph;

public class CyclePathArray {
	private boolean[] marked; 
	private HashSet<Integer> visited;
	private HashSet<Integer> cycle;
	private ArrayDeque<Integer> onePath;
	private ArrayList<ArrayDeque<Integer>> paths;

	private int[] edgeTo;
	
	BasicGraph graph;
	
	private int source;
    private int target;
	
	public CyclePathArray(BasicGraph _graph, int _source){
		this.graph = _graph;
		this.source = _source;
		onePath = new ArrayDeque<>(); 
		paths = new ArrayList<>();
		
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
	
	public ArrayList<ArrayDeque<Integer>> pathTo(int _v){
		if(!hasPathTo(_v)) return new ArrayList<ArrayDeque<Integer>>();
		paths.clear();
		dfsPath(graph.getReverse(), _v); // Em graph.getReverse(), target se torna source
		return paths;
	}	

    private void dfsPath(BasicGraph _graph, int _v) {
    	if(visited.contains(_v)){
//    		registraCycle();
    		return;
    	}
    	visited.add(_v);
    	cycle.add(_v);
    	
    	for(int adjacent = 0; adjacent < _graph.getAdj().length; adjacent++){
    		edgeTo[adjacent] = _v;
    		if(adjacent == source){
    			incluiPath(adjacent);
    			return;
    		}
    		dfsPath(_graph, adjacent);
    	}
    }
    
    private void incluiPath(int _adjacent){
    	for(int i = _adjacent; edgeTo[_adjacent] != source;  i = edgeTo[_adjacent]){
    		onePath.push(i);
    	}
    	onePath.push(source);
    	paths.add(onePath);
    }

    public ArrayList<ArrayDeque<Integer>> getPaths(){
    	return paths;
    }
}