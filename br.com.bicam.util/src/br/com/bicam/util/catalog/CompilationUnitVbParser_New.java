package br.com.bicam.util.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import br.com.bicam.parser.visualbasic6.VisualBasic6Lexer;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class CompilationUnitVbParser_New extends Parser_New{

	public CompilationUnitVbParser_New(File _file) {
		super(_file);
	}

	@Override
	protected void parse() {
		InputStream is = null;
        ANTLRInputStream input = null;

		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        ANTLRInputStream input;
		try {
			input = new ANTLRInputStream(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	}

	@Override
	public SymbolTable_New accept(DefSymbolVisitor_New _visitor, PropertyList _properties)  {
		return _visitor.visit(this, _properties);
	}

/*	@Override
	public SymbolTable_New accept(DefSymbolVisitor_New _visitor, PropertyList _properties) throws IOException {
		return _visitor.visit(this, _properties);
	}*/
	
	@Override
	public SymbolTable_New accept(RefTypeSymbolVisitor_New _visitor, PropertyList _properties) {
			return _visitor.visit(this, _properties);
	}
	
	@Override
	public SymbolTable_New accept(RefSymbolVisitor_New _visitor, PropertyList _properties)  {
		return _visitor.visit(this, _properties);
	}
	
	/*	
	@Override
	public SymbolTable_New accept(SetAssignmentVisitor_New _visitor, PropertyList _properties) throws IOException {
		return _visitor.visit(this, _properties);
	}*/	
}
