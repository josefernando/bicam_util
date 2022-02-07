package br.com.bicam.util.symboltable;

import javax.naming.OperationNotSupportedException;

import br.com.bicam.util.PropertyList;

public class GlobalScope_New extends VirtualSymbol{

	public GlobalScope_New(PropertyList _properties) {
		super(_properties);
	}
	public Type_New getType(){
		try {
			throw new  OperationNotSupportedException();
		}catch (OperationNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public void setType( Type_New _type){
		try {
			throw new  OperationNotSupportedException();
		}catch (OperationNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}	
}
