package br.com.bicam.util.symboltable;

import br.com.bicam.util.PropertyList;

public class GlobalScope_b  extends Base_b_Scope{

	public GlobalScope_b(PropertyList _propertyList) {
		super(_propertyList);
		_propertyList.addProperty(NAME, "GLOBAL");
	}
}
