package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.FILE;

import java.io.File;
import java.io.IOException;

import br.com.bicam.util.PropertyList;

public abstract class CompilationUnit implements ICompilationUnit{
//	File file;
	String name;
	PropertyList properties;
	
	public CompilationUnit(PropertyList _properties, String ..._name) {
		if(_name.length > 0) name = _name[0];
		properties = _properties;
		setFile( (File) properties.getProperty(FILE));
	}
	
	public String getName() {
		return name;
	}

	public File getFile() {
		return (File)getProperties().getProperty(FILE);
	}

	public void setFile(File _file) {
		getProperties().addProperty(FILE, _file);
	}
	
	public String getFileName() {
		try {
			return getFile().getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public PropertyList getProperties() {
		return properties;
	}
	
	public String toString() {
		return "CompilationUnit={Module Name=" + properties.getProperty("MODULE_NAME");
	}
}