package br.com.bicam.util.datamodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.InputRepository;
import br.com.bicam.util.PropertyList;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Catalog extends CatalogItem {
	private static final String  CATALOG = "CATALOG";
	private static final String  REPOSITORY = "REPOSITORY";
	private static final String  SOURCE = "SOURCE";
	
	private static final String  ID = "ID";
	
	private static final String  DATA_SOURCE_TYPE = "DATA_SOURCE_TYPE";
	private static final String  DATA_SOURCE_LOCATION = "DATA_SOURCE_LOCATION";
	private static final String  COMPILATION_UNIT_TYPE = "COMPILATION_UNIT_TYPE";

	private static final String  ERROR = "ERROR";
//	private static final String  WARNING = "WARNING";
	
	Repository currentRepository;
	String separator;

	public Catalog() {}

	public Catalog(String _id) {
		super(_id);
	}
	
	public void initRepositories(PropertyList _properties) {
		Set<String> repositoryNames = (Set<String>) _properties.getProperty("INIT_REPOSITORIES");
		if(repositoryNames == null || repositoryNames.isEmpty()) {
			BicamSystem.printLog("WARNING", "NOT FOUND ANY REPOSITORY");
			return;
		}
		
		for(String name :repositoryNames ) {
	    	Repository r = null;
			try {
				r = (Repository) Class.forName(Repository.class.getName()).getConstructor(String.class).newInstance(name);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			addItem(r);
		}
	}
	
	private void catalog(String _line) {
		//==== Start Valida input parameters
		String[] parts = _line.split(separator);
		if(parts.length != 2) {
			try {
				throw new Exception();
			}catch(Exception e) {
				BicamSystem.printLog("ERROR", "Invalid input. '" + _line + "'", e);
			}
		}
		//==== end Valida input parameters
		if(!getId().equals(_line.split(separator)[1])) {
			BicamSystem.printLog(ERROR, 
					new String().format("Invalid catatalog id: '%s'", _line.split(separator)[1]));
			}
	}
	
	private void repository(String _line) {
		//==== Start Valida input parameters
		String[] parts = _line.split(separator);
		if(parts.length != 2) {
			try {
				throw new Exception();
			}catch(Exception e) {
				BicamSystem.printLog("ERROR", "Invalid input. '" + _line + "'", e);
			}
		}
		//==== end Valida input parameters
		PropertyList properties = new PropertyList();
		properties.addProperty("ID", _line.split(separator)[1]);
		currentRepository = (Repository)getItem(properties);
		if(currentRepository == null) {
			BicamSystem.printLog(ERROR, 
					new String().format("Repository '%s' not found ", _line.split(separator)[1]));
		}
	}	
	
	private void source(String _line) {
		String[] parts = _line.split(separator);
		//==== Start Valida input parameters
		if(parts.length != 5) {
			try {
				throw new Exception();
			}catch(Exception e) {
				BicamSystem.printLog("ERROR", "Invalid input. '" + _line + "'", e);
			}
		}
		//==== End Valida input parameters
		PropertyList srcProperties  = new PropertyList();
		srcProperties.addProperty("ID", parts[1]);
		Source source = (Source)currentRepository.getItem(srcProperties);
		if(source == null) {
			source = new Source(parts[1]);
			source.getProperties().addProperty("TYPE", parts[2]);
		}
		
        currentRepository.addItem(source);
        InputStream is;
		String hash = null;
		
		try {
			is = new FileInputStream(new File(parts[4]));
			hash = BicamSystem.getInputStreamHash(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Version v = new Version(hash);
		source.addItem(v);
		
		PropertyList verProperties = new PropertyList();
		verProperties.addProperty(COMPILATION_UNIT_TYPE, parts[2]);

		verProperties.addProperty(DATA_SOURCE_TYPE, parts[3]);
		verProperties.addProperty(DATA_SOURCE_LOCATION, parts[4]);
		v.addProperties(verProperties);
	}	
	
//	public void load(InputRepository _inputRepository, String..._separator) {
	public void load(InputStream _is, String..._separator) {
		separator = ",";
		if(_separator.length > 0) {
			if(Character.isJavaIdentifierStart(_separator[0].charAt(0))
					|| Character.isJavaIdentifierStart(_separator[0].charAt(0))) {
				try {
					throw new InvalidParameterException();
				} catch (InvalidParameterException e) {
					BicamSystem.printLog("ERROR", "Invalid parameter: '" + _separator[0].charAt(0) + "' separator cannot be java identifier character", e);
				}				
			}
		}
		InputRepository in = new InputRepository(_is);

		while(in.hasNextLine()) {
			String line = in.readLine();
			String catalogItem = line.split(separator)[0];
			
			switch (catalogItem) {
			case CATALOG: catalog(line);
						  		break;
			case REPOSITORY: repository(line);
			  			  		break;
			case SOURCE: source(line);
			             		break; 
			default:
				BicamSystem.printLog(ERROR, 
						new String().format("Invalid catatalog type: '%s'. Must be 'CATALOG', 'REPOSITORY', 'SOURCE'", catalogItem));
			}
		}
	}	
	
    public Repository getRepository(PropertyList _properties) {
    	String id = (String)_properties.getProperty(ID);
    	CatalogItem repository = getItem().get(id);
    	
    	return (Repository)repository;
    }
	
	public static void main(String args[]) throws FileNotFoundException {
    	PropertyList properties = new PropertyList();
    	String catalogId = "PRODUCAO";
    	properties.addProperty("CATALOG_NAME", "PRODUCAO");
    	properties.addProperty("LOAD_LOCATION", "C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\loadCatalog.txt"); 
    	Catalog catalog = new Catalog(catalogId);
    	PropertyList initProperties = new PropertyList();
    	
    	Set<String> initRepositories = new HashSet<String>() {{
    		add("SEGUROS");
    		add("SHARED");
    	}};
    	
    	initProperties.addProperty("INIT_REPOSITORIES", initRepositories);
    	catalog.initRepositories(initProperties);
    	File f = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\loadCatalog.txt");
    	InputStream is = new FileInputStream(f);
    	catalog.load(is);
    	f = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\loadCatalogSTProcedure.txt");
    	is = new FileInputStream(f);
    	catalog.load(is);    	
		Repository rep = (Repository) catalog.getItem().get("SEGUROS");
    	try {
    		PropertyList xmlProperties = new PropertyList();
    		xmlProperties.addProperty("XML_FILE", new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\output" + "\\" + "TEMP.text"));
			BicamSystem.toXml(catalog, xmlProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.err.println(catalog.getItens());
//    	System.err.println(catalog.getInvertedList());
	}
}