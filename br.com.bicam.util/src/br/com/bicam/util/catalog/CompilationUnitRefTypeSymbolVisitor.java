package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class CompilationUnitRefTypeSymbolVisitor implements ICompilationUnitVisitor{
	
	SymbolTable_New st;
	PropertyList properties;

	@Override
	public void visit(CompilationUnitVb6 compUnitVb6) {
		st = (SymbolTable_New) compUnitVb6.getProperties().getProperty("SYMBOLTABLE");
		PropertyList properties = compUnitVb6.getProperties();
		
		System.err.println("REF TYPE SYMBOL VB6 - " + compUnitVb6.getFileName());

		
		ParseTreeWalker walker = new ParseTreeWalker();
		CompilationUnitRefTypeSymbolVb6 refType = new CompilationUnitRefTypeSymbolVb6(properties);
		ParseTree tree;
		tree = (ParseTree) compUnitVb6.getProperties().getProperty("AST");
        walker.walk(refType, tree);
	}

	@Override
	public void visit(CompilationUnitSqlTransactSybase compUnitSqlTransactSybase) {
		st = (SymbolTable_New) compUnitSqlTransactSybase.getProperties().getProperty("SYMBOLTABLE");
		PropertyList properties = compUnitSqlTransactSybase.getProperties();
		System.err.println("REF TYPE SYMBOL TRANSACT SYBASE - " + compUnitSqlTransactSybase.getFileName());
		ParseTreeWalker walker = new ParseTreeWalker();
		CompilationUnitRefTypeSymbolSqlTransactSybase refType = new CompilationUnitRefTypeSymbolSqlTransactSybase(properties);
		ParseTree tree;
		tree = (ParseTree) compUnitSqlTransactSybase.getProperties().getProperty("AST");
        walker.walk(refType, tree);
	}
}