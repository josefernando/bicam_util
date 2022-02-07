package br.com.bicam.util.catalog;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

public class ConditionaStmtControl {
    	ParserRuleContext ifStmt;
    	ParserRuleContext ifTrue;
    	Set<ConditionaStmtControl> elseIf;
    	ParserRuleContext ifFalse;
    	ParserRuleContext ifTrueLastStmt;
    	ParserRuleContext ifFalseLastStmt;

    	
    	public ConditionaStmtControl(ParserRuleContext ctx) {
    		this.ifStmt = ctx;
    		elseIf = new HashSet<ConditionaStmtControl>();
    	}
    	
    	public ParserRuleContext getIfStmt() {
    		return ifStmt;
    	}
    	
    	public void setIfTrue(ParserRuleContext _ctx) {
    		this.ifTrue = _ctx;
    	}
    	
    	public boolean hasIfTrue() {
    		return (ifTrue == null ? false : true);
    	}
    	
    	public void setIfFalse(ParserRuleContext _ctx) {
    		this.ifFalse = _ctx;
    	} 
    	
    	public boolean hasIfFalse() {
    		return (ifFalse == null ? false : true);
    	}
    	
    	public ParserRuleContext getIfTrueLastStmt() {
    		return this.ifTrueLastStmt;
    	}
    	
    	public void setIfTrueLastStmt(@NotNull ParserRuleContext _ctx) {
    		this.ifTrueLastStmt = _ctx;
    	}
    	
    	public ParserRuleContext getIfFalseLastStmt() {
    		return this.ifFalseLastStmt;
    	}   
    	
    	public void setIfFalseLastStmt(@NotNull ParserRuleContext _ctx) {
    		this.ifFalseLastStmt = _ctx;
    	}
    	
    	public void addElseIf(ParserRuleContext _elseIf){
    		elseIf.add(new ConditionaStmtControl(_elseIf));
    	}
    	
    	public ConditionaStmtControl getElseIf(ParserRuleContext _elseIf){
    		for(ConditionaStmtControl c : elseIf) {
    			if(c.getIfStmt() == _elseIf) return c;
    		}
    		return null;
    	}
    	
    	public Set<ConditionaStmtControl> getElseIf(){
    		return elseIf;
    	}
    }