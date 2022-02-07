package br.com.bicam.util.catalog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.VariableStmtContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType;
import br.com.bicam.util.symboltable.IScope;
import br.com.bicam.util.symboltable.SymbolTableFactory;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;
import br.com.bicam.util.symboltable.Type;
import br.com.bicam.util.symboltable.WhereUsed;

public class RefTypeVb6 extends VisualBasic6BaseListener{
	SymbolTable_b 		st;
	Deque<IScope> 		scopes;
	IScope globalScobe;
	SymbolTableFactory  symbolFactory;
	Object 				compUnit;
	PropertyList properties;

	LinkedList<PropertyList> nestedProperties;
	
	IScope currentScope;
	IScope compilationUnitScope;
	
	KeywordLanguage keywordLanguage;
	
	final String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$"};
	
	public RefTypeVb6(PropertyList _propertyList){
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
	
/*	public void enterGuiDefinition(@NotNull VisualBasic6Parser.GuiDefinitionContext ctx){
		PropertyList _properties = new PropertyList();
		_properties.addProperty("SYMBOLTABLE", st);
		_properties.addProperty("CLASS_TO_RESOLVE", IScope.class);
		_properties.addProperty("CONTEXT_TO_RESOLVE", ctx);

		for(Symbol_b sym : st.getSymbolByProperty("CATEGORY","UI")){
			String nameType = (String)sym.getProperty("CONTROL");
			Type type = (Type)st.getScope(ctx).resolve(nameType, _properties);
			st.getSymbol(ctx).setType(type);
		}
	}*/
	
	@Override
	public void enterVariableStmt(@NotNull VisualBasic6Parser.VariableStmtContext ctx) {
		if(NodeExplorer.getAncestorClass(ctx, "RedimStmtContext") == null) return;
		String name = removeParenETypeIndicator(ctx.Name.getText());
		
		PropertyList _properties = new PropertyList();
		_properties.addProperty("SYMBOLTABLE", st);
		_properties.addProperty("CLASS_TO_RESOLVE", IScope.class);
		_properties.addProperty("CONTEXT_TO_RESOLVE", ctx);

		if(st.getScope(ctx).resolve(name, _properties) == null)
			createVarSymbol(ctx);
	}
	
	@Override 
	public void enterType(@NotNull VisualBasic6Parser.TypeContext ctx) {
//		if(NodeExplorer.getNextAncestorClass(ctx, "AsTypeClauseContext") != null) return; // GuiDefinition

		Type type = null;
		String memberAcess = null;
		if(ctx.getText().contains(".")) memberAcess = "\\.";
		if(ctx.getText().contains("!")) memberAcess = "!";

		PropertyList _properties = new PropertyList();
		_properties.addProperty("SYMBOLTABLE", st);
		_properties.addProperty("CLASS_TO_RESOLVE", IScope.class);
		_properties.addProperty("MEMBER_ACCESS_OPER", memberAcess);
		_properties.addProperty("CONTEXT_TO_RESOLVE", ctx);

		String name = ctx.instance() == null ? ctx.getText() : ctx.instance().identifier().getText();
		
		if(ctx.identifier() != null){
			if(ctx.identifier().referenceOper() != null){
				name = ctx.identifier().identifier().getText();
			}
		}
		
		if(memberAcess != null){
			try{
				type = (Type)st.getScope(ctx).resolveMember(name, _properties);

			} catch (NullPointerException e) {
				System.err.println("** SYMBOL NOT RESOLVED at Line L  In Comp Unit C " 
			            + " S = " + name 
			            + " L = " + ctx.start.getLine()
			            + " C = " + st.getCompilarionUnitSymbol(ctx).getName());
				return;
			}
//			type = (Type)st.getScope(ctx).resolveMember(name, _properties);
			
			System.err.format("Type %s in \"%s\"  at line %d  resolved to symbol \"%s\"%n", name
                    , st.getCompilarionUnitSymbol(ctx).getName()
                    , ctx.start.getLine()
                    , st.getScope(ctx).resolveMember(name,_properties));
		}
		else {
			try{
		type = (Type)st.getScope(ctx).resolve(name,_properties);
			} catch (ClassCastException e){
				e.printStackTrace();
				int debugOnly = 0;
			}
		System.err.format("Type %s in \"%s\"  at line %d  resolved to symbol \"%s\"%n", name
				                                         , st.getCompilarionUnitSymbol(ctx).getName()
				                                         , ctx.start.getLine()
				                                         , st.getScope(ctx).resolve(name,_properties));
		}
		setType(ctx, type);
	}
	
	private void setType(ParserRuleContext _ctx, Type _type){
		if(_type == null){
			try{
				throw new IllegalArgumentException("** Type IS NULL at Line L  In Comp Unit C " 
			            + " S = " + _ctx.getText() 
			            + " L = " + _ctx.start.getLine()
			            + " C = " + st.getCompilarionUnitSymbol(_ctx).getName()); 
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		ParserRuleContext asClauseCtx = null;
		ParserRuleContext idOrExpr = null;
		 asClauseCtx = NodeExplorer.getAncestorClass(_ctx, "AsTypeClauseContext");
		 if(asClauseCtx == null) return; //?
		 idOrExpr = NodeExplorer.getFirstSibling(asClauseCtx, "IdentifierContext");
		if(idOrExpr == null){
			idOrExpr = NodeExplorer.getFirstSibling(asClauseCtx, "ExprContext");
		}
		
		if(st.getSymbol(idOrExpr) == null){
			if(NodeExplorer.getDepthFirstChildClass(idOrExpr, "IdentifierContext") != null){
				idOrExpr = (ParserRuleContext) NodeExplorer.getDepthFirstChildClass(idOrExpr, "IdentifierContext");
			}
		}
		
		if(idOrExpr == null){
			try{
				throw new Exception("*** ERROR: ClauseType Not Found for type t  at line L in Comp Unit C: " 
			     + " t = " +  _type.getName() 
			     + " L = " + _ctx.start.getLine()
			 	 + " C = " +  st.getCompilarionUnitSymbol(_ctx).getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		if(st.getSymbol(idOrExpr) == null){
			if(NodeExplorer.getAncestorClass(idOrExpr, "RedimStmtContext") != null){
				try{
					throw new Exception("** SYMBOL REDIM WITH CLAUSE TYPE at Line L  In Comp Unit C " 
				            + " S = " + idOrExpr.getText() 
				            + " L = " + idOrExpr.start.getLine()
				            + " C = " + st.getCompilarionUnitSymbol(idOrExpr).getName()); 
				} catch (Exception e) {
					e.printStackTrace();
				}
				return; //TO DO incluir where Used here
			}
		}
		
		st.getSymbol(idOrExpr).setType(_type);
		
		addTypeToDictionary(st.getSymbol(idOrExpr), (Symbol_b)_type); 
		
		Symbol_b symType = (Symbol_b)_type;
		if(symType.getProperty("WHERE_USED") == null){
			symType.addProperty("WHERE_USED", SymbolTableFactory.whereUsedDefault());
		}
		WhereUsed used = (WhereUsed)symType.getProperty("WHERE_USED");
		used.add(_ctx);
	}
	
	private void addTypeToDictionary(Symbol_b _sym , Symbol_b _type){
		PropertyList properties = _sym.getProperties();
//		properties.addProperty("CATEGORY_TYPE", _type.getProperty("CATEGORY"));
		properties.addProperty("CATEGORY_TYPE", _sym.getType().getName());

/*		String name = "ALIAS_" + _sym.getName();
		if(_type.getName().equalsIgnoreCase("CONNECTION")){
			properties.addProperty("NAME","ALIAS_" + _sym.getName());
			properties.addProperty("CATEGORY",SymbolType.DB.toString());
			properties.addProperty("CATEGORY_TYPE",SymbolType.DB_CONNECTION.toString());
			properties.addProperty("ALIAS_FROM",_sym);
			st.addDbSymbol(symbolFactory.getSymbol(properties));
		}
		if(_type.getName().equalsIgnoreCase("RECORDSET")){
			properties.addProperty("NAME","ALIAS_" + _sym.getName());
			properties.addProperty("CATEGORY",SymbolType.DB.toString());
			properties.addProperty("CATEGORY_TYPE",SymbolType.DB_RECORDSET.toString());
			properties.addProperty("ALIAS_FROM",_sym);
			st.addDbSymbol(symbolFactory.getSymbol(properties));
		}
		if(_type.getName().equalsIgnoreCase("COMMAND")){
			properties.addProperty("NAME","ALIAS_" + _sym.getName());
			properties.addProperty("CATEGORY",SymbolType.DB.toString());
			properties.addProperty("CATEGORY_TYPE",SymbolType.DB_COMMAND.toString());
			properties.addProperty("ALIAS_FROM",_sym);
			st.addDbSymbol(symbolFactory.getSymbol(properties));
		}	*/	
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
	
	private void createVarSymbol(VariableStmtContext varCtx){
		String name = removeParenETypeIndicator(varCtx.Name.getText());
		
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME",name);
		properties.addProperty("CATEGORY_TYPE","VARIABLE");
		
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

		if(modifierList.size() > 0){
			properties.addProperty("MODIFIER", modifierList);
		}
		properties.addProperty("CONTEXT",varCtx.Name);
		properties.addProperty("ENCLOSING_SCOPE", st.getScope(varCtx));	
		
		if(NodeExplorer.getAncestorClass(varCtx, "TypeDefinitionContext") != null){
			properties.addProperty("SYMBOL_TYPE", SymbolType.STRUCT);
			properties.addProperty("CATEGORY", SymbolType.STRUCT.toString());
		}
		else {
			properties.addProperty("SYMBOL_TYPE", SymbolType.VARIABLE);
			properties.addProperty("CATEGORY", SymbolType.VARIABLE.toString());
		}
		
		Symbol_b sym = symbolFactory.getSymbol(properties);
		
		
//		define(getCurrentScope(), sym, false);
		
		setGlobal(modifierList,sym);

		st.setSymbol(varCtx, sym);
		st.setSymbol(varCtx.Name, sym);
	}
	
	private boolean setGlobal(ArrayList<String> _modifierList, Symbol_b _sym){
		boolean isGlobal = false;
		if(_modifierList.size() > 0){
			for(String modifier :  _modifierList){
				if(modifier.equalsIgnoreCase("Public"))
					isGlobal = true;
				if(modifier.equalsIgnoreCase("Global"))
					isGlobal = true;
			}
			if(isGlobal) st.getGlobalScope().define(_sym);
		}
		return isGlobal;
	}	
}