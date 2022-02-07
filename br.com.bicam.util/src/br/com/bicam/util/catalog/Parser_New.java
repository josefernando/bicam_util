package br.com.bicam.util.catalog;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.tree.ParseTree;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public abstract class Parser_New {
	File file;
	ParseTree ast;
	
	public Parser_New(File _file){
		this.file = _file;
			parse();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public ParseTree getAST() {
		return ast;
	}
	
	protected abstract void parse() ;
	
	public abstract SymbolTable_New accept(DefSymbolVisitor_New _visitor, 	PropertyList _properties);

	
	public abstract SymbolTable_New accept(RefTypeSymbolVisitor_New _visitor, 	PropertyList _properties);
	
	public abstract SymbolTable_New accept(RefSymbolVisitor_New _visitor, 	PropertyList _properties);
	
	/*public abstract SymbolTable_New accept(SetAssignmentVisitor_New _visitor, 	PropertyList _properties)
			throws IOException;	*/

}
