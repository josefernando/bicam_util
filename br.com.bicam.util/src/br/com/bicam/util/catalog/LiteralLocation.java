package br.com.bicam.util.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class LiteralLocation extends SqlTransactSybaseBaseListener {
	
	SymbolTable_New	st;
	IScope_New		globalScobe;
	PropertyList 	properties;
	String          separator;
	StringBuffer    sb;
//	CompilationUnit compUnit;
//	File compUnitFile;

	Symbol_New compilationUnitSymbol;
	
	String arg;
	
	Map<String,List<ParserRuleContext>> locationList;
	
	public LiteralLocation(PropertyList _properties) {
		this.properties = _properties;
		this.st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");
//		compUnit = (CompilationUnit)properties.getProperty("COMPILATION_UNIT");
//		compUnitFile = compUnit.getFile();
		locationList = new HashMap<String,List<ParserRuleContext>>();
	}
	
	public void setArg(String _arg, Boolean... _append) {
		this.arg = _arg;
		if(_append.length > 0 && _append[0]) locationList.clear();
		
		locationList.put(arg, new ArrayList<ParserRuleContext>());
	}
	
	public Map<String, List<ParserRuleContext>> getLocation() {
		return locationList;
	}
	
	@Override 
	public void enterStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		compilationUnitSymbol = st.getSymbol(ctx);
	}
	
	@Override 
	public void exitStartRule(@NotNull SqlTransactSybaseParser.StartRuleContext ctx) {
		for(Entry entry : locationList.entrySet()) {
			List<ParserRuleContext> list = (List<ParserRuleContext>) entry.getValue();
			for(ParserRuleContext context : list) {
				BicamSystem.printLog(st, context, "INFO", "Encontrado ->" +arg+ "<-");
			}
		}
	}	
	
	@Override 
	public void enterStringLiteral(@NotNull SqlTransactSybaseParser.StringLiteralContext ctx) {
		if(ctx.getText().toUpperCase().contains(arg.toUpperCase())) {
			List list = locationList.get(arg);
			list.add(ctx);
		}
	}
}
