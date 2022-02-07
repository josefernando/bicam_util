package br.com.bicam.util.symboltable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;

import br.com.bicam.model.visualbasic.CompilationUnitComponent;

public class Catalog_b {

	SymbolTable_b st;
	
	List<Symbol_b> symbols;
	private Deque<Symbol_b> toVisit;
	private IdentityHashMap<Symbol_b, CompilationUnitComponent> toVisit1;
//	Project projectModel;
	CompilationUnitComponent projectModel;

	CompilationUnitComponent currentParent;

	public Catalog_b(SymbolTable_b _st) {
		this.st = _st;
		toVisit = new ArrayDeque<Symbol_b>();
		toVisit1 = new IdentityHashMap<Symbol_b, CompilationUnitComponent>();
		setRoot();
	}
	
	public void setRoot(){
		symbols = st.getSymbolByProperty("CATEGORY","APPLICATION");
		Symbol_b symPrj = symbols.get(0);
//		projectModel =  new Project(symPrj.getName());
		projectModel =  new CompilationUnitComponent(symPrj.getName());
		
//		currentParent = projectModel;
		addProperties(symPrj, projectModel);
		toVisit.push(symPrj);
		toVisit1.put(symPrj, projectModel);
		run();
	}
	
	public CompilationUnitComponent getCompilationUnitComponent(){
		return projectModel;
	}
	
	private void run(){
		while(!toVisit.isEmpty()){
//			addProperties(toVisit.peek());
			currentParent = toVisit1.get(toVisit.peek());
			addChildren(toVisit.pop());
		}
	}
	
	private void addChildren(Symbol_b _child){
		if(!(_child instanceof IScope)) return;
		IScope parent = (IScope)_child;
		
		if(parent.getMembers().isEmpty()) return;

		for( SymbolList sl : parent.getMembers().values()){
			for(Symbol_b s : sl.getSymbols()){
					if(!(s.getProperty("ENCLOSING_SCOPE")).equals(parent))
						continue; // Não pertence a esse escopo e está aqui apenas para a resolução no símbolo

					CompilationUnitComponent childComp = componentFactory(s);
					if(childComp == null) continue;
//					currentParent.getComponents().add(childComp);
					currentParent.addComponent(childComp);
					toVisit.push(s);
					toVisit1.put(s, projectModel);
			}
		}
	
	}
	
	private CompilationUnitComponent componentFactory(Symbol_b _symbol){
		String name = (String)_symbol.getProperty("NAME");
		
		if(_symbol.getProperty("CATEGORY") == null){
			try{
			throw new Exception();
			} catch (Exception e){
				System.err.println("*** CATEGORY IS  NULL: " + _symbol.getName());
				return null;
			}
			
		}
		
		if((((String)_symbol.getProperty("CATEGORY")).equalsIgnoreCase("APPLICATION_REFERENCE"))){
			CompilationUnitComponent component = new CompilationUnitComponent(name);
			for(String key : _symbol.getProperties().getProperties().keySet()){
				if(_symbol.getProperties().getProperties().get(key) instanceof String)
					component.addProperty(key, (String)_symbol.getProperties().getProperties().get(key));
			}
			return component;
		}
		else if((((String)_symbol.getProperty("CATEGORY")).equalsIgnoreCase("APPLICATION_COMPONENT"))){
			CompilationUnitComponent component = new CompilationUnitComponent(name);
			for(String key : _symbol.getProperties().getProperties().keySet()){
				if(_symbol.getProperties().getProperties().get(key) instanceof String)
					component.addProperty(key, (String)_symbol.getProperties().getProperties().get(key));
			}
			return component;
	}		
		else if((((String)_symbol.getProperty("CATEGORY")).equalsIgnoreCase("COMPILATION_UNIT"))){
				CompilationUnitComponent component = new CompilationUnitComponent(name);
				for(String key : _symbol.getProperties().getProperties().keySet()){
					if(_symbol.getProperties().getProperties().get(key) instanceof String)
						component.addProperty(key, (String)_symbol.getProperties().getProperties().get(key));
				}
				return component;
		}
		else if((((String)_symbol.getProperty("CATEGORY")).equalsIgnoreCase("PROCEDURE"))){
			CompilationUnitComponent component = new CompilationUnitComponent(name);
			for(String key : _symbol.getProperties().getProperties().keySet()){
				if(_symbol.getProperties().getProperties().get(key) instanceof String)
					component.addProperty(key, (String)_symbol.getProperties().getProperties().get(key));
			}
			return component;
		}
		else if((((String)_symbol.getProperty("CATEGORY")).equalsIgnoreCase("UI"))){
			CompilationUnitComponent component = new CompilationUnitComponent(name);
			for(String key : _symbol.getProperties().getProperties().keySet()){
				if(_symbol.getProperties().getProperties().get(key) instanceof String)
					component.addProperty(key, (String)_symbol.getProperties().getProperties().get(key));
			}
			return component;
		}			
		return null;
	}
	
	private void addProperties(Symbol_b _sym, CompilationUnitComponent _component){
		for(String key : _sym.getProperties().getProperties().keySet()){
			if(_sym.getProperties().getProperties().get(key) instanceof String)
				_component.getProperties().addProperty(key, (String)_sym.getProperties().getProperties().get(key));
		}		
	}
}
