package br.com.bicam.util.symboltable;

import static br.com.bicam.util.constant.PropertyName.GLOBAL_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.BCString;
import br.com.bicam.util.ContextInformation_b;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.TerminalValue;
import br.com.bicam.util.constant.SymbolType;

public class SymbolTable_b { //implements PropertyName{
	
	IdentityHashMap<ParserRuleContext, ContextInformation_b> contextInformation =
			new IdentityHashMap<ParserRuleContext, ContextInformation_b>();
	
	GlobalScope_b rootGlobalScope;
	KeywordLanguage keywordsLanguage;
	PropertyList properties;
	IdentityHashMap<Symbol_b, PropertyList> dictionary;
	IdentityHashMap<Symbol_b, PropertyList> DbDictionary;

	LinkedList<SymbolTable_b> symbolTableList
			= new LinkedList<SymbolTable_b>();
	SymbolTable_b parent;
	SymbolTableFactory stFactory;
	
	IdentityHashMap<Symbol_b, ArrayList<ParserRuleContext>> whereUsed 
			= new IdentityHashMap<Symbol_b, ArrayList<ParserRuleContext>>();
	
	public SymbolTable_b(PropertyList _propertyList){
		stFactory = new SymbolTableFactory();
		properties = _propertyList;
		dictionary = new IdentityHashMap<Symbol_b, PropertyList>();
		DbDictionary = new IdentityHashMap<Symbol_b, PropertyList>();
		setProperties();
		setBuiltinSymbols();
	}
	
	public void setProperties(){
		rootGlobalScope = (GlobalScope_b) properties.getProperty(GLOBAL_SCOPE);
		keywordsLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);		
	}
	
    public String toString() { return rootGlobalScope.toString(); }
    
    public IScope getGlobalScope(){  
    	return rootGlobalScope;
    }
    
    public IScope getScope(ParserRuleContext _ctx){
    	if (contextInformation.get(_ctx) == null){
    		return null;
    	}
    	return contextInformation.get(_ctx).getScope();
    }
    
    public void setTerminalValue(ParserRuleContext _ctx, TerminalValue _terminalValue){
    	ContextInformation_b moreAux = contextInformation.get(_ctx);
    	if(moreAux == null){
    		moreAux = new ContextInformation_b();
    		contextInformation.put(_ctx, moreAux);
    	}
    	moreAux.setTerminalValue(_terminalValue);
    }
    
    public TerminalValue getTerminalValue(ParserRuleContext _ctx){
    	if (contextInformation.get(_ctx) == null){
    		return null;
    	}
    	TerminalValue symx = contextInformation.get(_ctx).getTerminalValue();
    	return symx;
    }
    
    public void setScope(ParserRuleContext _ctx, IScope _scope){
    	ContextInformation_b moreAux = contextInformation.get(_ctx);
    	if(moreAux == null){
    		moreAux = new ContextInformation_b();
    		contextInformation.put(_ctx, moreAux);
    	}
    	moreAux.setScope(_scope);
    }
    
    public Symbol_b getSymbol(ParserRuleContext _ctx){
    	if (contextInformation.get(_ctx) == null){
    		return null;
    	}
    	Symbol_b symx = contextInformation.get(_ctx).getSymbol();
    	return symx;
    } 
    
    public void setSymbol(ParserRuleContext _ctx, Symbol_b _sym){
    	ContextInformation_b moreAux = contextInformation.get(_ctx);
    	if(moreAux == null){
    		moreAux = new ContextInformation_b();
    		contextInformation.put(_ctx, moreAux);
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
    
    public SymbolTable_b getparent(){
    	return parent;
    }
    
    public void setparent(SymbolTable_b _symbolTable){
    	parent = _symbolTable;
    }    
    
    public void addSymbolTable(SymbolTable_b _symbolTable){
    	symbolTableList.add(_symbolTable);
    }
    
    public LinkedList<SymbolTable_b> getSymbolTableList(){
    	return symbolTableList;
    }
    
    public Symbol_b getCompilarionUnitSymbol(ParserRuleContext _ctx){
		ParserRuleContext context  = NodeExplorer.getAncestorClass(_ctx, "StartRuleContext");
		return  getSymbol(context); 
    }
    
    public String getCompilationUnitName(ParserRuleContext _ctx){
    	return getCompilarionUnitSymbol(_ctx).getName();
    }
    
    public Symbol_b getProcedureSymbol(ParserRuleContext _ctx){
		ParserRuleContext context  = NodeExplorer.getAncestorClass(_ctx, "MethodDefinitionContext");
		return getSymbol(context);
    }    
    
    public ParserRuleContext getProcedureContext(ParserRuleContext _ctx){
		return NodeExplorer.getAncestorClass(_ctx, "MethodDefinitionContext");
    }     

    public List<Symbol_b> getSymbolByTypeProperty(IScope _scope, String _key, String _value){
    	List<Symbol_b> symbolList = new ArrayList<Symbol_b>();
       	for( BCString name : _scope.getMembers().keySet()){
       		for(Symbol_b sym : _scope.getMemberList(name).getSymbols()){
       			if((sym.getType() != null) && sym.getType().hasProperty(_key, _value)) symbolList.add(sym);
	    		if(sym instanceof IScope){
	    			symbolList.addAll(getSymbolByTypeProperty((IScope)sym, _key, _value));
	    		}
       		}
    	}
    	return symbolList;
    }
    
    public List<Symbol_b> getSymbolByProperty(IScope _scope, String _key, String _value){
    	List<Symbol_b> symbolList = new ArrayList<Symbol_b>();
       	for( SymbolList list : _scope.getMembers().values()){
       		for(Symbol_b sym : list.getSymbols()){
       			if(sym.hasProperty(_key, _value)) {
       				symbolList.add(sym);
//       				System.err.println(sym.toString());
       			}
	    		if(sym instanceof IScope){
	    			symbolList.addAll(getSymbolByProperty((IScope)sym, _key, _value));
	    		}
       		}
    	}
    	return symbolList;
    }
    
    public List<Symbol_b> getSymbolByProperty(String _key){
    	List<Symbol_b> symbolList = new ArrayList<Symbol_b>();
       	for( SymbolList list : getGlobalScope().getMembers().values()){
       		for(Symbol_b sym : list.getSymbols()){
       			if(sym.getProperty(_key) != null) {
       				symbolList.add(sym);
       			}
	    		if(sym instanceof IScope){
	    			symbolList.addAll(getSymbolByProperty((IScope)sym,_key));
	    		}
       		}
    	}
    	return symbolList;
    }
    
    public List<Symbol_b> getSymbolByProperty(IScope _scope, String _key){
    	List<Symbol_b> symbolList = new ArrayList<Symbol_b>();
       	for( SymbolList list : _scope.getMembers().values()){
       		for(Symbol_b sym : list.getSymbols()){
       			if(sym.getProperty(_key) != null) {
       				symbolList.add(sym);
       			}
	    		if(sym instanceof IScope){
	    			symbolList.addAll(getSymbolByProperty((IScope)sym, _key));
	    		}
       		}
    	}
    	return symbolList;
    }
    
	public List<Symbol_b> getSymbolByProperty(String _propKey, String _propValue) {
		return getSymbolByProperty(getGlobalScope(), _propKey, _propValue);
	}
	
	public List<Symbol_b> getSymbolByTypeProperty(String _propKey, String _propValue) {
		return getSymbolByTypeProperty(getGlobalScope(), _propKey, _propValue);
	}
	
	public  ArrayList<Symbol_b> getSymbolByName(String _name, boolean ..._isSensitive){
		return (_isSensitive.length > 0 ? getSymbolByName(getGlobalScope(), _name, _isSensitive[0])
				                        : getSymbolByName(getGlobalScope(), _name));
	}
	
	public  ArrayList<Symbol_b> getSymbolByName(IScope _scope, String _name, boolean ..._caseSensitive){
		ArrayList<Symbol_b> list = new ArrayList<Symbol_b>();
		for(SymbolList symbolList : _scope.getMembers().values()){
			for (Symbol_b sym : symbolList.getSymbols()) {
				if(_caseSensitive.length > 0 && !_caseSensitive[0]){
					if(sym.getName().equalsIgnoreCase(_name)){
						list.add(sym);
					}
				}
				else if(sym.getName().equals(_name)){
					list.add(sym);
				}
				if(sym instanceof IScope){
					if(_caseSensitive.length > 0) list.addAll(getSymbolByName((IScope)sym, _name, _caseSensitive[0]));
					else list.addAll(getSymbolByName((IScope)sym, _name));
				}
			}
		}		
		return list;
	}
	
	public void addUsedSymbol(Symbol_b _sym, ParserRuleContext _location){
		PropertyList usage = dictionary.get(_sym);
		if(usage == null){
			usage = new PropertyList();
		}
		List<ParserRuleContext> used = (List<ParserRuleContext>)usage.getProperty("WHERE_USED");
		if(used == null){
			used = new ArrayList<ParserRuleContext>();
			usage.addProperty("WHERE_USED", used);
		}
		used.add(_location);
	}
	
	public List<ParserRuleContext> getUsedSymbol(Symbol_b _sym){
		PropertyList usage = dictionary.get(_sym);
		if(usage != null){
			return (List<ParserRuleContext>)usage.getProperty("WHERE_USED");
		}
		return null;
	}
	
	public void addDbSymbol(Symbol_b _sym){
		if(DbDictionary.get(_sym) != null){
			try{
				throw new Exception();
			} catch (Exception e){
				System.err.format("*** ERROR - DUPLICATE SYMBOL %s at LINE %d in CompUnit %s%n"
						, _sym.getName()
						, _sym.getContext().start.getLine()
						, getCompilarionUnitSymbol(_sym.getContext()).getName());
			}
		}
		DbDictionary.put(_sym, new PropertyList());
	}
   
	private void setBuiltinSymbols(){
		for(String name : keywordsLanguage.getKeywords("FUNCTION")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "FUNCTION");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("TYPE")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "TYPE");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}		
		
		for(String name : keywordsLanguage.getKeywords("OBJECT")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "OBJECT");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("LIB")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "OBJECT");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("CONTROL")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "CONTROL");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}		
		
		for(String name : keywordsLanguage.getKeywords("CLASSCONSTANT")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "CLASSCONSTANT");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("userClass")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "userClass");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("PROPERTY")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "PROPERTY");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}		
		
		for(String name : keywordsLanguage.getKeywords("METHOD")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "METHOD");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("userMethod")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "userMethod");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("classMember")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "classMember");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
		
		for(String name : keywordsLanguage.getKeywords("daoLib")){
			PropertyList properties = new PropertyList();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("ENCLOSING_SCOPE", getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", getGlobalScope().getName());
			properties.addProperty("CATEGORY_TYPE", "LIB");
//			properties.addProperty("CATEGORY", "BUILTIN");	
			properties.addProperty("CATEGORY", "DB");	
			properties.addProperty("CREATE_MODE", "BUILTIN");	
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			stFactory.getSymbol(properties);
		}
	}    
}