package br.com.bicam.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.RealParameterContext;
import br.com.bicam.util.graph.BicamAdjacency;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

/*
 * Em Visual Basic 6:
 * Exemplo 1. Para localizar o texto "Número do Sorteio não Encontrado!"
 * => statement.... MsgBox "Número do Sorteio não Encontrado!", MB_ICONINFORMATION, txt_msg$
 * => baseName = MsgBox
 * => targetParameterName = "Número do Sorteio não Encontrado!" // parametro com este texto
 * => baseParameterName = null
 * => baseParameterIndex = 0, primeiro parametro
 * => targetParameterIndex = 0
 * 
 * Exemplo 2. Para localizar  stored procedure (qualquer uma, mas no caso é :prod30.dbnpesso..PR_PES_FIG_L_S07567
 * => statement...     ret% = SQLRPCInit%(SqlConn%, "prod30.dbnpesso..PR_PES_FIG_L_S07567", 0)
 * => baseName = SQLRPCInit
 * => targetParameterName = null  // a busca não será por nada específico
 * => baseParameterName = SqlConn
 * => baseParameterIndex = null
 * => targetParameterIndex = 1
 *  * 
 * Exemplo 3. Para localizar  stored procedure (a stored procedure prod30.dbnpesso..PR_PES_FIG_L_S07567
 * => statement...     ret% = SQLRPCInit%(SqlConn%, "prod30.dbnpesso..PR_PES_FIG_L_S07567", 0)
 * => baseName = SQLRPCInit
 * => targetParameterName = prod30.dbnpesso..PR_PES_FIG_L_S07567
 * => baseParameterName = SqlConn
 * => baseParameterIndex = null
 * => targetParameterIndex = 1
 * * 
 */

public class GetThingByParameter extends VisualBasic6BaseListener{
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	String          baseName = null;
	String          baseParameterName = null;
	String          targetParameterName = null;
	Integer         baseParameterIndex = null;
	Integer         targetParameterIndex = null;
	
	Symbol_New currentMethod;
	Symbol_New compilationUnit;
	
	BicamNode appNode;
	BicamNode methodNode;
	
	Set<String> stpList; // lista das storedProcedures localizadas
	Map<String,List<String>> dbAccessNodeMap;
	
	
	NodeList nodeList;
	
	public GetThingByParameter(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		this.nodeList = (NodeList) properties.getProperty("NODE_LIST");
		this.appNode = (BicamNode) properties.getProperty("APP_NODE");
		this.dbAccessNodeMap = (Map) properties.getProperty("DB_ACCESS_NODE_MAP");

		this.baseName = (getParameter("BASE_NAME") == null ? null 
			             : (String)getParameter("BASE_NAME"));
		
		this.baseParameterName = (getParameter("BASE_PARAMETER_NAME") == null ? null 
                               : (String)getParameter("BASE_PARAMETER_NAME"));
		
		this.targetParameterName = (getParameter("TARGET_PARAMETER_NAME") == null ? null 
                               : (String)getParameter("TARGET_PARAMETER_NAME"));
		
		this.baseParameterIndex = (getParameter("BASE_PARAMETER_INDEX") == null ? null 
                : (Integer)getParameter("BASE_PARAMETER_INDEX"));

       this.targetParameterIndex = (getParameter("TARGET_PARAMETER_INDEX") == null ? null 
                : (Integer)getParameter("TARGET_PARAMETER_INDEX"));		

		this.stpList = new HashSet<String>();
	}
	
	private Object getParameter(String _propertyName) {
		return (properties.getProperty(_propertyName) != null ? properties.getProperty(_propertyName) 
				                                               : null);
	}
	
	@Override 
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		compilationUnit = st.getSymbol(ctx);
//		setAppEntryPoint(ctx);
	}
	
/*	private void setAppEntryPoint(ParserRuleContext compUnitCtx) {
		Symbol_New appSym = (Symbol_New) compilationUnit.getProperty("ENCLOSING_SCOPE");
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
		appNode = nodeList.create(appName);
		appNode.getProperties().addProperty("LABEL", appName);
		appNode.getProperties().addProperty("TYPE", "Projet");
		
//		nodes.add(appNode);
		
//		TIPMA001.STARTUP {type/hover=event, label=startup, visible=false}
		String startupEntry = appName + "." + "STARTUP";
//		BicamNode appStartupEvent = new BicamNode(startupEntry);
		BicamNode appStartupEvent = nodeList.create(startupEntry);
		appStartupEvent.getProperties().addProperty("LABEL", "StartupEntry");
		appStartupEvent.getProperties().addProperty("TYPE", "Event");
		
//		TIPMA001.TIFMA001 {name=TIPMA001.TIFMA001,type/hover=Form, label=TIFMA001}
		String appEntry = (String)appSym.getProperty("STARTUP");
		entryPoint = appEntry.replaceAll("\"", "").toUpperCase();
		entryPoint = entryPoint.replaceAll(" ","").toUpperCase();
		entryPoint = entryPoint.replaceAll("SUB","").toUpperCase();
	}	
*/	
	@Override 
	public void enterMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) {
		Symbol_New sym = st.getSymbol(ctx);
		String compUnitName = (String) compilationUnit.getName();
		String nodeName = appNode.getId() + "." +
						  compUnitName + "." +
				          sym.getName();

		methodNode = nodeList.get(nodeName);
		
//		ParserRuleContext stmtContext = NodeExplorer.getAncestorClass(ctx, "StmtContext", true);

		String startIndexContext = null;
		String lineContext = null;
		
//		if(stmtContext != null) { // create Node
			startIndexContext = Integer.toString(ctx.start.getStartIndex());
			lineContext = Integer.toString(ctx.start.getLine());
			String nodeId = startIndexContext + ":" + lineContext;
			BicamNode node = new BicamNode(nodeId);
			BicamAdjacency adj = new BicamAdjacency(node);
			methodNode.addAdjacency(adj);
//		}
//		else {
//			BicamSystem.printLog("ERROR", "stmtContext is NULL");
//		}
		
	}
	
	@Override 
	public void enterRealParameter(@NotNull VisualBasic6Parser.RealParameterContext ctx) {
		if(baseParameterName != null) withBaseParameterName(ctx);
		else if(baseParameterIndex != null) withBaseParameterIndex(ctx);
	}
	
	private void withBaseParameterName(ParserRuleContext ctx) {
		if(!ctx.getText().toUpperCase().startsWith(baseParameterName.toUpperCase())) return;
		
		Integer baseIndex = NodeExplorer_New.getSiblingIndex(ctx,true);

		ParserRuleContext realParmList = (ParserRuleContext) NodeExplorer_New.getNextAncestorClass(ctx, "RealParametersContext");
		if(realParmList == null)
			realParmList = (ParserRuleContext) NodeExplorer_New.getNextAncestorClass(ctx, "RealParametersNoParenContext");

		IdentifierContext functionContext = (IdentifierContext) NodeExplorer_New.getFirstSibling(realParmList, "IdentifierContext");

		if(!functionContext.getText().toUpperCase().startsWith(baseName.toUpperCase())) return; // startswith para comportar type indicator: function%

		RealParameterContext realParameterContext = (RealParameterContext) NodeExplorer_New.getSiblingByIndex(ctx, baseIndex+targetParameterIndex, true);
		String storedProcName = realParameterContext.getText().replaceAll("\"", "");
		stpList.add(storedProcName);

//===============================================================================		
		String dbFullName = BicamSystem.sqlNameToFullQualifiedName(storedProcName);
		       dbFullName = dbFullName.toUpperCase();
		
		ParserRuleContext stmtContext = NodeExplorer.getAncestorClass(functionContext, "StmtContext", true);
		String startIndexContext = null;
		String lineContext = null;
		
		String stmtDbNodeId = null;
		if(stmtContext != null) { // create Node
			startIndexContext = Integer.toString(stmtContext.start.getStartIndex());
			lineContext = Integer.toString(stmtContext.start.getLine());
			stmtDbNodeId = startIndexContext + ":" + lineContext;
			BicamNode node = new BicamNode(stmtDbNodeId);
		}
		else {
			BicamSystem.printLog("ERROR", "stmtContext is NULL");
		}
		
		List<String> list = dbAccessNodeMap.get(dbFullName);
		if(list == null) {
			list = new ArrayList<String>();
			dbAccessNodeMap.put(dbFullName, list);
		}
		list.add(stmtDbNodeId);
		
		BicamNode dbNode = new BicamNode(dbFullName);
		BicamAdjacency adj = new BicamAdjacency(dbNode);
		methodNode.addAdjacency(adj);		
	}
	
	
	private void withBaseParameterIndex(ParserRuleContext ctx) {
		
		Integer baseIndex = baseParameterIndex;
		
		ParserRuleContext realParmList = (ParserRuleContext) NodeExplorer_New.getNextAncestorClass(ctx, "RealParametersContext");
		if(realParmList == null)
			realParmList = (ParserRuleContext) NodeExplorer_New.getNextAncestorClass(ctx, "RealParametersNoParenContext");

//		RuleContext functionContext = NodeExplorer_New.getFirstSibling(realParmList);
		ParseTree functionContext = NodeExplorer_New.getFirstSibling(realParmList);

		if(!functionContext.getText().toUpperCase().startsWith(baseName.toUpperCase())) return; // startswith para comportar type indicator: function%
		
//		RealParameterContext realParameterContext = (RealParameterContext) NodeExplorer_New.getNextSibling(ctx, "RealParameterContext");
//		String storedProcName = realParameterContext.getText().replaceAll("\"", "");

		RealParameterContext realParameterContext = (RealParameterContext) NodeExplorer_New.getSiblingByIndex(ctx, baseIndex+targetParameterIndex, true);
		String storedProcName = realParameterContext.getText().replaceAll("\"", "");
		
		stpList.add(storedProcName);
//=================================================================================
		String dbFullName = BicamSystem.sqlNameToFullQualifiedName(storedProcName);
		       dbFullName = dbFullName.toUpperCase();
		
		ParserRuleContext stmtContext = NodeExplorer.getAncestorClass((ParserRuleContext)functionContext, "StmtContext", true);
		String startIndexContext = null;
		String lineContext = null;
		
		String stmtDbNodeId = null;
		if(stmtContext != null) { // create Node
			startIndexContext = Integer.toString(stmtContext.start.getStartIndex());
			lineContext = Integer.toString(stmtContext.start.getLine());
			stmtDbNodeId = startIndexContext + ":" + lineContext;
			BicamNode node = new BicamNode(stmtDbNodeId);
		}
		else {
			BicamSystem.printLog("ERROR", "stmtContext is NULL");
		}
		
		List<String> list = dbAccessNodeMap.get(dbFullName);
		if(list == null) {
			list = new ArrayList<String>();
			dbAccessNodeMap.put(dbFullName, list);
		}
		list.add(stmtDbNodeId);
		
		BicamNode dbNode = new BicamNode(dbFullName);
		BicamAdjacency adj = new BicamAdjacency(dbNode);
		methodNode.addAdjacency(adj);	
	}	
	
	public List<String> getStoredProcedures() {
		return new ArrayList<String>(stpList);
	}
	
	public Map<String, List<String>> getNodeMap() {
		return dbAccessNodeMap;
	}
}