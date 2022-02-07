package br.com.bicam.util.catalog;

import java.util.ArrayList;
import java.util.HashSet;
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

public class SequenceCallVb6 extends VisualBasic6BaseListener{
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	String          separator;
	StringBuffer    sb;
	
	Set<String> calledModuleInMethod;
	Symbol_New currentMethod;
	Symbol_New compilationUnitSymbol;
	Set<String> uiComponenteName;
	Symbol_New appEntryPointSym; // method in Method or UI
	
	NodeList nodes;
	BicamNode appNode;
	BicamNode procedureNode;
	BicamNode formNode;
	String entryPoint;
	
	KeywordLanguage keywordLanguage;
	
	String appName;
	List<String> formNames;
	
	final String[] eventUIProcedureList = new String[] {"_CLICK","_DBLCLICK","_KEYPRESS"};
	
	final String[] preShowProcedureList = new String[] {"FORM_LOAD","FORM_ACTIVATE"};
	
	public SequenceCallVb6(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		this.separator = (String) properties.getProperty("SEPARATOR"); 
		this.keywordLanguage = st.getKeywordsLanguage();
		
		nodes = (NodeList) properties.getProperty("NODELIST");
		
		calledModuleInMethod = new HashSet<String>();
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

//		BicamNode node = new BicamNode(nodeName);
		BicamNode node = nodes.create(nodeName);

		node.getProperties().addProperty("LABEL", sym.getName());
		node.getProperties().addProperty("TYPE", "Procedure");
//		nodes.add(node);
		procedureNode = node;
		
//===================================== EDGE ENTRYPOINT ====================================================		
		if(sym.getName().equalsIgnoreCase(entryPoint)) {
			String appEntryId = appNode.getId() + "." + sym.getEnclosingScope().getName() + "." + sym.getName();
//			BicamNode appEntryNode = new BicamNode(appEntryId);
			BicamNode appEntryNode = nodes.create(appEntryId);
			appEntryNode.getProperties().addProperty("LABEL", sym.getName());
			appEntryNode.getProperties().addProperty("TYPE", "Procedure");
	
//			nodes.add(appEntryNode);
			
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
//								  forms.get(0).getName() + "." +
                                  formName + "." +
								  symUIComponent.getName();
				}catch(IndexOutOfBoundsException e) {
					int i = 0;
					i++;
					System.exit(1);
				}
				
				BicamNode uiComponentnode = nodes.get(UiComponentId);//new BicamNode(UIName);
//				uiComponentnode.getProperties().addProperty("LABEL", symUIComponent.getName());
//				uiComponentnode.getProperties().addProperty("TYPE", symUIComponent.getProperty("CONTROL"));
//				nodes.add(uiComponentnode);
				
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
//		appNode = new BicamNode(appName);
		appNode = nodes.create(appName);
		
		properties.addProperty("APP_NODE", appNode);
		appNode.getProperties().addProperty("LABEL", appName);
		appNode.getProperties().addProperty("TYPE", "Projet");
		
//		nodes.add(appNode);
		
//		TIPMA001.STARTUP {type/hover=event, label=startup, visible=false}
		String startupEntry = appName + "." + "STARTUP";
//		BicamNode appStartupEvent = new BicamNode(startupEntry);
		BicamNode appStartupEvent = nodes.create(startupEntry);
		appStartupEvent.getProperties().addProperty("LABEL", "StartupEntry");
		appStartupEvent.getProperties().addProperty("TYPE", "Event");
		
//		TIPMA001.TIFMA001 {name=TIPMA001.TIFMA001,type/hover=Form, label=TIFMA001}
		String appEntry = (String)appSym.getProperty("STARTUP");
		entryPoint = appEntry.replaceAll("\"", "").toUpperCase();
		entryPoint = entryPoint.replaceAll(" ","").toUpperCase();
		entryPoint = entryPoint.replaceAll("SUB","").toUpperCase();
	}
	
/*	private void addPreShow(String _preProcess, ParserRuleContext ctx) {
		BicamNode appNode = nodes.get(appName);
		String compUnitName = compilationUnitSymbol.getName();
		String preShowName = appName + "." + compUnitName + "." + _preProcess.toUpperCase();
//		TIPMA001.<compUnit>.FORM_LOAD/FORM_ACTIVATE {type/hover=procedure, label=form_load/form_activate, visible=false}
		BicamNode preShowNode = new BicamNode(preShowName);
		preShowNode.getProperties().addProperty("LABEL",preShowName);
		preShowNode.getProperties().addProperty("TYPE","Procedure");
 
		BicamAdjacency adj = new BicamAdjacency(preShowNode);
		adj.getProperties().addProperty("LABEL","Pre Show");
		adj.getProperties().addProperty("TYPE","Procedure");

		appNode.addAdjacency(adj);
	}*/
	
/*	private void addFormLoad() {
		List<Symbol_New> symAppList = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		sb.append(symApp.getName() + separator + compilationUnitSymbol.getName() + "." + "Form_Load" + System.lineSeparator());		
	}
	
	private void addFormActivate() {
		List<Symbol_New> symAppList = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		sb.append(symApp.getName() + separator + compilationUnitSymbol.getName() + "." + "Form_Activate" + System.lineSeparator());		
	}*/	
	
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
//		BicamNode node = new BicamNode(formId);
		BicamNode node = nodes.create(formId);	
		node.getProperties().addProperty("LABEL", sym.getName());
		node.getProperties().addProperty("TYPE", sym.getProperty("CONTROL"));

//		nodes.add(node);
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
//		BicamNode node = new BicamNode(uiComponentId);
		BicamNode node = nodes.create(uiComponentId);
		node.getProperties().addProperty("LABEL", sym.getName());
		node.getProperties().addProperty("TYPE", sym.getProperty("CONTROL"));

//		nodes.add(node);
		
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
		if(!calledModuleInMethod.contains(sym.getName())) {
			calledModuleInMethod.add(sym.getName());

			String compUnitName = sym.getEnclosingScope().getName();
			String nodeName = appNode.getId() + "." +
							  compUnitName + "." +
					          sym.getName();

//			BicamNode node = new BicamNode(nodeName);
			BicamNode node = nodes.create(nodeName);
			node.getProperties().addProperty("LABEL", sym.getName());
			node.getProperties().addProperty("TYPE", "Procedure");
//			nodes.add(node);
			
			BicamAdjacency adj = new BicamAdjacency(node);
			adj.getProperties().addProperty("LABEL", sym.getName());
			adj.getProperties().addProperty("TYPE", "Procedure");
			procedureNode.addAdjacency(adj);
		}
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
//							 sym.getEnclosingScope().getName() + "." +
//							 sym.getName();
                             compUnitName + "." +
                             formName;
			
//			BicamNode node = new BicamNode(nodeId);
			BicamNode node = nodes.create(nodeId);
			
			node.getProperties().addProperty("LABEL", sym.getName());
			node.getProperties().addProperty("TYPE", "Form");
//			nodes.add(node);
			
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
				
//				String compUnitName = (String) compilationUnitSymbol.getProperty("MODULE_NAME");
				String nodeName = appNode.getId() + "." +
						st.getCompilationUnitName(sym.getContext()) + "." +
						          sym.getName();
//				BicamNode node = new BicamNode(nodeName);
				BicamNode node = nodes.create(nodeName);				
				node.getProperties().addProperty("LABEL", sym.getName());
				node.getProperties().addProperty("TYPE", "Procedure");
//				nodes.add(node);
				
			    BicamAdjacency adj = new BicamAdjacency(node);
			    adj.getProperties().addProperty("LABEL", "CALL");
			    adj.getProperties().addProperty("TYPE", "PATH");
			    
			    procedureNode.addAdjacency(adj);
			}
		}
	}

	@Override 
	public void enterImplicitCallStmt(@NotNull ImplicitCallStmtContext ctx) {
		IdentifierContext idContext = (IdentifierContext) NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext");
		Symbol_New sym = st.getSymbol(idContext);

		if(((String)sym.getProperty("CATEGORY")).equalsIgnoreCase("PROCEDURE")) {
			if(!calledModuleInMethod.contains(sym.getName())) {
				calledModuleInMethod.add(sym.getName());
				
//				String compUnitName = (String) compilationUnitSymbol.getProperty("MODULE_NAME");
				String nodeName = appNode.getId() + "." +
								  st.getCompilationUnitName(sym.getContext()) + "." +
						          sym.getName();
//				BicamNode node = new BicamNode(nodeName);
				BicamNode node = nodes.create(nodeName);				
				node.getProperties().addProperty("LABEL", sym.getName());
				node.getProperties().addProperty("TYPE", "Procedure");
//				nodes.add(node);
				
			    BicamAdjacency adj = new BicamAdjacency(node);
			    adj.getProperties().addProperty("LABEL", "CALL");
			    adj.getProperties().addProperty("TYPE", "PATH");
			    
			    procedureNode.addAdjacency(adj);
			}
		}		
//==================================================================================		
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
	
}