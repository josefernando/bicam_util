package br.com.bicam.util.datamodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import br.com.bicam.util.BicamSystem;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Version extends CatalogItem{
	public Version() {}
	public Version(String _id) {
		super(_id);
	}
	
	public InputStream getInputStream() throws IOException {
		String dataSourceType = (String) getProperties().getProperty("DATA_SOURCE_TYPE");
		String dataSourceLocation = (String) getProperties().getProperty("DATA_SOURCE_LOCATION");
		
		InputStream is = null;
		switch(dataSourceType) {
		case "FILE" : 
			  is = new FileInputStream(new File(dataSourceLocation));
			break;
		case "URL"  : 
			URL url = new URL(dataSourceLocation);
            URLConnection site = url.openConnection();
            is     = site.getInputStream();
			break;
		default:
			BicamSystem.printLog("ERROR", "Invalid DATA SOURCE TYPE: " + dataSourceType);
			break;
		}
		return is;
	}
	
	public String toString() {
		return getParent().toString() + "/" + getId();
	}
}