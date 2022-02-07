package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.CATEGORY;
import static br.com.bicam.util.constant.PropertyName.DATA_TYPE_NAME;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE;
import static br.com.bicam.util.constant.PropertyName.ENCLOSING_SCOPE_NAME;
import static br.com.bicam.util.constant.PropertyName.KEYWORD_LANGUAGE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.model.visualbasic.Project;
import br.com.bicam.parser.visualbasic6.VbpBaseListener;
import br.com.bicam.parser.visualbasic6.VbpParser;
import br.com.bicam.parser.visualbasic6.VbpParser.PropertySectionContext;
import br.com.bicam.parser.visualbasic6.VbpParser.ProprietyContext;
import br.com.bicam.util.KeywordLanguage;
import br.com.bicam.util.KeywordVB6;
import br.com.bicam.util.NodeExplorer_New;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType_New;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.SymbolFactory;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class MetadataVb6 extends VbpBaseListener {

	// String absoluteFileName; // nome do diretório do projeto VB6
	File file;

	SymbolTable_New st;
	IScope_New appScope;                    // Project Scope
	IScope_New metadataComponentScope;      // Component Scope
	SymbolFactory symbolFactory = new SymbolFactory();
	LinkedList<PropertyList> nestedProperties = new LinkedList<PropertyList>();
	
	String appName;

	Project project;
	
	Deque<BicamNode> nodeScope;
	boolean addAtributeNode = false;
	
///	NodeList nodeList;
	BicamNode currentNode;

	KeywordLanguage keywordLanguage;

	final String[] sulfixTypeIndicator = new String[] { "&", "%", "#", "!", "@", "$" };
	
	public MetadataVb6(PropertyList _properties) {
		keywordLanguage = new KeywordVB6();
		nodeScope = new ArrayDeque<BicamNode>();
		st = (SymbolTable_New) _properties.getProperty("SYMBOLTABLE");
		this.file = (File) _properties.getProperty("FILE");
		this.appName = appName();
	}

	public SymbolTable_New getSymbolTable() {
		return st;
	}

	@Override
	public void enterModule(@NotNull VbpParser.ModuleContext _ctx) {

			String[] nameParts;

			nameParts = file.getName().split("\\\\");
			// <PROJETO_NAME>.VBP
			String name = nameParts[nameParts.length - 1]; // <PROJETO_NAME>
			nameParts = name.split("\\.");
			name = nameParts[0];
			String fileName = file.getName(); // <PROJETO_NAME>.VBP

			PropertyList properties = new PropertyList();
			properties.addProperty("ENCLOSING_SCOPE", st.getGlobalScope());
			properties.addProperty("ENCLOSING_SCOPE_NAME", st.getGlobalScope().getName());
			properties.addProperty("NAME", appName);
			properties.addProperty("FILE_NAME", fileName);
			properties.addProperty("CATEGORY_TYPE", "PROJECT");
			properties.addProperty("CATEGORY", "APPLICATION");
			properties.addProperty("CONTEXT", _ctx);
			properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
			properties.addProperty("SYMBOL_TYPE", SymbolType_New.APPLICATION);
			properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

			Symbol_New appSym = symbolFactory.getSymbol(properties);
			
/*
 * //=====================================================			
			BicamNode appParentNode = nodeList.get("PARENT_NODE");
			if(appParentNode == null) {
				appParentNode = new BicamNode("PARENT_NODE",null);
			}
			
			BicamNode appNode = new BicamNode(((Symbol_New)appSym).getId(),_ctx.start.getStartIndex());
			nodeList.add(appNode);
			BicamAdjacency adj = new BicamAdjacency(appNode);
			appParentNode.addAdjacency(adj);
			setCurrentNode(appNode);
//======================================================
*/			appScope = appSym;
			
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
			
 // MetadaComponent Define===============================================================
			PropertyList propMetadata = new PropertyList();
			propMetadata.addProperty("ENCLOSING_SCOPE", appScope);
			propMetadata.addProperty("ENCLOSING_SCOPE_NAME", appScope.getName());
			propMetadata.addProperty("NAME", "METADATA_COMPONENT");
			propMetadata.addProperty("CATEGORY_TYPE", "LOCAL");
			propMetadata.addProperty("CATEGORY", "SCOPE");
			propMetadata.addProperty("CONTEXT", _ctx);
			propMetadata.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
			propMetadata.addProperty("SYMBOL_TYPE", SymbolType_New.LOCAL_SCOPE);
///			propMetadata.addProperty("NODES", nodeList);
			properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

			metadataComponentScope = symbolFactory.getSymbol(propMetadata);	

			Iterator<ProprietyContext> ctxProperties = _ctx.propriety().iterator();

			while (ctxProperties.hasNext()) {
				ProprietyContext propCtx = ctxProperties.next();
				if(NodeExplorer_New.getFirstSibling(propCtx) instanceof PropertySectionContext) continue;
				createApplicationComponent(propCtx);
			}
	}

	public void exitModule(@NotNull VbpParser.ModuleContext _ctx) {
		// System.err.println(st.getGlobalScope().toString());
	}
	
	@Override 
	public void enterPropertySection(@NotNull VbpParser.PropertySectionContext ctx) {
		Iterator<ProprietyContext> ctxProperties = ctx.propriety().iterator();
        
		PropertyList ppMetadata = metadataComponentScope.getProperties().addNestedProperty(ctx.Name.getText());
		PropertyList ppApplication = appScope.getProperties().addNestedProperty(ctx.Name.getText());

		while (ctxProperties.hasNext()) {
			ProprietyContext ppCtx = ctxProperties.next();
			String[] invalidChar = { "\r", "\n", "=" };
			String value = ppCtx.PROPERTY_VALUE().getText();
			for (String c : invalidChar) {
				value = value.replace(c, "");
			}
			ppMetadata.addProperty(ppCtx.propertyKey.getText(), value);
			ppApplication.addProperty(ppCtx.propertyKey.getText(), value);
		}		
	}

	private void createApplicationComponent(ProprietyContext _property) {
		String componentType = _property.start.getText().toUpperCase();
		addAtributeNode = false;
		switch(componentType) {
		case "FORM" :
			addFormCompilationUnit(_property);
			addVbpProperty(_property);
			break;
		case "MODULE" :
			addBasCompilationUnit(_property);
			addVbpProperty(_property);	
			break;
		case "CLASS" :
			addClsCompilationUnit(_property);
			addVbpProperty(_property);
			break;
		case "OBJECT" :
			addReferenceObject(_property);
			addVbpProperty(_property);
			break;
		case "REFERENCE" :
			addReferenceReference(_property);
			addVbpProperty(_property);
			break;
		default:
			addAtributeNode = true;
			addVbpProperty(_property);
		}
		
/*		if (_property.start.getText().equalsIgnoreCase("form")) {
			addFormCompilationUnit(_property);
			addVbpProperty(_property);

		} else if (_property.start.getText().equalsIgnoreCase("Designer")) {
			addDesignerCompilationUnit(_property);
			addVbpProperty(_property);

		} else if (_property.start.getText().equalsIgnoreCase("module")) {
			addBasCompilationUnit(_property);
			addVbpProperty(_property);

		} else if (_property.start.getText().equalsIgnoreCase("Class")) {
			addClsCompilationUnit(_property);
			addVbpProperty(_property);

		} else if (_property.start.getText().equalsIgnoreCase("Object")) {
			addReferenceObject(_property);
			addVbpProperty(_property);

		} else if (_property.start.getText().equalsIgnoreCase("Reference")) {
			addReferenceReference(_property);
			addVbpProperty(_property);

		} else {
			addVbpProperty(_property);
		}*/
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
		}
		else {
			fileName = parts[0].trim();
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
		Symbol_New sym = symbolFactory.getSymbol(properties);
/*		
//============================================================================	
		BicamNode parentNode = nodeList.get("FORM_NODE");
		if(parentNode == null) {
			parentNode = new BicamNode("FORM_NODE",null);
			nodeList.add(parentNode);
			BicamAdjacency adj = new BicamAdjacency(parentNode);
			getCurrentNode().addAdjacency(adj);
		}
		BicamNode formNode = new BicamNode(sym.getId(),_propertyCtx.start.getStartIndex());
		nodeList.add(formNode);
		BicamAdjacency adj = new BicamAdjacency(formNode);
		parentNode.addAdjacency(adj);
//=============================================================================	
*/		
		PropertyList appProp = appScope.getProperties();
		List<PropertyList> formList = (List<PropertyList>) appProp.getProperty("Form");
		if(formList == null) {
			formList = new ArrayList<PropertyList>();
			appProp.addProperty("Form", formList);
		}
		PropertyList propForm = new PropertyList();
		formList.add(propForm);
		propForm.addProperty("NAME", name);
		propForm.addProperty("FILE_NAME", fileName);
		
//=============================================================================		
		
		PropertyList metaProp = metadataComponentScope.getProperties();
		List<PropertyList> l2 = (List<PropertyList>) metaProp.getProperty("Form");
		if(l2 == null) {
			l2 = new ArrayList<PropertyList>();
			metaProp.addProperty("Form", l2);
		}
		PropertyList p2 = new PropertyList();
		l2.add(p2);
		p2.addProperty("NAME", name);
		p2.addProperty("FILE_NAME", fileName);
		
	}

/*	private void addDesignerCompilationUnit(ProprietyContext _propertyCtx) {
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
				properties.addProperty("ENCLOSING_SCOPE", getCurrentScope());
		properties.addProperty("ENCLOSING_SCOPE_NAME", getCurrentScope().getName());
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
	}*/

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
		}
		else {
			fileName = parts[0].trim();
		}
		
		compilationUnitName = fileName.split("\\\\")[fileName.split("\\\\").length-1];
		
		name = parts[0].trim();
		
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

		Symbol_New sym = symbolFactory.getSymbol(properties);
/*		//============================================================================	
				BicamNode parentNode = nodeList.get("BAS_NODE");
				if(parentNode == null) {
					parentNode = new BicamNode("BAS_NODE",null);
					nodeList.add(parentNode);
					BicamAdjacency adj = new BicamAdjacency(parentNode);
					getCurrentNode().addAdjacency(adj);
				}
				BicamNode formNode = new BicamNode(sym.getId(),_propertyCtx.start.getStartIndex());
				nodeList.add(formNode);
				BicamAdjacency adj = new BicamAdjacency(formNode);
				parentNode.addAdjacency(adj);
		//=============================================================================			
*/
		PropertyList appProp = appScope.getProperties();
		List<PropertyList> formList = (List<PropertyList>) appProp.getProperty("Module");
		if(formList == null) {
			formList = new ArrayList<PropertyList>();
			appProp.addProperty("Module", formList);
		}
		PropertyList propForm = new PropertyList();
		formList.add(propForm);
		propForm.addProperty("NAME", name);
		propForm.addProperty("FILE_NAME", fileName);				
		
		PropertyList metaProp = metadataComponentScope.getProperties();
		List<PropertyList> l2 = (List<PropertyList>) metaProp.getProperty("Module");
		if(l2 == null) {
			l2 = new ArrayList<PropertyList>();
			metaProp.addProperty("Module", l2);
		}
		PropertyList p2 = new PropertyList();
		l2.add(p2);
		p2.addProperty("NAME", name);
		p2.addProperty("FILE_NAME", fileName);		
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
		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
		properties.addProperty("CATEGORY_TYPE", "OBJECT");
		properties.addProperty("CATEGORY", "APPLICATION_REFERENCE");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty("SYMBOL_TYPE", SymbolType_New.REFERENCE);

		Symbol_New sym = symbolFactory.getSymbol(properties);
/*//============================================================================	
		BicamNode parentNode = nodeList.get("OBJ_NODE");
		if(parentNode == null) {
			parentNode = new BicamNode("OBJ_NODE",null);
			nodeList.add(parentNode);
			BicamAdjacency adj = new BicamAdjacency(parentNode);
			getCurrentNode().addAdjacency(adj);
		}
		BicamNode formNode = new BicamNode(sym.getId(),_propertyCtx.start.getStartIndex());
		nodeList.add(formNode);
		BicamAdjacency adj = new BicamAdjacency(formNode);
		parentNode.addAdjacency(adj);
//=============================================================================			
*/
		PropertyList appProp = appScope.getProperties();
		List<PropertyList> objectList = (List<PropertyList>) appProp.getProperty("Object");
		if(objectList == null) {
			objectList = new ArrayList<PropertyList>();
			appProp.addProperty("Object", objectList);
		}
		PropertyList objReference = new PropertyList();
		objectList.add(objReference);
		objReference.addProperty("NAME", name);
		objReference.addProperty("REFERENCE_NAME", referenceName);
		objReference.addProperty("HKLM", hklm);		
		
		PropertyList metaProp = metadataComponentScope.getProperties();
		List<PropertyList> l2 = (List<PropertyList>) metaProp.getProperty("Object");
		if(l2 == null) {
			l2 = new ArrayList<PropertyList>();
			metaProp.addProperty("Object", l2);
		}
		PropertyList p2 = new PropertyList();
		l2.add(p2);
		p2.addProperty("NAME", name);
		p2.addProperty("REFERENCE_NAME", referenceName);
		p2.addProperty("HKLM", hklm);
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

		properties.addProperty("ENCLOSING_SCOPE", metadataComponentScope);
		properties.addProperty("ENCLOSING_SCOPE_NAME", metadataComponentScope.getName());
		properties.addProperty("FILE_NAME", fileName);
		properties.addProperty("CATEGORY_TYPE", "REFERENCE");
		properties.addProperty("CATEGORY", "APPLICATION_REFERENCE");
		properties.addProperty("CONTEXT", _propertyCtx);
		properties.addProperty(KEYWORD_LANGUAGE, keywordLanguage);
		properties.addProperty(DATA_TYPE_NAME, "UNDEFINED");

		properties.addProperty("SYMBOL_TYPE", SymbolType_New.REFERENCE);

		Symbol_New sym = symbolFactory.getSymbol(properties);
/*//============================================================================	
		BicamNode parentNode = nodeList.get("REF_NODE");
		if(parentNode == null) {
			parentNode = new BicamNode("REF_NODE",null);
			nodeList.add(parentNode);
			BicamAdjacency adj = new BicamAdjacency(parentNode);
			getCurrentNode().addAdjacency(adj);
		}
		BicamNode formNode = new BicamNode(sym.getId(),_propertyCtx.start.getStartIndex());
		nodeList.add(formNode);
		BicamAdjacency adj = new BicamAdjacency(formNode);
		parentNode.addAdjacency(adj);
//=============================================================================	
*/		
		PropertyList appProp = appScope.getProperties();
		List<PropertyList> referenceList = (List<PropertyList>) appProp.getProperty("Reference");
		if(referenceList == null) {
			referenceList = new ArrayList<PropertyList>();
			appProp.addProperty("Reference", referenceList);
		}
		PropertyList propReference = new PropertyList();
		referenceList.add(propReference);
		propReference.addProperty("NAME", name);
		propReference.addProperty("VERSION", version);
		propReference.addProperty("RELEASE", release);
		propReference.addProperty("HKLM", hklm);
		propReference.addProperty("REFERENCE_NAME", referenceName);
		propReference.addProperty("DESCRIPTION", description);		
		
		PropertyList metaProp = metadataComponentScope.getProperties();
		List<PropertyList> l2 = (List<PropertyList>) metaProp.getProperty("Reference");
		if(l2 == null) {
			l2 = new ArrayList<PropertyList>();
			metaProp.addProperty("Reference", l2);
		}
		PropertyList p2 = new PropertyList();
		l2.add(p2);
		p2.addProperty("NAME", name);
		p2.addProperty("VERSION", version);
		p2.addProperty("RELEASE", release);
		p2.addProperty("HKLM", hklm);
		p2.addProperty("REFERENCE_NAME", referenceName);
		p2.addProperty("DESCRIPTION", description);	
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
		}
		else {
			fileName = parts[0].trim();
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

		Symbol_New sym = symbolFactory.getSymbol(properties);
/*//============================================================================	
		BicamNode parentNode = nodeList.get("CLS_NODE");
		if(parentNode == null) {
			parentNode = new BicamNode("CLS_NODE",null);
			nodeList.add(parentNode);
			BicamAdjacency adj = new BicamAdjacency(parentNode);
			getCurrentNode().addAdjacency(adj);
		}
		BicamNode formNode = new BicamNode(sym.getId(),_propertyCtx.start.getStartIndex());
		nodeList.add(formNode);
		BicamAdjacency adj = new BicamAdjacency(formNode);
		parentNode.addAdjacency(adj);
//=============================================================================	
*/		
		PropertyList appProp = appScope.getProperties();
		List<PropertyList> formList = (List<PropertyList>) appProp.getProperty("Class");
		if(formList == null) {
			formList = new ArrayList<PropertyList>();
			appProp.addProperty("Class", formList);
		}
		PropertyList propForm = new PropertyList();
		formList.add(propForm);
		propForm.addProperty("NAME", name);
		propForm.addProperty("FILE_NAME", fileName);

		PropertyList metaProp = metadataComponentScope.getProperties();
		List<PropertyList> l2 = (List<PropertyList>) metaProp.getProperty("Class");
		if(l2 == null) {
			l2 = new ArrayList<PropertyList>();
			metaProp.addProperty("Class", l2);
		}
		PropertyList p2 = new PropertyList();
		l2.add(p2);
		p2.addProperty("NAME", name);
		p2.addProperty("FILE_NAME", fileName);		
	}

	public Project getProject() {
		return project;
	}

	private void addVbpProperty(ProprietyContext _propertyCtx) {
		if(!addAtributeNode) return;
		String[] invalidChar = { "\r", "\n", "=" };
		String value = 	_propertyCtx.propertyValue.getText().trim();
		String key = _propertyCtx.propertyKey.getText().trim().toUpperCase();
		for (String c : invalidChar) {
			value = value.replace(c, "");
		}
		
		if(!key.equalsIgnoreCase("NAME")) {
/*			((Symbol_New)appScope).addProperty(key, value);
			((Symbol_New)metadataComponentScope).addProperty(key, value);			
		}
		else {
*/			((Symbol_New)appScope).addProperty(key, value, true);
			((Symbol_New)metadataComponentScope).addProperty(key, value, true);
		}
		
/*		//============================================================================	
			BicamNode parentNode = nodeList.get("ATTRIBUTE_NODE");
			if(parentNode == null) {
				parentNode = new BicamNode("ATTRIBUTE_NODE",null);
				nodeList.add(parentNode);
				BicamAdjacency adj = new BicamAdjacency(parentNode);
				getCurrentNode().addAdjacency(adj);
			}
			BicamNode attrNode = new BicamNode(key,_propertyCtx.start.getStartIndex());
			nodeList.add(attrNode);
			BicamAdjacency adj = new BicamAdjacency(attrNode);
			parentNode.addAdjacency(adj);
		//=============================================================================			
*/	}
	
	private String appName() {
		try {
//			File file = (File) properties.getProperty("FILE");
			String fileName = file.getCanonicalPath();
			String REGEX = "^Name=\"([\\d\\w]+)\"$";
			// _file =>
			// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
			if (fileName == null)
				return null;
			appName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
			appName = appName.split("\\.")[0];
			Pattern p = Pattern.compile(REGEX);
			BufferedReader in = new BufferedReader(new FileReader(file));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				Matcher m = p.matcher(line);
				if ((m.matches())) {
					return appName = m.group(1);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return appName;
	}	
	
	
/*	public NodeList getNodeList() {
		return nodeList;
	}
	
	private void setCurrentNode(BicamNode _nodeScope) {
		nodeScope.push(_nodeScope);
	}

	private BicamNode getCurrentNode() {
		return nodeScope.peek();
	}

	private void removeCurrentNode() {
		nodeScope.pop();
	}*/	
}