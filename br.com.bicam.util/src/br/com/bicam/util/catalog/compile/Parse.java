package br.com.bicam.util.catalog.compile;

import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseLexer;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.visualbasic6.VbpLexer;
import br.com.bicam.parser.visualbasic6.VbpParser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Lexer;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.KeywordVB6;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.catalog.MetadataVb6;
import br.com.bicam.util.catalog.VerboseListener;
import br.com.bicam.util.datamodel.Catalog;
import br.com.bicam.util.datamodel.Repository;
import br.com.bicam.util.datamodel.Source;
import br.com.bicam.util.datamodel.Version;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class Parse {
	final static String VB6_PROJECT = "VB6_PROJECT";
	final static String VB6_COMPILATION_UNIT = "VB6_COMPILATION_UNIT";
	final static String SQL_TRANSACT_SYBASE = "SQL_TRANSACT_SYBASE";

	final static String INPUT_STREAM = "INPUT_STREAM";
	
	public static void parse(PropertyList _properties) {
		PropertyList properties = _properties;
		
		String compUnitType = (String) _properties.getProperty("COMPILATION_UNIT_TYPE");
		if(compUnitType == null) {
			BicamSystem.printLog("ERROR", new String().format("Null COMPILATION UNIT TYPE %s",compUnitType));
		}
		
		switch(compUnitType) {
		case VB6_PROJECT: vb6Project(_properties);
		    break;
		case VB6_COMPILATION_UNIT: vb6CompilationUnit(_properties);
        	break;
		case SQL_TRANSACT_SYBASE: sqlTransactSybase(_properties);
    		break;        	
		default:   
			BicamSystem.printLog("ERROR", new String().format("Invalid COMPILATION UNIT TYPE %s",compUnitType));
		}
	}
	
	private static void vb6Project(PropertyList _properties) {
		ParseTree ast = null;
		Version version = (Version) _properties.getProperty("VERSION");
		InputStream is = (InputStream) _properties.getProperty(INPUT_STREAM);
        ANTLRInputStream input = null;

        try {
			input = new ANTLRInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        VbpLexer lexer = new VbpLexer(input);
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
        
        if(parser.getNumberOfSyntaxErrors() > 0){
        	System.err.format(" ERRORS - %d errors during parsing process%n", parser.getNumberOfSyntaxErrors());
        }
        else {
        	version.getProperties().addProperty("AST", ast);
        	System.err.format("No ERRORS process%n", parser.getNumberOfSyntaxErrors());
        }
	}
	
	private static void vb6CompilationUnit(PropertyList _properties) {
		ParseTree ast = null;
		Version version = (Version) _properties.getProperty("VERSION");
		InputStream is = (InputStream) _properties.getProperty(INPUT_STREAM);
        ANTLRInputStream input = null;

        try {
			input = new ANTLRInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	    VisualBasic6Lexer lexer = new VisualBasic6Lexer(input);
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
        
        if(parser.getNumberOfSyntaxErrors() > 0){
        	System.err.format(" ERRORS - %d errors during parsing process%n", parser.getNumberOfSyntaxErrors());
        }
        else {
        	version.getProperties().addProperty("AST", ast);
        	System.err.format("No ERRORS process%n", parser.getNumberOfSyntaxErrors());
        }
	}	
	
	private static void sqlTransactSybase(PropertyList _properties) {
		ParseTree ast = null;
		Version version = (Version) _properties.getProperty("VERSION");
		InputStream is = (InputStream) _properties.getProperty(INPUT_STREAM);
        ANTLRInputStream input = null;

        try {
			input = new ANTLRInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
        SqlTransactSybaseLexer lexer = new SqlTransactSybaseLexer(input);
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
        
        if(parser.getNumberOfSyntaxErrors() > 0){
        	System.err.format(" ERRORS - %d errors during parsing process%n", parser.getNumberOfSyntaxErrors());
        }
        else {
        	version.getProperties().addProperty("AST", ast);
        	System.err.format("No ERRORS process%n", parser.getNumberOfSyntaxErrors());
        }
	}	
	
	public static void main(String args[]) throws Exception {
		InputStream in = new FileInputStream(new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\output" + "\\" + "TEMP.text"));
		Catalog catalog = BicamSystem.readObjectAsJaxbXml(in, Catalog.class);
		PropertyList properties = new PropertyList();
		properties.addProperty("ID", "SEGUROS");
		Repository repository = catalog.getRepository(properties);
		properties.addProperty("ID", "TIFMA001");
		Source source = (Source)repository.getItem(properties);
		if(source == null) {
			properties.addProperty("ID", "SHARED");
			repository = catalog.getRepository(properties);
			properties.addProperty("ID", "TIFMA001");
			source = (Source)repository.getItem(properties);
		}
		System.err.println(source.getId());
		Version version = (Version)source.getItens().iterator().next();
		String dataSourceType = (String) version.getProperties().getProperty("DATA_SOURCE_TYPE");
		String dataSourceLocation = (String) version.getProperties().getProperty("DATA_SOURCE_LOCATION");
		String compilationUnitType = (String) version.getProperties().getProperty("COMPILATION_UNIT_TYPE");
		
		InputStream is = null;
		switch(dataSourceType) {
		case "FILE" : 
			  is = new FileInputStream(new File(dataSourceLocation));
			break;
		case "URL"  : 
			URL url = new URL(dataSourceLocation);
            URLConnection site = url.openConnection();
            is     = site.getInputStream();
			break;
		default:
			BicamSystem.printLog("ERROR", "Invalid DATA SOURCE TYPE: " + dataSourceType);
			break;
		}
		
		PropertyList prop = new PropertyList();
		prop.addProperty("INPUT_STREAM", is);
		prop.addProperty("COMPILATION_UNIT_TYPE", compilationUnitType);
		prop.addProperty("VERSION", version);
		parse(prop);
//===============================================================================		
		PropertyList stProperties = new PropertyList();
		KeywordLanguage keywordLanguage = new KeywordVB6();
		stProperties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		SymbolFactory symbolfactory = new SymbolFactory();
		stProperties.addProperty(SYMBOL_FACTORY, symbolfactory);
		SymbolTable_New st = new SymbolTable_New(stProperties);
		properties.addProperty(SYMBOLTABLE, st);
		
		MetadataVb6 defSymMetada = new MetadataVb6(properties);
		ParseTreeWalker walker = new ParseTreeWalker();
		
		ParseTree tree = (ParseTree)version.getProperties().getProperty("AST");
        walker.walk(defSymMetada, tree);
        
        List<Symbol_New> appSymbolList = st.getSymbolByProperty("CATEGORY","APPLICATION");
        PropertyList propApp = appSymbolList.get(0).getProperties();
//=================================================================================		
//===============================================================================================
	}
}