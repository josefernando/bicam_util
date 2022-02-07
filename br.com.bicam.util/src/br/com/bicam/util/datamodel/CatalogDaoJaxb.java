package br.com.bicam.util.datamodel;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.InputRepository;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.PropertyListSerializable;
import br.com.bicam.util.catalog.Repository;
import br.com.bicam.util.catalog.Source;
import br.com.bicam.util.catalog.SourceVersion;

//@XmlType(propOrder = {"id", "name", "properties", "repositories"})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class CatalogDaoJaxb implements CatalogDao{
	String id;
	String name;
	String dataSourceLocation;
	PropertyListSerializable properties;
	Set<Repository> repositories; // sempre use LIST para jaxb, ArrayList não funciona
	Repository currentRepository;
	
	public CatalogDaoJaxb() {} // jaxb required

	public CatalogDaoJaxb(PropertyList _properties) {
		properties = _properties.getPropertyListSerializable();
		this.id = (String)properties.getProperty("CATALOG_ID");
		this.name = (String)properties.getProperty("CATALOG_NAME");
		this.dataSourceLocation = (String)properties.getProperty("DATA_SOURCE_LOCATION");
		repositories = new HashSet<Repository>();
	}	
	
	public PropertyListSerializable getProperties() {
		return properties;
	}
	
	public String getId() {
		return this.id;
	}
	
	@XmlElement
	public void setId(String _id) {
		this.id = _id;
	}	
	
	public String getName() {
		return this.id;
	}
	
	@XmlElement
	public void setName(String _name) {
		this.name = _name;
	}

	public Object getProperty(String _key) {
		return getProperties().getProperty(_key);
	}
	
	@XmlElement
	public void setProperties(PropertyListSerializable _properties) {
		properties = _properties;
	}	
	
	public Set<Repository> getRepositories() {
		return repositories;
	}
	
	@XmlElement
	public void setRepositories(Set<Repository> _repositories) {
		this.repositories = _repositories; 
	}
	
	public void addRepository(Repository _repository) {
		getRepositories().add(_repository);
	}
	
	public void removeRepository(Repository _repository) {
		getRepositories().remove(_repository);
	}
	
	public Repository getRepository(String _id) {
		for(Repository r : getRepositories()) {
			if(r.getName().equalsIgnoreCase(_id)) return r;
		}
		return null;
	}
	
	public void load(InputRepository _inputRepository, String..._separator) {
		String separator = ",";
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
		InputRepository in = _inputRepository;

		String catalogId = in.readLine().split(separator)[1];
		if(!getId().equalsIgnoreCase(catalogId)) {
			try {
				throw new Exception();
			} catch (Exception e) {
				BicamSystem.printLog("ERROR", "Invalid catalog name: must be '" + getName() + "'", e);
			}			
		}

		String repositoryName = in.readLine().split(separator)[1];
		
		setCurrentRepository(repositoryName);
	
		while (in.hasNextLine()) {
			String line = in.readLine();
			String[] parts = line.split(separator);
			if(parts.length == 2) {
				setCurrentRepository(parts[1]);
				continue;
			}
			Source source = currentRepository.getSource(parts[0]);

			if(source == null) {
				try {
					String className = parts[1];
					String constArg = parts[0];

					source = (Source) Class.forName(className).getConstructor(String.class).newInstance(parts[0]);
                    currentRepository.addSource(source);
        			InputRepository inputRepository = new InputRepository(new File(parts[3]));
        			
        			SourceVersion sv = new SourceVersion(inputRepository, source.getName());
        			source.addVersion(sv);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
					BicamSystem.printLog("ERROR", " class '" + parts[1] + "'", e);
				}
			}
		}
	}

	private Repository setCurrentRepository(String _name) {
		currentRepository = getRepository(_name);
		if(currentRepository == null) {
			try {
				throw new Exception();
			} catch (Exception e) {
				BicamSystem.printLog("ERROR", "Repository '" + _name + "'does not exist'" , e);
			}			
		}
		return currentRepository;
	}
	
	public void initRepositories(PropertyList _properties) {
		Set<String> repositoryNames = (Set<String>) _properties.getProperty("CATALOG_REPOSITORIES");
		if(repositoryNames == null || repositoryNames.isEmpty()) {
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
			addRepository(r);
		}
	} 	
	
	public void commit()  {
        if(dataSourceLocation == null) {
        	dataSourceLocation = "c:/temp/DATASOURCE/" + getId() + ".xml";
        }
        
        File xmlFile = new File(dataSourceLocation); 
      try {
        JAXBContext jc = null;
		jc = JAXBContext.newInstance(this.getClass());

        Marshaller marshaller = null;

		marshaller = jc.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(this, System.out);
        marshaller.marshal(this, new PrintWriter(xmlFile));
      } catch (Exception e) {
    	  e.printStackTrace();
      }
	}	
}