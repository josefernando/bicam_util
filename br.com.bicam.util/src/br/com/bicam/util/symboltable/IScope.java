package br.com.bicam.util.symboltable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.parser.constant.PropertyName;
import br.com.bicam.util.BCString;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType;


public interface IScope extends PropertyName{
	public SymbolList  getMemberList(BCString _name);
	
	public Map<BCString, SymbolList>  getMembers();	

	public void addProperty(String _key, Object _value);
	public Object getProperty(String _key);
	public IScope getScopeByProperty(String _key, String _value);
	public PropertyList getProperties();
	
	default public String getName(){
		return (String) getProperties().getProperty(NAME);
	}
	
	default public IScope getEnclosingScope(){
		return (IScope) getProperties().getProperty(ENCLOSING_SCOPE);
	}
	
	default public IScope getParentScope(){
		return (IScope) getProperties().getProperty(PARENT_SCOPE);
	}
	
	default public IScope getGlobalScope(){
		return (IScope) getProperties().getProperty(GLOBAL_SCOPE);
	}
	
	public void setCompilationUnitScope(IScope _scope);
	public void setGlobalScope(IScope _scope);
	
	default public void addMemberList(BCString _name, SymbolList _symList){
		getMembers().put(_name, _symList);
	}
	
    default public void define(Symbol_b _sym){
    	SymbolList symbolList = getMemberList(new BCString(_sym.getName(), false));
		if(symbolList == null){
			symbolList = new SymbolList(_sym.getName(), false);
			addMemberList(new BCString(_sym.getName(), false), symbolList);
		}
		else {
			int line = 0;
			if(_sym.getContext() != null){
				line = _sym.getContext().start.getLine();
			}
			System.err.format("* WARNING - Duplicated symbol: '%s' in scope '%s' at line %d%n"
								, _sym.getName(), getName(), line);
		}
		symbolList.add(_sym);
    }
    
    default public void remove(Symbol_b _sym){
    	SymbolList symbolList = getMemberList(new BCString(_sym.getName(), false));
		symbolList.remove(_sym);
    }
    
	default public void addResolveOrder(IScope _scope){
		getResolveOrder().add(_scope);
	}
	
	default public Symbol_b resolve(String _symbol_name, PropertyList _propertyList){
		BCString name = new BCString(_symbol_name,false);
		SymbolList symList = getMembers().get(name);
		if(symList != null){
			if(symList.getSymbols().isEmpty()){
				if(getEnclosingScope() != null)
					return getEnclosingScope().resolve(_symbol_name, _propertyList);
				else return null;
			}
			if(symList.getSymbols().size() == 1) {
				// desconsidera compilation unit para comandos VB do tipo:
				//  form.Show
				if(symList.getSymbols().get(0).hasProperty("CATEGORY", "COMPILATION_UNIT")){
					return getEnclosingScope().resolve(_symbol_name, _propertyList);
				}
				return  symList.getSymbols().get(0);
			}
			
			Symbol_b winnerSym = null;
			for(Symbol_b sym : symList.getSymbols()){
				if(sym.hasProperty("CATEGORY", "COMPILATION_UNIT")){
					if(winnerSym == null) winnerSym = sym;
					continue;
				}
				if(sym.hasProperty("CATEGORY", "REFERENCE")){
					winnerSym = sym;
					continue;
				}
				if(sym.hasProperty("CATEGORY", "UI")){
					winnerSym = sym;
					break;
				}
			}
			
			if(winnerSym != null) return winnerSym;
			
			Class classToResolve = (Class)_propertyList.getProperty("CLASS_TO_RESOLVE");
			
			for(Symbol_b sym : symList.getSymbols()){
				if (classToResolve.isInstance(sym))
				return sym; // Precisa selecionar o tipo
			}
			
			ParserRuleContext contextToResolve = (ParserRuleContext)_propertyList.getProperty("CONTEXT_TO_RESOLVE");
			SymbolTable_b st = (SymbolTable_b)_propertyList.getProperty("SYMBOLTABLE");
			
			try{
				throw new Exception("** MULTIPLES SYMBOLS  S IN SYMBOLTABLE - NOT RESOLVED-  at Line L  In Comp Unit C " 
			            + " S = " + contextToResolve.getText() 
			            + " L = " + contextToResolve.start.getLine()
			            + " C = " + st.getCompilarionUnitSymbol(contextToResolve).getName()); 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(getEnclosingScope() != null){
			return getEnclosingScope().resolve(_symbol_name,_propertyList);
		}
		return null; 
	}
	
	default public Symbol_b resolveMember(String _symbol_name, PropertyList _propertyList){
		String memberAccessOper = (String)_propertyList.getProperty("MEMBER_ACCESS_OPER");
		String[] qualifiedName = _symbol_name.split(memberAccessOper);

		if(qualifiedName.length == 1) {
			return resolve(_symbol_name,_propertyList);
		}
		
		StringBuffer nameToResolve = new StringBuffer();
		nameToResolve.append(qualifiedName[0]);
		for(int i=1; i < qualifiedName.length - 1; i++){
			nameToResolve.append("." + qualifiedName[i]);
		}
		
//		Symbol_b s = resolveMember(nameToResolve.toString());
		
		
		Symbol_b sym = resolveMember(nameToResolve.toString(), _propertyList);
		
		IScope scope = null;
		if(sym instanceof IScope)
			scope = (IScope)sym;
		else 
			scope = (IScope)sym.getType();
			
		if(scope == null) return null;
		
//======= GET MEMBERS ============
		BCString name = new BCString(qualifiedName[qualifiedName.length - 1],false);
		SymbolList symList = scope.getMembers().get(name);
		if(symList != null){
//			return symList.getSymbols().get(0); // Precisa selecionar o tipo todo

//	"CATEGORY_TYPE", "SET"	=> Significa Property Set, e no VB6 consideramos apenas Property Set		
			if(symList.getSymbols().get(0).hasProperty("CATEGORY_TYPE", "SET"))
				return symList.getSymbols().get(1); // Precisa selecionar o tipo todo
			else return symList.getSymbols().get(0);
		}
		// Ze
		else if(scope.getScopeByProperty("CATEGORY", "BUILTIN")!= null
				|| scope.getScopeByProperty("CREATE_MODE", "BUILTIN")!= null
				|| scope.getScopeByProperty("CREATE_MODE", "ON_DEMAND")!= null
				|| scope.getScopeByProperty("CATEGORY", "UI")!= null
				|| scope.getScopeByProperty("CATEGORY", "DB")!= null
				|| scope.getScopeByProperty("OBJECT", "TRUE")!= null
				|| scope.getScopeByProperty("CATEGORY","APPLICATION_COMPONENT")!= null
				|| scope.getScopeByProperty("CATEGORY", "APPLICATION_REFERENCE") != null){
			return createBuiltinTypeSymbol(name.toString(), scope);
		}
		// Ze 
		else if(scope instanceof Symbol_b){
				Type type = ((Symbol_b)scope).getType();
				if(type instanceof IScope){
					symList = ((IScope)type).getMembers().get(name);
					if(symList != null){
						return symList.getSymbols().get(0); // Precisa selecionar o tipo
					}					
				}
				if(scope.getScopeByProperty("CATEGORY", "BUILTIN")!= null
						|| scope.getScopeByProperty("CREATE_MODE", "BUILTIN")!= null
						|| scope.getScopeByProperty("CREATE_MODE", "ON_DEMAND")!= null
						|| scope.getScopeByProperty("CATEGORY", "UI")!= null
						|| scope.getScopeByProperty("CATEGORY", "DB")!= null
						|| scope.getScopeByProperty("OBJECT", "TRUE")!= null
						|| scope.getScopeByProperty("CATEGORY","APPLICATION_COMPONENT")!= null
						|| scope.getScopeByProperty("CATEGORY", "APPLICATION_REFERENCE") != null){
					return createBuiltinTypeSymbol(name.toString(), scope);
				}
		}
		return null;
	}
	
	default public Symbol_b createBuiltinTypeSymbol(String _name, IScope _scope){
		
		   if(isDbTypeSymbol(_scope)) return createBuiltinDbSymbol(_name, _scope);
		   
			PropertyList properties = new PropertyList();
			SymbolTableFactory stFactory = new SymbolTableFactory();
			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", _name);
			properties.addProperty("ENCLOSING_SCOPE", _scope);
			properties.addProperty("ENCLOSING_SCOPE_NAME", _scope.getName());
			properties.addProperty("CATEGORY_TYPE", "ON_DEMAND");
			properties.addProperty("CATEGORY", "BUILTIN");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.BUILTIN_TYPE);
			Symbol_b sym = stFactory.getSymbol(properties);
			System.err.format("*** WARNING  created builtin type symbol %s%n", sym.getName());
			return sym;
	}
	
	default public boolean isDbTypeSymbol(IScope _scope){
		if(_scope instanceof Symbol_b){
			if(((Symbol_b)_scope).hasProperty("CATEGORY", "DB")){
				return true;
			}
		}
		return false;
	}
	
	default public Symbol_b createBuiltinDbSymbol(String _name, IScope _scope){
		PropertyList properties = new PropertyList();
		SymbolTableFactory stFactory = new SymbolTableFactory();
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("NAME", _name);
		properties.addProperty("ENCLOSING_SCOPE", _scope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", _scope.getName());
		properties.addProperty("CATEGORY", "DB");
		properties.addProperty("CATEGORY_TYPE", "UNDEFINED");
/*		if(((String)_scope.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("Recordset")){
			properties.addProperty("CATEGORY_TYPE", "RECORDSET_FIELD");
		}
		else {
		properties.addProperty("CATEGORY_TYPE", _name);
		}*/
		properties.addProperty("CREATE_MODE", "ON_DEMAND");
		properties.addProperty(	"SYMBOL_TYPE", SymbolType.DB);
		Symbol_b sym = stFactory.getSymbol(properties);
		System.err.format("*** WARNING  created builtin type symbol %s%n", sym.getName());
		return sym;		
	}
	
	default public Symbol_b createVariantSymbol(String _name, IScope _scope){
			PropertyList properties = new PropertyList();
			SymbolTableFactory stFactory = new SymbolTableFactory();
			properties.addProperty("NAME", _name);
			properties.addProperty("ENCLOSING_SCOPE", _scope);
			properties.addProperty("ENCLOSING_SCOPE_NAME", _scope.getName());
			properties.addProperty("CATEGORY_TYPE", "ON_DEMAND");
			properties.addProperty("CATEGORY", "VARIABLE");
			properties.addProperty(	"SYMBOL_TYPE", SymbolType.VARIABLE);
			Symbol_b sym = stFactory.getSymbol(properties);
			System.err.format("*** WARNING  created variant on demand for symbol %s%n", sym.getName());
			return sym;
	}	
	
	public Deque<IScope> getResolveOrder();	
	public IScope getCurrentScopeResolveOrder();
	
	default public IScope getNextScopeResolveOrder(){
		if(getResolveOrder().isEmpty()) return null;
		
		if(getCurrentScopeResolveOrder() == null)
			return getResolveOrder().getFirst();
		
		ArrayList<IScope> aux = new ArrayList<IScope>();
		aux.addAll(getResolveOrder());
		boolean isNext = false;
		for(IScope _scope : aux){
			if(isNext) return _scope;
			if(_scope == getCurrentScopeResolveOrder()) isNext = true;
		}
		return null;
	} 
}
