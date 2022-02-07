package br.com.bicam.util.catalog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.SqlStmtContext;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.Version;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class DatabaseAccessSequenceBuilder extends SqlTransactSybaseBaseListener {
	
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	String          separator;
	StringBuffer    sb;
//	CompilationUnit compUnit;
//	File compUnitFile;
	File htmlContentDir;
	
	InputStream inputStream;
	
	Version version;
	
//	Set<String> calledModuleInMethod;
	Symbol_New currentMethod;
	String currentMethodName;
	String currentCommandInGraph;
	String currentTableInGraph;
	String currentLineInGraph;

	Symbol_New compilationUnitSymbol;
	
	Map<String,Set<String>> graphvizRankSame = new HashMap<String, Set<String>>();
	Map<String,String> graphvizNode = new HashMap<String, String>();
	
	public DatabaseAccessSequenceBuilder() {
		initializeGraphvizRankSame();
	}
	
	public void setProperties(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		this.separator = (String) properties.getProperty("SEPARATOR");
		sb = (StringBuffer) properties.getProperty("STRING_BUFFER");
		version = (Version) properties.getProperty("VERSION");
		try {
			inputStream = version.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		compUnit = (CompilationUnit)properties.getProperty("COMPILATION_UNIT");
//		compUnitFile = compUnit.getFile();
		String htmlContentDirName = (String) properties.getProperty("HTML_CONTENT_DIR_NAME");
		htmlContentDir = new File(htmlContentDirName);		
	}
	
	private void initializeGraphvizRankSame() {
			graphvizRankSame.put("PROCEDURE", new HashSet<String>());
			graphvizRankSame.put("TABLE", new HashSet<String>());
			graphvizRankSame.put("COMMAND", new HashSet<String>());
			graphvizRankSame.put("LINE", new HashSet<String>());
	}
	
	@Override 
	public void enterStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
	}
	
	@Override 
	public void exitStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		graphvizRankSame();	
		
	}	
	
	@Override 
	public void enterCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
		currentMethod = st.getSymbol(ctx);
		currentMethodName = currentMethod.getName().toUpperCase();
		graphvizRankSame.get("PROCEDURE").add(currentMethodName);
	}
	
	@Override 
	public void enterSqlSingleTable(@NotNull SqlTransactSybaseParser.SqlSingleTableContext ctx) {
		ParserRuleContext sqlCommand = sqlCommandContext(ctx);
		
/*		ParserRuleContext sqlStmtCtx = NodeExplorer_New.getAncestorClass(ctx, "SqlStmtContext"); 
		if(sqlStmtCtx == null ) sqlStmtCtx = NodeExplorer_New.getAncestorClass(ctx, "SqlSelectStmtContext"); // select in conditional Expression
		Integer lineStartCommand = sqlStmtCtx.start.getLine();
		Integer lineEndCommand   = sqlStmtCtx.stop.getLine();*/

		Integer lineStartCommand = sqlCommand.start.getLine();
		Integer lineEndCommand   = sqlCommand.stop.getLine();
		
		currentLineInGraph =  currentMethodName + ":" + "LINE" + ":" + lineStartCommand + ":" + lineEndCommand;

		String tableName = ctx.getText().toUpperCase();
		currentTableInGraph = currentMethodName + ":" + tableName;
		
		String  command = sqlCommand.start.getText().toUpperCase();
		currentCommandInGraph = command + ":" + tableName;

		graphvizRankSame.get("TABLE").add(currentTableInGraph);
		graphvizRankSame.get("COMMAND").add(currentCommandInGraph);
		graphvizRankSame.get("LINE").add(currentLineInGraph);
		
		sb.append("\"" + currentMethodName + "\""+ separator + "\"" + currentTableInGraph + "\""+ System.lineSeparator());
		sb.append("\"" + currentTableInGraph + "\""+ separator + "\"" + currentCommandInGraph + "\""+ System.lineSeparator());
		sb.append("\"" + currentCommandInGraph + "\""+ separator + "\"" +  currentLineInGraph + "\""+ System.lineSeparator());

		addGraphvizNode();
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
	
	public String toString() {
		return sb.toString();
	}
	
	public String toGraphviz() {
		return toString().replace("/", " -> ");
	}
	
	public String graphvizRankSame() {
		StringBuffer rank = new StringBuffer();
		rank.append("{ rank=same; ");
		for(String s : graphvizRankSame.get("PROCEDURE")) {
			rank.append("\"" + s + "\" ");
		}
		rank.append("}" + System.lineSeparator());
		
		rank.append("{ rank=same; ");
		for(String s : graphvizRankSame.get("TABLE")) {
			rank.append("\"" + s + "\" ");
		}
		rank.append("}" + System.lineSeparator());
		
		rank.append("{ rank=same; ");
		for(String s : graphvizRankSame.get("COMMAND")) {
			rank.append("\"" + s + "\" ");
		}
		rank.append("}" + System.lineSeparator());
		
		rank.append("{ rank=same; ");
		for(String s : graphvizRankSame.get("LINE")) {
			rank.append("\"" + s + "\" ");
		}
		rank.append("}" + System.lineSeparator());
		
		return rank.toString();
	}
	
/*	public String graphvizNode() {
		StringBuffer node = new StringBuffer();
		for(String key : graphvizRankSame.keySet()) {
			String attribute = null;
			for(String nodeName : graphvizRankSame.get(key)) {
				if(key.equalsIgnoreCase("PROCEDURE")) {
					attribute = " [shape=\"box\"]";
				}
				else if(key.equalsIgnoreCase("TABLE")){
					attribute = " [shape=\"box3d\", label=\"" + nodeName.split(":")[1] + "\"" + "]";
				}
				else if(key.equalsIgnoreCase("COMMAND")){
					attribute = " [shape=none, label=\"" + nodeName.split(":")[0] + "\"" + "]";
				}
				else if(key.equalsIgnoreCase("LINE")){
					String start = nodeName.split(":")[2];
					String end =   nodeName.split(":")[3];
					attribute = " [shape=none, label=\"" + "Line "+nodeName.split(":")[2] + "\"" 
				                  + getLinkHtmlSqlCommand(new Integer(start)
				                		  , new Integer(end)) +"]";
				}
				node.append("\"" + nodeName + "\"" + attribute + System.lineSeparator()) ;
			}
		}
		return node.toString();
	}
*/	
	public void addGraphvizNode() {
		
		if(graphvizNode.get(currentMethodName) == null) {
			graphvizNode.put(currentMethodName, " [shape=\"box\"]");
		}
		if(graphvizNode.get(currentTableInGraph) == null) {
			graphvizNode.put(currentTableInGraph, " [shape=\"box3d\", label=\"" + currentTableInGraph.split(":")[1] + "\"" + "]");
		}
		if(graphvizNode.get(currentCommandInGraph) == null) {
			graphvizNode.put(currentCommandInGraph, " [shape=none, label=\"" + currentCommandInGraph.split(":")[0] + "\"" + "]");
		}
		if(graphvizNode.get(currentLineInGraph) == null) {
			String start = currentLineInGraph.split(":")[2];
			String end =   currentLineInGraph.split(":")[3];
			String attribute;
			try {
				attribute = " [shape=none, label=<<FONT color=\"blue\"><U>Line "+currentLineInGraph.split(":")[2] + "</U></FONT>>" 
				              +  " URL=\" "+getLinkHtmlSqlCommand(inputStream, new Integer(start)
				            		  , new Integer(end)) +"\" ]";
				graphvizNode.put(currentLineInGraph, attribute);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} 
		}		
	}
	
	public String graphvizNode() {
		StringBuffer nodes = new StringBuffer();
		for(String nodeName : graphvizNode.keySet()) {
				nodes.append("\"" + nodeName + "\"" + graphvizNode.get(nodeName) + System.lineSeparator()) ;
			}
		return nodes.toString();
	}
	
	private String getCodeFragment(Integer startLine, Integer endLine) {
		StringBuffer sb = new StringBuffer();
		Integer lineCount = 1;
		try {
			Reader reader = new InputStreamReader(inputStream);
			BufferedReader in = new BufferedReader(reader);
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				if(lineCount >= startLine && lineCount <= endLine) {
					sb.append(line + System.lineSeparator());
				}
				lineCount++;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
/*	private String getLinkHtmlSqlCommand(Integer startLine, Integer endLine) {
		StringBuffer sb = new StringBuffer();
		sb.append("<!DOCTYPE html><html><body><pre>" + System.lineSeparator());
		sb.append(getCodeFragment(startLine,endLine));
		sb.append("<pre><body><html>" + System.lineSeparator());
		
		String  fileName = null;
		try {
			String dirName = htmlContentDir.getCanonicalPath();
			fileName = dirName +  "/" + currentMethodName + "_" + startLine + "_" + endLine + ".html";
		    BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(fileName));
		    writer.write(sb.toString());
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return fileName;
	}*/
	
/*	private String getLinkHtmlSqlCommand(String _compUnitFileName, Integer startLine, Integer endLine) {
		try {		    
			String dirName = htmlContentDir.getCanonicalPath();
			String fileHtml = dirName +  "/" + currentMethodName + "_" + startLine + "_" + endLine + ".html";
			PropertyList properties= new PropertyList();
			properties.addProperty("START_LINE_MARK", startLine);
			properties.addProperty("END_LINE_MARK", endLine);
//			properties.addProperty("LINE_TO_NODE", compUnit.getProperties().getProperty("GRAPH_LINE_TO_NODE"));
			
			File htmlFile = BicamSystem.toHtml(_compUnitFileName, fileHtml, properties);
			return htmlFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return null;
	}*/
	private String getLinkHtmlSqlCommand(InputStream _inputstream, Integer startLine, Integer endLine) {
		try {		    
			String dirName = htmlContentDir.getCanonicalPath();
			String fileHtml = dirName +  "/" + currentMethodName + "_" + startLine + "_" + endLine + ".html";
			PropertyList properties= new PropertyList();
			properties.addProperty("START_LINE_MARK", startLine);
			properties.addProperty("END_LINE_MARK", endLine);
//			properties.addProperty("LINE_TO_NODE", compUnit.getProperties().getProperty("GRAPH_LINE_TO_NODE"));
			
			File htmlFile = BicamSystem.toHtml(_inputstream, fileHtml, properties);
			return htmlFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return null;
	}	
}
