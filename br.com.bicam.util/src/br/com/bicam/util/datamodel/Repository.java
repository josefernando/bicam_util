package br.com.bicam.util.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Repository extends CatalogItem{ 

	public Repository() {}
	public Repository(String _id) {
		super(_id);
	}
}