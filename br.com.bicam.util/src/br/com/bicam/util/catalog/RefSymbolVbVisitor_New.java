package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class RefSymbolVbVisitor_New implements RefSymbolVisitor_New{

	@Override
    public SymbolTable_New visit(MetadataVbParser_New _parser, PropertyList _properties)  {
		return null;
    }

	@Override
	public SymbolTable_New visit(CompilationUnitVbParser_New _parser, PropertyList _properties) {
		ParseTreeWalker walker = new ParseTreeWalker();
		RefSymbolVb6_New refType = new RefSymbolVb6_New(_parser, _properties);
		ParseTree tree;
		tree = _parser.getAST();
        walker.walk(refType, tree);
        return refType.getSymbolTable();
	}
}
