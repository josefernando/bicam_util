package br.com.bicam.util.catalog;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public interface RefTypeSymbolVisitor_New {
	SymbolTable_New visit(MetadataVbParser_New _metadataParserVb, PropertyList _properties);
	SymbolTable_New visit(CompilationUnitVbParser_New _compilationUnitVbParser, PropertyList _properties);
}
