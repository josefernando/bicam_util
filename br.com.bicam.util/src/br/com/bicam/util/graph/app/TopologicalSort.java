package br.com.bicam.util.graph.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import br.com.bicam.util.graph.Graph;
import br.com.bicam.util.graph.Node;

public class TopologicalSort {
	private HashSet<String> visited = new HashSet<String>();
    private LinkedHashSet<String> precedence = new LinkedHashSet<String>();
    private Map<Integer,LinkedHashSet<String>> precedenceList = new HashMap<Integer,LinkedHashSet<String>>();
    private Integer count= 0;

	Graph graph;
	
	public TopologicalSort(Graph _graph){
		this.graph = _graph;
		CyclePath cycle = new CyclePath(_graph);
		if (cycle.hasCycle()){
			System.err.println("Graph has cycle. Precedence not processed.");
			System.exit(1);
		}
		
		Iterator<Node> it = _graph.getNodes().values().iterator();
		while(it.hasNext()){
			Node node = it.next();
			count++;
			if(!hasVisited(node.getId())){
				dfs(graph, node.getId());
			}
//			precedenceList.put(count, (LinkedHashSet<String>) precedence.clone());
//			precedenceList.clear();
		}
//		precedenceList.putAll(1,arg0);
		System.err.println(precedence);
	}

    // traça caminho a partir do node com id = v
    private void dfs(Graph _graph, String _v) {
        Iterator<Node> it = (Iterator<Node>)_graph.getOutEdges(_v);
        while (it.hasNext()) { // adjNodes
        	Node adjNode = it.next();
        	if(!hasVisited(adjNode.getId())) {
        		dfs(_graph, adjNode.getId());
        	}
        }
        precedence.add(_v);
        visited.add(_v);
    }
    
    private boolean hasVisited(String v){
    	return visited.contains(v);
    }
	
	public String toString(){
		return " ";
	}    
    
	public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) throw new IllegalArgumentException("Entre com o nome do arquivo");
        File file = new File(args[0]);
        String separator = "/";
        Graph g = new Graph(file, separator);
        System.err.println(g.toString());
        TopologicalSort sort = new TopologicalSort(g);
	}
}