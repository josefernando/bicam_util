package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.ApplicationComponent;
import br.com.bicam.util.datamodel.Version;
import br.com.bicam.util.graph.SymbolWeightedGraph;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class CompilationUnitSqlTransactSybaseGraph extends SqlTransactSybaseBaseListener {

	PropertyList properties;
	SymbolTable_New st;
	KeywordLanguage  keywordLanguage;
	SymbolFactory symbolFactory;
	ParserRuleContext previousNode;
	ParserRuleContext currentNode;
	
//	CompilationUnit compUnit;
	
	Version version;
	ApplicationComponent appComponent;

	IdentityHashMap<ParserRuleContext, ConditionaStmtControl> conditionalControl;
	IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>> labelControl;
	IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>> whileControl;
	
	List<InputGraph> inputGraph;
	SymbolWeightedGraph compUnitGraph;

	Set<ParserRuleContext> previuosConditional;
	
	final String[] noVisitStmt = new String[] {"CreateProcedureStmtContext"
			, "DeclareStmtContext"
			, "BeginStmtContext"
			, "IfStmtContext"
			, "LabelContext"
			, "WhileStmtContext"
			, "GoToStmtContext"
			, "BreakStmtContext"
			, "ContinueStmtContext"
			, "GoStmtContext"

			};
	
	final String[] inconditionalBrach = new String[] {"GoToStmtContext"
			, "BreakStmtContext"
			};
	
	public CompilationUnitSqlTransactSybaseGraph(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty(SYMBOLTABLE);
		this.symbolFactory = (SymbolFactory) properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);
		this.version = (Version) properties.getProperty("VERSION");
//		this.compUnit = (CompilationUnit) properties.getProperty("COMPILATION_UNIT");
		conditionalControl = new IdentityHashMap<ParserRuleContext, ConditionaStmtControl>();
		previuosConditional = new HashSet<ParserRuleContext>();
		labelControl = new IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>>();
		whileControl = new IdentityHashMap<ParserRuleContext, Set<ParserRuleContext>>();

		inputGraph = new ArrayList<InputGraph>();
	}
	
	@Override
	public void exitStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		
		for(ParserRuleContext labelCtx : labelControl.keySet()) {
			for(ParserRuleContext context : labelControl.get(labelCtx)) {
				printStmt(context, labelCtx);
			}
		}
		
		try {
			compUnitGraph = new SymbolWeightedGraph(getInputGraphList(), false, "/");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	@Override 
	public void enterCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
		
		String node = Integer.toString(ctx.start.getStartIndex());
		String nodeLine = Integer.toString(ctx.start.getLine());
		
		String inputNode = node + ":" + nodeLine;
		
		version.getProperties().addProperty("ENTRY_GRAPH", inputNode);
	    if(!conditionalControl.isEmpty()) {
	    	printLog(ctx, "ERROR", "CONDITIONAL NODE NOT CLEAR ON CREATE PROCEDURE STMT");
	    }
	    previousNode = null;
	    currentNode = ctx;
	}
	
	
	@Override 
	public void enterStmt(@NotNull SqlTransactSybaseParser.StmtContext ctx) {
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
	public void enterFormalParameter(@NotNull SqlTransactSybaseParser.FormalParameterContext ctx) {
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
	public void enterRealParameter(@NotNull SqlTransactSybaseParser.RealParameterContext ctx) {
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
	public void enterWhileStmt(@NotNull SqlTransactSybaseParser.WhileStmtContext ctx) {
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
	public void exitWhileStmt(@NotNull SqlTransactSybaseParser.WhileStmtContext ctx) {
		for ( ParserRuleContext context : whileControl.get(ctx)) {
			previuosConditional.add(context);
		}
		previuosConditional.add(ctx);  
	}
	
	@Override 
	public void enterContinueStmt(@NotNull SqlTransactSybaseParser.ContinueStmtContext ctx) {
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
		ParserRuleContext currentLoopContext = NodeExplorer_New.getAncestorClass(ctx, "WhileStmtContext");
        if(currentLoopContext == null) printLog(ctx, "ERROR", " Loop statement isn't  \"WhileStmt\" : " 
		                                        + ctx.start.getText()); 
	    printStmt(currentNode,currentLoopContext);

	    currentNode = currentLoopContext;
	}
	
	@Override 
	public void enterBreakStmt(@NotNull SqlTransactSybaseParser.BreakStmtContext ctx) {
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
		
		ParserRuleContext currentLoopContext = NodeExplorer_New.getAncestorClass(ctx, "WhileStmtContext");
        if(currentLoopContext == null) printLog(ctx, "ERROR", " Loop statement isn't  \"WhileStmt\" : " 
		                                        + ctx.start.getText());  
        whileControl.get(currentLoopContext).add(ctx);
	}
	
	@Override 
	public void enterIfStmt(@NotNull SqlTransactSybaseParser.IfStmtContext ctx) {
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
	public void enterGoStmt(@NotNull SqlTransactSybaseParser.GoStmtContext ctx) {
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
	
	@Override 
	public void enterIfTrue(@NotNull SqlTransactSybaseParser.IfTrueContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getNextAncestorClass(ctx, "IfStmtContext");
		conditionalControl.get(ifCtx).setIfTrue(ctx);  //e se for vazio?
	}
	
	@Override 
	public void exitIfTrue(@NotNull SqlTransactSybaseParser.IfTrueContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getNextAncestorClass(ctx, "IfStmtContext");
	    if(currentNode !=  conditionalControl.get(ifCtx).getIfStmt()) { 
	    	conditionalControl.get(ifCtx).setIfTrueLastStmt(currentNode);
	    }
	    currentNode  = conditionalControl.get(ifCtx).getIfStmt();	
	}	
	
	@Override 
	public void enterIfFalse(@NotNull SqlTransactSybaseParser.IfFalseContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getNextAncestorClass(ctx, "IfStmtContext");
		conditionalControl.get(ifCtx).setIfFalse(ctx);  //e se for vazio?	
	}
	
	@Override 
	public void exitIfFalse(@NotNull SqlTransactSybaseParser.IfFalseContext ctx) {
		ParserRuleContext ifCtx = NodeExplorer_New.getNextAncestorClass(ctx, "IfStmtContext");
	    if(currentNode !=  conditionalControl.get(ifCtx).getIfStmt()) { 
	    	conditionalControl.get(ifCtx).setIfFalseLastStmt(currentNode);
	    }
//	    currentNode  = null;
	    currentNode  = conditionalControl.get(ifCtx).getIfStmt();
	}
	
	@Override 
	public void exitIfStmt(@NotNull SqlTransactSybaseParser.IfStmtContext ctx) {
//		currentNode = null;
		if (!conditionalControl.get(ctx).hasIfTrue()) {
			previuosConditional.add(ctx);
		}
		
		if (!conditionalControl.get(ctx).hasIfFalse()) {
			previuosConditional.add(ctx);
		}
		
		if (conditionalControl.get(ctx).hasIfTrue()) {
			if(conditionalControl.get(ctx).getIfTrueLastStmt() != null) {
				previuosConditional.add(conditionalControl.get(ctx).getIfTrueLastStmt());
			}
			else {
				printLog(ctx, " INFO ", "IF BODY EMPTY.");
			}
		}
		
		if (conditionalControl.get(ctx).hasIfFalse()) {
			if(conditionalControl.get(ctx).getIfFalseLastStmt() != null) {
				previuosConditional.add(conditionalControl.get(ctx).getIfFalseLastStmt());
			}
			else {
				printLog(ctx, " INFO ", "ELSE BODY EMPTY.");
			}
		}
	}	
	
	@Override 
	public void enterVariableStmt(@NotNull SqlTransactSybaseParser.VariableStmtContext ctx) {
	    previousNode = currentNode;
	    currentNode = ctx;
	    printStmt(previousNode,currentNode);
	}
	 
	@Override 
	public void enterGoToStmt(@NotNull SqlTransactSybaseParser.GoToStmtContext ctx) { 
		String labelName = ctx.Name.getText();
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
	}
	
	@Override 
	public void enterLabel(@NotNull SqlTransactSybaseParser.LabelContext ctx) {
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
			if(_tailNodeCtx == null) {
				printStmt(_headNodeCtx);
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
				String inputHead = head + ":" + headLine;
				
				inputGraph.add(new InputGraph(inputTail, inputHead));
			}
		}
	
	private void printStmt(ParserRuleContext _nodeCtx) {
		System.err.format("%n*** Node: %s/%d - Stmt: %s%n"
				, _nodeCtx.start.getStartIndex(), _nodeCtx.start.getLine(), _nodeCtx.start.getText());
		String node = Integer.toString(_nodeCtx.start.getStartIndex());
		String nodeLine = Integer.toString(_nodeCtx.start.getLine());
		
		String inputNode = node + ":" + nodeLine;
		
		inputGraph.add(new InputGraph(inputNode));
	}
	
	private void printPreviousConditionalStmt(ParserRuleContext _stmtCtx) {
	    for(ParserRuleContext forIfCtx : previuosConditional) {
	    		 printStmt(forIfCtx,_stmtCtx);
	    }
		previuosConditional.clear();
	}
	
	private void printLog(ParserRuleContext _ctx, String _severity, String _msg) {
		System.err.format("*** %s - %s - at line %d in compilation unit %s%n",
				   _severity, _msg,  _ctx.start.getLine(),st.getCompilationUnitName(_ctx));
		if(_severity.equalsIgnoreCase("ERROR")) System.exit(1);
	}
	
	private boolean stmtToVisit(ParserRuleContext _ctx) {
		ParserRuleContext cmdCtx = (ParserRuleContext) NodeExplorer_New.getChildClassEndsWith(_ctx, "StmtContext");

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
	
	public List<InputGraph> getInputGraphList(){
		return inputGraph;
	}
	
	public SymbolWeightedGraph getGraph() {
		return compUnitGraph;
	}
	
}
