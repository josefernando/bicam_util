package br.com.bicam.util.symboltable;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.parser.constant.PropertyName;
import br.com.bicam.util.PropertyList;

public abstract class Symbol_b  implements PropertyName{
	private PropertyList properties;

	public Symbol_b(PropertyList _properties){
  		properties = _properties;
  		setProperties();
	}
	
	/*
	 * Este método deve listar os fields necessários na criação do object
	 */
	private void setProperties(){
			((IScope) properties.getProperty(ENCLOSING_SCOPE)).define(this);
	}

	public String getName(){
		return (String) getProperties().getProperty(NAME);
	}

	public Type getType(){
		if(properties.getProperty(TYPE) instanceof Type)
			return (Type) properties.getProperty(TYPE);
	    return null;
	}

	public void setType(Type _type){
		properties.addProperty(TYPE, _type);
	}
	
	public IScope getEnclosingScope(){
		return (IScope) properties.getProperty(ENCLOSING_SCOPE);
	}
	
	public ParserRuleContext getContext(){
		return (ParserRuleContext) properties.getProperty(CONTEXT);
	}
	
	public void addProperty(String propertyDescriptionP, Object valueP) {
		properties.addProperty(propertyDescriptionP, valueP);
	}
	
	public void removeProperty(String propertyDescriptionP) {
		properties.removeProperty(propertyDescriptionP);
	}
	
	public Object getProperty(String propertyDescriptionP) {
		return properties.getProperty(propertyDescriptionP);
	}
	
	public boolean hasProperty(String _key, String _val) {
		return properties.hasProperty(_key, _val);
	}

	public PropertyList getProperties() {
		return properties;
	}
	
	public Symbol_b getAnchorSymbol() { // primeiro symbol criado para simbol especifico
		return (Symbol_b)properties.getProperty("ANCHOR_SYMBOL");
	}
	
	public void setAnchorSymbol(Symbol_b _sym) { // primeiro symbol criado para simbol especifico
		properties.addProperty("ANCHOR_SYMBOL", _sym);
	}
	
	public String toString() {
		return getName() + "=" + getProperties().toString() + System.lineSeparator();
	}	
}