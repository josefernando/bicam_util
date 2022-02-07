package br.com.bicam.util.catalog;

import br.com.bicam.util.PropertyList;

public class MetadataProjectVb6 extends Metadata{

	public MetadataProjectVb6(PropertyList _properties) {
		super(_properties);
	}
	
	@Override
	public void accept(MetadataVisitor visitor) {
		visitor.visit(this);
	}
}