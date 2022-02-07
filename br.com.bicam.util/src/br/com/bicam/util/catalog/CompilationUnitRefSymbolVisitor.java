package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class CompilationUnitRefSymbolVisitor implements ICompilationUnitVisitor{
	
	SymbolTable_New st;
	PropertyList properties;

	@Override
	public void visit(CompilationUnitVb6 compUnitVb6) {
		st = (SymbolTable_New) compUnitVb6.getProperties().getProperty("SYMBOLTABLE");
		PropertyList properties = compUnitVb6.getProperties();
		
		System.err.println("REF SYMBOL VB6 - " + compUnitVb6.getFileName());

		
		ParseTreeWalker walker = new ParseTreeWalker();
		CompilationUnitRefSymbolVb6 refSymbol = new CompilationUnitRefSymbolVb6(properties);
		ParseTree tree;
		tree = (ParseTree) compUnitVb6.getProperties().getProperty("AST");
        walker.walk(refSymbol, tree);
	}

	@Override
	public void visit(CompilationUnitSqlTransactSybase compUnitSqlTransactSybase) {
		st = (SymbolTable_New) compUnitSqlTransactSybase.getProperties().getProperty("SYMBOLTABLE");
		PropertyList properties = compUnitSqlTransactSybase.getProperties();
		
		System.err.println("REF SYMBOL TRANSACT SYBASE - " + compUnitSqlTransactSybase.getFileName());
		
		ParseTreeWalker walker = new ParseTreeWalker();
		CompilationUnitRefSymbolSqlTransactSybase refSymbol = new CompilationUnitRefSymbolSqlTransactSybase(properties);
		ParseTree tree;
		tree = (ParseTree) compUnitSqlTransactSybase.getProperties().getProperty("AST");
        walker.walk(refSymbol, tree);		
	}
}