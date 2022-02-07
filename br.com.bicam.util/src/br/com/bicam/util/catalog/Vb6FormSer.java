package br.com.bicam.util.catalog;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Vb6Form")
public class Vb6FormSer extends CompilationUnitSer {
	public Vb6FormSer(){}
	
	public Vb6FormSer(String _name) {
		super(_name);
	}
}