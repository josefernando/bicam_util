package br.com.bicam.util.catalog;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;

public class GetVbName extends VisualBasic6BaseListener{
	
	String vbName;
	
	@Override 
	public void enterAttributeStmt(@NotNull VisualBasic6Parser.AttributeStmtContext ctx) {
		String key = ctx.identifier().getText();
		if(key.equalsIgnoreCase("VB_NAME")){
			vbName = 	ctx.expr(0).getText();
		}		
	}
	
	public String getVbName(){
		return vbName;
	}
}