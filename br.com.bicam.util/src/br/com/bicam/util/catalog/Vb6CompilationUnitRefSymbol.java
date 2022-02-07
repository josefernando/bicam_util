package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CATEGORY_TYPE;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.DEF_MODE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.EXPLICIT;
import static br.com.bicam.util.constant.PropertyName.GLOBAL_SCOPE;
import static br.com.bicam.util.constant.PropertyName.IMPLICIT;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.AttributeStmtContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.WithStmtContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.ApplicationComponent;
import br.com.bicam.util.datamodel.Version;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class Vb6CompilationUnitRefSymbol extends VisualBasic6BaseListener{
//	String absoluteFileName; // nome do diret�rio do projeto VB6
	
	SymbolTable_New     st;
	Deque<IScope_New>   scopes;
	Deque<IScope_New>   withStmtScope;
	IScope_New 		    globalScope;
	
	String vbName;
	Version version;
	ApplicationComponent appComponent;
	
//	CompilationUnit compilationUnit;


	SymbolFactory       symbolFactory;
	PropertyList 		properties;
	Symbol_New 			stubObjeto = null;  // apenas para efeito de testes de cria��o objetos
	
	String             optionExplicit;
	
	KeywordLanguage keywordLanguage;
	
	PropertyList currentProperties;
	
	final String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$"};
	
	public Vb6CompilationUnitRefSymbol(PropertyList _propertyList){
		this.scopes          = new ArrayDeque<IScope_New>();
		this.withStmtScope   = new ArrayDeque<IScope_New>();
		this.properties      = _propertyList;
		this.st              = (SymbolTable_New)properties.getProperty(SYMBOLTABLE);
		this.symbolFactory   = (SymbolFactory)properties.getProperty(SYMBOL_FACTORY);
		this.keywordLanguage = (KeywordLanguage)properties.getProperty(KEYWORD_LANGUAGE);
		this.globalScope     = (IScope_New)properties.getProperty(GLOBAL_SCOPE);
		this.vbName = (String) properties.getProperty("COMPONENT_ID");
		this.appComponent = (ApplicationComponent) properties.getProperty("APPLICATION_COMPONENT");
		this.version = (Version) properties.getProperty("VERSION");
		
		this.optionExplicit = getOptionExplicit();
	}
	
	public SymbolTable_New getSymbolTable(){
		return st;
	}
/*	public void exitStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
//		String fileName = compilationUnit.getFileName();;
//		Symbol_New symCompilationUnit = ((List<Symbol_New>)st.getSymbolByProperty(FILE_NAME, fileName)).get(0);
		
		ParseTreeWalker walker = new ParseTreeWalker();
        SymbolGraphBuilder_New graphBuild = null;
		try {
			graphBuild = new SymbolGraphBuilder_New(st);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ParseTree astree = (ParseTree) version.getProperties().getProperty("AST");		
//		ParseTree tree = (ParseTree) compilationUnit.getProperties().getProperty("AST");

		walker.walk(graphBuild, astree);		
		SymbolGraph sg = null;
		try {
			sg = new SymbolGraph((File)compilationUnit.properties.getProperty("FILE"), null, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		((Symbol_New)symCompilationUnit).addProperty(SYMBOL_GRAPH, sg);

		try {
			sg = new SymbolGraph(version.getInputStream(), null, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		version.getProperties().addProperty(SYMBOL_GRAPH, sg);
	}*/	
	
	@Override 
	public void exitIdentifier(@NotNull VisualBasic6Parser.IdentifierContext ctx) {
		if(!isSymbolToResolve(ctx)) return;
		Symbol_New scope = (Symbol_New)st.getScope(ctx);
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
		PropertyList properties = defaultProperties(ctx);
		
		String name = ctx.getText();
		if(name.startsWith(".")) {
			WithStmtContext wContext = (WithStmtContext)NodeExplorer.getAncestorClass(ctx, "WithStmtContext");
			name = wContext.identifier().getText() + name;
		}
		if(name.startsWith("!")) { // ex.: strPhone = !PhoneNumber & ""
			WithStmtContext wContext = (WithStmtContext)NodeExplorer.getAncestorClass(ctx, "WithStmtContext");
			name = wContext.identifier().getText() + name.replace("!", ".");
			properties.addProperty(CATEGORY_TYPE, "RECORDSET_FIELD");
		}
		
		name = name.replace("!", ".");  //  ex.: GetNextCustID = mobjRst!MaxID + 1
		
		name = removeParenETypeIndicator(name);
		properties.addProperty(NAME, name);
		
		if(NodeExplorer.hasAncestorClass(ctx, "AttributeStmtContext")) {
			AttributeStmtContext context = (AttributeStmtContext)NodeExplorer.getAncestorClass(ctx, "AttributeStmtContext");
			properties.addProperty(DEF_MODE, "BUILTIN_ATTRIBUTE");
			properties.addProperty("VALUE", context.expr(0).getText());
		}
		
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
		st.setSymbol(ctx, sym);
	}
    
    private String getOptionExplicit() {
    	if(optionExplicit != null) return optionExplicit;
  	  try {
// 		 String	fileName = compilationUnit.getFileName();;
 		 String REGEX = "^Option\\s+Explicit$";
         Pattern p = Pattern.compile(REGEX);
// 		  BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
	     InputStreamReader r = new InputStreamReader(version.getInputStream());
		 BufferedReader in = new BufferedReader(r); 		  
           for (String line = in.readLine(); line != null; line = in.readLine()) {	 
               Matcher m = p.matcher(line);
         	  if((m.matches())) {
         		 optionExplicit = EXPLICIT;
         	  }
           }
           in.close();
 	  }
 	  catch (IOException e){
 			  e.printStackTrace();
 	  }
  	  optionExplicit = IMPLICIT;
  	  return optionExplicit;
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
	
	private boolean isSymbolToResolve(ParserRuleContext _ctx){
		if(NodeExplorer.hasSibling(_ctx, "IdentifierContext")) return false;
		if(st.getSymbol(_ctx) != null) return false; // Symbol j� definido. Deve ser definiton Stmt

		if(!NodeExplorer.hasAncestorClass(_ctx, "StmtContext")) {
			if(NodeExplorer.getAncestorClass(_ctx, "AttributeStmtContext") != null); // do nothing
			else return false;
		}
		
		IdentifierContext childId = null;
		try { // DEBUG
		childId = (IdentifierContext)NodeExplorer.getFirstChildClass(_ctx, "IdentifierContext");
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		
		if(childId != null && st.getSymbol(childId) != null){
			st.setSymbol(_ctx, st.getSymbol(childId));
			return false;
		}		
		return true;
	}	
	
	private PropertyList defaultProperties(ParserRuleContext _ctx) {
		PropertyList properties = new PropertyList();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(SYMBOLTABLE, st);
		properties.addProperty(SYMBOL_FACTORY, symbolFactory);
		properties.addProperty(CONTEXT, _ctx);
		properties.addProperty(DEF_MODE, getOptionExplicit());
		properties.addProperty(ENCLOSING_SCOPE, st.getScope(_ctx));
		return properties;
	}
}