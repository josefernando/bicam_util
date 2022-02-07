package br.com.bicam.util.symboltable;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import br.com.bicam.parser.constant.PropertyName;
import br.com.bicam.util.BCString;
import br.com.bicam.util.PropertyList;

public class Base_b_Scope implements IScope, PropertyName {
	private PropertyList properties;

	Map<BCString, SymbolList> members;
	Deque<IScope> resolveOrder;
	IScope currentScopeResolveOrder;	
	
	IScope compilationUnitScope;
	IScope globalScope;

	public Base_b_Scope(PropertyList _propertyList) {
		members = new HashMap<BCString, SymbolList>();
		properties = _propertyList;
    }

	@Override
	public String getName() {
		return (String) getProperties().getProperty(NAME);
	}	

	@Override
	public IScope getEnclosingScope() {
		return (IScope) getProperties().getProperty(ENCLOSING_SCOPE);
	}

	@Override
	public IScope getParentScope() {
		return (IScope) getProperties().getProperty(PARENT_SCOPE);
	}

	@Override
	public IScope getGlobalScope() {
		if(getProperties().getProperty(GLOBAL_SCOPE) == null)
			return this;
		else return (IScope) getProperties().getProperty(GLOBAL_SCOPE);
	}	

	@Override
	public SymbolList getMemberList(BCString _name) {
		return members.get(_name);	
	}
	
	@Override
	public Map<BCString, SymbolList> getMembers() {
		return members;
	}	

	@Override
	public void addProperty(String _key, Object _value) {
		getProperties().addProperty(_key, _value);
	}

	@Override
	public Object getProperty(String _key) {
		return properties.getProperty(_key);
	}

	@Override
	public PropertyList getProperties() {
		return properties;
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

	public void removeProperty(String propertyDescriptionP) {
		properties.removeProperty(propertyDescriptionP);
	}
	
	public boolean hasProperty(String _key, String _val) {
		return properties.hasProperty(_key, _val);
	}
	
	public Deque<IScope> getResolveOrder(){
		return resolveOrder;
	}
	
	public IScope getCurrentScopeResolveOrder(){
		return currentScopeResolveOrder;
	}

	@Override
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
