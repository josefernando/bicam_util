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

import br.com.bicam.util.PathValue_New;
import br.com.bicam.util.symboltable.Symbol_New;

/** A scope of variable:value pairs */
public class MemorySpace_New {
    String name; // mainly for debugging purposes
    Symbol_New symbol;
    MemorySpace_New parent;
    Map<Symbol_New, PathValue_New> members = new IdentityHashMap<Symbol_New, PathValue_New>();

    public MemorySpace_New(String _name) {
    	this.name = _name; 
    }

    public MemorySpace_New(String _name, MemorySpace_New _parent) {
    	this(_name);
    	this.parent = _parent; 
    }

    public PathValue_New get(Symbol_New id) { return members.get(id); }

    public void put(Symbol_New id, PathValue_New value) { members.put(id, value); }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Symbol_New getSymbol() {
		return symbol;
	}
	
	public MemorySpace_New getParent() {
		return parent;
	}
	
	public void setParent(MemorySpace_New _parent) {
		this.parent = _parent;
	}

	public Map<Symbol_New, PathValue_New> getMembers() {
		return members;
	}
}
