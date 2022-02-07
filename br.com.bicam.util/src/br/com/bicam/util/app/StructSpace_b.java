package br.com.bicam.util.app;

import br.com.bicam.util.symboltable.Symbol_b;

/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
***/
/** A scope holding fields of a struct instance.  There can be
 *  multiple struct instances but only one StructSymbol (definition).
 */
public class StructSpace_b extends MemorySpace_b {
    Symbol_b def; // what kind of struct am I?

    public StructSpace_b(Symbol_b _struct) {
		super(_struct.getName());
        this.def = _struct;
	}
}
