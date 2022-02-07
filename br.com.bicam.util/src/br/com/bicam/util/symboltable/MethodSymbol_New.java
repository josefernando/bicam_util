package br.com.bicam.util.symboltable;

import br.com.bicam.util.PropertyList;

public  class MethodSymbol_New extends Symbol_New{

	public MethodSymbol_New(PropertyList _properties) {
		super(_properties); 
	}
	
/*	public String getId() {
		Symbol_New sym = (Symbol_New) getProperties().getProperty(ENCLOSING_SCOPE);
		if(sym == null) {
			BicamSystem.printLog("ERROR", "ENCLOSING_SCOPE IS NULL");
		}
		
		return sym.getId() + ":" + getName();
	}*/
}