package br.com.bicam.util;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.symboltable.SymbolTable_New;

public class PrintLog {
	public static  void printLog(SymbolTable_New _st,ParserRuleContext _ctx, String _severity, String _msg) {
		System.err.format("*** %s - %s - at line %d in compilation unit %s%n",
				   _severity, _msg,  _ctx.start.getLine(),_st.getCompilationUnitName(_ctx));
		if(_severity.equalsIgnoreCase("ERROR")) System.exit(1);
	}
}
