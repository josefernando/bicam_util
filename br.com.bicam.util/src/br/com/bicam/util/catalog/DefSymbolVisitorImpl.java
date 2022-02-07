package br.com.bicam.util.catalog;

import java.io.IOException;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;

public class DefSymbolVisitorImpl implements DefSymbolVisitor{

	@Override
    public SymbolTable_b visit(MetadataVbParser _parser, PropertyList _properties) throws IOException {
		ParseTreeWalker walker = new ParseTreeWalker();
		DefMetadataVb defApp = new DefMetadataVb(_parser, _properties);
		ParseTree tree;
		tree = _parser.getAST();
        walker.walk(defApp, tree);
        return defApp.getSymbolTable();
    }

	@Override
	public SymbolTable_b visit(CompilationUnitVbParser _parser, PropertyList _properties) throws IOException {
		ParseTreeWalker walker = new ParseTreeWalker();
		
		_properties.addProperty("COMPILATION_UNIT", _parser.getFileName());
        
		GetVbName getVbName = new GetVbName(); 
		
        walker.walk(getVbName, _parser.getAST());  

		String vbName = getVbName.getVbName();
		
        DefCompilationUnitSymbolVb6 defCompUnit = new DefCompilationUnitSymbolVb6(vbName,_properties);
        walker.walk(defCompUnit, _parser.getAST());  
        
        SymbolTable_b st = (SymbolTable_b)_properties.getProperty("SYMBOLTABLE");
        
   	 	Symbol_b compUnitSym = st.getSymbolByProperty("COMPILATION_UNIT", _parser.getFileName()).get(0);
   	 	compUnitSym.addProperty("PARSE_TREE", _parser.getAST());

   	 	return defCompUnit.getSymbolTable();		
	}
}
