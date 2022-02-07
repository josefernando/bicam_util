package br.com.bicam.util.catalog;

import br.com.bicam.util.PropertyList;

public class CompilationUnitSqlTransactSybase extends CompilationUnit{

	public CompilationUnitSqlTransactSybase(PropertyList _properties) {
		super(_properties);
	}

	@Override
	public void accept(ICompilationUnitVisitor visitor) {
		visitor.visit(this);
	}
}