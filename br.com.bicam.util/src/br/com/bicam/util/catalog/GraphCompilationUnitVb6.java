package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.SC_CondContext;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.graph.BicamAdjacency;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.graph.SymbolWeightedGraph;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class GraphCompilationUnitVb6 extends VisualBasic6BaseListener {

	PropertyList properties;
	SymbolTable_New st;
	KeywordLanguage  keywordLanguage;
	SymbolFactory symbolFactory;
	ParserRuleContext previousNode;
	ParserRuleContext currentNode;
	ParserRuleContext endProcedureNode;
	
	ParserRuleContext startRuleContext;

	IdentityHashMap<ParserRuleContext, ConditionaStmtControl> conditionalControl;
	IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>> labelControl;
	IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>> endProcedureControl;
	IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>> selectCaseControl;
	IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>> whileControl;
	
	NodeList nodeList;
	Map<Integer,String> lineToNodeId;
	
	SymbolWeightedGraph compUnitGraph;

	Set<ParserRuleContext> previuosConditional;
	Set<ParserRuleContext> previuosConditionalSave;
	
	final String[] noVisitStmt = new String[] {"MethodDefinitionContext"
			, "IfThenElseStmtContext"
			, "GoToStmtContext"
			, "ExitStmtContext"
			, "DoLoopStmtContext"
			, "SelectCaseStmtContext"
			};
	
	final String[] inconditionalBrach = new String[] {""}; 
	
	public GraphCompilationUnitVb6(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty(SYMBOLTABLE);
		this.symbolFactory = (SymbolFactory) properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);
		conditionalControl = new IdentityHashMap<ParserRuleContext, ConditionaStmtControl>();
		previuosConditional = new HashSet<ParserRuleContext>();
		previuosConditionalSave = new HashSet<ParserRuleContext>();
		labelControl = new IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>>();
		endProcedureControl = new IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>>();
		selectCaseControl   = new IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>>();

		whileControl = new IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>>();

		nodeList = new NodeList();
		lineToNodeId = new HashMap<Integer,String>();
	}
	
	@Override 
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		startRuleContext = ctx; // apenas para o controle de if e else
	}
	
	@Override
	public void exitStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		for(ParserRuleContext labelCtx : labelControl.keySet()) {
			for(ParserRuleContext context : labelControl.get(labelCtx)) {
				printStmt(context, labelCtx);
			}
		}
		try {
			compUnitGraph = new SymbolWeightedGraph(nodeList, false, "/");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void enterEndSub(@NotNull VisualBasic6Parser.EndSubContext ctx) {
		for(ParserRuleContext endCtx : endProcedureControl.keySet()) {
			for(ParserRuleContext context : endProcedureControl.get(endCtx)) {
				printStmt(context, endCtx);
			}
		}
		
		try {
			compUnitGraph = new SymbolWeightedGraph(nodeList, false, "/");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void enterEndFunction(@NotNull VisualBasic6Parser.EndFunctionContext ctx) {
		for(ParserRuleContext endCtx : labelControl.keySet()) {
			for(ParserRuleContext context : labelControl.get(endCtx)) {
				printStmt(context, endCtx);
			}
		}
		
		try {
			compUnitGraph = new SymbolWeightedGraph(nodeList, false, "/");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void enterEndProperty(@NotNull VisualBasic6Parser.EndPropertyContext ctx) {
		for(ParserRuleContext endCtx : labelControl.keySet()) {
			for(ParserRuleContext context : labelControl.get(endCtx)) {
				printStmt(context, endCtx);
			}
		}
		
		try {
			compUnitGraph = new SymbolWeightedGraph(nodeList, false, "/");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	
	
	@Override 
	public void enterStmt(@NotNull VisualBasic6Parser.StmtContext ctx) {
		if(!stmtToVisit(ctx)) return;
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}		
	}

	@Override 
	public void enterMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) { 
	    if(!conditionalControl.isEmpty()) {
	    	printLog(ctx, "ERROR", "CONDITIONAL NODE NOT CLEAR ON CREATE PROCEDURE STMT");
	    }
	    previousNode = null;
	    currentNode = ctx;
	    endProcedureNode = getEndProcedureNode(ctx);
	}
	
	private ParserRuleContext getEndProcedureNode(ParserRuleContext _ctx) {
		ParserRuleContext wProcNode = (ParserRuleContext) NodeExplorer_New.getFirstChildClass(_ctx, "EndSubContext");
		if(wProcNode == null) {
			wProcNode = (ParserRuleContext) NodeExplorer_New.getFirstChildClass(_ctx, "EndFunctionContext");
		}
		if(wProcNode == null) {
			wProcNode = (ParserRuleContext) NodeExplorer_New.getFirstChildClass(_ctx, "EndPropertyContext");
		}
		if(wProcNode == null) {
			BicamSystem.printLog("ERROR", "END PROCEDURE NODE NOT FOUND");
		}
		return wProcNode;
	}

	@Override 
	public void enterFormalParameter(@NotNull VisualBasic6Parser.FormalParameterContext ctx) { 
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	}
	
	@Override 
	public void enterDoLoopStmt(@NotNull VisualBasic6Parser.DoLoopStmtContext ctx) { 
		whileControl.put(ctx, new HashSet<ParserRuleContext>());
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	}
	
	@Override 
	public void exitDoLoopStmt(@NotNull VisualBasic6Parser.DoLoopStmtContext ctx) { 
		for ( ParserRuleContext context : whileControl.get(ctx)) {
			previuosConditional.add(context);
		}
	}
	
	@Override 
	public void enterEndLoop(@NotNull VisualBasic6Parser.EndLoopContext ctx) {
		ParserRuleContext loopContext = NodeExplorer_New.getAncestorClass(ctx,"DoLoopStmtContext");
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	    printStmt(currentNode,loopContext);
	    currentNode = loopContext;
	}	
	
	@Override 
	public void enterIfThenElseStmt(@NotNull VisualBasic6Parser.IfThenElseStmtContext ctx) {
		conditionalControl.put(ctx, new ConditionaStmtControl(ctx));
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	}
	
	@Override 
	public void enterSelectCaseStmt(@NotNull VisualBasic6Parser.SelectCaseStmtContext ctx) {
		selectCaseControl.put(ctx, new HashSet<ParserRuleContext>());
	}	
	
	@Override 
	public void enterEndSelect(@NotNull VisualBasic6Parser.EndSelectContext ctx) {
	    ParserRuleContext wSelCase =	NodeExplorer_New.getAncestorClass(ctx, "SelectCaseStmtContext");

	    currentNode = ctx;
	    Set<ParserRuleContext> caseList = selectCaseControl.get(wSelCase);
	    boolean hasElse = false;
	    for(ParserRuleContext c : caseList) {
		    printStmt(c,currentNode);
		    if(SC_CondContext.class.isInstance(ctx)) hasElse = true;
	    }
	    // Se não tem else ..
	    if(!hasElse) {
	    	BicamSystem.printLog("WARNING", "NOT FOUND 'ELSE' IN SELECT CASE IN LINE:" + wSelCase.start.getLine());
	    	printStmt(wSelCase,currentNode);
	    }
	}
	
	@Override 
	public void enterSC_Case(@NotNull VisualBasic6Parser.SC_CaseContext ctx) {
		ParserRuleContext wSelCase = NodeExplorer_New.getAncestorClass(ctx, "SelectCaseStmtContext");
		Set<ParserRuleContext> wcontrol = selectCaseControl.get(wSelCase);
		//=====
	    for(ParserRuleContext cmdCaseCtx1 : previuosConditional) {
	    	wcontrol.add(cmdCaseCtx1);
	   }
	   for(ParserRuleContext cmdCaseCtx2 : previuosConditionalSave) {
	    	wcontrol.add(cmdCaseCtx2);
	   }
		previuosConditional.clear();
		previuosConditionalSave.clear();		
//=====		
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
//			previousNode = null;
//		    currentNode = ctx;
		}
	    previousNode =	wSelCase;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
	}
	
	@Override 
	public void exitSC_Case(@NotNull VisualBasic6Parser.SC_CaseContext ctx) {
		ParserRuleContext wSelCase = NodeExplorer_New.getAncestorClass(ctx, "SelectCaseStmtContext");
		if(currentNode != null) {
			Set<ParserRuleContext> wSet = selectCaseControl.get(wSelCase);
			wSet.add(currentNode);
			if(currentNode == ctx) {
				BicamSystem.printLog("WARNING", "NO COMMAND FOUND IN SELECT CASE IN LINE: " + ctx.start.getLine());
			}
		}
	}
	
	@Override   
	public void enterSC_Cond(@NotNull VisualBasic6Parser.SC_CondContext ctx) {

		ParserRuleContext wSelCase = NodeExplorer_New.getAncestorClass(ctx, "SelectCaseStmtContext");
		if(ctx.getText().equalsIgnoreCase("else")) {
			selectCaseControl.get(wSelCase).add(ctx);
		}
	}	
	
	@Override 
	public void enterIfTrue(@NotNull VisualBasic6Parser.IfTrueContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		conditionalControl.get(ifCtx).setIfTrue(ctx);  
	}
	
	@Override public void exitIfTrue(@NotNull VisualBasic6Parser.IfTrueContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		if(currentNode !=  conditionalControl.get(ifCtx).getIfStmt()) { 
	    	conditionalControl.get(ifCtx).setIfTrueLastStmt(currentNode);
	    }
	    currentNode  = conditionalControl.get(ifCtx).getIfStmt();	
	}
	
	@Override 
	public void enterIfTrueInLine(@NotNull VisualBasic6Parser.IfTrueInLineContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		conditionalControl.get(ifCtx).setIfTrue(ctx);  //e se for vazio?
	}
	
	@Override public void exitIfTrueInLine(@NotNull VisualBasic6Parser.IfTrueInLineContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");

		if(currentNode !=  conditionalControl.get(ifCtx).getIfStmt()) { 
	    	conditionalControl.get(ifCtx).setIfTrueLastStmt(currentNode);
	    }
	    currentNode  = conditionalControl.get(ifCtx).getIfStmt();	
	}	
	
	@Override 
	public void enterIfFalse(@NotNull VisualBasic6Parser.IfFalseContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		conditionalControl.get(ifCtx).setIfFalse(ctx);  	
		
		if(!previuosConditional.isEmpty()) { 
			for(ParserRuleContext cx : previuosConditional) {
				previuosConditionalSave.add(cx);
			}
			previuosConditional.clear();
		}
	}
	
	@Override 
	public void exitIfFalse(@NotNull VisualBasic6Parser.IfFalseContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		if(currentNode !=  conditionalControl.get(ifCtx).getIfStmt()) { 
	    	conditionalControl.get(ifCtx).setIfFalseLastStmt(currentNode);
	    }
    currentNode  = conditionalControl.get(ifCtx).getIfStmt();
	}
	
	@Override 
	public void enterIfFalseInLine(@NotNull VisualBasic6Parser.IfFalseInLineContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		conditionalControl.get(ifCtx).setIfFalse(ctx);  //e se for vazio?	
	}
	
	@Override 
	public void exitIfFalseInLine(@NotNull VisualBasic6Parser.IfFalseInLineContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		if(currentNode !=  conditionalControl.get(ifCtx).getIfStmt()) { 
	    	conditionalControl.get(ifCtx).setIfFalseLastStmt(currentNode);
	    }
    currentNode  = conditionalControl.get(ifCtx).getIfStmt();
	}	
	
	@Override 
	public void exitIfThenElseStmt(@NotNull VisualBasic6Parser.IfThenElseStmtContext ctx) {
		int hasTrueAndFalse = 0;
		if (!conditionalControl.get(ctx).hasIfTrue()) {
			previuosConditional.add(ctx);
		}
		
		if (!conditionalControl.get(ctx).hasIfFalse()) {
			previuosConditional.add(ctx);
		}
		
		if (!conditionalControl.get(ctx).elseIf.isEmpty()) {
			for(ConditionaStmtControl c : conditionalControl.get(ctx).getElseIf()) {
				if(c.getIfTrueLastStmt() == null) {
					printLog(ctx, " INFO ", "ELSEIF BODY EMPTY.");
				}
				else {
					previuosConditional.add(c.getIfTrueLastStmt());
				}
			}
		}		
		
		if (conditionalControl.get(ctx).hasIfTrue()) {
			if(conditionalControl.get(ctx).getIfTrueLastStmt() != null) {
				previuosConditional.add(conditionalControl.get(ctx).getIfTrueLastStmt());
				hasTrueAndFalse++;
			}
			else {
				printLog(ctx, " INFO ", "IF BODY EMPTY.");
			}
		}
		
		if (conditionalControl.get(ctx).hasIfFalse()) {
			if(conditionalControl.get(ctx).getIfFalseLastStmt() != null) {
				previuosConditional.add(conditionalControl.get(ctx).getIfFalseLastStmt());
				hasTrueAndFalse++;
			}
			else {
				printLog(ctx, " INFO ", "ELSE BODY EMPTY.");
			}
		}
		if(hasTrueAndFalse == 2) currentNode = startRuleContext;
		conditionalControl.remove(ctx);
	}
	
	@Override 
	public void enterElseIf(@NotNull VisualBasic6Parser.ElseIfContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
		conditionalControl.get(ifCtx).addElseIf(ctx); 
		if(!previuosConditional.isEmpty()) { 
			for(ParserRuleContext cx : previuosConditional) {
				previuosConditionalSave.add(cx);
			}
			previuosConditional.clear();
		}
		currentNode  = conditionalControl.get(ifCtx).getIfStmt();	
	}
	
	@Override public void exitElseIf(@NotNull VisualBasic6Parser.ElseIfContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getAncestorClass(ctx, "IfThenElseStmtContext");
	    if(currentNode !=  conditionalControl.get(ifCtx).getIfStmt()) { 
	    	conditionalControl.get(ifCtx).getElseIf(ctx).setIfTrueLastStmt(currentNode);
	    }
	    currentNode  = conditionalControl.get(ifCtx).getIfStmt();	
	}
 
	@Override 
	public void enterGoToStmt(@NotNull VisualBasic6Parser.GoToStmtContext ctx) { 
		String labelName = ctx.Name.getText();
		if(!labelName.equalsIgnoreCase("0")) {
			Symbol_New labelSymbol = resolve(ctx, labelName);
			Set<ParserRuleContext> heads = labelControl.get(labelSymbol.getContext());
			if(heads == null) {
				heads = new HashSet<ParserRuleContext>();
				labelControl.put(labelSymbol.getContext(), heads);
			}
			heads.add(ctx);
			
			if(!previuosConditional.isEmpty()) { 
				printPreviousConditionalStmt(ctx);
				previousNode = null;
			    currentNode = ctx;
			}
			else {
		    previousNode = currentNode;
		    currentNode = ctx;
		    printStmt(previousNode,currentNode);
			}
			currentNode = null;
		}
		else {
			if(!previuosConditional.isEmpty()) { 
				printPreviousConditionalStmt(ctx);
				previousNode = null;
			    currentNode = ctx;
			}
			else {
		    previousNode = currentNode;
		    currentNode = ctx;
		    printStmt(previousNode,currentNode);
			}
		}
	}

	@Override 
	public void enterExitStmt(@NotNull VisualBasic6Parser.ExitStmtContext ctx) { 
		Set<ParserRuleContext> heads = endProcedureControl.get(endProcedureNode);
		if(heads == null) { // ainda não tem exit stmt apontado para final da procedure
			heads = new HashSet<ParserRuleContext>();
			endProcedureControl.put(endProcedureNode, heads);
		}
		heads.add(ctx);
		
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
		currentNode = null;
	}	
	
	private void exitEndProcedureNode(ParserRuleContext _ctx) {
		if(!_ctx.equals(endProcedureNode)) {
			BicamSystem.printLog("ERROR", "END PROCEDURE DO NOT MATCH EXIT CONTEXT");
		}

		if(!endProcedureControl.isEmpty()) {
			for(ParserRuleContext exitSmtCtx : endProcedureControl.keySet()) {
				for(ParserRuleContext context : endProcedureControl.get(exitSmtCtx)) {
					printStmt(context, exitSmtCtx);
				}
			}
			try {
				compUnitGraph = new SymbolWeightedGraph(nodeList, false, "/");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		endProcedureControl.clear();
	}
	
	@Override
	public void exitEndSub(@NotNull VisualBasic6Parser.EndSubContext ctx) {
		exitEndProcedureNode(ctx);
/*		for(ParserRuleContext exitSmtCtx : endProcedureControl.keySet()) {
			for(ParserRuleContext context : labelControl.get(exitSmtCtx)) {
				printStmt(context, exitSmtCtx);
			}
		}
		try {
			compUnitGraph = new SymbolWeightedGraph(nodeList, false, "/");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	}
	
	@Override
	public void exitEndFunction(@NotNull VisualBasic6Parser.EndFunctionContext ctx) {
		exitEndProcedureNode(ctx);

		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	}
	
	@Override
	public void exitEndProperty(@NotNull VisualBasic6Parser.EndPropertyContext ctx) {
		exitEndProcedureNode(ctx);

		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	}

/*
	@Override 
	public void enterLabelLine(@NotNull VisualBasic6Parser.LabelLineContext ctx) { 
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}
	}
	*/

	@Override 
	public void enterLabel(@NotNull VisualBasic6Parser.LabelContext ctx) {
		if(!previuosConditional.isEmpty()) { 
			printPreviousConditionalStmt(ctx);
			previousNode = null;
		    currentNode = ctx;
		}
		else {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
		}		
	}
	
	private void printStmt(ParserRuleContext _tailNodeCtx, ParserRuleContext _headNodeCtx) {
			if(_tailNodeCtx == null || _tailNodeCtx == startRuleContext) {
//				currentNode = null;
				currentNode = _headNodeCtx;
				
//				printStmt(_headNodeCtx);
			}
			else {
				System.err.format("*** Node: %s/%d - Stmt: %s  ->  Node: %s/%d - Stmt: %s%n"
					, _tailNodeCtx.start.getStartIndex(), _tailNodeCtx.start.getLine(), _tailNodeCtx.start.getText()
					, _headNodeCtx.start.getStartIndex(), _headNodeCtx.start.getLine(), _headNodeCtx.start.getText());

				String tail = Integer.toString(_tailNodeCtx.start.getStartIndex());
				String tailLine = Integer.toString(_tailNodeCtx.start.getLine());
				String head = Integer.toString(_headNodeCtx.start.getStartIndex());
				String headLine = Integer.toString(_headNodeCtx.start.getLine());
				
				String inputTail = tail + ":" + tailLine;
				BicamNode inputTailNode = nodeList.get(inputTail);
				if(inputTailNode == null) {
					inputTailNode = new BicamNode(inputTail);
					nodeList.add(inputTailNode);
					lineToNodeId.put(Integer.parseInt(tailLine), inputTailNode.getId());
				}
				
				String inputHead = head + ":" + headLine;
				BicamNode inputHeadNode = nodeList.get(inputHead);

				if(inputHeadNode == null) {
					inputHeadNode = new BicamNode(inputHead);
					nodeList.add(inputHeadNode);
					lineToNodeId.put(Integer.parseInt(headLine), inputHeadNode.getId());
				}				
				
				BicamAdjacency adj = new BicamAdjacency(inputHeadNode);
				inputTailNode.addAdjacency(adj);
			}
		}
	
	private void printStmt(ParserRuleContext _nodeCtx) {
		System.err.format("%n*** Node: %s/%d - Stmt: %s%n"
				, _nodeCtx.start.getStartIndex(), _nodeCtx.start.getLine(), _nodeCtx.start.getText());
		String node = Integer.toString(_nodeCtx.start.getStartIndex());
		String nodeLine = Integer.toString(_nodeCtx.start.getLine());
		
		String inputNode = node + ":" + nodeLine;
		
		BicamNode inputNodeNode = nodeList.get(inputNode);
		if(inputNodeNode == null) {
			inputNodeNode = new BicamNode(inputNode);
			nodeList.add(inputNodeNode);
		}
		
/*		if(inputGraph.isEmpty()) {
			compUnit.getProperties().addProperty("ENTRY_GRAPH", inputNode);
		}
		inputGraph.add(new InputGraph(inputNode));*/
	}
	
	private void printPreviousConditionalStmt(ParserRuleContext _stmtCtx) {
	    for(ParserRuleContext forIfCtx : previuosConditional) {
	    		 printStmt(forIfCtx,_stmtCtx);
	    }
	    for(ParserRuleContext forIfCtx : previuosConditionalSave) {
   		 printStmt(forIfCtx,_stmtCtx);
	    }
		previuosConditional.clear();
		previuosConditionalSave.clear();
	}
	
	private void printLog(ParserRuleContext _ctx, String _severity, String _msg) {
		System.err.format("*** %s - %s - at line %d in compilation unit %s%n",
				   _severity, _msg,  _ctx.start.getLine(),st.getCompilationUnitName(_ctx));
		if(_severity.equalsIgnoreCase("ERROR")) System.exit(1);
	}
	
	private boolean stmtToVisit(ParserRuleContext _ctx) {
		ParserRuleContext cmdCtx = (ParserRuleContext) NodeExplorer_New.getChildClassEndsWith(_ctx, "StmtContext");
		if(cmdCtx.getClass().getSimpleName().equals("CommandStmtContext")) {
			cmdCtx = (ParserRuleContext) NodeExplorer_New.getChildClassEndsWith(cmdCtx, "StmtContext");
		}

		if(cmdCtx == null) {
			if(!NodeExplorer_New.hasAncestorClass(_ctx, "RealParametersContext") &&
			   !NodeExplorer_New.hasAncestorClass(_ctx, "FormalParametersContext"))
			return false;
			else return true;
		}
		
		for(String s : noVisitStmt) {
			if(cmdCtx.getClass().getSimpleName().equals(s))
				return false;
		}
		if(isCurrentNodeInconditionalBranch(currentNode)) {
			currentNode = null;
		}
		return true;
	}
	
	private boolean isCurrentNodeInconditionalBranch(ParserRuleContext curContext) {
		if(curContext == null) return true;
		for(String contextName : inconditionalBrach) {
			if(curContext.getClass().getSimpleName().equals(contextName))
				return true;
		}
		return false;
	}
	
	private Symbol_New resolve(ParserRuleContext ctx, String _name) {
		PropertyList properties = defaultProperties(ctx);
		
		properties.addProperty(NAME, _name);
		
		Symbol_New sym = st.getScope(ctx).resolve(properties);
		if(sym == null) {
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: SYMBOL '%s' NOT RESOLVED in COMPILATION UNIT '%s' in line %d%n"
						                ,ctx.getText(), st.getCompilarionUnitSymbol(ctx).getName()
						                , ctx.start.getLine());
				e.printStackTrace();
			}
			return null;
		}
		return sym;
	}
	
	private PropertyList defaultProperties(ParserRuleContext _ctx) {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(DEF_MODE, "EXPLICIT"); 
		properties.addProperty(ENCLOSING_SCOPE, st.getScope(_ctx));
		return properties;
	}
	
	public NodeList getNodeList() {
		return nodeList;
	}
	
	
	public Map<Integer,String> getLineToNodeId() {
		return lineToNodeId;
	}
	
	public SymbolWeightedGraph getGraph() {
		return compUnitGraph;
	}
	
}