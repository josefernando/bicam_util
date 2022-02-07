package br.com.bicam.util.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import br.com.bicam.util.MinHeap;

//BreadthFirstPaths - http://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/BreadthFirstPaths.java.html
public class ShortestPathByWeightDijkstra {
    private static final int INFINITY = Integer.MAX_VALUE;
    private static final double INFINITY_DOUBLE = Double.MAX_VALUE;

    private boolean[] marked;  // marked[v] = is there an s-v path
    private int[] edgeTo;      // edgeTo[v] = previous edge on shortest s-v path
    private int[] distTo;      // distTo[v] = distancia do vertice inical a este vertice
    private boolean[] closedVertex;
    private double[] distToDijkstra;      // distTo[v] = distancia do vertice inical a este vertice

    private int source;
    private double[] weights;
    private WeightedGraph graph;
    private int target;
    
    private Integer[] pathTo;
    
    public ShortestPathByWeightDijkstra(WeightedGraph _graph, int _source) {
        marked = new boolean[_graph.getNumVertices()];
        distTo = new int[_graph.getNumVertices()];
        closedVertex = new boolean[_graph.getNumVertices()];
        distToDijkstra = new double[_graph.getNumVertices()];

        edgeTo = new int[_graph.getNumVertices()];
        this.source = _source;
        this.graph = _graph;
        bfs(_graph, _source);
    }
    
    // breadth-first search from a single source
    private void bfs(WeightedGraph _graph, int _source) {
        Deque<Integer> q = new ArrayDeque<Integer>();
        
        for (int v = 0; v < _graph.getNumVertices(); v++)
            distTo[v] = INFINITY;
        
        distTo[_source] = 0;
        marked[_source] = true;
        q.add(_source);

        while (!q.isEmpty()) {
            int v = q.pop();
            for (Adjacency w : _graph.getAdj()[v]) {
                if (!marked[w.getVertice()]) {
                    edgeTo[w.getVertice()] = v;
                    distTo[w.getVertice()] = distTo[v] + 1;
                    marked[w.getVertice()] = true;
                    q.add(w.getVertice());
                }
            }
        }
    }

    public boolean hasPathTo(int v) {
        return marked[v];
    }

    public int distTo(int v) {
        return distTo[v];
    }

/*    public Integer[] pathTo(int v) {
    	target = v;
        if (!hasPathTo(v)) return null;
        Stack<Integer> path = new Stack<Integer>();
        int x;
        
        for (x = v; distTo[x] != 0; x = edgeTo[x])
            path.push(x);
        
        path.push(x);
        
        //Prepara para transformar retorno de Stack em Integer[]
        Deque<Integer> r = new ArrayDeque<Integer>();
        for(Integer i : path){
        	r.addFirst(i);
        }
        return r.toArray( new Integer[r.size()]);
    }*/
    
//    public Integer[] pathToDijkstra(int v) {
  public Integer[] pathTo(int v) {

        if (!hasPathTo(v)) return null;
    	target = v;
        
        //inicialização
        for(int i = 0; i < graph.getNumVertices(); i++) {
        	distToDijkstra[i] = INFINITY_DOUBLE;
        	edgeTo[i] = -1;
        	closedVertex[i] = false;
        }
        distToDijkstra[source] = 0; // distância  do ponto inicial a ele mesmo é 0

        ArrayList<Double> initMinPQ = new ArrayList(); //(Arrays.asList(distToDijkstra)); //new ArrayList<>(Arrays.asList(array))
        for(int ix = 0; ix < distToDijkstra.length; ix++) {
        	initMinPQ.add(distToDijkstra[ix]);
        }
        
        MinHeap minPQ = new MinHeap(initMinPQ);
        int ixGraph = -1; //source;
    	Double ixDijk = -1.0; //minPQ.extractMin();
//    	int ixGraph = getGraphIndex(ixDijk);
        while(!minPQ.isEmpty()) {
        	ixDijk = minPQ.extractMin();
        	ixGraph = getGraphIndex(ixDijk);
        	for(int ixx = 0; ixx < graph.getAdj()[ixGraph].size(); ixx++) {
        		double finalWeight = distToDijkstra[graph.getAdj()[ixGraph].get(ixx).getVertice()];
        		double pathWeight = graph.getAdj()[ixGraph].get(ixx).weight + distToDijkstra[ixGraph];
        		if(pathWeight < finalWeight) {
        				distToDijkstra[graph.getAdj()[ixGraph].get(ixx).getVertice()] = pathWeight;
        				edgeTo[graph.getAdj()[ixGraph].get(ixx).getVertice()] = ixGraph;
        		}
        		
        		if(ixGraph == target) {
        			for(int i = 0; i < closedVertex.length; i++) closedVertex[i] = true;
        			break;
        		}
        	}
            closedVertex[ixGraph] = true;
    		//============ reconstroi a priority queue 
    		initMinPQ.clear();
            for(int ix = 0; ix < distToDijkstra.length; ix++) {
            	if(closedVertex[ix]) continue;
            	initMinPQ.add(distToDijkstra[ix]);
            }
            minPQ = new MinHeap(initMinPQ);       		
    		//=============
        }
//        ArrayList pathh = new ArrayList();
        Deque<Integer> pathh = new ArrayDeque<>();
        
        int prev = target;
        pathh.add(target);
        while(edgeTo[prev] != -1) {
        	pathh.addFirst(edgeTo[prev]);
        	prev = edgeTo[prev];
        }
        
        Integer[] ar = new Integer[pathh.size()];
        for(int ix=0; ix <ar.length; ix++) {
        	ar[ix] = pathh.pop();
        }
        pathTo = ar;
        return ar; //new Integer[pathh.size()](pathh.to);
    }
        
    private int getGraphIndex(double _currentDist) {
    	for(int i=0; i < distToDijkstra.length; i++) {
    		if(distToDijkstra[i] == _currentDist) {
    			if(!closedVertex[i]) return i;
    		}
    	}
    	return -1; // Nunca deve acontecer. Se acontecer é erro
    }
    
    public String toString(){
    	if(pathTo == null) return null;
		StringBuffer p = new StringBuffer();
		for(Integer item : pathTo){
			if(p.length() > 0) p.append(" -> ");
			p.append(item);
		}
		return p.toString();
    }
    
	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_NewGraph");
		WeightedGraph graph = new WeightedGraph(file, " ");
		System.err.println(graph.toString());
		System.err.println(graph.getReverse().toString());
		
		ShortestPathByWeightDijkstra paths = new ShortestPathByWeightDijkstra(graph, 0);
		Integer[] ppath = paths.pathTo(3);
//		Integer[] ppath = paths.pathToDijkstra(3);

/*		StringBuffer p = new StringBuffer();
		for(Integer item : ppath){
			if(p.length() > 0) p.append(" -> ");
			p.append(item);
		}
		System.err.println(p);*/
		System.err.println(paths.toString());
	}    
}