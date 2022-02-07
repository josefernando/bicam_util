package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.IdentifierContext;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.graph.BicamAdjacency;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class SequenceCallSqlTransactSybase extends SqlTransactSybaseBaseListener{
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
//	String          separator;
//	StringBuffer    sb;
//	KeywordLanguage keywordLanguage;
	
//	CompilationUnit compUnit;
	
/*	String serverName;
	String dbName;
	String dbOwnerName;*/
	
	
	Symbol_New currentProcedureSymbol;
	BicamNode  currentProcedureNode;
	Symbol_New compilationUnitSymbol;
	
	NodeList nodes;

	public SequenceCallSqlTransactSybase(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
//		this.separator = (String) properties.getProperty("SEPARATOR"); 
//		this.keywordLanguage = st.getKeywordsLanguage();
//		this.compUnit = (CompilationUnit) properties.getProperty("COMPILATION_UNIT");
		
		this.nodes = (NodeList) properties.getProperty("NODELIST");
	}
	
	@Override 
	public void enterStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
	}
	
/*	@Override 
	public void enterUseStmt(@NotNull SqlTransactSybaseParser.UseStmtContext ctx) {
		dbName = ctx.Name.getText();
	}*/
	
/*	private String getServerName() {
		if(serverName == null) return "SERVER_DEFAULT";
		return serverName.toUpperCase();
	}
	
	private String getDbName() {
		if(dbName == null) return "DATABASE_DEFAULT";
		return dbName.toUpperCase();
	}
	
	private String getDbOwnerName() {
		if(dbOwnerName == null) return "DB_OWNER_DEFAULT";
		return dbOwnerName.toUpperCase();
	}*/	
	
	@Override 
	public void enterCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
		Symbol_New sym = st.getSymbol(ctx);
		
		String nodeId = sym.getId().toUpperCase();

/*		String nodeName = serverName + "." 
							+ dbName + "."
						    + dbOwnerName + "." 
				            + sym.getName().toUpperCase();*/
		BicamNode node = nodes.create(nodeId);
		node.getProperties().addProperty("LABEL", sym.getName());
		node.getProperties().addProperty("TYPE", "Stored Procedure");
		currentProcedureNode = node;
	}
	
	@Override 
	public void enterProcedureCall(@NotNull SqlTransactSybaseParser.ProcedureCallContext ctx) {
		IdentifierContext idCtx = (IdentifierContext) NodeExplorer_New.getFirstChildClass(ctx, "IdentifierContext");

		Symbol_New calledProcedureSym = st.getSymbol(idCtx);
		if(calledProcedureSym == null) {
			BicamSystem.printLog(st, ctx, "ERROR", " SYMBOL NOT DEFINED FOR THIS PROCEDURE: " + idCtx.getText());
			return;
		}
		
		BicamNode calledProcedureNode = nodes.create(calledProcedureSym.getId().toUpperCase());
		
		BicamAdjacency adj = new BicamAdjacency(calledProcedureNode);
		currentProcedureNode.addAdjacencyOut(adj);		
	}
	
	public String getCompUnitName(){
		return compilationUnitSymbol.getName();
	}
	
	public NodeList getNodeList() {
		return nodes;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(BicamNode node : nodes) {
			sb.append(node + System.lineSeparator()); 
		}
		return sb.toString();
	}	
}