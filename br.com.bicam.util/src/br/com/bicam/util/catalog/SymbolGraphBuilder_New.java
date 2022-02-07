package br.com.bicam.util.catalog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.EndIfContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.graph.SymbolGraph;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class SymbolGraphBuilder_New extends VisualBasic6BaseListener {
	SymbolTable_New st;
	HashSet<Integer> vertices;
	HashMap<String,ParserRuleContext> contextNameToContext;
	HashMap<String,String> contextNameToSubContextName;


	String nodeSeparator;
	
	boolean isEndProcedure;
	
	Symbol_New compilationUnitSymbol;
	String compilationUnitName;
	
	PrintWriter fileOut;
	PrintWriter dictOut;

	StringBuffer out;
	StringBuffer dic;

	
	ParserRuleContext currentContext;
	ParserRuleContext currentMethodContext;

	// no en ter de stmt verifica se deve stmt deve ser apontado por últimos stataments de if stmt
	ParserRuleContext previousIfStmt = null;
	ParserRuleContext previousLoopStmt = null;
	
	ArrayDeque<ParserRuleContext> ifStmtStack;
	ArrayDeque<ParserRuleContext> loopStmtStack;
	ArrayDeque<ParserRuleContext> jumpEndProcedure;


	HashMap<ParserRuleContext, ArrayList<ParserRuleContext>> lastStmtInIfStmt;
//	HashMap<ParserRuleContext, ArrayList<ParserRuleContext>> fromEdges;
	
	HashMap<ParserRuleContext, ArrayList<ParserRuleContext>> jumpTargetSmt;
	HashMap<ParserRuleContext, ArrayList<ParserRuleContext>> nextStmtAfterLoop;

	HashSet<ParserRuleContext> ifHasTrue;
	HashSet<ParserRuleContext> ifHasFalse;
	
	HashMap<Integer, Integer> contextToIndex;
	Integer[] indexToContext;
	SymbolGraph symbolGraph;
	File file;
	File dictionary;
	Scanner scanner;
	
	String[] conditionalStmtList = new String[] {"IfThenElseStmtContext"};
//            , "DoLoopStmtContext"
//            , "WhileWendStmtContext"
//            , "ForEachStmtContext"
//            , "IfThenInLineContext"
//            , "ForNextStmtContext"};
	
	String[] loopStmtList = new String[] {
      "DoLoopStmtContext"};
//    , "WhileWendStmtContext"
//    , "ForEachStmtContext"
//    , "ForNextStmtContext"};	
            
	public  SymbolGraphBuilder_New(SymbolTable_New _st, String ..._nodeSeparator) throws FileNotFoundException {
		this.st = _st;
		dictionary = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\dictionary.txt");
		file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\fileNode.txt");
		fileOut = new PrintWriter(file);
		dictOut = new PrintWriter(dictionary);

		contextNameToContext = new 	HashMap<>();
		contextNameToSubContextName = new 	HashMap<>();

		out = new StringBuffer();
		dic = new StringBuffer();
		
		if(_nodeSeparator.length > 0) this.nodeSeparator = _nodeSeparator[0];
		else this.nodeSeparator = " ";

		ifStmtStack = new ArrayDeque<>();
		loopStmtStack = new ArrayDeque<>();
		jumpEndProcedure = new ArrayDeque<>();

		lastStmtInIfStmt = new HashMap<>();
//		fromEdges = new HashMap<>();
		
		jumpTargetSmt = new HashMap<>();
		nextStmtAfterLoop = new HashMap<>();
		ifHasTrue = new HashSet<>();
		ifHasFalse = new HashSet<>();
		
		vertices = new HashSet<>();
		
		contextToIndex = new HashMap<>(); 
	}
	
	private void printStmt (ParserRuleContext ctx){
		System.err.format("Line: %d StartIndex: %d Text: %s - Comp Unit: %s%n"
				,ctx.start.getLine()
				,ctx.start.getStartIndex()
				,ctx.start.getText()
				,st.getCompilationUnitName(ctx));
	}
	
	private void addEntryPoint(ParserRuleContext _vertice){
		if(out.length() > 0) out.append("\n"); 
		out.append(Integer.toString(_vertice.start.getStartIndex())); 
		System.err.println("NODE -> Line: " + _vertice.start.getLine() + " Position: " + Integer.toString(_vertice.start.getStartIndex()) + " - " + _vertice.start.getText());	
	}	
	
	private void addEdge(ParserRuleContext _incident){
		addEdge(currentContext, _incident);
	}
	
	private void addVerticeInGraph(ParserRuleContext value){
		vertices.add(value.start.getStartIndex());
		mapNameToContext(Integer.toString(value.start.getStartIndex()),value);
	}
	
	private void mapNameToContext(String key, ParserRuleContext value){
		contextNameToContext.put(key, value);
	}
	
	private void addEdge(ParserRuleContext _from, ParserRuleContext _incident){
		
		if(!vertices.contains(_incident.start.getStartIndex())) addVerticeInGraph(_incident);
		if(_from != null && !vertices.contains(_from.start.getStartIndex())) addVerticeInGraph(_from);

		boolean hasNoSeqAdges = addNoSequenceEdges(_incident);
		
		if(_from == null && hasNoSeqAdges == false) {
			addEntryPoint(_incident);
			return;
		}
		
		if(_from != null) addEdgeInputGraph(_from, _incident);
	}
	
	private void addEdgeInputGraph(ParserRuleContext _from, ParserRuleContext _incident){
		if(out.length() > 0 ) out.append("\n");
		out.append(Integer.toString(_from.start.getStartIndex()) 
				+ nodeSeparator 
				+ Integer.toString(_incident.start.getStartIndex()));
		
		System.err.println(Integer.toString(_from.start.getLine()) + "-" + _from.start.getStartIndex() + "-" + _from.start.getText()
				+ " --> "  
				+ Integer.toString(_incident.start.getLine()) + "-" + _incident.start.getStartIndex() + "-" + _incident.start.getText());
	}
	
	private boolean addNoSequenceEdges(ParserRuleContext _incident){
		
		boolean ret = false;
		
		// incident é label
		if(jumpTargetSmt.get(_incident) != null){
			for(ParserRuleContext context : jumpTargetSmt.get(_incident)){
				addEdgeInputGraph(context, _incident);
			}
			ret = true;
			jumpTargetSmt.remove(_incident);
		}
		
		if(previousIfStmt != null){
			for(ParserRuleContext context 
					: lastStmtInIfStmt.get(previousIfStmt)){
				addEdgeInputGraph(context, _incident);
			}
			ret = true;
			previousIfStmt = null;
		}
		
		if(!jumpEndProcedure.isEmpty() && isEndProcedure){
			for(ParserRuleContext context 
					: jumpEndProcedure){
				addEdgeInputGraph(context, _incident);
			}
			ret = true;
			jumpEndProcedure.clear();
			isEndProcedure = false;
		}		
		
		//è o 1o. comando após um loop e dentro do loop foi utlizado o exit loop
		if(previousLoopStmt != null && nextStmtAfterLoop.get(previousLoopStmt) != null){
			for(ParserRuleContext context 
					: nextStmtAfterLoop.get(previousLoopStmt)){
				addEdgeInputGraph(context, _incident);
			}
			ret = true;
			nextStmtAfterLoop.remove(previousLoopStmt);
			previousLoopStmt = null;
		}		
		return ret;
	}
	
	/*
	 * Fim dos eventos
	 */
	@Override public void exitStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		fileOut.print(out.toString());
		fileOut.close();

		dictOut.close();

			try {
				symbolGraph = getSymbolGraph();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			 for (Map.Entry<String, ParserRuleContext> entry : contextNameToContext.entrySet()) {
				 contextNameToSubContextName.put(entry.getKey(), Integer.toString(entry.getValue().start.getLine()) + "/"+ entry.getValue().start.getText());
			 }	
			 
			 symbolGraph.setContextToSubContext(contextNameToSubContextName);

//			 compilationUnitName = compilationUnitSymbol.getName();
		
	}
	
	@Override 
	public void enterStmt(@NotNull VisualBasic6Parser.StmtContext ctx) {
/*		printStmt(ctx);
		
        addEdge(ctx);
		currentContext = ctx;*/
		enterCmd(ctx);
	}
	 
	private void enterCmd(ParserRuleContext ctx) {
		printStmt(ctx);
        addEdge(ctx);
		currentContext = ctx;
	}
	
	@Override 
	public void enterDoLoopStmt(@NotNull VisualBasic6Parser.DoLoopStmtContext ctx) {
		loopStmtStack.push(ctx);
	}
	
	
	@Override 
	public void exitEndLoop(@NotNull VisualBasic6Parser.EndLoopContext ctx) {
		enterCmd(ctx);
		addEdge(ctx,loopStmtStack.peek());
		previousLoopStmt = loopStmtStack.peek();
		currentContext = loopStmtStack.pop();
	}
	
	@Override 
	public void exitExitDo(@NotNull VisualBasic6Parser.ExitDoContext ctx) {
		if(nextStmtAfterLoop.get(loopStmtStack.peek()) == null){
			nextStmtAfterLoop.put(loopStmtStack.peek(), new ArrayList<ParserRuleContext>());
		}
		nextStmtAfterLoop.get(loopStmtStack.peek()).add(ctx);
		currentContext = null;
	}	
	
	@Override 
	public void enterForStmt(@NotNull VisualBasic6Parser.ForStmtContext ctx) {
		loopStmtStack.push(ctx);
	}
	
	@Override 
	public void exitEndFor(@NotNull VisualBasic6Parser.EndForContext ctx) {
		enterCmd(ctx);
		addEdge(ctx,loopStmtStack.peek());
		previousLoopStmt = loopStmtStack.peek();
		currentContext = loopStmtStack.pop();
	} 
	
	@Override 
	public void exitExitFor(@NotNull VisualBasic6Parser.ExitForContext ctx) {
		if(nextStmtAfterLoop.get(loopStmtStack.peek()) == null){
			nextStmtAfterLoop.put(loopStmtStack.peek(), new ArrayList<ParserRuleContext>());
		}
		nextStmtAfterLoop.get(loopStmtStack.peek()).add(ctx);
		currentContext = null;
	}	
	
	@Override 
	public void enterIfThenElseStmt(@NotNull VisualBasic6Parser.IfThenElseStmtContext ctx) {
		enterIfStmt(ctx);
	}	
	
	@Override 
	public void exitIfThenElseStmt(@NotNull VisualBasic6Parser.IfThenElseStmtContext ctx) {
		exitIfStmt(ctx);
	}
	
	private void enterIfStmt(ParserRuleContext ctx){
		ifHasFalse.add(ctx);  // a princípio não tem ifTrue
		ifHasTrue.add(ctx);   // a princípio não tem ifFalse
		ifStmtStack.push(ctx);
		lastStmtInIfStmt.put(ctx, new ArrayList<ParserRuleContext>());
	}
	
	private void exitIfStmt(ParserRuleContext ctx){
		if(ifHasTrue.contains(ifStmtStack.peek())
				|| ifHasFalse.contains(ifStmtStack.peek())){
			lastStmtInIfStmt.get(ifStmtStack.peek()).add(ifStmtStack.peek());
		}

		ParserRuleContext endIfContext = (EndIfContext) NodeExplorer.getBreathFirstChildClass(ctx, "EndIfContext");
		if(endIfContext != null){
			if (previousIfStmt !=  null){
				for(ParserRuleContext context : lastStmtInIfStmt.get(previousIfStmt)){
					lastStmtInIfStmt.get(ifStmtStack.peek()).add(context);
				}
			}
			// if reduntande para evitar o erro
			// java.util.ConcurrentModificationException
			if (previousIfStmt !=  null){
				lastStmtInIfStmt.get(previousIfStmt).clear();		
			}
			
			for(ParserRuleContext context 
					: lastStmtInIfStmt.get(ifStmtStack.peek())){
				addEdge(context, endIfContext);
			}
			
			ifStmtStack.pop();
			previousIfStmt = null;
			currentContext = endIfContext;
			return;
		}

		previousIfStmt = ifStmtStack.pop();		
	}
	
//=============================================================================
	@Override 
	public void exitIfTrueInLine(@NotNull VisualBasic6Parser.IfTrueInLineContext ctx) {
		ifHasTrue.remove(ifStmtStack.peek());
		if(currentContext != null) // último comando poderia ser desvio to tipo: exit function ou  goto
			lastStmtInIfStmt.get(ifStmtStack.peek()).add(currentContext);
	}
	
	@Override  // else
	public void enterIfFalseInLine(@NotNull VisualBasic6Parser.IfFalseInLineContext ctx) {
		currentContext = ifStmtStack.peek();
		addEdge(ctx);
		currentContext = ctx;
	}
	
	@Override 
	public void exitIfFalseInLine(@NotNull VisualBasic6Parser.IfFalseInLineContext ctx) {
		ifHasFalse.remove(ifStmtStack.peek());
		if(currentContext != null) // último comando não pode ser desvio to tipo: exit function ou  goto
			lastStmtInIfStmt.get(ifStmtStack.peek()).add(currentContext);
	}	
	
	@Override 
	public void exitIfTrue(@NotNull VisualBasic6Parser.IfTrueContext ctx) {
		ifHasTrue.remove(ifStmtStack.peek());
		if(currentContext != null) // último comando poderia ser desvio to tipo: exit function ou  goto
			lastStmtInIfStmt.get(ifStmtStack.peek()).add(currentContext);	
	}
	
	@Override //Else
	public void enterElseIf(@NotNull VisualBasic6Parser.ElseIfContext ctx) {
		currentContext = ifStmtStack.peek();
		addEdge(ctx);
		currentContext = ctx;	
	}
	
	@Override 
	public void exitElseIf(@NotNull VisualBasic6Parser.ElseIfContext ctx) {
		ifHasFalse.remove(ifStmtStack.peek());
		if(currentContext != null) // último comando não pode ser desvio to tipo: exit function ou  goto
			lastStmtInIfStmt.get(ifStmtStack.peek()).add(currentContext);
	}
	
	@Override 
	public void enterIfFalse(@NotNull VisualBasic6Parser.IfFalseContext ctx) {
		currentContext = ifStmtStack.peek();
		addEdge(ctx);
		currentContext = ctx;
	}
	
	@Override 
	public void exitIfFalse(@NotNull VisualBasic6Parser.IfFalseContext ctx) {
		ifHasFalse.remove(ifStmtStack.peek());
		if(currentContext != null) // último comando não pode ser desvio to tipo: exit function ou  goto
			lastStmtInIfStmt.get(ifStmtStack.peek()).add(currentContext);
	}
	
//=============================================================================	
	
	
	@Override 
	public void enterMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) {
		ifStmtStack.clear();
		lastStmtInIfStmt.clear();
		jumpEndProcedure.clear();
		loopStmtStack.clear();
		lastStmtInIfStmt.put(ctx, new ArrayList<ParserRuleContext>());
		currentMethodContext = ctx;
		ifHasTrue.clear();
		ifHasFalse.clear();
		currentContext = null;
		previousIfStmt = null;
		isEndProcedure= false;
		
		addEdge(ctx);
		currentContext = ctx;
	}
	
	@Override
	public void exitEndSub(@NotNull VisualBasic6Parser.EndSubContext ctx) {
		isEndProcedure = true;
		printStmt(ctx);
		addEdge(ctx);
	}
	
	@Override
	public void exitEndFunction(@NotNull VisualBasic6Parser.EndFunctionContext ctx) {
		isEndProcedure = true;
		printStmt(ctx);
		addEdge(ctx);
	}
	
	@Override
	public void exitEndProperty(@NotNull VisualBasic6Parser.EndPropertyContext ctx) {
		isEndProcedure = true;
		printStmt(ctx);
		addEdge(ctx);
	}
	
	@Override
	public void exitExitSub(@NotNull VisualBasic6Parser.ExitSubContext ctx) {
		jumpEndProcedure.add(ctx);
	}
	
	@Override
	public void exitExitFunction(@NotNull VisualBasic6Parser.ExitFunctionContext ctx) {
		jumpEndProcedure.add(ctx);
	}
	
	@Override
	public void exitExitProperty(@NotNull VisualBasic6Parser.ExitPropertyContext ctx) {
		jumpEndProcedure.add(ctx);
	}	
	
	@Override 
	public void exitOnErrorStmt(@NotNull VisualBasic6Parser.OnErrorStmtContext ctx) {
		// On Error Goto 0
		if(ctx.goToStmt() != null){
			if(ctx.goToStmt().expr().getText().equals("0")) return;
		}
		// On Error Resume Next
		if(ctx.resumeStmt() != null) return;
		
		jumpStmt(ctx);
	}
	
	@Override 
	public void exitGoToStmt(@NotNull VisualBasic6Parser.GoToStmtContext ctx) {
		if(NodeExplorer.getNextAncestorClass(ctx, "OnErrorStmtContext") != null ) return; // trata on error e
        if(ctx.expr().getText().equals("0")) return; // Goto 0
		jumpStmt(ctx);
		currentContext = null; // deve apontar somente para o label
	}
	
	@Override 
	public void exitResumeStmt(@NotNull VisualBasic6Parser.ResumeStmtContext ctx) {
		if(NodeExplorer.getNextAncestorClass(ctx, "OnErrorStmtContext") != null ) return; // trata on error e
        if(ctx.expr() == null ) return;
		jumpStmt(ctx);
		currentContext = null; // deve apontar somente para o label
	}	
	
	private void jumpStmt(ParserRuleContext ctx){
		IdentifierContext identifierCtx = (IdentifierContext) NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext");
		Symbol_New symLabel = st.getSymbol(identifierCtx);
		
		ParserRuleContext labelCtx = symLabel.getContext();
		
		if(jumpTargetSmt.get(labelCtx) == null){
			jumpTargetSmt.put(labelCtx, new ArrayList<ParserRuleContext>());
		}
		
		jumpTargetSmt.get(labelCtx).add(ctx);
		
//		System.err.format ("=====> Nome: %s - Line: %d%n", symLabel.getName(), symLabel.getContext().start.getLine());
	}
	
	@Override
	public void enterLabel(@NotNull VisualBasic6Parser.LabelContext ctx){
		printStmt(ctx);

		addEdge(ctx);
		currentContext = ctx;
	}
	
	/*
	@Override
	public void enterLabelLine(@NotNull VisualBasic6Parser.LabelLineContext ctx){
		printStmt(ctx);

		addEdge(ctx);
		currentContext = ctx;
	}
	*/
	
	public SymbolGraph getSymbolGraph(boolean..._verbose) throws FileNotFoundException{
		if(symbolGraph != null) return symbolGraph;
		
		File fileOut = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\temp.txt");
		File fileIn = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\fileNode.txt");

		if(_verbose.length > 0)
			symbolGraph = new SymbolGraph(fileIn, fileOut, _verbose[0], " ");
		else symbolGraph = new SymbolGraph(fileIn, fileOut, false, " ");


		return symbolGraph;
	}
	
	public Symbol_New getCompilationUnitSymbol(){
		return compilationUnitSymbol;
	}
	
	public 	HashMap<String,ParserRuleContext> getNameToContext(){
		return contextNameToContext;
	}
}