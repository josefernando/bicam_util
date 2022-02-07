package br.com.bicam.util.app;

import java.util.IdentityHashMap;
/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
***/
import java.util.Map;

import br.com.bicam.util.symboltable.Symbol_b;

/** A scope of variable:value pairs */
public class MemorySpace_b {
    String name; // mainly for debugging purposes
    Symbol_b symbol;
    Map<Symbol_b, Object> members = new IdentityHashMap<Symbol_b, Object>();

    public MemorySpace_b(String _name) {
    	this.name = _name; 
    }

    public Object get(Symbol_b id) { return members.get(id); }

    public void put(Symbol_b id, Object value) { members.put(id, value); }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Symbol_b getSymbol() {
		return symbol;
	}

	public Map<Symbol_b, Object> getMembers() {
		return members;
	}
}
