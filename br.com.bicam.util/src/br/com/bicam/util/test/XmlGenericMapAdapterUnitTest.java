package br.com.bicam.util.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.jaxb.XmlGenericMapAdapter;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class XmlGenericMapAdapterUnitTest {
	Map<String,Integer> stringInteger;
	Map<String,List<String>> stringListString;
	
	public XmlGenericMapAdapterUnitTest() {
		stringInteger = new HashMap<String,Integer>(){{
			put("A",1);
			put("B",2);
			put("C",3);
		}};
		stringListString = new HashMap<String,List<String>>(){{
			put("AR1",new ArrayList() {{add("AR11");}});
		}};		
	}

	public Map<String, Integer> getStringInteger() {
		return stringInteger;
	}

	@XmlJavaTypeAdapter(XmlGenericMapAdapter.class)
	public void setStringInteger(Map<String, Integer> stringInteger) {
		this.stringInteger = stringInteger;
	}

	public Map<String, List<String>> getStringListString() {
		return stringListString;
	}

	@XmlJavaTypeAdapter(XmlGenericMapAdapter.class)
	public void setStringListString(Map<String, List<String>> stringListString) {
		this.stringListString = stringListString;
	}
	
	public static void main(String args[]) throws Exception {
		XmlGenericMapAdapterUnitTest unitTest = new XmlGenericMapAdapterUnitTest();
        File xmlFile = new File("c:/temp/jaxb1.xml"); 
        PropertyList p = new PropertyList();
        p.addProperty("XML_FILE", xmlFile);
		BicamSystem.toXml(unitTest,p);
	}	
}