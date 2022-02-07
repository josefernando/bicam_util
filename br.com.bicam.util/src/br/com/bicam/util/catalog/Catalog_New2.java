package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.parser.visualbasic6.VbpLexer;
import br.com.bicam.parser.visualbasic6.VbpParser;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.GetThingByParameter;
import br.com.bicam.util.InputRepository;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.KeywordVB6;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.PropertyListSerializable;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

@XmlType(propOrder = {"name", "properties", "repositorys"})
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Catalog_New2 {
	String name;
	PropertyListSerializable properties;
	Set<Repository> repositorys; // sempre use LIST para jaxb, ArrayList não funciona
	Repository currentRepository;
	
	public Catalog_New2() {} // jaxb required

	public Catalog_New2(String _name) {
		this.name = _name;
		repositorys = new HashSet<Repository>();
	}	
	
	public PropertyListSerializable getProperties() {
		return properties;
	}
	
	public String getName() {
		return name;
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
	
	public Set<Repository> getRepositorys() {
		return repositorys;
	}
	
	@XmlElement
	public void setRepositorys(Set<Repository> _repositorys) {
		this.repositorys = _repositorys; 
	}
	
	public void addRepository(Repository _repository) {
		getRepositorys().add(_repository);
	}
	
	public void removeRepository(Repository _repository) {
		getRepositorys().remove(_repository);
	}
	
	public Repository getRepository(String _name) {
		for(Repository r : getRepositorys()) {
			if(r.getName().equalsIgnoreCase(_name)) return r;
		}
		return null;
	}
	
	public void loadSource(Object fileOrUrl, String..._separator) {
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
		InputRepository in = null;
		if(File.class.isInstance(fileOrUrl)) {
			in = new InputRepository((File)fileOrUrl);
		}
		else if(URL.class.isInstance(fileOrUrl)) {
			in = new InputRepository((URL)fileOrUrl);
		}
		else {	
			try {
				throw new InvalidParameterException();
			} catch (InvalidParameterException e) {
				BicamSystem.printLog("ERROR", "Invalid parameter: must be 'FILE' or 'URL' type", e);
			}
		}
		
		String catalogName = in.readLine().split(separator)[1];
		if(!getName().equalsIgnoreCase(catalogName)) {
			try {
				throw new Exception();
			} catch (Exception e) {
				BicamSystem.printLog("ERROR", "Invalid catalog name: must be '" + getName() + "'", e);
			}			
		}

		String repositoryName = in.readLine().split(separator)[1];
		
		setCurrentRepository(repositoryName);
		
/*		Repository repository = getRepository(repositoryName);
		if(repository == null) {
			try {
				throw new Exception();
			} catch (Exception e) {
				BicamSystem.printLog("ERROR", "Repository '" + repositoryName + "'does not exist'" , e);
			}			
		}*/	
		
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
/*					Class c = Class.forName(className);
				    Class[] paramTypes = {String.class };
				    Constructor cons = c.getConstructor(paramTypes);
                    Object[] args = {constArg };
                    Object theObject = cons.newInstance(args);*/					
					
					source = (Source) Class.forName(className).getConstructor(String.class).newInstance(parts[0]);
                    currentRepository.addSource(source);
        			InputRepository inputRepository = new InputRepository(new File(parts[3]));
        			
        			SourceVersion sv = new SourceVersion(inputRepository, source.getName());
        			source.addVersion(sv);

					//					source = CompilationUnitSer.class.getConstructor(String.class).newInstance(parts[0]);
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
	
	private static void xml(Object obj) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(obj.getClass());

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(obj, System.out);
        
        File xmlFile = new File("c:/temp/jaxb1.xml"); 
//        File xmlFile2 = new File("c:/temp/jaxb2.xml"); 

        marshaller.marshal(obj, new PrintWriter(xmlFile));
        marshaller.marshal(obj, new PrintWriter(System.out));
        marshaller.marshal(obj, new PrintWriter(System.err));
        
        Object copy = (Object) readObjectAsXmlFrom(new FileReader(xmlFile.getAbsolutePath()), obj.getClass());

  	    System.out.println("=================>> \n\n\n");
  	    marshaller.marshal(copy, new PrintWriter(System.out));
  	    
  	    System.err.println("*************************** " + obj.getClass().getName());
  	    System.err.println(obj);
  	    System.err.println("=========================== COPY");
  	    System.err.println(copy); 
	}
    
    private static <T> T readObjectAsXmlFrom(Reader reader, Class<T> c) throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(c);

        XMLStreamReader xmlReader =
          XMLInputFactory.newInstance().createXMLStreamReader(reader);

        Unmarshaller xmlInterpreter = jaxb.createUnmarshaller();

        return xmlInterpreter.unmarshal(xmlReader, c).getValue();
      }
  
    private static File getSourceInRepository(Catalog_New2 _catalog, String _repository, String _source) {
    	Repository rep = _catalog.getRepository(_repository);
    	Source src = rep.getSource(_source);
    	SourceVersion ver = src.getVersions().get(0);
    	String sourceName = (String)ver.getProperties().getProperty("INPUT_LOCATION");
    	File file = new File(sourceName);
    	return file;
    }
       
    private static Source getExecutionUnit(Catalog_New2 _catalog, String _repository, String _source) {
    	Repository rep = _catalog.getRepository(_repository);
    	Source src = rep.getSource(_source);
    	SourceVersion ver = src.getVersions().get(0);
    	String sourceName = (String)ver.getProperties().getProperty("INPUT_LOCATION");
    	return src;
    }
    
    
/*    public SourceVersion getSourceVersion(String _id, Repository[] repositorys) {
    	int i = 0;
    	while(i < repositorys.length) {
    		SourceVersion sourceVersion = repositorys[i++].getSource(_name).getVersions().get(0);
    		if(sourceVersion.getId().equals(_id)) return sourceVersion;
    	}
    }*/
    
/*	private CompilationUnit createCompUnit(String _fileName) {
	    System.err.println("*** Compilation Unit File Name " + _fileName);
	    
	    PropertyList propertiesX = new PropertyList();
	    
		KeywordVB6  keywordLanguage = new KeywordVB6();
		propertiesX.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		
		SymbolFactory symbolfactory = new SymbolFactory();
		propertiesX.addProperty(SYMBOL_FACTORY, symbolfactory);

		propertiesX.addProperty(SYMBOLTABLE, st);
		propertiesX.addProperty("FILE", new File(_fileName));
		
	    compUnits.add(new CompilationUnitVb6(propertiesX));	
	    return compUnits.getLast();
	}*/   
    
    
	private static ParseTree parseProjectVbp(File _file) {
		ParseTree ast;
		InputStream is = null;
        ANTLRInputStream input = null;

		try {
			is = new FileInputStream(_file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			input = new ANTLRInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        VbpLexer lexer = new VbpLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VbpParser parser = new VbpParser(tokens);
        
        parser.removeErrorListeners();
        parser.addErrorListener(new VerboseListener());

        parser.getInterpreter().setPredictionMode(PredictionMode.SLL); 
        try { 
        	ast = parser.startRule(); 
        } 
        catch (Exception ex) { 
             	System.err.format("*** WARNING: re-parsing with  'PredictionMode.LL' %n%n");
                tokens.reset(); // rewind input stream 
                parser.reset(); 
                parser.getInterpreter().setPredictionMode(PredictionMode.LL); 
                ast = parser.startRule(); 
        }
        
        if(parser.getNumberOfSyntaxErrors() > 0){
        	System.err.format(" ERRORS - %d errors during parsing process%n", parser.getNumberOfSyntaxErrors());
        }
        return ast;
	}
	public static void main(String[] args) throws Exception {
//	   	File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TIPMA00");
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\bank_vb_project"); 
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0"); 

//INICIO =========================== CARREGA CATÁLOGO E REPOSITÓRIOS 
			Catalog_New2 catalog = new Catalog_New2("PRODUCAO");
			Repository repository = new Repository("SEGUROS");
			repository.addProperty("AREA", "SINISTRO");
			catalog.addRepository(repository);

			catalog.addRepository(new Repository("SHARED"));
//FIM    =========================== CARREGA CATÁLOGO E REPOSITÓRIOS 
//			
//INICIO =========================== CARREGA FONTES VIA ARQUIVO TEXTO
			File entryFile = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\cargaVb6Project.text");
			catalog.loadSource(entryFile);
//FIM =========================== CARREGA FONTES VIA ARQUIVO TEXTO
//			
	        xml(catalog); // LISTA CATÁLOGO
//	        
//INICIO =========================== GERA SYMBOLTABLE E COMPONENTES PROJECT VB6
	        
	        PropertyList properties01 = new PropertyList();
	        
	        String repositoryName = "SEGUROS";
	        String executionName = "TIPMA001";
	        
			String repositoryShared = "SHARED";

	        
	        File vbpFile = (File)getSourceInRepository(catalog,repositoryName,executionName);
	        
	        properties01.addProperty("FILE", vbpFile );
	        
	    	KeywordLanguage keywordLanguage01 = new KeywordVB6();

	    	properties01.addProperty(KEYWORD_LANGUAGE, keywordLanguage01); 		
			
			SymbolFactory symbolfactory01 = new SymbolFactory();
			
			properties01.addProperty(SYMBOL_FACTORY, symbolfactory01);
			
			properties01.addProperty("AST_LIST", new HashMap<String,ParseTree>());

			SymbolTable_New st01 = new SymbolTable_New(properties01);
			properties01.addProperty(SYMBOLTABLE, st01);			
			
	        Metadata projectVb6 = new MetadataProjectVb6(properties01);         // Metadata for Vb6
			projectVb6.accept(new MetadataParserVisitor(properties01)); 

			ParseTreeWalker walker01 = new ParseTreeWalker();
	///		MetadataDefSymbolVb6 defSymMetada = new MetadataDefSymbolVb6(properties);
			MetadataVb6 defSymMetada01 = new MetadataVb6(properties01);

			ParseTree tree01 = (ParseTree)properties01.getProperty("AST");
	        walker01.walk(defSymMetada01, tree01);
//	        System.err.println(defSymMetada01.getNodeList().inputGraphviz(null));	        
//===============================
	        
	        List<Symbol_New> appSymbolList01 = st01.getSymbolByProperty("CATEGORY","APPLICATION");
	        
	        PropertyList propApp01 = appSymbolList01.get(0).getProperties();
	        
	        PropertyListSerializable propSer01 = propApp01.getPropertyListSerializable();
//	        System.err.println(propApp.toString());
	        
//	        String name01 = (String)appSymbolList01.get(0).getProperties().getProperty("Name");
	        
			Source vbProjet01 = getExecutionUnit(catalog,repositoryName,executionName);
			
	    	SourceVersion projectSourceVersion = vbProjet01.getVersions().get(0);
			
	    	projectSourceVersion.setProperties(propSer01);
	    	
	    	vbProjet01.addProperty("CURRENT_VERSION", projectSourceVersion.getId());

	    	System.err.println("=========  PROJECT VERSION XML ==================");
	    	xml(projectSourceVersion);
// INICIO ================================== add componentes in executionUnit	    	
	    	List<String> components = new ArrayList<String>();
//	    	List<SourceVersion> components = new ArrayList<SourceVersion>();
	    	
	    	projectSourceVersion.getProperties().addProperty("COMPONENTS", components);
	    	
	    	ArrayList<PropertyListSerializable> forms = (ArrayList)projectSourceVersion.getProperties().getProperty("Form");
	    	if(forms != null) {
	    		for(PropertyListSerializable prop : forms) {
	    			String name = (String) prop.getProperty("NAME");
	    			Source src = catalog.getRepository(repositoryName).getSource(name);
	    			if (src == null) {
	    				src = catalog.getRepository(repositoryShared).getSource(name);
	    			}
	    			
	    			if (src == null) {
	    				try {
	    					throw new Exception();
	    				}catch (Exception e) {
	    					BicamSystem.printLog("ERROR", "MODULE '"+ name +"' NOT FOUND IN REPOSITORY", e);
	    				}
	    			}
	    			
	    			prop.addProperty("INPUT_LOCATION", src.versions.get(0).properties.getProperty("INPUT_LOCATION"));
	    			prop.addProperty("INPUT_TYPE", src.versions.get(0).properties.getProperty("INPUT_TYPE"));
	    			components.add(src.versions.get(0).getId());
	    		}
	    	}
	    	ArrayList<PropertyListSerializable> modules = (ArrayList)projectSourceVersion.getProperties().getProperty("Module");
	    	if(modules != null) {
	    		for(PropertyListSerializable prop : modules) {
	    			String name = (String) prop.getProperty("NAME");
	    			Source src = catalog.getRepository(repositoryName).getSource(name);
	    			if (src == null) {
	    				src = catalog.getRepository(repositoryShared).getSource(name);
	    			}
	    			
	    			if (src == null) {
	    				try {
	    					throw new Exception();
	    				}catch (Exception e) {
	    					BicamSystem.printLog("ERROR", "MODULE '"+ name +"' NOT FOUND IN REPOSITORY", e);
	    				}
	    			}
	    			
	    			prop.addProperty("INPUT_LOCATION", src.versions.get(0).properties.getProperty("INPUT_LOCATION"));
	    			prop.addProperty("INPUT_TYPE", src.versions.get(0).properties.getProperty("INPUT_TYPE"));
	    			components.add(src.versions.get(0).getId());
	    		}
	    	}	    	
	    	ArrayList<PropertyListSerializable> classes = (ArrayList)projectSourceVersion.getProperties().getProperty("Class");
	    	if(classes != null) {
	    		for(PropertyListSerializable prop : classes) {
	    			String name = (String) prop.getProperty("NAME");
	    			Source src = catalog.getRepository(repositoryName).getSource(name);
	    			if (src == null) {
	    				src = catalog.getRepository(repositoryShared).getSource(name);
	    			}
	    			
	    			if (src == null) {
	    				try {
	    					throw new Exception();
	    				}catch (Exception e) {
	    					BicamSystem.printLog("ERROR", "MODULE '"+ name +"' NOT FOUND IN REPOSITORY", e);
	    				}
	    			}
	    			
	    			prop.addProperty("INPUT_LOCATION", src.versions.get(0).properties.getProperty("INPUT_LOCATION"));
	    			prop.addProperty("INPUT_TYPE", src.versions.get(0).properties.getProperty("INPUT_TYPE"));
	    			components.add(src.versions.get(0).getId());
	    		}
	    	}	    	
// FIM ================================== add componentes in executionUnit ============================================	    	
	    	
	    	xml(catalog);	
// INICIO =============================== parsing  ==================================
	    	
	    	Repository repositoryApp = repository;
	    	Repository repositoryAppShared = catalog.getRepository("SHARED");
	    	
	    	for(String sourceVersionId : components) {
	    		SourceVersion srcVersion = repository.getSourceVersion(sourceVersionId);
	    		if(srcVersion == null) {
	    			srcVersion = repositoryAppShared.getSourceVersion(sourceVersionId);
	    		}
	    		if(srcVersion == null) {
	    			try {
	    				throw new Exception();
	    			}catch (Exception e) {
	    				BicamSystem.printLog("ERROR", "SOURCE VERSION '" + sourceVersionId +"' NOT FOUND", e);
	    			}
	    		}
	    		
	    		String locationSource = (String)srcVersion.getProperties().getProperty("INPUT_LOCATION");

	    		
	    	    System.err.format("*** PARSING Source name: %s - Compilation Unit File Name %s%n"
	    	    		         , srcVersion.sourceName, locationSource );
	    	    
	    	    PropertyList propertiesX = new PropertyList();
	    	    
	    		KeywordVB6  keywordLanguage = new KeywordVB6();
	    		propertiesX.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
	    		
	    		SymbolFactory symbolfactory = new SymbolFactory();
	    		propertiesX.addProperty(SYMBOL_FACTORY, symbolfactory);

	    		propertiesX.addProperty(SYMBOLTABLE, st01);
	    		propertiesX.addProperty("FILE", new File(locationSource));
	    		ParseTree ast = new CompilationUnitParserVb6(propertiesX).getAst();
	    		((HashMap<String,ParseTree>)st01.getProperties().getProperty("AST_LIST")).put(sourceVersionId, ast);
	    	}
// FIM =================================== parsing  =====================================
// INICIO ================================ def symbols ==================================
	    	
	    	repositoryApp = repository;
	    	repositoryAppShared = catalog.getRepository("SHARED");
	    	
	    	for(String sourceVersionId : components) {
	    		SourceVersion srcVersion = repository.getSourceVersion(sourceVersionId);

	    		if(srcVersion == null) {
	    			srcVersion = repositoryAppShared.getSourceVersion(sourceVersionId);
	    		}
	    		if(srcVersion == null) {
	    			try {
	    				throw new Exception();
	    			}catch (Exception e) {
	    				BicamSystem.printLog("ERROR", "SOURCE VERSION '" + sourceVersionId +"' NOT FOUND", e);
	    			}
	    		}
	    		
	    		String locationSource = (String)srcVersion.getProperties().getProperty("INPUT_LOCATION");

	    		
	    	    System.err.format("*** DEFINITION SYMBOLS FOR Source name: %s - Compilation Unit File Name %s%n"
	    	    		         , srcVersion.sourceName, locationSource );
	    	    
	    	    PropertyList propertiesX = new PropertyList();
	    	    
	    		KeywordVB6  keywordLanguage = new KeywordVB6();
	    		propertiesX.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
	    		
	    		SymbolFactory symbolfactory = new SymbolFactory();
	    		propertiesX.addProperty(SYMBOL_FACTORY, symbolfactory);

	    		propertiesX.addProperty(SYMBOLTABLE, st01);
	    		propertiesX.addProperty("FILE", new File(locationSource));
	    		propertiesX.addProperty("SOURCE_VERSION", srcVersion);

//	    		ParseTree ast = new CompilationUnitParserVb6(propertiesX).getAst();
//	    		((HashMap<String,ParseTree>)st01.getProperties().getProperty("AST_LIST")).put(sourceVersionId, ast);

	    	    System.err.format("*** DEF SYMBOL VB6 Source name: %s - Compilation Unit File Name %s%n"
	    		         , srcVersion.sourceName, locationSource );
	    		ParseTreeWalker walker = new ParseTreeWalker();
	    		CatalogDefSymbolVb6 defCompUnit = new CatalogDefSymbolVb6(propertiesX);
	    		
	    		ParseTree tree =
	    	    		((HashMap<String,ParseTree>)st01.getProperties().getProperty("AST_LIST")).get(sourceVersionId);
	            walker.walk(defCompUnit, tree);	    		
	    	}
// FIM ================================ def symbols ==================================
	
//        System.err.println(st01.toString());
// INICIO ================================ ref type symbols ==================================
	    	
	    	repositoryApp = repository;
	    	repositoryAppShared = catalog.getRepository("SHARED");
	    	
	    	for(String sourceVersionId : components) {
	    		SourceVersion srcVersion = repository.getSourceVersion(sourceVersionId);
	    		if(srcVersion == null) {
	    			srcVersion = repositoryAppShared.getSourceVersion(sourceVersionId);
	    		}
	    		if(srcVersion == null) {
	    			try {
	    				throw new Exception();
	    			}catch (Exception e) {
	    				BicamSystem.printLog("ERROR", "SOURCE VERSION '" + sourceVersionId +"' NOT FOUND", e);
	    			}
	    		}
	    		
	    		String locationSource = (String)srcVersion.getProperties().getProperty("INPUT_LOCATION");
	    		
	    	    System.err.format("*** REF TYPE FOR Source name: %s - Compilation Unit File Name %s%n"
	    	    		         , srcVersion.sourceName, locationSource );
	    	    
	    	    PropertyList propertiesX = new PropertyList();
	    	    
	    		KeywordVB6  keywordLanguage = new KeywordVB6();
	    		propertiesX.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
	    		
	    		SymbolFactory symbolfactory = new SymbolFactory();
	    		propertiesX.addProperty(SYMBOL_FACTORY, symbolfactory);

	    		propertiesX.addProperty(SYMBOLTABLE, st01);
	    		propertiesX.addProperty("FILE", new File(locationSource));
	    		propertiesX.addProperty("SOURCE_VERSION", srcVersion);

	    	    System.err.format("*** REF TYPE SYMBOL VB6 Source name: %s - Compilation Unit File Name %s%n"
	    		         , srcVersion.sourceName, locationSource );
	    		ParseTreeWalker walker = new ParseTreeWalker();
	    		CatalogRefTypeSymbolVb6 defCompUnit = new CatalogRefTypeSymbolVb6(propertiesX);
	    		
	    		ParseTree tree =
	    	    		((HashMap<String,ParseTree>)st01.getProperties().getProperty("AST_LIST")).get(sourceVersionId);
	            walker.walk(defCompUnit, tree);	    		
	    	}
// FIM ================================ ref type ==================================	        
// INICIO ================================ ref symbol symbols ==================================
	    	
	    	repositoryApp = repository;
	    	repositoryAppShared = catalog.getRepository("SHARED");
	    	
	    	for(String sourceVersionId : components) {
	    		SourceVersion srcVersion = repository.getSourceVersion(sourceVersionId);
	    		if(srcVersion == null) {
	    			srcVersion = repositoryAppShared.getSourceVersion(sourceVersionId);
	    		}
	    		if(srcVersion == null) {
	    			try {
	    				throw new Exception();
	    			}catch (Exception e) {
	    				BicamSystem.printLog("ERROR", "SOURCE VERSION '" + sourceVersionId +"' NOT FOUND", e);
	    			}
	    		}
	    		
	    		String locationSource = (String)srcVersion.getProperties().getProperty("INPUT_LOCATION");
	    		
	    	    System.err.format("*** REF SYMBOL FOR Source name: %s - Compilation Unit File Name %s%n"
	    	    		         , srcVersion.sourceName, locationSource );
	    	    
	    	    PropertyList propertiesX = new PropertyList();
	    	    
	    		KeywordVB6  keywordLanguage = new KeywordVB6();
	    		propertiesX.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
	    		
	    		SymbolFactory symbolfactory = new SymbolFactory();
	    		propertiesX.addProperty(SYMBOL_FACTORY, symbolfactory);

	    		propertiesX.addProperty(SYMBOLTABLE, st01);
	    		propertiesX.addProperty("FILE", new File(locationSource));
	    		propertiesX.addProperty("SOURCE_VERSION", srcVersion);

	    	    System.err.format("*** REF SYMBOL VB6 Source name: %s - Compilation Unit File Name %s%n"
	    		         , srcVersion.sourceName, locationSource );
	    		ParseTreeWalker walker = new ParseTreeWalker();
	    		CatalogRefSymbolVb6 refSymbol = new CatalogRefSymbolVb6(propertiesX);
	    		
	    		ParseTree tree =
	    	    		((HashMap<String,ParseTree>)st01.getProperties().getProperty("AST_LIST")).get(sourceVersionId);
	            walker.walk(refSymbol, tree);	  
	    	}
	    	
// FIM ================================ ref type ========================================
// INÍCIO =================================== ref Call  ==================================		    	
			PropertyList propCall = new PropertyList();
			propCall.addProperty("SYMBOLTABLE", st01);
			Set<String> fullNameProcedures = new HashSet<String>();

			for(String sourceVersionId : components){
	    		SourceVersion srcVersion = repository.getSourceVersion(sourceVersionId);
	    		if(srcVersion == null) {
	    			srcVersion = repositoryAppShared.getSourceVersion(sourceVersionId);
	    		}
	    		if(srcVersion == null) {
	    			try {
	    				throw new Exception();
	    			}catch (Exception e) {
	    				BicamSystem.printLog("ERROR", "SOURCE VERSION '" + sourceVersionId +"' NOT FOUND", e);
	    			}
	    		}
	    		
	    		String locationSource = (String)srcVersion.getProperties().getProperty("INPUT_LOCATION");
				if (locationSource.toUpperCase().endsWith("VBP")) continue;
				propCall.addProperty("BASE_NAME", "SQLRPCInit");
				propCall.addProperty("BASE_PARAMETER_NAME", "SqlConn");
				propCall.addProperty("BASE_PARAMETER_INDEX", null);

				propCall.addProperty("TARGET_PARAMETER_NAME", null);
				propCall.addProperty("TARGET_PARAMETER_INDEX", 1);

				ParseTreeWalker walker = new ParseTreeWalker();
				GetThingByParameter thingByParm = new GetThingByParameter(propCall);
//				ParseTree tree;
//				tree = (ParseTree) p.getProperties().getProperty("AST");
	    		ParseTree tree =
	    	    		((HashMap<String,ParseTree>)st01.getProperties().getProperty("AST_LIST")).get(sourceVersionId);
	    		
		        walker.walk(thingByParm, tree);

		        
//		        System.err.println(uiGraphBuilder.getInputGraph()+ System.lineSeparator());
		        try {
					System.err.println("********GetThingByParameter*************" + locationSource  +"/"+ thingByParm.getStoredProcedures() 
					+ "**********************");
					
					 srcVersion.getProperties().addProperty("STORED_PROCEDURES", thingByParm.getStoredProcedures());

//===========================================================================
			        for(String name : thingByParm.getStoredProcedures()) {
			        	fullNameProcedures.add(BicamSystem.sqlNameToFullQualifiedName(name.toUpperCase()));
			        }
		//===========================================================================					 
							 
							 xml(srcVersion);
							 
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
			}	    	
// Fim ==============================================================================
			System.err.println("*** PROCEDURES: " + fullNameProcedures);
			for(String n :fullNameProcedures) {
				System.err.println(n + ",FILE,C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\PROCEDURES\\" + n);
			}
			
// Início =============================UI Sequence ==============================================
			StringBuffer sb = new StringBuffer();
			PropertyList propUiSeq = new PropertyList();
			propUiSeq.addProperty("SYMBOLTABLE", st01);
			propUiSeq.addProperty("SEPARATOR", "/");
			
			List<Symbol_New> symAppList = st01.getSymbolByProperty("CATEGORY", "APPLICATION");
			Symbol_New symApp = symAppList.get(0);
			
			String appEntry = (String)symApp.getProperty("Startup");
			appEntry = appEntry.replace("\"", "");
			if(appEntry.split(" ").length > 1) {
				appEntry = appEntry.split(" ")[appEntry.split(" ").length-1];
				List<Symbol_New> lista = st01.getSymbolByProperty(NAME, appEntry);
				appEntry = lista.get(0).getEnclosingScope().getName() + "." + appEntry;
			}
			
			System.err.println("**********************" + symApp.getName() 
			+ "**********************");
			System.err.println(symApp.getName() + "/" + appEntry);
			sb.append(symApp.getName() + "/" + appEntry + System.lineSeparator());	
			
			for(String sourceVersionId : components){
	    		SourceVersion srcVersion = repository.getSourceVersion(sourceVersionId);
	    		if(srcVersion == null) {
	    			srcVersion = repositoryAppShared.getSourceVersion(sourceVersionId);
	    		}
	    		if(srcVersion == null) {
	    			try {
	    				throw new Exception();
	    			}catch (Exception e) {
	    				BicamSystem.printLog("ERROR", "SOURCE VERSION '" + sourceVersionId +"' NOT FOUND", e);
	    			}
	    		}
	    		
	    		String locationSource = (String)srcVersion.getProperties().getProperty("INPUT_LOCATION");
				if (locationSource.toUpperCase().endsWith("VBP")) continue;
				ParseTreeWalker walker = new ParseTreeWalker();
				UISequenceGraphBuilder uiGraphBuilder = new UISequenceGraphBuilder(propUiSeq);
		  		ParseTree tree =
	    	    		((HashMap<String,ParseTree>)st01.getProperties().getProperty("AST_LIST")).get(sourceVersionId);
	  		        walker.walk(uiGraphBuilder, tree);

		        
//		        System.err.println(uiGraphBuilder.getInputGraph()+ System.lineSeparator());
		        try {
					System.err.println("**********************" + locationSource  +"/"+uiGraphBuilder.getCompUnitName() 
					+ "**********************");
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
		        System.err.println(uiGraphBuilder.getInputGraph());
		        
		        sb.append(uiGraphBuilder.getInputGraph());
			}
			
// End    ===========================================================================			
	        
//END ============================== GERA SYMBOLTABLE E COMPONENTES PROJECT VB6
			
			// create source
//			ExecutionUnit vbProjet = new Vb6ProjectSerializable("R1PAB0");
//			repository.addSource(vbProjet);
////			vbProjet.properties.addProperty("FILE", "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TIPMA00");
///			File filex = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TIPMA00\\TIPMA0.VBP");
			File filex = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1PAB0.VBP"); 

			// create version of source
			
			InputRepository inputRepository = new InputRepository(filex);
			
			SourceVersion sv = new SourceVersion(inputRepository, "UNDEFINED");

			// include version in source
//			vbProjet.addVersion(sv);
			
			String fileName = (String)sv.getProperties().getProperty("INPUT_LOCATION");
			
			File inputRepositoryFile = new File(fileName);
			
			ParseTree astx = parseProjectVbp(inputRepositoryFile);
			
//=============================================================================
			PropertyList properties = new PropertyList();
			
			KeywordVB6 keywordLanguage = new KeywordVB6();

			properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
			
			SymbolFactory symbolfactory = new SymbolFactory();
			
			properties.addProperty(SYMBOL_FACTORY, symbolfactory);

			SymbolTable_New st = new SymbolTable_New(properties);
			properties.addProperty(SYMBOLTABLE, st);
			properties.addProperty("FILE", filex);	
//=============================================================================			
			ParseTreeWalker walker = new ParseTreeWalker();
			MetadataVb6 defSymMetada = new MetadataVb6(properties);
			ParseTree tree = astx;
	        walker.walk(defSymMetada, tree);
	        
	        List<Symbol_New> appSymbolList = st.getSymbolByProperty("CATEGORY","APPLICATION");
	        
	        PropertyList propApp = appSymbolList.get(0).getProperties();
	        
	        PropertyListSerializable propSer = propApp.getPropertyListSerializable();
	        System.err.println(propApp.toString());
	        
	        String name = (String)appSymbolList.get(0).getProperties().getProperty("Name");
	        
			ExecutionUnit vbProjet = new Vb6ProjectSerializable("R1PAB0");
			
			vbProjet.setProperties(propSer);
			// include version in source
			vbProjet.addVersion(sv);			
			repository.addSource(vbProjet);	        
	        
//	        xml(repository);
//	        xml(catalog);
			
	}	
}