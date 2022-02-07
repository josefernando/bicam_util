package br.com.bicam.util.app;

import br.com.bicam.util.symboltable.SymbolTable_b;

public class GlobalSpace_b extends MemorySpace_b{
    SymbolTable_b st; 
    public GlobalSpace_b(SymbolTable_b _st) {
		super(null);
		this.st = _st;
		setName("global");
	}
}
