package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY_TYPE;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_TYPE;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser.SqlTableReferenceContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class CompilationUnitRefTypeSymbolSqlTransactSybase extends SqlTransactSybaseBaseListener {
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

	public CompilationUnitRefTypeSymbolSqlTransactSybase(PropertyList _propertyList) {
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

	@Override
	public void enterType(@NotNull SqlTransactSybaseParser.TypeContext ctx) {
		if(!NodeExplorer.hasAncestorClass(ctx, "VariableStmtContext")) return;
		if(NodeExplorer.hasAncestorClass(ctx, "ColumnDefinitionContext")) return;

		
		IScope_New scope = st.getScope(ctx);
		String dataTypeName = ctx.Name.getText();

		ParserRuleContext ctxSym = NodeExplorer.getAncestorClass(ctx, "VariableStmtContext");

		if(ctxSym == null) {
			try {
				throw new Exception();
			} catch (Exception e) {
				System.err.format("*** ERROR: SYMBOL not set in context 'VariableStmtContext' in COMPILATION UNIT '%s' in line %d - datatype '%s' NOT RESOLVED.%n"
						,st.getCompilarionUnitSymbol(ctx).getName()
		                , ctx.start.getLine(), dataTypeName);
				System.err.format("=> TREE: %s%n%n", NodeExplorer_New.getTreeToRootClass(ctx));
                e.printStackTrace();
			}
			return;
		}
		
		Symbol_New sym = st.getSymbol(ctxSym); // Símbolo da definição da variável

		PropertyList prop = defaultProperties(ctx);
		prop.addProperty(NAME, dataTypeName);
		
		Symbol_New dataTypeSym = scope.resolve(prop);

		if(dataTypeSym != null) {
			System.err.format("*** INF0: NAME '%s' resolve to '%s'%n", dataTypeName, dataTypeSym.getName());
		}
		else {
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: NOT RESOLVED NAME '%s' in COMPILATION UNIT '%s' in line %d%n"
						                ,dataTypeName, st.getCompilarionUnitSymbol(ctx).getName()
						                , ctx.start.getLine());
				e.printStackTrace();
			}
		}
		
		if(sym == null) {
		try {
			throw new Exception();
		}catch (Exception e) {
			System.err.format("*** ERROR: SYMBOL NOT MARKED AT COMMAND  in COMPILATION UNIT '%s' in line %d%n"
					                , st.getCompilarionUnitSymbol(ctx).getName()
					                , ctx.start.getLine());
			e.printStackTrace();
			return;
		}
		}
		sym.addProperty(DATA_TYPE, dataTypeSym);		
	}
	
/*	@Override 
	public void enterSqlTableReference(@NotNull SqlTransactSybaseParser.SqlTableReferenceContext ctx) {
		setCurrentScope(st.getScope(ctx));
		String name = ctx.sqlSingleTable().Name.getText();
		String alias = (ctx.Alias == null ? null : ctx.Alias.getText());

		PropertyList properties = new PropertyList();
		properties.addProperty("SYMBOLTABLE",st);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("NAME", name);
		properties.addProperty("SYMBOL_FACTORY",symbolFactory);
		properties.addProperty("KEYWORD_LANGUAGE",keywordLanguage);
		
		SetDatabasePropertiesSqlTransactSybaseVisitor setDatabaseProp = 
				new SetDatabasePropertiesSqlTransactSybaseVisitor(properties);
		
		PropertyList dbProperties = setDatabaseProp.run();
		if(dbProperties == null) return;
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
		
		if(alias != null) createAlias(sym, alias);
		
		st.setSymbol(ctx.sqlSingleTable(),sym);
		st.setSymbol((ParserRuleContext)NodeExplorer_New.getFirstChildClass(ctx.sqlSingleTable(), "IdentifierContext"), sym);
		removeCurrentScope();
	}*/
	
	
	@Override 
	public void enterUseStmt(@NotNull SqlTransactSybaseParser.UseStmtContext ctx) {
		dbName = ctx.Name.getText();
	}
	
	@Override 
	public void enterSqlSingleTable(@NotNull SqlTransactSybaseParser.SqlSingleTableContext ctx) {
		
		IScope_New scope = st.getScope(ctx);
		String name = ctx.Name.getText();
		
		if(!name.contains(".")) {
			if(!dbName.equalsIgnoreCase("DNPROD")) name = dbName + ".." + name;
		}
		
		PropertyList properties = new PropertyList();
		properties.addProperty("SYMBOLTABLE",st);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("NAME", name);
		properties.addProperty("SYMBOL_FACTORY",symbolFactory);
		properties.addProperty("KEYWORD_LANGUAGE",keywordLanguage);
		properties.addProperty("SCOPE",scope);
		
		SetDatabasePropertiesSqlTransactSybaseVisitor setDatabaseProp = 
				new SetDatabasePropertiesSqlTransactSybaseVisitor(properties);
		
		PropertyList dbProperties = setDatabaseProp.run();
		if(dbProperties == null) return;
		Symbol_New sym = scope.resolve(dbProperties);
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
		
		if(NodeExplorer_New.getNextAncestorClass(ctx, "SqlTableReferenceContext") != null) {
			SqlTableReferenceContext refContext = (SqlTableReferenceContext) NodeExplorer_New.getNextAncestorClass(ctx, "SqlTableReferenceContext");
			String alias = (refContext.Alias == null ? null : refContext.Alias.getText());
			if(alias != null) createAlias(sym, alias, scope);
		}
		
		st.setSymbol(ctx,sym);
		st.setSymbol((ParserRuleContext)NodeExplorer_New.getFirstChildClass(ctx, "IdentifierContext"), sym);
	}	
	
	private Symbol_New createAlias(Symbol_New _sym, String _alias, IScope_New _scope) {
		PropertyList properties = defaultProperties(_sym.getContext());

		properties.addProperty(NAME, _alias);
		properties.addProperty(CATEGORY,_sym.getProperty(CATEGORY));
		properties.addProperty(CATEGORY_TYPE, SymbolType_New.ALIAS.toString());

		properties.addProperty(SYMBOL_TYPE, SymbolType_New.ALIAS);
		
		properties.addProperty(CONTEXT, _sym.getContext()); // ? Não sei para que serve
		properties.addProperty(ENCLOSING_SCOPE, _scope);
		properties.addProperty(SymbolType_New.ALIAS.toString(), _sym);
		
		return symbolFactory.getSymbol(properties);	
	}

	private PropertyList defaultProperties(ParserRuleContext _ctx) {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, _ctx);
		return properties;
	}
}