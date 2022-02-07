package br.com.bicam.util.catalog;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Vb6Project")
public class Vb6ProjectSerializable extends ExecutionUnit{
	public Vb6ProjectSerializable(){}
	
	public Vb6ProjectSerializable(String _name) {
		super(_name);
	}
}