package br.com.bicam.util.catalog;

import br.com.bicam.util.PropertyList;

public class CompilationUnitVb6 extends CompilationUnit{

	public CompilationUnitVb6(PropertyList _properties) {
		super(_properties);
	}

	@Override
	public void accept(ICompilationUnitVisitor visitor) {
		visitor.visit(this);
	}
}