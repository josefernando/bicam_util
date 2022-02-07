package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.COMPILATION_UNIT;
import static br.com.bicam.util.constant.PropertyName.CONTEXT;
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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.model.visualbasic.ApplicationModel;
import br.com.bicam.model.visualbasic.CompilationUnitComponent;
import br.com.bicam.util.KeywordVB6;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.Member;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class Catalog_New{
	
	ArrayList<Parser_New> parsers;
	PropertyList properties; 
	SymbolTable_New st;
	KeywordVB6 keywordLanguage;
	//Set<String> suspiciousCompUnits;
	Map<String,Set<String>> suspiciousCompUnits;
	private Deque<Symbol_New> toVisit;
	private IdentityHashMap<Symbol_New, CompilationUnitComponent> toVisit1;
	CompilationUnitComponent currentParent;
	
	public Catalog_New(ArrayList<Parser_New> _parsers) { //, PropertyList _propertyList){
		this.parsers = _parsers;
//		this.suspiciousCompUnits = new HashSet<String>();
		suspiciousCompUnits = new HashMap<String,Set<String>>();
		toVisit = new ArrayDeque<Symbol_New>();
		toVisit1 = new IdentityHashMap<Symbol_New, CompilationUnitComponent>();
			run();
	}
	
/*	public Set<String> getSuspiciousCompUnits() {
		return suspiciousCompUnits;
	}*/
	
	private void run() {
		properties = new PropertyList();
		
		KeywordVB6  keywordLanguage = new KeywordVB6();
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage); 		
		
		SymbolFactory symbolfactory = new SymbolFactory();
		properties.addProperty(SYMBOL_FACTORY, symbolfactory);

		st = new SymbolTable_New(properties);
		properties.addProperty(SYMBOLTABLE, st);
	
//		Map<String,String> vbNameMap = new HashMap<String,String>();
		
		for(Parser_New parser : parsers){ 
			String vbName = getVbName(parser);
			if(vbName !=null) { // Ainda não existe file para este vbName
				Set<String> compUnitFiles = suspiciousCompUnits.get(vbName);
				if(compUnitFiles == null) {
					compUnitFiles = new HashSet<String>();
					suspiciousCompUnits.put(vbName, compUnitFiles);
				}
				compUnitFiles.add(parser.getFileName()); 
			}
		}
		
		properties.addProperty(SUSPICIOUS_COMPILATION_UNIT, suspiciousCompUnits);
		
		// Define Symbols
		DefSymbolVisitor_New defVisitor = new DefSymbolVisitorVb_New();
		
		for(Parser_New parser : parsers){ 
			if(parser instanceof MetadataVbParser_New){
					properties.addProperty(COMPILATION_UNIT, parser.getFileName());
				st = parser.accept(defVisitor, properties);				
			}
		}
		
		for(String vbName : suspiciousCompUnits.keySet() ) {
			if(suspiciousCompUnits.get(vbName).size() == 1) {
				suspiciousCompUnits.get(vbName).clear();
			}
			else {
				checkCompUnitIsVbpFile(vbName);
			}
		}
		
		for(Parser_New parser : parsers){ 
			if(!(parser instanceof MetadataVbParser_New)){
					properties.addProperty(COMPILATION_UNIT, parser.getFileName());
				st = parser.accept(defVisitor, properties);				
			}
		}
		
		RefTypeSymbolVisitor_New refTypeSymbolVisitor = new RefTypeSymbolVbVisitor_New();
		
		for(Parser_New parser : parsers){ 
			if(!(parser instanceof MetadataVbParser_New)){
					properties.addProperty(COMPILATION_UNIT, parser.getFileName());
				st = parser.accept(refTypeSymbolVisitor, properties);				
			}
		}
		
   	 	
//   	 	System.err.println(getSymbolTable().toString());
		
		RefSymbolVisitor_New refSymbolVisitor = new RefSymbolVbVisitor_New();
		
		for(Parser_New parser : parsers){ 
			if(!(parser instanceof MetadataVbParser_New)){
					properties.addProperty(COMPILATION_UNIT, parser.getFileName());
				st = parser.accept(refSymbolVisitor, properties);				
			}
		}		
	}
	
	public SymbolTable_New getSymbolTable(){
		return st;
	}
	
    private String getVbName(Parser_New _parser) {
	  try {
		 String	fileName = _parser.getFileName();
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
    
    private String ModuleCount(Parser_New _parser) {  // para terminar
	  try {
		 String	fileName = _parser.getFileName();
		 String REGEX = "^Form=(\\w+\\.\\w+)";
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
    
	private void checkCompUnitIsVbpFile(String vbName) {
		    
		    Set<String >suspiciousCompUnitFiles = suspiciousCompUnits.get(vbName);

			List<Symbol_New> ListOfMetaComponentSymbol = st.getSymbolByProperty(NAME, "METADATA_COMPONENT");
			Symbol_New metaComponentSymbol  = null;
			if(ListOfMetaComponentSymbol.size() == 0) {
				try {
					throw new Exception();
				} catch (Exception e) {
					System.err.println("*** ERROR: METADATA COMPONENTE NOT FOUND FOR APPLICATION %n");
					e.printStackTrace();
				}
				return;
			}
			else {
				metaComponentSymbol = ListOfMetaComponentSymbol.get(0);
			}
			
			String compilationUnitName = null;
			for(String fileName : suspiciousCompUnitFiles) {
				compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];	
				List<Symbol_New> listSym = st.getSymbolByProperty(metaComponentSymbol, "COMPILATION_UNIT_NAME", compilationUnitName.toUpperCase());
				if(listSym.size() > 0) {
					compilationUnitName = null;
					continue;
				}
				else break;
			}
			
			if(compilationUnitName != null) { // file not in VBP file
				Parser_New invalidParser = null;
				for(Parser_New parser : parsers) {
					String simpleFileName = parser.getFileName().split("\\\\")[parser.getFileName().split("\\\\").length-1];	
					if(simpleFileName.equalsIgnoreCase(compilationUnitName)) {
						invalidParser = parser;
						break;
					}
				}
				if(invalidParser != null) {
					System.err.format("*** WARNING: COMPILATION UNIT FILE '%s' IS NOT IN VBP FILE. IT WILL NOT BE PROCESSED.%n", invalidParser.getFileName());
					parsers.remove(invalidParser);
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
			
			for(Parser_New parser : parsers){ 
				String fileName = parser.getFileName();
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
	
	public CompilationUnitComponent reportApplicationXComponents(){
		List<Symbol_New> symbols = st.getSymbolByProperty("CATEGORY","APPLICATION");
		Symbol_New symApplication = symbols.get(0);
		ApplicationModel appModel =   new ApplicationModel(symApplication.getProperties().getPropertyListSerializable());
		
//		currentParent = projectModel;
//		addProperties(symPrj, projectModel);
		toVisit.push(symApplication);
		toVisit1.put(symApplication, appModel);
		while(!toVisit.isEmpty()){
			currentParent = toVisit1.get(toVisit.peek());
			toVisit1.remove(toVisit.peek());
			addChildren(toVisit.pop());
		}
		return appModel;
	}
	
	private void addChildren(Symbol_New _parent){
		if(_parent.getMembers().isEmpty()) return;

		for( Member members : _parent.getMembers().values()){
			for(Symbol_New s : members.getSymbols()){
//					if(!(s.getProperty("ENCLOSING_SCOPE")).equals(parent))
//						continue; // Não pertence a esse escopo e está aqui apenas para a resolução no símbolo

//					CompilationUnitComponent childComp = filterReportApplicationXComponents(s);
					CompilationUnitComponent childComp = filterReportApplicationXModules(s);
					if(childComp == null) continue;
					currentParent.addComponent(childComp);
					toVisit.push(s);
					toVisit1.put(s, childComp);
			}
		}
	}
	
	private CompilationUnitComponent filterReportApplicationXComponents(Symbol_New _symbol){
//		String name = (String)_symbol.getProperty("NAME");
		String category = (String)_symbol.getProperty("CATEGORY");
		String categoryType = (String)_symbol.getProperty("CATEGORY_TYPE");
		
		if(_symbol.getProperty("CATEGORY") == null){
			try{
			throw new Exception();
			} catch (Exception e){
				ParserRuleContext context = (ParserRuleContext) _symbol.getProperty(CONTEXT);
				System.err.format("*** ERROR: SYMBOL '%s' WITHOUT CATEGORY in COMPILATION UNIT '%s' in line %d%n"
		                ,_symbol.getProperty(NAME), st.getCompilarionUnitSymbol(context).getName()
		                , context.start.getLine());
				return null;
			}
		}
		
		if(categoryType == null){
			try{
			throw new Exception();
			} catch (Exception e){
				ParserRuleContext context = (ParserRuleContext) _symbol.getProperty(CONTEXT);
				System.err.format("*** ERROR: SYMBOL '%s' WITHOUT CATEGORY_TYPE in COMPILATION UNIT '%s' in line %d%n"
		                ,_symbol.getProperty(NAME), st.getCompilarionUnitSymbol(context).getName()
		                , context.start.getLine());
				return null;
			}
		}		
		
		if(category.equalsIgnoreCase("UI") && categoryType.equalsIgnoreCase("FORM")){
			CompilationUnitComponent component = new CompilationUnitComponent(_symbol.getProperties().getPropertyListSerializable());
			component.getProperties().setProperties(_symbol.getProperties());
			return component;
		}
		else if(category.equalsIgnoreCase("UI") && categoryType.equalsIgnoreCase("MDIForm")){
			CompilationUnitComponent component = new CompilationUnitComponent(_symbol.getProperties().getPropertyListSerializable());
			component.getProperties().setProperties(_symbol.getProperties());
			return component;
		}
		else if(category.equalsIgnoreCase("COMPILATION_UNIT")){
			CompilationUnitComponent component = new CompilationUnitComponent(_symbol.getProperties().getPropertyListSerializable());
			component.getProperties().setProperties(_symbol.getProperties());
			return component;
		}	
		return null;
	}
	
	private CompilationUnitComponent filterReportApplicationXModules(Symbol_New _symbol){
//		String name = (String)_symbol.getProperty("NAME");
		String category = (String)_symbol.getProperty("CATEGORY");
		String categoryType = (String)_symbol.getProperty("CATEGORY_TYPE");
		
		if(_symbol.getProperty("CATEGORY") == null){
			try{
			throw new Exception();
			} catch (Exception e){
				ParserRuleContext context = (ParserRuleContext) _symbol.getProperty(CONTEXT);
				System.err.format("*** ERROR: SYMBOL '%s' WITHOUT CATEGORY in COMPILATION UNIT '%s' in line %d%n"
		                ,_symbol.getProperty(NAME), st.getCompilarionUnitSymbol(context).getName()
		                , context.start.getLine());
				return null;
			}
		}
		
		if(categoryType == null){
			try{
			throw new Exception();
			} catch (Exception e){
				ParserRuleContext context = (ParserRuleContext) _symbol.getProperty(CONTEXT);
				System.err.format("*** ERROR: SYMBOL '%s' WITHOUT CATEGORY_TYPE in COMPILATION UNIT '%s' in line %d%n"
		                ,_symbol.getProperty(NAME), st.getCompilarionUnitSymbol(context).getName()
		                , context.start.getLine());
				return null;
			}
		}		
		
		if(category.equalsIgnoreCase("PROCEDURE")){
			CompilationUnitComponent component = new CompilationUnitComponent(_symbol.getProperties().getPropertyListSerializable());
			component.getProperties().setProperties(_symbol.getProperties());
			return component;
		}
		else if(category.equalsIgnoreCase("COMPILATION_UNIT")){
			CompilationUnitComponent component = new CompilationUnitComponent(_symbol.getProperties().getPropertyListSerializable());
			component.getProperties().setProperties(_symbol.getProperties());
			return component;
		}	
		return null;
	}	
}
