package br.com.bicam.util.symboltable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.bicam.util.BCString;

public class SymbolList {
	BCString name;
	boolean caseSensitive;
	Set<Symbol_b> symbolList;
	
	public SymbolList(String _name, boolean _caseSensitive){
		name = new BCString(_name, _caseSensitive);
		caseSensitive = _caseSensitive;
		symbolList = new HashSet<Symbol_b>();
	}
	
	public String getName(){
		return name.toString();
	}
	
	public void add(Symbol_b _sym){
		symbolList.add(_sym);
	}
	
	public void remove(Symbol_b _sym){
		symbolList.remove(_sym);
	}
	
	public List<Symbol_b> getSymbols(){
		List<Symbol_b> list = new  ArrayList<Symbol_b>();
		for(Symbol_b sym : symbolList){
			list.add(sym);
		}
		return list;
	}
	
	public boolean equals(Symbol_b _otherSymbol, Type _symbolType){
		if((new BCString(_otherSymbol.getName(),caseSensitive)).equals(name)){
			if(symbolList.size() == 1){
				return true;
			}
			else {
				for(Symbol_b sym : symbolList){
					if(sameType(sym.getType(), _symbolType)) return true;
				}
			}
		}
		return false;
	}
	
	private boolean sameType(Type _this, Type _other){
		return _this.getClass().getName().equals(_other.getClass().getName());
	}
	
	public boolean isCaseSensitive(){
		return caseSensitive;
	}
	
	public int size(){
		return symbolList.size();
	}	
}
