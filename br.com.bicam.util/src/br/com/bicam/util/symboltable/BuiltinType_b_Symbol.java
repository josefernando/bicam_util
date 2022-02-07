package br.com.bicam.util.symboltable;

import br.com.bicam.parser.constant.PropertyName;
import br.com.bicam.util.PropertyList;

public class BuiltinType_b_Symbol  extends Scopeb_Symbol implements Type, PropertyName{

	public BuiltinType_b_Symbol(PropertyList _properties) {
		super(_properties);
		addProperty(TYPE, this);
	}
}
