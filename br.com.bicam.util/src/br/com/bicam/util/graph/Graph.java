package br.com.bicam.util.graph;

/*
 * File format: string string
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Graph<T> {
    // assume Unicode UTF-8 encoding
    private static final String CHARSET_NAME = "UTF-8";
    // assume language = English, country = US for consistency with System.out.
    private static final Locale LOCALE = Locale.US;
    private Scanner scanner;
//    private boolean isReverse = false;
    
// number of vertex
    private int numVertices;
// number of edges
	private int numEdges;  
// adjacency list
//	private Integer[] adj;
	
	Graph reverseGraph;
	
	LinkedHashMap<String, Node<T>> nodes;  // vertices
    Map<T, Node<T>> nodesByGeneric;  
    Map<String,Integer> nameToIndex;
    Map<Integer,String> indexToName;
    ArrayList<Integer>[] adjOut;
    ArrayList<Integer>[] adjIn;

    public Graph(File file, String _separator) {
        nodes = new LinkedHashMap<String, Node<T>>();
        nodesByGeneric = new IdentityHashMap<T, Node<T>>();  
        nameToIndex = new HashMap<>();
        indexToName = new HashMap<>();

        
	    scanner = new Scanner(new BufferedInputStream(System.in), CHARSET_NAME);
	    scanner.useLocale(LOCALE);
	    try {
	        scanner = new Scanner(file, CHARSET_NAME);
	        scanner.useLocale(LOCALE);
	    }
	    catch (IOException ioe) {
	        System.err.println("Could not open " + file);
	    }
	    
	    while(hasNextLine()){
	    	String line = readLine();
	    	String[] lineParts = line.split(_separator);
	    	if(lineParts.length == 0){
	    		try {
					throw new Exception("Invalid input. It's discarded.");
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    	Node<T> tail = addNode(lineParts[0], null);
	    	if(lineParts.length > 1){
	    		Node<T> head = addNode(lineParts[1], null);
	    		addEdge(tail, head);
	    	}
	    }
    }
    
    public Graph(){
        nodes = new LinkedHashMap<String, Node<T>>();
        nodesByGeneric = new IdentityHashMap<T, Node<T>>();
        nameToIndex = new HashMap<>();
        indexToName = new HashMap<>();
    }
    
    public Node<T> addNode(String _id, T _item){
    	Node<T> n = createNode(_id, _item);
    	return addNode(n);
    }

	public Node<T> addNode(Node<T> _node){
    	Node<T> n = getNode(_node.getId());
    	if(n == null) {
    		nodes.put(_node.getId(), _node);
    		n = getNode(_node.getId());
        	nodesByGeneric.put((T)n.getGeneric(), _node);
    	}
    	return n;
    }    
    
    public void addEdge(Node<T> _vertice, Node<T> _adj){
    	Node<T> node1 = getNode(_vertice.getId());
    	node1.addOutEdge(_adj);  // Adiciona edge de saída no vértice
    	node1 = getNode(_adj.getId());
    	if(node1 == null){
    		System.err.println();
    	}
    	node1.addInEdge(_vertice); // Adiciona vértice de entrada no vértice
    	return;
    }
    
    public int getNumVertex(){
//    	return numVertex;
    	return getNumNodes();
    }
    
    public int getNumEdges(){
    	return numEdges;
    }

    public int getNumNodes(){
    	return nodes.values().size();
    }
    
    public Map<String, Node<T>> getNodes(){
    	return nodes;
    }
    
    public Map<String, Node<T>> getGraph(){ 
    	return nodes;
    }    
    
    public Node<T> getNode(String _s){
    	return nodes.get(_s);
    }
    
    
    public Node<T> getNodeByGeneric(T _item){
    	return nodesByGeneric.get(_item);
    }
    
    public Iterator<Node<T>> getOutEdges(String _nodeId){
    	return getNode(_nodeId).getAdjacentList().iterator();
    }
    
    public Iterator<Node<T>> getInEdges(String _nodeId){
    	return getNode(_nodeId).getInEdge().iterator();
    }    
    
    private Node<T> createNode(String _s, T _item){
//    	Node<T> n = getNode(_s);
//    	return n != null ? nodes.get(_s) : new Node<T>(_s, _item);
    	Node<T> n = getNode(_s);
    	return n != null ? n : new Node<T>(_s, _item);    	
    }
    
    private void setNodes(LinkedHashMap<String, Node<T>> _nodes){
    	this.nodes = _nodes;
    }

    private String readLine() {
        String line;
        try {
            line = scanner.nextLine();
        }
        catch (NoSuchElementException e) {
            line = null;
        }
        return line;
    }
    
    private boolean hasNextLine(){
    	return scanner.hasNextLine();
    }
    
    public Graph<T> reverse(){
    	if(reverseGraph != null) return reverseGraph;
    	Graph<T> graphReverse = new Graph<T>();
    	LinkedHashMap<String, Node<T>> wNodes;
        wNodes = new LinkedHashMap<String, Node<T>>();
        graphReverse.setNodes(wNodes);
        
        List<Node<T>> nodeValues = new ArrayList<Node<T>>();
        nodeValues.addAll(nodes.values());
        if(nodeValues.size() == 0) return null;
        ListIterator<Node<T>> litr = nodeValues.listIterator(nodeValues.size());
        while(litr.hasPrevious()){
    		Node<T> wnode = litr.previous().getCopy(false);
    		graphReverse.addNode(wnode);
        }
        reverseGraph = graphReverse;
      return reverseGraph;  
    }
    
    public String toString(){
    	StringBuffer s = new StringBuffer();
    	s.append("");
    	for(Node<T> node : nodes.values()){
    		s.append("\n" + node.getId() + ": ");
    		Iterator<Node<T>> it = node.getAdjacentList().iterator();
    		String comma = "";
    		while(it.hasNext()){
    			s.append(comma + it.next().getId());
    			comma = ", ";
    		}
    		comma = "";
    	}     	
    	return s.toString();
    }
    
/*
 * pega graph definido em nodes e gera graph definido com array    
 */
    public ArrayList[] ArrayGraph(){
    	loadArray();

//    	StringBuffer s = new StringBuffer();
//    	s.append("");  
    	
		for(String name : nodes.keySet()){
			int vertex = nameToIndex.get(name);
			
    		Iterator<Node<T>> it = nodes.get(name).getAdjacentList().iterator();
			System.err.println(name + ": ");
    		while(it.hasNext()){
    			Node<T> n = it.next();
    			adjOut[vertex].add(nameToIndex.get(n.getId()));
//    			System.err.println(" > " + indexToName.get(nameToIndex.get(n.getId())));
//   			System.err.println(" >> Name: " + indexToName.get(vertex)+ " - " + adj[vertex]);
    		}
//    		s.append("\n");
		}
		return adjOut;
    }
	
	private void loadArray() {
		int vertex = -1;
		adjOut = new ArrayList[nodes.size()];
		adjIn = new ArrayList[nodes.size()];

		for(String name : nodes.keySet()){
			vertex++;
			nameToIndex.put(name,vertex);
			indexToName.put(vertex,name);
			adjOut[vertex] = new ArrayList<Integer>();
			adjIn[vertex] = new ArrayList<Integer>();
			
//    		Iterator<Node<T>> it = nodes.get(name).getAdjacentList().iterator();

//			System.err.format("Index: %d - text: %s%n" , vertex, name);
		}
	}
    
    public String toString(String _desc, String... _line){
    	StringBuffer s = new StringBuffer();
    	s.append("");
    	for(Node<T> node : nodes.values()){
    		s.append("\n" + (_line.length == 0 ? node.getId() : (String)node.getProperty(_line[0])) 
    		+ "-"  + (String)node.getProperty(_desc) + "-"+ node.getId() + ": ");
    		Iterator<Node<T>> it = node.getAdjacentList().iterator();
    		String comma = "";
    		while(it.hasNext()){
    			Node<T> n = it.next();
    			s.append(comma + (_line.length == 0 ? n.getId() : (String)n.getProperty(_line[0]))
    			+ "-"  + (String)n.getProperty(_desc) + "-"+ n.getId());
    			comma = ", ";
    		}
    		comma = "";
    	}     	
    	return s.toString();
    }
    
/*    public String toStringArray(){
    	StringBuffer s = new StringBuffer();
    	s.append("");
    	
    	for(int i = 0; i < adj.length; i++){
    		s.append(indexToName.get(i) + ": ");
    		for(int ii = 0; ii < adj[i].size(); ii++){
    			s.append(indexToName.get(ii) + " ") ;
    		}
    		s.append("\n");
    	}
    	return s.toString();
    } */   
    
	public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) throw new IllegalArgumentException("Entre com o nome do arquivo");
        File file = new File(args[0]);
        String separator = " ";
        if(args.length > 1) separator = args[1];
        Graph g = new Graph(file, separator);
        g.ArrayGraph();
        System.err.println(g.toString());
//        System.err.println("\n TO STRING ARRAY...");
//        System.err.println(g.toStringArray());
	}

	public Integer getNameToIndex(String _name) {
		return nameToIndex.get(_name);
	}

	public String getIndexToName(Integer _index) {
		return indexToName.get(_index);
	}
}
