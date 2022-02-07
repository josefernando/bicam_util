package br.com.bicam.util.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

public class AllPathsByEdges {
	private boolean[] marked; 
	private HashSet<Integer> visited;
	private ArrayDeque<Integer> onePath;
	private ArrayList<Integer[]> paths;
	private ArrayList<PathOld> pathsx;
	private int maxPaths;
	
	WeightedGraph graph;
	
	SymbolGraph symGraph;
	
	private int source;
	
	public AllPathsByEdges(WeightedGraph _graph, int _source){
		this.graph = _graph;
		this.source = _source;
		onePath = new ArrayDeque<>(); 
		paths = new ArrayList<>();
		pathsx = new ArrayList<>();
		
		visited = new HashSet<>();

		
		marked = new boolean[_graph.getNumVertices()];
		
		dfs(_graph,_source); 
	}
	
	private void dfs(WeightedGraph _graph, int v){
		marked[v] = true;
		for(Adjacency adjacent : _graph.getAdj()[v]){
			if(!marked[adjacent.getVertice()]){
				dfs(_graph, adjacent.getVertice());
			}
		}
	}
	
	public boolean hasPathTo(int v){
		return marked[v];
	}
	
	public ArrayList<Integer[]> pathTo(int _v, int..._maxPaths){
		if(_maxPaths.length > 0) maxPaths = _maxPaths[0]; // interrrompe a pesquisa paths ao atingir maxPaths
		else maxPaths = 10;
		if(!hasPathTo(_v)) return new ArrayList<>(); //se _v não é atigido a partir de source então retorna  zero paths

		paths.clear();
		dfsPath(graph.getReverse(), _v); // Em graph.getReverse(), target se torna source
		return paths;
	}	

    private void dfsPath(WeightedGraph _graph, int _v) {
    	if(visited.contains(_v) || paths.size() >= maxPaths){
    		return;
    	}
    	visited.add(_v);
    	onePath.push(_v);
    	
    	for(Adjacency adjacent : _graph.getAdj()[_v]){
    		if(adjacent.getVertice() == source){
    			onePath.push(adjacent.getVertice());
				paths.add(onePath.toArray( new Integer[onePath.size()]));
				
				PathOld pathx = new PathOld(onePath.toArray( new Integer[onePath.size()]), symGraph);
				pathx.setLastMilePaths();
				pathsx.add(pathx);
				
				onePath.pop();
				if(paths.size() < maxPaths) continue;
				else {
				    System.err.format("*** WARNNG SEARCH PATHS ATINGIU MAX PATHS %d%n", maxPaths);
				    break;
//					System.exit(0); // atingiu o numero máximo de paths solicitados ou default de 10
				}
				             // para evitar erro "...java.lang.OutOfMemoryError: GC overhead limit exceeded"
    			
    		}
    		else dfsPath(_graph, adjacent.getVertice());
    	}
    	visited.remove(_v);
    	onePath.pop();
    }
    
    public ArrayList<Integer[]> getPaths(){
    	return paths;
    }
    
    public void  setSymGraph(SymbolGraph _symGraph){
    	this.symGraph =_symGraph;
    }
    
    public SymbolGraph  getSymGraph(){
    	return symGraph;
    }    
    
	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_NewGraph");
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotasForm.txt");

		WeightedGraph graph = new WeightedGraph(file, " ");
		System.err.println(graph.toString());
		System.err.println(graph.getReverse().toString());
		
		AllPathsByEdges paths = new AllPathsByEdges(graph, 0);
		
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