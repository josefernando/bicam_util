package br.com.bicam.util.catalog;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.tree.ParseTree;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_b;

public abstract class Parser {
	File file;
	ParseTree ast;
	
	public Parser(File _file){
		this.file = _file;
		try {
			parse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public String getFileName() {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ParseTree getAST() {
		return ast;
	}
	
	protected abstract void parse() throws IOException;
	
	public abstract SymbolTable_b accept(DefSymbolVisitor _visitor, 	PropertyList _properties)
			throws IOException;
	
	public abstract SymbolTable_b accept(RefTypeSymbolVisitor _visitor, 	PropertyList _properties)
			throws IOException;
	
	public abstract SymbolTable_b accept(RefSymbolVisitor _visitor, 	PropertyList _properties)
			throws IOException;
	
	public abstract SymbolTable_b accept(SetAssignmentVisitor _visitor, 	PropertyList _properties)
			throws IOException;	

}
