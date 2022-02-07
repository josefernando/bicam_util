package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.ExprContext;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.IdentifierContext;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.RealParameterContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class SqlTransactSybaseCompilationUnitRefSymbol extends SqlTransactSybaseBaseListener {
	String absoluteFileName; 

	String moduleName;
	
	ParserRuleContext startRuleContext;

	SymbolTable_New st;
	Deque<IScope_New> scopes;
	IScope_New globalScope;
	IScope_New compilationUnitScope;

	SymbolFactory symbolFactory;
	Object compUnit;
	PropertyList properties;
//	CompilationUnit compilationUnit;

	KeywordLanguage keywordLanguage;

	PropertyList currentProperties;

	Set<String> controlArrays;
	Map<Integer, String> controlArrayMap;
	Set<String> controlArrayAddedInScope;
	Map<String, Integer> occurs;
	Map<String,Symbol_New> uicontrolVisited;

	final String[] sulfixTypeIndicator = new String[] { "&", "%", "#", "!", "@", "$" };

	public SqlTransactSybaseCompilationUnitRefSymbol(PropertyList _propertyList) {
		this.scopes = new ArrayDeque<IScope_New>();
		this.properties = _propertyList;
//		this.compilationUnit = (CompilationUnit) properties.getProperty("COMPILATION_UNIT");
		this.st = (SymbolTable_New) properties.getProperty(SYMBOLTABLE);
		this.symbolFactory = (SymbolFactory) properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);
		this.globalScope = st.getGlobalScope();//(IScope_New) properties.getProperty(GLOBAL_SCOPE);
//		setCurrentScope(globalScope);
	}

	
	@Override 
	public void exitIdentifier(@NotNull SqlTransactSybaseParser.IdentifierContext ctx) { 
		if(!isSymbolToResolve(ctx)) return;
		Symbol_New scope = null;
		scope = (Symbol_New)st.getScope(ctx);
		if(scope == null) {
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: SCOPE NOT MARKED IN identifierContext '%s' in COMPILATION UNIT '%s' in line %d%n"
						                ,ctx.getText(), st.getCompilarionUnitSymbol(ctx).getName()
						                , ctx.start.getLine());
				e.printStackTrace();
			}
			return;
		}
		
		String name = ctx.getText();
		
		PropertyList properties = defaultProperties(ctx);
		properties.addProperty("NAME", name);

		PropertyList dbPproperties = new PropertyList();
		dbPproperties.addProperty("SYMBOLTABLE",st);
		dbPproperties.addProperty("CONTEXT",ctx);
		dbPproperties.addProperty("NAME", name);
		dbPproperties.addProperty("SYMBOL_FACTORY",symbolFactory);
		dbPproperties.addProperty("KEYWORD_LANGUAGE",keywordLanguage);	
		dbPproperties.addProperty("SCOPE",scope);
		
		SetDatabasePropertiesSqlTransactSybaseVisitor setDatabaseProp = 
				new SetDatabasePropertiesSqlTransactSybaseVisitor(dbPproperties);
		
		PropertyList dbProperties = setDatabaseProp.run();
		if(dbProperties != null) properties = dbProperties;
		
		Symbol_New sym = scope.resolve(properties);
		if(sym == null) {
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: SYMBOL '%s' NOT RESOLVED in COMPILATION UNIT '%s' in line %d%n"
						                ,ctx.getText(), st.getCompilarionUnitSymbol(ctx).getName()
						                , ctx.start.getLine());
				e.printStackTrace();
			}
			return;
		}
		st.setSymbol(ctx,sym);
	}
	
	public SymbolTable_New getSymbolTable() {
		return st;
	}

	private void setCurrentScope(IScope_New _scope) {
		scopes.push(_scope);
	}

/*	private IScope_New getCurrentScope() {
		return scopes.peek();
	}

	private void removeCurrentScope() {
		scopes.pop();
	}*/

	private String[] removeParen(String[] _nameParts) { // A(i) -> A
		int parenCount = 0;
		String _name = _nameParts[0];
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < _name.length(); i++) {
			if (_name.substring(i, i + 1).equals("(")) {
				parenCount++;
				continue;
			}
			if (_name.substring(i, i + 1).equals(")")) {
				parenCount--;
				continue;
			}
			if (parenCount == 0) {
				s = s.append(_name.substring(i, i + 1));
			}
		}
		_nameParts[0] = s.toString();
		return _nameParts;
	}

	private String[] removeParenETypeIndicator(String _name) {
		return removeParen(removeTypeIndicator(_name));
	}

	private String[] removeTypeIndicator(String name) {
		ArrayList<String> parts = new ArrayList<String>();
		for (String s : sulfixTypeIndicator) {
			if (name.endsWith(s)) {
				name = name.replace(s, ""); // A$ -> A
				parts.add(name);
				parts.add(s);
				break;
			}
		}
		if (parts.size() == 0)
			parts.add(name);

		String[] ret = new String[parts.size()];
		for (int ix = 0; ix < parts.size(); ix++) {
			ret[ix] = parts.get(ix);
		}
		return ret;
	}
	
	private boolean isSymbolToResolveDatabase(ParserRuleContext _ctx) {
		String name = _ctx.getText();
		if(name.startsWith("@")) return false; // é variável local ou de sistemas
        if(NodeExplorer_New.hasSibling(_ctx, "RealParametersContext")) return false;
        // Colunas orderBy tem que estar em resultSet, então não valido
        return true;
	}
	
	private boolean isSymbolToResolve(ParserRuleContext _ctx){
		if(NodeExplorer.hasSibling(_ctx, "IdentifierContext")) return false;
		if(st.getSymbol(_ctx) != null) return false; // Symbol já definido. Deve ser definiton Stmt
        if(NodeExplorer_New.getAncestorClass(_ctx, "SqlOrderEntrieContext") != null) return false;
        if(NodeExplorer_New.getAncestorClass(_ctx, "SqlGroupByClauseContext") != null) return false;
		
		if(!NodeExplorer.hasAncestorClass(_ctx, "CreateProcedureStmtContext")) {
			return false;
		}
		
		if(NodeExplorer.getNextAncestorClass(_ctx, "CreateProcedureStmtContext") != null) { // Nome da procedure
			return false;
		}
		
		if(NodeExplorer.hasAncestorClass(_ctx, "VariableStmtContext")) {
			return false;
		}	
		
		if(NodeExplorer_New.hasAncestorClass(_ctx, "ProcedureCallContext")) {
			if(NodeExplorer_New.hasAncestorClass(_ctx, "RealParameterContext")) {
				RealParameterContext realContext = (RealParameterContext) NodeExplorer_New.getAncestorClass(_ctx, "RealParameterContext");
				ExprContext exprContext = (ExprContext) NodeExplorer_New.getFirstChildClass(realContext, "ExprContext");
				IdentifierContext idContext = (IdentifierContext) NodeExplorer_New.getChildClass(exprContext, "IdentifierContext");

				if(idContext.equals(_ctx)) return false;
			}
		}
		
		IdentifierContext childId = null;
		childId = (IdentifierContext)NodeExplorer.getFirstChildClass(_ctx, "IdentifierContext");
		
		if(childId != null && st.getSymbol(childId) != null){
			st.setSymbol(_ctx, st.getSymbol(childId));
			return false;
		}		
		return true;
	}
	
/*	private PropertyList setDatabaseProperties(ParserRuleContext _ctx, String _name) {
		if(_name.startsWith("@")) return null; // é variável local ou de sistemas
		
		Map<String,PropertyList> propertiesByName = new HashMap<String,PropertyList>();
		
		PropertyList properties = null;

		String defaultServerName = keywordLanguage.getParameter("SERVER_DEFAULT");
		String defaultDbName = keywordLanguage.getParameter("DATABASE_DEFAULT");
		String defaultDbOwnerName = keywordLanguage.getParameter("DB_OWNER_DEFAULT");
		
		String fullName = null;
		
		String serverName = null;
		String dbName = null;
		String ownerName = null;
		String tableOrProcName = null;
		String columnName = null;

		
		SymbolType_New symbolType = null;
		String categoryType = null;
		

		if(NodeExplorer_New.getAncestorClass(_ctx, "SqlTableReferenceContext",true) != null) {
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
		}
		
		// definir properties de  cada uma das partes
		else if(NodeExplorer_New.hasAncestorClass(_ctx, "ProcedureCallContext")) {
			properties = databaseProperties(_ctx);
			symbolType = SymbolType_New.STORED_PROCEDURE;
			categoryType = "STORED_PROCEDURE";
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
			
		}
//		else if (NodeExplorer_New.getAncestorClassStartsWith(_ctx, "Sql") != null) {
		else if (NodeExplorer_New.getAncestorClass(_ctx, "SqlSubSelectStmtContext") != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlUpdateStmtContext")  != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlDeleteStmtContext")  != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlInsertStmtContext")  != null	) {
		if(_name.startsWith("@")) return properties; // variável local ou de sistema(@@)
			if(!isSymbolToResolveDatabase(_ctx)) return properties;
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
		}

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
	}*/
	
/*	private PropertyList setDatabaseProperties(ParserRuleContext _ctx, String _name) {
		if(_name.startsWith("@")) return null; // é variável local ou de sistemas
		
		Map<String,PropertyList> propertiesByName = new HashMap<String,PropertyList>();
		
		PropertyList properties = null; 
		String defaultDbName = null;

		String defaultServerName = keywordLanguage.getParameter("SERVER_DEFAULT");
		if(_name.startsWith("#")) {
			defaultDbName = "tempdb";
			defaultServerName = defaultServerName + "_TEMPDB";
		}
		else {
			defaultDbName = keywordLanguage.getParameter("DATABASE_DEFAULT");
		}
		String defaultDbOwnerName = keywordLanguage.getParameter("DB_OWNER_DEFAULT");
		
		String fullName = null;
		
		String serverName = null;
		String dbName = null;
		String ownerName = null;
		String objectName = null;
		
		SymbolType_New symbolType = null;
		String categoryType = null;
		

		if(NodeExplorer_New.getAncestorClass(_ctx, "SqlTableReferenceContext",true) != null) {
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
		}
		
		// definir properties de  cada uma das partes
		else if(NodeExplorer_New.hasAncestorClass(_ctx, "ProcedureCallContext")) {
			properties = databaseProperties(_ctx);
			symbolType = SymbolType_New.STORED_PROCEDURE;
			categoryType = "STORED_PROCEDURE";
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
			
		}
		// definir properties de  cada uma das partes
		else if(NodeExplorer_New.getAncestorClass(_ctx, "SqlCreateTableStmtContext", true) != null) {
			properties = databaseProperties(_ctx);
			symbolType = SymbolType_New.TABLE;
			categoryType = SymbolType_New.TABLE.toString();
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
			
		}
//		else if (NodeExplorer_New.getAncestorClassStartsWith(_ctx, "Sql") != null) {
		else if (NodeExplorer_New.getAncestorClass(_ctx, "SqlSubSelectStmtContext") != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlUpdateStmtContext")  != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlDeleteStmtContext")  != null
				|| NodeExplorer_New.getAncestorClass(_ctx, "SqlInsertStmtContext")  != null	) {
		
			if(_name.startsWith("@")) return properties; // variável local ou de sistema(@@)
			if(!isSymbolToResolveDatabase(_ctx)) return properties;
			
			properties = databaseProperties(_ctx);
			properties.addProperty("CATEGORY_TYPE", "COLUMN");
			properties.addProperty(SYMBOL_TYPE, SymbolType_New.COLUMN);
			symbolType = SymbolType_New.COLUMN;
			categoryType = SymbolType_New.COLUMN.toString();
			fullName = _name;
		}

		if(properties != null) {
			properties.addProperty("NAME", fullName);
		
			serverName = fullName.split("\\.")[0];
			dbName = fullName.split("\\.")[1];
			ownerName = fullName.split("\\.")[2];
			objectName = fullName.split("\\.")[3];	
			
			PropertyList serverProperties = databaseProperties(_ctx);
			PropertyList dbProperties = databaseProperties(_ctx);
			PropertyList ownerProperties = databaseProperties(_ctx);
			PropertyList objectProperties = databaseProperties(_ctx);
			
			serverProperties.addProperty("NAME", serverName);
			serverProperties.addProperty("CATEGORY_TYPE", "SERVER");
			serverProperties.addProperty(SYMBOL_TYPE, SymbolType_New.SERVER);
			if(fullName.contains("#")) { // temporary nonsharable table
				serverProperties.addProperty(ENCLOSING_SCOPE, compilationUnitScope);
			}
			
			dbProperties.addProperty("NAME", dbName);
			dbProperties.addProperty("CATEGORY_TYPE", "DATABASE");
			dbProperties.addProperty(SYMBOL_TYPE, SymbolType_New.DATABASE);
			
			ownerProperties.addProperty("NAME", ownerName);
			ownerProperties.addProperty("CATEGORY_TYPE", "OWNER_DB");
			ownerProperties.addProperty(SYMBOL_TYPE, SymbolType_New.USERDB);
			
			objectProperties.addProperty("NAME", objectName);
			objectProperties.addProperty("CATEGORY_TYPE", categoryType);
			objectProperties.addProperty(SYMBOL_TYPE, symbolType);
			
			propertiesByName.put(serverName, serverProperties);
			propertiesByName.put(dbName, dbProperties);
			propertiesByName.put(ownerName, ownerProperties);
			propertiesByName.put(objectName, objectProperties);
			
			properties.addProperty("PROPERTIES_BY_NAME", propertiesByName);
		}

		return properties;
	}*/		
	
/*	private String getTableNameByColumn(ParserRuleContext _columnContext) {
		
		
		SqlTableReferenceContext tableCtx = null;
		
        if(NodeExplorer_New.hasAncestorClass(_columnContext, "SqlResultColumnContext")) {
    		SqlResultColumnContext resultCtx = (SqlResultColumnContext) NodeExplorer_New.getAncestorClass(_columnContext, "SqlResultColumnContext");
            if(resultCtx == null) return null;
    		SqlFromClauseContext fromCtx = (SqlFromClauseContext) NodeExplorer_New.getSibling(resultCtx, "SqlFromClauseContext");
            if(fromCtx == null) return null;
    		tableCtx = (SqlTableReferenceContext) NodeExplorer_New.getFirstChildClass(fromCtx, "SqlTableReferenceContext");
        }
        else if(NodeExplorer_New.hasAncestorClass(_columnContext, "SqlUpdateAssignmentContext")){
        	SqlUpdateAssignmentContext updateAssigmentCtx = (SqlUpdateAssignmentContext) NodeExplorer_New.getAncestorClass(_columnContext, "SqlUpdateAssignmentContext");
            if(updateAssigmentCtx == null) return null;
        	tableCtx = (SqlTableReferenceContext) NodeExplorer_New.getSibling(updateAssigmentCtx, "SqlTableReferenceContext");
        }
        else if(NodeExplorer_New.hasAncestorClass(_columnContext, "SqlInsertColumnsContext")){
    		SqlInsertColumnsContext insertCtx = (SqlInsertColumnsContext) NodeExplorer_New.getAncestorClass(_columnContext, "SqlInsertColumnsContext");
            if(insertCtx == null) return null;
    		tableCtx = (SqlTableReferenceContext) NodeExplorer_New.getSibling(insertCtx, "SqlTableReferenceContext");
        }
        else if(NodeExplorer_New.hasAncestorClass(_columnContext, "SqlWhereClauseContext")){
        	SqlWhereClauseContext whereCtx = (SqlWhereClauseContext) NodeExplorer_New.getAncestorClass(_columnContext, "SqlWhereClauseContext");
            if(whereCtx == null) return null;
    		tableCtx = (SqlTableReferenceContext) NodeExplorer_New.getSibling(whereCtx, "SqlTableReferenceContext");
    		if(tableCtx == null) { // Não é insert ou update. /então é "where" de "Select"
        		SqlFromClauseContext fromCtx = (SqlFromClauseContext) NodeExplorer_New.getSibling(whereCtx, "SqlFromClauseContext");
                if(fromCtx == null) return null;
        		tableCtx = (SqlTableReferenceContext) NodeExplorer_New.getFirstChildClass(fromCtx, "SqlTableReferenceContext");
    		}
        }
        
        if(tableCtx == null) {
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: TABLE NAME NOT FOUND FOR COLUMN %s IN COMPILATION UNIT '%s' in line %d%n"
						                , _columnContext.getText(), st.getCompilarionUnitSymbol(_columnContext).getName()
						                , _columnContext.start.getLine());
				e.printStackTrace();
			}
        }
        
        return tableCtx.getText();
	}*/
	
/*	private String montaFullName(String _name) {
		String defaultServerName = keywordLanguage.getParameter("SERVER_DEFAULT");
		String defaultDbName = keywordLanguage.getParameter("DATABASE_DEFAULT");
		String defaultDbOwnerName = keywordLanguage.getParameter("DB_OWNER_DEFAULT");
		
		String fullName = null;
		
		String serverName = null;
		String dbName = null;
		String ownerName = null;
		String tableOrProcName = null;
		String columnName = null;	
		
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
		
		return fullName;
	}*/
	
/*	private PropertyList databaseProperties(ParserRuleContext _ctx) {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(DEF_MODE, "INFER");
		properties.addProperty("CATEGORY", "DATABASE");
//		properties.addProperty(ENCLOSING_SCOPE, st.getGlobalScope());
		return properties;
	}*/
	
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