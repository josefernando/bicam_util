package br.com.bicam.util.catalog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.CallStmtContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.ImplicitCallStmtContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.RealParametersContext;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.graph.BicamAdjacency;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class SequenceCallVb6_New extends VisualBasic6BaseListener{
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	String          separator;
	StringBuffer    sb;
	
    IdentityHashMap<Symbol_New,ArrayDeque<Symbol_New>> nestedCall;
    Set<ParserRuleContext> visitedContext;

    ArrayDeque<Symbol_New> currentNestedCall;
	
	Set<String> calledModuleInMethod;
	Symbol_New currentMethod;
	Symbol_New compilationUnitSymbol;
	Set<String> uiComponenteName;
	Symbol_New appEntryPointSym; // method in Method or UI
	
	NodeList nodes;
	NodeList app_NodeList;
	BicamNode appNode;
	BicamNode procedureNode;
	BicamNode formNode;
	String entryPoint;
	
	KeywordLanguage keywordLanguage;
	boolean caseSensitive;
	
	String appName;
	List<String> formNames;
	
	final String[] eventUIProcedureList = new String[] {"_CLICK","_DBLCLICK","_KEYPRESS"};
	
	final String[] preShowProcedureList = new String[] {"FORM_LOAD","FORM_ACTIVATE"};
	
	public SequenceCallVb6_New(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		this.separator = (String) properties.getProperty("SEPARATOR"); 
		this.keywordLanguage = st.getKeywordsLanguage();
		this.caseSensitive = (Boolean)keywordLanguage.getProperties().getProperty("CASE_SENSITIVE");
		
		nodes = (NodeList) properties.getProperty("NODELIST");
		app_NodeList = (NodeList) properties.getProperty("APP_NODELIST");
		
		calledModuleInMethod = new HashSet<String>();
		visitedContext = new    HashSet<ParserRuleContext>();

		nestedCall = new IdentityHashMap<Symbol_New,ArrayDeque<Symbol_New>>();
		currentNestedCall = new ArrayDeque<Symbol_New>();
		sb = new StringBuffer();
		
		formNames = new ArrayList<String>();
		uiComponenteName = new HashSet<String>();
	}
	
	@Override 
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
		setAppEntryPoint(ctx);
	}
	
	@Override 
	public void exitStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		System.err.println("============>  " + compilationUnitSymbol.getName() );
		System.err.println(nodes.inputSymbolGraph());
	}
	
	@Override 
	public void enterMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) {
		calledModuleInMethod.clear();
		Symbol_New sym = st.getSymbol(ctx);
		String compUnitName = (String) compilationUnitSymbol.getName();
		String nodeName = appNode.getId() + "." +
						  compUnitName + "." +
				          sym.getName();

		BicamNode node = nodes.create(nodeName);

		node.getProperties().addProperty("LABEL", sym.getName());
		node.getProperties().addProperty("TYPE", "Procedure");
		procedureNode = node;
		//==============
		visitedContext.add(ctx);
		nestedCall.put(sym, new ArrayDeque<Symbol_New>());
		currentNestedCall.push(sym);
		//==============
		
//===================================== EDGE ENTRYPOINT ====================================================		
		if(sym.getName().equalsIgnoreCase(entryPoint)) {
			String appEntryId = appNode.getId() + "." + sym.getEnclosingScope().getName() + "." + sym.getName();
			BicamNode appEntryNode = nodes.create(appEntryId);
			appEntryNode.getProperties().addProperty("LABEL", sym.getName());
			appEntryNode.getProperties().addProperty("TYPE", "Procedure");
			
			BicamAdjacency startupEdge = new BicamAdjacency(appEntryNode);
			startupEdge.getProperties().addProperty("LABEL", "STARTUP");
			startupEdge.getProperties().addProperty("TYPE", "PROCEDURE");
			appNode.addAdjacency(startupEdge);
		}
//===================================== EDGE EVENT CLICK, DBLCLICK, ... ====================================================		
        for(String event : eventUIProcedureList) {
			if(sym.getName().toUpperCase().endsWith(event)) {
				int ixEvent = procedureNode.getId().toUpperCase().indexOf(event);
				String uiComponentName = procedureNode.getId().substring(0, ixEvent);
 				uiComponentName = uiComponentName.split("\\.")[uiComponentName.split("\\.").length-1];
				
				PropertyList prop = new PropertyList();
				prop.addProperty("NAME", uiComponentName);
				prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
				
				Symbol_New symUIComponent = compilationUnitSymbol.findMember(prop);
				
				if(symUIComponent == null) {
					BicamSystem.printLog("WARNING", " EMPTY PROCEDURE: " + sym.getName());
					return;
				}
				
				List<Symbol_New> forms = st.getSymbolByProperty(compilationUnitSymbol, "CONTROL","Form");

				if(forms.isEmpty()) {
					forms = st.getSymbolByProperty(compilationUnitSymbol, "CONTROL","MDIForm");
					if(forms.isEmpty()) {
						BicamSystem.printLog("WARNING", "FORM MISSING");
						return;					
					}
				}

				String UiComponentId = symUIComponent.getName();
				try {
				String formName = forms.get(0).getName();
				if(forms.get(0).getProperty("THIS") != null) {
					formName = ((Symbol_New)forms.get(0).getProperty("THIS")).getName();
				}
				UiComponentId = appNode.getId() + "." +
								  compUnitName + "." +
                                  formName + "." +
								  symUIComponent.getName();
				}catch(IndexOutOfBoundsException e) {
					int i = 0;
					i++;
					System.exit(1);
				}
				
				BicamNode uiComponentnode = nodes.get(UiComponentId);//new BicamNode(UIName);
				
				BicamAdjacency adj = new BicamAdjacency(procedureNode);
				adj.getProperties().addProperty("LABEL", event.substring(1));
				adj.getProperties().addProperty("TYPE", "UI"); // e.g._CLICK -> CLICK
				uiComponentnode.addAdjacency(adj);
			}	
        }
//====================================  timer
		if(sym.getName().endsWith("_Timer")) {
			String timerId = appNode.getId() + "." + sym.getEnclosingScope().getName() + "." + sym.getName();
			BicamNode timerIdNode = nodes.create(timerId);
			timerIdNode.getProperties().addProperty("LABEL", sym.getName());
			timerIdNode.getProperties().addProperty("TYPE", "Procedure");
			
			BicamAdjacency adj = new BicamAdjacency(timerIdNode);
			adj.getProperties().addProperty("LABEL", "TIMER");
			adj.getProperties().addProperty("TYPE", "PROCEDURE");
			formNode.addAdjacency(adj);
		}        
	}
	
	public void exitMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) {
		nestedCall.remove(currentNestedCall.peek());
		currentNestedCall.pop();
	}
	
	private void setAppEntryPoint(ParserRuleContext compUnitCtx) {
		Symbol_New appSym = (Symbol_New) compilationUnitSymbol.getProperty("ENCLOSING_SCOPE");
		if(appSym == null) {
			try {
				throw new  Exception();
			} catch (Exception e) {
				BicamSystem.printLog("WARNING", "NO APPLICATION SYMBOL FOUND", e);
				return;
			}
		}
//TIPMA001 {name=TIPMA001, type/hover=project, label=<project title>}
		String appName = (String) appSym.getProperty("NAME");

		appNode = app_NodeList.create(appName); // if  it already exists then get it otherwise create it
		
		properties.addProperty("APP_NODE", appNode);
		appNode.getProperties().addProperty("LABEL", appName);
		appNode.getProperties().addProperty("TYPE", "Projet");
		
//		TIPMA001.STARTUP {type/hover=event, label=startup, visible=false}
		String startupEntry = appName + "." + "STARTUP";
		BicamNode appStartupEvent = nodes.create(startupEntry);
		appStartupEvent.getProperties().addProperty("LABEL", "StartupEntry");
		appStartupEvent.getProperties().addProperty("TYPE", "Event");
		
//		TIPMA001.TIFMA001 {name=TIPMA001.TIFMA001,type/hover=Form, label=TIFMA001}
		String appEntry = (String)appSym.getProperty("STARTUP");
		entryPoint = appEntry.replaceAll("\"", "").toUpperCase();
		entryPoint = entryPoint.replaceAll(" ","").toUpperCase();
		entryPoint = entryPoint.replaceAll("SUB","").toUpperCase();
	}
	
	@Override 
	public void enterGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx) {
		Symbol_New symUI = st.getSymbol(ctx);
		if(symUI == null) {
			BicamSystem.printLog(st, ctx, "WARNING", "NOT FOUND form IN APPLICATION ");
		}
		else {
			if(symUI.hasProperty("CONTROL", "Form") || symUI.hasProperty("CONTROL", "MDIForm")) createFormNode(ctx);
			else {
				uiComponentNode(ctx);
			}
		}
	}
	
	private void createFormNode(ParserRuleContext ctx) {
		Symbol_New sym = st.getSymbol(ctx);
		formNames.add(sym.getName());
		
		String formId = appNode.getId() + "." + 
				sym.getEnclosingScope().getName() + "." +
				sym.getName();
		
//		formId = getCaseSentitiveName(formId);
		
		BicamNode node = nodes.create(formId);	
		node.getProperties().addProperty("LABEL", sym.getName());
		node.getProperties().addProperty("TYPE", sym.getProperty("CONTROL"));

		formNode = node;
		
//==============================================================================================		
		if(sym.getName().equalsIgnoreCase(entryPoint)) {
			BicamAdjacency startupEdge = new BicamAdjacency(node);
			startupEdge.getProperties().addProperty("LABEL", "STARTUP");
			startupEdge.getProperties().addProperty("TYPE", "FORM");
			appNode.addAdjacency(startupEdge);
		}		
	}

	private void uiComponentNode(ParserRuleContext ctx) {
		Symbol_New sym = st.getSymbol(ctx);
		uiComponenteName.add(sym.getName());

		String uiComponentId = formNode.getId() + "." +	sym.getName();
		BicamNode node = nodes.create(uiComponentId);
		node.getProperties().addProperty("LABEL", sym.getName());
		node.getProperties().addProperty("TYPE", sym.getProperty("CONTROL"));
		
		BicamAdjacency uiComponentEdge = new BicamAdjacency(node);
		uiComponentEdge.getProperties().addProperty("LABEL", sym.getProperty("CONTROL"));
		uiComponentEdge.getProperties().addProperty("TYPE", "CONTAINS");
		
		formNode.addAdjacency(uiComponentEdge);		
	}
	
	@Override 
	public void enterRealParameters(@NotNull RealParametersContext ctx) {
		ParserRuleContext context = NodeExplorer_New.getFirstSibling(ctx, "IdentifierContext");
		if(NodeExplorer_New.getAncestorClass(context, "VariableStmtContext") != null) return;
		if(NodeExplorer_New.getAncestorClass(context, "GuiAttributeSettingContext") != null) return;
		Symbol_New sym = st.getSymbol(NodeExplorer_New.getAncestorClass(context,"IdentifierContext"));
        if(((String)sym.getProperties().getProperty("CATEGORY")).equalsIgnoreCase("VARIABLE")) return;

        if(!calledModuleInMethod.contains(sym.getName())) {
			calledModuleInMethod.add(sym.getName());
			
			String nodeName = null;
			if(sym.getContext() != null) {
				nodeName = appNode.getId() + "." + sym.getEnclosingScope().getName() + "." + sym.getName();
			}
			else {
				/*					nodeName = (String)sym.getProperties().getProperty("LANGUAGE") + "." 
			    + (String)sym.getProperties().getProperty("CATEGORY_TYPE") + "."
                + getBuitinCompilationUnit(sym);*/
				nodeName = BicamSystem.getBuitinCompilationUnit(sym);
			}
			
//			String compUnitName = sym.getEnclosingScope().getName();


			BicamNode node = nodes.create(nodeName);
			node.getProperties().addProperty("LABEL", sym.getName());
			node.getProperties().addProperty("TYPE", "Procedure");
			
			BicamAdjacency adj = new BicamAdjacency(node);
			adj.getProperties().addProperty("LABEL", sym.getName());
			adj.getProperties().addProperty("TYPE", "Procedure");
			procedureNode.addAdjacency(adj);
			//==============
			visitedContext.add(ctx);
			nestedCall.put(sym, new ArrayDeque<Symbol_New>());
			currentNestedCall.push(sym);
			//==============
		}
	}
	
	public void exitRealParameters(@NotNull RealParametersContext ctx) {
		if(!visitedContext.contains(ctx)) return;
		nestedCall.remove(currentNestedCall.peek());
		Symbol_New sym = currentNestedCall.pop();	
//		System.err.format("CALL ==>  %s -> %s%n", currentNestedCall.peek().getId(),sym.getId());
	}
	
	@Override 
	public void enterIdentifier(@NotNull IdentifierContext ctx) {
		Symbol_New sym = null;
		if(st.getSymbol(ctx) != null && st.getSymbol(ctx).getName().equalsIgnoreCase("Show")) {
			
			sym = (Symbol_New) st.getSymbol(ctx).getEnclosingScope();
			String formName = null;
			String compUnitName = null;
			
			if(sym.getName().equalsIgnoreCase("GLOBAL")) { // Show
				formName = formNames.get(0);
				compUnitName = compilationUnitSymbol.getName();
			}
			else {                                        // <form>.Show
				formName = sym.getName();
				compUnitName = sym.getEnclosingScope().getName();
			}
			
			String nodeId  = appNode.getId() + "." +
                             compUnitName + "." +
                             formName;
			
			BicamNode node = nodes.create(nodeId);
			
			node.getProperties().addProperty("LABEL", sym.getName());
			node.getProperties().addProperty("TYPE", "Form");
		    BicamAdjacency adj = new BicamAdjacency(node);
		    adj.getProperties().addProperty("LABEL", "SHOW");
		    adj.getProperties().addProperty("TYPE", "FORM");			
			
		    procedureNode.addAdjacency(adj);			
		}
	}

	@Override 
	public void enterCallStmt(@NotNull CallStmtContext ctx) {
		IdentifierContext idContext = (IdentifierContext) NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext");
		Symbol_New sym = st.getSymbol(idContext);

		if(((String)sym.getProperty("CATEGORY")).equalsIgnoreCase("PROCEDURE")) {
			if(!calledModuleInMethod.contains(sym.getName())) {
				calledModuleInMethod.add(sym.getName());
				
				String nodeName = appNode.getId() + "." +
						st.getCompilationUnitName(sym.getContext()) + "." +
						          sym.getName();
				BicamNode node = nodes.create(nodeName);				
				node.getProperties().addProperty("LABEL", sym.getName());
				node.getProperties().addProperty("TYPE", "Procedure");
				
			    BicamAdjacency adj = new BicamAdjacency(node);
			    adj.getProperties().addProperty("LABEL", "CALL");
			    adj.getProperties().addProperty("TYPE", "PATH");
			    
			    procedureNode.addAdjacency(adj);
				//==============
			    visitedContext.add(ctx);
				nestedCall.put(sym, new ArrayDeque<Symbol_New>());
				currentNestedCall.push(sym);
				//==============			    
			}
		}
	}
	
	public void exitCallStmt(@NotNull CallStmtContext ctx) {
		if(!visitedContext.contains(ctx)) return;
		nestedCall.remove(currentNestedCall.peek());
		Symbol_New sym = currentNestedCall.pop();	
//		System.err.format("CALL ==>  %s -> %s%n", currentNestedCall.peek().getId(),sym.getId());
	}
	
/*	private String getBuitinCompilationUnit(Symbol_New _sym) {
		if(_sym.getEnclosingScope().getEnclosingScope() == null) {
			return (String)_sym.getProperties().getProperty("LANGUAGE") + "." 
				    + (String)_sym.getProperties().getProperty("CATEGORY_TYPE") + "." 
					+ _sym.getName();
		}
		else {
			return getBuitinCompilationUnit((Symbol_New)_sym.getEnclosingScope()) + "." +_sym.getName();
		}
	}*/

	@Override 
	public void enterImplicitCallStmt(@NotNull ImplicitCallStmtContext ctx) {
		IdentifierContext idContext = (IdentifierContext) NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext");
		Symbol_New sym = st.getSymbol(idContext);

//		if(((String)sym.getProperty("CATEGORY")).equalsIgnoreCase("PROCEDURE")) {
			if(!calledModuleInMethod.contains(sym.getName())) {
				calledModuleInMethod.add(sym.getName());
				
				String nodeName = null;
				
				if(sym.getContext() != null) {
					nodeName = appNode.getId() + "." + sym.getEnclosingScope().getName() + "." + sym.getName();
				}
				else {
/*					nodeName = (String)sym.getProperties().getProperty("LANGUAGE") + "." 
							    + (String)sym.getProperties().getProperty("CATEGORY_TYPE") + "."
				                + getBuitinCompilationUnit(sym);*/
					nodeName = BicamSystem.getBuitinCompilationUnit(sym);
				}				
				
/*				if(sym.getContext() == null ) {
					nodeName = appNode.getId() + "." + getBuitinCompilationUnit(sym) + "." + sym.getName();
				}
				else {
					nodeName = appNode.getId() + "." + st.getCompilationUnitName(sym.getContext()) + "." + sym.getName();
				}*/
				
				BicamNode node = nodes.create(nodeName);				
				node.getProperties().addProperty("LABEL", sym.getName());
				node.getProperties().addProperty("TYPE", "Procedure");
				
			    BicamAdjacency adj = new BicamAdjacency(node);
			    adj.getProperties().addProperty("LABEL", "CALL");
			    adj.getProperties().addProperty("TYPE", "PATH");
			    
			    procedureNode.addAdjacency(adj);
				//==============.
			    visitedContext.add(ctx);
				nestedCall.put(sym, new ArrayDeque<Symbol_New>());
				currentNestedCall.push(sym);
				//==============			    
			}
//		}		
//==================================================================================		
	}
	
	public void exitImplicitCallStmt(@NotNull ImplicitCallStmtContext ctx) {
		if(!visitedContext.contains(ctx)) return;
		nestedCall.remove(currentNestedCall.peek());
		Symbol_New sym = currentNestedCall.pop();	
//		System.err.format("CALL ==>  %s -> %s%n", currentNestedCall.peek().getId(),sym.getId());
	}

	
	public List<String> findFormNames(){
		List<Symbol_New> listForms = st.getSymbolByProperty("CONTROL", "Form");
		if(listForms.size() == 0) listForms = st.getSymbolByProperty("CONTROL", "MDIForm");
		for(Symbol_New sym : listForms) {
			formNames.add(sym.getName());
		}
		return formNames;
	}
	
	public List<Symbol_New> getFormSymbols(){
		List<Symbol_New> listForms = st.getSymbolByProperty("CONTROL", "Form");
		if(listForms.size() == 0) listForms = st.getSymbolByProperty("CONTROL", "MDIForm");
		return listForms;
	}	
	
	public List<String> getFormNames(){
		return formNames;
	}
	
	public String getappName(){
		return appName;
	}
	
	public String getCompUnitName(){
		return compilationUnitSymbol.getName();
	}	
	
	public String getInputGraph() {
		return sb.toString();
	}
	
	public NodeList getNodelist() {
		return nodes;
	}
	
	private String getCaseSentitiveName(String _name) {
		if (keywordLanguage.getProperties().getProperty("CASE_SENSITIVE") == null) return _name;
		boolean b = (boolean) keywordLanguage.getProperties().getProperty("CASE_SENSITIVE");
		return b == false ? _name : _name.toUpperCase();
	}
}