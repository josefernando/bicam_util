package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY_TYPE;
import static br.com.bicam.util.constant.PropertyName.COLUMN;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_NAME;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.FILE_NAME;
import static br.com.bicam.util.constant.PropertyName.FORMAL_PARAMETER;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.LOCAL;
import static br.com.bicam.util.constant.PropertyName.MODULE_NAME;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SCOPE;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_TYPE;
import static br.com.bicam.util.constant.PropertyName.VARIABLE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.VariableStmtContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.StringOptionalCase;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.Member;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class CompilationUnitDefSymbolSqlTransactSybase extends SqlTransactSybaseBaseListener {
	String absoluteFileName; 

	String moduleName;
	String dbName;
	
	ParserRuleContext startRuleContext;

	SymbolTable_New st;
	Deque<IScope_New> scopes;
	IScope_New globalScope;
	IScope_New compilationUnitScope;

	SymbolFactory symbolFactory;
	Object compUnit;
	PropertyList properties;
	CompilationUnit compilationUnit;

	KeywordLanguage keywordLanguage;

	PropertyList currentProperties;

	Set<String> controlArrays;
	Map<Integer, String> controlArrayMap;
	Set<String> controlArrayAddedInScope;
	Map<String, Integer> occurs;
	Map<String,Symbol_New> uicontrolVisited;

	final String[] sulfixTypeIndicator = new String[] { "&", "%", "#", "!", "@", "$" };

	public CompilationUnitDefSymbolSqlTransactSybase(PropertyList _propertyList) {
		this.scopes = new ArrayDeque<IScope_New>();
		this.properties = _propertyList;
		this.compilationUnit = (CompilationUnit) properties.getProperty("COMPILATION_UNIT");
		this.st = (SymbolTable_New) properties.getProperty(SYMBOLTABLE);
		this.symbolFactory = (SymbolFactory) properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);
		this.globalScope = st.getGlobalScope();//(IScope_New) properties.getProperty(GLOBAL_SCOPE);
		setCurrentScope(globalScope);
	}

	public SymbolTable_New getSymbolTable() {
		return st;
	}

	private String getProcedureName(File _file) {
		String procName = null;
		try {
			String fileName = _file.getCanonicalPath();
			//=============== TUTORIAL  REGEX ==========================
			// "CREATE PROC PROCXXXX", identifica PROCXXXX como procName
			// ^ come�o de palavra
			// \\s* 0 mais de 0 espa�os
			// (?i) pr�xima palavra caseinsensitive
			// \\b  match palavra
			// ?: n�o inclui em group matched
			// | alternative
			// \\w match letra a-z,A-Z,_
			// \\d match n�mero 0-9
			// .* match 0 ou mais de 0 qualquer caracter
			String REGEX = "^\\s*(?i)CREATE\\s+\\b(?:(?i)PROCEDURE|(?i)PROC)\\b\\s+([\\d\\w]+).*";
			// _file =>
			// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
			Pattern p = Pattern.compile(REGEX);
			BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				Matcher m = p.matcher(line);
				if ((m.matches())) {
					return procName = m.group(1);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return procName;
	}	
	
	@Override 
	public void enterStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		String fileName = compilationUnit.getFileName();
		String compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];

		String name = compilationUnitName.split("\\.")[0]; // remove exten��o

		// defining compilation unit scope
		PropertyList compUnitProperties = defaultProperties(); // new PropertyList();
		compUnitProperties.addProperty(ENCLOSING_SCOPE, st.getGlobalScope()); // publicScope);
		compUnitProperties.addProperty(ENCLOSING_SCOPE_NAME, st.getGlobalScope().getName()); // publicScope.getName());

		compUnitProperties.addProperty(NAME, "COMP_UNIT_" + name + "_SCOPE");
		
		compUnitProperties.addProperty(MODULE_NAME, getProcedureName(compilationUnit.getFile()));
		compUnitProperties.addProperty(FILE_NAME, fileName);
		compUnitProperties.addProperty("COMPILATION_UNIT", compilationUnit);
		compUnitProperties.addProperty("COMPILATION_UNIT_NAME", compilationUnitName);
		compUnitProperties.addProperty(CATEGORY_TYPE, "FILE");
		compUnitProperties.addProperty(CATEGORY, "COMPILATION_UNIT");
		compUnitProperties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

		compUnitProperties.addProperty("DEF_MODE", "EXPLICIT");
		compUnitProperties.addProperty(SYMBOL_TYPE, SymbolType_New.COMPILATION_UNIT);
		compilationUnitScope = (IScope_New) symbolFactory.getSymbol(compUnitProperties);

		st.setSymbol(ctx, (Symbol_New) compilationUnitScope);
		setCurrentScope(compilationUnitScope);
	}
	
	public void exitStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		removeCurrentScope();
	}
	
/*	@Override 
	public void enterUseStmt(@NotNull SqlTransactSybaseParser.UseStmtContext ctx) {
		String dbName=ctx.Name.getText();
		//CATEGORY=DATABASE
		//CATEGORY_TYPE=DATABASE
		//NAME=dbName
		
		PropertyList serverProperties = defaultProperties();
		serverProperties.addProperty("NAME", "SERVER_DEFAULT");
		serverProperties.addProperty("CONTEXT", ctx);

		Symbol_New serverSym = getCurrentScope().resolve(serverProperties);
		
		String fullDbName = keywordLanguage.getParameter("SERVER_DEFAULT") + "." + dbName;
		
		PropertyList properties = defaultProperties();
		properties.addProperty("NAME", fullDbName);
		properties.addProperty("CONTEXT", ctx);
		
		
		PropertyList resolveProp = new PropertyList();
		resolveProp.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		resolveProp.addProperty(SYMBOLTABLE, st);
		resolveProp.addProperty(NAME, dbName);
		resolveProp.addProperty(ENCLOSING_SCOPE, serverSym);
		resolveProp.addProperty(ENCLOSING_SCOPE_NAME, serverSym.getName());
		resolveProp.addProperty(SYMBOL_FACTORY, symbolFactory);
		resolveProp.addProperty("CONTEXT", ctx);
		resolveProp.addProperty(DEF_MODE, "INFER");
		resolveProp.addProperty(CATEGORY, "DATABASE");
		resolveProp.addProperty(CATEGORY_TYPE, "DATABASE");
		resolveProp.addProperty("SYMBOL_TYPE", SymbolType_New.DATABASE);
		resolveProp.addProperty("DATA_TYPE_NAME", "UNDEFINED");
		
		properties.addProperty("PROPERTY_LIST", resolveProp);
		
		Symbol_New dbSym = getCurrentScope().resolve(properties);

		setCurrentScope(dbSym);
		createOwnerDatabaseDefautl(dbSym);
	}*/
	
	@Override 
	public void enterUseStmt(@NotNull SqlTransactSybaseParser.UseStmtContext ctx) {
		dbName=ctx.Name.getText();
		//CATEGORY=DATABASE
		//CATEGORY_TYPE=DATABASE
		//NAME=dbName
		PropertyList properties = new PropertyList();
		
		properties.addProperty("SYMBOLTABLE",st);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("NAME",dbName);
		properties.addProperty("SYMBOL_FACTORY",symbolFactory);
		properties.addProperty("KEYWORD_LANGUAGE",keywordLanguage);
		properties.addProperty("SCOPE",getCurrentScope());

		SetDatabasePropertiesSqlTransactSybaseVisitor use = 
				new SetDatabasePropertiesSqlTransactSybaseVisitor(properties);
		
		Symbol_New ownerDbSym = getCurrentScope().resolve(use.setUseScope());
		
		setCurrentScope(ownerDbSym);
	}	
	
/*	@Override 
	public void enterCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
		st.setScope(ctx, getCurrentScope()); // marca scope em nome AST

		String name = ctx.Name.getText();
		PropertyList properties = defaultProperties();

		properties.addProperty(NAME, name);
		properties.addProperty(CATEGORY, PROCEDURE);
		properties.addProperty(CATEGORY_TYPE, "STORED_PROCEDURE");

		properties.addProperty(SYMBOL_TYPE, SymbolType_New.STORED_PROCEDURE);
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());

		properties.addProperty(RETURN_TYPE_NAME, "Void");

		Symbol_New sym = symbolFactory.getSymbol(properties);

		setCurrentScope(sym); // marca escopo antes da defini��o dos par�meros formais (real parameters)

		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);		
	}*/
	
	@Override 
	public void enterCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
		st.setScope(ctx, getCurrentScope()); // marca scope em nome AST
	
		String name = ctx.Name.getText();
		
		String DATABASE_DEFAULT = "DBNPROD";
		
		if(!dbName.equalsIgnoreCase(DATABASE_DEFAULT)) {
			name = getCurrentScope().getName() + ".." + ctx.Name.getText();
		}
		
		PropertyList properties = defaultProperties();
		properties.addProperty("NAME", name);
	
		PropertyList dbPproperties = new PropertyList();
		dbPproperties.addProperty("SYMBOLTABLE",st);
		dbPproperties.addProperty("CONTEXT",ctx.Name);
		dbPproperties.addProperty("NAME", name);
		dbPproperties.addProperty("SYMBOL_FACTORY",symbolFactory);
		dbPproperties.addProperty("KEYWORD_LANGUAGE",keywordLanguage);	
		dbPproperties.addProperty("SCOPE",getCurrentScope());
		
		SetDatabasePropertiesSqlTransactSybaseVisitor setDatabaseProp = 
				new SetDatabasePropertiesSqlTransactSybaseVisitor(dbPproperties);
		
		PropertyList dbProperties = setDatabaseProp.run();
		if(dbProperties != null) properties = dbProperties;
		
		Symbol_New sym = getCurrentScope().resolve(properties);
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
		setCurrentScope(sym); // marca escopo antes da defini��o dos par�meros formais (real parameters)

		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
	}
	
	@Override 
	public void exitCreateProcedureStmt(@NotNull SqlTransactSybaseParser.CreateProcedureStmtContext ctx) {
//		removeCurrentScope();
	}
	
/*	@Override 
	public void enterVariableStmt(@NotNull SqlTransactSybaseParser.VariableStmtContext ctx) {
		st.setScope(ctx, getCurrentScope());
		createVarSymbol(ctx);		
	}*/
	
	
	@Override 
	public void enterVariableStmt(@NotNull SqlTransactSybaseParser.VariableStmtContext ctx) {
		st.setScope(ctx, getCurrentScope());
		createVarSymbol(ctx);		
	}
	
	@Override 
	public void enterFormalParameters(@NotNull SqlTransactSybaseParser.FormalParametersContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}
	
/*	@Override 
	public void enterCreateIndexStmt(@NotNull SqlTransactSybaseParser.CreateIndexStmtContext ctx) {
        st.setScope(ctx, getCurrentScope());
        String name = null;
        if(ctx.Name != null) {
        	name = ctx.Name.getText();
        }
		
		PropertyList properties = defaultProperties();
		properties.addProperty(NAME, name);
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.INDEX);
		properties.addProperty(CATEGORY, "DATABASE");
		properties.addProperty(CATEGORY_TYPE, "INDEX");

		Symbol_New sym = symbolFactory.getSymbol(properties);
	}*/

	
	/*
     * Escopo em comando sql utilizado para enclosing alias de nome de tabela
	 */
	
	@Override 
	public void enterSqlSubSelectStmt(@NotNull SqlTransactSybaseParser.SqlSubSelectStmtContext ctx) {
		createSqlScope(ctx);
	}
	
	@Override 
	public void exitSqlSubSelectStmt(@NotNull SqlTransactSybaseParser.SqlSubSelectStmtContext ctx) {
		removeCurrentScope();
	}
	
	@Override 
	public void enterSqlInsertStmt(@NotNull SqlTransactSybaseParser.SqlInsertStmtContext ctx) {
		createSqlScope(ctx);
	}
	
	@Override 
	public void exitSqlInsertStmt(@NotNull SqlTransactSybaseParser.SqlInsertStmtContext ctx) {
		removeCurrentScope();
	}	
	
	@Override 
	public void enterSqlDeleteStmt(@NotNull SqlTransactSybaseParser.SqlDeleteStmtContext ctx) {
		createSqlScope(ctx);
	}
	
	@Override 
	public void exitSqlDeleteStmt(@NotNull SqlTransactSybaseParser.SqlDeleteStmtContext ctx) {
		removeCurrentScope();
	}
	
	@Override 
	public void enterSqlUpdateStmt(@NotNull SqlTransactSybaseParser.SqlUpdateStmtContext ctx) {
		createSqlScope(ctx);
	}
	
	@Override 
	public void exitSqlUpdateStmt(@NotNull SqlTransactSybaseParser.SqlUpdateStmtContext ctx) {
		removeCurrentScope();
	}

	@Override 
	public void enterBeginTranStmt(@NotNull SqlTransactSybaseParser.BeginTranStmtContext ctx) {
        st.setScope(ctx, getCurrentScope());
        String name = null;
        if(ctx.Name != null) {
        	name = ctx.Name.getText();
        }
        else {
        	name = "UNAMED_" + ctx.start.getStartIndex() + "_" + ctx.start.getLine();
        }
		
		PropertyList properties = defaultProperties();
		properties.addProperty(NAME, name);
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.TRANSACTION);
		properties.addProperty(CATEGORY, "TRANSACTION");
		properties.addProperty(CATEGORY_TYPE, "TRANSACTION");

		Symbol_New sym = symbolFactory.getSymbol(properties);
	}
	
/*	@Override 
	public void enterSqlCreateTableStmt(@NotNull SqlTransactSybaseParser.SqlCreateTableStmtContext ctx) {
		PropertyList properties = defaultProperties();
		
		String name = ctx.Name.getText();
		
		PropertyList dbProperties = setDatabaseProperties(ctx, name);
		
		if(dbProperties != null) {
			properties.addProperty(NAME, dbProperties.getProperty("NAME"));
			properties.addProperty("PROPERTY_LIST", dbProperties);
		}
		
		Symbol_New sym = getCurrentScope().resolve(properties);
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
		st.setSymbol(ctx, sym);		
		setCurrentScope(sym);
	}*/
	
/*	public void exitSqlCreateTableStmt(@NotNull SqlTransactSybaseParser.SqlCreateTableStmtContext ctx) {
		removeCurrentScope();
	}*/
	
/*	@Override 
	public void enterSqlTableReference(@NotNull SqlTransactSybaseParser.SqlTableReferenceContext ctx){ 
		
		SqlSingleTableContext sigleTableContext = (SqlSingleTableContext) NodeExplorer.getFirstChildClass(ctx, "SqlSingleTableContext");
		
		String name = sigleTableContext.getText();
		
		PropertyList dbProperties = setDatabaseProperties(ctx, name);

		dbProperties.addProperty("PROPERTY_LIST", dbProperties);
		
		Symbol_New sym = getCurrentScope().resolve(dbProperties);
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
		
		if(NodeExplorer.getNextFirstChildClass(ctx, "IdentifierContext") != null) {
			IdentifierContext correlationCtx = (IdentifierContext) NodeExplorer.getNextFirstChildClass(ctx, "IdentifierContext");
			String correlationName = correlationCtx.start.getText();
			PropertyList aliasProperties =  defaultProperties();
			
			aliasProperties.addProperty("NAME", correlationName);
			aliasProperties.addProperty("CONTEXT", correlationCtx);
			aliasProperties.addProperty("ALIAS", sym);
			aliasProperties.addProperty(SYMBOL_FACTORY, symbolFactory);
			aliasProperties.addProperty(SYMBOL_TYPE, SymbolType_New.ALIAS);
			aliasProperties.addProperty("CATEGORY", "SQL");
			aliasProperties.addProperty("CATEGORY_TYPE", "ALIAS");
			aliasProperties.addProperty(ENCLOSING_SCOPE, getCurrentScope()); // LOCAL_SubsectectStmt
			aliasProperties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
			Symbol_New symAlias = symbolFactory.getSymbol(aliasProperties);
		}
		st.setSymbol(ctx, sym);
	}*/
	
	@Override 
	public void exitFormalParameters(@NotNull SqlTransactSybaseParser.FormalParametersContext ctx) {
		Symbol_New symEnclosing = (Symbol_New) getCurrentScope();
		List<String> orderedFormalParameters = new ArrayList<String>();

		for (Entry<StringOptionalCase, Member> entry : symEnclosing.getMembers().entrySet()) {
			entry.getValue().findSymbol();
			while (entry.getValue().hasNext()) {
				Symbol_New symFp = entry.getValue().getNextSymbol();
				if (symFp.hasProperty("CATEGORY_TYPE", "FORMAL_PARAMETER")) {
					orderedFormalParameters.add((String) symFp.getProperty("DATA_TYPE_NAME"));
				}
			}
		}
		symEnclosing.addProperty("ORDERED_ARGS", orderedFormalParameters);

		PropertyList properties = defaultProperties();
		properties.addProperty(NAME, "LOCAL_" + getCurrentScope().getName());
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.LOCAL_SCOPE);
		properties.addProperty(CATEGORY, SCOPE);
		properties.addProperty(CATEGORY_TYPE, LOCAL);

		Symbol_New sym = symbolFactory.getSymbol(properties);
		setCurrentScope(sym);
	}
	


	private void setCurrentScope(IScope_New _scope) {
		scopes.push(_scope);
	}

	private IScope_New getCurrentScope() {
		return scopes.peek();
	}

	private void removeCurrentScope() {
		scopes.pop();
	}

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
	
	@Override 
	public void enterGoToStmt(@NotNull SqlTransactSybaseParser.GoToStmtContext ctx) { 
		st.setScope(ctx, getCurrentScope());
	}

	@Override public void enterLabel(@NotNull SqlTransactSybaseParser.LabelContext ctx) {
		st.setScope(ctx, getCurrentScope());
		
		PropertyList properties = defaultProperties();
		String name = ctx.start.getText();
		name = name.replace(":", "");
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.LABEL);
		properties.addProperty(CATEGORY_TYPE, "LABEL_NAME");
		properties.addProperty(CATEGORY, "LABEL");
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");
		properties.addProperty(CONTEXT, ctx);

		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(ctx, sym);
	}

	@Override 
	public void enterType(@NotNull SqlTransactSybaseParser.TypeContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}
	
	@Override
	public void enterSqlSingleTable(@NotNull SqlTransactSybaseParser.SqlSingleTableContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}
	
	@Override 
	public void enterSqlDeclareCursorStmt(@NotNull SqlTransactSybaseParser.SqlDeclareCursorStmtContext ctx) {
		st.setScope(ctx, getCurrentScope());
		
		PropertyList properties = defaultProperties();
		String name = ctx.Name.getText();
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.CURSOR);
		properties.addProperty(CATEGORY_TYPE, "CURSOR");
		properties.addProperty(CATEGORY, "SQL");
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");
		properties.addProperty(CONTEXT, ctx);

		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
	}

	@Override 
	public void enterIdentifier(@NotNull SqlTransactSybaseParser.IdentifierContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}
	
	@Override 
	public void enterSqlTableReference(@NotNull SqlTransactSybaseParser.SqlTableReferenceContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}
	
	private void createVarSymbol(VariableStmtContext varCtx) {
		if(st.getSymbol(varCtx) != null) return;
		if(NodeExplorer_New.hasAncestorClass(varCtx, "ColumnDefinitionContext")) return;
		PropertyList properties = defaultProperties();

		properties.addProperty(NAME, varCtx.Name.getText());
		
		String categoryType = getVarCategory(varCtx);
		if(categoryType.equalsIgnoreCase("COLUMN")) {
			properties.addProperty(SYMBOL_TYPE, SymbolType_New.COLUMN);
			properties.addProperty(CATEGORY, SymbolType_New.DATABASE.toString());
		}
		else {
			properties.addProperty(SYMBOL_TYPE, SymbolType_New.VARIABLE);
			properties.addProperty(CATEGORY, SymbolType_New.VARIABLE.toString());	
		}
		
		properties.addProperty(CATEGORY_TYPE, categoryType);

		if (varCtx.initialValue() != null) {
			properties.addProperty("INITIAL_VALUE", varCtx.initialValue().getText().replace("=", ""));
		}
		properties.addProperty(DATA_TYPE_NAME, varCtx.type().getText());
		properties.addProperty(DATA_TYPE_CONTEXT, varCtx.type());

		if(varCtx.datatypeLength() != null) {
			properties.addProperty("LENGHT", varCtx.datatypeLength().Lenght.getText());
		}

		if(varCtx.outputClause() != null) {
			properties.addProperty("OUTPUT", varCtx.outputClause().getText());
		}		
		
		properties.addProperty(CONTEXT, varCtx.Name); // ? N�o sei para que serve
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());

//		properties.addProperty(SYMBOL_TYPE, SymbolType_New.VARIABLE);
//		properties.addProperty(CATEGORY, SymbolType_New.VARIABLE.toString());

		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(varCtx, sym);
		st.setSymbol(varCtx.Name, sym);
	}

	private String getVarCategory(ParserRuleContext varCtx) {
//		if (NodeExplorer.getAncestorClass(varCtx, "ConstantDefinitionContext") != null) {
//			return CONSTANT;
//		}
		if (NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext") != null) {
			return VARIABLE;
		}
		if (NodeExplorer.getAncestorClass(varCtx, "FormalParameterContext") != null) {
			return FORMAL_PARAMETER;
		} 
		if (NodeExplorer.getAncestorClass(varCtx, "ColumnDefinitionContext") != null) {
			return COLUMN;
		}
		else {
			try {
				throw new Exception();
			} catch (Exception e) {
				System.err.format(
						"*** WARNING: VARIABLE CATEGORY not found for variable '%s' in line %d in compilation unit '%s'%n",
						defaultProperties().getProperty(NAME), varCtx.start.getLine(), compilationUnit.getFileName());
				e.printStackTrace();
			}
			return "UNDEFINED";
		}
	}
	
	/*
	 * Utilizado como escopo de alias table
	 */

	private void createSqlScope(ParserRuleContext _ctx) {
		PropertyList properties = defaultProperties();
		properties.addProperty(NAME, "SQL_SCOPE_" + _ctx.start.getStartIndex() );
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.LOCAL_SCOPE);
		properties.addProperty(CATEGORY, SCOPE);
		properties.addProperty(CATEGORY_TYPE, LOCAL);

		Symbol_New sym = symbolFactory.getSymbol(properties);
		setCurrentScope(sym);		
	}	
	
	private PropertyList databaseProperties(ParserRuleContext _ctx) {
		
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(DEF_MODE, "INFER");
		properties.addProperty("CATEGORY", "DATABASE");
		properties.addProperty("PROPERTIES_BY_NAME", new HashMap<String,PropertyList>());
		return properties;
	}

	private PropertyList defaultProperties() {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		return properties;
	}
	
/*	private PropertyList setDatabaseProperties(ParserRuleContext _ctx, String _name) {
		if(_name.startsWith("@")) return null; // � vari�vel local ou de sistemas
		
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
		else if (NodeExplorer_New.getAncestorClassStartsWith(_ctx, "Sql") != null) {
			if(_name.startsWith("@")) return properties; // vari�vel local ou de sistema(@@)
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
}