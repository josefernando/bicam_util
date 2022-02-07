package br.com.bicam.util.catalog;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.CallStmtContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.ImplicitCallStmtContext;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class UISequenceGraphBuilder extends VisualBasic6BaseListener{
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	String          separator;
	StringBuffer    sb;
	
	Set<String> calledModuleInMethod;
	Symbol_New currentMethod;
	Symbol_New compilationUnitSymbol;
	Set<String> uiComponenteName;
	
	String appName;
	List<String> formNames;
	
//	Parser_New parsedfile;
	
	final String[] eventUIProcedureList = new String[] {"_CLICK","_DBLCLICK"};
	
	final String[] preShowProcedureList = new String[] {"FORM_LOAD","FORM_ACTIVATE"};
	
	public UISequenceGraphBuilder(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		this.separator = (String) properties.getProperty("SEPARATOR");
//		this.parsedfile = (Parser_New) properties.getProperty("PARSED_FILE");
	//	List<Symbol_New> compUnitSymbolList = st.getSymbolByProperty("FILE_NAME", parsedfile.getFileName());
//		compilationUnitSymbol = compUnitSymbolList.get(0);
		
		calledModuleInMethod = new HashSet<String>();
		sb = new StringBuffer();
		
		formNames = new ArrayList<String>();
		uiComponenteName = new HashSet<String>();
	}
	
/*	@Override
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		List<Symbol_New> symList = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New sym = symList.get(0);
		String entryPointApp = (String) sym.getProperty("Startup");
		entryPointApp = entryPointApp.replace("\"", "");
		sb.append(sym.getName() + separator + entryPointApp + System.lineSeparator());
	}*/
	
	@Override 
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
	}
	
	@Override 
	public void enterMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) {
		Symbol_New symCompUnit = st.getCompilarionUnitSymbol(ctx);
		currentMethod = st.getSymbol(ctx);
		calledModuleInMethod.clear();
//		compilationUnitSymbol = st.getCompilarionUnitSymbol(ctx);
		
		for(String uiProcedureEvent: eventUIProcedureList) {
			if(currentMethod.getName().toUpperCase().endsWith(uiProcedureEvent.toUpperCase())){ // termina com "procedure_event" _Click, _Dblclick, ....
				for(String uicompName:uiComponenteName) { // is guicomponent como:  commandText, dataGrid, ....
					if((uicompName+uiProcedureEvent).equalsIgnoreCase(currentMethod.getName())){
						sb.append( formNames.get(0) + "." + uicompName + separator + compilationUnitSymbol.getName() + "." + currentMethod.getName()+ System.lineSeparator());

						sb.append( compilationUnitSymbol.getName() + "." +formNames.get(0) + separator +  formNames.get(0)+ "."+uicompName + System.lineSeparator());

						break;
					}
				}				
			}
		}
		
		for(String preShowProcedure: preShowProcedureList) { // Form_Load or Form_Activate
			if(currentMethod.getName().toUpperCase().equalsIgnoreCase(preShowProcedure)){ // termina com "procedure_event" _Click, _Dblclick,...
				sb.append( compilationUnitSymbol.getName() + "." + currentMethod.getName()
				+ separator + compilationUnitSymbol.getName() + "." +formNames.get(0)+ System.lineSeparator());
				
				if(preShowProcedure.toUpperCase().contains("LOAD")) addFormLoad();
				
				if(preShowProcedure.toUpperCase().contains("ACTIVATE")) addFormActivate();

				break;
			}
		}
	}
	
	private void addFormLoad() {
		List<Symbol_New> symAppList = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		sb.append(symApp.getName() + separator + compilationUnitSymbol.getName() + "." + "Form_Load" + System.lineSeparator());		
	}
	
	private void addFormActivate() {
		List<Symbol_New> symAppList = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		sb.append(symApp.getName() + separator + compilationUnitSymbol.getName() + "." + "Form_Activate" + System.lineSeparator());		
	}	
	
	@Override 
	public void enterGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx) {
		Symbol_New symUI = st.getSymbol(ctx);
		if(symUI == null) {
			try {
				throw new Exception();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			if(symUI.hasProperty("CONTROL", "Form") || symUI.hasProperty("CONTROL", "MDIForm")) formNames.add(symUI.getName());
			uiComponenteName.add(symUI.getName());
		}
	}

	@Override 
	public void enterIdentifier(@NotNull IdentifierContext ctx) {
		if(st.getSymbol(ctx) == null) {
/*			try {
				throw new Exception();
			} catch (Exception e) {
				System.err.format("*** ERROR: SYMBOL IS NULL AT COMPILATION UNIT NAME '%s' in line %d%n"
		                , st.getCompilarionUnitSymbol(ctx).getName()
		                , ctx.start.getLine());
				e.printStackTrace();
			}*/
		}
		else 
		if(st.getSymbol(ctx).getName().equalsIgnoreCase("Show")) {
			String formName = null;
			String formcompilationUnitSymbolName = null;
			
			if(st.getSymbol(ctx).getEnclosingScope().getName().equalsIgnoreCase("GLOBAL")) { // Show foi definido como builtin em Global scope
				discoverFormName();
				formName = formNames.get(0);
				formcompilationUnitSymbolName = compilationUnitSymbol.getName();
			}
			else { 
				formName = st.getSymbol(ctx).getEnclosingScope().getName();
				formcompilationUnitSymbolName = st.getSymbol(ctx).getEnclosingScope().getEnclosingScope().getName();
			}
			
			if(!calledModuleInMethod.contains(formName)) {
				calledModuleInMethod.add( formName); // evita duplicação de edges
//				Symbol_New symEnclosingScope = (Symbol_New) st.getSymbol(ctx).getEnclosingScope();

				sb.append(compilationUnitSymbol.getName() + "." + currentMethod.getName() + separator 
				+ formName + System.lineSeparator());
				
				sb.append(formName + separator + formcompilationUnitSymbolName +"."+ formName + System.lineSeparator());  
				
//=====================================================================
				if(currentMethod.getName().toUpperCase().endsWith("_TIMER")) {
					discoverFormName();
					formName = formNames.get(0);
					sb.append( compilationUnitSymbol.getName() +"."+ formName + separator+compilationUnitSymbol.getName() + "." + currentMethod.getName() + System.lineSeparator());
				}
//=====================================================================				
			}
		}
	}

	@Override 
	public void enterCallStmt(@NotNull CallStmtContext ctx) {
		IdentifierContext idContext = (IdentifierContext) NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext");
		Symbol_New sym = st.getSymbol(idContext);

		if(((String)sym.getProperty("CATEGORY")).equalsIgnoreCase("PROCEDURE")) {

			if(!calledModuleInMethod.contains(sym.getName())) {
				calledModuleInMethod.add(sym.getName());
				sb.append(compilationUnitSymbol.getName() + "." + currentMethod.getName() + separator + st.getCompilarionUnitSymbol(sym.getContext()).getName() + "."+sym.getName() + System.lineSeparator());
			}
		}
	}

	@Override 
	public void enterImplicitCallStmt(@NotNull ImplicitCallStmtContext ctx) {
		try {
			IdentifierContext idContext = (IdentifierContext) NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext");
			Symbol_New sym = st.getSymbol(idContext);
			if(((String)sym.getProperty("CATEGORY")).equalsIgnoreCase("PROCEDURE")) {
				if(!calledModuleInMethod.contains(sym.getName())) {
					calledModuleInMethod.add(sym.getName());
					sb.append(compilationUnitSymbol.getName() + "." + currentMethod.getName() + separator + st.getCompilarionUnitSymbol(sym.getContext()).getName() + "."+sym.getName() + System.lineSeparator());
				}
			}
		} catch (NullPointerException e) {
			System.err.format("*** ERROR: in COMPILATION UNINT '%s' IN LINE '%d'%n"
					, st.getCompilarionUnitSymbol(ctx).getName() ,ctx.start.getLine());
			
			e.printStackTrace();
		}
	}
	
	public List<String> discoverFormName(){
		List<Symbol_New> listForms = st.getSymbolByProperty("CONTROL", "Form");
		if(listForms.size() == 0) listForms = st.getSymbolByProperty("CONTROL", "MDIForm");
		for(Symbol_New sym : listForms) {
			formNames.add(sym.getName());
		}
		return formNames;
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
}