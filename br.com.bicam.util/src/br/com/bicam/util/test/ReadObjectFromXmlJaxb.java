package br.com.bicam.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.datamodel.Catalog;
import br.com.bicam.util.datamodel.CatalogItem;

public class ReadObjectFromXmlJaxb {
	public static void main (String args[]) throws Exception {
		InputStream in = new FileInputStream (new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\output\\TEMP.text"));
		Catalog c = (Catalog) BicamSystem.readObjectAsJaxbXml(in, Catalog.class);
		System.err.println(c.getItens());
		printItens(c);
	}
	
	private static void printItens(CatalogItem cItem) {
		System.err.println(cItem.getProperties());
		if(cItem.getItens() == null) return;
		for(CatalogItem item: cItem.getItens()) {
			printItens(item);
		}
	}
	
	
	
	
	
	
	
}