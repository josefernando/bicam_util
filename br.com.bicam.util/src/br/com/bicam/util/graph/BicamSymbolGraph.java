package br.com.bicam.util.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import br.com.bicam.util.PropertyList;

public class BicamSymbolGraph {
	PropertyList properties;

	Set<String> vertices; // tem que ser set, porque não pode ter duplicações

	String nodeSeparator;
	
	File fileIn;
	StringBuffer out;
	
	boolean verbose;
	
	static HashMap<String, Integer> vertexIndex;
	
	static HashMap<String, String> contextToSubContext;
	
	static HashMap<String, BicamNode> NodeIndex;
	
	static BicamNode[] nodes;
	
	String[] vertexName;
	static String[] nodeId;
	
	WeightedGraph graph;
	WeightedGraph graph1; // com Bicam
	Scanner scanner;
	
	public BicamSymbolGraph(){} // Jaxb be need no parameter constructor
	
	public BicamSymbolGraph(File _fileIn, boolean _verbose, String ..._nodeSeparator) throws FileNotFoundException{

		properties = new PropertyList();
		
		fileIn = _fileIn;
		
		this.verbose = _verbose;
	
		if(_nodeSeparator.length > 0) this.nodeSeparator = _nodeSeparator[0];
		else this.nodeSeparator = " ";

		vertices = new LinkedHashSet<>();   // HashSet porque não pode ser repetido
									        // a ordenação (LinkedHashSet) apenas ajuda em debug porque 
									        // faz com que a ordem de entrada do vertice de filein
									        // tenha a mesma ordem do conjunto "vertices

		vertexIndex = new LinkedHashMap<>(); 
		NodeIndex   = new LinkedHashMap<String, BicamNode>();
		createGraph();
		verbose();
	}

	private void createGraph() throws FileNotFoundException{
		mapVertices();
		graph = new WeightedGraph(vertices.size());
		
		graph1 = new WeightedGraph(vertices.size());
		
		scanner = new Scanner(fileIn);
		String line = "";
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(nodeSeparator);
			if(parts.length > 2) {        // has weight
				graph.addEdge(vertexIndex(parts[0]), new Adjacency(vertexIndex(parts[1]) ,Double.parseDouble(parts[2])));
				
				BicamAdjacency adj = new BicamAdjacency(NodeIndex.get(parts[1]));
				adj.setWeight(Double.parseDouble(parts[2]));
				NodeIndex.get(parts[0]).addAdjacencyOut(adj);
				graph1.addEdge(NodeIndex.get(parts[0]).getVertex(), new Adjacency(adj.getNode().getVertex() ,adj.getWeight()));
			}
			else if(parts.length == 2){   
				graph.addEdge(vertexIndex(parts[0]), new Adjacency(vertexIndex(parts[1])));

				BicamAdjacency adj = new BicamAdjacency(NodeIndex.get(parts[1]));
				NodeIndex.get(parts[0]).addAdjacencyOut(adj);
				graph1.addEdge(NodeIndex.get(parts[0]).getVertex(), new Adjacency(adj.getNode().getVertex()));
			}
		}
		scanner.close();
	}
	
	private void mapVertices() throws FileNotFoundException {
        loadVertices(fileIn); // carrega os vertices em "vertices" set, não pode ser duplicado por isso classe "Set"
		List<String> list = new ArrayList<String>(vertices);

		vertexName = new String[vertices.size()];
		nodeId = new String[vertices.size()];
		nodes      = new BicamNode[vertices.size()];
		for(int i = 0; i < vertices.size(); i++){
			vertexName[i] = list.get(i);
			vertexIndex.put(list.get(i), i);
			nodeId[i] = list.get(i);
			nodes[i] = new BicamNode(nodeId[i],i);
			NodeIndex.put(nodes[i].getId(),  nodes[i]);
		}
		
		if(verbose){
			System.err.println("vertexName:");
			for(int i = 0; i < vertexName.length; i++){
				System.err.format("[%d]: %s%n",i,vertexName[i]);
			}
			
			System.err.println("Node:");
			for(int i = 0; i < nodes.length; i++){
				System.err.format("[%d]: %s%n",i,nodes[i]);
			}
			
			System.err.println("\nvertexIndex:");
			for(String context : vertexIndex.keySet()){
				System.err.format("[%s]: %d%n",context,vertexIndex.get(context));
			}
			
			System.err.println("\nNodeIndex:");
			for(String nodeId : NodeIndex.keySet()){
				System.err.format("[%s]: %d%n",nodeId,NodeIndex.get(nodeId).getVertex());
			}			
		}		
	}
	
	public void verbose() {
			System.err.println("vertexName:");
			for(int i = 0; i < vertexName.length; i++){
				System.err.format("[%d]: %s%n",i,vertexName[i]);
			}
			
			System.err.println("Node:");
			for(int i = 0; i < nodes.length; i++){
				System.err.format("[%d]: %s%n",i,nodes[i]);
			}
			
			System.err.println("\nvertexIndex:");
			for(String context : vertexIndex.keySet()){
				System.err.format("[%s]: %d%n",context,vertexIndex.get(context));
			}
			
			System.err.println("\nNodeIndex:");
			for(String nodeId : NodeIndex.keySet()){
				System.err.format("[%s]: %d%n",nodeId,NodeIndex.get(nodeId).getVertex());
			}			
	}
	
	public WeightedGraph getGraph(){
//		return graph;
		return graph;
	}

	public int getNumEdges() {
		return graph.getNumEdges();
	}

	public int getNumVertices() {
		return graph.getNumVertices();
	}
	
	public Integer vertexIndex(String _key){
		if(vertexIndex.get(_key) == null){
			System.err.println("\n" +"*** DEBUG: context-> " + _key);
			System.err.println(toString() + "\n");
		}
		return vertexIndex.get(_key);
	}
	
	public String vertexName(int _index){
		return vertexName[_index];
	}
	
	/*
	 * descobre todos os vertices e os coloca em um conjunto sem redundância (Set)
	 */
	private void loadVertices(File _file) throws FileNotFoundException {
		String line = "";
		scanner = new Scanner(fileIn);
		while(scanner.hasNextLine()){
			line = scanner.nextLine(); // a b p, onde "a" = começo da aresta, "b" = fim da aresta  e "p" = peso da aresta
			String[] parts = line.split(nodeSeparator);
			for(int i = 0; i < 2; i++){
				vertices.add(parts[i]); // quando acidionados vertices iguais não duplica por "vertices" é definido como "Set"
			}
		}
	}
	
	public void getAllPath(String _source, String _target, Integer ..._maxPaths){
		StringBuffer sb = new StringBuffer();
		AllPathsByEdges allpaths = new AllPathsByEdges(getGraph(), vertexIndex(_source));
		ArrayList<Integer[]> all;
		if(_maxPaths.length > 0 )
			all = allpaths.pathTo(vertexIndex(_target), _maxPaths[0]);
		else
			all = allpaths.pathTo(vertexIndex(_target), 10);
		
		int i = 0;
		sb.setLength(0);
		for(Integer[] path : all){
			System.err.format("\nPath %d ", i++);
			for (Integer v : path){
				if(sb.length() > 0 ) sb.append(" -> ");
				sb.append(vertexName(v));
			}
			System.err.println(sb.toString());
			sb.setLength(0);
		}		
	}
	
	public String toString() {
		return toString(getGraph());
	}

	public String toString(WeightedGraph _graph) {
		StringBuffer sb = new StringBuffer();
		for(int v = 0; v < _graph.getNumVertices(); v++ ){
			sb.append("[" + v + "/" + vertexName[v] + "]");
			boolean l = false;
			for(Adjacency a : _graph.getAdj()[v]){
				sb.append((l == true ? ", " : "-> ") + a.getVertice() +  "/" + vertexName[a.getVertice()] + "/" +a.getWeight());
				l = true;
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public Set<String> getContexts(){
		return vertexIndex.keySet();
	}
	
	public void  setContextToSubContext(HashMap<String, String> _contextToSubContext){
		this.contextToSubContext =  _contextToSubContext;
	}
	
	public HashMap<String, String>  getContextToSubContext(){
		return this.contextToSubContext;
	}
	
/*    private static void writeAsXml(Object o) throws Exception {
      JAXBContext jaxb = JAXBContext.newInstance(o.getClass());

	    Marshaller xmlConverter = jaxb.createMarshaller();
	    xmlConverter.setProperty("jaxb.formatted.output", true);
	    
	    File xmlFile = File.createTempFile("raagsMap", ".xml"); 
      
	    xmlConverter.marshal(o, new PrintWriter(xmlFile));
	    xmlConverter.marshal(o, new PrintWriter(System.out));
	    
//==================================================================
	    SymbolGraph_New rebuildObject = new SymbolGraph_New();
	    
	    SymbolGraph_New copy = (SymbolGraph_New) readObjectAsXmlFrom(new FileReader(xmlFile.getAbsolutePath()), rebuildObject.getClass());
	    //Root2 copy = readObjectAsXmlFrom(new FileReader(xmlFile.getAbsolutePath()), root.getClass());

	    System.out.println("=========TO STRING ========>> \n\n\n");
//	    System.err.println(copy.toString());
	    xmlConverter.marshal(copy, new PrintWriter(System.out));
//==================================================================
  }	*/
    
/*    private static <T> T readObjectAsXmlFrom(Reader reader, Class<T> c) throws Exception{
        JAXBContext jaxb = JAXBContext.newInstance(c);

        XMLStreamReader xmlReader =
          XMLInputFactory.newInstance().createXMLStreamReader(reader);

        Unmarshaller xmlInterpreter = jaxb.createUnmarshaller();

        return xmlInterpreter.unmarshal(xmlReader, c).getValue();
      }*/  

	public static void rotasForm(String[] args) throws FileNotFoundException{
//		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotasPeso.txt");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\Sequence01.txt");
		boolean verbose = args.length > 0 ? false : true;
		
		BicamSymbolGraph symbolGraph = new BicamSymbolGraph(fileIn, verbose, "/");
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.graph.getReverse()));
		
		System.err.println("SHORTEST PATH");
//		ShortestPathByEdges paths = new ShortestPathByEdges(symbolGraph.getGraph(), symbolGraph.vertexIndex("Sao Paulo"));
//		Integer[] ppath = paths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));
		ShortestPathByEdges paths = new ShortestPathByEdges(symbolGraph.getGraph(), symbolGraph.vertexIndex("TAPUO0"));
		Integer[] ppath = paths.pathTo(symbolGraph.vertexIndex("TASUO001.SU_Saida"));		
		StringBuffer p = new StringBuffer();
		for(Integer item : ppath){
			if(p.length() > 0) p.append(" -> ");
			p.append(symbolGraph.vertexName(item));
		}
		System.err.println(p);
		
		System.err.println("SHORTEST PATH WEIGHT");
//		ShortestPathByWeightDijkstra paths2 = new ShortestPathByWeightDijkstra(symbolGraph.getGraph(), symbolGraph.vertexIndex("Sao Paulo"));
//		ppath = paths2.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));
		ShortestPathByWeightDijkstra paths2 = new ShortestPathByWeightDijkstra(symbolGraph.getGraph(), symbolGraph.vertexIndex("TAPUO0"));
		ppath = paths2.pathTo(symbolGraph.vertexIndex("TASUO001.SU_Saida"));

		p = new StringBuffer();
		for(Integer item : ppath){
			if(p.length() > 0) p.append(" -> ");
			p.append(symbolGraph.vertexName(item));
		}
		System.err.println(p);
		
		BicamSymbolGraph _symbolGraph = symbolGraph;
		Integer[] _path = ppath;
		HashMap<String, Integer> _vertexIndex = vertexIndex;
		HashMap<String, String> _contextToSubContext = contextToSubContext;
		HashMap<String, BicamNode> _nodeIndex = NodeIndex;
		BicamNode[] _nodes = nodes;
		String[] _nodeId = nodeId;
		
		generateGraphvizInputShortest( _symbolGraph,
				_path,
				_vertexIndex,
				_contextToSubContext,
				_nodeIndex,
				_nodes,
				_nodeId,
				true);
		
///		System.err.println("ALL PATHS");
//		AllPathsByEdges allPaths = new AllPathsByEdges(symbolGraph.getGraph(), symbolGraph.vertexIndex("Sao Paulo"));
//		ArrayList<Integer[]> ppaths = allPaths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));
		AllPathsByEdges allPaths = new AllPathsByEdges(symbolGraph.getGraph(), symbolGraph.vertexIndex("TAPUO0"));
		ArrayList<Integer[]> ppaths = allPaths.pathTo(symbolGraph.vertexIndex("TASUO001.SU_Saida"));

///		System.err.println(ppaths.size() + " paths");
		for(Integer[] pp : ppaths){
/*			StringBuffer spp = new StringBuffer();
			for(Integer item : pp){
				if(spp.length() > 0) spp.append(" -> ");
				spp.append(symbolGraph.vertexName(item));
			}
			System.err.println(spp);*/
			
			_path = pp;
			
			generateGraphvizInputShortest( _symbolGraph,
					_path,
					_vertexIndex,
					_contextToSubContext,
					_nodeIndex,
					_nodes,
					_nodeId,
					false);			
		}		

//		System.err.println("**" + symbolGraph.vertexIndex.get("Sao Paulo"));
//		System.err.println("**" + symbolGraph.vertexIndex.get("Campos dos Jordao"));
	}	
	
	public static void main(String[] args) throws FileNotFoundException {
//		rotas(args);
//		resolveSymbol(args);
		rotasForm(args);	
	}
	
	private static String generateGraphvizInputShortest(BicamSymbolGraph _symbolGraph,
			Integer[] _path,
			HashMap<String, Integer> _vertexIndex,
			HashMap<String, String> _contextToSubContext,
			HashMap<String, BicamNode> _nodeIndex,
			BicamNode[] _nodes,
			String[] _nodeId,
			Boolean isShortestPath
			) {
		HashMap<String, Integer> vertexIndex = _vertexIndex;
		
		HashMap<String, String> contextToSubContext = _contextToSubContext;
		
		HashMap<String, BicamNode> nodeIndex = _nodeIndex;
		
		BicamNode[] nodes = _nodes;
		
		String[] nodeId = _nodeId;
		
		Integer previousStep = null;
		
			StringBuffer graphviz = new StringBuffer();
			if(isShortestPath) {
			graphviz.append("digraph G {\n" 
				 + "ranksep=.75; size = \"7.5->7.5\"\n"
				 + "concentrate=true\n");
			}
			String headGraphviz = null;
			String tailGraphviz = null;
			for(Integer item : _path){
				if(previousStep == null) {
					previousStep = item;
					continue;
				}
				BicamNode headGraphvizNode = nodeIndex.get(_symbolGraph.vertexName(previousStep));
				BicamNode tailGraphvizNode = nodeIndex.get(_symbolGraph.vertexName(item));

				headGraphviz = "\"" + headGraphvizNode.getId() + "\"";
				tailGraphviz = "\"" + tailGraphvizNode.getId() + "\"";
				
				graphviz.append(headGraphviz + " -> " + tailGraphviz + " [") ;
//				graphviz.append (" label=" + headGraphvizNode.getBicamAdjacencyOutProperty(tailGraphvizNode.getId(),"WEIGHT"));
				if(isShortestPath)
					graphviz.append(" color=red ");
				graphviz.append("]\n ");

				previousStep = item;
			}
//			graphviz.append("}\n");

			System.err.println(graphviz);
		
		return graphviz.toString();
	}
}