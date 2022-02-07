package br.com.bicam.util.symboltable;

import static br.com.bicam.util.constant.PropertyName.NAME;

import java.util.ArrayList;
import java.util.List;

import br.com.bicam.util.PropertyList;

public class UnitTestScope {
	SymbolTable_New st;
	List<String> scopeTree;
	IScope_New currentScope;
	PropertyList properties;
	
	public UnitTestScope(SymbolTable_New _st, List<String> _scopeTree, PropertyList _properties) {
		this.st = _st;
		this.scopeTree = _scopeTree;
		this.properties = _properties;
		if(_scopeTree == null) loadScopeTree(); // insere scope tree manualmente
		setScope();
	}
	
	private List<String> loadScopeTree() {
		scopeTree = new ArrayList<String>();
		scopeTree.add("R1FAB001#COMPILATION_UNIT");
		scopeTree.add("R1FAB001");
		scopeTree.add("pa3d_status");
		return scopeTree;
	}
	
	public Symbol_New resolve(PropertyList _properties) {
		return currentScope.resolve(_properties);
	}
	
	public Symbol_New resolveMember(PropertyList _properties) {
		return currentScope.resolveMember(_properties);
	}
	
	private IScope_New setScope() {
		IScope_New scope = (IScope_New)st.getGlobalScope();
		for(int ix=0; ix < scopeTree.size(); ix++) {
			String name = scopeTree.get(ix);
			properties.addProperty(NAME, name);
	        
			currentScope = scope.resolveMember(properties);
			if(currentScope == null) {
				try {
					throw new Exception();
				} catch (Exception e) {
					System.err.format("*** ERROR: SYMBOL  '%s' not found in SCOPE '%s'%n"
							,name,scope.getName());
				}
			}
			scope = currentScope;
		}

		if(currentScope == null) {
			try {
				throw new Exception();
			}catch(Exception e){
				System.err.format("*** ERROR: Invalid scopeTree: %s%n",scopeTree.toString());
                e.printStackTrace();
			}
			System.exit(1);
		}
		return currentScope;
	}
}