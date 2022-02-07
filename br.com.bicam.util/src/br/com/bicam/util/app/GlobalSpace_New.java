package br.com.bicam.util.app;

import br.com.bicam.util.symboltable.SymbolTable_New;

public class GlobalSpace_New extends MemorySpace_New{
    SymbolTable_New st; 
    public GlobalSpace_New(SymbolTable_New _st) {
		super(null);
		this.st = _st;
		setName("global");
	}
}
