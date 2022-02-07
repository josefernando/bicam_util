package br.com.bicam.util.catalog;

//https://en.wikipedia.org/wiki/Visitor_pattern#Java_example
public interface ICompilationUnitVisitor {
	void visit (CompilationUnitVb6 compUnitVb6);	
	void visit (CompilationUnitSqlTransactSybase compUnitSqlTransactSybase);
}
