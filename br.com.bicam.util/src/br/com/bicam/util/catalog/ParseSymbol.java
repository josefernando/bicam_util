package br.com.bicam.util.catalog;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseLexer;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.visualbasic6.VbpLexer;
import br.com.bicam.parser.visualbasic6.VbpParser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Lexer;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.Version;

public class ParseSymbol {
	final static String VB6_PROJECT_CONFIGURATION_UNIT = "VB6_PROJECT_CONFIGURATION_UNIT";
	final static String VB6_COMPILATION_UNIT = "VB6_COMPILATION_UNIT";
	final static String SQL_TRANSACT_SYBASE = "SQL_TRANSACT_SYBASE";

	final static String INPUT_STREAM = "INPUT_STREAM";
	
	PropertyList properties;
	ParseTree ast;
	Version version;
	InputStream is;
    ANTLRInputStream input;
	String compUnitType;
	
	public ParseSymbol(PropertyList _properties) throws IOException {
		properties = _properties;
		version = (Version) _properties.getProperty("VERSION");
/*		String dataSourceType = (String) version.getProperties().getProperty("DATA_SOURCE_TYPE");
		String dataSourceLocation = (String) version.getProperties().getProperty("DATA_SOURCE_LOCATION");
		String compilationUnitType = (String) version.getProperties().getProperty("COMPILATION_UNIT_TYPE");
*/		
		InputStream is = version.getInputStream();
//		is = (InputStream) _properties.getProperty(INPUT_STREAM);
//		compUnitType = (String) _properties.getProperty("COMPILATION_UNIT_TYPE");
		compUnitType = (String) version.getProperties().getProperty("COMPILATION_UNIT_TYPE");
		
		
	    try {
			input = new ANTLRInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(compUnitType == null) {
			BicamSystem.printLog("ERROR", new String().format("Null COMPILATION UNIT TYPE %s",compUnitType));
		}
		
		switch(compUnitType) {
		case VB6_PROJECT_CONFIGURATION_UNIT: vb6Project(_properties);
		    break;
		case VB6_COMPILATION_UNIT: vb6CompilationUnit(_properties);
	    	break;
		case SQL_TRANSACT_SYBASE: sqlTransactSybase(_properties);
			break;        	
		default:   
			BicamSystem.printLog("ERROR", new String().format("Invalid COMPILATION UNIT TYPE %s",compUnitType));
		}
	}

	private  void vb6Project(PropertyList _properties) {
	    Lexer lexer = new VbpLexer(input);
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
	    VbpParser parser = new VbpParser(tokens);
	    
	    parser.removeErrorListeners();
	    parser.addErrorListener(new VerboseListener());
	
	    parser.getInterpreter().setPredictionMode(PredictionMode.SLL); 
	    try { 
	    	ast = parser.startRule();
	    } 
	    catch (Exception ex) { 
	         	System.err.format("*** WARNING: re-parsing with  'PredictionMode.LL' %n%n");
	            tokens.reset(); // rewind input stream 
	            parser.reset(); 
	            parser.getInterpreter().setPredictionMode(PredictionMode.LL); 
	            ast = parser.startRule(); 
	    }
	    
	    setResult(parser);
	}

	private  void vb6CompilationUnit(PropertyList _properties) {
	    Lexer lexer = new VisualBasic6Lexer(input);
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
	    VisualBasic6Parser parser = new VisualBasic6Parser(tokens);
	    
	    parser.removeErrorListeners();
	    parser.addErrorListener(new VerboseListener());
	
	    parser.getInterpreter().setPredictionMode(PredictionMode.SLL); 
	    try { 
	    	ast = parser.startRule(); 
	    } 
	    catch (Exception ex) { 
	         	System.err.format("*** WARNING: re-parsing with  'PredictionMode.LL' %n%n");
	            tokens.reset(); // rewind input stream 
	            parser.reset(); 
	            parser.getInterpreter().setPredictionMode(PredictionMode.LL); 
	            ast = parser.startRule(); 
	    }
	    
	    setResult(parser);
	}	

	private  void sqlTransactSybase(PropertyList _properties) {
	    Lexer lexer = new SqlTransactSybaseLexer(input);
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
	    SqlTransactSybaseParser parser = new SqlTransactSybaseParser(tokens);
	    
	    parser.removeErrorListeners();
	    parser.addErrorListener(new VerboseListener());
	
	    parser.getInterpreter().setPredictionMode(PredictionMode.SLL); 
	    try { 
	    	ast = parser.startRule(); 
	    } 
	    catch (Exception ex) { 
	         	System.err.format("*** WARNING: re-parsing with  'PredictionMode.LL' %n%n");
	            tokens.reset(); // rewind input stream 
	            parser.reset(); 
	            parser.getInterpreter().setPredictionMode(PredictionMode.LL); 
	            ast = parser.startRule(); 
	    }
	    
	    setResult(parser);
	}	
	
	private void setResult(Parser _parser) {
		version.getProperties().addProperty("PARSER_ERROR", _parser.getNumberOfSyntaxErrors());
	    if(_parser.getNumberOfSyntaxErrors() > 0){
	    	System.err.format(" ERRORS - %d errors during parsing process%n", _parser.getNumberOfSyntaxErrors());
	    }
	    else {
	    	version.getProperties().addProperty("AST", ast);
	    	System.err.format("No ERRORS FOUND IN %s PARSING PROCESS%n", version.getParent().getId(), _parser.getNumberOfSyntaxErrors());
	    }		
	}
}