package br.com.bicam.util.graph;

import java.util.ArrayList;

public class PathOld {
	Integer[] path;
	SymbolGraph graph;
	
	ArrayList<Integer[]> lastMilePaths;
	
	public PathOld(Integer[] _path, SymbolGraph _graph) {
		this.path = _path;
		this.graph = _graph;
	}
	
	public Integer[] getPath(){
		return path;
	}
	
/*	public void setGraph(SymbolGraph _graph){
		this.graph = _graph;
	}*/
	
	public boolean setLastMilePaths(){
		 if(graph == null){
			 return false;
		 }
		 Integer start = path[path.length-2];
		 Integer end = path[path.length-1];
		 AllPaths allpaths = new AllPaths(graph.getbasicGraph(), start);
		 lastMilePaths = allpaths.pathTo(end);
		 return true;
	}
	
	public ArrayList<Integer[]> getLastMilePaths(){
		if(lastMilePaths == null) setLastMilePaths();
		return lastMilePaths;
	}
}