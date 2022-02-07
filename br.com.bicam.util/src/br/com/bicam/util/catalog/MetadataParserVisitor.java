package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.COMPILATION_UNIT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;

import br.com.bicam.parser.visualbasic6.VbpLexer;
import br.com.bicam.parser.visualbasic6.VbpParser;
import br.com.bicam.util.PropertyList;

public class MetadataParserVisitor implements MetadataVisitor {
	
	PropertyList properties;
	File file;
	ParseTree ast;

	public MetadataParserVisitor(PropertyList _properties) {
		this.properties = _properties;
		this.file = (File)_properties.getProperty("FILE");
		properties.addProperty(COMPILATION_UNIT, file);
	}

	@Override
	public void visit(MetadataProjectVb6 metadataVb6) {
		System.err.println("Parsing Project Vb6");
		properties.addProperty("AST",parseProjectVbp());
	}

	private ParseTree parseProjectVbp() {
		InputStream is = null;
        ANTLRInputStream input = null;

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
        
        return ast;
	}
}
