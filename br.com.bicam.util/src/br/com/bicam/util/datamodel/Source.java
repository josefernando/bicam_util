package br.com.bicam.util.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Source extends CatalogItem{
	public Source() {}
	public Source(String parts) {
		super(parts);
	}
}