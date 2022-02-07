package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.SqlStmtContext;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.graph.BicamAdjacency;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class DbAccessSqlTransactSybase extends SqlTransactSybaseBaseListener {
	
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	NodeList nodeList;
	CompilationUnit compUnit;
	
	Symbol_New currentMethod;
	String currentMethodName;
	String currentCommandInGraph;
	String currentTableInGraph;
	String currentLineInGraph;
	
	BicamNode currentProcedure;

	Symbol_New compilationUnitSymbol;
	
/*	public DbAccessSqlTransactSybase() {
		initializeGraphvizRankSame();
	}*/
	
	public DbAccessSqlTransactSybase(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		this.compUnit = (CompilationUnit)properties.getProperty("COMPILATION_UNIT");
		this.nodeList = (NodeList) properties.getProperty("NODELIST");	
	}
	
/*	private void initializeGraphvizRankSame() {
			graphvizRankSame.put("PROCEDURE", new HashSet<String>());
			graphvizRankSame.put("TABLE", new HashSet<String>());
			graphvizRankSame.put("COMMAND", new HashSet<String>());
			graphvizRankSame.put("LINE", new HashSet<String>());
	}*/
	
	@Override 
	public void enterStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
	}
	
/*	@Override 
	public void exitStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		graphvizRankSame();	
	}*/	
	
	@Override 
	public void enterCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
		currentMethod = st.getSymbol(ctx);
		currentMethodName = currentMethod.getId().toUpperCase();
		
		BicamNode node = nodeList.create(currentMethodName);
		node.getProperties().addProperty("LABEL", currentMethod.getName());
		node.getProperties().addProperty("TYPE", "stored procedure");
		currentProcedure = node;
//		graphvizRankSame.get("PROCEDURE").add(currentMethodName);
	}
	
	@Override 
	public void enterSqlSingleTable(@NotNull SqlTransactSybaseParser.SqlSingleTableContext ctx) {
		if(currentProcedure == null) return;
		
		ParserRuleContext sqlCommand = sqlCommandContext(ctx);

		Integer lineStartCommand = sqlCommand.start.getLine();
		Integer lineEndCommand   = sqlCommand.stop.getLine();
		
		currentLineInGraph =  currentMethodName + ":" + "LINE" + ":" + lineStartCommand + ":" + lineEndCommand;

		Symbol_New tableSymbol = st.getSymbol(ctx);
//		String tableName = ctx.getText().toUpperCase();
		String tableId = tableSymbol.getId().toUpperCase();
		
		BicamNode tbNode = nodeList.create(tableId);
		tbNode.getProperties().addProperty("LABEL", tableSymbol.getName());
		tbNode.getProperties().addProperty("TYPE", "TABLE");

		String  command = sqlCommand.start.getText().toUpperCase();
		
		BicamAdjacency adj = new BicamAdjacency(tbNode);
		adj.getProperties().addProperty("LABEL", command);
		adj.getProperties().addProperty("TYPE", "sql command");		

		currentProcedure.addAdjacency(adj);
	}
	
	private ParserRuleContext sqlCommandContext(ParserRuleContext _ctx) {
		ParserRuleContext sqlcommandCtx = (SqlStmtContext) NodeExplorer_New.getAncestorClass(_ctx, "SqlStmtContext");
		if(sqlcommandCtx == null || sqlcommandCtx.getText().startsWith("(")) sqlcommandCtx = NodeExplorer_New.getAncestorClass(_ctx, "SqlFullSelectStmtContext"); // select in conditional Expression
		if(sqlcommandCtx == null) {
			ParserRuleContext cx = NodeExplorer_New.getAncestorClass(_ctx, "DefinitionContext");
			sqlcommandCtx = (ParserRuleContext) NodeExplorer_New.getFirstChildInstanceOf(cx, ParserRuleContext.class); // create index 
		}

		return sqlcommandCtx;
	}
}
