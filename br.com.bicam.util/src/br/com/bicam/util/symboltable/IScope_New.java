package br.com.bicam.util.symboltable;

import static br.com.bicam.util.constant.PropertyName.ALIAS;
import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY_TYPE;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.EXPLICIT;
import static br.com.bicam.util.constant.PropertyName.IMPLICIT;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.PARENT_SCOPE;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_TYPE;
import static br.com.bicam.util.constant.PropertyName.THIS;
import static br.com.bicam.util.constant.PropertyName.VARIABLE;
import static br.com.bicam.util.constant.PropertyName.WHERE_USED;

import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.StringOptionalCase;
import br.com.bicam.util.constant.SymbolType_New;

public interface IScope_New {
	public String getName();
	public Map<StringOptionalCase, Member> getMembers();
	public int defineScope();
	public int defineEnclosingScope();
	public int defineParentScope();
	public int defineVisibilityScope();
	public PropertyList getProperties();
	public Object getProperty(String propertyDescriptionP);
	
	default public IScope_New getEnclosingScope(){
		return (IScope_New) getProperties().getProperty(ENCLOSING_SCOPE);
	}
	
	default public IScope_New getParent(){
		if(getProperties().hasProperty(PARENT_SCOPE))
			return (IScope_New) getProperties().getProperty(PARENT_SCOPE);
		else return null;
	}
	
	default public Symbol_New resolve(PropertyList _propertyList){
		String memberAccessOper = "\\."; //(String)_propertyList.getProperty("MEMBER_ACCESS_OPER");
		String name = (String)_propertyList.getProperty(NAME);
		String[] qualifiedName = name.split(memberAccessOper);

		if(qualifiedName.length == 1) {
			Member member = getMembers().get(getStringOptionCase(name, _propertyList));
			if(member != null && member.size() > 0){
				return setUsed(resolveAmbiguity(member, _propertyList), _propertyList);
			}
			else if (getEnclosingScope() != null){
				
				Symbol_New s = getEnclosingScope().resolve(_propertyList);
				if(s != null) {
					if(s.getProperty("SYMBOL") != null && s.hasProperty("CATEGORY", "ALIAS")) {
						s = (Symbol_New) s.getProperty("SYMBOL");
						
					}
					return setUsed(s, s.getProperties());
				}
				return null;
			}
			else if(isDefModeImplicit(_propertyList)) {
				return setUsed(createImplicitSymbol(_propertyList), _propertyList);
			}
			else if(isDefModeInfer(_propertyList)) {
				return setUsed(createInferSymbol(_propertyList), _propertyList);
			}
			else if(isDefModeInferDatabase(_propertyList)) {
				return setUsed(createInferDatabaseSymbol(_propertyList, true), _propertyList);
			}			
			else if( _propertyList.hasProperty(DEF_MODE, "BUILTIN_ATTRIBUTE")) {
				_propertyList.addProperty(CATEGORY_TYPE, "ATTRIBUTE");
				_propertyList.addProperty("VALUE", _propertyList.getProperty("VALUE"));
				return setUsed(createBuiltinSymbol(_propertyList), _propertyList);
			}
			else return null;
		}
		
		// Membros não podem ter criação de variáveis implicita
		// ex.: em 'Form.Show', Form não pode ser criado implicitamente
		_propertyList.addProperty(DEF_MODE, EXPLICIT);
		
		StringBuffer scopeToResolve = new StringBuffer();
		scopeToResolve.append(qualifiedName[0]);
		for(int i=1; i < qualifiedName.length - 1; i++){
			scopeToResolve.append("." + qualifiedName[i]);
		}
		
		PropertyList propScope = _propertyList.getCopy();
		PropertyList propMember = _propertyList.getCopy();

		propScope.addProperty(NAME, scopeToResolve.toString());
		propMember.addProperty(NAME, qualifiedName[qualifiedName.length - 1]);
		
		Symbol_New s = null;
		try {
		 s = ((IScope_New)resolve(propScope)).resolveMember(propMember);
		} catch (NullPointerException e) {
			ParserRuleContext context = (ParserRuleContext) getProperty(CONTEXT);
			SymbolTable_New  st = (SymbolTable_New) getProperty(SYMBOLTABLE);
			System.err.format("*** ERROR: SCOPE NAME '%s' NOT RESOLVED in COMPILATION UNIT '%s' in line %d%n"
	                ,getProperty(NAME), st.getCompilarionUnitSymbol(context).getName()
	                , context.start.getLine());
			return s;
		}
		
		if(s == null) { // tente resover com data_Type
			            // ex.: Private proInfo As PROCESS_INFORMATION  
			            //      proInfo.hProcess
			Symbol_New parentScope = (Symbol_New)((IScope_New)resolve(propScope)).getProperty(DATA_TYPE);
			return parentScope.resolveMember(propMember);
		}
		return s;
	}
	
	default public Symbol_New resolveMember(PropertyList _properties) {
		String name = (String)_properties.getProperty(NAME);
		Member member = getMembers().get(getStringOptionCase(name, _properties));
		if(member != null && member.size() > 0){
			return setUsed(resolveAmbiguity(member, _properties), _properties);
		}
		else {
			Symbol_New trySymbol = tryAlias(_properties);
			if(trySymbol != null) {
				return setUsed(trySymbol, _properties);
			}
			trySymbol = tryCreateForBuiltinSymbol(_properties);
			if(trySymbol != null) {
				return setUsed(trySymbol, _properties);
			}
			trySymbol = tryCreateInferDbSymbol(_properties);
			if(trySymbol != null) {
				return setUsed(trySymbol, _properties);
			}
			return setUsed(null, _properties);
		}
	}
	
	default public Symbol_New findMember(PropertyList _properties) {
		String name = (String)_properties.getProperty(NAME);
		Member member = getMembers().get(getStringOptionCase(name, _properties));
		if(member != null && member.size() > 0){
			return member.getFirstSymbol();
		}
		return null;
	}	
	
	default public Symbol_New tryAlias(PropertyList _properties) {
		String name = (String)_properties.getProperty(NAME);
		IScope_New symAlias = (IScope_New) _properties.getProperty(ALIAS);
		if(symAlias != null) {
			Member member = symAlias.getMembers().get(getStringOptionCase(name, _properties));
			if(member != null && member.size() > 0){
				return setUsed(resolveAmbiguity(member, _properties), _properties);
			}
		}
		return null;
	}
	
	default public boolean deleteMember(PropertyList _properties) {
		String name = (String)_properties.getProperty(NAME);
		Member member = getMembers().get(getStringOptionCase(name, _properties));
		if(member != null && member.size() > 0){
			getMembers().remove(getStringOptionCase(name, _properties));
			return true;
		}
		return false;
	}	
	
	default public Symbol_New tryCreateInferDbSymbol(PropertyList _properties) {
		if(getProperties().hasProperty(DEF_MODE, "PRE_DEFINED") 
				|| getProperties().hasProperty(DEF_MODE, "INFER")) {
			return createInferDatabaseSymbol(_properties);
		}
		return null;
	}
	
	default public Symbol_New tryCreateForBuiltinSymbol(PropertyList _properties) {
		
		if(getProperties().hasProperty("CATEGORY", "DATABASE")) return null;
		
		if(getProperties().hasProperty(DEF_MODE, "BUILTIN") 
				|| _properties.hasProperty(DEF_MODE, "BUILTIN_ATTRIBUTE")
				|| getProperties().hasProperty(DEF_MODE, "IMPLICIT")
				|| getProperties().hasProperty(DEF_MODE, "MEMBER_BUILTIN")
				|| getProperties().hasProperty(DEF_MODE, "PRE_DEFINED")
				|| getProperties().hasProperty(DEF_MODE, "USER_DEFINED")
				|| getProperties().hasProperty(DEF_MODE, "MEMBER_PRE_DEFINED")) { 
			return createBuiltinSymbol(_properties);
		}
		
		Symbol_New sym = (Symbol_New)getProperties().getProperty(DATA_TYPE);
		
		if(sym.getProperties().hasProperty(DEF_MODE, "BUILTIN")
				|| sym.getProperties().hasProperty(DEF_MODE, "BUILTIN_ATTRIBUTE")
				|| sym.getProperties().hasProperty(DEF_MODE, "IMPLICIT")
				|| sym.getProperties().hasProperty(DEF_MODE, "MEMBER_BUILTIN")
				|| sym.getProperties().hasProperty(DEF_MODE, "PRE_DEFINED")
				|| sym.getProperties().hasProperty(DEF_MODE, "MEMBER_PRE_DEFINED")) {
			_properties.addProperty(ENCLOSING_SCOPE, getProperty(ENCLOSING_SCOPE));
			_properties.addProperty(ENCLOSING_SCOPE_NAME, getProperty(ENCLOSING_SCOPE_NAME));
			return createBuiltinSymbol(_properties);
		}
		else return null;
	}
	
	default public Symbol_New resolveAmbiguity(Member _member, PropertyList _properties) {
		if(_member.size() > 1) {
		System.err.format("*** WARNING - Resolving(first) ambiguity for %s in SCOPE %s%n",
				          _member.getName(), getName());
		}
		
		// Creado This em DefSymbol...
		if(_member.getFirstSymbol().getProperty(THIS) != null ) {
			return (Symbol_New)_member.getFirstSymbol().getProperty(THIS);
		}
		if(_member.getFirstSymbol().getProperty("ALIAS") != null ) {
			return (Symbol_New)_member.getFirstSymbol().getProperty("ALIAS");
		}
		return _member.getFirstSymbol();
	}
	
	default public StringOptionalCase getStringOptionCase(String _name, 
        PropertyList _properties) {
		return new StringOptionalCase(_name, ((KeywordLanguage)_properties.getProperty(KEYWORD_LANGUAGE)).isCaseSensitive());
	}
	
	default public Symbol_New createBuiltinSymbol(PropertyList _properties) {
		String name = (String)_properties.getProperty(NAME);
		SymbolTable_New st = (SymbolTable_New)_properties.getProperty(SYMBOLTABLE);
		KeywordLanguage keywordLanguage = (KeywordLanguage)_properties.getProperty(KEYWORD_LANGUAGE);
		SymbolFactory symbolFactory = (SymbolFactory)_properties.getProperty(SYMBOL_FACTORY);
		ParserRuleContext context = (ParserRuleContext)_properties.getProperty(CONTEXT);
		
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, this);
		properties.addProperty(ENCLOSING_SCOPE_NAME, this.getName());
		properties.addProperty(DEF_MODE, IMPLICIT);
		if( _properties.hasProperty(DEF_MODE, "BUILTIN_ATTRIBUTE")) {
			properties.addProperty(CATEGORY_TYPE, "ATTRIBUTE");
			properties.addProperty("VALUE", _properties.getProperty("VALUE"));
		}
		else {
			properties.addProperty(CATEGORY_TYPE, "MEMBER");
		}
		properties.addProperty(CATEGORY, this.getProperty(CATEGORY));
		properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");
		
		WhereUsed whereUsed = new WhereUsed();
		whereUsed.add(context);
		properties.addProperty(WHERE_USED, whereUsed);
		
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.BUILTIN);
		
		System.err.format("*** WARNING: CREATED BUITIN SYMBOL %s in SCOPE '%s' in COMPILATION UNINT '%s' IN LINE '%d'%n"
				, name, getName(), st.getCompilarionUnitSymbol(context).getName() ,context.start.getLine());
		return symbolFactory.getSymbol(properties);
	}
	
	default public Symbol_New createImplicitSymbol(PropertyList _properties) {
		String name = (String)_properties.getProperty(NAME);
		SymbolTable_New st = (SymbolTable_New)_properties.getProperty(SYMBOLTABLE);
		KeywordLanguage keywordLanguage = (KeywordLanguage)_properties.getProperty(KEYWORD_LANGUAGE);
		SymbolFactory symbolFactory = (SymbolFactory)_properties.getProperty(SYMBOL_FACTORY);
		ParserRuleContext context = (ParserRuleContext)_properties.getProperty(CONTEXT);
		Symbol_New enclosingScope = (Symbol_New)_properties.getProperty(ENCLOSING_SCOPE);
		
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, enclosingScope);
		properties.addProperty(ENCLOSING_SCOPE_NAME, enclosingScope.getName());
		properties.addProperty(DEF_MODE, "LANGUAGE_IMPLICIT");
		properties.addProperty(CATEGORY_TYPE, IMPLICIT);
		properties.addProperty(CATEGORY, VARIABLE);
		properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");
		
		WhereUsed whereUsed = new WhereUsed();
		whereUsed.add(context);
		properties.addProperty(WHERE_USED, whereUsed);

		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.VARIABLE);
		
		System.err.format("*** WARNING: CREATED IMPLICIT SYMBOL %s  IN  DEF MODE  in SCOPE '%s' in COMPILATION UNINT '%s' IN LINE '%d'%n"
				, name, enclosingScope.getName(), st.getCompilarionUnitSymbol(context).getName() ,context.start.getLine());
		return symbolFactory.getSymbol(properties);
	}
	
	default public Symbol_New createInferSymbol(PropertyList _properties) {
		String name = (String)_properties.getProperty(NAME);
		SymbolTable_New st = (SymbolTable_New)_properties.getProperty(SYMBOLTABLE);
		KeywordLanguage keywordLanguage = (KeywordLanguage)_properties.getProperty(KEYWORD_LANGUAGE);
		SymbolFactory symbolFactory = (SymbolFactory)_properties.getProperty(SYMBOL_FACTORY);
		ParserRuleContext context = (ParserRuleContext)_properties.getProperty(CONTEXT);
		Symbol_New enclosingScope = (Symbol_New)_properties.getProperty(ENCLOSING_SCOPE);
		
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, enclosingScope);
		properties.addProperty(ENCLOSING_SCOPE_NAME, enclosingScope.getName());
		properties.addProperty(DEF_MODE, "INFER");
		properties.addProperty(CATEGORY_TYPE, IMPLICIT);
		properties.addProperty(CATEGORY, VARIABLE);
		properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");
		
		WhereUsed whereUsed = new WhereUsed();
		whereUsed.add(context);
		properties.addProperty(WHERE_USED, whereUsed);

		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.VARIABLE);
		
		System.err.format("*** WARNING: CREATED INFER SYMBOL %s  IN  DEF MODE  in SCOPE '%s' in COMPILATION UNINT '%s' IN LINE '%d'%n"
				, name, enclosingScope.getName(), st.getCompilarionUnitSymbol(context).getName() ,context.start.getLine());
		return symbolFactory.getSymbol(properties);
	}
	
	default public Symbol_New createInferDatabaseSymbol(PropertyList _properties, boolean..._mantemPropertyList) {
		Map<String,PropertyList> propertiesByName = (Map<String, PropertyList>) _properties.getProperty("PROPERTIES_BY_NAME");

		if(propertiesByName == null) {
			PropertyList propertyList = (PropertyList) _properties.getProperty("PROPERTY_LIST");
			propertiesByName = (Map<String, PropertyList>) propertyList.getProperty("PROPERTIES_BY_NAME");
		}
		
		PropertyList dbProperties = propertiesByName.get((String)_properties.getProperty(NAME));
		String name = (String)dbProperties.getProperty(NAME);
		SymbolTable_New st = (SymbolTable_New)dbProperties.getProperty(SYMBOLTABLE);
		KeywordLanguage keywordLanguage = (KeywordLanguage)dbProperties.getProperty(KEYWORD_LANGUAGE);
		SymbolFactory symbolFactory = (SymbolFactory)dbProperties.getProperty(SYMBOL_FACTORY);
		ParserRuleContext context = (ParserRuleContext)dbProperties.getProperty(CONTEXT);
		String category = (String)dbProperties.getProperty("CATEGORY");
		String categoryType = (String)dbProperties.getProperty("CATEGORY_TYPE");

		Symbol_New enclosingScope = null;
		
		if(dbProperties.getProperty(ENCLOSING_SCOPE) == null) {
			enclosingScope = (Symbol_New) this;
		}
		else if(categoryType.equalsIgnoreCase("COLUMN")){
			enclosingScope = (Symbol_New) dbProperties.getProperty(ENCLOSING_SCOPE); // COLUMN
			Symbol_New sym = enclosingScope.resolve(dbProperties);
			if(sym != null) return sym;
		}
		else {
			enclosingScope = (Symbol_New) dbProperties.getProperty(ENCLOSING_SCOPE) ; // ALIAS
			Symbol_New sym = enclosingScope.resolve(dbProperties);
			if(sym != null) return sym;
		}
		
		dbProperties.addProperty(ENCLOSING_SCOPE, enclosingScope);
		dbProperties.addProperty(ENCLOSING_SCOPE_NAME, enclosingScope.getName());
		dbProperties.addProperty(DEF_MODE, "INFER");
		dbProperties.addProperty("DATA_TYPE_NAME", "UNDEFINED");
		
		WhereUsed whereUsed = new WhereUsed();
		whereUsed.add(context);
		dbProperties.addProperty(WHERE_USED, whereUsed);
		
		System.err.format("*** WARNING: CREATED DATABASE SYMBOL BY INFERENCE '%s' - (%s / %s) -  IN  DEF MODE  in SCOPE '%s' in COMPILATION UNINT '%s' IN LINE '%d'%n"
				, name, category, categoryType, enclosingScope.getName(), st.getCompilarionUnitSymbol(context).getName() ,context.start.getLine());
		return symbolFactory.getSymbol(dbProperties);
	}	
	
	default public boolean isDefModeImplicit(PropertyList _properties) {
		ParserRuleContext context = (ParserRuleContext)_properties.getProperty(CONTEXT);
		if(context == null ) return false;
		SymbolTable_New st = (SymbolTable_New)_properties.getProperty(SYMBOLTABLE);
		if(st.getCompilarionUnitSymbol(context).hasProperty(DEF_MODE, "LANGUAGE_IMPLICIT")) return true;
		return false;
	}
	
	default public boolean isDefModeInfer(PropertyList _properties) {
		if(((String)this.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("OBJECT")){
			return true;
		}
		return false;
	}
	
	default public boolean isDefModeInferDatabase(PropertyList _properties) {
		if(_properties.hasProperty("PROPERTY_LIST")) 	return true;
		return false;
	}
	
	default public Symbol_New setUsed(Symbol_New _sym, PropertyList _properties){
		if (_sym == null) return _sym; 
		
		if(_sym.getProperty("ALIAS") != null){
			_sym = (Symbol_New) _sym.getProperty("SYMBOL");
		}
		
		WhereUsed used = (WhereUsed)_sym.getProperty("WHERE_USED");

		if( used == null){
			used = new WhereUsed();
			_sym.addProperty("WHERE_USED", used);
		}
		ParserRuleContext context = (ParserRuleContext)_properties.getProperty(CONTEXT);
		used.add(context);
		return _sym;
	}
}