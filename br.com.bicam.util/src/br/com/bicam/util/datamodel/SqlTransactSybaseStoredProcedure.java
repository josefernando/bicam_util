package br.com.bicam.util.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import br.com.bicam.util.symboltable.Symbol_New;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class SqlTransactSybaseStoredProcedure extends ApplicationComponent{
	public SqlTransactSybaseStoredProcedure() {}
	
	public SqlTransactSybaseStoredProcedure(String _name) {
		super(_name);
	}
	public SqlTransactSybaseStoredProcedure(Symbol_New _symbol) {
		super(_symbol);
	}

	@Override
	public void bindSymbol() {
	}
}