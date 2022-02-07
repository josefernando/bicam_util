package br.com.bicam.util.catalog;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.tree.ParseTree;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

/*
 * Metadata
 * Estruturas de dados de definem dados.
 * Ex.: Schema de BD, Arquivo VBP Vb6, 
 * 
 * 
 */
public abstract class Metadata implements IMetadata{
//	String id;
	String name;
	File file;
	ParseTree ast;
	PropertyList properties;
	SymbolTable_New st;
	
	public Metadata(PropertyList _properties, String ..._name) {
//		this.id = (String)_properties.getProperty("ID");
		if(_name.length > 0 ) this.name = _name[0];
		this.properties = _properties;
		st = (SymbolTable_New)_properties.getProperty("SYMBOLTABLE");
	}
	
	public String getName() {
		return (String)getProperties().getProperty("NAME");
	}

	public File getFile() {
		return file;
	}

	public void setFile(File _file) {
		this.file = _file;
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
	
	public void setAST(ParseTree _abstractSyntaxTree) {
		this.ast = _abstractSyntaxTree;
	}
	
	public PropertyList getProperties() {
		return properties;
	}
	
	public void setProperties(PropertyList _properties) {
		this.properties = _properties;
	}
	
	public SymbolTable_New getSymbolTable() {
		return st;
	}
}