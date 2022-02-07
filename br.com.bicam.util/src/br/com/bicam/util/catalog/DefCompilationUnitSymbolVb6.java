package br.com.bicam.util.catalog;


import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.AccessModifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.EnumerationDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.ExprContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.FormalParameterContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.GuiDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.MethodDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.TypeDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.VariableDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.VariableStmtContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType;
import br.com.bicam.util.symboltable.CompilationUnit_b_Symbol;
import br.com.bicam.util.symboltable.GuiSymbol_b;
import br.com.bicam.util.symboltable.IScope;
import br.com.bicam.util.symboltable.LabelSymbol;
import br.com.bicam.util.symboltable.LocalScope_b;
import br.com.bicam.util.symboltable.MethodSymbol_b;
import br.com.bicam.util.symboltable.Reference_b_Symbol;
import br.com.bicam.util.symboltable.StructSymbol_b;
import br.com.bicam.util.symboltable.SymbolTableFactory;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;
import br.com.bicam.util.symboltable.Type;

public class DefCompilationUnitSymbolVb6 extends VisualBasic6BaseListener{
	String absoluteFileName; // nome do diretório do projeto VB6

	String vbName;
	
	SymbolTable_b 		st;
	Deque<IScope> 		scopes;
	IScope 				globalScobe;
	SymbolTableFactory  symbolFactory;
	Object 				compUnit;
	PropertyList 		properties;
	Symbol_b 			stubObjeto = null;  // apenas para efeito de testes de criação objetos

	LinkedList<PropertyList> nestedProperties;
	
	IScope currentScope;
	IScope compilationUnitScope;
	
	KeywordLanguage keywordLanguage;
	
	final String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$"};
	
	public DefCompilationUnitSymbolVb6(String _vbName, PropertyList _propertyList){
		scopes = new ArrayDeque<IScope>();
		this.properties = _propertyList;
		this.vbName = _vbName != null ? _vbName.replace("\"", "") : null;
	}
	
	public SymbolTable_b getSymbolTable(){
		return st;
	}
	
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		this.compUnit = properties.getProperty("COMPILATION_UNIT"); 
		String strCompUnit = (String)compUnit;
		String compUnitType = strCompUnit.split("\\.")[strCompUnit.split("\\.").length-1].toUpperCase();
		this.keywordLanguage = (KeywordLanguage)properties.getProperty("KEYWORD_LANGUAGE");
		this.symbolFactory = (SymbolTableFactory)properties.getProperty("SYMBOL_FACTORY");
		this.st = (SymbolTable_b) properties.getProperty("SYMBOLTABLE");

		List<Symbol_b> lista = st.getSymbolByProperty("CATEGORY_TYPE", "PROJECT");

		if(lista.size() > 1 ){ // É esperado a apenas uma ocorrência para "CATEGORY_TYPE", "PROJECT"
			try{
				throw new Exception("** ERROR - ESPERADO APENAS 1 PROJECT " + lista.toString()); 
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		else if(lista.size() == 0 ){ // É esperado a apenas uma ocorrência para "CATEGORY_TYPE", "PROJECT"
                setCurrentScope(st.getGlobalScope());
			    System.err.println("** WARNING - PROJECT FILE NÃO ENCONTRADO "); 
				PropertyList properties = new PropertyList();
				properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
				properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
				properties.addProperty("VISIBILITY", "GLOBAL");
				properties.addProperty("NAME","dummy");
				properties.addProperty("FILE_NAME", compUnit);
				properties.addProperty("CATEGORY_TYPE", "PROJECT");
				properties.addProperty("CATEGORY", "APPLICATION");
				properties.addProperty("CONTEXT", ctx);
				properties.addProperty("SYMBOL_TYPE", SymbolType.APPLICATION);		
				
				Symbol_b appSym = symbolFactory.getSymbol(properties);
				lista.add(appSym);
				setCurrentScope((IScope)appSym);
		}
		else {
		System.err.println();
		setCurrentScope((IScope)lista.get(0));
		}
		
		nestedProperties = new LinkedList<PropertyList>();

//		properties.addProperty("ENCLOSING_SCOPE", (IScope)lista.get(0));
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
//		properties.addProperty("ENCLOSING_SCOPE_NAME", ((IScope)lista.get(0)).getName());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		
		if(vbName != null) 		properties.addProperty("NAME", vbName);
		else properties.addProperty("NAME", getCompilationUnitName(compUnit));
		
//		properties.addProperty("NAME", getCompilationUnitName(compUnit));
		properties.addProperty("DEF_MODE", "UNDEFINED");
		properties.addProperty("CATEGORY", "COMPILATION_UNIT");
		properties.addProperty("CATEGORY_TYPE", compUnitType);

		properties.addProperty("SYMBOL_TYPE", SymbolType.COMPILATION_UNIT);

		CompilationUnit_b_Symbol CompilationUnit_b = (CompilationUnit_b_Symbol)symbolFactory.getSymbol(properties);
		setCompilationUnitProperties(compUnit, CompilationUnit_b.getProperties());

		st.setSymbol(ctx, CompilationUnit_b);
		st.setScope(ctx, CompilationUnit_b);

		compilationUnitScope = CompilationUnit_b;
//		getCurrentScope().define(CompilationUnit_b);
//		define(getCurrentScope(), CompilationUnit_b, false);
		setCurrentScope(CompilationUnit_b);
	}
		
	@Override 
	public void enterOptionStmt(@NotNull VisualBasic6Parser.OptionStmtContext ctx) {
		// property utilizada em refSymbol para a criação implícita de variável
		if(ctx.OPTION_EXPLICIT() != null){
			/*
			 * sobrepõe "DEF_MODE"="UNDEFINED" com "DEF_MODE"="EXPLICIT"
			 */
			st.getCompilarionUnitSymbol(ctx).getProperties().addProperty("DEF_MODE", "EXPLICIT");
		}
	}	
/*
 * alguns símbolos alem de definidos em "ENCLOSING_SCOPE" também são definidos em "COMPILATION_UNIT"
 * Ex.: componentes visuais de forms	
 */
/*	private void defineInPrivateScope(Symbol_b _sym){
		compilationUnitScope.define(_sym);
	}*/
	
/*	private void addSymbolInOtherScopes(Symbol_b _sym){
		addSymbolInPrivateScope(_sym);
// to be defined		addSymbolInProtectedScope(_sym);
		addSymbolInGlobalScope(_sym);
	}*/
	
	private void addSymbolInPrivateScope(Symbol_b _sym){
		((IScope)st.getCompilarionUnitSymbol((ParserRuleContext)_sym.getProperty("CONTEXT")))
		.define(_sym);		
	}
	
	private void addSymbolInGlobalScope(Symbol_b _sym){
		st.getGlobalScope().define(_sym);
	}	
	
	private void addSymbolInProtectedScope(Symbol_b _sym){ //Application
		List<Symbol_b> lista = st.getSymbolByProperty("CATEGORY_TYPE", "PROJECT");
		((IScope)lista.get(0)).define(_sym);
	}		
	
	private void setCurrentScope(IScope _scope){
		scopes.push(_scope);
	}
	
	private IScope getCurrentScope(){
		return scopes.peek();
	}
	
	private void removeCurrentScope(){
		scopes.pop();
	}	
	
	private void setCompilationUnitProperties(Object _compilationUnit, PropertyList _properties){
		if(_compilationUnit instanceof File){
			_properties.addProperty("COMPILATION_UNIT_TYPE", "FILE");
			String compilationUnitName = ((File)_compilationUnit).getAbsolutePath();
					String fileParts[] = 
					((File)_compilationUnit).getAbsolutePath().split("\\\\");
			String name = fileParts[fileParts.length -1];
			name = name.split("\\.")[0];
			_properties.addProperty("COMPILATION_UNIT_NAME", compilationUnitName);
			if(vbName == null) _properties.addProperty("NAME", name);
//			_properties.addProperty("NAME", name);
		}
	}
	
	@Override 
	public void enterAttributeStmt(@NotNull VisualBasic6Parser.AttributeStmtContext ctx) {
		String key = ctx.identifier().getText();
		if(key.equalsIgnoreCase("VB_NAME")){
			String value = 	ctx.expr(0).getText();
			value = value.replace("\"", "");
			addPropertyList("MODULE_NAME",value);
		}		
	}
	
	public void enterGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx){
		st.setScope(ctx, getCurrentScope());
		String name = removeParenETypeIndicator(ctx.Name.getText());
		
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME",name);
		properties.addProperty("CATEGORY","UI");
		
		String lib = null;
		String control = null;
		String type = null;   

		
		if(ctx.type() != null){ // É Form e não Designer
			if(ctx.type().getText().contains(".")){  // VB.Form
				lib = ctx.type().getText().split("\\.")[0];  // VB
				control = ctx.type().getText().split("\\.")[1];  // Form
			}
			else {
				control = ctx.designerHKLM().getText();
			}
			type = ctx.type().getText();  // VB.Form
		}
		else if(ctx.designerHKLM() != null){ // compilation unit com extensão "dsr"
			control = "Designer"; // Form
			type = "Designer";   // compilation unit cm extensão dsr
		}

		properties.addProperty("SYMBOL_TYPE", SymbolType.GUI);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		
		properties.addProperty("TYPE", type);
		properties.addProperty("LIB",lib);
		properties.addProperty("CONTROL", control);	
		properties.addProperty("CATEGORY_TYPE", control);
		properties.addProperty("CATEGORY", "UI");

		
		GuiSymbol_b sym = (GuiSymbol_b) symbolFactory.getSymbol(properties);

		
		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
		
		
		
//		getCurrentScope().define(sym); // ZE ratificar se define deve ficar aqui ou em symbol
		
		if(control.equalsIgnoreCase("FORM") || control.equalsIgnoreCase("MDIFORM")){
			sym.addProperty("VISIBILITY", "GLOBAL");	
			addSymbolInGlobalScope(sym);
			addSymbolInProtectedScope(sym);
		}
		
		addSymbolInPrivateScope(sym);
		setCurrentScope(sym);
	}
	
	public void exitGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx){
		setTypeInGui(ctx);
		removeCurrentScope();
	}
	
	private void setTypeInGui(GuiDefinitionContext _ctx){
		PropertyList _properties = new PropertyList();
		_properties.addProperty("SYMBOLTABLE", st);
		_properties.addProperty("CLASS_TO_RESOLVE", IScope.class);
		_properties.addProperty("CONTEXT_TO_RESOLVE", _ctx);

		String nameType = (String)st.getSymbol(_ctx).getProperty("CONTROL");
		Type type = (Type)st.getScope(_ctx).resolve(nameType, _properties);
		st.getSymbol(_ctx).setType(type);
	}
	
	public void enterGuiProperty(@NotNull VisualBasic6Parser.GuiPropertyContext ctx){
		st.setScope(ctx, getCurrentScope());
		Token tk = ctx.PROPERTY_NAME.start;
		String name = tk.getText();
		Symbol_b guiSymbol = (Symbol_b)getCurrentScope(); // 
		st.setSymbol(ctx.PROPERTY_NAME, guiSymbol);
		
// Exemplo de NestedProperty =>  "Font" em "Form" é property e tem properties	
		if(nestedProperties.isEmpty()) {
			nestedProperties.addLast(guiSymbol.getProperties().addNestedProperty(name));
		}
		else {
			nestedProperties.addLast(nestedProperties.peekLast().addNestedProperty(name));
		}
		if(ctx.HKLM != null){
			nestedProperties.peekLast().addProperty("HKLM",ctx.HKLM.getText());
		}
	}
	
	public void exitGuiProperty(@NotNull VisualBasic6Parser.GuiPropertyContext ctx){
		nestedProperties.removeLast();
	}	
	
	public void enterGuiAttributeSetting(@NotNull VisualBasic6Parser.GuiAttributeSettingContext ctx){
//Exemplo:         Name = "MS Sans Serif"

		ExprContext exprContext = (ExprContext) NodeExplorer.getFirstChildClass(ctx, "ExprContext");

		String[] property =  exprContext.getText().split("=");
		
		String keyProp = property[0];


		String valueProp = property[1];			
		
		if(!nestedProperties.isEmpty()){
			PropertyList p = nestedProperties.peekLast();
			p.addProperty(keyProp, valueProp);
		}
		else{
			((Symbol_b)getCurrentScope()).addProperty(keyProp, valueProp);
		}
	}
	
	public void enterIdentifier(@NotNull IdentifierContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}
	
	private String getCompilationUnitName(Object _compilationUnit){
		String [] parts = _compilationUnit.toString().split("\\\\");
		String name = parts[parts.length - 1];
		name = name.split("\\.")[0];
		return name;
	}
	
	private void addPropertyList(String _key, Object _value){
		getCurrentScope().getProperties().addProperty(_key, _value);
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
	
/*	private Symbol_b getCompilationUnitSymbol(){
		st.getCompilarionUnitSymbol(_ctx)
		return (Symbol_b)compilationUnitScope;
	}*/

	private String removeTypeIndicator(String name) {
		for (String s : sulfixTypeIndicator){
			if(name.endsWith(s)){
				name = name.replace(s, ""); // A$ -> A
				break;
			}
		}
		return name;
	}
	
	public void enterMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx){
		st.setScope(ctx, getCurrentScope());  // marca scope em nome AST

// Verifica se o método é do tipo "Property Get" para criar a variável de classe correspondente
		
//ZE  ****		defVarForPropertyGet(ctx);
		
		String name = removeParenETypeIndicator(ctx.Name.getText());
		PropertyList properties = new PropertyList();
	
		String methodType = ctx.methodType().getText().toUpperCase().split(" ")[ctx.methodType().getText().toUpperCase().split(" ").length-1];
		properties.addProperty("NAME",name);
		properties.addProperty("CATEGORY","PROCEDURE");
		properties.addProperty("CATEGORY_TYPE",methodType);
	
		properties.addProperty("SYMBOL_TYPE", SymbolType.PROCEDURE);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		
		if(ctx.ReturnType != null){
			st.setScope(ctx.asTypeClause().type(),
					getCurrentScope());			
			properties.addProperty("RETURN_TYPE",ctx.asTypeClause().type().getText());			
			properties.addProperty("CONTEXT_TYPE",ctx.asTypeClause().type());			
		}
		else {
			properties.addProperty("RETURN_TYPE", "Void");
		}
		
		ArrayList<String> modifierList = new ArrayList<String>();
		for(AccessModifierContext amc : getModifierContext(ctx))	{
			modifierList.add(amc.getText());
		}
		if(modifierList.size() > 0){
			properties.addProperty("MODIFIER", modifierList);
		}
	
//		MethodSymbol sym = (MethodSymbol)symFactory.getSymbol();
		
		MethodSymbol_b sym = (MethodSymbol_b)symbolFactory.getSymbol(properties);

/*		if(sym.hasProperty("RETURN_TYPE", "Void")){
			PropertyList typeProperties = new PropertyList();
			sym.setType();
		}*/
		
//		define(getCurrentScope(), sym, true);
		setCurrentScope(sym); // marca escopo anter da definição dos parâmeros formais (real parameters)

		setGlobal(modifierList,sym,ctx);
		
		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
//		st.setSymbol((IdentifierContext)NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext"), sym);
//		st.setSymbol(ctx.Name, sym);
//		setSymbolToIdentifier(ctx.expr(), sym);
	}
	
	public void exitMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx){
		while((getCurrentScope() instanceof LocalScope_b) || !getCurrentScope().getProperties().hasProperty("CATEGORY", "PROCEDURE")){
			removeCurrentScope(); // REMOVE LOCAL SCOPES
		}
		removeCurrentScope();
	}
	
	public void enterTypeDefinition(@NotNull VisualBasic6Parser.TypeDefinitionContext ctx){
		st.setScope(ctx, getCurrentScope());  // marca scope em nome AST

		String name = removeParenETypeIndicator(ctx.Name.getText());
		PropertyList properties = new PropertyList();
	
		properties.addProperty("NAME",name);
		properties.addProperty("CATEGORY","TYPE");
		properties.addProperty("CATEGORY_TYPE","TYPE");
	
		properties.addProperty("SYMBOL_TYPE", SymbolType.STRUCT);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		
		ArrayList<String> modifierList = new ArrayList<String>();
		for(AccessModifierContext amc : getModifierContext(ctx))	{
			modifierList.add(amc.getText());
		}
		if(modifierList.size() > 0){
			properties.addProperty("MODIFIER", modifierList);
		}
		
		StructSymbol_b sym = (StructSymbol_b)symbolFactory.getSymbol(properties);

		setCurrentScope(sym); // marca escopo anter da definição dos parâmeros formais (real parameters)

		if(modifierList.size() > 0){
			setGlobal(modifierList, sym,ctx);
		}
		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
	}
	
	public void exitTypeDefinition(@NotNull VisualBasic6Parser.TypeDefinitionContext ctx){
		removeCurrentScope();
	}	

	public void enterEnumerationDefinition (@NotNull EnumerationDefinitionContext ctx){
		st.setScope(ctx, getCurrentScope());  // marca scope em nome AST

		String name = removeParenETypeIndicator(ctx.Name.getText());
		PropertyList properties = new PropertyList();
	
		properties.addProperty("NAME",name);
		properties.addProperty("CATEGORY","ENUM");
		properties.addProperty("CATEGORY_TYPE","TYPE");
	
		properties.addProperty("SYMBOL_TYPE", SymbolType.STRUCT);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		
		ArrayList<String> modifierList = new ArrayList<String>();
		
		for(AccessModifierContext amc : getModifierContext(ctx))	{
			modifierList.add(amc.getText());
		}
		
		if(modifierList.size() > 0){
			properties.addProperty("MODIFIER", modifierList);
		}
		
		StructSymbol_b sym = (StructSymbol_b)symbolFactory.getSymbol(properties);

		setCurrentScope(sym);
		
		setGlobal(modifierList,sym,ctx);

		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
	}
	
	public void exitEnumerationDefinition(@NotNull VisualBasic6Parser.EnumerationDefinitionContext ctx){
		removeCurrentScope();
	}	
	
	@Override
	public void enterFormalParameters (@NotNull VisualBasic6Parser.FormalParametersContext _ctx){ 
		st.setScope(_ctx, getCurrentScope());
		Symbol_b sym = (Symbol_b)getCurrentScope();
		List<String> orderedFormalParameters = new ArrayList<String>();
		for (FormalParameterContext fpCTtx : _ctx.formalParameter()){
//			FormalParameterContext fpCTtx = stmtCTtx.formalParameter();
					st.setScope(fpCTtx, getCurrentScope());  // marca scope em nome AST
					if(fpCTtx.variableStmt().asTypeClause() != null){
						orderedFormalParameters.add(fpCTtx.variableStmt().asTypeClause().type().getText());
					}
		}
		sym.addProperty("ORDERED_ARGS", orderedFormalParameters);
	}
	
	@Override	
	public void exitFormalParameters (@NotNull VisualBasic6Parser.FormalParametersContext _ctx){ 
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME","LOCAL_" + getCurrentScope().getName());
		properties.addProperty("CONTEXT",_ctx);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		properties.addProperty("SYMBOL_TYPE", SymbolType.LOCAL_SCOPE);
		properties.addProperty("CATEGORY", "SCOPE");
		properties.addProperty("CATEGORY_TYPE", "LOCAL");

		LocalScope_b scopeSym = (LocalScope_b)symbolFactory.getSymbol(properties);
//		define(getCurrentScope(),scopeSym, false);
		setCurrentScope(scopeSym);		
	}
	
	
	/*
	 * adiciona enumValues em privateScope, ou seja, em Compilation Unit Scope
	 */
	public void exitEnumValues (@NotNull VisualBasic6Parser.EnumValuesContext _ctx){
		for(VariableStmtContext varCtx : _ctx.variableStmt()){
			addSymbolInPrivateScope(st.getSymbol(varCtx));
		}
	}
	
/*	public void enterFormalParameter (@NotNull VisualBasic6Parser.FormalParameterContext _ctx){
		st.setScope(_ctx, getCurrentScope());
		createFormalParameterSymbol(_ctx);
	}*/	

/*	private void createFormalParameterSymbol(FormalParameterContext _ctx){
		String name = removeParenETypeIndicator(_ctx.Name.getText());
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME",name);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		properties.addProperty("CATEGORY","VARIABLE");
		properties.addProperty("CATEGORY_TYPE","FORMAL_PARAMETER");

		
//		if (_ctx.paramDefaultValue() != null){
		if (_ctx.variableStmt().initialValue() != null){
//			if (_ctx.paramDefaultValue().initialValue() != null){
			if (_ctx.variableStmt().initialValue() != null){
				
				properties.addProperty("INITIAL_VALUE",_ctx.variableStmt().initialValue().getText().replace("=", ""));
			}
		}
		
		if(_ctx.variableStmt().asTypeClause() != null){
			if(_ctx.variableStmt().asTypeClause().type() != null ){
				properties.addProperty("DATA_TYPE", _ctx.variableStmt().asTypeClause().type().getText());
				properties.addProperty("TYPE_CONTEXT", _ctx.variableStmt().asTypeClause().type());
			}
			else {
				properties.addProperty("DATA_TYPE", "Variant");
				properties.addProperty("DEF_MODE", "Implicit");
			}
			if(_ctx.fieldLength() != null ){
				properties.addProperty("LENGHT", _ctx.fieldLength().expr().getText());
			}			
		}

		properties.addProperty("SYMBOL_TYPE", SymbolType.VARIABLE);
		properties.addProperty("CONTEXT",_ctx);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		
		VariableSymbol_b sym = (VariableSymbol_b)symbolFactory.getSymbol(properties);
		
		st.setSymbol(_ctx, sym);
//		st.setSymbol(_ctx.Name, sym);

//		define(getCurrentScope(), sym, false);
		
//		st.setSymbol((IdentifierContext)NodeExplorer.getNextFirstChildClass(_ctx, "IdentifierContext"), sym);
		st.setSymbol((IdentifierContext)NodeExplorer.getDepthFirstChildClass(_ctx, "IdentifierContext"), sym);

	}	*/

	@Override
	public void enterVariableStmt(@NotNull VisualBasic6Parser.VariableStmtContext ctx) {
		st.setScope(ctx, getCurrentScope());
		if(NodeExplorer.getAncestorClass(ctx, "RedimStmtContext") != null) return;
        if(NodeExplorer.getNextAncestorClass(ctx, "RedimStmtContext") != null) {
    		//Variáveis ReDim serão definidas em RefTypeSymbol
        	if(ctx.asTypeClause() == null) return;
        }
		
		createVarSymbol(ctx);
	}
	@Override
	public void enterLabel(@NotNull VisualBasic6Parser.LabelContext _ctx) {
		st.setScope(_ctx, getCurrentScope());		

		PropertyList properties = new PropertyList();
		String name = _ctx.start.getText();
		name = name.replace(":", "");
		properties.addProperty("NAME",name);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		properties.addProperty("SYMBOL_TYPE", SymbolType.LABEL);

		properties.addProperty("CATEGORY_TYPE","LABEL_NAME");
		properties.addProperty("CATEGORY","LABEL");
		properties.addProperty("TYPE","ALPHA_NUMERIC");
		properties.addProperty("CONTEXT",_ctx);		
		
		LabelSymbol sym = (LabelSymbol)symbolFactory.getSymbol(properties);
		
//		define(getCurrentScope(), sym, false);
		
		st.setSymbol(_ctx, sym);		
	}

	/*	
	@Override
	public void enterLabelLine(@NotNull VisualBasic6Parser.LabelLineContext _ctx) {
		st.setScope(_ctx, getCurrentScope());		

		PropertyList properties = new PropertyList();
		String name = _ctx.start.getText();
		name = name.replace(":", "");
		properties.addProperty("NAME",name);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		properties.addProperty("SYMBOL_TYPE", SymbolType.LABEL);

		properties.addProperty("CATEGORY_TYPE","LABEL_NAME");
		properties.addProperty("CATEGORY","LABEL");
		properties.addProperty("TYPE","ALPHA_NUMERIC");
		properties.addProperty("CONTEXT",_ctx);		
		
		LabelSymbol sym = (LabelSymbol)symbolFactory.getSymbol(properties);
		
//		define(getCurrentScope(), sym, false);
		
		st.setSymbol(_ctx, sym);		
	}
	
	@Override
	public void enterLineNumber(@NotNull VisualBasic6Parser.LineNumberContext _ctx) {
		st.setScope(_ctx, getCurrentScope());		

		PropertyList properties = new PropertyList();
		String name = _ctx.start.getText();
		name = name.replace(":", "");
		properties.addProperty("NAME",name);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		properties.addProperty("SYMBOL_TYPE", SymbolType.LABEL);

		properties.addProperty("CATEGORY_TYPE","LABEL_NUMBER");
		properties.addProperty("CATEGORY","LABEL");
		properties.addProperty("TYPE","ALPHA_NUMERIC");
		properties.addProperty("CONTEXT",_ctx);		
		
		LabelSymbol sym = (LabelSymbol)symbolFactory.getSymbol(properties);
		
//		define(getCurrentScope(), sym, false);
		
		st.setSymbol(_ctx, sym);		
	}
*/	

	
	public void enterRedimStmt(@NotNull VisualBasic6Parser.RedimStmtContext _ctx) {
		st.setScope(_ctx, getCurrentScope());
	}	
	
	public void enterSetStmt(@NotNull VisualBasic6Parser.SetStmtContext _ctx) {
		st.setScope(_ctx, getCurrentScope());
	}	

	public void enterDeclareStmt(@NotNull VisualBasic6Parser.DeclareStmtContext ctx){
/*		getCurrentScope().setDefMode(); // Se DEF_MODE is Undefined 
                                        // então altera DEF_MODE para "IMPLICIT"
                                        // por que comando "Option Explicit" não foi utilizado
		                                // e porque este comamdo aqui?... Porque
		                                // a definição de método
*/		
		st.setScope(ctx, getCurrentScope());  

		String name = removeParenETypeIndicator(ctx.Name.getText());
		PropertyList properties = new PropertyList();
	
		properties.addProperty("NAME",name);
//		properties.addProperty("CATEGORY","PROCEDURE");
		properties.addProperty("CATEGORY","REFERENCE");
		properties.addProperty("CATEGORY_TYPE",ctx.methodType().getText().toUpperCase());
		properties.addProperty("SYMBOL_TYPE", SymbolType.REFERENCE);
		properties.addProperty("CONTEXT",ctx);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());
		
		
		if(ctx.ReturnType != null){
			st.setScope(ctx.asTypeClause().type(),
					getCurrentScope());			
			properties.addProperty("RETURN_TYPE",ctx.asTypeClause().type().getText());			
		}
		else {
			properties.addProperty("RETURN_TYPE", "Void");
		}
		
		ArrayList<String> modifierList = new ArrayList<String>();
		for(AccessModifierContext amc : getModifierContext(ctx))	{
			modifierList.add(amc.getText());
		}
		if(modifierList.size() > 0){
			properties.addProperty("MODIFIER", modifierList);
		}
	
//		MethodSymbol_b sym = (MethodSymbol_b)symbolFactory.getSymbol(properties);
		Reference_b_Symbol sym = (Reference_b_Symbol)symbolFactory.getSymbol(properties);
		
		
/*		if(sym.hasProperty("RETURN_TYPE", "Void"))
			sym.setType((Type)getCurrentScope().resolve((String)sym.getProperty("RETURN_TYPE"), PropertyList.getDefault(st, ctx,getCurrentScope())));
*/

//		define(getCurrentScope(), sym, true);
		setCurrentScope(sym);     // marca escopo anter da definição dos parâmeros formais
		setGlobal(modifierList,sym,ctx);

		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
		IdentifierContext idCtx = (IdentifierContext)NodeExplorer.getFirstChildClass(ctx, "IdentifierContext");
		st.setSymbol(idCtx, sym);
	}
	
	public void exitDeclareStmt(@NotNull VisualBasic6Parser.DeclareStmtContext ctx){
//		if(getCurrentScope().getName().equalsIgnoreCase("LOCAL")){ // FormalParameters
		if(getCurrentScope().getName().startsWith("LOCAL")){ // FormalParameters
			removeCurrentScope();
		}
		removeCurrentScope();
	}
	
	public void enterType(@NotNull VisualBasic6Parser.TypeContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}
	
/*	private boolean isDbVariable(VariableStmtContext varCtx){
		ParserRuleContext typeContext = (ParserRuleContext) NodeExplorer.getDepthFirstChildClass(varCtx, "TypeContext");
		if(typeContext != null) return false;
		ParserRuleContext identifierContext =  (ParserRuleContext)NodeExplorer.getDepthFirstChildClass(typeContext, "IdentifierContext");

		if(identifierContext != null && identifierContext.getText().contains("ADODB")){
			return true;
		}
		
		return false;
	}*/
	
	private void createVarSymbol(VariableStmtContext varCtx){
		String name = removeParenETypeIndicator(varCtx.Name.getText());
		
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME",name);
		properties.addProperty("CATEGORY_TYPE",getVarCategory(varCtx));
		
		if(isObject(varCtx)){
			properties.addProperty("OBJECT","TRUE");
		}
		
		if (varCtx.initialValue() != null){
			properties.addProperty("INITIAL_VALUE",varCtx.initialValue().getText().replace("=", ""));
		}
		
		if(varCtx.asTypeClause() != null){
			if(varCtx.asTypeClause().type() != null ){
				properties.addProperty("DATA_TYPE", varCtx.asTypeClause().type().getText());
				properties.addProperty("TYPE_CONTEXT", varCtx.asTypeClause().type());
			}
			else {
				properties.addProperty("DATA_TYPE", "Variant");				
				properties.addProperty("DEF_MODE", "Implicit");	
			}
			if(varCtx.fieldLength() != null ){
				
				properties.addProperty("LENGHT", varCtx.fieldLength().expr().getText());
			}			
		}
		else {
			if(NodeExplorer.hasAncestorClass(varCtx, "EnumValueContext")){
				properties.addProperty("DATA_TYPE", "Integer");				
				properties.addProperty("DEF_MODE", "Implicit");	
			}
		}
		
		ArrayList<String> modifierList = new ArrayList<String>();
		for(AccessModifierContext amc : getModifierContext(varCtx))	{
			modifierList.add(amc.getText());
		}
		if(modifierList.size() > 0){
			properties.addProperty("MODIFIER", modifierList);
		}
		properties.addProperty("CONTEXT",varCtx.Name);
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME",getCurrentScope().getName());	

		
		if(NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null){
			properties.addProperty("SYMBOL_TYPE", SymbolType.STRUCT);
			properties.addProperty("CATEGORY", SymbolType.STRUCT.toString());
		}

		else if((properties.getProperty("DATA_TYPE") != null )
				&& properties.getProperty("DATA_TYPE").toString().contains("ADODB")){
			properties.addProperty("SYMBOL_TYPE", SymbolType.DB);
			properties.addProperty("CATEGORY", SymbolType.DB.toString());
			properties.addProperty("CATEGORY_TYPE", SymbolType.DB.toString());			
			if(properties.getProperty("DATA_TYPE").toString().contains("Connection"))
				properties.addProperty("CATEGORY_TYPE", "Connection");			
			if(properties.getProperty("DATA_TYPE").toString().contains("Recordset"))
				properties.addProperty("CATEGORY_TYPE", "Recordset");
			if(properties.getProperty("DATA_TYPE").toString().contains("Command"))
				properties.addProperty("CATEGORY_TYPE", "Command");				
		}
		else if(properties.getProperty("OBJECT") != null){
			properties.addProperty("SYMBOL_TYPE", SymbolType.OBJECT);
			properties.addProperty("CATEGORY", SymbolType.VARIABLE.toString());
		}		
		else {
			properties.addProperty("SYMBOL_TYPE", SymbolType.VARIABLE);
			properties.addProperty("CATEGORY", SymbolType.VARIABLE.toString());
		}
		
		Symbol_b sym = symbolFactory.getSymbol(properties);
		
		
//		define(getCurrentScope(), sym, false);
		
		setGlobal(modifierList,sym,varCtx);

		st.setSymbol(varCtx, sym);
		st.setSymbol(varCtx.Name, sym);
//		if(NodeExplorer.getDepthFirstChildClass(varCtx, "IdentifierContext") != null){
//			st.setSymbol((IdentifierContext)NodeExplorer.getDepthFirstChildClass(varCtx, "IdentifierContext"), sym);
//		}
	}
	
	private String getVarCategory(ParserRuleContext varCtx){
		
		if(NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null){
			return  "TYPE";
		}
		if(NodeExplorer.getAncestorClass(varCtx, "EnumValueContext") != null){
			return  "ENUM_VALUE";
		}
		if(NodeExplorer.getAncestorClass(varCtx, "ConstantDefinitionContext") != null){
			return  "CONSTANT";
		}
		if(NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext") != null){
			return  "VARIABLE";
		}
		if(NodeExplorer.getAncestorClass(varCtx, "RedimStmtContext") != null){
			return  "REDIM";
		}		
		if(NodeExplorer.getAncestorClass(varCtx, "FormalParameterContext") != null){
			return  "FORMAL_PARAMETER";
		}		
		return "UNDEFINED";
	}
	
	private boolean isObject(VariableStmtContext _ctx){ 
		
		ParserRuleContext typeCtx = NodeExplorer.getChildClass(_ctx, "TypeContext");
		if(typeCtx != null){ //  esse teste foi ver se funciona em "form", mas falta o teste real
			if(typeCtx.start.getText().equalsIgnoreCase("Form")) return true;
		}
		return false;
	}

	private List<AccessModifierContext> getVarModifierList(VariableStmtContext varCtx) {
		if(NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null){
			final TypeDefinitionContext prc1 = (TypeDefinitionContext)NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext");
			return prc1.accessModifier();
		}
		if(NodeExplorer.getAncestorClass(varCtx, "EnumerationDefinitionContext") != null){
			final EnumerationDefinitionContext prc2 = (EnumerationDefinitionContext)NodeExplorer.getAncestorClass(varCtx, "EnumerationDefinitionContext");
			return prc2.accessModifier();
		}
		if(NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext") != null){
			final VariableDefinitionContext prc4 = (VariableDefinitionContext)NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext");
			return prc4.accessModifier();
		}
		return new ArrayList<AccessModifierContext>();
	}
	
	private boolean setGlobal(ArrayList<String> _modifierList, Symbol_b _sym, ParserRuleContext _ctx){
		boolean isGlobal = false;
		if(_modifierList.size() > 0){
			for(String modifier :  _modifierList){
				if(modifier.equalsIgnoreCase("Public"))
					isGlobal = true;
				if(modifier.equalsIgnoreCase("Global"))
					isGlobal = true;
			}
			if(isGlobal){
				addSymbolInGlobalScope(_sym);
//				st.getGlobalScope().define(_sym);
			}
			return isGlobal;
		}
		
		if(NodeExplorer.getAncestorClass(_ctx, "FormalParameterContext") != null) return false;
		
		
		if(st.getCompilarionUnitSymbol(_ctx).hasProperty("CATEGORY_TYPE", "BAS")
				|| st.getCompilarionUnitSymbol(_ctx).hasProperty("CATEGORY_TYPE", "CLS")){
			isGlobal = true;
		}
		
		if(isGlobal) {
			addSymbolInGlobalScope(_sym);
		}

		return isGlobal;
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
	
}