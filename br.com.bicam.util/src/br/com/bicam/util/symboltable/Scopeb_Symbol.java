package br.com.bicam.util.symboltable;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import br.com.bicam.util.BCString;
import br.com.bicam.util.PropertyList;

public class Scopeb_Symbol extends Symbol_b implements Type, IScope
{
	Map<BCString, SymbolList> members;
	Deque<IScope> resolveOrder;
	IScope currentScopeResolveOrder;
	
	IScope compilationUnitScope;
	IScope globalScope;

	public Scopeb_Symbol(PropertyList _properties) {
		super(_properties);
		members = new HashMap<BCString, SymbolList>();
		resolveOrder = new ArrayDeque<IScope>();
	} 

	@Override
	public SymbolList getMemberList(BCString _name) {
		return members.get(_name);	
	}
	
	@Override
	public Map<BCString, SymbolList> getMembers() {
		return members;
	}	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		
		sb.append(this.getName() + "=" 
		       + this.getProperties().toString() + System.lineSeparator());
		
		for(BCString key : members.keySet()){
//			sb.append( key.toString() + " - " + members.get(key).getName() + System.lineSeparator());
			for(Symbol_b sym : getMembers().get(key).symbolList){
				sb.append(sym.toString());
			}
		}
		return sb.toString();
	}	
	
	public Deque<IScope> getResolveOrder(){
		return resolveOrder;
	}
	
	public IScope getCurrentScopeResolveOrder(){
		return currentScopeResolveOrder;
	}
	
	public IScope getScopeByProperty(String _key, String _value){
		if(hasProperty(_key, _value))
			return this;
		else return null;
	}	
	
	public void setCompilationUnitScope(IScope _scope){
		compilationUnitScope = _scope;
	}
	
	public void setGlobalScope(IScope _scope){
		globalScope = _scope;
	}
}
