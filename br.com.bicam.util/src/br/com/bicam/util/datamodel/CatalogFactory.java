package br.com.bicam.util.datamodel;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import br.com.bicam.util.InputRepository;
import br.com.bicam.util.PropertyList;

public class CatalogFactory { 
	
	static PropertyList properties;
	static Set<String> repositories;
	
	public static CatalogDao create(PropertyList _properties){
		properties = _properties;
		String type = (String)_properties.getProperty("CATALOG_DAO_TYPE");
		switch (type){
		case "JAXB":
			try {
				return jaxb(CatalogDaoJaxb.class, _properties);
			} catch (Exception e) {
				e.printStackTrace();
			}
		default:	 return null;
		}
	}
	
	private static CatalogDaoJaxb jaxb(Class cls, PropertyList _properties) throws Exception {
		String fileName = (String)_properties.getProperty("DATA_SOURCE_LOCATION");
		if(fileName == null) return emptyCatalogDao(cls);
		
		File xmlFile = new File(fileName);
        
        CatalogDaoJaxb catalog = (CatalogDaoJaxb) readObjectAsXmlFrom(new FileReader(xmlFile.getAbsolutePath()), cls);
        return catalog;
	}
    
    private static <T> T readObjectAsXmlFrom(Reader reader, Class<T> c) throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(c);
        XMLStreamReader xmlReader =
        XMLInputFactory.newInstance().createXMLStreamReader(reader);
        Unmarshaller xmlInterpreter = jaxb.createUnmarshaller();
        return xmlInterpreter.unmarshal(xmlReader, c).getValue();
      }
    
    private static CatalogDaoJaxb emptyCatalogDao(Class cls) {
    	CatalogDaoJaxb c = null;
		try {
			c = (CatalogDaoJaxb) Class.forName(cls.getName()).getConstructor(PropertyList.class).newInstance(properties);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
    	return c;
    }
    
    public static void main(String args[]) {
    	PropertyList properties = new PropertyList();
    	properties.addProperty("CATALOG_DAO_TYPE", "JAXB");
    	properties.addProperty("CATALOG_ID", "PRODUCAO");
    	properties.addProperty("CATALOG_NAME", "PRODUCAO_NAME");
    	properties.addProperty("DATA_SOURCE_LOCATION", "C:\\Temp\\DATASOURCE\\PRODUCAO.xml");      	
    	CatalogDaoJaxb catalog = (CatalogDaoJaxb)CatalogFactory.create(properties);
    	
    	int i = catalog.getRepositories().size();
    	
    	HashSet<String> repositories = new HashSet<String>(){{
    		add("SEGUROS");
    		add("SHARED");
    	}};
    	
    	properties.addProperty("CATALOG_REPOSITORIES", repositories);   
    	catalog.initRepositories(properties);

    	InputRepository in = new InputRepository(new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\cargaVb6Project.text"));
    	
//    	catalog.load(in);
    	catalog.commit();
    	
    	System.err.println(catalog.getRepositories());
    }
}