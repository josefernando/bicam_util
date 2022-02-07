package br.com.bicam.util.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;

import br.com.bicam.parser.visualbasic6.VisualBasic6Lexer;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.util.PropertyList;

public class CompilationUnitParserVb6 {
	File file;
	ParseTree ast;
	public CompilationUnitParserVb6(PropertyList _properties) {
		this.file = (File)_properties.getProperty("FILE");
		this.ast = parser();
	}
	
	public ParseTree getAst() {
		return ast;
	}
	
	private ParseTree parser() {
		InputStream is = null;
	    ANTLRInputStream input = null;
	    ParseTree ast;

		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
	    return ast;
	}	
}