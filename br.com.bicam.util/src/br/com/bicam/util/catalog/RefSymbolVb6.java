package br.com.bicam.util.catalog;



import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.WithStmtContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.IScope;
import br.com.bicam.util.symboltable.SymbolList;
import br.com.bicam.util.symboltable.SymbolTableFactory;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;
import br.com.bicam.util.symboltable.WhereUsed;

public class RefSymbolVb6 extends VisualBasic6BaseListener{
	SymbolTable_b 		st;
	Deque<IScope> 		scopes;
	IScope globalScobe;
	SymbolTableFactory  symbolFactory;
	Object 				compUnit;
	PropertyList properties;
	boolean isfield;

	LinkedList<PropertyList> nestedProperties;
	
	IScope currentScope;
	IScope compilationUnitScope;
	
	KeywordLanguage keywordLanguage;
	
	final String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$"};
	
	public RefSymbolVb6(PropertyList _propertyList){
		scopes = new ArrayDeque<IScope>();
		this.properties = _propertyList;
		setProperties();
	}
	
	private void setProperties(){
		this.st = (SymbolTable_b) properties.getProperty("SYMBOLTABLE");
		this.keywordLanguage = (KeywordLanguage)properties.getProperty("KEYWORD_LANGUAGE");
		this.symbolFactory = (SymbolTableFactory)properties.getProperty("SYMBOL_FACTORY");
	}
	
	public SymbolTable_b getSymbolTable(){
		return st;
	}
	
	private boolean isSymbolToResolve(ParserRuleContext _ctx){
		if(NodeExplorer.hasSibling(_ctx, "IdentifierContext")) return false;
		if(NodeExplorer.getSibling(_ctx, "MemberAccessOperContext") != null) return false;
		if(st.getSymbol(_ctx) != null) return false; // Symbol já definido. Deve ser definiton Stmt
		if(!NodeExplorer.hasAncestorClass(_ctx, "StmtContext")) return false;
		return true;
	}
	
	public String getCompUnitName(){
		return compilationUnitScope.getName();
	}
	public void enterStartRule(@NotNull VisualBasic6Parser.StartRuleContext ctx) {
		compilationUnitScope = (IScope)st.getSymbol(ctx);
	}
		
	@Override 
	public void exitIdentifier(@NotNull IdentifierContext ctx) {
		if(!isSymbolToResolve(ctx)) return;
		if(NodeExplorer.getAncestorClass(ctx, "AttributeStmtContext") != null) return;
		
		IdentifierContext childId = (IdentifierContext)NodeExplorer.getFirstChildClass(ctx, "IdentifierContext");

		if(childId != null && st.getSymbol(childId) != null){
			st.setSymbol(ctx, st.getSymbol(childId));
			return;
		}
		
		Symbol_b  sym = null;
		String memberAcess = null;
		String memberAcess1 = ".";

		if(ctx.getText().contains(".")){
			memberAcess = "\\.";
		}
		if(ctx.getText().contains("!")){
			memberAcess = "!";
			isfield = true;
		}
		
		PropertyList _properties = new PropertyList();
		_properties.addProperty("SYMBOLTABLE", st);
		_properties.addProperty("CLASS_TO_RESOLVE", IScope.class);
		_properties.addProperty("MEMBER_ACCESS_OPER", memberAcess);	
		_properties.addProperty("CONTEXT_TO_RESOLVE", ctx);
		_properties.addProperty("DEF_MODE", (String) compilationUnitScope.getProperty("DEF_MODE"));


		if(memberAcess != null){
			if(ctx.getText().startsWith(memberAcess) || ctx.getText().startsWith(memberAcess1)){
				String[] groupQualified = getFullQualifiedInWithStmt(ctx);
				String qualifiedName = groupQualified[0] +  ctx.getText();
				String name = removeParenETypeIndicator(qualifiedName,ctx);
				sym = (Symbol_b)st.getScope(ctx).resolveMember(name,_properties);
				try{
				sym.addProperty("FULLNAME", qualifiedName);
				} catch (NullPointerException e){
					System.err.println("** SYMBOL S NULL at Line L  In Comp Unit C " 
				            + " S = " + name 
				            + " L = " + ctx.start.getLine()
				            + " C = " + st.getCompilarionUnitSymbol(ctx).getName());
				    e.printStackTrace();
				}
				
			}
			else {
				try{
				String name = removeParenETypeIndicator(ctx.getText(), ctx);
/*				if(name.contains("Me.")){
					String xThis = st.getCompilarionUnitSymbol(ctx).getName();
					name.replace("Me.", xThis);
				}*/
				sym = (Symbol_b)st.getScope(ctx).resolveMember(name,_properties);
				}
				catch (NullPointerException e){
					int debugOnly = 0;
				}
			}
			if(sym == null){
				try{
					throw new Exception("** SYMBOL S NOT FOUND at Line L  In Comp Unit C " 
				            + " S = " + ctx.getText() 
				            + " L = " + ctx.start.getLine()
				            + " C = " + st.getCompilarionUnitSymbol(ctx).getName()); 
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			else {
			System.err.format("Symbol %s in \"%s\"  at line %d  resolved to symbol \"%s\"%n", ctx.getText()
                    , st.getCompilarionUnitSymbol(ctx).getName()
                    , ctx.start.getLine()
                    , sym.getName());
			}
		    if(isfield){
		    	sym.addProperty("CATEGORY_TYPE", "RecordsetField");
		    	isfield = false;
		    }
		}
		else {
	    String name = removeParenETypeIndicator(ctx.getText(),ctx);
		try{
		sym = st.getScope(ctx).resolve(name,_properties);
		} catch (NullPointerException e){
			System.err.println("** SYMBOL S NOT FOUND at Line L  In Comp Unit C " 
		            + " S = " + name 
		            + " L = " + ctx.start.getLine()
		            + " C = " + st.getCompilarionUnitSymbol(ctx).getName());
		}
		
		if(sym == null){
			Symbol_b scopeSym = st.getCompilarionUnitSymbol(ctx);
			if(scopeSym.getProperty("DEF_MODE") != null
					&& !((String)scopeSym.getProperty("DEF_MODE")).equalsIgnoreCase("EXPLICIT")){
				Symbol_b symx = st.getScope(ctx).createVariantSymbol(name, st.getScope(ctx));
				st.setSymbol(ctx, symx);
				setUsed(symx, ctx);
				return;
			}
			try{
				throw new Exception("** SYMBOL S NOT FOUND at Line L  In Comp Unit C " 
			            + " S = " + ctx.getText() 
			            + " L = " + ctx.start.getLine()
			            + " C = " + st.getCompilarionUnitSymbol(ctx).getName()); 
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		else
		System.err.format("Symbol %s in \"%s\"  at line %d  resolved to symbol \"%s\"%n", ctx.getText()
				                                         , st.getCompilarionUnitSymbol(ctx).getName()
				                                         , ctx.start.getLine()
				                                         , sym.getName());
		}
		
		st.setSymbol(ctx, sym);
		setUsed(sym, ctx);
	}
	
	private void setUsed(Symbol_b _sym, ParserRuleContext _ctx){
		WhereUsed used = (WhereUsed)_sym.getProperty("WHERE_USED");
		Object fullName = _sym.getProperty("FULLNAME");

		if( used == null){
			used = new WhereUsed();
			_sym.addProperty("WHERE_USED", used);
		}
		used.add(_ctx);
		
		if( fullName != null){
			IdentityHashMap<ParserRuleContext,String> withGroup = null;
			if(_sym.getProperty("WITH_GROUP") == null) {
				withGroup = new IdentityHashMap<ParserRuleContext,String>();
				_sym.addProperty("WITH_GROUP", withGroup);
			}
			else withGroup = (IdentityHashMap<ParserRuleContext,String>)_sym.getProperty("WITH_GROUP");
			withGroup.put(_ctx, (String)fullName);
			_sym.removeProperty("FULLNAME");
		}
	}
	
	private String removeParenETypeIndicator(String _name, ParserRuleContext _ctx){
		String name = removeParen(removeTypeIndicator(_name, _ctx));
		if(name.equals("Me")){
			name = getThisName(name, _ctx);
		}
		
		try {
		String[] nameParts = name.split("\\.");

		StringBuffer newName = new StringBuffer();
		
		if(nameParts.length > 1){
			for(String n : nameParts){
				if(n.equals("Me")){
					n = getThisName(name, _ctx);
				}
				if(newName.length() == 0) newName.append(n);
				else newName.append("." + n);
			}
			name = newName.toString();
		}
		}
		catch (NullPointerException e){
			int ddbuonly = 0;
		}
		return name;
	}
	
	private String getThisName(String _name, ParserRuleContext _ctx){
		IScope scope = (IScope)st.getCompilarionUnitSymbol(_ctx);
		for(SymbolList symList : scope.getMembers().values()){
			for(Symbol_b symMember : symList.getSymbols()){
				if(symMember.hasProperty("CONTROL", "Form") || symMember.hasProperty("CONTROL", "MDIForm")){
					return symMember.getName();
				}
			}
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
	
	private String removeTypeIndicator(String name, ParserRuleContext _ctx) {
		for (String s : sulfixTypeIndicator){
			if(name.endsWith(s)){
				name = name.replace(s, "");
				break;
			}
			else {
				if(name.startsWith(s)){ // trata identificador de arquivo #arquivo
					name = name.replace(s, "");
					break;
				}				
			}
		}
		
/*		if(name.equalsIgnoreCase("Me")){
			name = st.getSymbolByProperty("CONTROL", "Form").get(0).getName();
		}
		
		if(name.indexOf("Me.") > -1){
			String me = st.getSymbolByProperty("CONTROL", "Form").get(0).getName();
			name.replace("Me.", me);
		}*/
		return name;
	}	
	
	private String[] getFullQualifiedInWithStmt(ParserRuleContext _ctx){
		String[] ret = new String[2];
		 WithStmtContext withContext = (WithStmtContext)NodeExplorer.getAncestorClass(_ctx, "WithStmtContext");
		 if(withContext == null){
				try{
					throw new Exception("*** ERROR: withStatement not found for expression " + _ctx.getText() 
				 	+ " at line " + _ctx.start.getLine());
				} catch (Exception e) {
					e.printStackTrace();
				}				 
			 return  null;
		 }
		 else {
			 IdentifierContext identifierContext = (IdentifierContext)NodeExplorer.getFirstChildClass(withContext, "IdentifierContext");
			 if(identifierContext == null){
					try{
						throw new Exception("*** ERROR: IdentifierContext not found for expression " + _ctx.getText() 
					 	+ " at line " + _ctx.start.getLine());
					} catch (Exception e) {
						e.printStackTrace();
					}				 
				 return  null;
			 }
			 else {
				 ret[0] = identifierContext.getText();
				 ret[1] = Integer.toString(withContext.start.getLine());
				 return ret;
			 }
		 }		
	}
}