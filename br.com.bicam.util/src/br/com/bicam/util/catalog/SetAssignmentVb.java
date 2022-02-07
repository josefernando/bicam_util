package br.com.bicam.util.catalog;

import java.util.ArrayList;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VisualBasic6BaseListener;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.IdentifierContext;
import br.com.bicam.parser.visualbasic6.VisualBasic6Parser.StmtContext;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;

public class SetAssignmentVb extends VisualBasic6BaseListener {
	SymbolTable_b st;
	public  SetAssignmentVb(SymbolTable_b _st)  {
		this.st = _st;
	}
	
	public void enterAssignmentStmt(@NotNull VisualBasic6Parser.AssignmentStmtContext ctx) {
		IdentifierContext lhs = (IdentifierContext)NodeExplorer.getDepthFirstChildClass(ctx, "IdentifierContext");
        Symbol_b sym = st.getSymbol(lhs);
        
        if(sym == null){
        	try {
				throw new Exception("*** ERROR SYMBOL (S) NOT FOUND in COMP_UNIT(C) at Line (L)" 
						+ "S: " + lhs.getText() 
						+ "C: " + st.getCompilarionUnitSymbol(ctx).getName() 
						+ "L: " + lhs.start.getLine());
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
        }
        
        if(sym.getProperty("ASSIGNMENT_LIST") == null){
        	sym.addProperty("ASSIGNMENT_LIST", new ArrayList<>());
        	sym.addProperty("ASSIGNMENT_LINE_LIST", new ArrayList<>());
        }
        
//        stmt -> assigmentStmt, ou stmt -> setStmt -> Set 
        StmtContext stmtContext = (StmtContext) NodeExplorer.getAncestorClass(ctx, "StmtContext");
         ((ArrayList<Integer>)sym.getProperty("ASSIGNMENT_LIST")).add(stmtContext.start.getStartIndex());
         ((ArrayList<Integer>)sym.getProperty("ASSIGNMENT_LINE_LIST")).add(stmtContext.start.getLine());
	}
}
