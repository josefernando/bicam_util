package br.com.bicam.util.catalog;

import java.io.IOException;

import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_b;

public class SetAssignmentVisitorImpl implements SetAssignmentVisitor{

	@Override
    public SymbolTable_b visit(MetadataVbParser _parser, PropertyList _properties) throws IOException {
        // Do nothing
		return (SymbolTable_b)_properties.getProperty("SYMBOLTABLE");
	}

	@Override
	public SymbolTable_b visit(CompilationUnitVbParser _parser, PropertyList _properties) throws IOException {
        ParseTreeWalker walker = new ParseTreeWalker();
        SetAssignmentVb assigmentSym = new SetAssignmentVb((SymbolTable_b)_properties.getProperty("SYMBOLTABLE"));
        walker.walk(assigmentSym, _parser.getAST()); 
		return (SymbolTable_b)_properties.getProperty("SYMBOLTABLE");
	}
}