package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_NAME;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;

import java.util.LinkedList;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.model.visualbasic.Project;
import br.com.bicam.parser.visualbasic6.VbpBaseListener;
import br.com.bicam.parser.visualbasic6.VbpParser;
import br.com.bicam.parser.visualbasic6.VbpParser.ProprietyContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.KeywordVB6;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class DefMetadataVb_New extends VbpBaseListener {

	// String absoluteFileName; // nome do diretório do projeto VB6
	Parser_New parser;

	SymbolTable_New st;
	IScope_New appScope;       // Project Scope
	IScope_New metadataComponentScope; // Component Scope
	SymbolFactory symbolFactory = new SymbolFactory();
	LinkedList<PropertyList> nestedProperties = new LinkedList<PropertyList>();

	Project project;

	KeywordLanguage keywordLanguage;

	final String[] sulfixTypeIndicator = new String[] { "&", "%", "#", "!", "@", "$" };
	public DefMetadataVb_New(Parser_New _parser, PropertyList _properties) {
		keywordLanguage = new KeywordVB6();
		st = (SymbolTable_New) _properties.getProperty("SYMBOLTABLE");
		this.parser = _parser;
	}

	public SymbolTable_New getSymbolTable() {
		return st;
	}

/*	@Override
	public void exitStartRule(@NotNull VbpParser.StartRuleContext ctx) {
		List<Symbol_New> symbols = st.getSymbolByProperty("CATEGORY", "APPLICATION");
		if (symbols.size() > 0) {
			try {
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < symbols.size(); i++) {
					sb.append(" " + symbols.get(i).getName()); 
				}
				throw new Exception("*** WARNING - Two applications symbol for" + sb.toString());
			} catch (Exception e) {	}
		}
		Symbol_New sym = symbols.get(0);
		project = new Project(sym.getName());

		for (String key : sym.getProperties().getProperties().keySet()) {
			if (sym.getProperties().getProperties().get(key) instanceof String)
				project.getProperties().addProperty(key, (String) sym.getProperties().getProperties().get(key));
		}

		symbols = st.getSymbolByProperty("CATEGORY", "APPLICATION_COMPONENT");
		for (Symbol_New s : symbols) {
			Module module;

			if (((String) s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("BAS")) {
				module = new Module(s.getName());
				for (String key : s.getProperties().getProperties().keySet()) {
					if (s.getProperties().getProperties().get(key) instanceof String)
						module.addProperty(key, (String) s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(module);
			}
			if (((String) s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("FRM")) {
				module = new Module(s.getName());
				for (String key : s.getProperties().getProperties().keySet()) {
					if (s.getProperties().getProperties().get(key) instanceof String)
						module.addProperty(key, (String) s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(module);
			}
			if (((String) s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("CLS")) {
				module = new Module(s.getName());
				for (String key : s.getProperties().getProperties().keySet()) {
					if (s.getProperties().getProperties().get(key) instanceof String)
						module.addProperty(key, (String) s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(module);
			} else {
				try {
					throw new Exception("*** ERROR - Reference type not found: " + s.getProperty("CATEGORY_TYPE"));
				} catch (Exception e) {
					// System.err.println("*** ERROR - Reference type not found: " +
					// s.getProperty("CATEGORY_TYPE"));
				}
			}
		}
		// =======================================================================================
		symbols = st.getSymbolByProperty("CATEGORY", "APPLICATION_REFERENCE");
		for (Symbol_New s : symbols) {
			Reference reference;
			ObjectReference object;

			if (((String) s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("REFERENCE")) {
				reference = new Reference(s.getName());
				for (String key : s.getProperties().getProperties().keySet()) {
					if (s.getProperties().getProperties().get(key) instanceof String)
						reference.addProperty(key, (String) s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(reference);
			}

			if (((String) s.getProperty("CATEGORY_TYPE")).equalsIgnoreCase("OBJECT")) {
				object = new ObjectReference(s.getName());
				for (String key : s.getProperties().getProperties().keySet()) {
					if (s.getProperties().getProperties().get(key) instanceof String)
						object.addProperty(key, (String) s.getProperties().getProperties().get(key));
				}
				project.getComponents().add(object);
			} else {
				try {
					throw new Exception("*** ERROR - Reference type not found: " + s.getProperty("CATEGORY_TYPE"));
				} catch (Exception e) {
					// System.err.println("*** ERROR - Reference type not found: " +
					// s.getProperty("CATEGORY_TYPE"));
				}
			}
		}
		// =======================================================================================
	}*/

	@Override
	public void enterModule(@NotNull VbpParser.ModuleContext _ctx) {
		/*
		 * if(absoluteFileName == null){ throw new
		 * IllegalArgumentException("Invalid file name: " + absoluteFileName); }
		 */
/*		try {*/
			String[] nameParts;

			nameParts = parser.getFileName().split("\\\\");
			// <PROJETO_NAME>.VBP
			String name = nameParts[nameParts.length - 1]; // <PROJETO_NAME>
			nameParts = name.split("\\.");
			name = nameParts[0];
			String fileName = parser.getFileName(); // <PROJETO_NAME>.VBP

			PropertyList properties = new PropertyList();
/*			properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());st.getGlobalScope()
			properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());*/
			properties.addProperty("ENCLOSING_SCOPE", st.getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", st.getGlobalScope().getName());
//			properties.addProperty("VISIBILITY", "GLOBAL");
			properties.addProperty("NAME", name);
			properties.addProperty("FILE_NAME", fileName);
			properties.addProperty("CATEGORY_TYPE", "PROJECT");
			properties.addProperty("CATEGORY", "APPLICATION");
			properties.addProperty("CONTEXT", _ctx);
			properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
			properties.addProperty("SYMBOL_TYPE", SymbolType_New.APPLICATION);
			properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

			Symbol_New appSym = symbolFactory.getSymbol(properties);

//			setCurrentScope((IScope_New) appSym);
			appScope = appSym;
			
// Public Scope
			PropertyList  propPublic = new PropertyList();
			propPublic.addProperty(ENCLOSING_SCOPE, appScope);
			propPublic.addProperty(ENCLOSING_SCOPE_NAME, appScope.getName());
			propPublic.addProperty("NAME", "PUBLIC");
			propPublic.addProperty("CATEGORY_TYPE", "VISIBILITY");
			propPublic.addProperty(KEYWORD_LANGUAGE, st.getKeywordsLanguage());

			propPublic.addProperty(CATEGORY, "SCOPE");
			propPublic.addProperty("SYMBOL_TYPE", SymbolType_New.VISIBILITY_SCOPE);
			symbolFactory.getSymbol(propPublic);			
			
/*
 * // MetadaComponent Define===============================================================
			PropertyList propMetadata = new PropertyList();
			propMetadata.addProperty("ENCLOSING_SCOPE", appSym);
			propMetadata.addProperty("ENCLOSING_SCOPE_NAME", appSym.getName());
			propMetadata.addProperty("NAME", "METADATA_COMPONENT");
			propMetadata.addProperty("CATEGORY_TYPE", "LOCAL");
			propMetadata.addProperty("CATEGORY", "SCOPE");
			propMetadata.addProperty("CONTEXT", _ctx);
			propMetadata.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
			propMetadata.addProperty("SYMBOL_TYPE", SymbolType_New.LOCAL_SCOPE);
			properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

			metadataComponentScope = symbolFactory.getSymbol(propMetadata);			

			Iterator<ProprietyContext> ctxProperties = _ctx.propriety().iterator();
			while (ctxProperties.hasNext()) {
				ProprietyContext propCtx = ctxProperties.next();
				createApplicationComponent(propCtx);
			}*/
	}

	public void exitModule(@NotNull VbpParser.ModuleContext _ctx) {
		// System.err.println(st.getGlobalScope().toString());
	}

	private void createApplicationComponent(ProprietyContext _property) {
		if (_property.start.getText().equalsIgnoreCase("form")) {
			addFormCompilationUnit(_property);
		} else if (_property.start.getText().equalsIgnoreCase("Designer")) {
			addDesignerCompilationUnit(_property);
		} else if (_property.start.getText().equalsIgnoreCase("module")) {
			addBasCompilationUnit(_property);
		} else if (_property.start.getText().equalsIgnoreCase("Class")) {
			addClsCompilationUnit(_property);
		} else if (_property.start.getText().equalsIgnoreCase("Object")) {
			addReferenceObject(_property);
		} else if (_property.start.getText().equalsIgnoreCase("Reference")) {
			addReferenceReference(_property);
		} else {
			addVbpProperty(_property);
		}
	}

	private void addFormCompilationUnit(ProprietyContext _propertyCtx) {
		// Form=R1FAB001.FRM ou FORM=..\Form1.frm
		String[] invalidChar = { "\r", "\n", "=" };
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for (String c : invalidChar) {
			name = name.replace(c, "");
		}
		
		String parts[] = name.split(";");
		String fileName = null;
		String compilationUnitName = null;
		
		if(parts.length > 1) {
			fileName = parts[1].trim();
//			compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];
		}
		else {
			fileName = parts[0].trim();
//			compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];			
		}

		compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];			

		name = compilationUnitName.split("\\.")[0]; // remove extenção
		
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
		properties.addProperty("FILE_NAME", fileName);		
		properties.addProperty("COMPILATION_UNIT_NAME", compilationUnitName.toUpperCase());		
		properties.addProperty("CATEGORY_TYPE", "FRM");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty("SYMBOL_TYPE", SymbolType_New.COMPILATION_UNIT);
		symbolFactory.getSymbol(properties);
	}

	private void addDesignerCompilationUnit(ProprietyContext _propertyCtx) {
		// Designer=rptMotivosPlanoContabil.Dsr
		String[] invalidChar = { "\r", "\n", "=" };
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for (String c : invalidChar) {
			name = name.replace(c, "");
		}

		String value[] = name.split("\\\\");
		int len = value.length;
		String FILE_NAME = value[len - 1];
		name = FILE_NAME.split("\\.")[0].trim(); // substitui o ponto "." da extension:
		name = FILE_NAME.split("\\.")[0].trim(); // substitui o ponto "." da extension:
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		/*		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());*/
		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
//		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("FILE_NAME", FILE_NAME);
		properties.addProperty("CATEGORY_TYPE", "DESIGNER");
		// properties.addProperty("CATEGORY", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		// properties.addProperty("SYMBOL_TYPE", SymbolType_New.REFERENCE);
		properties.addProperty("SYMBOL_TYPE", SymbolType_New.COMPILATION_UNIT);

		symbolFactory.getSymbol(properties);
	}

	private void addBasCompilationUnit(ProprietyContext _propertyCtx) {
		// Module=Sybase_Especifico;\\fswcorp\ceic\ITAUSEG\INFRA\producao\vb6\ROTINAS\GEMOSY01.BAS
		String[] invalidChar = { "\r", "\n", "=" };
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for (String c : invalidChar) {
			name = name.replace(c, "");
		}
		
		String parts[] = name.split(";");
		String fileName = null;
		String compilationUnitName = null;
		
		if(parts.length > 1) {
			fileName = parts[1].trim();
//			compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];
		}
		else {
			fileName = parts[0].trim();
//			compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];			
		}
		
		compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];
		
		name = compilationUnitName.split("\\.")[0]; // remove extenção
		
		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
		properties.addProperty("FILE_NAME", fileName);
		properties.addProperty("COMPILATION_UNIT_NAME", compilationUnitName.toUpperCase());		
		properties.addProperty("CATEGORY_TYPE", "BAS");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty("SYMBOL_TYPE", SymbolType_New.COMPILATION_UNIT);

		symbolFactory.getSymbol(properties);
	}

	private void addReferenceObject(ProprietyContext _propertyCtx) {
		// Object = "{0BA686C6-F7D3-101A-993E-0000C0EF6F5E}#1.0#0"; "THREED32.OCX"
		String[] invalidChar = { "\r", "\n", "=" };
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for (String c : invalidChar) {
			name = name.replace(c, "");
		}

		String valueParts[] = name.split(";");
		String hklm = valueParts[0];

		String referenceName = valueParts[valueParts.length - 1].trim();

		name = referenceName.split("\\.")[0].trim();

		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("REFERENCE_NAME", referenceName);
		properties.addProperty("HKLM", hklm);
		/*		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());*/
		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
//		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("CATEGORY_TYPE", "OBJECT");
		properties.addProperty("CATEGORY", "APPLICATION_REFERENCE");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty("SYMBOL_TYPE", SymbolType_New.REFERENCE);

		symbolFactory.getSymbol(properties);
	}

	private void addReferenceReference(ProprietyContext _propertyCtx) {
		// Reference=*\G{00020430-0000-0000-C000-000000000046}#2.0#0#..\..\..\WINDOWS\system32\stdole2.tlb#OLE
		// Automation
		// Reference=*\G{00020905-0000-0000-C000-000000000046}#8.0#409#C:\Arquivos de
		// programas\Microsoft Office\Office14\MSWORD.OLB#MICROSOFT WORD 8.0 OBJECT
		// LIBRARY
		String[] invalidChar = { "\r", "\n", "=" };
		String value = _propertyCtx.PROPERTY_VALUE().getText();
		for (String c : invalidChar) {
			value = value.replace(c, "");
		}

		String fileName = value.split("#")[3];
		String referenceName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
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

		/*		properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());*/
		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
//		properties.addProperty("VISIBILITY", "GLOBAL");
		properties.addProperty("FILE_NAME", fileName);
		properties.addProperty("CATEGORY_TYPE", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_REFERENCE");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty("SYMBOL_TYPE", SymbolType_New.REFERENCE);

		symbolFactory.getSymbol(properties);
	}

	private void addClsCompilationUnit(ProprietyContext _propertyCtx) {
		// Module=Sybase_Especifico;\\fswcorp\ceic\ITAUSEG\INFRA\producao\vb6\ROTINAS\GEMOSY01.BAS
		String[] invalidChar = { "\r", "\n", "=" };
		String name = _propertyCtx.PROPERTY_VALUE().getText().replace("=", "");
		for (String c : invalidChar) {
			name = name.replace(c, "");
		}
		
		String parts[] = name.split(";");
		String fileName = null;
		String compilationUnitName = null;
		
		if(parts.length > 1) {
			fileName = parts[1].trim();
//			compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];
		}
		else {
			fileName = parts[0].trim();
//			compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];			
		}
		
		compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];			
		
		name = compilationUnitName.split("\\.")[0]; // remove extenção

		PropertyList properties = new PropertyList();
		properties.addProperty("NAME", name);
		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
		properties.addProperty("FILE_NAME", fileName);
		properties.addProperty("COMPILATION_UNIT_NAME", compilationUnitName.toUpperCase());		
		
		properties.addProperty("CATEGORY_TYPE", "CLS");
		properties.addProperty("CATEGORY", "APPLICATION_COMPONENT");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty("SYMBOL_TYPE", SymbolType_New.COMPILATION_UNIT);

		symbolFactory.getSymbol(properties);
	}

	public Project getProject() {
		return project;
	}

	private void addVbpProperty(ProprietyContext _propertyCtx) {
		String[] invalidChar = { "\r", "\n", "=" };
		String value = _propertyCtx.PROPERTY_VALUE().getText().trim();
		String key = _propertyCtx.PROPERTY_KEY().getText().trim();

//		String key = _propertyCtx.PROPERTY_KEY(0).getText().trim();
		for (String c : invalidChar) {
			value = value.replace(c, "");
		}
		((Symbol_New)appScope).addProperty(key, value);
	}
}