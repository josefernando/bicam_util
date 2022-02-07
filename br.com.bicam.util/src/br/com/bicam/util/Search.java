package br.com.bicam.util;

import static br.com.bicam.util.constant.PropertyName.CONTEXT;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.WHERE_USED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;
import br.com.bicam.util.symboltable.WhereUsed;



public class Search {
	
	public  static Map<Symbol_New, List<Location>> byName(SymbolTable_New _st, String _name){
		 List<Symbol_New> symbols  = _st.getSymbolByProperty(NAME, _name);
		 Map<Symbol_New, List<Location>> symlocations = new HashMap<Symbol_New, List<Location>>();
		 for(Symbol_New sym : symbols) {
			 List<Location> locations = new ArrayList<Location>();
			 symlocations.put(sym,locations);
			 if(sym.getProperty(WHERE_USED) == null) continue;
			 WhereUsed whereUsed = (WhereUsed)sym.getProperty(WHERE_USED);
			 for(ParserRuleContext ctx: whereUsed.used()) {
				 Location location = new Location(_name, ctx);
				 location.setCompilationUnit(_st.getCompilarionUnitSymbol(ctx).getName());
				 Symbol_New symNextCollableProcedure = _st.getProcedureSymbol(ctx);
				 ParserRuleContext ctxNextCollableProcedure = (ParserRuleContext) symNextCollableProcedure.getProperty(CONTEXT);
				 location.setNextCallableProcedure(new Location(symNextCollableProcedure.getName(), ctxNextCollableProcedure) );
			 }
		 }
		return symlocations;
	}
}
