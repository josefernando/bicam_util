package br.com.bicam.util.catalog;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Vb6ModuleSer")
public class Vb6ModuleSer extends CompilationUnitSer {
	public Vb6ModuleSer(){}
	
	public Vb6ModuleSer(String _name) {
		super(_name);
	}
}