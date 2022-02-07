package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.SqlStmtContext;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class SequenceDbAccessSqlTransactSybase extends SqlTransactSybaseBaseListener {
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	
	CompilationUnit compUnit;
	
	Symbol_New currentProcedureSymbol;
	BicamNode  currentProcedureNode;
	Symbol_New compilationUnitSymbol;
	
	NodeList nodes;

	public SequenceDbAccessSqlTransactSybase(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");

		compUnit = (CompilationUnit) properties.getProperty("COMPILATION_UNIT");
		nodes = new NodeList();
		
		compUnit = (CompilationUnit) properties.getProperty("COMPILATION_UNIT");
		compUnit.getProperties().addProperty("NODES_SEQUENCE_CALL", nodes);
	}
	
	@Override 
	public void enterStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
		compilationUnitSymbol.addProperty("NODES_SEQUENCE_CALL", nodes);
	}
	
	@Override 
	public void enterCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
		currentProcedureSymbol = st.getSymbol(ctx);
		currentProcedureNode = new BicamNode(currentProcedureSymbol.getId(), ctx.start.getStartIndex());
		nodes.add(currentProcedureNode);
	}
	
	@Override 
	public void enterSqlSingleTable(@NotNull SqlTransactSybaseParser.SqlSingleTableContext ctx) {
		ParserRuleContext sqlCommand = sqlCommandContext(ctx);

		String tableName = ctx.getText().toUpperCase();
		
		String  command = sqlCommand.start.getText().toUpperCase();
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