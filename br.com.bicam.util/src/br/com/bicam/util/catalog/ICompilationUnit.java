package br.com.bicam.util.catalog;

public interface ICompilationUnit {
	public void accept(ICompilationUnitVisitor visitor);
}
