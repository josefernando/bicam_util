package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.Source;
import br.com.bicam.util.datamodel.Version;

public class GraphCompUnit {
	final static String VB6_COMPILATION_UNIT = "VB6_COMPILATION_UNIT";
	final static String SQL_TRANSACT_SYBASE = "SQL_TRANSACT_SYBASE";

	final static String INPUT_STREAM = "INPUT_STREAM";
	
	PropertyList properties;
	Version version;
	ParseTree astree;
	String compUnitType;
	Source source;
	
	public GraphCompUnit(PropertyList _properties) {
		properties = _properties;
		version = (Version) _properties.getProperty("VERSION");
		source = (Source) version.getParent();
		astree = (ParseTree) version.getProperties().getProperty("AST");
		
		compUnitType = (String) version.getProperties().getProperty("COMPILATION_UNIT_TYPE");
		
		if(compUnitType == null) {
			BicamSystem.printLog("ERROR", new String().format("Null COMPILATION UNIT TYPE %s",compUnitType));
		}
		
		switch(compUnitType) {
			case VB6_COMPILATION_UNIT: vb6CompilationUnit(_properties);
	        	break;
			case SQL_TRANSACT_SYBASE: sqlTransactSybase(_properties);
	    		break;        	
			default:   
				BicamSystem.printLog("ERROR", new String().format("Invalid COMPILATION UNIT TYPE %s",compUnitType));
		}
	}
	
	private void vb6CompilationUnit(PropertyList _properties) {
/*			PropertyList properties = _properties;
			Version version = (Version) properties.getProperty("VERSION");
			ParseTree astree = (ParseTree) version.getProperties().getProperty("AST");

			Source source = (Source) version.getParent();
*/
			System.err.println("BUILDING  VB6 COMP UNIT GRAPH: " + source.getId());
			ParseTreeWalker walker = new ParseTreeWalker();
			GraphCompilationUnitVb6 graphBuilder = new GraphCompilationUnitVb6(properties);

	        walker.walk(graphBuilder, astree);
	        version.getProperties().addProperty("COMP_UNIT_GRAPH", graphBuilder.getGraph());
	        version.getProperties().addProperty("GRAPH_LINE_TO_NODE", graphBuilder.getLineToNodeId());
	}	
	
	private static void sqlTransactSybase(PropertyList _properties) {
			// TBD
	}	
}