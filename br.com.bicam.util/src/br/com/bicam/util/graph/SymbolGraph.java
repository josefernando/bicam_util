package br.com.bicam.util.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Marshaller;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlElementWrapper;
//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import br.com.bicam.model.util.MapAdapterString2Integer;
import br.com.bicam.model.util.MapAdapterString2String;


//@XmlAccessorType (XmlAccessType.PROPERTY)
//@XmlRootElement
public class SymbolGraph implements Serializable {
//	@XmlElementWrapper(name = "vertices")
//	@XmlElement(name = "node")
	LinkedHashSet<String> vertices;
	
	String nodeSeparator;
	
	File fileIn;
	StringBuffer out;
	
	InputStream inputStream;

	
	boolean verbose;
	
//	@XmlJavaTypeAdapter(MapAdapterString2Integer.class)
	HashMap<String, Integer> contextToIndex;
	
	HashMap<String, String> contextToSubContext;
	
//	@XmlElement
	String[] indexToContext;
	
//	@XmlElement
	BasicGraph basicGraph;
	
	Scanner scanner;
	
	public SymbolGraph(){} // Jaxb be need no parameter constructor
	
	public SymbolGraph(File _fileIn, File _fileOut, boolean _verbose, String ..._nodeSeparator) throws FileNotFoundException{

		fileIn = _fileIn;
		
		this.verbose = _verbose;
		
		out = new StringBuffer();
		
		if(_nodeSeparator.length > 0) this.nodeSeparator = _nodeSeparator[0];
		else this.nodeSeparator = " ";

		vertices = new LinkedHashSet<>(); // HashSet porque não pode ser repetido
		contextToIndex = new LinkedHashMap<>(); 
		createBasicGraph();
	}
	
	public SymbolGraph(InputStream _inputStream, File _fileOut, boolean _verbose, String ..._nodeSeparator) throws FileNotFoundException{

		inputStream = _inputStream;
		
		this.verbose = _verbose;
		
		out = new StringBuffer();
		
		if(_nodeSeparator.length > 0) this.nodeSeparator = _nodeSeparator[0];
		else this.nodeSeparator = " ";

		vertices = new LinkedHashSet<>(); // HashSet porque não pode ser repetido
		contextToIndex = new LinkedHashMap<>(); 
		createBasicGraph();
	}	

	private void createBasicGraph() throws FileNotFoundException{
		mapVertices();
		
		basicGraph = new BasicGraph(vertices.size());
		if(fileIn != null) scanner = new Scanner(fileIn);
		else scanner = new Scanner(inputStream);
//		scanner = new Scanner(fileIn);
		String line = "";
		
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(nodeSeparator);
			if(parts.length > 1){
				basicGraph.addEdge(contextToIndex.get(parts[0]), contextToIndex.get(parts[1]));
			}
		}
	}
	
	private void mapVertices() throws FileNotFoundException{

//        loadVertices(fileIn);
      loadVertices();

		List<String> list = new ArrayList<String>(vertices);

		indexToContext = new String[vertices.size()];
		for(int i = 0; i < indexToContext.length; i++){
			indexToContext[i] = list.get(i);
			contextToIndex.put(list.get(i), i);
		}
		
		if(verbose){
			System.err.println("indexToContext:");
			for(int i = 0; i < indexToContext.length; i++){
				System.err.format("[%d]: %s%n",i,indexToContext[i]);
			}
			
			System.err.println("\ncontextToIndex:");
			for(String context : contextToIndex.keySet()){
				System.err.format("[%s]: %d%n",context,contextToIndex.get(context));
			}
		}		
	}
	
	public BasicGraph getbasicGraph(){
		return basicGraph;
	}

	public int getNumEdges() {
		return basicGraph.getNumEdges();
	}

	public int getNumVertices() {
		return basicGraph.getNumVertices();
	}
	
	public Integer contextToIndex(String _key){
		if(contextToIndex.get(_key) == null){
			System.err.println("\n" +"*** DEBUG: context-> " + _key);
			System.err.println(toString() + "\n");
		}
		return contextToIndex.get(_key);
	}
	
	public String indexToContext(int _index){
		return indexToContext[_index];
	}
	
//	private void loadVertices(File _file) throws FileNotFoundException{
		private void loadVertices() throws FileNotFoundException{
		
		String line = "";
		if( fileIn != null ) scanner = new Scanner(fileIn);
		else scanner = new Scanner(inputStream);
//		scanner = new Scanner(fileIn);
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(nodeSeparator);
			for(String s : parts){
				vertices.add(s);
			}
		}
	}
	
	public void getAllPath(String _source, String _target, Integer ..._maxPaths){
		StringBuffer sb = new StringBuffer();
		AllPaths allpaths = new AllPaths(getbasicGraph(), contextToIndex(_source));
		ArrayList<Integer[]> all;
		if(_maxPaths.length > 0 )
			all = allpaths.pathTo(contextToIndex(_target), _maxPaths[0]);
		else
			all = allpaths.pathTo(contextToIndex(_target), 10);
		
		int i = 0;
		sb.setLength(0);
		for(Integer[] path : all){
			System.err.format("\nPath %d ", i++);
			for (Integer v : path){
				if(sb.length() > 0 ) sb.append(" -> ");
				sb.append(indexToContext(v));
			}
			System.err.println(sb.toString());
			sb.setLength(0);
		}		
	}
	
	public String toString() {
		return toString(getbasicGraph());
	}
	
	public String toString(BasicGraph _graph) {
		StringBuffer sb = new StringBuffer();
		for(int v = 0; v < _graph.getNumVertices(); v++ ){
			sb.append("[" + v + "/" + indexToContext[v] + "]");
			boolean l = false;
			for(int i : _graph.getAdj()[v]){
				sb.append((l == true ? ", " : "-> ") + i + "/" + indexToContext[i]);
				l = true;
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public Set<String> getContexts(){
		return contextToIndex.keySet();
	}
	
//	@XmlJavaTypeAdapter(MapAdapterString2String.class)
	public void  setContextToSubContext(HashMap<String, String> _contextToSubContext){
		this.contextToSubContext =  _contextToSubContext;
	}
	
	public HashMap<String, String>  getContextToSubContext(){
		return this.contextToSubContext;
	}
	
	/*
	 * private static void writeAsXml(Object o) throws Exception { JAXBContext jaxb
	 * = JAXBContext.newInstance(o.getClass());
	 * 
	 * Marshaller xmlConverter = jaxb.createMarshaller();
	 * xmlConverter.setProperty("jaxb.formatted.output", true);
	 * 
	 * File xmlFile = File.createTempFile("raagsMap", ".xml");
	 * 
	 * xmlConverter.marshal(o, new PrintWriter(xmlFile)); xmlConverter.marshal(o,
	 * new PrintWriter(System.out));
	 * 
	 * //==================================================================
	 * SymbolGraph rebuildObject = new SymbolGraph();
	 * 
	 * SymbolGraph copy = (SymbolGraph) readObjectAsXmlFrom(new
	 * FileReader(xmlFile.getAbsolutePath()), rebuildObject.getClass()); //Root2
	 * copy = readObjectAsXmlFrom(new FileReader(xmlFile.getAbsolutePath()),
	 * root.getClass());
	 * 
	 * System.out.println("=========TO STRING ========>> \n\n\n"); //
	 * System.err.println(copy.toString()); xmlConverter.marshal(copy, new
	 * PrintWriter(System.out));
	 * //================================================================== }
	 */
	/*
	 * private static <T> T readObjectAsXmlFrom(Reader reader, Class<T> c) throws
	 * Exception{ JAXBContext jaxb = JAXBContext.newInstance(c);
	 * 
	 * XMLStreamReader xmlReader =
	 * XMLInputFactory.newInstance().createXMLStreamReader(reader);
	 * 
	 * Unmarshaller xmlInterpreter = jaxb.createUnmarshaller();
	 * 
	 * return xmlInterpreter.unmarshal(xmlReader, c).getValue(); }
	 */
	public static void main(String[] args) throws FileNotFoundException {
		rotas(args);
//		resolveSymbol(args);
//		rotasForm(args);		
		System.exit(1);
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicbasicGraph");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotas.txt");
        boolean verbose = args.length > 0 ? false : true;
		
//		SymbolGraph symbolGraph = new SymbolGraph(fileIn, fileOut, verbose, "/");
        FileInputStream fis = new FileInputStream(fileIn);
		SymbolGraph symbolGraph = new SymbolGraph(fis, fileOut, verbose, "/");

		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println(symbolGraph.toString());		
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.getbasicGraph().getReverse()));
		
		System.err.println("**" + symbolGraph.contextToIndex.get("Sao Paulo"));
		System.err.println("**" + symbolGraph.contextToIndex.get("Campos dos Jordao"));

		try {
//			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.contextToIndex("Sao Paulo"));
		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.contextToIndex("Sao Paulo"));

		//		ArrayDeque sp = paths.pathTo(symbolGraph.contextToIndex("Campos dos Jordao"));
		Integer[] sp = paths.pathTo(symbolGraph.contextToIndex("Campos dos Jordao"));

		System.err.format("Size of shortest path: %d%n",sp.length);
//		Iterator<Integer> nodes = sp.iterator();
		StringBuffer sb = new StringBuffer();
/*		while(nodes.hasNext()){
			sb.append(symbolGraph.indexToContext(nodes.next()) + " -> ");
		}*/
		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.indexToContext(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
		System.err.println("*** Sao Paulo " + symbolGraph.contextToIndex("Sao Paulo"));
		System.err.println("*** Campos dos Jordao " + symbolGraph.contextToIndex("Campos dos Jordao"));
		
		
		symbolGraph.getAllPath("Sao Paulo", "Campos dos Jordao", 99);

/*		AllPaths allpaths = new AllPaths(symbolGraph.getbasicGraph(), symbolGraph.contextToIndex("Sao Paulo"));
//		ArrayList<ArrayDeque<Integer>> all = allpaths.pathTo(symbolGraph.contextToIndex("Campos dos Jordao"));
		ArrayList<Integer[]> all = allpaths.pathTo(symbolGraph.contextToIndex("Campos dos Jordao"), 99);

//		System.err.println("\n" + paths.pathTo(symbolGraph.contextToIndex("Campos dos Jordao")).size());
		System.err.println("\nQuantidade de Paths:" + all.size());	
	
		int i = 0;
		sb.setLength(0);
		for(Integer[] path : all){
			System.err.format("\nPath %d ", i++);
			for (Integer v : path){
				if(sb.length() > 0 ) sb.append(" -> ");
				sb.append(symbolGraph.indexToContext(v));
			}
			System.err.println(sb.toString());
			sb.setLength(0);
		}*/
	}
	public static void rotasForm(String[] args) throws FileNotFoundException{
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicbasicGraph");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotasForm.txt");
        boolean verbose = args.length > 0 ? false : true;
		
		SymbolGraph symbolGraph = new SymbolGraph(fileIn, fileOut, verbose, " ");
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println(symbolGraph.toString());		
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.getbasicGraph().getReverse()));
		
//		System.err.println("**" + symbolGraph.contextToIndex.get("Sao Paulo"));
//		System.err.println("**" + symbolGraph.contextToIndex.get("Campos dos Jordao"));

		try {
//			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.contextToIndex("R1PAB0"));

		Integer[] sp = paths.pathTo(symbolGraph.contextToIndex("R1FAB001.BU3D_Cancela"));

		System.err.format("Size of shortest path: %d%n",sp.length);
		StringBuffer sb = new StringBuffer();

		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.indexToContext(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
//		System.err.println("*** Sao Paulo " + symbolGraph.contextToIndex("Sao Paulo"));
//		System.err.println("*** Campos dos Jordao " + symbolGraph.contextToIndex("Campos dos Jordao"));
		
		
		symbolGraph.getAllPath("R1PAB0", "R1FAB001.BU3D_Cancela", 99);
	}	
	
	public static void rotas(String[] args) throws FileNotFoundException{
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicbasicGraph");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\rotas.txt");
        boolean verbose = args.length > 0 ? false : true;
		
		SymbolGraph symbolGraph = new SymbolGraph(fileIn, fileOut, verbose, "/");
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println(symbolGraph.toString());		
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.getbasicGraph().getReverse()));
		
		System.err.println("**" + symbolGraph.contextToIndex.get("Sao Paulo"));
		System.err.println("**" + symbolGraph.contextToIndex.get("Campos dos Jordao"));

		try {
//			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.contextToIndex("Sao Paulo"));

		Integer[] sp = paths.pathTo(symbolGraph.contextToIndex("Campos dos Jordao"));

		System.err.format("Size of shortest path: %d%n",sp.length);
		StringBuffer sb = new StringBuffer();

		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.indexToContext(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
		System.err.println("*** Sao Paulo " + symbolGraph.contextToIndex("Sao Paulo"));
		System.err.println("*** Campos dos Jordao " + symbolGraph.contextToIndex("Campos dos Jordao"));
		
		
		symbolGraph.getAllPath("Sao Paulo", "Campos dos Jordao", 99);
	}
	
	public static void resolveSymbol(String[] args) throws FileNotFoundException{
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\input_BasicbasicGraph");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\resolve.txt");
        boolean verbose = args.length > 0 ? false : true;
		
		SymbolGraph symbolGraph = new SymbolGraph(fileIn, fileOut, verbose, "/");
		System.err.println();
		System.err.println(symbolGraph.toString());
		System.err.println();
		System.err.println(symbolGraph.toString());		
		System.err.println("Reverse");
		System.err.println(symbolGraph.toString(symbolGraph.getbasicGraph().getReverse()));
		
		System.err.println("** Use" + symbolGraph.contextToIndex.get("KeyAscii"));
		System.err.println("** Defined" + symbolGraph.contextToIndex.get("KeyAscii"));

		try {
//			writeAsXml(symbolGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ShortestPath paths = new ShortestPath(symbolGraph.getbasicGraph(), symbolGraph.contextToIndex("KeyAscii"));

		Integer[] sp = paths.pathTo(symbolGraph.contextToIndex("KeyAscii"));

		System.err.format("Size of shortest path: %d%n",sp.length);
		StringBuffer sb = new StringBuffer();

		for (Integer v : sp){
			if(sb.length() > 0 ) sb.append(" -> ");
			sb.append(symbolGraph.indexToContext(v));
		}
		System.err.println(sb.toString());	
		
		System.err.println(symbolGraph.getbasicGraph().toString());
		
		
		System.err.println("*** Use " + symbolGraph.contextToIndex("KeyAscii"));
		System.err.println("*** defined " + symbolGraph.contextToIndex("KeyAscii"));
		
		
		symbolGraph.getAllPath("KeyAscii", "KeyAscii", 99);
	}
}