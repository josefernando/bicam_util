package br.com.bicam.util.graph.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.graph.Graph;
import br.com.bicam.util.graph.Node;

public class CyclePath {
	private HashSet<String> marked = new HashSet<String>();
	private HashSet<String> visited = new HashSet<String>();
	private List<String> keyProperties;

	private Stack<String> trackCycle = new Stack<String>();
	private ArrayList<Stack<String>> cycleList= new ArrayList<Stack<String>>();
	private ArrayList<Stack<String>> pathList= new ArrayList<Stack<String>>();

	private Map<String, ArrayDeque<Node>> pathGraph = new HashMap<String, ArrayDeque<Node>>();

	private ArrayList<ArrayDeque<Node>> paths = new ArrayList<ArrayDeque<Node>>();

	Graph graph;
	String source;

    Stack<String> cycle = new Stack<String>();
    Stack<String> path = new Stack<String>();
    Stack<String> finalPath = new Stack<String>();


    Stack<String> pathCycle = new Stack<String>();
    LinkedList<String> lkdlst = new LinkedList<String>();

    private String target;
    
    public CyclePath(Graph _graph){
		Iterator<Node> it = _graph.getNodes().values().iterator();
		dfs(_graph, it.next().getId());
    }
	
	public CyclePath(Graph _graph, String _source){
		this.graph = _graph;
		this.source = _source;
		keyProperties = new ArrayList<String>();
		if(_graph.getNode(_source) == null)
			try{
				throw new Exception("Source not in graph: " + source);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}			
		dfs(_graph,_source); // marca o caminho a partir de source
	}

 // traça caminho a partir do node com id = v
    private void dfs(Graph _graph, String _v) {
        Iterator<Node> it = (Iterator<Node>)_graph.getOutEdges(_v);
        marked.add(_v);
        trackCycle.add(_v);
        while (it.hasNext()) { // adjNodes
        	Node adjNode = it.next();
        	if(!isMarked(adjNode.getId())) {
        		dfs(_graph, adjNode.getId());
        	}
        	else if(isInCycle(adjNode.getId())){
        		cycle.add(adjNode.getId()); // starts pathCycle
        		String fim = adjNode.getId();
        		while(!trackCycle.peek().equals(fim)){
        			cycle.add(trackCycle.pop());
        		}
    			cycle.add(trackCycle.peek());
    			Stack<String> c = new Stack<String>();
    			c.addAll(cycle);
    			cycleList.add(c);
    			cycle.clear();
        	}
        }
        trackCycle.remove(_v);
    }
    
    // traça caminho a partir do node com id = v
    public void dfsPath(Graph _graph, String _target) {
        Iterator<Node> incidentEdges = (Iterator<Node>)_graph.getInEdges(_target);
        visited.add(_target);
        path.add(_target);
        while (incidentEdges.hasNext()) { // adjNodes
        	Node adjNode = incidentEdges.next();
        	if(!hasVisited(adjNode.getId())) {
        		dfsPath(_graph, adjNode.getId());
        	}
        	else {
        		continue;
        	}
        }
        if(_target.equals(source)){
			Stack<String> p = new Stack<String>();
			p.addAll(path);
			pathList.add(p);
        }
    	visited.remove(_target);
    	path.remove(_target);
    }    
  
    private boolean isMarked(String v){
    	return marked.contains(v);
    }
    
    private boolean hasVisited(String v){
    	return visited.contains(v);
    }
    
    private boolean isInCycle(String v){
    	return trackCycle.contains(v);
    }
    
    public boolean hasCycle(String v){
    	return !cycleList.isEmpty();
    }
    
    public ArrayList<Stack<String>> getCycles(){
    	return cycleList;
    }
    
    public boolean hasCycle(){
    	System.err.println(cycleList);
    	return cycleList.size() > 0;
    }    
    
    public boolean hasPathTo(String v) {
        return marked.contains(v);
    }
    
    public ArrayList<ArrayDeque<Node>> pathTo(String _target) {
    	return pathTo(_target, true);
    }
    
    public ArrayList<ArrayDeque<Node>> pathTo(String _target, boolean _printPaths) {
    	this.target= _target;
    	
    	// Se não há coaminho de target até Source retorna ArrayVazio.
        if (!hasPathTo(target)) return new ArrayList<ArrayDeque<Node>>();
    	if (!hasPathTo(target)) return null;
        dfsPath(graph, _target);
        Iterator<Stack<String>> it = pathList.iterator();
        while(it.hasNext()){
        	Stack<String> s = it.next();
        	ArrayDeque<Node> lnkNode = new ArrayDeque<Node>();
        	while(!s.empty()){
        		lnkNode.add(graph.getNode(s.pop()));
        	}
        	paths.add(lnkNode);
        }
        
        if(_printPaths) printPaths();
        
        return paths;    	
    }
    
    public void printPaths(){
    	Iterator <ArrayDeque<Node>> it = paths.iterator();
    	while(it.hasNext()){
    		Iterator<Node> it2 = it.next().iterator();
    		while(it2.hasNext()){
    			Node wNode = it2.next();
    			int lineNumber= wNode.getItem() != null ? ((ParserRuleContext)wNode.getItem()).start.getLine()
    					: 0;
    			System.err.print(wNode.getId() + "(" + lineNumber +")"
    					+ (keyProperties.size() > 0 ? "-" + getPropertyValues(wNode, keyProperties) : "")
    					+" >> ");
    		}
        	System.err.println();
    	}
    }
    
    public void printPath(LinkedHashSet<Node> _path) {
		Iterator<Node> it2 = _path.iterator();
		while(it2.hasNext()){
			Node wNode = it2.next();
			System.err.print(wNode.getId() 
					+ (keyProperties.size() > 0 ? "-" + getPropertyValues(wNode, keyProperties) : "")
					+" >> ");
		}
    	System.err.println();
    }
    
    public ArrayList<ArrayDeque<Node>> getPaths(){
    	return paths;
    }
    
    private String getPropertyValues(Node _node, List<String> _k){
    	StringBuffer sb = new StringBuffer();
    	for(int i = 0; i < _k.size(); i++){
    		sb.append(_node.getProperty(_k.get(i)));
    		if(i < _k.size()) sb.append("-");
    	}
    	return sb.toString();
    }
	
	public void addKeyProperty(String _keyProperty){
		keyProperties.add(_keyProperty);
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
        String source = "Sao Paulo";
//        String source = "Menu_Lesson8Menu";
        CyclePath path = new CyclePath(g, source);
        System.err.println(path.hasCycle());
        String destino = "Campos dos Jordao";
//        String destino = "Form_SAdresIn";
    	path.pathTo(destino);
	}
}