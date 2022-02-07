package br.com.bicam.util.symboltable;

import br.com.bicam.util.PropertyList;

public  class ProcedureSymbol_New extends Symbol_New{

	public ProcedureSymbol_New(PropertyList _properties) {
		super(_properties);
	}
	
	public String getId() {
		Symbol_New ownerSym = (Symbol_New) getEnclosingScope();
		Symbol_New dbSym = (Symbol_New) ownerSym.getEnclosingScope();
		Symbol_New serverSym = (Symbol_New) dbSym.getEnclosingScope();

		String id = serverSym.getId() + dbSym.getId() + ownerSym.getId() + getName();
		
		return id;
	}
}