package br.com.bicam.util.catalog;

/*
Identifier type character	Data type	Example
%							Integer		Dim L%
&							Long		Dim M&
@							Decimal		Const W@ = 37.5
!							Single		Dim Q!
#							Double		Dim X#
$							String		Dim V$ = "Secret"
*/

import static br.com.bicam.util.constant.PropertyName.ARRAY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.CATEGORY_TYPE;
import static br.com.bicam.util.constant.PropertyName.CONSTANT;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_NAME;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.ENUM;
import static br.com.bicam.util.constant.PropertyName.ENUM_VALUE;
import static br.com.bicam.util.constant.PropertyName.FILE_NAME;
import static br.com.bicam.util.constant.PropertyName.FORMAL_PARAMETER;
import static br.com.bicam.util.constant.PropertyName.GLOBAL;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.LOCAL;
import static br.com.bicam.util.constant.PropertyName.MODULE_NAME;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.PARENT_SCOPE;
import static br.com.bicam.util.constant.PropertyName.PARENT_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.PROCEDURE;
import static br.com.bicam.util.constant.PropertyName.PUBLIC;
import static br.com.bicam.util.constant.PropertyName.REDIM;
import static br.com.bicam.util.constant.PropertyName.RETURN_TYPE_NAME;
import static br.com.bicam.util.constant.PropertyName.SCOPE;
import static br.com.bicam.util.constant.PropertyName.STRUCTURE;
import static br.com.bicam.util.constant.PropertyName.SUSPICIOUS_COMPILATION_UNIT;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_TYPE;
import static br.com.bicam.util.constant.PropertyName.THIS;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.AccessModifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.DeclareStmtContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.EnumerationDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.ExprContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.MethodDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.TypeDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.VariableDefinitionContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.VariableStmtContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.StringOptionalCase;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.symboltable.GuiSymbol_New;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.LocalScope_New;
import br.com.bicam.util.symboltable.Member;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class CompilationUnitDefSymbolVb6 extends VisualBasic6BaseListener {
	String absoluteFileName; // nome do diretório do projeto VB6

	String vbName;
	boolean optionExpicit;

	SymbolTable_New st;
	Deque<IScope_New> scopes;
	IScope_New globalScope;
	IScope_New compilationUnitScope;
	IScope_New formScope;

	SymbolFactory symbolFactory;
	Object compUnit;
	PropertyList properties;
	CompilationUnit compilationUnit;
	Symbol_New stubObjeto = null; // apenas para efeito de testes de criação objetos

	LinkedList<PropertyList> nestedProperties;

	KeywordLanguage keywordLanguage;

	PropertyList currentProperties;

	Set<String> controlArrays;
	Map<Integer, String> controlArrayMap;
	Set<String> controlArrayAddedInScope;
	Map<String, Integer> occurs;
//	Set<String> uicontrolVisited;
	Map<String,Symbol_New> uicontrolVisited;


	// Set<String> suspiciousCompUnits;
	Map<String, Set<String>> suspiciousCompUnits;

	boolean isCompUnitValid;

	final String[] sulfixTypeIndicator = new String[] { "&", "%", "#", "!", "@", "$" };

	public CompilationUnitDefSymbolVb6(PropertyList _propertyList) {
		this.scopes = new ArrayDeque<IScope_New>();
		this.properties = _propertyList;
		this.compilationUnit = (CompilationUnit) properties.getProperty("COMPILATION_UNIT");
		this.st = (SymbolTable_New) properties.getProperty(SYMBOLTABLE);
		this.symbolFactory = (SymbolFactory) properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage) properties.getProperty(KEYWORD_LANGUAGE);
		this.globalScope = st.getGlobalScope();//(IScope_New) properties.getProperty(GLOBAL_SCOPE);

		this.suspiciousCompUnits = (Map<String, Set<String>>) properties.getProperty(SUSPICIOUS_COMPILATION_UNIT);
		this.nestedProperties = new LinkedList<PropertyList>();

		this.controlArrayAddedInScope = new HashSet<String>();
		this.controlArrayMap = new HashMap<Integer, String>();
		this.uicontrolVisited = new HashMap<String, Symbol_New>();


		this.controlArrays = arrayInVbForm(); // todos os form control que são arrays (tem index)
		this.occurs = arrayInVbForm2();

	}

	public SymbolTable_New getSymbolTable() {
		return st;
	}

	private String getVbName() {
		try {
			String fileName = compilationUnit.getFileName();
			String REGEX = "^Attribute\\s+VB_Name\\s+=\\s+\\\"([\\d\\w]+)\\\"$";
			// _file =>
			// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
			if (fileName == null)
				return null;
			vbName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
			vbName = vbName.split("\\.")[0];
			Pattern p = Pattern.compile(REGEX);
			BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				Matcher m = p.matcher(line);
				if ((m.matches())) {
					return vbName = m.group(1);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vbName;
	}

	private boolean isOptionExpicit() {
		try {
			String fileName = compilationUnit.getFileName();
			String REGEX = "^Option\\s+Explicit$";
			Pattern p = Pattern.compile(REGEX);
			BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				Matcher m = p.matcher(line);
				if ((m.matches())) {
					return true;
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Set<String> arrayInVbForm() {
		/*
		 * want search for "Index" then record "lb_Label" Begin VB.Label lb_Label
		 * Appearance = 0 'Flat AutoSize = -1 'True Caption = "Produto" ForeColor =
		 * &H00800000& Height = 195 Index = 4 Left = 105 TabIndex = 93 Top = 0 Width =
		 * 675 End
		 */ HashSet<String> controls = new HashSet<String>();
		String control = null;
		String REGEXBegin = "^\\s*Begin\\s+\\w+\\.\\w+\\s+(\\w+)\\s*";
		String REGEXIndex = "^\\s*Index\\s+=\\s+(\\d+)\\s*";
		Integer lineNum = 0;
		Integer lineControlNum = 0;

		// _file =>
		// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
		Pattern pBegin = Pattern.compile(REGEXBegin);
		Pattern pIndex = Pattern.compile(REGEXIndex);

		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(compilationUnit.getFileName())));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				lineNum++;
				Matcher mBegin = pBegin.matcher(line);
				Matcher mIndex = pIndex.matcher(line);
				if ((mBegin.matches())) {
					control = mBegin.group(1);
					lineControlNum = lineNum;
				}
				if ((mIndex.matches())) {
					controls.add(control);
					controlArrayMap.put(lineControlNum, mIndex.group(1));
					control = null;
				}
			}
		} catch (Exception e) {
			System.out.println("File I/O error!");
		}
		return controls;
	}

	private Map<String, Integer> arrayInVbForm2() {
		Map<String, Integer> occurs = new HashMap<String, Integer>();
		String control = null;
		String REGEXBegin = "^\\s*Begin\\s+\\w+\\.\\w+\\s+(\\w+)\\s*";
		String REGEXIndex = "^\\s*Index\\s+=\\s+(\\d+)\\s*";
		Integer lineNum = 0;

		// _file =>
		// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
		Pattern pBegin = Pattern.compile(REGEXBegin);
		Pattern pIndex = Pattern.compile(REGEXIndex);

		try {
			BufferedReader in = new BufferedReader(new FileReader((compilationUnit.getFileName())));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				lineNum++;
				Matcher mBegin = pBegin.matcher(line);
				Matcher mIndex = pIndex.matcher(line);
				if ((mBegin.matches())) {
					control = mBegin.group(1);
				}
				if ((mIndex.matches())) {
					Integer index = occurs.get(control);
					if (index != null) {
						if (Integer.parseInt(mIndex.group(1)) + 1 > index) {
							occurs.put(control, Integer.parseInt(mIndex.group(1)) + 1);
						}
					} else {
						occurs.put(control, Integer.parseInt(mIndex.group(1)) + 1);
					}
					control = null;
				}
			}
		} catch (IOException e) {
			System.out.println("File I/O error!");
		}
		return occurs;
	}

	/*
	 * rename nomes de controles que são array para não serem duplicados nos escopos
	 */
	private String getFormControlName(String _name, ParserRuleContext _ctx) {
		if (controlArrays.contains(_name)) {
			return controlArrayMap.get(_ctx.start.getLine());
		}
		return _name;
	}

	public boolean IsValidCompUnit() {
		return isCompUnitValid;
	}

	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		List<Symbol_New> listAppSym = st.getSymbolByProperty(CATEGORY, "APPLICATION");
		IScope_New appSym = null;
		if (listAppSym.size() == 1) {
			st.setPublicScope(listAppSym.get(0));
		} else {
			try {
				throw new Exception();
			} catch (Exception e) {
				System.err.println("*** ERROR - More than on application symbol");
				e.printStackTrace();
				System.exit(1);
			}
		}

		String fileName = compilationUnit.getFileName();
		String compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];

		String name = compilationUnitName.split("\\.")[0]; // remove extenção

		// defining compilation unit scope
		PropertyList prop2 = defaultProperties(); // new PropertyList();
		prop2.addProperty(ENCLOSING_SCOPE, st.getPublicScope()); // publicScope);
		prop2.addProperty(ENCLOSING_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
		
		if (name.equalsIgnoreCase(getVbName()) && compilationUnit.getFileName().toUpperCase().endsWith("FRM") ) {
			prop2.addProperty("NAME", getVbName() + "#COMPILATION_UNIT"); // "#COMPILATION_UNIT# diferencia compUnitName
		} else {
			if(compilationUnit.getFileName().toUpperCase().endsWith("FRM")){
				prop2.addProperty(NAME, name); // Form file tem nome diferente do form 
			}
			else prop2.addProperty(NAME, getVbName());
		}	
		
		prop2.addProperty(MODULE_NAME, getVbName());
		prop2.addProperty(FILE_NAME, fileName);
		prop2.addProperty("COMPILATION_UNIT", compilationUnit);
		prop2.addProperty("COMPILATION_UNIT_NAME", compilationUnitName);
		prop2.addProperty(CATEGORY_TYPE, "FILE");
		prop2.addProperty(CATEGORY, "COMPILATION_UNIT");
		prop2.addProperty("DATA_TYPE_NAME", "UNDEFINED");

		if (isOptionExpicit())
			prop2.addProperty("DEF_MODE", "EXPLICIT");
		else
			prop2.addProperty("DEF_MODE", "LANGUAGE_IMPLICIT");
		prop2.addProperty(SYMBOL_TYPE, SymbolType_New.COMPILATION_UNIT);
		compilationUnitScope = (IScope_New) symbolFactory.getSymbol(prop2);

		if (!compilationUnitName.split("\\.")[1].equalsIgnoreCase("FRM")) {
			createThis(prop2, (Symbol_New) compilationUnitScope, (Symbol_New) compilationUnitScope);
		}

		setCurrentScope(compilationUnitScope);
		st.setSymbol(ctx, (Symbol_New) compilationUnitScope);
	}

	private void createThis(PropertyList _properties, Symbol_New _this, Symbol_New _enclosingScope) {
		PropertyList property_this = _properties.getCopy();
		property_this.addProperty(NAME, "Me");
		property_this.addProperty(THIS, _this);

		// Não faz sentido "me" com visibilidade pública ou global
		property_this.removeProperty(VISIBILITY_SCOPE);
		property_this.removeProperty(VISIBILITY_SCOPE_NAME);
		property_this.addProperty(ENCLOSING_SCOPE, compilationUnitScope);
		property_this.addProperty(ENCLOSING_SCOPE_NAME, compilationUnitScope.getName());
		symbolFactory.getSymbol(property_this);
	}

	public void exitStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
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

	private GuiSymbol_New createControlArraySymbol(PropertyList _properties, ParserRuleContext _ctx) {
		PropertyList properties = _properties.getCopy();
		String name = (String) _properties.getProperty("NAME") + "#ix" + controlArrayMap.get(_ctx.start.getLine());
		properties.addProperty(NAME, name);
		GuiSymbol_New sym = (GuiSymbol_New) symbolFactory.getSymbol(properties);
		return sym;
	}

	@Override
	public void enterGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx) {
		st.setScope(ctx, getCurrentScope());
		String name = removeParenETypeIndicator(ctx.Name.getText())[0];

		GuiSymbol_New sym = null;

		PropertyList properties = defaultProperties(); // new PropertyList();

		properties.addProperty(NAME, name);

		properties.addProperty(CATEGORY, "UI");

		String lib = null;
		String control = null;
		String dataTypeName = null;

		lib = ctx.type().getText().split("\\.")[0]; // VB, Threed, etc...
		control = ctx.type().getText().split("\\.")[1]; // Form

		dataTypeName = ctx.type().getText(); // VB.Form

		properties.addProperty(CONTEXT, ctx);

		properties.addProperty(DATA_TYPE_NAME, dataTypeName);
		properties.addProperty("LIB", lib);
		properties.addProperty("CONTROL", control);
		properties.addProperty(CATEGORY_TYPE, control);
		properties.addProperty(CATEGORY, "UI");

		properties.addProperty(SYMBOL_TYPE, SymbolType_New.GUI);

		if (lib.equals("VB") && (control.equals("Form") || control.equals("MDIForm"))) {
			properties.addProperty(ENCLOSING_SCOPE, getCurrentScope()); // Form é visível a nível de aplicação
			properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
			properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope); // Form é visível a nível de
																			// aplicação
			properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope()); // publicScope.getName());
		} else { // control é visível a nivel de module (compilation unit)
			properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
			properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
			properties.addProperty(PARENT_SCOPE, getCurrentScope());
			properties.addProperty(PARENT_SCOPE_NAME, getCurrentScope().getName());
			properties.addProperty(VISIBILITY_SCOPE, compilationUnitScope);
			properties.addProperty(VISIBILITY_SCOPE_NAME, compilationUnitScope.getName());

			if (occurs.get(name) != null) {
				sym = createControlArraySymbol(properties, ctx); // cria UI com nome + #ix<arrayindedx>
				properties.addProperty("ARRAY", Integer.toString(occurs.get(name)));
			}
		}

		if (sym == null) { // Não é array
			sym = (GuiSymbol_New) symbolFactory.getSymbol(properties);
			if ((control.equals("Form") || control.equals("MDIForm"))) {
				formScope = sym;
			} else {
				properties.addProperty(PARENT_SCOPE, formScope); // resolve por exemplo: form.button
				properties.addProperty(PARENT_SCOPE_NAME, formScope.getName());
				sym.defineParentScope();
			}
		}

		if (st.getSymbol(ctx) == null && !sym.getName().contains("#ix")) {
			st.setSymbol(ctx, sym); // pode ter sido incluido em createControlArraySymbol
			st.setSymbol(ctx.Name, sym); // Utilizado em RefTypeSymbol
		}

		if (dataTypeName.equalsIgnoreCase("VB.Form") || dataTypeName.equalsIgnoreCase("VB.MDIForm")) {
			createThis(properties, (Symbol_New) sym, (Symbol_New) compilationUnitScope);
		}

		setCurrentScope(sym);
	}

	@Override
	public void exitGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx) {
		Symbol_New sym = (Symbol_New) getCurrentScope();
		if (sym.getName().contains("#ix")) {
	        if(uicontrolVisited.get(sym.getName().split("#")[0]) == null) {
				PropertyList properties = sym.getProperties().getCopy(); // Não faça properties = sym.getProperties(); senão estão alterando propriedades de sym
				properties.addProperty(SYMBOL_TYPE, SymbolType_New.GUI);
				properties.addProperty(NAME, sym.getName().split("#")[0]);
				properties.addProperty("Index", "-1");
				sym = (GuiSymbol_New) symbolFactory.getSymbol(properties);
				uicontrolVisited.put(sym.getName().split("#")[0],sym);
				st.setSymbol(ctx, sym);
				st.setSymbol(ctx.Name, sym);
	        }
	        else {
	        	st.setSymbol(ctx, uicontrolVisited.get(sym.getName().split("#")[0]));
	        	st.setSymbol(ctx.Name, uicontrolVisited.get(sym.getName().split("#")[0]));
	        }
			removeCurrentScope();
		} else
			removeCurrentScope();
		
	}

	// GuiAttributeProperty
	@Override
	public void enterGuiProperty(@NotNull VisualBasic6Parser.GuiPropertyContext ctx) {
		st.setScope(ctx, getCurrentScope());
		Token tk = ctx.PROPERTY_NAME.start;
		String name = tk.getText();
		Symbol_New guiSymbol = (Symbol_New) getCurrentScope(); //
		st.setSymbol(ctx.PROPERTY_NAME, guiSymbol);

		// Exemplo de NestedProperty => "Font" em "Form" é property e tem properties
		if (nestedProperties.isEmpty()) {
			nestedProperties.addLast(guiSymbol.getProperties().addNestedProperty(name));
		} else {
			nestedProperties.addLast(nestedProperties.peekLast().addNestedProperty(name));
		}
		if (ctx.HKLM != null) { // HKLM é {0BE35203-8F91-11CE-9DE3-00AA004BB851} em ... BeginProperty Font
								// {0BE35203-8F91-11CE-9DE3-00AA004BB851}
			nestedProperties.peekLast().addProperty("HKLM", ctx.HKLM.getText());
		}
	}

	// GuiAttributeProperty
	@Override
	public void exitGuiProperty(@NotNull VisualBasic6Parser.GuiPropertyContext ctx) {
		nestedProperties.removeLast();
	}

	@Override
	public void enterGuiAttributeSetting(@NotNull VisualBasic6Parser.GuiAttributeSettingContext ctx) {
		// Exemplo: Name = "MS Sans Serif"
		ExprContext exprContext = (ExprContext) NodeExplorer.getFirstChildClass(ctx, "ExprContext");
		String[] property = exprContext.getText().split("=");
		String keyProp = property[0];
		String valueProp = property[1];
		if (!nestedProperties.isEmpty()) {
			PropertyList p = nestedProperties.peekLast();
			p.addProperty(keyProp, valueProp);
		} else {
			((Symbol_New) getCurrentScope()).addProperty(keyProp, valueProp);
		}
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
	public void enterMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) {
		st.setScope(ctx, getCurrentScope()); // marca scope em nome AST

		// Verifica se o método é do tipo "Property Get" para criar a variável de classe
		// correspondente

		// ZE **** defVarForPropertyGet(ctx);

		String name = removeParenETypeIndicator(ctx.Name.getText())[0];
		PropertyList properties = defaultProperties();

		String methodType = ctx.methodType().getText().toUpperCase()
				.split(" ")[ctx.methodType().getText().toUpperCase().split(" ").length - 1];

		properties.addProperty(NAME, name);
		properties.addProperty(CATEGORY, PROCEDURE);
		properties.addProperty(CATEGORY_TYPE, methodType);

		properties.addProperty(SYMBOL_TYPE, SymbolType_New.METHOD);
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());

		if (ctx.ReturnType != null) {
			st.setScope(ctx.asTypeClause().type(), getCurrentScope());
			properties.addProperty(RETURN_TYPE_NAME, ctx.asTypeClause().type().getText());
			properties.addProperty("CONTEXT_TYPE", ctx.asTypeClause().type());
		} else {
			properties.addProperty(RETURN_TYPE_NAME, "Void");
		}

		ArrayList<String> modifierList = new ArrayList<String>();
		for (AccessModifierContext amc : getModifierContext(ctx)) {
			modifierList.add(amc.getText());
			if (amc.getText().equalsIgnoreCase(PUBLIC) || amc.getText().equalsIgnoreCase(GLOBAL)) {
				properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope);
				properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
			}
		}
		if (modifierList.size() > 0) {
			properties.addProperty("MODIFIER", modifierList);
		} else { // default para quando não se especifíca modifiers de escopo
			Symbol_New symModuleType = st.getCompilarionUnitSymbol(ctx);
			if (symModuleType.getProperty("COMPILATION_UNIT_NAME") != null) {
				String moduleType = (String) symModuleType.getProperty("COMPILATION_UNIT_NAME");
				if (moduleType.toUpperCase().endsWith("BAS")) {
					properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope);
					properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
				}
			}
		}

		Symbol_New sym = symbolFactory.getSymbol(properties);

		setCurrentScope(sym); // marca escopo antes da definição dos parâmeros formais (real parameters)

		st.setSymbol(ctx, sym);
		st.setSymbol(ctx.Name, sym);
	}

	@Override
	public void exitMethodDefinition(@NotNull VisualBasic6Parser.MethodDefinitionContext ctx) {
		while ((getCurrentScope() instanceof LocalScope_New)
				|| !((Symbol_New) getCurrentScope()).getProperties().hasProperty(CATEGORY, PROCEDURE)) {
			removeCurrentScope(); // REMOVE LOCAL SCOPES
		}
		removeCurrentScope();
	}

	@Override
	public void enterFormalParameters(@NotNull VisualBasic6Parser.FormalParametersContext _ctx) {
		st.setScope(_ctx, getCurrentScope());
	}

	@Override
	public void enterRealParameter(@NotNull VisualBasic6Parser.RealParameterContext _ctx) {
		st.setScope(_ctx, getCurrentScope());
	}	

	@Override
	public void exitFormalParameters(@NotNull VisualBasic6Parser.FormalParametersContext _ctx) {
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
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.LOCAL_SCOPE);
		properties.addProperty(CATEGORY, SCOPE);
		properties.addProperty(CATEGORY_TYPE, LOCAL);

		Symbol_New sym = symbolFactory.getSymbol(properties);
		setCurrentScope(sym);
	}

	@Override
	public void enterDeclareStmt(@NotNull VisualBasic6Parser.DeclareStmtContext ctx) {
		/*
		 * getCurrentScope().setDefMode(); // Se DEF_MODE is Undefined // então altera
		 * DEF_MODE para "IMPLICIT" // por que comando "Option Explicit" não foi
		 * utilizado // e porque este comamdo aqui?... Porque // a definição de método
		 */
		st.setScope(ctx, getCurrentScope());

		String name = removeParenETypeIndicator(ctx.Name.getText())[0];
		PropertyList properties = defaultProperties();

		properties.addProperty(NAME, name);
		properties.addProperty(CATEGORY, "REFERENCE");
		properties.addProperty(CATEGORY_TYPE, ctx.methodType().getText().toUpperCase());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.REFERENCE);
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());

		if (ctx.ReturnType != null) {
			st.setScope(ctx.asTypeClause().type(), getCurrentScope());
			properties.addProperty("RETURN_TYPE", ctx.asTypeClause().type().getText());
			properties.addProperty("RETURN_TYPE_NAME", ctx.asTypeClause().type().getText());

		} else {
			properties.addProperty("RETURN_TYPE", "Void");
			properties.addProperty("RETURN_TYPE_NAME", "Void");

		}

		ArrayList<String> modifierList = new ArrayList<String>();
		for (AccessModifierContext amc : getModifierContext(ctx)) {
			modifierList.add(amc.getText());
			if (amc.getText().equalsIgnoreCase("PUBLIC") || amc.getText().equalsIgnoreCase("GLOBAL")) {
				properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope);
				properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
			}
		}
		if (modifierList.size() > 0) {
			properties.addProperty("MODIFIER", modifierList);
		}

		Symbol_New sym = symbolFactory.getSymbol(properties);

		setCurrentScope(sym); // marca escopo anter da definição dos parâmeros formais

		st.setSymbol(ctx, sym);
	}
	
	public void enterGoToStmt(@NotNull VisualBasic6Parser.GoToStmtContext ctx) { 
		st.setScope(ctx, getCurrentScope());
	}

	@Override
	public void exitDeclareStmt(@NotNull VisualBasic6Parser.DeclareStmtContext ctx) {
		if (getCurrentScope().getName().startsWith("LOCAL")) { // FormalParameters
			removeCurrentScope();
		}
		removeCurrentScope();
	}

	@Override
	public void enterVariableStmt(@NotNull VisualBasic6Parser.VariableStmtContext ctx) {
		st.setScope(ctx, getCurrentScope());

		// Variáveis ReDim serão definidas em RefTypeSymbol
		if (NodeExplorer.getAncestorClass(ctx, "RedimStmtContext") != null)
			return;
		if (NodeExplorer.getNextAncestorClass(ctx, "RedimStmtContext") != null) {
			if (ctx.asTypeClause() == null)
				return; // apenas redefinição de array
		}
		createVarSymbol(ctx);
	}

	public void enterTypeDefinition(@NotNull VisualBasic6Parser.TypeDefinitionContext ctx) {
		st.setScope(ctx, getCurrentScope());
		PropertyList properties = defaultProperties();

		String name = removeParenETypeIndicator(ctx.Name.getText())[0];

		properties.addProperty(NAME, name);
		properties.addProperty(CATEGORY, STRUCTURE);
		properties.addProperty(CATEGORY_TYPE, TYPE);

		properties.addProperty(SYMBOL_TYPE, SymbolType_New.STRUCTURE);
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());

		properties.addProperty("DATA_TYPE_NAME", "UNDEFINED");

		ArrayList<String> modifierList = new ArrayList<String>();
		for (AccessModifierContext amc : getModifierContext(ctx)) {
			modifierList.add(amc.getText());
			if (amc.getText().equalsIgnoreCase(PUBLIC) || amc.getText().equalsIgnoreCase(GLOBAL)) {
				properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope);
				properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
			}
		}
		if (modifierList.size() > 0) {
			properties.addProperty("MODIFIER", modifierList);
		} else { // default para quando não se especifíca modifiers de escopo
			Symbol_New symModuleType = st.getCompilarionUnitSymbol(ctx);
			if (symModuleType.getProperty("COMPILATION_UNIT_NAME") != null) {
				String moduleType = (String) symModuleType.getProperty("COMPILATION_UNIT_NAME");
				if (moduleType.toUpperCase().endsWith("BAS")) {
					properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope);
					properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
				}
			}
		}

		Symbol_New sym = symbolFactory.getSymbol(properties);

		setCurrentScope(sym);

		st.setSymbol(ctx, sym);
	}

	public void exitTypeDefinition(@NotNull VisualBasic6Parser.TypeDefinitionContext ctx) {
		removeCurrentScope();
	}

	public void enterEnumerationDefinition(@NotNull VisualBasic6Parser.EnumerationDefinitionContext ctx) {
		st.setScope(ctx, getCurrentScope());
		PropertyList properties = defaultProperties();

		String name = removeParenETypeIndicator(ctx.Name.getText())[0];

		properties.addProperty(NAME, name);
		properties.addProperty(CATEGORY, STRUCTURE);
		properties.addProperty(CATEGORY_TYPE, ENUM);

		properties.addProperty(SYMBOL_TYPE, SymbolType_New.STRUCTURE);
		properties.addProperty(CONTEXT, ctx);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());

		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		ArrayList<String> modifierList = new ArrayList<String>();
		for (AccessModifierContext amc : getModifierContext(ctx)) {
			modifierList.add(amc.getText());
			if (amc.getText().equalsIgnoreCase(PUBLIC) || amc.getText().equalsIgnoreCase(GLOBAL)) {
				properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope());// publicScope);
				properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
			}
		}
		if (modifierList.size() > 0) {
			properties.addProperty("MODIFIER", modifierList);
		} else { // default para quando não se especifíca modifiers de escopo
			Symbol_New symModuleType = st.getCompilarionUnitSymbol(ctx);
			if (symModuleType.getProperty("COMPILATION_UNIT_NAME") != null) {
				String moduleType = (String) symModuleType.getProperty("COMPILATION_UNIT_NAME");
				if (moduleType.toUpperCase().endsWith("BAS")) {
					properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope);
					properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
				}
			}
		}

		Symbol_New sym = symbolFactory.getSymbol(properties);

		setCurrentScope(sym);

		st.setSymbol(ctx, sym);
	}

	@Override
	public void exitEnumerationDefinition(@NotNull VisualBasic6Parser.EnumerationDefinitionContext ctx) {
		removeCurrentScope();
	}
	
	@Override
	public void enterLabel(@NotNull VisualBasic6Parser.LabelContext _ctx) {
		st.setScope(_ctx, getCurrentScope());

		PropertyList properties = defaultProperties();
		String name = _ctx.start.getText();
		name = name.replace(":", "");
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.LABEL);

		properties.addProperty(CATEGORY_TYPE, "LABEL_NAME");
		properties.addProperty(CATEGORY, "LABEL");
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");
		properties.addProperty(CONTEXT, _ctx);

		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(_ctx, sym);
	}
/*
	@Override
	public void enterLabelLine(@NotNull VisualBasic6Parser.LabelLineContext _ctx) {
		st.setScope(_ctx, getCurrentScope());

		PropertyList properties = defaultProperties();
		String name = _ctx.start.getText();
		name = name.replace(":", "");
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.LABEL);

		properties.addProperty(CATEGORY_TYPE, "LABEL_NAME");
		properties.addProperty(CATEGORY, "LABEL");
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");
		properties.addProperty(CONTEXT, _ctx);

		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(_ctx, sym);
	}
*/	

/*	@Override
	public void enterLineNumber(@NotNull VisualBasic6Parser.LineNumberContext _ctx) {
		st.setScope(_ctx, getCurrentScope());

		PropertyList properties = defaultProperties();
		String name = _ctx.start.getText();
		name = name.replace(":", "");
		properties.addProperty(NAME, name);
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());
		properties.addProperty(SYMBOL_TYPE, SymbolType_New.LABEL);

		properties.addProperty(CATEGORY_TYPE, "LABEL_NUMBER");
		properties.addProperty(CATEGORY, "LABEL");
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");
		properties.addProperty(CONTEXT, _ctx);

		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(_ctx, sym);
	}*/

	@Override
	public void enterType(@NotNull VisualBasic6Parser.TypeContext ctx) {
		st.setScope(ctx, getCurrentScope());
	}

	@Override
	public void enterRedimStmt(@NotNull VisualBasic6Parser.RedimStmtContext _ctx) {
		st.setScope(_ctx, getCurrentScope());
	}

	@Override
	public void enterIdentifier(@NotNull VisualBasic6Parser.IdentifierContext _ctx) {
		st.setScope(_ctx, getCurrentScope());
	}

	@Override
	public void enterSetStmt(@NotNull VisualBasic6Parser.SetStmtContext _ctx) {
		st.setScope(_ctx, getCurrentScope());
	}

	private void createVarSymbol(VariableStmtContext varCtx) {
		PropertyList properties = defaultProperties();
		if (varCtx.Name.getText().endsWith(")")) {
			properties.addProperty(ARRAY, Integer.parseInt("0"));
		}

		String[] nameParts = removeParenETypeIndicator(varCtx.Name.getText());
		String name = nameParts[0];
		String varTypeIndicator = null;

		if (nameParts.length > 1) {
			/*
			 * Identifier type character Data type Example % Integer Dim L% & Long Dim M&
			 * 
			 * @ Decimal Const W@ = 37.5 ! Single Dim Q! # Double Dim X# $ String Dim V$ =
			 * "Secret"
			 */
			if (nameParts[1].equalsIgnoreCase("%"))
				varTypeIndicator = "Integer";
			if (nameParts[1].equalsIgnoreCase("&"))
				varTypeIndicator = "Long";
			if (nameParts[1].equalsIgnoreCase("@"))
				varTypeIndicator = "Decimal";
			if (nameParts[1].equalsIgnoreCase("!"))
				varTypeIndicator = "Single";
			if (nameParts[1].equalsIgnoreCase("#"))
				varTypeIndicator = "Double";
			if (nameParts[1].equalsIgnoreCase("$"))
				varTypeIndicator = "String";
		}

		properties.addProperty(NAME, name);
		properties.addProperty(CATEGORY_TYPE, getVarCategory(varCtx));

		if (varCtx.initialValue() != null) {
			properties.addProperty("INITIAL_VALUE", varCtx.initialValue().getText().replace("=", ""));
		}

		if (varCtx.asTypeClause() != null || varTypeIndicator != null) {
			if (varCtx.asTypeClause() != null) {
				properties.addProperty(DATA_TYPE_NAME, varCtx.asTypeClause().type().getText());
				properties.addProperty(DATA_TYPE_CONTEXT, varCtx.asTypeClause().type());
				if (NodeExplorer.getChildClass(varCtx.asTypeClause(), "IdentifierContext") != null) {
					properties.addProperty(CATEGORY_TYPE, "OBJECT");
				}
			} else {
				properties.addProperty(DATA_TYPE_NAME, varTypeIndicator);
				properties.addProperty(DATA_TYPE_CONTEXT, null);
			}
		} else {
			if (NodeExplorer.hasAncestorClass(varCtx, "EnumValueContext")) {
				properties.addProperty(DATA_TYPE_NAME, "Integer");
				properties.addProperty(DATA_TYPE_CONTEXT, null);
				properties.addProperty("DEF_MODE", "Implicit");
			} else {
				properties.addProperty(DATA_TYPE_NAME, "Variant");
				properties.addProperty(DATA_TYPE_CONTEXT, null);
				properties.addProperty("DEF_MODE", "Implicit");
			}
		}
		if (varCtx.fieldLength() != null) {
			properties.addProperty("LENGHT", varCtx.fieldLength().expr().getText());
		}

		ArrayList<String> modifierList = new ArrayList<String>();
		for (AccessModifierContext amc : getModifierContext(varCtx)) {
			modifierList.add(amc.getText());
			if (amc.getText().equalsIgnoreCase("PUBLIC") || amc.getText().equalsIgnoreCase("GLOBAL")) {
				properties.addProperty(VISIBILITY_SCOPE, st.getPublicScope()); // publicScope);
				properties.addProperty(VISIBILITY_SCOPE_NAME, st.getPublicScope().getName()); // publicScope.getName());
			}
		}

		if (modifierList.size() > 0) {
			properties.addProperty("MODIFIER", modifierList);
		} else {
			if (NodeExplorer.getAncestorClass(varCtx, "EnumerationDefinitionContext", true) != null) {
				// verifica se modifier de enclosing scope se estende à variável
				if (getCurrentScope().getProperty(VISIBILITY_SCOPE) != null) {
					properties.addProperty(VISIBILITY_SCOPE, getCurrentScope().getProperty(VISIBILITY_SCOPE)); // publicScope);
					properties.addProperty(VISIBILITY_SCOPE_NAME, getCurrentScope().getProperty(VISIBILITY_SCOPE_NAME)); // publicScope.getName());
				}
			}
		}

		properties.addProperty(CONTEXT, varCtx.Name); // ? Não sei para que serve
		properties.addProperty(ENCLOSING_SCOPE, getCurrentScope());
		properties.addProperty(ENCLOSING_SCOPE_NAME, getCurrentScope().getName());

		if (NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null) {
			properties.addProperty(PARENT_SCOPE, getCurrentScope());
			properties.addProperty(PARENT_SCOPE_NAME, getCurrentScope().getName());
		}

		properties.addProperty(SYMBOL_TYPE, SymbolType_New.VARIABLE);
		properties.addProperty(CATEGORY, SymbolType_New.VARIABLE.toString());

		Symbol_New sym = symbolFactory.getSymbol(properties);

		st.setSymbol(varCtx, sym);
		st.setSymbol(varCtx.Name, sym);
	}

	private String getVarCategory(ParserRuleContext varCtx) {
		if (NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null) {
			return TYPE;
		}
		if (NodeExplorer.getAncestorClass(varCtx, "EnumValuesContext", true) != null) {
			return ENUM_VALUE;
		}
		if (NodeExplorer.getAncestorClass(varCtx, "ConstantDefinitionContext") != null) {
			return CONSTANT;
		}
		if (NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext") != null) {
			return VARIABLE;
		}
		if (NodeExplorer.getAncestorClass(varCtx, "RedimStmtContext") != null) {
			return REDIM;
		}
		if (NodeExplorer.getAncestorClass(varCtx, "FormalParameterContext") != null) {
			return FORMAL_PARAMETER;
		} else {
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

	private List<AccessModifierContext> getModifierContext(ParserRuleContext _ctx) {
		VariableStmtContext varCtx = null;
		MethodDefinitionContext methodCtx = null;
		TypeDefinitionContext typeCtx = null;
		EnumerationDefinitionContext enumCtx = null;
		DeclareStmtContext dclCtx = null;

		if (_ctx instanceof VariableStmtContext) {
			varCtx = (VariableStmtContext) _ctx;
			return getVarModifierList(varCtx);
		} else if (_ctx instanceof MethodDefinitionContext) {
			methodCtx = (MethodDefinitionContext) _ctx;
			return methodCtx.accessModifier();
		} else if (_ctx instanceof TypeDefinitionContext) {
			typeCtx = (TypeDefinitionContext) _ctx;
			return typeCtx.accessModifier();
		} else if (_ctx instanceof EnumerationDefinitionContext) {
			enumCtx = (EnumerationDefinitionContext) _ctx;
			return enumCtx.accessModifier();
		} else if (_ctx instanceof DeclareStmtContext) {
			dclCtx = (DeclareStmtContext) _ctx;
			return dclCtx.accessModifier();
		}
		return new ArrayList<AccessModifierContext>();
	}

	private List<AccessModifierContext> getVarModifierList(VariableStmtContext varCtx) {
		if (NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null
				&& NodeExplorer.getAncestorClass(varCtx, "VariableStmtContext") != null) {
			final TypeDefinitionContext prc1 = (TypeDefinitionContext) NodeExplorer.getAncestorClass(varCtx,
					"TypeDefinitionContext");
			return prc1.accessModifier();
		}
		if (NodeExplorer.getAncestorClass(varCtx, "EnumerationDefinitionContext") != null
				&& NodeExplorer.getAncestorClass(varCtx, "VariableStmtContext") != null) {
			final EnumerationDefinitionContext prc2 = (EnumerationDefinitionContext) NodeExplorer
					.getAncestorClass(varCtx, "EnumerationDefinitionContext");
			return prc2.accessModifier();
		}
		if (NodeExplorer.getAncestorClass(varCtx, "VariableDefinitionContext") != null) {
			final VariableDefinitionContext prc4 = (VariableDefinitionContext) NodeExplorer.getAncestorClass(varCtx,
					"VariableDefinitionContext");
			return prc4.accessModifier();
		}
		return new ArrayList<AccessModifierContext>();
	}

	private PropertyList defaultProperties() {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		return properties;
	}
}