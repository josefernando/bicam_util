package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_TYPE;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.SqlSingleTableContext;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class SetDatabasePropertiesSqlTransactSybaseVisitor {
	SymbolTable_New st;
	ParserRuleContext context;
	String name;
	SymbolFactory symbolFactory;
	KeywordLanguage keywordLanguage;
	PropertyList properties;
	String categoryType;
	SymbolType_New symType;
	IScope_New scope;
	
	public SetDatabasePropertiesSqlTransactSybaseVisitor(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) _properties.getProperty("SYMBOLTABLE");
		this.context = (ParserRuleContext) _properties.getProperty("CONTEXT");
		this.name = (String) _properties.getProperty("NAME");
		this.symbolFactory = (SymbolFactory)_properties.getProperty("SYMBOL_FACTORY");
		this.keywordLanguage = (KeywordLanguage)_properties.getProperty("KEYWORD_LANGUAGE");
		this.scope = (IScope_New)_properties.getProperty("SCOPE");
	}
	
	public PropertyList run() {
		if(!isSymbolToResolveDatabase(context)) return null;
//		String qualifiedName = getQualifiedName();
		String qualifiedName = BicamSystem.sqlNameToFullQualifiedName(name);
		
		setCategoryType();
		
		Map<String,PropertyList> propertiesByName = new HashMap<String,PropertyList>();
		
		PropertyList setProperties = null; 
		
		setProperties = databaseProperties();
		
		SymbolType_New symbolType = null;
		
		if(categoryType.equalsIgnoreCase("COLUMN")) {
			if(name.contains(".")) {
				symbolType = SymbolType_New.COLUMN;
				categoryType = "COLUMN";
				PropertyList columnProperties = databaseProperties();
				columnProperties.addProperty("NAME", name.split("\\.")[1]);

				columnProperties.addProperty("CATEGORY_TYPE", categoryType);
				columnProperties.addProperty("SYMBOL_TYPE", symbolType);	
				setProperties = databaseProperties();
				setProperties.addProperty("NAME", name);
				setProperties.addProperty("CATEGORY", "DATABASE");
				setProperties.addProperty("PROPERTIES_BY_NAME", propertiesByName);
				propertiesByName.put(name.split("\\.")[1], columnProperties);
				return setProperties;
			}
			else {
				ParserRuleContext sqlContext = (ParserRuleContext) NodeExplorer_New.getAncestorClass(context, "SqlStmtContext");
				if(sqlContext == null) {
					sqlContext = (ParserRuleContext) NodeExplorer_New.getAncestorClass(context, "SqlSubSelectStmtContext");
				}
				SqlSingleTableContext sigleTableContext = (SqlSingleTableContext) NodeExplorer_New.getFirstChildClass(sqlContext, "SqlSingleTableContext");
				Symbol_New sym = st.getSymbol(sigleTableContext);
				
				PropertyList columnProperties = databaseProperties();
				symbolType = SymbolType_New.COLUMN;
				columnProperties.addProperty("NAME", name);
				columnProperties.addProperty("CATEGORY_TYPE", categoryType);
				columnProperties.addProperty("SYMBOL_TYPE", symbolType);
				columnProperties.addProperty("ENCLOSING_SCOPE", sym);

				propertiesByName.put(name, columnProperties);

				setProperties = databaseProperties();
				setProperties.addProperty("NAME", name);
				setProperties.addProperty("CATEGORY", "DATABASE");
				setProperties.addProperty("PROPERTIES_BY_NAME", propertiesByName);
				setProperties.addProperty("PROPERTY_LIST", setProperties);

				return setProperties;				
 			}
		}
		
			String serverName = qualifiedName.split("\\.")[0];
			String dbName = qualifiedName.split("\\.")[1];
			String ownerName = qualifiedName.split("\\.")[2];
			String objectName = qualifiedName.split("\\.")[3];	
			
			PropertyList serverProperties = databaseProperties();
			PropertyList dbProperties = databaseProperties();
			PropertyList ownerProperties = databaseProperties();
			PropertyList objectProperties = databaseProperties();
			
			serverProperties.addProperty("NAME", serverName);
			serverProperties.addProperty("CATEGORY_TYPE", "SERVER");
			serverProperties.addProperty(SYMBOL_TYPE, SymbolType_New.SERVER);
			if(qualifiedName.contains("#")) { // temporary nonsharable table
				serverProperties.addProperty(ENCLOSING_SCOPE, st.getCompilarionUnitSymbol(context));
			}
			
			dbProperties.addProperty("NAME", dbName);
			dbProperties.addProperty("CATEGORY_TYPE", "DATABASE");
			dbProperties.addProperty(SYMBOL_TYPE, SymbolType_New.DATABASE);
			
			ownerProperties.addProperty("NAME", ownerName);
			ownerProperties.addProperty("CATEGORY_TYPE", "OWNER_DB");
			ownerProperties.addProperty(SYMBOL_TYPE, SymbolType_New.USERDB);
			
			objectProperties.addProperty("NAME", objectName);
			objectProperties.addProperty("CATEGORY_TYPE", categoryType);
			objectProperties.addProperty(SYMBOL_TYPE, symType);
			
			propertiesByName.put(serverName, serverProperties);
			propertiesByName.put(dbName, dbProperties);
			propertiesByName.put(ownerName, ownerProperties);
			propertiesByName.put(objectName, objectProperties);
			
			setProperties.addProperty("PROPERTIES_BY_NAME", propertiesByName);
		
		properties.addProperty(NAME, qualifiedName);
		properties.addProperty("PROPERTY_LIST", setProperties);
		return properties;
	}
	
	public PropertyList setUseScope() {
		Map<String,PropertyList> propertiesByName = new HashMap<String,PropertyList>();
		
		PropertyList setProperties = databaseProperties();
		
		String serverName = "SERVER_DEFAULT";
		String dbName = name;
		String ownerName = "DB_OWNER_DEFAULT";
		
		String name = serverName + "." + dbName + "." + dbName;
		
		PropertyList serverProperties = databaseProperties();
		PropertyList dbProperties = databaseProperties();
		PropertyList ownerProperties = databaseProperties();
		
		serverProperties.addProperty("NAME", serverName);
		serverProperties.addProperty("CATEGORY_TYPE", "SERVER");
		serverProperties.addProperty(SYMBOL_TYPE, SymbolType_New.SERVER);
		
		dbProperties.addProperty("NAME", dbName);
		dbProperties.addProperty("CATEGORY_TYPE", "DATABASE");
		dbProperties.addProperty(SYMBOL_TYPE, SymbolType_New.DATABASE);
		
		ownerProperties.addProperty("NAME", ownerName);
		ownerProperties.addProperty("CATEGORY_TYPE", "OWNER_DB");
		ownerProperties.addProperty(SYMBOL_TYPE, SymbolType_New.USERDB);
		
		propertiesByName.put(serverName, serverProperties);
		propertiesByName.put(dbName, dbProperties);
		propertiesByName.put(ownerName, ownerProperties);
		
		setProperties.addProperty("PROPERTIES_BY_NAME", propertiesByName);
		setProperties.addProperty("ENCLOSING_SCOPE", scope);
		setProperties.addProperty("ENCLOSING_SCOPE_NAME", scope.getName());

		setProperties.addProperty(NAME, name);
		
		return setProperties;
	}
	
	
/*	private String getQualifiedName() {
		final int NUMPARTS = name.split("\\.").length;
		switch(NUMPARTS) {
			case 1: return simpleName();     // object
			case 2: return twoPartName();    // owner.object or tempdb.object
			case 3: return threePartName();  // database.owner.object or database..object
			case 4: return fourPartName();   // server.database.owner.object or missing database or owner
			default:
				try {
					throw new Exception();
				}
				catch(Exception e){
					BicamSystem.printLog(st, context, "ERROR", "Invalid sql name", e);
				}
		}
		return null;
	}
	
	private String simpleName() {
		if(name.startsWith("#")) {
			return "SERVER_TEMPDB" + "." + "TEMPDB" + "." + "DB_OWNER_DEFAULT" + "." + name;
		}
		return "SERVER_DEFAULT" + "." + "DATABASE_DEFAULT" + "." + "OWNER_DEFAULT" + "." + name;
	}
	
	private String twoPartName() {
		if(name.split("\\.")[0].equalsIgnoreCase("TEMPDB")) {
			return "SERVER_TEMPDB" + "." + "TEMPDB" + "." + "DB_OWNER_DEFAULT" + "." + name;
		}
		return "SERVER_DEFAULT" + "." + "DATABASE_DEFAULT" + "." + name.split("\\.")[0] + "." + name.split("\\.")[1];
	}
	
	private String threePartName() {
		if(name.split("\\.")[1].length() == 0) { // db..objet
			return "SERVER_DEFAULT" + "." + name.split("\\.")[0] + "." + "DB_OWNER_DEFAULT" + "." + name.split("\\.")[2];
		}
		else { // db.ownerdb.object
			return "SERVER_DEFAULT" + "." + name.split("\\.")[0] + "." + name.split("\\.")[1] + "." + name.split("\\.")[2];
		}
	}	
	
	private String fourPartName() { 
		if(name.split("\\.")[1].length() == 0 && name.split("\\.")[2].length() > 0) { // server..owner.object
			return name.split("\\.")[0] + "." + "DATABASE_DEFAULT" + "." + name.split("\\.")[2] + "." + name.split("\\.")[3];
		}
		else if(name.split("\\.")[2].length() == 0 && name.split("\\.")[1].length() > 0) { // server.database..object
			return name.split("\\.")[0] + "." + name.split("\\.")[1] + "." + "DB_OWNER_DEFAULT" + "." + name.split("\\.")[3];
		}
		else if(name.split("\\.")[1].length() == 0 && name.split("\\.")[2].length() == 0) { // server...object
			return name.split("\\.")[0] + "." + "DATABASE_DEFAULT" + "." + "DB_OWNER_DEFAULT" + "." + name.split("\\.")[3];
		}
		return name.split("\\.")[0] + "." + name.split("\\.")[1] + "." + name.split("\\.")[2] + "." + name.split("\\.")[3];
	}*/
	
	private void setCategoryType() { // table, column or stored procedure
		if(NodeExplorer_New.hasAncestorClass(context, "ProcedureCallContext"))	{
			categoryType = "STORED_PROCEDURE";
			symType = SymbolType_New.STORED_PROCEDURE;
		}
		else if(NodeExplorer_New.getNextAncestorClass(context, "CreateProcedureStmtContext") != null) {
			categoryType = "STORED_PROCEDURE";
			symType = SymbolType_New.STORED_PROCEDURE;
		}		
		else if(NodeExplorer_New.getAncestorClass(context, "SqlTableReferenceContext",true) != null) {
			categoryType = "TABLE";
			symType = SymbolType_New.TABLE;
		}
		else if(NodeExplorer_New.hasAncestorWithLabel(context, "ColumnRef")) {
			categoryType =  "COLUMN";
			symType = SymbolType_New.COLUMN;
		}
		else if(NodeExplorer_New.hasAncestorClass(context, "ConditionalExprContext")) {
			categoryType =  "COLUMN";
			symType = SymbolType_New.COLUMN;
		}
		else if(NodeExplorer_New.hasAncestorClass(context, "SqlInsertStmtContext")) {
			categoryType =  "COLUMN";
			symType = SymbolType_New.COLUMN;
		}	
		else if(NodeExplorer_New.hasAncestorClass(context, "SqlUpdateStmtContext")) {
			categoryType =  "COLUMN";
			symType = SymbolType_New.COLUMN;
		}		
		else try {
			throw new Exception();
		}
		catch(Exception e){
			BicamSystem.printLog(st, context, "ERROR", "Invalid sql object: not table nor procedure nor column", e);
			categoryType = null;
			symType = null;
		}
	}
	
	private boolean isSymbolToResolveDatabase(ParserRuleContext _ctx) {
		String name = _ctx.getText();
		if(name.startsWith("@")) return false; // é variável local ou de sistemas
        if(NodeExplorer_New.hasSibling(_ctx, "RealParametersContext")) return false;
        if(NodeExplorer_New.getAncestorClass(_ctx, "SqlFetchStmtContext") != null ) return false;
        if(NodeExplorer_New.getAncestorClass(_ctx, "SqlStmtContext") != null ) return true;
        if(NodeExplorer_New.getAncestorClass(_ctx, "SqlSubSelectStmtContext") != null) return true;
        if(NodeExplorer_New.getAncestorClass(_ctx, "ProcedureCallContext") != null) return true;
        if(NodeExplorer_New.getNextAncestorClass(_ctx, "CreateProcedureStmtContext") != null) return true;
        if(NodeExplorer_New.getAncestorClass(_ctx, "CreateIndexStmtContext") != null) return false;
        if(NodeExplorer_New.getAncestorClass(_ctx, "SqlCreateTableStmtContext") != null) return true;
        
        // Colunas orderBy tem que estar em resultSet, então não referencio
        
        
        return false;
	}
	
	public PropertyList databaseProperties() {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, context);
		properties.addProperty(DEF_MODE, "INFER");
		properties.addProperty("CATEGORY", "DATABASE");
		return properties;
	}	
}