package br.com.bicam.util.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import br.com.bicam.util.symboltable.Symbol_New;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Vb6Object extends ApplicationComponent{
	public Vb6Object() {}
	
	public Vb6Object(String _name) {
		super(_name);
	}
	public Vb6Object(Symbol_New _symbol) {
		super(_symbol);
	}

	@Override
	public void bindSymbol() {
	}
}