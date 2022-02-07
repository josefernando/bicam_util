package br.com.bicam.util.symboltable;

import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY_TYPE;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.PARENT_SCOPE;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.TYPE;
import static br.com.bicam.util.constant.PropertyName.VISIBILITY_SCOPE;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.model.util.SymbolSerializable;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.StringOptionalCase;

public abstract class Symbol_New  implements IScope_New, Type_New{
	private PropertyList properties;
	Map<StringOptionalCase, Member> members;
	
	public Symbol_New(PropertyList _properties){
  		properties = _properties;
		members = new HashMap<StringOptionalCase, Member>();
		defineScope();
	}
	
	/*
	 * Este método deve listar os fields necessários na criação do object
	 */
	public String getName(){
			return (String) getProperties().getProperty(NAME);
	}
	
	public String getId() {
		if(getEnclosingScope() instanceof VirtualSymbol) return getName(); //Scope
		return ((Symbol_New)getEnclosingScope()).getId() + "." + getName();
	}
	
	public ParserRuleContext getContext(){
		return (ParserRuleContext) getProperties().getProperty(CONTEXT);
	}
	
	public Type_New getType(){
		if(getProperties().getProperty(TYPE) instanceof Type)
			return (Type_New) getProperties().getProperty(TYPE);
	    return null;
	}

	public void setType( Type_New _type){
		getProperties().addProperty(TYPE, _type);
	}
	
	public void addProperty(String propertyDescriptionP, Object valueP) {
		getProperties().addProperty(propertyDescriptionP, valueP);
	}
	
	public void addProperty(String propertyDescriptionP, Object valueP, boolean _replace) {
		getProperties().addProperty(propertyDescriptionP, valueP, _replace);
	}
	
	public void removeProperty(String propertyDescriptionP) {
		getProperties().removeProperty(propertyDescriptionP);
	}
	
	public Object getProperty(String propertyDescriptionP) {
		return getProperties().getProperty(propertyDescriptionP);
	}
	
	public boolean hasProperty(String _key, String _val) {
		return getProperties().hasProperty(_key, _val);
	}

	public PropertyList getProperties() {
		return properties;
	}
	
/*	public String toString(){
		StringBuffer sb = new StringBuffer();
		Symbol_New symAux = null;
        if(getProperty("ENCLOSING_SCOPE_NAME") != null) {
        	sb.append( getProperty("ENCLOSING_SCOPE_NAME") + " -> ");
        }  
		sb.append(this.getName() + "=" 
		       + this.getProperties().toString() + System.lineSeparator());
		
		for(Member member : getMembers().values()){
			member.findSymbol();
			symAux = member.getNextSymbol();
			sb.append(symAux.toString());
			while(member.hasNext()) {
				symAux = member.getNextSymbol();
				sb.append(", " + symAux.toString());
			}
		}
		return sb.toString();
	}*/
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		Symbol_New symAux = null;
/*        if(getProperty("ENCLOSING_SCOPE_NAME") != null) {
        	sb.append( getProperty("ENCLOSING_SCOPE_NAME") + " -> ");
        }*/  
		sb.append(this.getName() + "=" 
		       + this.getProperties().toString() + System.lineSeparator());
		
		for(Member member : getMembers().values()){
			member.findSymbol();
			symAux = member.getNextSymbol();
			sb.append(symAux.toString());
			while(member.hasNext()) {
				symAux = member.getNextSymbol();
				sb.append(", " + symAux.toString());
			}
		}
		return sb.toString();
	}	

	public Map<StringOptionalCase, Member> getMembers() {
		return members;
	}
	
//	@Override
	public int defineScope() {
		defineEnclosingScope();
		defineVisibilityScope();
		defineParentScope();
		return -1;
	}
	
	@Override
	public int defineVisibilityScope() {
		IScope_New scope = null;
		if(getProperties().hasProperty(VISIBILITY_SCOPE)) {
			scope = (IScope_New)getProperties().getProperty(VISIBILITY_SCOPE);
		}
		else {
			return -1;
		}
		if(getProperties().getProperty(VISIBILITY_SCOPE) == getProperties().getProperty(ENCLOSING_SCOPE)){
			return -1;
		}		
		StringOptionalCase symbolName = new StringOptionalCase(getName(),((KeywordLanguage)getProperty(KEYWORD_LANGUAGE)).isCaseSensitive());
		Member members = scope.getMembers().get(symbolName);
		if(members ==  null){
			members = new Member(symbolName, this);
			scope.getMembers().put(symbolName, members);
		}
		else {
			System.err.format("*** WARNING - Define duplicate SYMBOL NAME '%s' for in VISIBILITY SCOPE '%s' in COMPILATION UNIT '%s'%n",
			          getName(), scope.getName(),  ((SymbolTable_New)properties.getProperty(SYMBOLTABLE)).getCompilarionUnitSymbol(getContext()).getName());
            String locationOfDuplicate = Integer.toString(members.getFirstSymbol().getContext().start.getLine()) + " ";
			while(members.hasNext()) {
				locationOfDuplicate = locationOfDuplicate + Integer.toString(members.getNextSymbol().getContext().start.getLine()) + " ";
			}
			locationOfDuplicate = locationOfDuplicate + Integer.toString(this.getContext().start.getLine()) + " ";
			System.err.format("LINES OF DUPLICATION IN COMPILATION UNIT: %s%n", locationOfDuplicate);
			members.add(this);
			System.err.println(duplicateSymbolInScope(members));
			try {
				throw new Exception();
			}catch(Exception e) {
//				e.printStackTrace();
			}
		}
		return scope.getMembers().size();
	}	

	@Override
	public int defineEnclosingScope() { // Escopo no qual está definido
		IScope_New scope = null;
		if(getProperties().getProperty(ENCLOSING_SCOPE) != null) {
			scope = (IScope_New)getProperties().getProperty(ENCLOSING_SCOPE);
		}
		else {
			if(getProperties().hasProperty("CATEGORY_TYPE", "GLOBAL")) return -1;
			else {
				if(getProperties().getProperty(PARENT_SCOPE) != null) {
					return -1;
				}
				try {
					throw new  Exception();
				}
				catch(Exception e) {
					System.err.println("*** ERROR: ENCLOSING_SCOPE IS NULL AND SYMBOL IS NOT GLOBAL SCOPE");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		StringOptionalCase symbolName = new StringOptionalCase(getName(),((KeywordLanguage)getProperty(KEYWORD_LANGUAGE)).isCaseSensitive());
		Member members = scope.getMembers().get(symbolName);
		if(members ==  null){
			members = new Member(symbolName, this);
			scope.getMembers().put(symbolName, members);
		}
		else {
			if(this.getProperty("Index") == null                   // Não é array na tela para VB6. Melhorar solução
			   && !isDupNameAllowed(members)) { // Exe. Type com mesmo nome de variável
				System.err.format("*** WARNING - Define duplicate symbol name '%s'  in ENCLOSING SCOPE '%s' in COMPILATION UNIT '%s'%n",
				          getName(), scope.getName(), ((SymbolTable_New)properties.getProperty(SYMBOLTABLE)).getCompilarionUnitSymbol(getContext()).getName());
	            String locationOfDuplicate = Integer.toString(members.getFirstSymbol().getContext().start.getLine()) + " ";
				while(members.hasNext()) {
					locationOfDuplicate = locationOfDuplicate + Integer.toString(members.getNextSymbol().getContext().start.getLine()) + " ";
				}
				
				locationOfDuplicate = locationOfDuplicate + Integer.toString(this.getContext().start.getLine()) + " ";
				System.err.format("LINES OF DUPLICATION IN COMPILATION UNIT: %s%n", locationOfDuplicate);
				try {
					throw new Exception();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			members.add(this);
		}
		return scope.getMembers().size();
	}
	
	@Override
	public int defineParentScope() { // onde pode ser referido. Exemplo: <formName>.<labelName>, onde 
		                             // <label> tem como enclosing escope um frame.
		IScope_New scope = null;
		if(getProperties().hasProperty(PARENT_SCOPE)) {
			scope = (IScope_New)getProperties().getProperty(PARENT_SCOPE);
		}
		else {
			return -1;
		}
		if(getProperties().getProperty(PARENT_SCOPE) == getProperties().getProperty(ENCLOSING_SCOPE)){
			return -1;
		}		
		StringOptionalCase symbolName = new StringOptionalCase(getName(),((KeywordLanguage)getProperty(KEYWORD_LANGUAGE)).isCaseSensitive());
		Member members = scope.getMembers().get(symbolName);
		if(members ==  null){
			members = new Member(symbolName, this);
			scope.getMembers().put(symbolName, members);
		}
		else {
			System.err.format("*** WARNING - Define duplicate SYMBOL NAME '%s' for in PARENT SCOPE '%s' in COMPILATION UNIT '%s'%n",
			          getName(), scope.getName(), ((SymbolTable_New)properties.getProperty(SYMBOLTABLE)).getCompilarionUnitSymbol(getContext()).getName());
            String locationOfDuplicate = Integer.toString(members.getFirstSymbol().getContext().start.getLine()) + " ";
			while(members.hasNext()) {
				locationOfDuplicate = locationOfDuplicate + Integer.toString(members.getNextSymbol().getContext().start.getLine()) + " ";
			}
			
			locationOfDuplicate = locationOfDuplicate + Integer.toString(this.getContext().start.getLine()) + " ";
			System.err.format("LINES OF DUPLICATION IN COMPILATION UNIT: %s%n", locationOfDuplicate);

			members.add(this);
			System.err.println(duplicateSymbolInScope(members));
			try {
				throw new Exception();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return scope.getMembers().size();
	}
	
	public SymbolSerializable getSerializable() {
		SymbolSerializable propListSer = new SymbolSerializable();
		    for (Map.Entry<String, Object> entry : this.getProperties().getProperties().entrySet()) {
		    	if(entry.getValue() instanceof String) {
		    		propListSer.getProperties().put(entry.getKey(), (String)entry.getValue());
		    	}
		    	else if(entry.getValue() instanceof PropertyList) {
		    		PropertyList propw = (PropertyList)entry.getValue();
		    		propListSer.getProperties().put(entry.getKey(), propw.getPropertyListSerializable());
		    	}
		    }
			return propListSer;
	}	
	
	private String duplicateSymbolInScope(Member _members) {
		StringBuffer sb = new StringBuffer();
		_members.findSymbol();
		while (_members.hasNext()) {
			Symbol_New sym = _members.getNextSymbol();
			int line = sym.getContext().start.getLine();
			String name = sym.getName();
			String scopeName = sym.getEnclosingScope().getName();
			sb.append("SCOPE: " + sym.getEnclosingScope().getName()
					  + " SYMBOL NAME: " + sym.getName() 
					  + " PROPERTIES: " + sym.getProperties().toString()
					  + " Line: " + sym.getContext().start.getLine()
					  + System.lineSeparator());
		}
		return sb.toString();
	}
	
	private boolean isDupNameAllowed(Member _members) {
		Symbol_New sym = _members.getFirstSymbol();
		String category = (String)getProperty(CATEGORY);
		String categoryType = (String)getProperty(CATEGORY_TYPE);
		String otherCategory = (String)sym.getProperty(CATEGORY);
		String otherCategoryType = (String)sym.getProperty(CATEGORY_TYPE);
		if(category.equals(otherCategory) && categoryType.equals(otherCategoryType)) return false;
		while(_members.hasNext()) {
			sym = _members.getNextSymbol();
			otherCategory = (String)sym.getProperty(CATEGORY);
			otherCategoryType = (String)sym.getProperty(CATEGORY_TYPE);	
			if(category.equals(otherCategory) && categoryType.equals(otherCategoryType)) return false;
		}
		return true;
	}
}