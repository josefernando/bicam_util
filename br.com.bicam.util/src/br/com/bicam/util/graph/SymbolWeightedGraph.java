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

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.catalog.InputGraph;

public class SymbolWeightedGraph {

	Set<String> vertices; // tem que ser set, porque não pode ter duplicações

	String nodeSeparator;
	
	File fileIn;
	List<InputGraph> inputGraph;
	NodeList nodeList;
	
	StringBuffer out;
	
	boolean verbose;
	
	HashMap<String, Integer> vertexIndex;
	
	HashMap<String, String> contextToSubContext;
	
	String[] vertexNames;
	
	WeightedGraph graph;
	
	Scanner scanner;
	
	public SymbolWeightedGraph(){} // Jaxb be need no parameter constructor
	
	public SymbolWeightedGraph(File _fileIn, boolean _verbose, String ..._nodeSeparator) throws FileNotFoundException{

		fileIn = _fileIn;
		
		this.verbose = _verbose;
		
		out = new StringBuffer();
		
		if(_nodeSeparator.length > 0) this.nodeSeparator = _nodeSeparator[0];
		else this.nodeSeparator = " ";

		vertices = new LinkedHashSet<>(); // HashSet porque não pode ser repetido, 
		                                  // a ordenação (LinkedHashSet) apenas ajuda em debug porque 
		                                  // faz com que a ordem de entrada do vertice de filein
		                                  // tenha a mesma ordem do conjunto "vertices

		vertexIndex = new LinkedHashMap<>(); 
		createGraph();
	}
	
	public SymbolWeightedGraph(List<InputGraph> _inputGraph, boolean _verbose, String ..._nodeSeparator) throws FileNotFoundException{

		inputGraph = _inputGraph;
		
		this.verbose = _verbose;
		
		out = new StringBuffer();
		
		if(_nodeSeparator.length > 0) this.nodeSeparator = _nodeSeparator[0];
		else this.nodeSeparator = " ";

		vertices = new LinkedHashSet<>(); // HashSet porque não pode ser repetido, 
		                                  // a ordenação (LinkedHashSet) apenas ajuda em debug porque 
		                                  // faz com que a ordem de entrada do vertice de filein
		                                  // tenha a mesma ordem do conjunto "vertices

		vertexIndex = new LinkedHashMap<>(); 
		createGraph();
	}	
	
	public SymbolWeightedGraph(NodeList _nodeList, boolean _verbose, String ..._nodeSeparator) throws FileNotFoundException{

		nodeList = _nodeList;
		
		this.verbose = _verbose;
		
		out = new StringBuffer();
		
		if(_nodeSeparator.length > 0) this.nodeSeparator = _nodeSeparator[0];
		else this.nodeSeparator = " ";

		vertices = new LinkedHashSet<>(); // HashSet porque não pode ser repetido, 
		                                  // a ordenação (LinkedHashSet) apenas ajuda em debug porque 
		                                  // faz com que a ordem de entrada do vertice de filein
		                                  // tenha a mesma ordem do conjunto "vertices

		vertexIndex = new LinkedHashMap<>(); 
		createGraph();
	}	

	private void createGraph() throws FileNotFoundException{
		mapVertices();
		graph = new WeightedGraph(vertices.size());
		
		String INPUT_TYPE = null;
		if(fileIn != null) INPUT_TYPE = "FILE";
		else if(inputGraph != null) INPUT_TYPE = "INPUT_GRAPH";
		else if(nodeList != null) INPUT_TYPE = "NODE_LIST";

		
		switch(INPUT_TYPE) {
			case "FILE" :{
				scanner = new Scanner(fileIn);
				String line = "";
				while(scanner.hasNextLine()){
					line = scanner.nextLine();
					String[] parts = line.split(nodeSeparator);
					if(parts.length > 2) {        // has weight
						graph.addEdge(vertexIndex(parts[0]), new Adjacency(vertexIndex(parts[1]) ,Double.parseDouble(parts[2])));
					}
					else if(parts.length == 2){   
						graph.addEdge(vertexIndex(parts[0]), new Adjacency(vertexIndex(parts[1])));
					}
				}
				scanner.close();				
			}
			break;
			case "INPUT_GRAPH" :{
				for(InputGraph ig : inputGraph) {
					if(ig.getHeadNode() != null) {
						graph.addEdge(vertexIndex(ig.getTailNode()), new Adjacency(vertexIndex(ig.getHeadNode()) ,ig.getWeight()));
					}
				}				
			}
			break;
			case "NODE_LIST" :{
				for(BicamNode node : nodeList.getNodes()) {
					String tail = node.getId();
					for(BicamAdjacency adj : node.getAdjacencyOut()) {
						String head = adj.node.getId();
						graph.addEdge(vertexIndex(tail), new Adjacency(vertexIndex(head) ,adj.getWeight()));
					}
				}				
			}
			break;
			default:
				BicamSystem.printLog("ERROR", "INPUT TYPE INVALID.");
		}
		
/*		if(fileIn != null) {
			scanner = new Scanner(fileIn);
			String line = "";
			while(scanner.hasNextLine()){
				line = scanner.nextLine();
				String[] parts = line.split(nodeSeparator);
				if(parts.length > 2) {        // has weight
					graph.addEdge(vertexIndex(parts[0]), new Adjacency(vertexIndex(parts[1]) ,Double.parseDouble(parts[2])));
				}
				else if(parts.length == 2){   
					graph.addEdge(vertexIndex(parts[0]), new Adjacency(vertexIndex(parts[1])));
				}
			}
			scanner.close();
		}
		else {
			for(InputGraph ig : inputGraph) {
				if(ig.getHeadNode() != null) {
					graph.addEdge(vertexIndex(ig.getTailNode()), new Adjacency(vertexIndex(ig.getHeadNode()) ,ig.getWeight()));
				}
			}
		}*/
	}
	
	private void mapVertices() throws FileNotFoundException {
		PropertyList properties = null;
		
		if(fileIn != null) {
			properties = BicamSystem.graphVertices(fileIn, "/");
		} else if(inputGraph != null) {
			properties = BicamSystem.graphVertices(inputGraph, "/");
		}
		else if(nodeList != null) {
			properties = BicamSystem.graphVertices(nodeList, "/");
		}

		vertices = (Set<String>) properties.getProperty("VERTICES");	
		vertexNames = (String[]) properties.getProperty("VERTEX_TO_NAME");
		vertexIndex = (HashMap<String, Integer>) properties.getProperty("NAME_TO_VERTEX");
		
		if(verbose){
			System.err.println("vertexName:");
			for(int i = 0; i < vertexNames.length; i++){
				System.err.format("[%d]: %s%n",i,vertexNames[i]);
			}
			
			System.err.println("\nvertexIndex:");
			for(String context : vertexIndex.keySet()){
				System.err.format("[%s]: %d%n",context,vertexIndex.get(context));
			}
		}		
	}
	
	public WeightedGraph getGraph(){
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
		return vertexNames[_index];
	}
	
	/*
	 * descobre todos os vertices e os coloca em um conjunto sem redundância (Set)
	 */
/*	private void loadVertices(File _file) throws FileNotFoundException {
		String line = "";
		scanner = new Scanner(fileIn);
		int numline = 0;
		while(scanner.hasNextLine()){
			line = scanner.nextLine(); // a b p, onde "a" = começo da aresta, "b" = fim da aresta  e "p" = peso da aresta
			numline++;
			String[] parts = line.split(nodeSeparator);
			for(int i = 0; i < 2; i++){
				vertices.add(parts[i]);
			}
		}
	}*/
	
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
			sb.append("[" + v + "/" + vertexNames[v] + "]");
			boolean l = false;
			for(Adjacency a : _graph.getAdj()[v]){
				sb.append((l == true ? ", " : "-> ") + a.getVertice() +  "/" + vertexNames[a.getVertice()] + "/" +a.getWeight());
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
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotasPeso.txt");
//		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotasForm.txt");
//		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\flowSequence.txt");
//		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\procedureSequence.txt");
//		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\uiSequence.txt");

		boolean verbose = args.length > 0 ? false : true;
		
		String separator = "/";
//		String separator = " ";
		
		SymbolWeightedGraph symbolGraph = new SymbolWeightedGraph(fileIn, verbose, separator);
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.graph.getReverse()));
		
		System.err.println("SHORTEST PATH");
//		ShortestPathByEdges paths = new ShortestPathByEdges(symbolGraph.getGraph()
//				, symbolGraph.vertexIndex("TIPMA001"));
//		Integer[] ppath = paths.pathTo(symbolGraph.vertexIndex("TIPMA001.TIFMA001#COMPILATION_UNIT.GR_manutencao_faixa_Click"));
		ShortestPathByEdges paths = new ShortestPathByEdges(symbolGraph.getGraph()
		, symbolGraph.vertexIndex("Guarulhos"));
Integer[] ppath = paths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));

		StringBuffer p = new StringBuffer();
		for(Integer item : ppath){
			if(p.length() > 0) p.append(" -> ");
			p.append(symbolGraph.vertexName(item));
		}
		System.err.println(p);
		
		System.err.println("SHORTEST PATH WEIGHT");
//		ShortestPathByWeightDijkstra paths2 = new ShortestPathByWeightDijkstra(symbolGraph.getGraph(), symbolGraph.vertexIndex("TIPMA001"));
//		ppath = paths2.pathTo(symbolGraph.vertexIndex("TIPMA001.TIFMA001#COMPILATION_UNIT.GR_manutencao_faixa_Click"));
		ShortestPathByWeightDijkstra paths2 = new ShortestPathByWeightDijkstra(symbolGraph.getGraph(), symbolGraph.vertexIndex("Guarulhos"));
		ppath = paths2.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));

		p = new StringBuffer();
		for(Integer item : ppath){
			if(p.length() > 0) p.append(" -> ");
			p.append(symbolGraph.vertexName(item));
		}
		System.err.println(p);		
		
		System.err.println("ALL PATHS");
//		AllPathsByEdges allPaths = new AllPathsByEdges(symbolGraph.getGraph(), symbolGraph.vertexIndex("TIPMA001"));
//		ArrayList<Integer[]> ppaths = allPaths.pathTo(symbolGraph.vertexIndex("TIPMA001.TIFMA001#COMPILATION_UNIT.GR_manutencao_faixa_Click"), 99);
		AllPathsByEdges allPaths = new AllPathsByEdges(symbolGraph.getGraph(), symbolGraph.vertexIndex("Guarulhos"));
		ArrayList<Integer[]> ppaths = allPaths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"), 99);

		System.err.println(ppaths.size() + " paths");
		
		int i = 1;
		for(Integer[] pp : ppaths){
			StringBuffer spp = new StringBuffer();
			for(Integer item : pp){
				if(spp.length() > 0) spp.append(" -> ");
				spp.append("\"" + symbolGraph.vertexName(item) + "\"");
			}
			System.err.println(i++);
			System.err.println(spp);
		}
		
		i = 101;		
		for(Integer[] pp : ppaths){
			StringBuffer spp1 = new StringBuffer();
			String ant = null;
			for(Integer item : pp){
				if(ant != null) {
					spp1.append("\"" + ant + "\"" + " -> " + "\"" + symbolGraph.vertexName(item) + "\"" + System.lineSeparator());
				}
				ant = symbolGraph.vertexName(item);
			}
			System.err.println(i++);
			System.err.println("\n" + spp1.toString());
		}		
		
//		System.err.println("**" + symbolGraph.vertexIndex.get("Sao Paulo"));
//		System.err.println("**" + symbolGraph.vertexIndex.get("Campos dos Jordao"));

/*		try {
			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
/*		ShortestPath paths = new ShortestPath(symbolGraph.getGraph().reverse.getbasicGraph(), symbolGraph.vertexIndex("R1PAB0"));

		Integer[] sp = paths.pathTo(symbolGraph.vertexIndex("R1FAB002Show"));

		System.err.format("Size of shortest path: %d%n",sp.length);
		StringBuffer sb = new StringBuffer();

		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.vertexName(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
//		System.err.println("*** Sao Paulo " + symbolGraph.vertexIndex("Sao Paulo"));
//		System.err.println("*** Campos dos Jordao " + symbolGraph.vertexIndex("Campos dos Jordao"));
		
		
		symbolGraph.getAllPath("R1PAB0", "R1FAB002Show", 99);*/
	}	
	
/*	public static void rotas(String[] args) throws FileNotFoundException{
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicbasicGraph");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotas.txt");
        boolean verbose = args.length > 0 ? false : true;
		
		SymbolWeightedGraph symbolGraph = new SymbolWeightedGraph(fileIn, verbose, "/");
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println(symbolGraph.toString());		
		System.err.println("Reverse");
//		System.err.println(symbolGraph.toString(symbolGraph.getbasicGraph().getReverse()));
		
		System.err.println("**" + symbolGraph.vertexIndex.get("Sao Paulo"));
		System.err.println("**" + symbolGraph.vertexIndex.get("Campos dos Jordao"));

		try {
			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.vertexIndex("Sao Paulo"));

		Integer[] sp = paths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));

		System.err.format("Size of shortest path: %d%n",sp.length);
		StringBuffer sb = new StringBuffer();

		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.vertexName(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
		System.err.println("*** Sao Paulo " + symbolGraph.vertexIndex("Sao Paulo"));
		System.err.println("*** Campos dos Jordao " + symbolGraph.vertexIndex("Campos dos Jordao"));
		
		
		symbolGraph.getAllPath("Sao Paulo", "Campos dos Jordao", 99);
	}*/
	
/*	public static void resolveSymbol(String[] args) throws FileNotFoundException{
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicbasicGraph");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\resolve.txt");
        boolean verbose = args.length > 0 ? false : true;
		
		SymbolWeightedGraph symbolGraph = new SymbolWeightedGraph(fileIn, verbose, "/");
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println(symbolGraph.toString());		
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.getbasicGraph().getReverse()));
		
		System.err.println("** Use" + symbolGraph.vertexIndex.get("KeyAscii"));
		System.err.println("** Defined" + symbolGraph.vertexIndex.get("KeyAscii"));

		try {
			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.vertexIndex("KeyAscii"));

		Integer[] sp = paths.pathTo(symbolGraph.vertexIndex("KeyAscii"));

		System.err.format("Size of shortest path: %d%n",sp.length);
		StringBuffer sb = new StringBuffer();

		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.vertexName(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
		System.err.println("*** Use " + symbolGraph.vertexIndex("KeyAscii"));
		System.err.println("*** defined " + symbolGraph.vertexIndex("KeyAscii"));
		
		
		symbolGraph.getAllPath("KeyAscii", "KeyAscii", 99);
	}*/
	
	public static void main(String[] args) throws FileNotFoundException {
//		rotas(args);
//		resolveSymbol(args);
		rotasForm(args);	
	}
/*	public static void main(String[] args) throws FileNotFoundException {
//		rotas(args);
//		resolveSymbol(args);
		rotasForm(args);		
		System.exit(1);
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicbasicGraph");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotas.txt");
        boolean verbose = args.length > 0 ? false : true;
		
		SymbolWeightedGraph symbolGraph = new SymbolWeightedGraph(fileIn, verbose, "/");
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println(symbolGraph.toString());		
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.getGraph().reverse));
		
		System.err.println("**" + symbolGraph.vertexIndex.get("Sao Paulo"));
		System.err.println("**" + symbolGraph.vertexIndex.get("Campos dos Jordao"));

		try {
			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.vertexIndex("Sao Paulo"));
		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.vertexIndex("Sao Paulo"));

		//		ArrayDeque sp = paths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));
		Integer[] sp = paths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));

		System.err.format("Size of shortest path: %d%n",sp.length);
//		Iterator<Integer> nodes = sp.iterator();
		StringBuffer sb = new StringBuffer();
		while(nodes.hasNext()){
			sb.append(symbolGraph.vertexName(nodes.next()) + " -> ");
		}
		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.vertexName(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
		System.err.println("*** Sao Paulo " + symbolGraph.vertexIndex("Sao Paulo"));
		System.err.println("*** Campos dos Jordao " + symbolGraph.vertexIndex("Campos dos Jordao"));
		
		
		symbolGraph.getAllPath("Sao Paulo", "Campos dos Jordao", 99);

		AllPaths allpaths = new AllPaths(symbolGraph.getbasicGraph(), symbolGraph.vertexIndex("Sao Paulo"));
//		ArrayList<ArrayDeque<Integer>> all = allpaths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"));
		ArrayList<Integer[]> all = allpaths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao"), 99);

//		System.err.println("\n" + paths.pathTo(symbolGraph.vertexIndex("Campos dos Jordao")).size());
		System.err.println("\nQuantidade de Paths:" + all.size());	
	
		int i = 0;
		sb.setLength(0);
		for(Integer[] path : all){
			System.err.format("\nPath %d ", i++);
			for (Integer v : path){
				if(sb.length() > 0 ) sb.append(" -> ");
				sb.append(symbolGraph.vertexName(v));
			}
			System.err.println(sb.toString());
			sb.setLength(0);
		}
	}	*/
}