package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SUSPICIOUS_COMPILATION_UNIT;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.GetThingByParameter;
import br.com.bicam.util.InputSource;
import br.com.bicam.util.KeywordVB6;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.graph.Path;
import br.com.bicam.util.graph.SymbolWeightedGraph;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class Aplication {

	File appDir;
	PropertyList properties;
	SymbolTable_New st;
	
	BicamNode appNode;
	
	Deque<CompilationUnit> compUnits;
	
	InputSource appSource;
	
	Map<String,Set<String>> multiplesCompUnitByModule;
	
	Set<String> compUnitVbNameList;
	Set<String> VbpModuleList;
	
	KeywordVB6  keywordLanguage;
	SymbolFactory symbolfactory;

	public Aplication(File _appDir) {
		this.appDir = _appDir;
		this.properties = new PropertyList();
		this.compUnitVbNameList = new HashSet<String>();
		this.VbpModuleList = new HashSet<String>();
		
		keywordLanguage = new KeywordVB6();

		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		
		symbolfactory = new SymbolFactory();
		
		properties.addProperty(SYMBOL_FACTORY, symbolfactory);

		st = new SymbolTable_New(properties);
		properties.addProperty(SYMBOLTABLE, st);		
//		properties.addProperty("FILE", file );
		
//		appSource = new InputSource(appDir); // Cria objeto de compilationf units relacionados

		PropertyList inputSrcProp = new PropertyList();
		ArrayList<File> orderedFileInputs = new ArrayList<File>();
		
		orderedFileInputs.add(appDir);
		// Se não acha os módulos em appDir, procura (na ordem) em "orderedFileInputs"
//		orderedFileInputs.add(new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\Shared_Modules"));
		
		inputSrcProp.addProperty("ORDERED_FILE_INPUTS",orderedFileInputs);
		
//		appSource = new InputSource(appDir); // Cria objeto de compilation units relacionados
		appSource = new InputSource(orderedFileInputs); 

		
		multiplesCompUnitByModule = new HashMap<String,Set<String>>();
		
		compUnits = new ArrayDeque<CompilationUnit>();
		
		run();
	}

	private void run() {
		//===============  START RUN METADATA VB6 - PROJECT FILE (VBP) ===========
		File vbpFile = new File(appSource.getFirstVbpFile());
		properties.addProperty("FILE", vbpFile );
		
        System.err.println("*** appSource.toString() " + appSource.toString());
        
        /*
         * Metadata: "Compilation Unit" que define dados.
         * Ex.: Project File(Vb6) - vbp file
         *      Schema de banco de dados 
         */
		
        Metadata projectVb6 = new MetadataProjectVb6(properties);         // Metadata for Vb6
		projectVb6.accept(new MetadataParserVisitor(properties)); 

		ParseTreeWalker walker = new ParseTreeWalker();
///		MetadataDefSymbolVb6 defSymMetada = new MetadataDefSymbolVb6(properties);
		MetadataVb6 defSymMetada = new MetadataVb6(properties);

		ParseTree tree = (ParseTree)properties.getProperty("AST");
        walker.walk(defSymMetada, tree);
///        System.err.println(defSymMetada.getNodeList().inputGraphviz(null));
//        properties.addProperty("SYMBOLTABLE", defSymMetada.getSymbolTable());
        
		//===============  END RUN METADATA VB6 - PROJECT FILE (VBP) =============
/*		VbpModules vbpModules = new VbpModules();
		tree = (ParseTree)properties.getProperty("AST");
        walker.walk(vbpModules, tree);
        List<String> moduleList = vbpModules.getModuleList();
        System.err.println("*** VBP MODULE LIST:" + moduleList);*/
        
        List<Symbol_New> appSymbolList = st.getSymbolByProperty("CATEGORY","APPLICATION");
        
        PropertyList propApp = appSymbolList.get(0).getProperties();
 //       Set<String> moduleNames = new HashSet<String>();
        
        List<Object> propModules = (List)propApp.getProperty("Module".toUpperCase());
        if(propModules != null) {
	        for(Object prop : propModules) {
	        	if(prop instanceof PropertyList)
	        		VbpModuleList.add((String)((PropertyList)prop).getProperty("NAME"));
	        }
        }
        
        List<Object> propClass = (List)propApp.getProperty("Class".toUpperCase());
        if(propClass != null) {
	        for(Object prop : propClass) {
	        	if(prop instanceof PropertyList)
	        		VbpModuleList.add((String)((PropertyList)prop).getProperty("NAME"));
	        }
        }
        
        List<Object> propForm = (List)propApp.getProperty("Form".toUpperCase());
        if(propForm != null) {
	        for(Object prop : propForm) {
	        	if(prop instanceof PropertyList)
	        		VbpModuleList.add((String)((PropertyList)prop).getProperty("NAME"));
	        }
        }
        
//======================  START VALIDA FILES EM PROJECT FILE VBP  ====================
        String nextModule = appSource.getFirstModuleFile(vbpFile);
	    
        createCompUnit(nextModule);
        
	    while(appSource.hasNextModuleFile(vbpFile)){
   	        nextModule = appSource.getNextModuleFile(vbpFile);
//   	        System.err.println("*** Compilation Unit File Name " + nextModule);
   	        createCompUnit(nextModule);
  	 	}
	    
// start Associa vbName à file name, garantindo que será usado o file name correto para determinado vbName
		for(CompilationUnit compUnit : compUnits){ 
			String vbName = getVbName(compUnit.getFileName());
			if(vbName !=null) {
				compUnit.getProperties().addProperty("MODULE_NAME", vbName);
				compUnitVbNameList.add(vbName);
				Set<String> compUnitFiles = multiplesCompUnitByModule.get(vbName);
				if(compUnitFiles == null) {
					compUnitFiles = new HashSet<String>();
					multiplesCompUnitByModule.put(vbName, compUnitFiles);
				}
				compUnitFiles.add(compUnit.getFileName()); 
			}
		}
     	
		for(String vbName : multiplesCompUnitByModule.keySet() ) {
			if(multiplesCompUnitByModule.get(vbName).size() == 1) {
				multiplesCompUnitByModule.get(vbName).clear();
			}
			else { // Existe mais de um "compilation Unit (file)" com o mesmo "vbname"
				checkIfCompUnitInVbpFile(vbName);
			}
		}
		
		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			properties.addProperty("COMPILATION_UNIT", compUnit);
			properties.addProperty(SUSPICIOUS_COMPILATION_UNIT, multiplesCompUnitByModule);  

			compUnit.accept(new CompilationUnitParserVisitor());
		
			compUnit.accept(new CompilationUnitDefSymbolVisitor());
		}
		
        System.err.println(VbpModuleList);
        System.err.println(compUnitVbNameList);
        
       for(String forStr : VbpModuleList) { // Para cada modulo definido em Vbp file deve existir seu "compilation Unit" correspondente
    	   boolean achou = false;
    	   for(String for1String : compUnitVbNameList) {
    		   if(for1String.equalsIgnoreCase(forStr)) {
    			   achou = true;
    			   break;
    		   }
    	   }
    	   if(!achou) { // Não achou compilation unit para módulo declarado em Vbp file
    		   PropertyList propFind = new PropertyList();
    		   propFind.addProperty("FILTER", forStr);
    		   appSource.findFile(propFind);
    		   File f = (File) propFind.getProperty("FILE");
    		   if(f != null) {
        		   System.err.println("*** MODULE FOUND " + forStr + " " + f);
        		   compUnitVbNameList.add(forStr);

    			   try {
    				   
    				CompilationUnit cunit = createCompUnit(f.getCanonicalPath());
    				PropertyList properties = cunit.getProperties();
    				properties.addProperty("COMPILATION_UNIT", cunit);
    				properties.addProperty(SUSPICIOUS_COMPILATION_UNIT, multiplesCompUnitByModule);
    				cunit.accept(new CompilationUnitParserVisitor());
    				
    				cunit.accept(new CompilationUnitDefSymbolVisitor());
    				
//					createCompUnit(f.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
    		   }
    		   else {
    			   System.err.println("*** MODULE NOT FOUND " + forStr);
    		   }
    		   System.err.println(propFind.getProperty("FILE"));
    	   }
       }
       System.err.println(compUnitVbNameList);       
        
//================ VERIFICA SE EXISTEM TODOS OS FONTES NECESSÁRIOS .FRM, .BAS, .CLS

/*        st = (SymbolTable_New) properties.getProperty("SYMBOLTABLE");

	Symbol_New appSymbol = st.getScopeByProperty(st.getGlobalScope(), "CATEGORY", "APPLICATION");

	if(appSymbol == null) {
		System.err.println("*** APLICATION HAS NO METADATA");
		System.exit(1);
	}
	
	PropertyList propApp = appSymbol.getProperties();

	PropertyList propFrms = (PropertyList)propApp.getProperty("Form");*/
	
	
	
//================================================================================		
		
		
		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			compUnit.accept(new CompilationUnitRefTypeSymbolVisitor());
		}
		
		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			compUnit.accept(new CompilationUnitRefSymbolVisitor());
		}
		
//		System.err.println(st);
		
		//=============================================================================		
		
/*	      	    PropertyList seqProperties = new PropertyList();
		        seqProperties.addProperty("SYMBOLTABLE", st);
				for(CompilationUnit compUnit : compUnits){ 	
					seqProperties.addProperty("COMPILATION_UNIT", compUnit);
					walker = new ParseTreeWalker();
					SequenceCallVb6 sequenceCall = new SequenceCallVb6(seqProperties);
					tree = (ParseTree) compUnit.getProperties().getProperty("AST");
			        walker.walk(sequenceCall, tree);
			        System.err.println(sequenceCall.toString());
			        System.err.println(sequenceCall.getNodeList().inputSymbolGraph(null));
				}*/
				
		//======================================================================================================		

		//======================SearchInFunctionCall========================================
/*		PropertyList searchProperties = new PropertyList();
		
		searchProperties.addProperty(SYMBOLTABLE, st);
		searchProperties.addProperty("FUNCTION_NAME","SQLRPCInit");
		searchProperties.addProperty("PARAMETER_NAME",null);
		searchProperties.addProperty("PARAMETER_REF","SqlConn");
		searchProperties.addProperty("PARAMETER_REF_POSITION",1);

		for(CompilationUnit compUnit : compUnits){ 			
			System.err.println("SEARCH IN FUNCTION CALL " + compUnit.getFileName());
			searchProperties.addProperty("COMPILATION_UNIT", compUnit);	
			searchProperties.addProperty(SYMBOL_FACTORY, symbolfactory);	
			searchProperties.addProperty("KEYWORD_LANGUAGE", keywordLanguage);	

			walker = new ParseTreeWalker();
			SearchInFunctionCall searchCall = new SearchInFunctionCall(searchProperties);
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(searchCall, tree);
		}*/
		
/*		// Lista stored procedures acessads por sqlrpcinit
		for(CompilationUnit compUnit : compUnits) { 		
			NodeList nodes = (NodeList) compUnit.getProperties().getProperty("NODES_SEQUENCE_CALL");
			StringBuffer sb = new StringBuffer();
			for(BicamNode node : nodes) {
				sb.append(node + System.lineSeparator()); 
			}
			System.err.println(sb.toString());
			System.err.println(" =======================  GRAPHVIZ ========================== ");
			System.err.println(nodes.inputGraphviz(new PropertyList()));
			System.err.println(nodes.inputSymbolGraph(null));
		}*/
		
/*		searchProperties.clear();
		searchProperties.addProperty(SYMBOLTABLE, st);
		searchProperties.addProperty("FUNCTION_NAME","SQLRPCInit");
		searchProperties.addProperty("PARAMETER_NAME","dbntribt..pr_fxa_ntz_tbt_s05352");
		searchProperties.addProperty("PARAMETER_REF","SqlConn");
		searchProperties.addProperty("PARAMETER_REF_POSITION",1);

		for(CompilationUnit compUnit : compUnits){ 			
			System.err.println("SEARCH IN FUNCTION CALL 2 " + compUnit.getFileName());
			searchProperties.addProperty("COMPILATION_UNIT", compUnit);			
			walker = new ParseTreeWalker();
			SearchInFunctionCall compUnitGraph = new SearchInFunctionCall(searchProperties);
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(compUnitGraph, tree);
		}
		
		searchProperties.clear();
		searchProperties.addProperty(SYMBOLTABLE, st);
		searchProperties.addProperty("FUNCTION_NAME","SQLRPCInit");
		searchProperties.addProperty("PARAMETER_NAME","dbntribt..pr_fxa_ntz_tbt_s05352");
		searchProperties.addProperty("PARAMETER_REF",null);
		searchProperties.addProperty("PARAMETER_REF_POSITION",null);

		for(CompilationUnit compUnit : compUnits){ 			
			System.err.println("SEARCH IN FUNCTION CALL 3 " + compUnit.getFileName());
			searchProperties.addProperty("COMPILATION_UNIT", compUnit);			
			walker = new ParseTreeWalker();
			SearchInFunctionCall compUnitGraph = new SearchInFunctionCall(searchProperties);
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(compUnitGraph, tree);
		}		
*/		
		
		//==================================================================================		
				PropertyList compUnitGraphProperties = new PropertyList();
				compUnitGraphProperties.addProperty(SYMBOLTABLE, st);
				compUnitGraphProperties.addProperty(SYMBOL_FACTORY,symbolfactory);
				compUnitGraphProperties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
				
				SymbolFactory symbolfactory = new SymbolFactory();
				for(CompilationUnit compUnit : compUnits){ 			
					System.err.println("BUILDING GRAHP FOR " + compUnit.getFileName());
					compUnitGraphProperties.addProperty("COMPILATION_UNIT", compUnit);			
					walker = new ParseTreeWalker();
//					CompilationUnitGraphVb6 compUnitGraph = new CompilationUnitGraphVb6(compUnitGraphProperties);
					GraphCompilationUnitVb6 compUnitGraph = new GraphCompilationUnitVb6(compUnitGraphProperties);

					tree = (ParseTree) compUnit.getProperties().getProperty("AST");
			        walker.walk(compUnitGraph, tree);
			        compUnit.getProperties().addProperty("COMP_UNIT_GRAPH", compUnitGraph.getGraph());
			        compUnit.getProperties().addProperty("GRAPH_LINE_TO_NODE", compUnitGraph.getLineToNodeId());
				}
//===================================== start html compunit file ======================================= 				
				for(CompilationUnit compUnit : compUnits){ 			
					String in = compUnit.getFileName();
					String out = in.split("\\\\")[in.split("\\\\").length-1];
					out = "c:\\HTML\\FILE_GRAPH_" + out.split("\\.")[0]  + ".HTML";
					Map<Integer,String> lineToNodeId = (Map<Integer, String>) compUnit.getProperties().getProperty("GRAPH_LINE_TO_NODE");
					PropertyList prop = new PropertyList();
					prop.addProperty("LINE_TO_NODE", lineToNodeId);
					BicamSystem.toHtml(in, out, prop);
				}
				
//===================================== end html compunit file ======================================= 				
		

//=============================== START TESTE SHORTEST PATH ================================================================		
				for(CompilationUnit compUnit : compUnits){ 
					if(!compUnit.getFileName().contains("TIFMA001")) continue;
			        PropertyList locProperties = new PropertyList();
			        locProperties.addProperty("COMPILATION_UNIT", compUnit);
			        locProperties.addProperty("COMP_UNIT_GRAPH", compUnit.getProperties().getProperty("COMP_UNIT_GRAPH"));
			        
			        SymbolWeightedGraph symGraph = (SymbolWeightedGraph) compUnit.getProperties().getProperty("COMP_UNIT_GRAPH");
			        System.err.println(symGraph.toString());
			        
			        Path path = new Path(locProperties);
/*			        String from = (String)compUnit.getProperties().getProperty("ENTRY_GRAPH");
			        path.setFrom((String)compUnit.getProperties().getProperty("ENTRY_GRAPH"));*/
//			        String from = "65711:1785";104425:2803
			        String from = "104425:2803";

			        path.setFrom(from);
			        
//			    	String inputNode = "66104:1797";106808:2866
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
        			PropertyList properties= new PropertyList();
        			
        			properties.addProperty("PATH_START_LINE", startPathLine);       			
        			properties.addProperty("PATH_MARKED_LINES", shortestPath);
        			properties.addProperty("PATH_TARGET_LINE", targetLine);
        			properties.addProperty("LINE_TO_NODE", compUnit.getProperties().getProperty("GRAPH_LINE_TO_NODE"));
        			
        			File htmlFile = BicamSystem.toHtml(compUnit.getFileName(), fileHtml, properties);
				}
				
//=============================== END TESTE SHORTEST PATH ======================================================================
		PropertyList literalProperties = new PropertyList();
		literalProperties.addProperty(SYMBOLTABLE, st);
		for(CompilationUnit compUnit : compUnits){ 			
			System.err.println("\n=> BUSCANDO LITERAIS EM " + compUnit.getFileName());
			literalProperties.addProperty(SYMBOLTABLE, st);
			literalProperties.addProperty("COMPILATION_UNIT",compUnit);
			walker = new ParseTreeWalker();
			LiteralLocation literalLocation = new LiteralLocation(literalProperties);
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
			literalLocation.setArg("NAO ALTEROU NENHUMA LINHA NA TB_CTL_PRT");
	        walker.walk(literalLocation, tree);
	        
	        Map<String, List<ParserRuleContext>> locations = literalLocation.getLocation();
	        
	        PropertyList locProperties = new PropertyList();
	        locProperties.addProperty("COMPILATION_UNIT", compUnit);
	        locProperties.addProperty("COMP_UNIT_GRAPH", compUnit.getProperties().getProperty("COMP_UNIT_GRAPH"));
	        
	        SymbolWeightedGraph symGraph = (SymbolWeightedGraph) compUnit.getProperties().getProperty("COMP_UNIT_GRAPH");
	        System.err.println(symGraph.toString());
	        
	        Path path = new Path(locProperties);
	        String from = (String)compUnit.getProperties().getProperty("ENTRY_GRAPH");
	        path.setFrom((String)compUnit.getProperties().getProperty("ENTRY_GRAPH"));
	        
	        for(Entry<String, List<ParserRuleContext>> e :  locations.entrySet()){
	        	for(ParserRuleContext context : e.getValue()) {
	        		ParserRuleContext ctx = NodeExplorer.getAncestorClass(context, "StmtContext", true);
	        		String node = Integer.toString(ctx.start.getStartIndex());
	        		String nodeLine = Integer.toString(ctx.start.getLine());
	        		String inputNode = node + ":" + nodeLine;
	        		
	        		path.setTo(inputNode);
	        		path.run();
	        		
	        		Set<Integer> shortestPath = path.getShortestPath();
	        		
	        		
//=======================================================================
        			String dirName  = "C:/input";
        			String fileHtml = "C:/input" +  "/" + "PATH" + "_" + from.split(":")[1] + "_" + inputNode.split(":")[1] + ".html";
        			PropertyList properties= new PropertyList();
        			
        			properties.addProperty("MARKED_LINES", shortestPath);
        			File htmlFile = BicamSystem.toHtml(compUnit.getFileName(), fileHtml, properties);
//=======================================================================
	        	}
	        }
		}		
//=====================================================================================================			
		
		
   	    VbFormToHtml_New vbFormToHtml = new VbFormToHtml_New("C:\\pgm\\HtmlNew.stg", st);

   	    formSequence();
   	    
   	    //=====================  END VALIDA FILES EM PROJECT FILE VBP =======================
 	}
	
	public PropertyList getProperties() {
		return properties;
	}
	
	private CompilationUnit createCompUnit(String _fileName) {
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
	}
	
    private String getVbName(String _file) {
	  try {
		 String	fileName = _file;
		 String REGEX = "^Attribute\\s+VB_Name\\s+=\\s+\\\"([\\d\\w]+)\\\"$";
		  //_file => "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
		  if(fileName == null) return null;
		  Pattern p = Pattern.compile(REGEX);
		  BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
          for (String line = in.readLine(); line != null; line = in.readLine()) {	 
              Matcher m = p.matcher(line);
        	  if((m.matches())) {
        		  return m.group(1);
        	  }
          }
          in.close();
	  }
	  catch (IOException e){
			  e.printStackTrace();
	  }
      return null;
  }

	private void checkIfCompUnitInVbpFile(String vbName) {
	    Set<String> filesWithSameVbName = multiplesCompUnitByModule.get(vbName);

		List<Symbol_New> ListOfMetaComponentSymbol = st.getSymbolByProperty(NAME, "METADATA_COMPONENT");
		Symbol_New metadataComponentSymbol  = null;
		if(ListOfMetaComponentSymbol.size() == 0) { // Não encontrou VBP file
			try {
				throw new Exception();
			} catch (Exception e) {
				System.err.println("*** ERROR: METADATA COMPONENTE NOT FOUND FOR APPLICATION %n");
				e.printStackTrace();
			}
			return;
		}
		else { // Pega o primeiro. Assumido que existe sómente 1. tratar essa situação.
			metadataComponentSymbol = ListOfMetaComponentSymbol.get(0); // Vbpfile
		}
		
		String compilationUnitName = null;
		for(String fileName : filesWithSameVbName) {
			compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];	
			List<Symbol_New> listSym = st.getSymbolByProperty(metadataComponentSymbol, "COMPILATION_UNIT_NAME", compilationUnitName.toUpperCase());
			if(listSym.size() > 0) {
				compilationUnitName = null;
				continue;
			}
			else break;
		}
		
		if(compilationUnitName != null) { // file not in VBP file
			CompilationUnit invalidCompUnit = null;
			for(CompilationUnit compUnit : compUnits) {
				String simpleFileName = compUnit.getFileName().split("\\\\")[compUnit.getFileName().split("\\\\").length-1];	
				if(simpleFileName.equalsIgnoreCase(compilationUnitName)) {
					invalidCompUnit = compUnit;
					break;
				}
			}
			if(invalidCompUnit != null) {
				System.err.format("*** WARNING: COMPILATION UNIT FILE '%s' IS NOT IN VBP FILE. IT WILL NOT BE PROCESSED.%n", invalidCompUnit.getFileName());
				compUnits.remove(invalidCompUnit);
			}
			else {
				try {
					System.err.println("*** ERROR: Trying delete invalid compUnitFile failed");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
//==================VERIFICA SE TODOS OS .FRM, .BAS E .CLS NO VBP TEM FONTES ===============
		List<Symbol_New> listOfCompUnits = st.getSymbolByProperty("CATEGORY", "APPLICATION_COMPONENT");
		
		for(CompilationUnit compUnit : compUnits){ 
			String fileName = compUnit.getFileName();
			String compilationUnitNameMeta = fileName.split("\\\\")[fileName.split("\\\\").length-1];	
			for(int ix=0; ix < listOfCompUnits.size(); ix++) {
				Symbol_New s = listOfCompUnits.get(ix);
				String compUnitParser = (String)s.getProperty("COMPILATION_UNIT_NAME");
				if(compUnitParser.equalsIgnoreCase(compilationUnitNameMeta)) {
					listOfCompUnits.remove(s);
					break;
				}
			}
		}
		
		if(listOfCompUnits.size() > 0) {
			try {
				throw new Exception();
			}catch (Exception e) {
				StringBuffer sb = new StringBuffer();
				for(int ix = 0; ix < listOfCompUnits.size(); ix++) {
					sb.append((String)listOfCompUnits.get(ix).getProperty("COMPILATION_UNIT_NAME") + " ");
				}
				System.err.format("*** ERROR: MISSING %d COMPILATION UNIT FILES PRESENTED IN VBP: %s%n", listOfCompUnits.size(), sb.toString());
				e.printStackTrace();
			}
		}
//==========================================================================================
} 
	
	private void formSequence() {
		StringBuffer sb = new StringBuffer();
		PropertyList properties = new PropertyList();
		properties.addProperty("SYMBOLTABLE", st);
		properties.addProperty("SEPARATOR", "/");
		
		List<Symbol_New> symAppList = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		
		String appEntry = (String)symApp.getProperty("Startup".toUpperCase());
		appEntry = appEntry.replace("\"", "");
		if(appEntry.split(" ").length > 1) {
			appEntry = appEntry.split(" ")[appEntry.split(" ").length-1];
			List<Symbol_New> lista = st.getSymbolByProperty(NAME, appEntry);
			appEntry = lista.get(0).getEnclosingScope().getName() + "." + appEntry;
		}
		
		System.err.println("**********************" + symApp.getName() 
		+ "**********************");
//		System.err.println(symApp.getName() + "/" + appEntry);
//		sb.append(symApp.getName() + "/" + appEntry + System.lineSeparator());	
		NodeList nodeList = new NodeList();
		
		properties.addProperty("NODELIST", nodeList);
		
		for(CompilationUnit p : compUnits){
			if (p.getFileName().toUpperCase().endsWith("VBP")) continue;

//			properties.addProperty("PARSED_FILE",p);
			ParseTreeWalker walker = new ParseTreeWalker();
//			UISequenceGraphBuilder uiGraphBuilder = new UISequenceGraphBuilder(properties);
//			SequenceUIVb6 uiGraphBuilder = new SequenceUIVb6(properties);
			SequenceCallVb6 uiGraphBuilder = new SequenceCallVb6(properties);

			ParseTree tree;
			tree = (ParseTree) p.getProperties().getProperty("AST");
	        walker.walk(uiGraphBuilder, tree);

	        
//	        System.err.println(uiGraphBuilder.getInputGraph()+ System.lineSeparator());
	        try {
				System.err.println("**********************" + p.getFileName()  +"/"+uiGraphBuilder.getCompUnitName() 
				+ "**********************");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
	        
	        if(appNode == null) {
	        	appNode = (BicamNode) properties.getProperty("APP_NODE");
	        }
//	        System.err.println("-->\n" + uiGraphBuilder.getInputGraph());
	        
//	        sb.append(uiGraphBuilder.getInputGraph());
//	        sb.append(uiGraphBuilder.getNodelist().inputSymbolGraph());
		}
		
//		System.err.println(sb.toString());
		
		System.err.println(nodeList.inputSymbolGraph());
		
		properties.addProperty("NODE_LIST", nodeList);

		properties.addProperty("DB_ACCESS_NODE_MAP", new HashMap<String,List<String>>());
		properties.addProperty("APP_NODE", appNode);
		
		
		for(CompilationUnit p : compUnits){
			if (p.getFileName().toUpperCase().endsWith("VBP")) continue;
			properties.addProperty("BASE_NAME", "SQLRPCInit");
			properties.addProperty("BASE_PARAMETER_NAME", "SqlConn");
			properties.addProperty("BASE_PARAMETER_INDEX", null);

			properties.addProperty("TARGET_PARAMETER_NAME", null);
			properties.addProperty("TARGET_PARAMETER_INDEX", 1);

			ParseTreeWalker walker = new ParseTreeWalker();
			GetThingByParameter thingByParm = new GetThingByParameter(properties);
			ParseTree tree;
			tree = (ParseTree) p.getProperties().getProperty("AST");
	        walker.walk(thingByParm, tree);

	        
//	        System.err.println(uiGraphBuilder.getInputGraph()+ System.lineSeparator());
	        try {
				System.err.println("**********************" + p.getFileName()  +"/"+ thingByParm.getStoredProcedures() 
				+ "**********************");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		
		System.err.println(nodeList.inputSymbolGraph(null));
		
		
		
		for(CompilationUnit p : compUnits){
			if (p.getFileName().toUpperCase().endsWith("VBP")) continue;
			properties.addProperty("BASE_NAME", "SQLRPCInit");
			properties.addProperty("BASE_PARAMETER_NAME", "SqlConn");
			properties.addProperty("BASE_PARAMETER_INDEX", null);

			properties.addProperty("TARGET_PARAMETER_NAME", null);
			properties.addProperty("TARGET_PARAMETER_INDEX", 1);


			ParseTreeWalker walker = new ParseTreeWalker();
			GetThingByParameter thingByParm = new GetThingByParameter(properties);
			ParseTree tree;
			tree = (ParseTree) p.getProperties().getProperty("AST");
	        walker.walk(thingByParm, tree);

	        
//	        System.err.println(uiGraphBuilder.getInputGraph()+ System.lineSeparator());
	        try {
				System.err.println("*****PROCEDURES *****************" + p.getFileName()  +"/"+ thingByParm.getStoredProcedures() 
				+ "**********************");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		
//        If FU_Parametro_Ret(SqlConn, "@i_exi_cvd_cia", ==> i_exi_cvd_cia$, SQLCHAR%) = FAIL Then SU_Saida
//      GetThingByParameter
		for(CompilationUnit p : compUnits){
			if (p.getFileName().toUpperCase().endsWith("VBP")) continue;
			properties.addProperty("BASE_NAME", "FU_Parametro");
			properties.addProperty("BASE_PARAMETER_NAME", "SqlConn");
			properties.addProperty("BASE_PARAMETER_INDEX", null);

			properties.addProperty("TARGET_PARAMETER_NAME", null);
			properties.addProperty("TARGET_PARAMETER_INDEX", 2);


			ParseTreeWalker walker = new ParseTreeWalker();
			GetThingByParameter thingByParm = new GetThingByParameter(properties);
			ParseTree tree;
			tree = (ParseTree) p.getProperties().getProperty("AST");
	        walker.walk(thingByParm, tree);

	        
//	        System.err.println(uiGraphBuilder.getInputGraph()+ System.lineSeparator());
	        try {
				System.err.println("***** parâmetros da procedure *****************" + p.getFileName()  +"/"+ thingByParm.getStoredProcedures() 
				+ "**********************");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}		
//        If FU_Parametro_Ret(SqlConn, "@c_err", ==> "0", SQLINT4%) = FAIL Then SU_Saida
//      GetThingByParameter
		for(CompilationUnit p : compUnits){
			if (p.getFileName().toUpperCase().endsWith("VBP")) continue;
			properties.addProperty("BASE_NAME", "FU_Parametro_Ret");
			properties.addProperty("BASE_PARAMETER_NAME", "SqlConn");
			properties.addProperty("BASE_PARAMETER_INDEX", null);

			properties.addProperty("TARGET_PARAMETER_NAME", null);
			properties.addProperty("TARGET_PARAMETER_INDEX", 2);


			ParseTreeWalker walker = new ParseTreeWalker();
			GetThingByParameter thingByParm = new GetThingByParameter(properties);
			ParseTree tree;
			tree = (ParseTree) p.getProperties().getProperty("AST");
	        walker.walk(thingByParm, tree);

	        
//	        System.err.println(uiGraphBuilder.getInputGraph()+ System.lineSeparator());
	        try {
				System.err.println("***** parâmetros da procedure com retorno *****************" + p.getFileName()  +"/"+ thingByParm.getStoredProcedures() 
				+ "**********************");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
/*		List<Symbol_New> appList = st.getSymbolByProperty("CATEGORY","APPLICATION");
		String appEntry = (String)appList.get(0).getProperty("Startup"); // Startup="Sub Main" or Startup="R1FAB001" or ...
		appEntry = appEntry.replaceAll("\"", "");
		appEntry = appEntry.split(" ")[appEntry.split(" ").length-1];
		sb.append();*/
		
		
        System.err.println("\n=======================");
        System.err.println(sb.toString());		
	}	
	
	public static void main(String[] args) {
   	 File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TIPMA00");
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\bank_vb_project"); 
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0"); 
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\CRPSV0"); 

		
		Aplication app = new Aplication(file);
		SymbolTable_New st = (SymbolTable_New) app.getProperties().getProperty("SYMBOLTABLE");
		System.err.println(st.toString());
	}
}
