package br.com.bicam.util;

import java.util.ArrayList;
import java.util.List;

import br.com.bicam.util.jaxb.symboltable.SymbolJaxb;
import br.com.bicam.util.symboltable.IScope;

public class ContextInformation {
	IScope scope;
	SymbolJaxb symbol;
	TerminalValue terminalValue;
	List<Object> contextValues;

	SymbolJaxb appCategory;    // infrastructure, business, control, etc... 
	SymbolJaxb appType;        // infrastructure: performance, dao or net. business: client, product
	String type;       	   // tipo da variavel: String, int, long
	boolean model;
	boolean view;
	boolean control;
	Proprietary properties;
	/*	 
	 * Na expressão " 1 + 2.5", 1 (int) será promovido para 1.0 (double)
	 */
	String promotionType; 	
	/*
   	 Ex: msg = 1 + " OK"
     então resultType de msg será String
     */
	String resultType;
	
	public ContextInformation(){
		properties = new Proprietary();
		List<Object> contextValues = new ArrayList<Object>();

	}
	
	public SymbolJaxb getAppCategory() {
		return appCategory;
	}
	public void setAppCategory(SymbolJaxb appCategory) {
		this.appCategory = appCategory;
	}
	public SymbolJaxb getAppType() {
		return appType;
	}
	public void setAppType(SymbolJaxb appType) {
		this.appType = appType;
	}
	public boolean isModel() {
		return model;
	}
	public void setModel(boolean model) {
		this.model = model;
	}
	public boolean isView() {
		return view;
	}
	public void setView(boolean view) {
		this.view = view;
	}
	public boolean isControl() {
		return control;
	}
	public void setControl(boolean control) {
		this.control = control;
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
	
	public IScope getScope() {
		return scope;
	}
	public void setScope(IScope scope) {
		this.scope = scope;
	}

	public SymbolJaxb getSymbol() {
		return symbol;
	}
	public void setSymbol(SymbolJaxb symbol) {
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
	
	public Proprietary getProperties(){
		return properties;
	}
}
