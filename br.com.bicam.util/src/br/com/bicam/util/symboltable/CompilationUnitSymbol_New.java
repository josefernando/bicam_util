package br.com.bicam.util.symboltable;

import static br.com.bicam.util.constant.PropertyName.NAME;

import br.com.bicam.util.PropertyList;

public  class CompilationUnitSymbol_New extends Symbol_New{

	public CompilationUnitSymbol_New(PropertyList _properties) {
		super(_properties);
	}
	
	public String getId() {
		return (String) getProperty(NAME);
	}
}