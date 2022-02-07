package br.com.bicam.util.symboltable;

import br.com.bicam.util.PropertyList;

public interface DbType {
	default public Symbol_b getBaseSymbol(PropertyList _properties){
		return (Symbol_b)_properties.getProperty("BASE_SYMBOL");
	}
}
