package br.com.bicam.util;

import java.util.ArrayList;
import java.util.List;

import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class ContextData_New {
	IScope_New scope;
	Symbol_New symbol;
	TerminalValue terminalValue;
	List<Object> contextValues;

	String type;       	     // tipo da variavel: String, int, long
	boolean model;
	boolean view;
	boolean control;
	PropertyList properties;
	/*	 
	 * Na expressão " 1 + 2.5", 1 (int) será promovido para 1.0 (double)
	 */
	String promotionType; 	
	/*
   	 Ex: msg = 1 + " OK"
     então resultType de msg será String
     */
	String resultType;
	
	public ContextData_New(){
		properties = new PropertyList();
		List<Object> contextValues = new ArrayList<Object>();
	}
	
	public List<Object> getContextValues() {
		return contextValues;
	}
	
	public void addContextValue(Object _contextValue) {
		this.contextValues.add(_contextValue);
	}
	
	public Object getContextValue(int _ix) {
		return contextValues.get(_ix);
	}
	
	public IScope_New getScope() {
		return scope;
	}
	public void setScope(IScope_New scope) {
		this.scope = scope;
	}

	public Symbol_New getSymbol() {
		return symbol;
	}
	public void setSymbol(Symbol_New symbol) {
		this.symbol = symbol;
	}
	
	public TerminalValue getTerminalValue() {
		return terminalValue;
	}
	public void setTerminalValue(TerminalValue _terminalValue) {
		this.terminalValue = _terminalValue;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPromotionType() {
		return promotionType;
	}
	public void setPromotionType(String promotionType) {
		this.promotionType = promotionType;
	}
	public String getResultType() {
		return resultType;
	}
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	
	public PropertyList getProperties(){
		return properties;
	}
}
