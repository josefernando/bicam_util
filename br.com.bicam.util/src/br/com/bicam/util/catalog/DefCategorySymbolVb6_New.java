package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.GLOBAL_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class DefCategorySymbolVb6_New extends VisualBasic6BaseListener{
	String absoluteFileName; // nome do diretório do projeto VB6
	
	SymbolTable_New     st;
	Deque<IScope_New>   scopes;
	Deque<IScope_New>   withStmtScope;
	IScope_New 		    globalScope;

	SymbolFactory       symbolFactory;
	PropertyList 		properties;
	Parser_New          parser;
	Symbol_New 			stubObjeto = null;  // apenas para efeito de testes de criação objetos
	
	String             optionExplicit;
	
	KeywordLanguage keywordLanguage;
	
	PropertyList currentProperties;
	
	final String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$"};
	
	public DefCategorySymbolVb6_New(Parser_New _parser, PropertyList _propertyList){
		this.scopes          = new ArrayDeque<IScope_New>();
		this.withStmtScope   = new ArrayDeque<IScope_New>();
		this.properties      = _propertyList;
		this.parser          = _parser;
		this.st              = (SymbolTable_New)properties.getProperty(SYMBOLTABLE);
		this.symbolFactory   = (SymbolFactory)properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage)properties.getProperty(KEYWORD_LANGUAGE);
		this.globalScope     = (IScope_New)properties.getProperty(GLOBAL_SCOPE);
	}
	
	public SymbolTable_New getSymbolTable(){
		return st;
	}
	
	@Override
	public void enterImplicitCallStmt(@NotNull VisualBasic6Parser.ImplicitCallStmtContext ctx) {
		ParserRuleContext context = (ParserRuleContext) NodeExplorer_New.getFirstChildClass(ctx, "ExprContext");
		IdentifierContext ctxIdentifier = (IdentifierContext) NodeExplorer_New.getChildClass(context, "IdentifierContext");
		Symbol_New sym = st.getSymbol(ctxIdentifier);
		
		if(sym.hasProperty("CATEGORY_TYPE", "MEMBER")) {
			sym.addProperty("CATEGORY_TYPE", "METHOD");
		}
		if(!sym.hasProperty("CATEGORY", "FUNCTION")){
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: IDENTIFIER OF IMPLICIT MEMBER NOT IS FUNCTION '%s' in COMPILATION UNIT '%s' in line %d%n"
						                ,ctxIdentifier.getText(), st.getCompilarionUnitSymbol(ctx).getName()
						                , ctxIdentifier.start.getLine());
				e.printStackTrace();
			}			
		}
	}
	
	@Override
	public void enterRealParameters(@NotNull VisualBasic6Parser.RealParametersContext ctx) {
		IdentifierContext ctxIdentifier = (IdentifierContext) NodeExplorer_New.getFirstSibling(ctx, "IdentifierContext");
		Symbol_New sym = st.getSymbol(ctxIdentifier);
		
		if(sym.hasProperty("CATEGORY_TYPE", "MEMBER")) {
			sym.addProperty("CATEGORY_TYPE", "METHOD");
		}
		if(!sym.hasProperty("CATEGORY", "FUNCTION")){
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: IDENTIFIER OF IMPLICIT MEMBER NOT IS FUNCTION '%s' in COMPILATION UNIT '%s' in line %d%n"
						                ,ctxIdentifier.getText(), st.getCompilarionUnitSymbol(ctx).getName()
						                , ctxIdentifier.start.getLine());
				e.printStackTrace();
			}			
		}
	}
	
	@Override
	public void enterAssignmentStmt(@NotNull VisualBasic6Parser.AssignmentStmtContext ctx) {
		IdentifierContext ctxIdentifier = (IdentifierContext) NodeExplorer_New.getFirstChildClass(ctx, "IdentifierContext");
		Symbol_New sym = st.getSymbol(ctxIdentifier);
		
		if(sym.getProperty("LSH") == null) {
			sym.addProperty("LSH", new ArrayList<ParserRuleContext>());
		}
		((ArrayList)sym.getProperty("LSH")).add(ctxIdentifier);
	}	
	
	@Override	
	public void enterRealParametersNoParen(@NotNull VisualBasic6Parser.RealParametersNoParenContext ctx) {
		IdentifierContext ctxIdentifier = (IdentifierContext) NodeExplorer_New.getFirstSibling(ctx, "IdentifierContext");
		Symbol_New sym = st.getSymbol(ctxIdentifier);
		
		if(sym.hasProperty("CATEGORY_TYPE", "MEMBER")) {
			sym.addProperty("CATEGORY_TYPE", "METHOD");
		}
		if(!sym.hasProperty("CATEGORY", "FUNCTION")){
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.format("*** ERROR: IDENTIFIER OF IMPLICIT MEMBER NOT IS FUNCTION '%s' in COMPILATION UNIT '%s' in line %d%n"
						                ,ctxIdentifier.getText(), st.getCompilarionUnitSymbol(ctx).getName()
						                , ctxIdentifier.start.getLine());
				e.printStackTrace();
			}			
		}
	}	
}