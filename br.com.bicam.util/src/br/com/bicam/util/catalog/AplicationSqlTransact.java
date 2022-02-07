package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;
import static br.com.bicam.util.constant.PropertyName.NAME;
import static br.com.bicam.util.constant.PropertyName.SYMBOLTABLE;
import static br.com.bicam.util.constant.PropertyName.SYMBOL_FACTORY;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.GetThingByParameter;
import br.com.bicam.util.InputSourceProcedure;
import br.com.bicam.util.KeywordSqlSybase;
import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.graph.Path;
import br.com.bicam.util.graph.SymbolWeightedGraph;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class AplicationSqlTransact {

	File appDir;
	PropertyList properties;
	SymbolTable_New st;
	
	Deque<CompilationUnit> compUnits;
	
	InputSourceProcedure inputSource;
	
	
	Set<String> compUnitNameList;
	
	KeywordSqlSybase  keywordLanguage;
	
	SymbolFactory symbolfactory;

	public AplicationSqlTransact(File _appDir) {
		this.appDir = _appDir;
		this.properties = new PropertyList();
		this.compUnitNameList = new HashSet();
		
		keywordLanguage = new KeywordSqlSybase();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		
		symbolfactory = new SymbolFactory();
		properties.addProperty(SYMBOL_FACTORY, symbolfactory);

		st = new SymbolTable_New(properties);
		properties.addProperty(SYMBOLTABLE, st);		

		PropertyList inputSrcProp = new PropertyList();
		ArrayList<File> orderedFileInputs = new ArrayList<File>();
		
		orderedFileInputs.add(appDir);
		// Se não acha os módulos em appDir, procura (na ordem) em "orderedFileInputs"
		
		inputSrcProp.addProperty("ORDERED_FILE_INPUTS",orderedFileInputs);
		
//		appSource = new InputSource(appDir); // Cria objeto de compilation units relacionados
		inputSource = new InputSourceProcedure(orderedFileInputs); 

		compUnits = new ArrayDeque<CompilationUnit>();
		
		run();
	}

	private void run() {
		
//    	System.err.println(inputSource.getFirstModuleDir());
    	
    	String nextModule = inputSource.getFirstModuleDir();
    	System.err.println(nextModule);
        createCompUnit(nextModule);
    	
    	while(inputSource.hasNextModuleDir()) {
    		nextModule = inputSource.getNextModuleDir();
        	System.err.println(nextModule);
        	
            createCompUnit(nextModule);
    	}        
		
		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			properties.addProperty("COMPILATION_UNIT", compUnit);

			compUnit.accept(new CompilationUnitParserVisitor());
			
			compUnit.accept(new CompilationUnitDefSymbolVisitor());
		}
		
		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			compUnit.accept(new CompilationUnitRefTypeSymbolVisitor());
		}
		
		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			compUnit.accept(new CompilationUnitRefSymbolVisitor());
		}		
		
/*		for(CompilationUnit compUnit : compUnits){ 			
			ParseTreeWalker walker = new ParseTreeWalker();
			DatabaseAccess dbAccess = new DatabaseAccess(st);
			ParseTree tree;
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(dbAccess, tree);
	        System.err.println("*** CompUnit: " + compUnit.getProperties().getProperty("MODULE_NAME") + " - " + dbAccess.getTableList());
		}*/
//=============================== INÍCIO CODE GRAPH =======================================================================		
		PropertyList compUnitGraphProperties = new PropertyList();
		compUnitGraphProperties.addProperty(SYMBOLTABLE, st);
		compUnitGraphProperties.addProperty(SYMBOL_FACTORY,symbolfactory);
		compUnitGraphProperties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		
		SymbolFactory symbolfactory = new SymbolFactory();
		for(CompilationUnit compUnit : compUnits){ 			
			System.err.println("BUILDING GRAHP FOR " + compUnit.getFileName());
			compUnitGraphProperties.addProperty("COMPILATION_UNIT", compUnit);			
			ParseTreeWalker walker = new ParseTreeWalker();
			CompilationUnitSqlTransactSybaseGraph compUnitGraph = new CompilationUnitSqlTransactSybaseGraph(compUnitGraphProperties);
			ParseTree tree;
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(compUnitGraph, tree);
	        compUnit.getProperties().addProperty("COMP_UNIT_GRAPH", compUnitGraph.getGraph());
		}
//=============================== FIM CODE GRAPH =======================================================================		
		
//=============================== INÍCIO CALL GRAPH INPUT =======================================================================		
		PropertyList callProperties = new PropertyList();
		callProperties.addProperty(SYMBOLTABLE, st);
		
		NodeList nodeList = new NodeList();
		callProperties.addProperty("NODELIST", nodeList);		
		
		for(CompilationUnit compUnit : compUnits){ 			
			callProperties.addProperty("COMPILATION_UNIT", compUnit);			
			ParseTreeWalker walker = new ParseTreeWalker();
			SequenceCallSqlTransactSybase compUnitGraph = new SequenceCallSqlTransactSybase(callProperties);
			ParseTree tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(compUnitGraph, tree);
		}
		System.err.println("====================== CALL GRAPH INPUT ");
		System.err.println(nodeList.inputSymbolGraph(null));
//=============================== FIM CALL GRAPH INPUT =======================================================================		

//=============================== INÍCIO SQL GRAPH INPUT =======================================================================		
//		System.err.println("====================== SQL GRAPH INPUT ");
		PropertyList sqlProperties = new PropertyList();
		sqlProperties.addProperty(SYMBOLTABLE, st);
		
		sqlProperties.addProperty("NODELIST", nodeList);		
		
		for(CompilationUnit compUnit : compUnits){ 			
			sqlProperties.addProperty("COMPILATION_UNIT", compUnit);			
			ParseTreeWalker walker = new ParseTreeWalker();
			DbAccessSqlTransactSybase compUnitGraph = new DbAccessSqlTransactSybase(callProperties);
			ParseTree tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(compUnitGraph, tree);
		}
		
		System.err.println("====================== SQL GRAPH INPUT ");		
		System.err.println(nodeList.inputSymbolGraph(null));
//=============================== FIM SQL GRAPH INPUT =======================================================================		
		
//================================= INÍCIO LOCALIZAÇÃO DE LITERIAIS ============================
		PropertyList literalProperties = new PropertyList();
		literalProperties.addProperty(SYMBOLTABLE, st);
		for(CompilationUnit compUnit : compUnits){ 			
			System.err.println("\n=> BUSCANDO LITERAIS EM " + compUnit.getFileName());
			literalProperties.addProperty(SYMBOLTABLE, st);
			literalProperties.addProperty("COMPILATION_UNIT",compUnit);
			ParseTreeWalker walker = new ParseTreeWalker();
			LiteralLocation literalLocation = new LiteralLocation(literalProperties);
			ParseTree tree;
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
//					literalLocation.setArg("NAO ALTEROU NENHUMA LINHA NA TB_CTL_PRT");
			literalLocation.setArg("A SOMA DOS VALORES DE DEBITOS E CREDITOS NAO BATEM COM O V_SLD_RMU");
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
	        		//
	        		String s = new String(inputNode.split(":")[1]);
	        		Integer targetLine = new Integer(s);
	        		//
	        		
	        		//=======================================================================
        			String dirName  = "C:/input";
        			String fileHtml = "C:/input" +  "/" + "PATH" + "_" + from.split(":")[1] + "_" + inputNode.split(":")[1] + ".html";
        			PropertyList properties= new PropertyList();
        			
        			properties.addProperty("MARKED_LINES", shortestPath);
        			properties.addProperty("TARGET_LINE", targetLine);

        			File htmlFile = BicamSystem.toHtml(compUnit.getFileName(), fileHtml, properties);
	        		//=======================================================================
	        	}
	        }
		}
//================================= END LOCALIZAÇÃO DE LITERIAIS ============================
		
//=====================================================================================================		
//				PropertyList procSequenceProperties = new PropertyList();
//				procSequenceProperties.addProperty(SYMBOLTABLE, st);
//				procSequenceProperties.addProperty("SEPARATOR", "/");
//				procSequenceProperties.addProperty("STRING_BUFFER", new StringBuffer());
//				
//				SqlProcedureSequenceBuilder sqlProcSequence = new SqlProcedureSequenceBuilder();
//				
//				for(CompilationUnit compUnit : compUnits){ 			
//					procSequenceProperties.addProperty("COMPILATION_UNIT", compUnit);
//					procSequenceProperties.addProperty("HTML_CONTENT_DIR_NAME", "C:/input");
//
//					ParseTreeWalker walker = new ParseTreeWalker();
////					dbAccessSequence = new DatabaseAccessSequenceBuilder(sequenceProperties);
//					sqlProcSequence.setProperties(procSequenceProperties);
//					ParseTree tree;
//					tree = (ParseTree) compUnit.getProperties().getProperty("AST");
//			        walker.walk(sqlProcSequence, tree);
////			        System.err.println("/**** CompUnit: " + compUnit.getProperties().getProperty("MODULE_NAME") + "*/" +System.lineSeparator() + dbAccessSequence.toGraphviz());
//				}
				
//======================================================================================
		PropertyList sequenceProperties = new PropertyList();
		sequenceProperties.addProperty(SYMBOLTABLE, st);
		sequenceProperties.addProperty("SEPARATOR", "/");
		sequenceProperties.addProperty("STRING_BUFFER", new StringBuffer());
		
		DatabaseAccessSequenceBuilder dbAccessSequence = new DatabaseAccessSequenceBuilder();
		
		for(CompilationUnit compUnit : compUnits){ 			
			sequenceProperties.addProperty("COMPILATION_UNIT", compUnit);
			sequenceProperties.addProperty("HTML_CONTENT_DIR_NAME", "C:/input");

			ParseTreeWalker walker = new ParseTreeWalker();
//			dbAccessSequence = new DatabaseAccessSequenceBuilder(sequenceProperties);
			dbAccessSequence.setProperties(sequenceProperties);
			ParseTree tree;
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(dbAccessSequence, tree);
	        System.err.println("/**** CompUnit: " + compUnit.getProperties().getProperty("MODULE_NAME") + "*/" +System.lineSeparator() + dbAccessSequence.toGraphviz());
		}
		    System.err.println(dbAccessSequence.graphvizRankSame());
		    System.err.println(dbAccessSequence.graphvizNode());
		    System.err.println("}"); // end para graphviz

//		    System.err.println(DatabaseAccessSequenceBuilder.graphvizNode());

		
/*		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			compUnit.accept(new CompilationUnitRefTypeSymbolVisitor());
		}
		
		for(CompilationUnit compUnit : compUnits){ 			
			PropertyList properties = compUnit.getProperties();
			compUnit.accept(new CompilationUnitRefSymbolVisitor());
		}*/
		//=====================================================================================================		
		PropertyList procSequenceProperties = new PropertyList();
		procSequenceProperties.addProperty(SYMBOLTABLE, st);
		procSequenceProperties.addProperty("SEPARATOR", "/");
		procSequenceProperties.addProperty("STRING_BUFFER", new StringBuffer());
		
		ProcedureSequenceBuilder sqlProcSequence = new ProcedureSequenceBuilder();
		
		for(CompilationUnit compUnit : compUnits){ 			
			procSequenceProperties.addProperty("COMPILATION_UNIT", compUnit);
			procSequenceProperties.addProperty("HTML_CONTENT_DIR_NAME", "C:/input");

			ParseTreeWalker walker = new ParseTreeWalker();
			sqlProcSequence.setProperties(procSequenceProperties);
			ParseTree tree;
			tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(sqlProcSequence, tree);
		}
		System.err.println(sqlProcSequence.toGraphviz());

/*//====================== Sequence Procedure ===============================
  	    PropertyList seqProperties = new PropertyList();
        seqProperties.addProperty("SYMBOLTABLE", st);
        NodeList allNodeList = new NodeList();
		for(CompilationUnit compUnit : compUnits){ 	
			seqProperties.addProperty("COMPILATION_UNIT", compUnit);
			ParseTreeWalker walker = new ParseTreeWalker();
			SequenceCallSqlTransactSybase sequenceCall = new SequenceCallSqlTransactSybase(seqProperties);
			ParseTree tree = (ParseTree) compUnit.getProperties().getProperty("AST");
	        walker.walk(sequenceCall, tree);
//	        System.err.println(sequenceCall.toString());
	        System.err.println(sequenceCall.getNodeList().inputSymbolGraph(null));
	        allNodeList.appendNodeList(sequenceCall.getNodeList());
		}
		
        System.err.println(">>> PROCEDURES : " + allNodeList.getNodes().size() + "\n" + allNodeList.toString());
        System.err.println(">>> PROCEDURES CALL: \n" + allNodeList.inputSymbolGraph(null));
        
        System.err.println(allNodeList.inputGraphviz(null));

  String debug = null;	*/	
//=========================================================================		
 	}
	
	public PropertyList getProperties() {
		return properties;
	}
	
	private CompilationUnit createCompUnit(String _procName) {
//	    System.err.println("*** Compilation Unit File Name " + inputSource.getFile(_procName));
	    
	    PropertyList propertiesX = new PropertyList();
	    
//		KeywordVB6  keywordLanguage = new KeywordVB6();
		KeywordSqlSybase keywordLanguage = new KeywordSqlSybase();
		
		propertiesX.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		
		SymbolFactory symbolfactory = new SymbolFactory();
		propertiesX.addProperty(SYMBOL_FACTORY, symbolfactory);

		propertiesX.addProperty(SYMBOLTABLE, st);
		propertiesX.addProperty("FILE", inputSource.getFile(_procName));
		
	    compUnits.add(new CompilationUnitSqlTransactSybase(propertiesX));	
	    compUnits.getLast().getProperties().addProperty("MODULE_NAME", _procName);
	    return compUnits.getLast();
	}
	
	private void formSequence() {
		StringBuffer sb = new StringBuffer();
		PropertyList properties = new PropertyList();
		properties.addProperty("SYMBOLTABLE", st);
		properties.addProperty("SEPARATOR", "/");
		
		List<Symbol_New> symAppList = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		Symbol_New symApp = symAppList.get(0);
		
		String appEntry = (String)symApp.getProperty("Startup");
		appEntry = appEntry.replace("\"", "");
		if(appEntry.split(" ").length > 1) {
			appEntry = appEntry.split(" ")[appEntry.split(" ").length-1];
			List<Symbol_New> lista = st.getSymbolByProperty(NAME, appEntry);
			appEntry = lista.get(0).getEnclosingScope().getName() + "." + appEntry;
		}
		
		System.err.println("**********************" + symApp.getName() 
		+ "**********************");
		System.err.println(symApp.getName() + "/" + appEntry);
		sb.append(symApp.getName() + "/" + appEntry + System.lineSeparator());	
		
		for(CompilationUnit p : compUnits){
			if (p.getFileName().toUpperCase().endsWith("VBP")) continue;

//			properties.addProperty("PARSED_FILE",p);
			ParseTreeWalker walker = new ParseTreeWalker();
			UISequenceGraphBuilder uiGraphBuilder = new UISequenceGraphBuilder(properties);
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
	        System.err.println(uiGraphBuilder.getInputGraph());
	        
	        sb.append(uiGraphBuilder.getInputGraph());
		}
		
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

		
		
        System.err.println("\n=======================");
        System.err.println(sb.toString());		
	}	
	public static void main(String[] args) {
//   	 File file = new File("C:\\workspace\\workspace_desenv_java8\\sybase\\antlr4.transactSql\\input");
  	 File file = new File("C:\\workspace\\workspace_desenv_java8\\sybase\\antlr4.transactSql\\input\\ConversionVb6");

		//		File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\bank_vb_project"); 
//		File file = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0"); 

		AplicationSqlTransact app = new AplicationSqlTransact(file);
		SymbolTable_New st = (SymbolTable_New) app.getProperties().getProperty("SYMBOLTABLE");
		System.err.println(st.toString());
	}
}
