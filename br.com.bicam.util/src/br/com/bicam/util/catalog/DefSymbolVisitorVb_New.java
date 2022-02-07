package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class DefSymbolVisitorVb_New implements DefSymbolVisitor_New{

	@Override
    public SymbolTable_New visit(MetadataVbParser_New _parser, PropertyList _properties)  {
		ParseTreeWalker walker = new ParseTreeWalker();
		DefMetadataVb_New defApp = new DefMetadataVb_New(_parser, _properties);
		ParseTree tree;
		tree = _parser.getAST();
        walker.walk(defApp, tree);
        return defApp.getSymbolTable();
    }

	@Override
	public SymbolTable_New visit(CompilationUnitVbParser_New _parser, PropertyList _properties) {
		ParseTreeWalker walker = new ParseTreeWalker();
		DefCompilationUnitSymbolVb6_New defCompUnit = new DefCompilationUnitSymbolVb6_New(_parser, _properties);
        
//		if(!defCompUnit.IsValidCompUnit()) return defCompUnit.getSymbolTable();
		
		ParseTree tree;
		tree = _parser.getAST();
        walker.walk(defCompUnit, tree);
        return defCompUnit.getSymbolTable();
	}
}
