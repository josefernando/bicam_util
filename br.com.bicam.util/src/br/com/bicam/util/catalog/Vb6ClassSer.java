package br.com.bicam.util.catalog;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Vb6ClassSer")
public class Vb6ClassSer extends CompilationUnitSer {
	public Vb6ClassSer(){}
	
	public Vb6ClassSer(String _name) {
		super(_name);
	}
}