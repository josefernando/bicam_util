package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.Source;
import br.com.bicam.util.datamodel.Version;

public class SequenceCall {
	final static String VB6_COMPILATION_UNIT = "VB6_COMPILATION_UNIT";
	final static String SQL_TRANSACT_SYBASE = "SQL_TRANSACT_SYBASE";

	final static String INPUT_STREAM = "INPUT_STREAM";
	
	public SequenceCall(PropertyList _properties) {
		
		String compUnitType = (String) _properties.getProperty("COMPILATION_UNIT_TYPE");
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
	
	private static void vb6CompilationUnit(PropertyList _properties) {
			PropertyList properties = _properties;
			Version version = (Version) properties.getProperty("VERSION");
			ParseTree astree = (ParseTree) version.getProperties().getProperty("AST");

			Source source = (Source) version.getParent();

			System.err.println("SEQUENCE CALL  VB6 " + source.getId());
			ParseTreeWalker walker = new ParseTreeWalker();
//			SequenceCallVb6 sequenceCall = new SequenceCallVb6(properties);
			SequenceCallVb6_New sequenceCall = new SequenceCallVb6_New(properties);

	        walker.walk(sequenceCall, astree);
//	        NodeList nodeList = (NodeList) properties.getProperty("NODELIST");
//			System.err.println(nodeList.inputSymbolGraph());
//  UTILIZA SymbolWeightedGraph para lista paths	        
        	version.getProperties().addProperty("APP_NODE", properties.getProperty("APP_NODE"));
        	version.getProperties().addProperty("NODELIST", sequenceCall.getNodelist());
	}	
	
	private static void sqlTransactSybase(PropertyList _properties) {
			// TBD
	}	
}