package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class CompilationUnitDefSymbolVisitor implements ICompilationUnitVisitor{
	
	SymbolTable_New st;
	PropertyList properties;

	@Override
	public void visit(CompilationUnitVb6 compUnitVb6) {
		st = (SymbolTable_New) compUnitVb6.getProperties().getProperty("SYMBOLTABLE");
		PropertyList properties = compUnitVb6.getProperties();

		System.err.println("DEF SYMBOL VB6 - " + compUnitVb6.getFileName());
		ParseTreeWalker walker = new ParseTreeWalker();
		CompilationUnitDefSymbolVb6 defCompUnit = new CompilationUnitDefSymbolVb6(properties);
		
		ParseTree tree;
		tree = (ParseTree) compUnitVb6.getProperties().getProperty("AST");
        walker.walk(defCompUnit, tree);
	}

	@Override
	public void visit(CompilationUnitSqlTransactSybase compUnitSqlTransactSybase) {
		st = (SymbolTable_New) compUnitSqlTransactSybase.getProperties().getProperty("SYMBOLTABLE");
		PropertyList properties = compUnitSqlTransactSybase.getProperties();

		System.err.println("DEF SYMBOL TRANSACT SYBASE - " + compUnitSqlTransactSybase.getFileName());
		ParseTreeWalker walker = new ParseTreeWalker();
		CompilationUnitDefSymbolSqlTransactSybase defCompUnit = new CompilationUnitDefSymbolSqlTransactSybase(properties);
		
		ParseTree tree;
		tree = (ParseTree) compUnitSqlTransactSybase.getProperties().getProperty("AST");
        walker.walk(defCompUnit, tree);		
	}
}