package br.com.bicam.util.catalog;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.Source;
import br.com.bicam.util.datamodel.Version;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class RefSymbol {
	final static String VB6_COMPILATION_UNIT = "VB6_COMPILATION_UNIT";
	final static String SQL_TRANSACT_SYBASE = "SQL_TRANSACT_SYBASE";

	final static String INPUT_STREAM = "INPUT_STREAM";
	PropertyList properties;
	SymbolTable_New st;
	InputStream is;
	String compilationUnitType;
	Source source;
	Version version;
	ParseTree astree;
	String compUnitType;
	
	public RefSymbol(PropertyList _properties) throws IOException {
/*		
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
		}*/
		properties = _properties;
		st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		version = (Version) _properties.getProperty("VERSION");
		is = version.getInputStream();
		
		compUnitType = (String) version.getProperties().getProperty("COMPILATION_UNIT_TYPE");
		astree = (ParseTree) version.getProperties().getProperty("AST");

		source = (Source) version.getParent();	
		astree = (ParseTree) version.getProperties().getProperty("AST");

		
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
			SymbolTable_New st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
			InputStream is = (InputStream) properties.getProperty(INPUT_STREAM);
			String compilationUnitType = (String) properties.getProperty("COMPILATION_UNIT_TYPE");
			Version version = (Version) properties.getProperty("VERSION");
			ParseTree astree = (ParseTree) version.getProperties().getProperty("AST");

			Source source = (Source) version.getParent();

			System.err.println("DEF SYMBOL VB6 - " + source.getId());
			ParseTreeWalker walker = new ParseTreeWalker();
			Vb6CompilationUnitRefSymbol refSymbol = new Vb6CompilationUnitRefSymbol(properties);

	        walker.walk(refSymbol, astree);
	}	
	
	private static void sqlTransactSybase(PropertyList _properties) {
		PropertyList properties = _properties;
//		SymbolTable_New st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
//		InputStream is = (InputStream) properties.getProperty(INPUT_STREAM);
//		String compilationUnitType = (String) properties.getProperty("COMPILATION_UNIT_TYPE");
		Version version = (Version) properties.getProperty("VERSION");
		ParseTree astree = (ParseTree) version.getProperties().getProperty("AST");

		Source source = (Source) version.getParent();

		System.err.println("REF SYMBOL sqlTransactSybase - " + source.getId());
		ParseTreeWalker walker = new ParseTreeWalker();
		SqlTransactSybaseCompilationUnitRefSymbol refSymbol = new SqlTransactSybaseCompilationUnitRefSymbol(properties);

        walker.walk(refSymbol, astree);
	}	
}