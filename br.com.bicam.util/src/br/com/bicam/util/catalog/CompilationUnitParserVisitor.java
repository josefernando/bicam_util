package br.com.bicam.util.catalog;

import java.io.File;
import java.io.IOException;

public class CompilationUnitParserVisitor implements ICompilationUnitVisitor{
	
	@Override
	public void visit(CompilationUnitVb6 compUnitVb6) {
		System.err.println("PARSING VB6");
		try {
			System.err.println("Parsing CompUnit:" + ((File)compUnitVb6.getProperties().getProperty("FILE")).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		compUnitVb6.getProperties().addProperty("AST", parseVb6((File)compUnitVb6.getProperties().getProperty("FILE")));
		compUnitVb6.getProperties().addProperty("AST", new CompilationUnitParserVb6(compUnitVb6.getProperties()).getAst());
	}

	@Override
	public void visit(CompilationUnitSqlTransactSybase compUnitSqlTransactSybase) {
		System.err.println("PARSING SQL TRANSACT SYBASE");
		try {
			System.err.println("Parsing CompUnit:" + ((File)compUnitSqlTransactSybase.getProperties().getProperty("FILE")).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		compUnitSqlTransactSybase.getProperties().addProperty("AST", parseSybase((File)compUnitSqlTransactSybase.getProperties().getProperty("FILE")));
		compUnitSqlTransactSybase.getProperties().addProperty("AST", new CompilationUnitParserSqlTransactSybase(compUnitSqlTransactSybase.getProperties()).getAst());
	}
	
/*	private ParseTree parseVb6(File _file) {
		InputStream is = null;
        ANTLRInputStream input = null;
        ParseTree ast;

		try {
			is = new FileInputStream(_file);
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
	}*/
	
/*	private ParseTree parseSybase(File _file) {
		InputStream is = null;
        ANTLRInputStream input = null;
        ParseTree ast;

		try {
			is = new FileInputStream(_file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
        return ast;
	}*/	
}
