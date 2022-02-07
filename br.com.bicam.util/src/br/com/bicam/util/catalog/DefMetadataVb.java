package br.com.bicam.util.catalog;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.model.visualbasic.Module;
import br.com.bicam.model.visualbasic.ObjectReference;
import br.com.bicam.model.visualbasic.Project;
import br.com.bicam.model.visualbasic.Reference;
import br.com.bicam.parser.visualbasic6.VbpBaseListener;
import br.com.bicam.parser.visualbasic6.VbpParser;
import br.com.bicam.parser.visualbasic6.VbpParser.ProprietyContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType;
import br.com.bicam.util.symboltable.IScope;
import br.com.bicam.util.symboltable.SymbolTableFactory;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;

public class DefMetadataVb extends VbpBaseListener{

//	String absoluteFileName; // nome do diretório do projeto VB6 
	Parser parser;

	SymbolTable_b 		st;
	Deque<IScope> 		scopes;
	SymbolTableFactory  symbolFactory = new SymbolTableFactory();
	LinkedList<PropertyList> nestedProperties = new LinkedList<PropertyList>();

	Project project;
	
	KeywordLanguage keywordLanguage;
	
	final String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$"};

//	public DefMetadataVb(String absoluteFileName, PropertyList _properties){
	public DefMetadataVb(Parser _parser, PropertyList _properties){
		scopes = new ArrayDeque<IScope>();
		st = (SymbolTable_b)_properties.getProperty("SYMBOLTABLE");
		setCurrentScope(st.getGlobalScope());
		this.parser = _parser;
	}
	
	public SymbolTable_b getSymbolTable(){
		return st;
	}
	
	private IScope getCurrentScope(){
		return scopes.peek();
	}
	
	private void setCurrentScope(IScope s){
		scopes.push(s);
	}
	
	@Override 
	public void exitStartRule(@NotNull VbpParser.StartRuleContext ctx) {
		List<Symbol_b> symbols = st.getSymbolByProperty("CATEGORY","APPLICATION");
		Symbol_b sym = symbols.get(0);
		project =  new Project(sym.getName());
		
		for(String key : sym.getProperties().getProperties().keySet()){
			if(sym.getProperties().getProperties().get(key) instanceof String)
				project.getProperties().addProperty(key, (String)sym.getProperties().getProperties().get(key));
		}
		
		symbols = st.getSymbolByProperty("CATEGORY","APPLICATION_COMPONENT");
		for(Symbol_b s : symbols){
			Module module;

			if(((String)s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("BAS")){
				module = new Module(s.getName());
				for(String key : s.getProperties().getProperties().keySet()){
					if(s.getProperties().getProperties().get(key) instanceof String)
						module.addProperty(key, (String)s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(module);
			}
			if(((String)s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("FRM")){
				module = new Module(s.getName());
				for(String key : s.getProperties().getProperties().keySet()){
					if(s.getProperties().getProperties().get(key) instanceof String)
						module.addProperty(key, (String)s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(module);
			}	
			if(((String)s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("CLS")){
				module = new Module(s.getName());
				for(String key : s.getProperties().getProperties().keySet()){
					if(s.getProperties().getProperties().get(key) instanceof String)
						module.addProperty(key, (String)s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(module);
			}				
			else {
				try {
					throw new Exception("*** ERROR - Reference type not found: " + s.getProperty("CATEGORY_TYPE"));
				} catch (Exception e){
//					System.err.println("*** ERROR - Reference type not found: " + s.getProperty("CATEGORY_TYPE"));
				}
			}
		}
//=======================================================================================
		symbols = st.getSymbolByProperty("CATEGORY","APPLICATION_REFERENCE");
		for(Symbol_b s : symbols){
			Reference reference;
			ObjectReference object;
			
			if(((String)s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("REFERENCE")){
				reference = new Reference(s.getName());
				for(String key : s.getProperties().getProperties().keySet()){
					if(s.getProperties().getProperties().get(key) instanceof String)
						reference.addProperty(key, (String)s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(reference);
			}
			
			if(((String)s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("OBJECT")){
				object = new ObjectReference(s.getName());
				for(String key : s.getProperties().getProperties().keySet()){
					if(s.getProperties().getProperties().get(key) instanceof String)
						object.addProperty(key, (String)s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(object);
			}
			else {
				try {
					throw new Exception("*** ERROR - Reference type not found: " + s.getProperty("CATEGORY_TYPE"));
				} catch (Exception e){
//					System.err.println("*** ERROR - Reference type not found: " + s.getProperty("CATEGORY_TYPE"));
				}
			}
		}		
//=======================================================================================
	}

	@Override
	public void enterModule(@NotNull VbpParser.ModuleContext _ctx) {
/*		if(absoluteFileName == null){
			throw new IllegalArgumentException("Invalid file name: " + absoluteFileName);
		}*/
/*		try {*/		
		String[] nameParts;

			nameParts = parser.getFileName().split("\\\\");
  // <PROJETO_NAME>.VBP
		String name = nameParts[nameParts.length-1]; 					// <PROJETO_NAME>
		nameParts  = name.split("\\."); 
		name = nameParts[0];
		String fileName = parser.getFileName();					// <PROJETO_NAME>.VBP
		
		PropertyList properties = new PropertyList();
		properties.addProperty("ENCLOSING_SCOPE",getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("NAME",name);
		properties.addProperty("FILE_NAME", fileName);
		properties.addProperty("CATEGORY_TYPE", "PROJECT");
		properties.addProperty("CATEGORY", "APPLICATION");
		properties.addProperty("CONTEXT", _ctx);
		properties.addProperty("SYMBOL_TYPE", SymbolType.APPLICATION);	
		
		
		Symbol_b appSym = symbolFactory.getSymbol(properties);
		
		setCurrentScope((IScope)appSym);
		
		Iterator<ProprietyContext> ctxProperties = _ctx.propriety().iterator();
		while(ctxProperties.hasNext()){
			ProprietyContext propCtx = ctxProperties.next();
			createApplicationComponent(propCtx);
		}
/*		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public void exitModule(@NotNull VbpParser.ModuleContext _ctx) {
//		System.err.println(st.getGlobalScope().toString());
	}
	
	private void createApplicationComponent(ProprietyContext _property){
		if(_property.start.getText().equalsIgnoreCase("form")){
			addFormCompilationUnit(_property);
		}
		else if(_property.start.getText().equalsIgnoreCase("Designer")){
			addDesignerCompilationUnit(_property);
		}		
		else if(_property.start.getText().equalsIgnoreCase("module")){
			addBasCompilationUnit(_property);
		}
		else if(_property.start.getText().equalsIgnoreCase("Class")){
			addClsCompilationUnit(_property);
		}
		else if(_property.start.getText().equalsIgnoreCase("Object")){
			addReferenceObject(_property);
		}
		else if(_property.start.getText().equalsIgnoreCase("Reference")){
			addReferenceReference(_property);
		}		
		else {
			addVbpProperty(_property);
		}
	}
	
	private void addFormCompilationUnit(ProprietyContext _propertyCtx) {
		// Form=R1FAB001.FRM ou FORM=..\Form1.frm
		String[] invalidChar = {"\r","\n","="};
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for(String c : invalidChar){
			name = name.replace(c, "");
		}
		
		String value[] = name.split("\\\\");
		int len = value.length;
		String FILE_NAME = value[len-1];
//		name = FILE_NAME.replace(".", "_").trim();  // substitui o ponto "." da extension:
//													// abcform.FRM -> abcform_FRM
		name = FILE_NAME.split("\\.")[0].trim();  // substitui o ponto "." da extension:
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("FILE_NAME", FILE_NAME);
		properties.addProperty("CATEGORY_TYPE", "FRM");
//		properties.addProperty("CATEGORY", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
//		properties.addProperty("SYMBOL_TYPE", SymbolType.REFERENCE);
		properties.addProperty("SYMBOL_TYPE", SymbolType.COMPONENT);
		
		
		symbolFactory.getSymbol(properties);
	}
	
	private void addDesignerCompilationUnit(ProprietyContext _propertyCtx) {
		// Designer=rptMotivosPlanoContabil.Dsr 
		String[] invalidChar = {"\r","\n","="};
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for(String c : invalidChar){
			name = name.replace(c, "");
		}
		
		String value[] = name.split("\\\\");
		int len = value.length;
		String FILE_NAME = value[len-1];
		name = FILE_NAME.split("\\.")[0].trim();  // substitui o ponto "." da extension:
		name = FILE_NAME.split("\\.")[0].trim();  // substitui o ponto "." da extension:
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("FILE_NAME", FILE_NAME);
		properties.addProperty("CATEGORY_TYPE", "DESIGNER");
//		properties.addProperty("CATEGORY", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
//		properties.addProperty("SYMBOL_TYPE", SymbolType.REFERENCE);	
		properties.addProperty("SYMBOL_TYPE", SymbolType.COMPONENT);	
		
		
		symbolFactory.getSymbol(properties);
	}	
	
	private void addBasCompilationUnit(ProprietyContext _propertyCtx) {
		//Module=Sybase_Especifico;\\fswcorp\ceic\ITAUSEG\INFRA\producao\vb6\ROTINAS\GEMOSY01.BAS
		String[] invalidChar = {"\r","\n","="};
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for(String c : invalidChar){
			name = name.replace(c, "");
		}
		
		String valueParts[] = name.split(";");
		
		String moduleName = valueParts[0].split("\\.")[0];
		
		String fileName = valueParts[0];
		
		if(valueParts.length > 1){
			fileName = valueParts[1];
			valueParts = fileName.split("\\\\");
			name = valueParts[valueParts.length - 1].split("\\.")[0];
		}
		PropertyList properties = new PropertyList();
//		properties.addProperty("NAME", name);
		properties.addProperty("NAME", moduleName);
		properties.addProperty("MODULE_NAME", moduleName);
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("FILE_NAME", fileName);			
		properties.addProperty("CATEGORY_TYPE", "BAS");
//		properties.addProperty("CATEGORY", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
//		properties.addProperty("SYMBOL_TYPE", SymbolType.REFERENCE);	
		properties.addProperty("SYMBOL_TYPE", SymbolType.COMPONENT);	
		
		symbolFactory.getSymbol(properties);
	}		
	
	
	private void addReferenceObject(ProprietyContext _propertyCtx) {
//		Object = "{0BA686C6-F7D3-101A-993E-0000C0EF6F5E}#1.0#0"; "THREED32.OCX"
		String[] invalidChar = {"\r","\n","="};
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for(String c : invalidChar){
			name = name.replace(c, "");
		}
		
		String valueParts[] = name.split(";");
		String hklm = valueParts[0];
		
		String referenceName = valueParts[valueParts.length-1].trim();
		
		name = referenceName.split("\\.")[0].trim();
		
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("REFERENCE_NAME", referenceName);
		properties.addProperty("HKLM", hklm);
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("CATEGORY_TYPE", "OBJECT");
		properties.addProperty("CATEGORY", "APPLICATION_REFERENCE");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty("SYMBOL_TYPE", SymbolType.REFERENCE);	
		
		symbolFactory.getSymbol(properties);
	}
	
	private void addReferenceReference(ProprietyContext _propertyCtx){
//Reference=*\G{00020430-0000-0000-C000-000000000046}#2.0#0#..\..\..\WINDOWS\system32\stdole2.tlb#OLE Automation
//Reference=*\G{00020905-0000-0000-C000-000000000046}#8.0#409#C:\Arquivos de programas\Microsoft Office\Office14\MSWORD.OLB#MICROSOFT WORD 8.0 OBJECT LIBRARY
		String[] invalidChar = {"\r","\n","="};
		String value = _propertyCtx.PROPERTY_VALUE().getText();
		for(String c : invalidChar){
			value = value.replace(c, "");
		}	

		 String fileName = value.split("#")[3];
		 String referenceName = fileName.split("\\\\")[fileName.split("\\\\").length-1];
		 String name = referenceName.split("\\.")[0];
		 String hklm = value.split("#")[0];
		 String version = value.split("#")[1];
		 String release = value.split("#")[2];
		 String description = value.split("#")[4];
		
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("VERSION", version);
		properties.addProperty("RELEASE", release);
		properties.addProperty("HKLM", hklm);
		properties.addProperty("REFERENCE_NAME", referenceName);
		properties.addProperty("DESCRIPTION", description);
		
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("FILE_NAME", fileName);			
		properties.addProperty("CATEGORY_TYPE", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_REFERENCE");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty("SYMBOL_TYPE", SymbolType.REFERENCE);	
		
		symbolFactory.getSymbol(properties);		
	}	
	private void addClsCompilationUnit(ProprietyContext _propertyCtx) {
		//Module=Sybase_Especifico;\\fswcorp\ceic\ITAUSEG\INFRA\producao\vb6\ROTINAS\GEMOSY01.BAS
		String[] invalidChar = {"\r","\n","="};
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for(String c : invalidChar){
			name = name.replace(c, "");
		}
		
		String valueParts[] = name.split(";");
		
		String moduleName = valueParts[0].split("\\.")[0];
		
		String fileName = valueParts[0];
		
		if(valueParts.length > 1){
			fileName = valueParts[1];
			valueParts = fileName.split("\\\\");
			name = valueParts[valueParts.length - 1].split("\\.")[0];
		}
		
		PropertyList properties = new PropertyList();
//		properties.addProperty("NAME", name);
		properties.addProperty("NAME", moduleName);
		properties.addProperty("MODULE_NAME", moduleName);
		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("FILE_NAME", fileName);			
		properties.addProperty("CATEGORY_TYPE", "CLS");
//		properties.addProperty("CATEGORY", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
//		properties.addProperty("SYMBOL_TYPE", SymbolType.REFERENCE);
		properties.addProperty("SYMBOL_TYPE", SymbolType.COMPONENT);	
		
		
		symbolFactory.getSymbol(properties);
	}
	
	public Project getProject(){
		return project;
	}
	
	private void addVbpProperty(ProprietyContext _propertyCtx){
		String[] invalidChar = {"\r","\n","="};
		String value = _propertyCtx.PROPERTY_VALUE().getText().trim();
		String key = _propertyCtx.PROPERTY_KEY(0).getText().trim();		
		for(String c : invalidChar){
			value = value.replace(c, "");
		}		
		Symbol_b sym = (Symbol_b) getCurrentScope();
		sym.addProperty(key, value);
	}
}