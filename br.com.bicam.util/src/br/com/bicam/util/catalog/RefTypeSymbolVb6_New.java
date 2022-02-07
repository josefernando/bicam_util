package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.ARRAY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY_TYPE;
import static br.com.bicam.util.constant.PropertyName.CONSTANT;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_NAME;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.ENUM_VALUE;
import static br.com.bicam.util.constant.PropertyName.FORMAL_PARAMETER;
import static br.com.bicam.util.constant.PropertyName.GLOBAL_SCOPE;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.PARENT_SCOPE;
import static br.com.bicam.util.constant.PropertyName.PARENT_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.REDIM;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_TYPE;
import static br.com.bicam.util.constant.PropertyName.TYPE;
import static br.com.bicam.util.constant.PropertyName.VARIABLE;
import static br.com.bicam.util.constant.PropertyName.VISIBILITY_SCOPE;
import static br.com.bicam.util.constant.PropertyName.VISIBILITY_SCOPE_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.AccessModifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.DeclareStmtContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.EnumerationDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.EventStmtContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.GuiDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.MethodDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.TypeDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.VariableDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.VariableStmtContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class RefTypeSymbolVb6_New extends VisualBasic6BaseListener{
	String absoluteFileName; // nome do diretório do projeto VB6
	
	SymbolTable_New     st;
	Deque<IScope_New>   scopes;
	IScope_New 		    globalScope;

	SymbolFactory       symbolFactory;
	PropertyList 		properties;
	Parser_New          parser;
	Symbol_New 			stubObjeto = null;  // apenas para efeito de testes de criação objetos
	
	KeywordLanguage keywordLanguage;
	
	PropertyList currentProperties;
	
	final String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$"};
	
	public RefTypeSymbolVb6_New(Parser_New _parser, PropertyList _propertyList){
		this.scopes          = new ArrayDeque<IScope_New>();
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
    
    private boolean isOptionExpicit() {
  	  try {
 		 String	fileName = parser.getFileName();
 		 String REGEX = "^Option\\s+Explicit$";
         Pattern p = Pattern.compile(REGEX);
 		  BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
           for (String line = in.readLine(); line != null; line = in.readLine()) {	 
               Matcher m = p.matcher(line);
         	  if((m.matches())) {
         		  return true;
         	  }
           }
           in.close();
 	  }
 	  catch (IOException e){
 			  e.printStackTrace();
 	  }
       return false;    	
    }
	
	private void setCurrentScope(IScope_New _scope){
		scopes.push(_scope);
	}
	
	private IScope_New getCurrentScope(){
		return scopes.peek();
	}
	
	private void removeCurrentScope(){
		scopes.pop();
	}	
	
	@Override
	public void enterGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx){
		Symbol_New sym = st.getSymbol(ctx);
		IScope_New scope = st.getScope(ctx);
		String dataTypeName = null;
		
		try {

		dataTypeName = (String)((Symbol_New)sym).getProperty(DATA_TYPE_NAME);
		} catch (NullPointerException e) {
			int debug = 0;
			e.printStackTrace();
		}
        
		PropertyList prop = defaultProperties(ctx);
		prop.addProperty(NAME, dataTypeName);
		
		Symbol_New dataTypeSym = scope.resolve(prop);
		
		if(dataTypeSym != null) {
//			System.err.format("*** INF0: NAME '%s' resolve to '%s'%n", dataTypeName, dataTypeSym.getName());
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
		sym.addProperty(DATA_TYPE, dataTypeSym);
	}
	
	@Override
	public void enterVariableStmt(@NotNull VisualBasic6Parser.VariableStmtContext ctx) {
		setCurrentScope(st.getScope(ctx));
		
		//Variáveis ReDim serão definidas em RefTypeSymbol
		if(NodeExplorer.getAncestorClass(ctx, "RedimStmtContext") == null) return;
		
		String name = removeParenETypeIndicator(ctx.Name.getText());
		
		PropertyList properties = defaultProperties(ctx);
		
		properties.addProperty(NAME, name);
		
		Symbol_New sym = getCurrentScope().resolve(properties);
		
		if (sym != null) {
			if(st.getSymbol(ctx) == null) {
				st.setSymbol(ctx, sym);
			}
			else {
				try {
					throw new Exception();
				} catch (Exception e) {
					System.err.format("*** ERROR: SYMBOL ALREADY MARKED IN REDIM COMMAND in COMPILATION UNIT '%s' in line %d%n"
							,st.getCompilarionUnitSymbol(ctx).getName()
			                , ctx.start.getLine());
	                e.printStackTrace();
				}
			}
			return; // Redim já definido anteriormente
		}

		createVarSymbol(ctx);
	}
	
	@Override
	public void exitVariableStmt(@NotNull VisualBasic6Parser.VariableStmtContext ctx) {
		removeCurrentScope();
	}
	
	private void createVarSymbol(VariableStmtContext varCtx){
		PropertyList properties = defaultProperties(varCtx);
		if(varCtx.Name.getText().endsWith(")")){
			properties.addProperty(ARRAY, Integer.parseInt("0"));
		}

		String name = removeParenETypeIndicator(varCtx.Name.getText());
		
		properties.addProperty(NAME,name);
		properties.addProperty(CATEGORY_TYPE,getVarCategory(varCtx));
		
		if (varCtx.initialValue() != null){
			properties.addProperty("INITIAL_VALUE",varCtx.initialValue().getText().replace("=", ""));
		}
		
		if(varCtx.asTypeClause() != null){
				properties.addProperty(DATA_TYPE_NAME, varCtx.asTypeClause().type().getText());
				properties.addProperty(DATA_TYPE_CONTEXT, varCtx.asTypeClause().type());
		}
		else {
			if(NodeExplorer.hasAncestorClass(varCtx, "EnumValueContext")){
				properties.addProperty(DATA_TYPE_NAME, "Integer");
				properties.addProperty(DATA_TYPE_CONTEXT, null);
				properties.addProperty("DEF_MODE", "Implicit");	
			}
			else {
				properties.addProperty(DATA_TYPE_NAME, "Variant");	
				properties.addProperty(DATA_TYPE_CONTEXT, null);
				properties.addProperty("DEF_MODE", "Implicit");	
			}
		}
		if(varCtx.fieldLength() != null ){
			properties.addProperty("LENGHT", varCtx.fieldLength().expr().getText());
		}			
		
		ArrayList<String> modifierList = new ArrayList<String>();
		for(AccessModifierContext amc : getModifierContext(varCtx))	{
			modifierList.add(amc.getText());
			if(amc.getText().equalsIgnoreCase("PUBLIC")
					|| amc.getText().equalsIgnoreCase("GLOBAL")) {
				properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope());
				properties.addProperty(VISIBILITY_SCOPE_NAME,st.getPublicScope().getName());				
			}
		}
		
		if(modifierList.size() > 0){
			properties.addProperty("MODIFIER", modifierList);
		}
		
		properties.addProperty(CONTEXT,varCtx.Name); //? Não sei para que serve
		properties.addProperty(ENCLOSING_SCOPE,getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME,getCurrentScope().getName());	
		
		if(NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null){
			properties.addProperty(PARENT_SCOPE,getCurrentScope());
			properties.addProperty(PARENT_SCOPE_NAME,getCurrentScope().getName());	
		}

			properties.addProperty(SYMBOL_TYPE, SymbolType_New.VARIABLE);
			properties.addProperty(CATEGORY, SymbolType_New.VARIABLE.toString());
		
		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(varCtx, sym);
	}		
	
	@Override
	public void enterType(@NotNull VisualBasic6Parser.TypeContext ctx){
		IScope_New scope = st.getScope(ctx);
		String dataTypeName = ctx.getText();
//		Symbol_New sym = getSymbolOfType(ctx);

//		if(sym == null) return;

		ParserRuleContext ctxSym = NodeExplorer.getAncestorClass(ctx, "VariableStmtContext");
		if (ctxSym == null)
			ctxSym = NodeExplorer.getAncestorClass(ctx, "TypeDefinitiontContext");
		if (ctxSym == null)
			ctxSym = NodeExplorer.getAncestorClass(ctx, "DeclareStmtContext");
		if (ctxSym == null)
			ctxSym = NodeExplorer.getAncestorClass(ctx, "MethodDefinitionContext");
		if (ctxSym == null) {
			GuiDefinitionContext  guiCtx = (GuiDefinitionContext)NodeExplorer.getAncestorClass(ctx, "GuiDefinitionContext");	
			if(guiCtx != null) {
				ctxSym = guiCtx.Name; // Marcado em DefSymbol...
				dataTypeName = guiCtx.type().getText();
			}
		}
			
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
		
		Symbol_New sym = st.getSymbol(ctxSym);

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

	private Symbol_New getSymbolOfType(ParserRuleContext _ctx) {
		ParserRuleContext context = NodeExplorer.getAncestorClass(_ctx, "MethodDefinitionContext");
		if(context instanceof VariableStmtContext) return st.getSymbol(_ctx);
		if(context instanceof MethodDefinitionContext) return st.getSymbol(_ctx);
		if(context instanceof TypeDefinitionContext) return st.getSymbol(_ctx);

		if(context instanceof DeclareStmtContext) return st.getSymbol(_ctx);;
		if(context instanceof EventStmtContext)  return st.getSymbol(_ctx);;
		
		try {
			throw new Exception();
		}catch (Exception e) {
			System.err.format("*** ERROR: TYPE '%s' NOT KNOWN in COMPILATION UNIT '%s' in line %d%n"
	                ,_ctx.getText(), st.getCompilarionUnitSymbol(_ctx).getName()
	                , _ctx.start.getLine());
			e.printStackTrace();
		}
		return null;
	}

	private String removeParen(String _name) { // A(i) -> A
		int parenCount = 0;
		StringBuffer s = new StringBuffer();
		for(int i=0; i < _name.length(); i++){
			if(_name.substring(i, i+1).equals("(")) {parenCount++; continue;}
			if(_name.substring(i, i+1).equals(")")) {parenCount--; continue;}
			if (parenCount == 0){
				s = s.append(_name.substring(i, i+1));
			}
		}
		return s.toString();
	}
	
	private String removeParenETypeIndicator(String _name){
		return removeParen(removeTypeIndicator(_name));
	}	
	
	private String removeTypeIndicator(String name) {
		for (String s : sulfixTypeIndicator){
			if(name.endsWith(s)){
				name = name.replace(s, ""); // A$ -> A
				break;
			}
		}
		return name;
	}
	
	private String getVarCategory(ParserRuleContext varCtx){
		if(NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null){
			return  TYPE;
		}
		if(NodeExplorer.getAncestorClass(varCtx, "EnumValuesContext") != null){
			return  ENUM_VALUE;
		}
		if(NodeExplorer.getAncestorClass(varCtx, "ConstantDefinitionContext") != null){
			return  CONSTANT;
		}
		if(NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext") != null){
			return  VARIABLE;
		}
		if(NodeExplorer.getAncestorClass(varCtx, "RedimStmtContext") != null){
			return  REDIM;
		}		
		if(NodeExplorer.getAncestorClass(varCtx, "FormalParameterContext") != null){
			return  FORMAL_PARAMETER;
		}
		else {
			try {
				throw new Exception();
			}catch (Exception e) {
				System.err.println("*** WARNING: VARIABLE CATEGORY not found for variable");
/*				System.err.format
				("*** WARNING: VARIABLE CATEGORY not found for variable '%s' in line %d in compilation unit '%s'%n", defaultProperties().getProperty(NAME),varCtx.start.getLine(), parser.getFileName());
*/				e.printStackTrace();
			}
			return "UNDEFINED";			
		}
	}
	private List<AccessModifierContext> getModifierContext(ParserRuleContext _ctx){
		VariableStmtContext varCtx = null;
		MethodDefinitionContext methodCtx = null;
		TypeDefinitionContext typeCtx = null;
		EnumerationDefinitionContext enumCtx = null;

		if(_ctx instanceof VariableStmtContext){
			varCtx = (VariableStmtContext)_ctx;
			return getVarModifierList(varCtx);
		}
		else if(_ctx instanceof MethodDefinitionContext){
			methodCtx = (MethodDefinitionContext)_ctx;
			return methodCtx.accessModifier();
		}
		else if(_ctx instanceof TypeDefinitionContext){
			typeCtx = (TypeDefinitionContext)_ctx;
			return typeCtx.accessModifier();
		}
		else if(_ctx instanceof EnumerationDefinitionContext){
			enumCtx = (EnumerationDefinitionContext)_ctx;
			return enumCtx.accessModifier();
		}		
		
//		System.err.println("DEBUG: " + _ctx.getClass().getName());
		
		return new ArrayList<AccessModifierContext>();
	}
	
	private List<AccessModifierContext> getVarModifierList(VariableStmtContext varCtx) {
		if(NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null
				&& NodeExplorer.getAncestorClass(varCtx, "VariableStmtContext") != null){
			final TypeDefinitionContext prc1 = (TypeDefinitionContext)NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext");
			return prc1.accessModifier();
		}
		if(NodeExplorer.getAncestorClass(varCtx, "EnumerationDefinitionContext") != null
			&& NodeExplorer.getAncestorClass(varCtx, "VariableStmtContext") != null){
			final EnumerationDefinitionContext prc2 = (EnumerationDefinitionContext)NodeExplorer.getAncestorClass(varCtx, "EnumerationDefinitionContext");
			return prc2.accessModifier();
		}
		if(NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext") != null){
			final VariableDefinitionContext prc4 = (VariableDefinitionContext)NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext");
			return prc4.accessModifier();
		}
		return new ArrayList<AccessModifierContext>();
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