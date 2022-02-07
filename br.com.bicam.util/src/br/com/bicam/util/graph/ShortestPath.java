package br.com.bicam.util.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

//BreadthFirstPaths - http://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/BreadthFirstPaths.java.html
public class ShortestPath {
    private static final int INFINITY = Integer.MAX_VALUE;
    private boolean[] marked;  // marked[v] = is there an s-v path
    private int[] edgeTo;      // edgeTo[v] = previous edge on shortest s-v path
    private int[] distTo;      // distTo[v] = number of edges shortest s-v path

    public ShortestPath(BasicGraph _graph, int _source) {
        marked = new boolean[_graph.getNumVertices()];
        distTo = new int[_graph.getNumVertices()];
        edgeTo = new int[_graph.getNumVertices()];
        bfs(_graph, _source);
    }
    
    // breadth-first search from a single source
    private void bfs(BasicGraph _graph, int _source) {
        Deque<Integer> q = new ArrayDeque<Integer>();
        
        for (int v = 0; v < _graph.getNumVertices(); v++)
            distTo[v] = INFINITY;
        
        distTo[_source] = 0;
        marked[_source] = true;
        q.add(_source);

        while (!q.isEmpty()) {
            int v = q.pop();
            for (int w : _graph.getAdj()[v]) {
                if (!marked[w]) {
                    edgeTo[w] = v;
                    distTo[w] = distTo[v] + 1;
                    marked[w] = true;
                    q.add(w);
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

    public Integer[] pathTo(int v) {
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
    }
}