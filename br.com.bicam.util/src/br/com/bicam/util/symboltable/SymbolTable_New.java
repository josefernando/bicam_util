package br.com.bicam.util.symboltable;

import static br.com.bicam.util.constant.ASTRuleName.METHOD_DEFINITION_CONTEXT;
import static br.com.bicam.util.constant.ASTRuleName.START_RULE_CONTEXT;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.ContextData_New;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType_New;

public class SymbolTable_New { //implements PropertyName{
	
	IdentityHashMap<ParserRuleContext, ContextData_New> contextData =
			new IdentityHashMap<ParserRuleContext, ContextData_New>();
	
	Symbol_New globalScope;
	Symbol_New publicScope;

	KeywordLanguage keywordsLanguage;
	PropertyList properties;

	SymbolFactory stFactory;
	
	IdentityHashMap<Symbol_New, ArrayList<ParserRuleContext>> whereUsed 
			= new IdentityHashMap<Symbol_New, ArrayList<ParserRuleContext>>();
	
	public SymbolTable_New(PropertyList _propertyList){
		properties = _propertyList;
		setProperties();
		setSymbols();
		setConfigurationSymbols();
	}
	
	public PropertyList getProperties() {
		return properties;
	}
	
	public void setProperties(){
		stFactory =  (SymbolFactory)properties.getProperty(SYMBOL_FACTORY);
		keywordsLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);
		globalScope = getGlobalScope();
	}
	
    public String toString() { return globalScope.toString(); }
    
    public IScope_New getScope(ParserRuleContext _ctx){
    	if (contextData.get(_ctx) == null){
    		return null;
    	}
    	return contextData.get(_ctx).getScope();
    }
    
    public void setScope(ParserRuleContext _ctx, IScope_New _scope){
    	ContextData_New moreAux = contextData.get(_ctx);
    	if(moreAux == null){
    		moreAux = new ContextData_New();
    		contextData.put(_ctx, moreAux);
    	}
    	moreAux.setScope(_scope);
    }
    
    public Symbol_New getSymbol(ParserRuleContext _ctx){
    	if(_ctx == null) {
    		try{
    			throw new Exception("*** ERROR: NULL PARAMETER IS NOT ALLOWED" );
    		} catch (Exception e) {
    			e.printStackTrace();
    			System.exit(1);
    		}    		
    	}
    	if (contextData.get(_ctx) == null){

    		return null;
    	}
    	Symbol_New symx = contextData.get(_ctx).getSymbol();
    	return symx;
    } 
    
    public Symbol_New getGlobalScope(){
    	if(globalScope != null) return globalScope;
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", "GLOBAL");
		properties.addProperty("ENCLOSING_SCOPE", null);
		properties.addProperty("ENCLOSING_SCOPE_NAME", null);
		properties.addProperty("CATEGORY", "SCOPE");		
		properties.addProperty("CATEGORY_TYPE", "GLOBAL");
		properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
		properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.GLOBAL_SCOPE);
		globalScope = stFactory.getSymbol(properties);
		return globalScope;
    }
    
    public KeywordLanguage getKeywordsLanguage() {
    	return keywordsLanguage;
    }
    
    public Symbol_New  getPublicScope() {
    	return publicScope;
    }
    
    public void  setPublicScope(Symbol_New _publicScope) {
    	this.publicScope = _publicScope;
    }
    
	public List<Symbol_New> getSymbolByProperty(String _propKey, String _propValue) {
		return getSymbolByProperty(getGlobalScope(), _propKey, _propValue);
	}  
    
    public List<Symbol_New> getSymbolByProperty(Symbol_New _scope, String _key, String _value){
   		List<Symbol_New> symbolList = new ArrayList<Symbol_New>();
    	for( Member member : _scope.getMembers().values()){
       		member.findSymbol();
       		while(member.hasNext()) {
       			Symbol_New sym = member.getNextSymbol();
       			if(sym.hasProperty(_key, _value)){
       				symbolList.add(sym);
       			}
    			symbolList.addAll(getSymbolByProperty(sym, _key, _value));
       		}
    	}
    	return symbolList;
    }
    
    public List<Symbol_New> getSymbolByProperty(Symbol_New _scope, String _key){
   		List<Symbol_New> symbolList = new ArrayList<Symbol_New>();
    	for( Member member : _scope.getMembers().values()){
       		member.findSymbol();
       		while(member.hasNext()) {
       			Symbol_New sym = member.getNextSymbol();
       			if(sym.getProperty(_key) != null){
       				symbolList.add(sym);
       			}
    			symbolList.addAll(getSymbolByProperty(sym, _key));
       		}
    	}
    	return symbolList;
    }    
    
	public List<Symbol_New> getSymbolByProperties(Map<String, String> _properties) {
		return getSymbolByProperties(getGlobalScope(), _properties);
	}  
    
    public List<Symbol_New> getSymbolByProperties(Symbol_New _scope, Map<String, String> _properties){
   		List<Symbol_New> symbolList = new ArrayList<Symbol_New>();
    	for( Member member : _scope.getMembers().values()){
       		member.findSymbol();
       		while(member.hasNext()) {
       			Symbol_New sym = member.getNextSymbol();
       			boolean win = true;
   	    		for (Map.Entry<String, String> entry : _properties.entrySet()) {
   	    			if(!sym.hasProperty(entry.getKey(), entry.getValue())) {
   	    				win = false;
   	    				break;
   	    			}
   	    		} 
   	    		if(win) symbolList.add(sym);
    			symbolList.addAll(getSymbolByProperties(sym, _properties));
       		}
    	}
    	return symbolList;
    }    
    
	public Symbol_New getScopeByProperty(Symbol_New _scope, String _propKey, String _propValue) {
		if(_scope.hasProperty(_propKey, _propValue)) return _scope;
        if (_scope.getProperty(ENCLOSING_SCOPE) == null) return null;
        _scope = (Symbol_New)_scope.getProperty(ENCLOSING_SCOPE);
        return getScopeByProperty(_scope, _propKey, _propValue);
	}
	
    public void setSymbol(ParserRuleContext _ctx, Symbol_New _sym){
    	ContextData_New moreAux = contextData.get(_ctx);
    	if(moreAux == null){
    		moreAux = new ContextData_New();
    		contextData.put(_ctx, moreAux);
    	}
    	
    	if(_sym == null){
    		try{
    			throw new Exception("ERROR: NULL SYMBOL IS NOT ALLOWED  in setSymbol " 
    					+ _ctx.getText()  + " at line " 
    					+ _ctx.start.getLine());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		return;
    	}
    	
    	if(_ctx == null){
    		try{
    			throw new Exception("ERROR: NULL CONTEXT IS NOT ALLOWED : Symbol (S) , CompUnit (C) , Line (L) " 
    					+ "S = " + _sym.getName() 
    					+ "C = " + getCompilarionUnitSymbol(_ctx).getName()
    					+ "L = " + _ctx.start.getLine());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		return;
    	}    	
    	
    	if(getSymbol(_ctx) == null) {   	
    		moreAux.setSymbol(_sym);
    	}
    	else if(_ctx.getText().contains("CreateObject") 
    			|| _ctx.getText().contains("GetObject")){ // a ser arrumado , vide refSymbol (setStmt)
    		moreAux.setSymbol(_sym); 
    	}
    	else {
    		try{
    			throw new Exception("ERROR: SYMBOL ALREADY DEFINED in SYMBOLTABLE and CONTEXT: " 
    		            + _sym.getName() + " - Context of REDEFINE: " 
    					+ _ctx.getText() + "  class: " + _ctx.getClass().getSimpleName() + " at line " 
    					+ _ctx.start.getLine());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}    		
    	}
    }
    
    public Symbol_New getCompilarionUnitSymbol(ParserRuleContext _ctx){
		ParserRuleContext context  = NodeExplorer_New.getAncestorClass(_ctx, START_RULE_CONTEXT);
		return  getSymbol(context); 
    }
    
    public String getCompilationUnitName(ParserRuleContext _ctx){
    	return getCompilarionUnitSymbol(_ctx).getName();
    }
    
    public Symbol_New getProcedureSymbol(ParserRuleContext _ctx){
		ParserRuleContext context  = NodeExplorer_New.getAncestorClass(_ctx, METHOD_DEFINITION_CONTEXT);
		return getSymbol(context);
    }    
    
    public ParserRuleContext getProcedureContext(ParserRuleContext _ctx){
		return NodeExplorer_New.getAncestorClass(_ctx, METHOD_DEFINITION_CONTEXT);
    } 
   
	private void setSymbols(){
		for(String name : keywordsLanguage.getKeywords("LANGUAGE_FUNCTION")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
//			properties.addProperty("CATEGORY_TYPE", "LANGUAGE");
			properties.addProperty("LANGUAGE", keywordsLanguage.getProperties().getProperty("LANGUAGE"));
			properties.addProperty("CATEGORY_TYPE", "BUILTIN");
			properties.addProperty("CATEGORY", "FUNCTION");
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");
			properties.addProperty("DEF_MODE", "BUILTIN");			
			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("DATA_TYPE")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", globalScope);
			properties.addProperty("ENCLOSING_SCOPE_NAME", globalScope.getName());
			properties.addProperty("CATEGORY_TYPE", "LANGUAGE");
			properties.addProperty("CATEGORY", "DATA_TYPE");
			properties.addProperty("DEF_MODE", "BUILTIN");
			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}		
		
		for(String name : keywordsLanguage.getKeywords("LANGUAGE_OBJECT")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
//			properties.addProperty("CATEGORY_TYPE", "LANGUAGE");
			properties.addProperty("LANGUAGE", keywordsLanguage.getProperties().getProperty("LANGUAGE"));
			properties.addProperty("CATEGORY_TYPE", "BUILTIN");			
			properties.addProperty("CATEGORY", "OBJECT");
			properties.addProperty("DEF_MODE", "BUILTIN");
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("LANGUAGE_LIB")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "LANGUAGE");
			properties.addProperty("CATEGORY", "LIB");
			properties.addProperty("DEF_MODE", "BUILTIN");
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("CONTROL")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "CONTROL");
			properties.addProperty("CATEGORY", "CONTROL");

			properties.addProperty("DEF_MODE", "BUILTIN");
//			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}		
		
		for(String name : keywordsLanguage.getKeywords("CLASSCONSTANT")){
			PropertyList properties = new PropertyList();
//			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "CONSTANT");
			properties.addProperty("CATEGORY", "CONSTANT");

//			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty("DEF_MODE", "BUILTIN");

			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("userClass")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
//			properties.addProperty("CATEGORY_TYPE", "userClass");
			properties.addProperty("LANGUAGE", keywordsLanguage.getProperties().getProperty("LANGUAGE"));
			properties.addProperty("CATEGORY_TYPE", "USER");				
			properties.addProperty("DEF_MODE", "USER_DEFINED");
			properties.addProperty("CATEGORY", "CLASS");
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.PRE_DEFINED);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("PROPERTY")){
			PropertyList properties = new PropertyList();
//			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "PROPERTY");
			properties.addProperty("DEF_MODE", "BUILTIN");

			properties.addProperty("CATEGORY", "PROPERTY");
			properties.addProperty("LANGUAGE", keywordsLanguage.getProperties().getProperty("LANGUAGE"));
			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}		
		
		for(String name : keywordsLanguage.getKeywords("METHOD")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "METHOD");
			properties.addProperty("CATEGORY", "METHOD");
			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");
			properties.addProperty("DEF_MODE", "BUILTIN");

			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("userMethod")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "userMethod");
			properties.addProperty("CATEGORY", "userMethod");
			properties.addProperty("DEF_MODE", "USER_DEFINED");

			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.PRE_DEFINED);
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("classMember")){
			PropertyList properties = new PropertyList();
//			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "classMember");
			properties.addProperty("CATEGORY", "classMember");
			properties.addProperty("DEF_MODE", "USER_DEFINED");

			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("database")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "DB");

			properties.addProperty("CATEGORY", "DB");	
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty("DEF_MODE", "BUILTIN");
			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("userDatabase")){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "DB");

			properties.addProperty("CATEGORY", "DB");	
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty("DEF_MODE", "USER_DEFINED");
			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}	
		
/*		for(Entry name : keywordsLanguage.getParameters().entrySet()){
			PropertyList properties = new PropertyList();
			properties.addProperty("NAME", name.getKey());
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "DB");

			properties.addProperty("CATEGORY", "DB");	
			properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

			properties.addProperty("DEF_MODE", "USER_DEFINED");
			properties.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			properties.addProperty(	"SYMBOL_TYPE", SymbolType_New.BUILTIN);
			stFactory.getSymbol(properties);
		}	*/	
	}
	
	private void setConfigurationSymbols() {
		if(keywordsLanguage.getParameter("SERVER_DEFAULT") != null) {
			PropertyList propertiesServer = new PropertyList();
			propertiesServer.addProperty("NAME", keywordsLanguage.getParameter("SERVER_DEFAULT"));
			propertiesServer.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			propertiesServer.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			propertiesServer.addProperty("DEF_MODE", "PRE_DEFINED");	
			propertiesServer.addProperty("CATEGORY", "DATABASE");	
			propertiesServer.addProperty("CATEGORY_TYPE", "SERVER");
			propertiesServer.addProperty("DATA_TYPE_NAME", "UNDEFINED");
			propertiesServer.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
			propertiesServer.addProperty(	"SYMBOL_TYPE", SymbolType_New.SERVER);
			Symbol_New serverSym = stFactory.getSymbol(propertiesServer);			
		
			if(keywordsLanguage.getParameter("DATABASE_DEFAULT") != null) {
				PropertyList propertiesDB = new PropertyList();
				propertiesDB.addProperty("NAME", "DATABASE_DEFAULT");
				propertiesDB.addProperty("ENCLOSING_SCOPE", serverSym);
				propertiesDB.addProperty("ENCLOSING_SCOPE_NAME", serverSym.getName());
				propertiesDB.addProperty("CATEGORY", "DATABASE");	
				propertiesDB.addProperty("DEF_MODE", "PRE_DEFINED");	
				propertiesDB.addProperty("CATEGORY_TYPE", "DATABASE");
				propertiesDB.addProperty("DATA_TYPE_NAME", "UNDEFINED");
				propertiesDB.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
				propertiesDB.addProperty(	"SYMBOL_TYPE", SymbolType_New.DATABASE);
				Symbol_New DbSym = stFactory.getSymbol(propertiesDB);	

				if(keywordsLanguage.getParameter("DB_OWNER_DEFAULT") != null) {
					PropertyList propertiesDbOwner = new PropertyList();
					propertiesDbOwner.addProperty("NAME", "DB_OWNER_DEFAULT");
					propertiesDbOwner.addProperty("ENCLOSING_SCOPE", DbSym);
					propertiesDbOwner.addProperty("ENCLOSING_SCOPE_NAME", DbSym.getName());
					propertiesDbOwner.addProperty("DEF_MODE", "PRE_DEFINED");	
					propertiesDbOwner.addProperty("CATEGORY", "DATABASE");	
					propertiesDbOwner.addProperty("CATEGORY_TYPE", "USER");
					propertiesDbOwner.addProperty("DATA_TYPE_NAME", "UNDEFINED");
					propertiesDbOwner.addProperty(KEYWORD_LANGUAGE, keywordsLanguage);
					propertiesDbOwner.addProperty("SYMBOL_TYPE", SymbolType_New.USERDB);
					Symbol_New DbOwnerSym = stFactory.getSymbol(propertiesDbOwner);			
				}				
			}
		}
	}
}