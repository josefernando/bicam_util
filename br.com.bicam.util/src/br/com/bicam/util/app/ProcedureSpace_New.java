package br.com.bicam.util.app;

import br.com.bicam.util.symboltable.Symbol_New;

/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
***/
/** A function invocation scope; stores arguments and locals */
public class ProcedureSpace_New extends MemorySpace_New {
    Symbol_New def; // what function are we executing?
    public ProcedureSpace_New(Symbol_New _methodSymbol) {
		super(_methodSymbol.getName());
        this.def = _methodSymbol;
	}
}
