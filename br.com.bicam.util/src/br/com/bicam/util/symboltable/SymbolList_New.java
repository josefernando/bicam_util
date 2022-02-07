package br.com.bicam.util.symboltable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.bicam.util.StringOptionalCase;

public class SymbolList_New {
	StringOptionalCase name;
	boolean caseSensitive;
	Set<Symbol_New> symbolList;
	
	public SymbolList_New(String _name, boolean _caseSensitive){
		name = new StringOptionalCase(_name, _caseSensitive);
		caseSensitive = _caseSensitive;
		symbolList = new HashSet<Symbol_New>();
	}
	
	public String getName(){
		return name.toString();
	}
	
	public void add(Symbol_New _sym){
		symbolList.add(_sym);
	}
	
	public void remove(Symbol_New _sym){
		symbolList.remove(_sym);
	}
	
	public List<Symbol_New> getSymbols(){
		List<Symbol_New> list = new  ArrayList<Symbol_New>();
		for(Symbol_New sym : symbolList){
			list.add(sym);
		}
		return list;
	}
	
	public boolean isCaseSensitive(){
		return caseSensitive;
	}
	
	public int size(){
		return symbolList.size();
	}	
}
