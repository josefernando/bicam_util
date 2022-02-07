package br.com.bicam.util.catalog;

import java.io.IOException;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_b;

public interface DefSymbolVisitor {
	SymbolTable_b visit(MetadataVbParser _metadataParserVb, PropertyList _properties) throws IOException;
	SymbolTable_b visit(CompilationUnitVbParser _compilationUnitVbParser, PropertyList _properties) throws IOException;
}
