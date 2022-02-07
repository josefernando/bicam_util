package br.com.bicam.util;

import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.graph.BicamAdjacency;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

/*
 * Em Visual Basic 6:
 * Exemplo 1. Para localizar o texto "Número do Sorteio não Encontrado!"
 * => statement.... MsgBox "Número do Sorteio não Encontrado!", MB_ICONINFORMATION, txt_msg$
 * => Function      = MsgBox
 * => parameterName = "Número do Sorteio não Encontrado!" // parametro com este texto
 * => parameterType = String Literal, String Number, Variable, etc...
 * => parameterRef  = null
 * => parameterIndex = null
 * 
 * Exemplo 2. Para localizar  stored procedure (qualquer uma, mas no caso é :prod30.dbnpesso..PR_PES_FIG_L_S07567
 * => statement...     ret% = SQLRPCInit%(SqlConn%, "prod30.dbnpesso..PR_PES_FIG_L_S07567", 0)
 * => Function             = SQLRPCInit
 * => parameterName        = null  // a busca não será por nada específico
 * => parameterType        = null
 * => parameterRef         = SqlConn
 * => parameterIndex       = 1 (primeiro parâmetro depois de SqlConn)
 *  
 * Exemplo 3. Para localizar  stored procedure (a stored procedure prod30.dbnpesso..PR_PES_FIG_L_S07567
 * => statement...     ret% = SQLRPCInit%(SqlConn%, "prod30.dbnpesso..PR_PES_FIG_L_S07567", 0)
 * => Function            = SQLRPCInit
 * => parameterName       = prod30.dbnpesso..PR_PES_FIG_L_S07567
 * => parameterType       = null, ou String Literal
 * => parameterRef        = SqlConn
 * => parameterIndex      = 1
 * 
 */
public class SearchInFunctionCall extends VisualBasic6BaseListener{
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	String          functionName = null;
	String          parameterName = null;
	String          parameterType = null;
	String          parameterRef = null;
	Integer         parameterRefPosition = null;
	
	Integer         parameterRefIndex = 0;

	Symbol_New currentMethod;
	Symbol_New compilationUnitSymbol;
	
	SymbolFactory symbolFactory;
	KeywordLanguage keywordLanguage;

	//	BicamNode  functionNode;
	
	NodeList nodes;
	
	Set<String> stpList; // lista das storedProcedures localizadas
	
	public SearchInFunctionCall(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
		
		this.symbolFactory = (SymbolFactory) properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);		
		
		this.functionName    = (String) _properties.getProperty("FUNCTION_NAME");
		this.parameterName   = (String) _properties.getProperty("PARAMETER_NAME");
		this.parameterType   = (String) _properties.getProperty("PARAMETER_TYPE");
		this.parameterRef    = (String) _properties.getProperty("PARAMETER_REF");
		this.parameterRefPosition  = (Integer) _properties.getProperty("PARAMETER_REF_POSITION");

		this.stpList = new HashSet<String>();
	}
	
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
		Object obj = compilationUnitSymbol.getProperty("NODES_SEQUENCE_CALL");
		if(obj != null) {
			nodes = (NodeList) obj;
		}
	}
	
	public void exitStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		StringBuffer sb = new StringBuffer();
		for(BicamNode node : nodes) {
			sb.append(node + System.lineSeparator()); 
		}
		System.err.println(sb.toString());
	}
	
	@Override 
	public void enterRealParameters(@NotNull VisualBasic6Parser.RealParametersContext ctx) {
		ParserRuleContext functionContext = NodeExplorer.getSibling(ctx, "IdentifierContext");
		
    	RuleContext parameterRefCtx = null;
    	Integer parameterRefIndex = null;

		if(!functionName.equals(BicamSystem.removeTypeIndicator(functionContext.getText())[0])) return;
		
		BicamNode  functionNode = null;

		Symbol_New functionSymbol = st.getSymbol(functionContext);
		if( functionSymbol != null) {
			functionNode = nodes.get(functionSymbol.getId());
			if(functionNode == null) {
				functionNode = new BicamNode(functionSymbol.getId(),functionContext.start.getStartIndex());
				nodes.add(functionNode);
				System.err.println(" INCLUDED FUNCTION IN NODES :" + functionSymbol.getId());
			}
			else {
				System.err.println(" FUNCTION ALREADY IN NODES :" + functionSymbol.getId());
			}
		}
		
        String msg = "Encontrada a funcão: " + functionName;
        BicamSystem.printLog(st, ctx, "INFO", msg);
        
        if(parameterRef != null) {
        	parameterRefCtx = (RuleContext) NodeExplorer_New.getFirstChildWithValue(ctx, parameterRef);
        	if(parameterRefCtx == null) return;
        	parameterRefIndex = NodeExplorer_New.getSiblingIndex(parameterRefCtx);
        }
        else {
        	parameterRefIndex = -1;
        }
        
        if(parameterRefPosition != null) {
        	Integer ix = parameterRefPosition + parameterRefIndex ;
        	parameterRefCtx = (RuleContext) NodeExplorer_New.getSiblingByIndex(parameterRefCtx, ix);
        	String target = parameterRefCtx.getText().replaceAll("\"", "");
        	
        	//=======================================
        	
        	Symbol_New scope = (Symbol_New) st.getScope((ParserRuleContext)parameterRefCtx);
        	
        	Symbol_New procInferSym = getSymbolInParameter((ParserRuleContext)parameterRefCtx,target, scope);       	
        	
        
        	
        	//=======================================
        	
        	target = procInferSym.getId();
        	
        	if(parameterName == null ) { // seleciona todas as procedures
        		String msgx = " store procedure : " + target;
//================================ START target parameter in node =============================
        		BicamNode targetNode = nodes.get(target.toUpperCase());
        		if(targetNode == null) {
            		targetNode = new BicamNode(target.toUpperCase(),((ParserRuleContext)parameterRefCtx).start.getStartIndex());
    				nodes.add(targetNode);
    				System.err.println(" INCLUDED TARGET IN NODES :" + targetNode.getId());
    			}   
    			functionNode.addAdjacencyOut(new BicamAdjacency(targetNode));
//============================================ END ============================================
        		BicamSystem.printLog(st, (ParserRuleContext)parameterRefCtx, "INFO", msgx);
        	}
        	else {
        		if(parameterName.equalsIgnoreCase(target)) {
            		String msgx = " store procedure : " + target;
            		BicamNode targetNode = nodes.get(target.toUpperCase());
            		if(targetNode == null) {
                		targetNode = new BicamNode(target.toUpperCase(),((ParserRuleContext)parameterRefCtx).start.getStartIndex());
        				nodes.add(targetNode);
        				System.err.println(" INCLUDED TARGET IN NODES :" + targetNode.getId());
        			}   
        			functionNode.addAdjacencyOut(new BicamAdjacency(targetNode));
    //============================================ END ============================================
            		BicamSystem.printLog(st, (ParserRuleContext)parameterRefCtx, "INFO", msgx);
            		return;
        		}
        	}
        }
        else {
        	ParserRuleContext parameterNameCtx = (ParserRuleContext) NodeExplorer_New.getFirstChildWithValue(ctx, "\"" + parameterName + "\"");
        	if(parameterNameCtx == null) return;
        	String target = parameterNameCtx.getText().replaceAll("\"", "");
        	String msgx = " store procedure : " + target;
        	//================================ START target parameter in node =============================
    		BicamNode targetNode = nodes.get(target.toUpperCase());
    		if(targetNode == null) {
        		targetNode = new BicamNode(target.toUpperCase(),((ParserRuleContext)parameterRefCtx).start.getStartIndex());
				nodes.add(targetNode);
				System.err.println(" INCLUDED TARGET IN NODES :" + targetNode.getId());
			}   
			functionNode.addAdjacencyOut(new BicamAdjacency(targetNode));
//============================================ END ============================================
    		BicamSystem.printLog(st, (ParserRuleContext)parameterRefCtx, "INFO", msgx);
        }
 	}
	
	public List<String> getStoredProcedures() {
		return new ArrayList<String>(stpList);
	}
	
	private Symbol_New getSymbolInParameter(ParserRuleContext _ctx, String _name, Symbol_New _scope) {
		PropertyList properties = defaultProperties(_ctx);
	
		PropertyList dbProperties = setDatabaseProperties(_ctx, _name);
		
		if(dbProperties != null) {
			properties.addProperty(NAME, dbProperties.getProperty("NAME"));
			properties.addProperty("PROPERTY_LIST", dbProperties);
		}
		
		Symbol_New sym = _scope.resolve(properties);
		if(sym == null) {
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: SYMBOL '%s' NOT RESOLVED in COMPILATION UNIT '%s' in line %d%n"
						                ,_ctx.getText(), st.getCompilarionUnitSymbol(_ctx).getName()
						                , _ctx.start.getLine());
				e.printStackTrace();
			}
			return null;
		}
		return sym;
	}
	
	private PropertyList setDatabaseProperties(ParserRuleContext _ctx, String _name) {
		
		
//		if(_name.startsWith("@")) return null; // é variável local ou de sistemas
		
		Map<String,PropertyList> propertiesByName = new HashMap<String,PropertyList>();
		
		PropertyList properties = null;

		String defaultServerName = "SERVER_DEFAULT";//keywordLanguage.getParameter("SERVER_DEFAULT");
		String defaultDbName = "DATABASE_DEFAULT";//.getParameter("DATABASE_DEFAULT");
		String defaultDbOwnerName = "DB_OWNER_DEFAULT";//keywordLanguage.getParameter("DB_OWNER_DEFAULT");
		
		String fullName = null;
		
		String serverName = null;
		String dbName = null;
		String ownerName = null;
		String tableOrProcName = null;
		String columnName = null;

		
		SymbolType_New symbolType = null;
		String categoryType = null;
		

/*		if(NodeExplorer_New.getAncestorClass(_ctx, "SqlTableReferenceContext",true) != null) {
			properties = databaseProperties(_ctx);
			symbolType = SymbolType_New.TABLE;
			categoryType = "TABLE";

			String memberAccessOper = "\\."; 
			String[] qualifiedName = _name.split(memberAccessOper);
			if(qualifiedName.length == 1) {  //select table
				fullName = defaultServerName + "."
						   + defaultDbName + "."
						   + defaultDbOwnerName + "."
						   + qualifiedName[0];
			}
			else if(qualifiedName.length == 2) { // select dbOwner.table
				fullName = defaultServerName + "."
						   + defaultDbName + "."
						   + qualifiedName[0] + "."
						   + qualifiedName[1];				
			}
			else if(qualifiedName.length == 3) { // select db.dbOwner.table ou db..table
				if(qualifiedName[1].length() == 0){
					fullName = defaultServerName + "."
							   + qualifiedName[0] + "."
							   + defaultDbOwnerName + "."
							   + qualifiedName[2];					
				}
				else {
					fullName = defaultServerName + "."
							   + qualifiedName[0] + "."
							   + qualifiedName[1] + "."
							   + qualifiedName[2];					
				}
			}
			else if(qualifiedName.length == 4) { // select server.db.dbOwner.table ou server.db..table 
				if(qualifiedName[2].length() == 0){
					fullName = qualifiedName[0] + "."
							   + qualifiedName[1] + "."
							   + defaultDbOwnerName + "."
							   + qualifiedName[3];					
				}
				else {
					fullName = qualifiedName[0] + "."
							   + qualifiedName[1] + "."
							   + qualifiedName[2] + "."
							   + qualifiedName[3];						
				}				
			}
		}*/
		
		// definir properties de  cada uma das partes
//		else if(NodeExplorer_New.hasAncestorClass(_ctx, "ProcedureCallContext")) {
			if(NodeExplorer_New.getAncestorClass(_ctx, "RealParameterContext",true) !=null) {
			
			properties = databaseProperties(_ctx);
			symbolType = SymbolType_New.STORED_PROCEDURE;
			categoryType = "STORED_PROCEDURE";
			String memberAccessOper = "\\."; 
			_name = _name.replace("\"", "");
			String[] qualifiedName = _name.split(memberAccessOper);

			if(qualifiedName.length == 1) {  //select table
				fullName = defaultServerName + "."
						   + defaultDbName + "."
						   + defaultDbOwnerName + "."
						   + qualifiedName[0];
			}
			else if(qualifiedName.length == 2) { // select dbOwner.table
				fullName = defaultServerName + "."
						   + defaultDbName + "."
						   + qualifiedName[0] + "."
						   + qualifiedName[1];				
			}
			else if(qualifiedName.length == 3) { // select db.dbOwner.table ou db..table
				if(qualifiedName[1].length() == 0){
					fullName = defaultServerName + "."
							   + qualifiedName[0] + "."
							   + defaultDbOwnerName + "."
							   + qualifiedName[2];					
				}
				else {
					fullName = defaultServerName + "."
							   + qualifiedName[0] + "."
							   + qualifiedName[1] + "."
							   + qualifiedName[2];					
				}
			}
			else if(qualifiedName.length == 4) { // select server.db.dbOwner.table ou server.db..table 
				if(qualifiedName[2].length() == 0){
					fullName = qualifiedName[0] + "."
							   + qualifiedName[1] + "."
							   + defaultDbOwnerName + "."
							   + qualifiedName[3];					
				}
				else {
					fullName = qualifiedName[0] + "."
							   + qualifiedName[1] + "."
							   + qualifiedName[2] + "."
							   + qualifiedName[3];						
				}				
			}
			
		}
//		else if (NodeExplorer_New.getAncestorClassStartsWith(_ctx, "Sql") != null) {
/*		else if (NodeExplorer_New.getAncestorClass(_ctx, "SqlSubSelectStmtContext") != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlUpdateStmtContext")  != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlDeleteStmtContext")  != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlInsertStmtContext")  != null	) {
		if(_name.startsWith("@")) return properties; // variável local ou de sistema(@@)
//			if(!isSymbolToResolveDatabase(_ctx)) return properties;
			if(_name.contains(".")) {
				symbolType = SymbolType_New.COLUMN;
				categoryType = "COLUMN";
				PropertyList columnProperties = databaseProperties(_ctx);
				columnProperties.addProperty("NAME", _name.split("\\.")[1]);
				columnProperties.addProperty("CATEGORY_TYPE", categoryType);
				columnProperties.addProperty(SYMBOL_TYPE, symbolType);	
				propertiesByName.put(_name.split("\\.")[1], columnProperties);
				properties = databaseProperties(_ctx);
				properties.addProperty("NAME", _name);
				properties.addProperty("CATEGORY", "DATABASE");
				properties.addProperty("PROPERTIES_BY_NAME", propertiesByName);
				return properties;
			}
			properties = databaseProperties(_ctx);
			symbolType = SymbolType_New.COLUMN;
			categoryType = "COLUMN";
			String tableName = getTableNameByColumn(_ctx);
			
			if(tableName == null) return null;
			
			fullName = montaFullName(tableName);
			fullName = fullName +  "." + _name;
			columnName = _name;
		}*/

		if(properties != null) {
			properties.addProperty("NAME", fullName);
		
			serverName = fullName.split("\\.")[0];
			dbName = fullName.split("\\.")[1];
			ownerName = fullName.split("\\.")[2];
			tableOrProcName = fullName.split("\\.")[3];	
			
			PropertyList serverProperties = databaseProperties(_ctx);
			PropertyList dbProperties = databaseProperties(_ctx);
			PropertyList ownerProperties = databaseProperties(_ctx);
			PropertyList tableOrProcProperties = databaseProperties(_ctx);
			
			serverProperties.addProperty("NAME", serverName);
			serverProperties.addProperty("CATEGORY_TYPE", "SERVER");
			serverProperties.addProperty(SYMBOL_TYPE, SymbolType_New.SERVER);
			
			dbProperties.addProperty("NAME", dbName);
			dbProperties.addProperty("CATEGORY_TYPE", "DATABASE");
			dbProperties.addProperty(SYMBOL_TYPE, SymbolType_New.DATABASE);
			
			ownerProperties.addProperty("NAME", ownerName);
			ownerProperties.addProperty("CATEGORY_TYPE", "OWNER_DB");
			ownerProperties.addProperty(SYMBOL_TYPE, SymbolType_New.USERDB);
			
			tableOrProcProperties.addProperty("NAME", tableOrProcName);
			tableOrProcProperties.addProperty("CATEGORY_TYPE", categoryType);
			tableOrProcProperties.addProperty(SYMBOL_TYPE, symbolType);
			
			propertiesByName.put(serverName, serverProperties);
			propertiesByName.put(dbName, dbProperties);
			propertiesByName.put(ownerName, ownerProperties);
			propertiesByName.put(tableOrProcName, tableOrProcProperties);
			
			if(columnName != null) {
				PropertyList columnProperties = databaseProperties(_ctx);
				columnProperties.addProperty("NAME", columnName);
				columnProperties.addProperty("CATEGORY_TYPE", categoryType);
				columnProperties.addProperty(SYMBOL_TYPE, symbolType);	
				propertiesByName.put(columnName, columnProperties);
			}
			
			properties.addProperty("PROPERTIES_BY_NAME", propertiesByName);
		}

		return properties;
	}
	
	private PropertyList databaseProperties(ParserRuleContext _ctx) {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(DEF_MODE, "INFER");
		properties.addProperty("CATEGORY", "DATABASE");
		return properties;
	}
	
	private PropertyList defaultProperties(ParserRuleContext _ctx) {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(DEF_MODE, "EXPLICIT");// MANUTENÇÃO DA LÓGICO
		properties.addProperty(ENCLOSING_SCOPE, st.getScope(_ctx));
		return properties;
	}	
}