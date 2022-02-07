package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.KeywordSqlSybase;
import br.com.bicam.util.KeywordVB6;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.datamodel.ApplicationComponent;
import br.com.bicam.util.datamodel.Catalog;
import br.com.bicam.util.datamodel.Repository;
import br.com.bicam.util.datamodel.Source;
import br.com.bicam.util.datamodel.SqlTransactSybaseStoredProcedure;
import br.com.bicam.util.datamodel.Vb6Application;
import br.com.bicam.util.datamodel.Version;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.graph.Path;
import br.com.bicam.util.graph.SymbolWeightedGraph;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class Parse {
	final static String INPUT_STREAM = "INPUT_STREAM";
	
	public static void main(String args[]) throws Exception {
		InputStream in = new FileInputStream(new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\output" + "\\" + "TEMP.text"));
		Catalog catalog = BicamSystem.readObjectAsJaxbXml(in, Catalog.class);
		PropertyList properties = new PropertyList();
		properties.addProperty("ID", "SEGUROS");
		Repository repository = catalog.getRepository(properties);
		properties.addProperty("ID", "TIPMA001");
		Source source = (Source)repository.getItem(properties);
		if(source == null) {
			properties.addProperty("ID", "SHARED");
			repository = catalog.getRepository(properties);
			properties.addProperty("ID", "TIPMA001");
			source = (Source)repository.getItem(properties);
		}
		System.err.println(source.getId());
		Version version = (Version)source.getMoreRecentItem();// corrigir para o + recente
		
		PropertyList prop = new PropertyList();
		prop.addProperty("VERSION", version);
		new ParseSymbol(prop);
		
		PropertyList stProperties = new PropertyList();
		KeywordLanguage keywordLanguage = new KeywordVB6();
		stProperties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		SymbolFactory symbolfactory = new SymbolFactory();
		stProperties.addProperty(SYMBOL_FACTORY, symbolfactory);
		SymbolTable_New st = new SymbolTable_New(stProperties);
		stProperties.addProperty(SYMBOLTABLE, st);
		stProperties.addProperty("VERSION", version);
		
		Vb6ProjectDefSymbol vb6ProjectDefSymbol = new Vb6ProjectDefSymbol(stProperties);
		ParseTreeWalker walker = new ParseTreeWalker();
		
		ParseTree tree = (ParseTree)version.getProperties().getProperty("AST");
        walker.walk(vb6ProjectDefSymbol, tree);
        
        List<Symbol_New> appSymbolList = st.getSymbolByProperty("CATEGORY","APPLICATION");
        PropertyList propApp = appSymbolList.get(0).getProperties();
        System.err.println(propApp);
        Vb6Application vb6App = new Vb6Application(appSymbolList.get(0));
        
        System.err.println(vb6App.getProperties());
        
        PropertyList appProperties = new PropertyList();
        File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\app.xml");
        appProperties.addProperty("XML_FILE", file);
        BicamSystem.toXml(vb6App, appProperties);

        Vb6Application appcopy = BicamSystem.readObjectAsXmlFrom(new FileReader(file), Vb6Application.class);
        System.err.println(appcopy.getProperties());
        System.err.println(vb6App.getProperties());
        
//=====================================INÍCIO Parse application components ======================================================
	for(ApplicationComponent app : appcopy.dependesOn()) {
		String appId = app.getId();
		prop = new PropertyList();
		prop.addProperty("CATALOG",catalog);
		prop.addProperty("SOURCE_ID",appId);			
		Version versionApp = getVersion(prop);	
		if(versionApp == null) {
			BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
			continue;
		}
		prop = new PropertyList();
		prop.addProperty("VERSION", versionApp);
		BicamSystem.printLog("INFO", "Parsing :" + appId);
		new ParseSymbol(prop);
	}
//=====================================INÍCIO Def Symbol components ======================================================
	for(ApplicationComponent app : appcopy.dependesOn()) {
		String appId = app.getId();
		prop = new PropertyList();
		prop.addProperty("CATALOG",catalog);
		prop.addProperty("SOURCE_ID",appId);			
		Version versionApp = getVersion(prop);	
		if(versionApp == null) {
			BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
			continue;
		}
		prop = new PropertyList();
		prop.addProperty("VERSION", versionApp);
		prop.addProperty("COMPONENT_ID", appId);
		prop.addProperty("APPLICATION_COMPONENT", app);
		prop.addProperty("VERSION", versionApp);
		prop.addProperty("SYMBOLTABLE", st);
		prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
		prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());

		BicamSystem.printLog("INFO", "Def Symbol :" + appId);
		new	DefSymbol(prop);
	}
//=====================================FIM Def Symbol components ======================================================
//=====================================INÍCIO Ref Type Symbol components ======================================================
	for(ApplicationComponent app : appcopy.dependesOn()) {
		String appId = app.getId();
		prop = new PropertyList();
		prop.addProperty("CATALOG",catalog);
		prop.addProperty("SOURCE_ID",appId);			
		Version versionApp = getVersion(prop);	
		if(versionApp == null) {
			BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
			continue;
		}
		prop = new PropertyList();
		prop.addProperty("VERSION", versionApp);
		prop.addProperty("COMPONENT_ID", appId);
		prop.addProperty("APPLICATION_COMPONENT", app);
		prop.addProperty("VERSION", versionApp);
		prop.addProperty("SYMBOLTABLE", st);
		prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
		prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());

		BicamSystem.printLog("INFO", "Ref Type Symbol :" + appId);
		new	RefTypeSymbol(prop);
	}
//=====================================FIM Ref Type Symbol components ======================================================
//=====================================INÍCIO Ref Symbol components ======================================================
		for(ApplicationComponent app : appcopy.dependesOn()) {
			String appId = app.getId();
			prop = new PropertyList();
			prop.addProperty("CATALOG",catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
				continue;
			}
			prop = new PropertyList();
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("COMPONENT_ID", appId);
			prop.addProperty("APPLICATION_COMPONENT", app);
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("SYMBOLTABLE", st);
			prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
			prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());

			BicamSystem.printLog("INFO", "Ref Symbol :" + appId);
			new	RefSymbol(prop);
		}
//=====================================FIM Ref Symbol components ======================================================

		System.err.println("\nPRINTING SYMBOL TABLE");
		System.err.println(st);
		
//=====================================INÍCIO building COMP UNIT GRAPH =====================================================
		for(ApplicationComponent app : appcopy.dependesOn()) {
			String appId = app.getId();
			prop = new PropertyList();
			prop.addProperty("CATALOG",catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "SOURCE NOT FOUND: " + appId);
				continue;
			}
			prop = new PropertyList();
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("COMPONENT_ID", appId);
			prop.addProperty("APPLICATION_COMPONENT", app);
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("SYMBOLTABLE", st);
			prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
			prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());

			BicamSystem.printLog("INFO", "BUILDING COMP UNIT GRAPH :" + appId);
			new	GraphCompUnit(prop);
		}
			
//===================================== FIM building COMP UNIT GRAPH =====================================================
//=====================================INÍCIO COMPUNIT FILE TO HTML =====================================================
		for(ApplicationComponent app : appcopy.dependesOn()) {
			String appId = app.getId();
			prop = new PropertyList();
			prop.addProperty("CATALOG",catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "SOURCE NOT FOUND: " + appId);
				continue;
			}
			else {
				BicamSystem.printLog("INFO", "BUILDING UI HTML: " + appId);
			}
			
			String out = "c:\\HTML\\FILE_GRAPH_" + versionApp.getParent().getId()  + ".HTML";
			Map<Integer,String> lineToNodeId = (Map<Integer, String>) versionApp.getProperties().getProperty("GRAPH_LINE_TO_NODE");
			PropertyList propHtml = new PropertyList();
			propHtml.addProperty("LINE_TO_NODE", lineToNodeId);
			BicamSystem.toHtml(versionApp.getInputStream(), out, propHtml);			
		}
//=====================================INÍCIO COMPUNIT FILE TO HTML=====================================================
//===================================== INÍCIO UI HTML =====================================================

   	    VbFormToHtml_New vbFormToHtml = new VbFormToHtml_New("C:\\pgm\\HtmlNew.stg", st);

//===================================== FIM UI HTML =====================================================

   	    formSequence(appcopy, st, catalog);
   	    
   	    
   	    shortestPath(appcopy, st, catalog);
	}
	
//================================= SEQUENCE ( EXECUÇÃO OBRIGATÓRIA) =========================================	
	private static void formSequence(Vb6Application _appcopy, SymbolTable_New _st, Catalog _catalog) {
		StringBuffer sb = new StringBuffer();
		PropertyList properties = new PropertyList();
		properties.addProperty("SYMBOLTABLE", _st);
		properties.addProperty("SEPARATOR", "/");
		
		List<Symbol_New> symAppList = _st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		
		String appEntry = (String)symApp.getProperty("Startup".toUpperCase());
		appEntry = appEntry.replace("\"", "");
		if(appEntry.split(" ").length > 1) {
			appEntry = appEntry.split(" ")[appEntry.split(" ").length-1];
			List<Symbol_New> lista = _st.getSymbolByProperty(NAME, appEntry);
			appEntry = lista.get(0).getEnclosingScope().getName() + "." + appEntry;
		}
		
		System.err.println("**********************" + symApp.getName() 
		+ "**********************");
		
		NodeList aplicationNodeList = new NodeList();
//NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
		for(ApplicationComponent app : _appcopy.dependesOn()) {
			String appId = app.getId();
			PropertyList prop = new PropertyList();
			prop.addProperty("CATALOG",_catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "SOURCE NOT FOUND: " + appId);
				continue;
			}
			String compilationUnitType = (String) versionApp.getProperties().getProperty("COMPILATION_UNIT_TYPE");

			prop = new PropertyList();
			prop.addProperty("COMPILATION_UNIT_TYPE", compilationUnitType);
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("SYMBOLTABLE", _st);
			prop.addProperty("KEYWORD_LANGUAGE",new KeywordVB6());
			prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());
			NodeList componentNodeList = new NodeList();
			prop.addProperty("NODELIST", componentNodeList);
			prop.addProperty("APP_NODELIST", aplicationNodeList);
			
			prop.addProperty("APPLICATION", app);

			BicamSystem.printLog("INFO", "SEQUENCE CALL :" + appId);
			new	SequenceCall(prop);
			versionApp.toString();
			aplicationNodeList.appendNodeList(componentNodeList);
		}
		

		System.err.println("=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=1=");
		System.err.println(aplicationNodeList.inputSymbolGraph(null));
//NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
//================================= FIM SEQUENCE ===========================================	
//================================= INICIO STORED PROCEDURE SEGURADORA ===========================================	
		HashSet<ApplicationComponent> wcomponents = new HashSet<ApplicationComponent>();

		for(ApplicationComponent app : _appcopy.dependesOn()) {
			String appId = app.getId();
			PropertyList prop = new PropertyList();
			prop.addProperty("CATALOG",_catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "SOURCE NOT FOUND: " + appId);
				continue;
			}
			String compilationUnitType = (String) versionApp.getProperties().getProperty("COMPILATION_UNIT_TYPE");

			prop = new PropertyList();
			prop.addProperty("COMPILATION_UNIT_TYPE", compilationUnitType);
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("SYMBOLTABLE", _st);
			prop.addProperty("KEYWORD_LANGUAGE",new KeywordVB6());
			prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());
			prop.addProperty("NODE_LIST", versionApp.getProperties().getProperty("NODELIST"));

			prop.addProperty("DB_ACCESS_NODE_MAP", new HashMap<String,List<String>>());
			prop.addProperty("APP_NODE", versionApp.getProperties().getProperty("APP_NODE"));
			
			prop.addProperty("BASE_NAME", "SQLRPCInit");
			prop.addProperty("BASE_PARAMETER_NAME", "SqlConn");
			prop.addProperty("BASE_PARAMETER_INDEX", null);

			prop.addProperty("TARGET_PARAMETER_NAME", null);
			prop.addProperty("TARGET_PARAMETER_INDEX", 1);

			ParseTreeWalker walker = new ParseTreeWalker();
			GetParameterByPosition thingByParm = new GetParameterByPosition(prop);
			ParseTree astTree = (ParseTree) versionApp.getProperties().getProperty("AST");
	        walker.walk(thingByParm, astTree);

	        try {
				System.err.println("**********************" + appId  +"/"+ thingByParm.getStoredProcedures() 
				+ "**********************");
				
				for(String procedure : thingByParm.getStoredProcedures() ) {
					System.err.println("==> " + procedure + "/" + BicamSystem.sqlNameToFullQualifiedName(procedure));
					ApplicationComponent appx = new SqlTransactSybaseStoredProcedure(BicamSystem.sqlNameToFullQualifiedName(procedure).toUpperCase());
                    appx.getProperties().addProperty("LANGUAGE", "TRANSACT_SYBASE");
                    wcomponents.add(appx);
                    app.addComponent(appx);
				}
				
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
	        
	        for(ApplicationComponent appx : app.dependesOn()) {
	        	System.err.println(appx);
	        }

	        NodeList nodeList = (NodeList) versionApp.getProperties().getProperty("NODELIST");
	        System.err.println(nodeList.inputSymbolGraph(null));
		}
		
//================================= FIM STORED PROCEDURE1  ===========================================	
//============================= INÍCIO SALVA APPLICATION TO XML ======================================
        _appcopy.addComponents(wcomponents);  // Procedures utilizadas nos forms, modules e classes

		File file = new File("C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\app.xml");
        PropertyList appProperties = new PropertyList();
		appProperties.addProperty("XML_FILE", file);
        try {
			BicamSystem.toXml(_appcopy, appProperties);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        
//============================= FIM SALVA APPLICATION TO XML ============================
		for(ApplicationComponent app : _appcopy.dependesOn()) {
			String appId = app.getId();
			PropertyList prop = new PropertyList();
			prop.addProperty("CATALOG",_catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "SOURCE NOT FOUND: " + appId);
				continue;
			}
			app.getProperties().addProperty("VERSION", versionApp);
			String compilationUnitType = (String) versionApp.getProperties().getProperty("COMPILATION_UNIT_TYPE");
			if(!compilationUnitType.contentEquals("VB6_COMPILATION_UNIT")) continue;
			prop = new PropertyList();
			prop.addProperty("COMPILATION_UNIT_TYPE", compilationUnitType);
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("SYMBOLTABLE", _st);
			prop.addProperty("KEYWORD_LANGUAGE",new KeywordVB6());
			prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());
			prop.addProperty("NODE_LIST", versionApp.getProperties().getProperty("NODELIST"));

			prop.addProperty("DB_ACCESS_NODE_MAP", new HashMap<String,List<String>>());
			prop.addProperty("APP_NODE", versionApp.getProperties().getProperty("APP_NODE"));
			
			properties.addProperty("BASE_NAME", "FU_Parametro");
			properties.addProperty("BASE_PARAMETER_NAME", "SqlConn");
			properties.addProperty("BASE_PARAMETER_INDEX", null);

			properties.addProperty("TARGET_PARAMETER_NAME", null);
			properties.addProperty("TARGET_PARAMETER_INDEX", 2);

			ParseTreeWalker walker = new ParseTreeWalker();
			GetParameterByPosition thingByParm = new GetParameterByPosition(prop);
			ParseTree astTree = (ParseTree) versionApp.getProperties().getProperty("AST");
	        walker.walk(thingByParm, astTree);
	        
	        try {
				System.err.println(">>>>>>>>>>>>>>>>>>" + appId  +"/"+ thingByParm.getStoredProcedures() 
				+ "<<<<<<<<<<<<<<<<<<<<");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
	        
	        NodeList nodeList = (NodeList) versionApp.getProperties().getProperty("NODELIST");
	        System.err.println(nodeList.inputSymbolGraph(null));
		}

		for(ApplicationComponent app : _appcopy.dependesOn()) {
			String appId = app.getId();
			PropertyList prop = new PropertyList();
			prop.addProperty("CATALOG",_catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "SOURCE NOT FOUND: " + appId);
				continue;
			}
			String compilationUnitType = (String) versionApp.getProperties().getProperty("COMPILATION_UNIT_TYPE");
			if(!compilationUnitType.contentEquals("VB6_COMPILATION_UNIT")) continue;

			prop = new PropertyList();
			prop.addProperty("COMPILATION_UNIT_TYPE", compilationUnitType);
			prop.addProperty("VERSION", versionApp);
			prop.addProperty("SYMBOLTABLE", _st);
			prop.addProperty("KEYWORD_LANGUAGE",new KeywordVB6());
			prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());
			prop.addProperty("NODE_LIST", versionApp.getProperties().getProperty("NODELIST"));

			prop.addProperty("DB_ACCESS_NODE_MAP", new HashMap<String,List<String>>());
			prop.addProperty("APP_NODE", versionApp.getProperties().getProperty("APP_NODE"));
			
			properties.addProperty("BASE_NAME", "FU_Parametro_Ret");
			properties.addProperty("BASE_PARAMETER_NAME", "SqlConn");
			properties.addProperty("BASE_PARAMETER_INDEX", null);

			properties.addProperty("TARGET_PARAMETER_NAME", null);
			properties.addProperty("TARGET_PARAMETER_INDEX", 2);

			ParseTreeWalker walker = new ParseTreeWalker();
			GetParameterByPosition thingByParm = new GetParameterByPosition(prop);
			ParseTree astTree = (ParseTree) versionApp.getProperties().getProperty("AST");
	        walker.walk(thingByParm, astTree);
	        
	        try {
				System.err.println("........................" + appId  +"/"+ thingByParm.getStoredProcedures() 
				+ "........................");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
	        
	        NodeList nodeList = (NodeList) versionApp.getProperties().getProperty("NODELIST");
	        System.err.println(nodeList.inputSymbolGraph(null));
		}		
	}
	
	private static void shortestPath(Vb6Application _appcopy, SymbolTable_New _st, Catalog _catalog) throws IOException {
		StringBuffer sb = new StringBuffer();
		PropertyList properties = new PropertyList();
		properties.addProperty("SYMBOLTABLE", _st);
		properties.addProperty("SEPARATOR", "/");
		
		List<Symbol_New> symAppList = _st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		
		String appEntry = (String)symApp.getProperty("Startup".toUpperCase());
		appEntry = appEntry.replace("\"", "");
		if(appEntry.split(" ").length > 1) {
			appEntry = appEntry.split(" ")[appEntry.split(" ").length-1];
			List<Symbol_New> lista = _st.getSymbolByProperty(NAME, appEntry);
			appEntry = lista.get(0).getEnclosingScope().getName() + "." + appEntry;
		}
		
		System.err.println("*******  SHORTEST PATH ***************" + symApp.getName() 
		+ "**********************");
		//=============================== START TESTE SHORTEST PATH ================================================================		
		for(ApplicationComponent app : _appcopy.dependesOn()){ 
			String appId = app.getId();
			PropertyList prop = new PropertyList();
			prop.addProperty("CATALOG",_catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "SOURCE NOT FOUND: " + appId);
				continue;
			}
			String compilationUnitType = (String) versionApp.getProperties().getProperty("COMPILATION_UNIT_TYPE");
			
			if(!versionApp.getParent().getId().equalsIgnoreCase("TIFMA001")) continue;
			
	        PropertyList locProperties = new PropertyList();

	        locProperties.addProperty("COMP_UNIT_GRAPH", versionApp.getProperties().getProperty("COMP_UNIT_GRAPH"));
	        
	        SymbolWeightedGraph symGraph = (SymbolWeightedGraph) versionApp.getProperties().getProperty("COMP_UNIT_GRAPH");
	        System.err.println(symGraph.toString());
	        
	        Path path = new Path(locProperties);
	        String from = "104425:2803";

	        path.setFrom(from);
	        
	    	String inputNode = "106808:2866";
	        		
    		path.setTo(inputNode);
    		path.run();
    		
    		Set<Integer> shortestPath = path.getShortestPath();
    		String xTarget = new String(inputNode.split(":")[1]);
    		String xFrom = new String(from.split(":")[1]);

    		Integer startPathLine = new Integer(xFrom);

    		Integer targetLine = new Integer(xTarget);

			String dirName  = "C:/input";
			String fileHtml = dirName +  "/" + "PATH" + "_" + from.split(":")[1] + "_" + inputNode.split(":")[1] + ".html";
			PropertyList pathProperties= new PropertyList();
			
			properties.addProperty("PATH_START_LINE", startPathLine);       			
			properties.addProperty("PATH_MARKED_LINES", shortestPath);
			properties.addProperty("PATH_TARGET_LINE", targetLine);
			properties.addProperty("LINE_TO_NODE", versionApp.getProperties().getProperty("GRAPH_LINE_TO_NODE"));
			
			try {
				File htmlFile = BicamSystem.toHtml(versionApp.getInputStream(), fileHtml, pathProperties);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//=============================== END TESTE SHORTEST PATH ======================================================================
//============================ INÍCIO PROCESSA STORED PROCEDURES =======================================	
		KeywordLanguage keywordLanguage = new KeywordSqlSybase();
		PropertyList stProperties = new PropertyList();
		stProperties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		SymbolFactory symbolfactory = new SymbolFactory();
		stProperties.addProperty(SYMBOL_FACTORY, symbolfactory);
		SymbolTable_New st = new SymbolTable_New(stProperties);
		//=====================================INÍCIO Parse components ======================================================
		for(ApplicationComponent app : _appcopy.dependesOn()) {
			if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue;
			String appId = app.getId();
			PropertyList prop = new PropertyList();
			prop.addProperty("CATALOG",_catalog);
			prop.addProperty("SOURCE_ID",appId);			
			Version versionApp = getVersion(prop);	
			if(versionApp == null) {
				BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
				continue;
			}
			String compilationUnitType = (String) versionApp.getProperties().getProperty("COMPILATION_UNIT_TYPE");

/*			InputStream isx = null;
			try {
				isx = versionApp.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
*/			prop = new PropertyList();
//			prop.addProperty("INPUT_STREAM", isx);
			prop.addProperty("COMPILATION_UNIT_TYPE", compilationUnitType);
			prop.addProperty("VERSION", versionApp);
			BicamSystem.printLog("INFO", "Parsing :" + appId);
			new ParseSymbol(prop);
		}
		//=====================================INÍCIO Def Symbol components ======================================================
			for(ApplicationComponent app : _appcopy.dependesOn()) {
				if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue;
				String appId = app.getId();
				PropertyList prop = new PropertyList();
				prop.addProperty("CATALOG",_catalog);
				prop.addProperty("SOURCE_ID",appId);			
				Version versionApp = getVersion(prop);	
				if(versionApp == null) {
					BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
					continue;
				}
				prop = new PropertyList();
				prop.addProperty("VERSION", versionApp);
				prop.addProperty("COMPONENT_ID", appId);
				prop.addProperty("APPLICATION_COMPONENT", app);
				prop.addProperty("VERSION", versionApp);
				prop.addProperty("SYMBOLTABLE", st);
				prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
				try {
					prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BicamSystem.printLog("INFO", "Def Symbol :" + appId);
				new	DefSymbol(prop);
			}
		//=====================================FIM Def Symbol components ======================================================
		//=====================================INÍCIO Ref Type Symbol components ======================================================
			for(ApplicationComponent app : _appcopy.dependesOn()) {
				if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue;
				String appId = app.getId();
				PropertyList prop = new PropertyList();
				prop.addProperty("CATALOG",_catalog);
				prop.addProperty("SOURCE_ID",appId);			
				Version versionApp = getVersion(prop);	
				if(versionApp == null) {
					BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
					continue;
				}
				String compilationUnitType = (String) versionApp.getProperties().getProperty("COMPILATION_UNIT_TYPE");

				InputStream isx = null;
				try {
					isx = versionApp.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
				prop = new PropertyList();
				prop.addProperty("INPUT_STREAM", isx);
				prop.addProperty("COMPILATION_UNIT_TYPE", compilationUnitType);
				prop.addProperty("VERSION", versionApp);
				prop.addProperty("COMPONENT_ID", appId);
				prop.addProperty("APPLICATION_COMPONENT", app);
				prop.addProperty("VERSION", versionApp);
				prop.addProperty("SYMBOLTABLE", st);
				prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
				prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());

				BicamSystem.printLog("INFO", "Ref Type Symbol :" + appId);
				new	RefTypeSymbol(prop);
			}
		//=====================================FIM Ref Type Symbol components ======================================================
		//=====================================INÍCIO Ref Symbol components ======================================================
			for(ApplicationComponent app : _appcopy.dependesOn()) {
				if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue;
				String appId = app.getId();
				PropertyList prop = new PropertyList();
				prop.addProperty("CATALOG",_catalog);
				prop.addProperty("SOURCE_ID",appId);			
				Version versionApp = getVersion(prop);	
				if(versionApp == null) {
					BicamSystem.printLog("WARNING", "Application component source not found: " + appId);
					continue;
				}
				prop = new PropertyList();
				prop.addProperty("VERSION", versionApp);
				prop.addProperty("COMPONENT_ID", appId);
				prop.addProperty("APPLICATION_COMPONENT", app);
				prop.addProperty("VERSION", versionApp);
				prop.addProperty("SYMBOLTABLE", st);
				prop.addProperty("KEYWORD_LANGUAGE", keywordLanguage);
				prop.addProperty("SYMBOL_FACTORY", new SymbolFactory());

				BicamSystem.printLog("INFO", "Ref Symbol :" + appId);
				new	RefSymbol(prop);
			}
					
					System.err.println(st.toString());
//=====================================FIM Ref Symbol components ======================================================
		for(ApplicationComponent app : _appcopy.dependesOn()) {		
			if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue;
			ParseTreeWalker walker = new ParseTreeWalker();
			Version version = (Version) app.getProperties().getProperty("VERSION");
			if(version  == null) {
				BicamSystem.printLog("WARNING", "****************** PROCEDURE NOT FOUND " + app.getId());
				continue;
			}

			PropertyList dbProperties = new PropertyList();
			dbProperties.addProperty("SYMBOLTABLE", st);
			dbProperties.addProperty("COMPILATION_UNIT_NAME", version.getId());
			dbProperties.addProperty("FULL_SQL_NAME", app.getId());
			
			DatabaseAccess dbAccess = new DatabaseAccess(dbProperties);
			ParseTree tree = (ParseTree) version.getProperties().getProperty("AST");

	        walker.walk(dbAccess, tree);
	        System.err.println("*** CompUnit: " + app.getId() + " - " + dbAccess.getTableList());
		}					
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++					
		SymbolFactory wsymbolfactory = new SymbolFactory();
		
		PropertyList compUnitGraphProperties = new PropertyList();
		compUnitGraphProperties.addProperty(SYMBOLTABLE, st);
		compUnitGraphProperties.addProperty(SYMBOL_FACTORY,wsymbolfactory);
		compUnitGraphProperties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		

		for(ApplicationComponent app : _appcopy.dependesOn()) {		
			if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue; 			
			Version version = (Version) app.getProperties().getProperty("VERSION");
			if(version  == null) {
				BicamSystem.printLog("WARNING", "*****ARRUMAR ************* PROCEDURE NOT FOUND " + app.getId());
				continue;
			}
			
			compUnitGraphProperties.addProperty("VERSION", version); 		

			System.err.println("BUILDING GRAPH FOR " + app.getId());

			ParseTreeWalker walker = new ParseTreeWalker();
			CompilationUnitSqlTransactSybaseGraph compUnitGraph = new CompilationUnitSqlTransactSybaseGraph(compUnitGraphProperties);
			ParseTree tree = (ParseTree) version.getProperties().getProperty("AST");
	        walker.walk(compUnitGraph, tree);
	        version.getProperties().addProperty("COMP_UNIT_GRAPH", compUnitGraph.getGraph());
		}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	
//###################################  INICIO ##########################################################							
	PropertyList literalProperties = new PropertyList();
	literalProperties.addProperty(SYMBOLTABLE, st);
	for(ApplicationComponent app : _appcopy.dependesOn()) {		
		if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue; 			
		Version version = (Version) app.getProperties().getProperty("VERSION");
		if(version  == null) {
			BicamSystem.printLog("WARNING", "*****ARRUMAR ************* PROCEDURE NOT FOUND " + app.getId());
			continue;
		}			
		System.err.println("\n=> BUSCANDO LITERAIS EM " + app.getId());
		literalProperties.addProperty(SYMBOLTABLE, st);
		literalProperties.addProperty("VERSION",version);
		ParseTreeWalker walker = new ParseTreeWalker();
		LiteralLocation literalLocation = new LiteralLocation(literalProperties);
		ParseTree tree = (ParseTree) version.getProperties().getProperty("AST");
		literalLocation.setArg("A SOMA DOS VALORES DE DEBITOS E CREDITOS NAO BATEM COM O V_SLD_RMU");
        walker.walk(literalLocation, tree);
        
        Map<String, List<ParserRuleContext>> locations = literalLocation.getLocation();
        
        PropertyList locProperties = new PropertyList();
        locProperties.addProperty("VERSION", version);
        locProperties.addProperty("COMP_UNIT_GRAPH", version.getProperties().getProperty("COMP_UNIT_GRAPH"));
        
        SymbolWeightedGraph symGraph = (SymbolWeightedGraph) version.getProperties().getProperty("COMP_UNIT_GRAPH");
        System.err.println(symGraph.toString());
        
        Path path = new Path(locProperties);
        String from = (String)version.getProperties().getProperty("ENTRY_GRAPH");
        path.setFrom((String)version.getProperties().getProperty("ENTRY_GRAPH"));
        
        for(Entry<String, List<ParserRuleContext>> e :  locations.entrySet()){
        	for(ParserRuleContext context : e.getValue()) {
        		ParserRuleContext ctx = NodeExplorer.getAncestorClass(context, "StmtContext", true);
        		String node = Integer.toString(ctx.start.getStartIndex());
        		String nodeLine = Integer.toString(ctx.start.getLine());
        		String inputNode = node + ":" + nodeLine;
        		
        		path.setTo(inputNode);
        		path.run();
        		
        		Set<Integer> shortestPath = path.getShortestPath();
        		//
        		String s = new String(inputNode.split(":")[1]);
        		Integer targetLine = new Integer(s);
        		//
        		
        		//=======================================================================
    			String dirName  = "C:/input";
    			String fileHtml = "C:/input" +  "/" + "PATH" + "_" + from.split(":")[1] + "_" + inputNode.split(":")[1] + ".html";
    			PropertyList pathProperties= new PropertyList();
    			
    			pathProperties.addProperty("MARKED_LINES", shortestPath);
    			pathProperties.addProperty("TARGET_LINE", targetLine);

    			try {
    				File htmlFile = BicamSystem.toHtml(version.getInputStream(), fileHtml, pathProperties);
    			} catch (IOException ee) {
    				ee.printStackTrace();
    			}
        	}
        }
	}		
//#####################################################################################################
//*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*  INICIO  #*#*#*#*#**#**##*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#	
	PropertyList sequenceProperties = new PropertyList();
	sequenceProperties.addProperty(SYMBOLTABLE, st);
	sequenceProperties.addProperty("SEPARATOR", "/");
	sequenceProperties.addProperty("STRING_BUFFER", new StringBuffer());
	
	DatabaseAccessSequenceBuilder dbAccessSequence = new DatabaseAccessSequenceBuilder();
	
	for(ApplicationComponent app : _appcopy.dependesOn()) {		
		if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue; 			
		Version version = (Version) app.getProperties().getProperty("VERSION");
		if(version  == null) {
			BicamSystem.printLog("WARNING", "*****ARRUMAR ************* PROCEDURE NOT FOUND " + app.getId());
			continue;
		}		
		sequenceProperties.addProperty("VERSION", version);
		sequenceProperties.addProperty("HTML_CONTENT_DIR_NAME", "C:/input");

		ParseTreeWalker walker = new ParseTreeWalker();
		dbAccessSequence.setProperties(sequenceProperties);
		ParseTree tree = (ParseTree) version.getProperties().getProperty("AST");
        walker.walk(dbAccessSequence, tree);
        System.err.println("/**** CompUnit: " + app.getId() + "*/" +System.lineSeparator() + dbAccessSequence.toGraphviz());
	}
	    System.err.println(dbAccessSequence.graphvizRankSame());
	    System.err.println(dbAccessSequence.graphvizNode());
	    System.err.println("}"); // end para graphviz
		
		
//*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*  FIM #*#*#*#*#*#*#*#*#*#*#**##*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*##		
//=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-   INÍCIO =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
		PropertyList procSequenceProperties = new PropertyList();
		procSequenceProperties.addProperty(SYMBOLTABLE, st);
		procSequenceProperties.addProperty("SEPARATOR", "/");
		procSequenceProperties.addProperty("STRING_BUFFER", new StringBuffer());
		
		ProcedureSequenceBuilder sqlProcSequence = new ProcedureSequenceBuilder();
		
		for(ApplicationComponent app : _appcopy.dependesOn()) {		
			if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue; 			
			Version version = (Version) app.getProperties().getProperty("VERSION");
			if(version  == null) {
				BicamSystem.printLog("WARNING", "*****ARRUMAR ************* PROCEDURE NOT FOUND " + app.getId());
				continue;
			}			
			procSequenceProperties.addProperty("VERSION", version);
			procSequenceProperties.addProperty("HTML_CONTENT_DIR_NAME", "C:/input");

			ParseTreeWalker walker = new ParseTreeWalker();
			sqlProcSequence.setProperties(procSequenceProperties);
			ParseTree tree = (ParseTree) version.getProperties().getProperty("AST");
	        walker.walk(sqlProcSequence, tree);
		}
		System.err.println(sqlProcSequence.toGraphviz());	
//=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-   FIM   =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   INÍCIO $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		//====================== Sequence Procedure ===============================
    PropertyList seqProperties = new PropertyList();
    seqProperties.addProperty("SYMBOLTABLE", st);
    NodeList allNodeList = new NodeList();
	for(ApplicationComponent app : _appcopy.dependesOn()) {		
		if(!app.getProperties().hasProperty("LANGUAGE", "TRANSACT_SYBASE")) continue; 			
		Version version = (Version) app.getProperties().getProperty("VERSION");
		if(version  == null) {
			BicamSystem.printLog("WARNING", "*****ARRUMAR ************* PROCEDURE NOT FOUND " + app.getId());
			continue;
		}	
		
		BicamSystem.printLog("INFO", " SEQUENCE CALL: " + app.getId());
		
		seqProperties.addProperty("VERSION", version);
		seqProperties.addProperty("NODELIST", new NodeList());
		ParseTreeWalker walker = new ParseTreeWalker();
		SequenceCallSqlTransactSybase sequenceCall = new SequenceCallSqlTransactSybase(seqProperties);
		ParseTree tree = (ParseTree) version.getProperties().getProperty("AST");
        walker.walk(sequenceCall, tree);
        System.err.println(sequenceCall.getNodeList().inputSymbolGraph(null));
        allNodeList.appendNodeList(sequenceCall.getNodeList());
	}
		
    System.err.println(">>> PROCEDURES : " + allNodeList.getNodes().size() + "\n" + allNodeList.toString());
    System.err.println(">>> PROCEDURES CALL: \n" + allNodeList.inputSymbolGraph(null));
        
        System.err.println(allNodeList.inputGraphviz(null));	
//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   INÍCIO $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//============================ FIM PROCESSA STORED PROCEDURES =================================	
	}
	
	private static Version getVersion(PropertyList _properties) {
		Catalog catalog = (Catalog)_properties.getProperty("CATALOG");
		String sourceId = (String)_properties.getProperty("SOURCE_ID");
		PropertyList properties = new PropertyList();
		properties.addProperty("ID", "SEGUROS");
		Repository repository = catalog.getRepository(properties);
		properties.addProperty("ID", sourceId);
		Source source = (Source)repository.getItem(properties);
		if(source == null) {
			properties.addProperty("ID", "SHARED");
			repository = catalog.getRepository(properties);
			properties.addProperty("ID", sourceId);
			source = (Source)repository.getItem(properties);
		}
		if(source == null) {
			return null;
		}
//		Version version = (Version)source.getItens().iterator().next();
		Version version = (Version)source.getMoreRecentItem();// corrigir para o + recente
		
		return version;
	}
}