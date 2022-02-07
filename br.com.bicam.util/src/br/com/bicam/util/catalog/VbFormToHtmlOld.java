package br.com.bicam.util.catalog;

import static br.com.bicam.util.constant.PropertyName.THIS;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.stringtemplate.v4.ST;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.StringOptionalCase;
import br.com.bicam.util.symboltable.IScope_New;
import br.com.bicam.util.symboltable.Member;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;


public class VbFormToHtmlOld extends UIToHtml{
	
	//  void elements
	//area, base, br, col, command, embed, hr, img, input, keygen, link, meta, param, source, track, wbr
	
	//======================MAP CONTROL VB6 to .Net =============================
	// MAP Control            	To .Net
	// Threed.SSCommand							System.Windows.Forms.Button
	// Threed.Constants_ButtonStyle				System.Windows.Forms.FlatStyle
	// Threed.SSFrame							System.Windows.Forms.GroupBox
	// Threed.SSPanel							System.Windows.Forms.Panel
	// Threed.SSCheck							System.Windows.Forms.CheckBox
	// Threed.SSOption							System.Windows.Forms.RadioButton
	// Threed.Constants_Alignment				System.Drawing.ContentAlignment
	// Threed.Constants_PictureBackgroundStyle	System.Windows.Forms.ImageLayout
	// Threed.Constants_Bevel					System.Windows.Forms.BorderStyle
	// Threed.Constants_MousePointer			System.Windows.Forms.Cursor
	// Threed.Constants_CheckBoxValue			System.Windows.Forms.CheckState
	//=============================================================================

	/*
	============================== VB5 Activex Controls =========================
	AniBtn32.ocx
	Gauge32.ocx
	Graph32.ocx
	Gsw32.EXE
	Gswdll32.DLL
	Grid32.ocx
	KeySta32.ocx
	MSOutl32.ocx
	Spin32.ocx
	Threed32.ocx
	MSChart.ocx
	==============================================================================
	*/
	/*
	===== Herança da linguagem BASIC - Variávies terminadas com os seguintes caracteres
	===== Não tem qualquer significado em VB ou VB.Net
	      %                 Integer
	      &                 Long
	      !                 Single
	      #                 Double
	      $                 String
	      @                 Currency
	=============================================================================
	*/     

	/* ============= VBP File - References Vs Objects
	Objects are for ActiveX controls which are usually compiled to .ocx files.
	References are for type libraries usually compiled to .dll files or .tlb files.
	Notice that .ocx files contain typelib too so this is very inconsistent and pretty much a legacy division.

	Paths and filenames are optional, typelib IDs are canonical way to resolve dependency.
	Only if these are not found in registry there is a auto-resolve strategy searching
	for files in current folder for .ocxes only. 
	This most annoying behavior happens at run-time too when the applications starts 
	to auto-registering .ocxes in current folder if typelibs are not found and often 
	fails on modern OSes for lack of permissions to write in HKLM.

	There are Object lines in .frm/.ctl source files too. These get appended to 
	current project if adding existing form/usercontrol.

	If an .ocx typelib is added as Reference line the IDE usually fails to load the project 
	and a manual edit is needed.
	*/ 

	/*
	ScaleMode 	Meaning 
	0 	User-defined. 
	1 	Twips - 1440 per inch. 
	2 	Points - 72 per inch. 
	3 	Pixels - number per inch depends on monitor. 
	4 	Characters - character = 1/6 inch high and 1/12 inch wide. 
	5 	Inches. 
	6 	Millimeters. 
	7 	Centimeters.
	*/
	
	/*
	 *  15 twips são iguais a um pixel, e 567 twips equivale a 1 cm.
	 */
	
	
/* 

a) hex number with the appropriate rgb number.

"&H80000000": R = 213: G = 209: B = 200: 'Scroll Bars colour 
"&H80000001": R = 63: G = 109: B = 167: : 'Desktop colour
"&H80000002": R = 0: G = 0: B = 107: : 'Active Title Bar colour
"&H80000003": R = 128: G = 128: B = 128: 'Inactive Title Bar colour
"&H80000004": R = 213: G = 209: B = 200: 'Menu Bar colour
"&H80000005": R = 255: G = 255: B = 255: 'Window Background colour
"&H80000006": R = 0: G = 0: B = 0: 'Window Frame colour'
"&H80000007": R = 0: G = 0: B = 0: 'Menu Text colour
"&H80000008": R = 0: G = 0: B = 0: 'Window Text colour
"&H80000009": R = 255: G = 255: B = 255: 'Active Title Bar Text colour
"&H8000000A": R = 213: G = 209: B = 200: 'Active Border colour
"&H8000000B": R = 213: G = 209: B = 200: 'Inactive Border colour
"&H8000000C": R = 128: G = 128: B = 128: 'Application Workspace colour
"&H8000000D": R = 0: G = 0: B = 107: 'Highlight colour
"&H8000000E": R = 255: G = 255: B = 255: 'Highlight Text colour
"&H8000000F": R = 213: G = 209: B = 200: 'Button Face colour
"&H80000010": R = 128: G = 128: B = 128: 'Button Shadow colour
"&H80000011": R = 128: G = 128: B = 128: 'Disabled Text colour
"&H80000012": R = 0: G = 0: B = 0: 'Button Text colour
"&H80000013": R = 213: G = 209: B = 200: 'Inactive Toolbar Text colour
"&H80000014": R = 255: G = 255: B = 255: 'Button Highlight colour
"&H80000015': R = 213: G = 209: B = 200: 'Button Dark Shadow colour"
"&H80000016": R = 213: G = 209: B = 200: 'Button Light Shadow colour
"&H80000017": R = 0: G = 0: B = 0: 'Tooltip Text colour
"&H80000018": R = 255: G = 255: B = 225: 'Tooltip colour





b) Organised by RGB value equivalents


R = 0: G = 0: B = 0: "&H80000006": 'Window Frame colour'
R = 0: G = 0: B = 0: "&H80000007": 'Menu Text colour
R = 0: G = 0: B = 0: "&H80000008": 'Window Text colour
R = 0: G = 0: B = 0: "&H80000012": 'Button Text colour
R = 0: G = 0: B = 0: "&H80000017": 'Tooltip Text colour

R = 0: G = 0: B = 107: "&H80000002": 'Active Title Bar colour
R = 0: G = 0: B = 107: "&H8000000D": 'Highlight colour

R = 63: G = 109: B = 167: "&H80000001": 'Desktop colour

R = 128: G = 128: B = 128: "&H80000003": 'Inactive Title Bar colour
R = 128: G = 128: B = 128: "&H8000000C": 'Application Workspace colour
R = 128: G = 128: B = 128: "&H80000010": 'Button Shadow colour
R = 128: G = 128: B = 128: "&H80000011": 'Disabled Text colour

R = 213: G = 209: B = 200: "&H80000000": 'Scroll Bars colour 
R = 213: G = 209: B = 200: "&H80000004": 'Menu Bar colour
R = 213: G = 209: B = 200: "&H8000000A": 'Active Border colour
R = 213: G = 209: B = 200: "&H8000000B": 'Inactive Border colour
R = 213: G = 209: B = 200: "&H8000000F": 'Button Face colour
R = 213: G = 209: B = 200: "&H80000013": 'Inactive Toolbar Text colour
R = 213: G = 209: B = 200: "&H80000015': 'Button Dark Shadow colour"
R = 213: G = 209: B = 200: "&H80000016": 'Button Light Shadow colour

R = 255: G = 255: B = 255: "&H80000005": 'Window Background colour
R = 255: G = 255: B = 255: "&H80000009": 'Active Title Bar Text colour
R = 255: G = 255: B = 255: "&H8000000E": 'Highlight Text colour
R = 255: G = 255: B = 255: "&H80000014": 'Button Highlight colour
R = 255: G = 255: B = 225: "&H80000018": 'Tooltip colour  
 * 
 * 
 * 
 * COLOR IN VB6 = BGR => Ex.: 0000FF
 * PADRÃO       = RGB => Ex.: FF0000	
 */
/*   protected STGroup templates;
   protected Stack<ST> hierarchyChainST;
   protected ST rootST;
   protected ST htmlDocST;
   protected ST htmlTagST;
   protected ST styleTagST;
   protected ST attributeST;
   protected ST contentST;
   protected ST auxST;

   protected SymbolTable_New symTable;
   
   HashSet<String> htmlFiles;*/
   
   public VbFormToHtmlOld(String groupTemplates, SymbolTable_New _symTable){
	   super(groupTemplates, _symTable);
   }
   
   protected void run(){
	   List<Symbol_New>  formList= new ArrayList<Symbol_New>();
	   
	   formList =  symTable.getSymbolByProperty("CONTROL", "Form");
	   
	   if(formList.isEmpty()){
		   formList =  symTable.getSymbolByProperty("CONTROL", "MDIForm");
	   }
	   
/*	   for(int ix=0; ix < formList.size(); ix++) {
		   if(formList.get(ix).getProperty(THIS) != null) {
			   formList.remove(ix);
		   }
	   }*/
	   
	   boolean converted = false;
	   
	   Set visitedSymbol = new HashSet<Symbol_New>();
	   for(Symbol_New sym : formList){
		   if(sym.getProperty(THIS) != null) continue; // Pula Me references
		   if(!sym.hasProperty("CATEGORY", "UI"))  continue; //  NÃO TRATA CATEGORY={REFERENCE,COMPILATION_UNIT)
           if(visitedSymbol.contains(sym)) continue; // Existe o mesmo symbol em diferentes escopos
           visitedSymbol.add(sym);
		   //		   converted = true; // Cada form é definido em vários escopos da Symbol Table
		   System.err.println("Building web page: " + sym.getName());
		   clearST();
		   rootST = templates.getInstanceOf("root");
		   pushST(rootST);
		   htmlDoc(sym);
//		   System.err.println("\nWeb Page...\n" + rootST.render());
		   String fileName = sym.getName() + ".html";
		   try {
			sendHtmlToFile(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	   }
   }
   
   private void sendHtmlToFile(String fileName) throws FileNotFoundException{
	   // Verifica se existe forms diferentes com o mesmo nome
	   if(!htmlFiles.add(fileName)) fileName = fileName + "_" + System.currentTimeMillis() + ".html";
	   PrintStream printToFile = new PrintStream("C:\\HTML\\VB2HTML" + fileName);
	   PrintStream printToConsole = System.out;
	   System.setOut(printToFile);
	   System.out.println(rootST.render());
	   System.setOut(printToConsole);
	   printToConsole.close();
   }
   
   private void htmlDoc(Symbol_New form){
	   //ST auxST;
	   htmlDocST = templates.getInstanceOf("htmlDoc");
	   peekST().add("parts", htmlDocST);
	   pushST(htmlDocST);
	   auxST = htmlTag("html");
	   peekST().add("content", auxST);
	   pushST(auxST);
	   peekST().add("attribute", "lang=\"pt-br\"");
	   head(form);
	   body(form); 
   }
   
/*   private void htmlDocFrame(Symbol form){
	   //ST auxST;
	   htmlDocST = templates.getInstanceOf("htmlDoc");
	   peekST().add("parts", htmlDocST);
	   pushST(htmlDocST);
	   auxST = htmlTag("html");
	   peekST().add("content", auxST);
	   pushST(auxST);
	   peekST().add("attribute", "lang=\"pt-br\"");
	   head(form);
	   bodyFrame(form); 
   }*/
   
   private void head(Symbol_New form){
	   //ST auxST;
	   auxST = htmlTag("head");
	   peekST().add("content",auxST);
	   pushST(auxST); // starts head
	   
	   auxST = htmlTag("meta");
	   peekST().add("content",auxST);
	   pushST(auxST); // starts meta
	   attributeST = templates.getInstanceOf("attribute");
	   peekST().add("attribute", attributeST);
	   attributeST.add("attribName", "charset");
	   attributeST.add("attribValue", "UTF-8");	   
	   popST(); // ends meta
	   
	   auxST = htmlTag("title");
	   peekST().add("content", auxST);
	   pushST(auxST); // starts title
	   if(form.getProperty("Caption") != null){
		   peekST().add("content", ((String)form.getProperty("Caption")).replace("\"", ""));
	   }
	   else {
		   System.err.println("*** WARNING - No \"Caption\" in form " + form.getName() 
				    + " at line " + form.getContext().start.getLine());
	   }
	   popST(); // ends title
	   
	   styleTagST = htmlTag("style"); /* preenchido simultaneamente com os campos */
	   peekST().add("content",styleTagST);
	   
	   popST(); // ends head 
   }
   
   private void body(Symbol_New form){
	   auxST = htmlTag("body");
	   peekST().add("content",auxST);
	   pushST(auxST); // starts body
	   
	   auxST = htmlTag("div");
	   peekST().add("content",auxST);
	   pushST(auxST);   
	   String id = "id=" + "\"" + form.getName() + "\"";
	   peekST().add("attribute", id);
//	   getProperties(form);
	   setCSS(form);
	   
	   fillBody((Symbol_New)form);
	   
	   popST(); // ends div
	   
	   if(!(form instanceof IScope_New)){
		   System.err.println("Erro 149: Symbol is not scope: " + form.getName());
		   return;
	   }
	   popST(); // ends body
/*	   geraTag(form);*/
   }
   
/*   private void bodyFrame(Symbol form){
	   auxST = htmlTag("body");
	   peekST().add("content",auxST);
	   pushST(auxST); // starts body
	   
	   fillBody((Scope)form);
	   
	   if(!(form instanceof Scope)){
		   System.err.println("Erro 149: Symbol is not scope: " + form.getName());
		   return;
	   }
	   popST(); // ends body
   } */  
   
   private ST htmlTag(String tagname, boolean... _endTag){
	   htmlTagST = templates.getInstanceOf("htmlTag");
	   htmlTagST.add("startTagName", tagname);
	   if(_endTag.length > 0){
		   if(_endTag[0]) htmlTagST.add("endTagName", tagname);
	   }
	   else {
	   htmlTagST.add("endTagName", tagname);
	   }
	   return htmlTagST;
   }
   
   private void setCSS(Symbol_New _sym){
	   
	   Map<String,String> properties = new HashMap<String,String>();
//	   properties.put("background-color", "#FFFFC0");
	   properties.put("margin", "0px");
	   
	   Symbol_New scp = symTable.getScopeByProperty(_sym,"CONTROL", "Form"); 
	   
	   if(scp == null){
		   scp = symTable.getScopeByProperty(_sym,"CONTROL", "MDIForm"); 
	   }
	   
	   if(scp != null){
		   properties.put("position", "relative");
		   properties.put("background-color", "gainsboro");
		   properties.put("border-width", "1px");
		   properties.put("border-style", "dotted");
	   }
	   else {
		   properties.put("position", "absolute");
		   properties.put("border-width", "1px");
		   properties.put("border-style", "dotted");
	   }

//	   System.err.format("%nProperties of: %s%n", _sym.getName());
	   
	   /*CAPTION*/
	   if(_sym.getProperty("Caption") != null){
//		   System.err.format("Properties of: %s : %s%n", "Caption",
//		   ((String)_sym.getProperty("Caption")).replace("\"", ""));
		   
		   scp = symTable.getScopeByProperty(_sym,"CONTROL", "Form");
		   if(scp == null){
			   if(_sym.getProperty("CONTROL") != null){
				   if (_sym.hasProperty("CONTROL", "CommandButton")){
					   addButton(_sym,properties);
				   }
				   else if(_sym.hasProperty("CONTROL", "CheckBox")) {
					   addCheckBox(_sym,properties);
					   }
				   else {
					   peekST().add("content",((String)_sym.getProperty("Caption")).replace("\"", ""));
				   }
			   }
		   }
	   }
	   
	   /*WIDTH*/
	   if(_sym.getProperty("ClientWidth") != null){
/*		   System.err.format("Properties of: %s : %s%n", "ClientWidth",
		   (String)_sym.getProperty("ClientWidth"));*/
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("ClientWidth"))/15;
		   properties.put("width", Twips2Pixel.toString() + "px");
	   }
	   else if(_sym.getProperty("Width") != null){
/*		   System.err.format("Properties of: %s : %s%n", "Width",
		   (String)_sym.getProperty("Width"));*/
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("Width"))/15;
		   properties.put("width", Twips2Pixel.toString() + "px");
	   }
	   
	   /*HEIGHT*/
	   if(_sym.getProperty("ClientHeight") != null){
/*		   System.err.format("Properties of: %s : %s%n", "ClientHeight",
		   (String)_sym.getProperty("ClientHeight"));*/
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("ClientHeight"))/15;
		   properties.put("height", Twips2Pixel.toString() + "px");
	   }
	   else if(_sym.getProperty("Height") != null){
/*		   System.err.format("Properties of: %s : %s%n", "Height",
		   ((String)_sym.getProperty("Height")).replace("\"", ""));*/
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("Height"))/15;
		   properties.put("height", Twips2Pixel.toString() + "px");
	   }
	   
	   /*TOP*/
	   if(_sym.getProperty("ClientTop") != null){
/*		   System.err.format("Properties of: %s : %s%n", "ClientTop",
		   (String)_sym.getProperty("ClientTop"));*/
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("ClientTop"))/15;
		   properties.put("top", Twips2Pixel.toString() + "px");
	   }
	   else if(_sym.getProperty("Top") != null){
/*		   System.err.format("Properties of: %s : %s%n", "Top",
		   ((String)_sym.getProperty("Top")).replace("\"", ""));*/
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("Top"))/15;
		   properties.put("top", Twips2Pixel.toString() + "px");
	   }
	   
	   /*LEFT*/
	   if(_sym.getProperty("ClientLeft") != null){
/*		   System.err.format("Properties of: %s : %s%n", "ClientLeft",
		   (String)_sym.getProperty("ClientLeft"));*/
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("ClientLeft"))/15;
		   properties.put("left", Twips2Pixel.toString() + "px");
	   }
	   else if(_sym.getProperty("Left") != null){
/*		   System.err.format("Properties of: %s : %s%n", "Left",
		   ((String)_sym.getProperty("Left")).replace("\"", ""))*/;
		   Integer Twips2Pixel = Integer.parseInt((String)_sym.getProperty("Left"))/15;
		   properties.put("left", Twips2Pixel.toString() + "px");
	   }
	   
	   /*FONT*/
	   if(_sym.getProperty("Font") != null){
		   PropertyList fontProp = (PropertyList)_sym.getProperty("Font");
		   getFontProperties(fontProp, properties);
	   }
	   
	   /*VISIBILITY*/
	   if(_sym.getProperty("Visible") != null){ 
		   String value = (String)_sym.getProperty("Visible");
		   if (value.equalsIgnoreCase("0")){
			   properties.put("visibility", "hidden");
		   }
	   }
	   
	   /*FORE COLOR*/
	   if(_sym.getProperty("ForeColor") != null){ 
		   String color = (String)_sym.getProperty("ForeColor");
		   if(color.startsWith("&")) {
			   String red = color.substring(8, 10);
			   String green = color.substring(6, 8);
			   String blue = color.substring(4, 6);
			   color = "#" + red + green + blue;
			   properties.put("color", color);
		   }
	   }
	   
	   /*BACK COLOR*/
	   if(_sym.getProperty("BackColor") != null){ 
		   String color = (String)_sym.getProperty("BackColor");
		   if(color.startsWith("&")) {
			   String red = color.substring(8, 10);
			   String green = color.substring(6, 8);
			   String blue = color.substring(4, 6);
			   color = "#" + red + green + blue;
			   properties.put("background-color", color);
		   }		   
	   }	   
	   
	   /*TRANSPARENT*/
	   if(_sym.hasProperty("CONTROL", "Label")){ 
//		   if(_sym.getProperty("BackStyle") == null){
		   if(_sym.hasProperty("BackStyle", "0")){
			   properties.put("opacity", ".5");
		   }
	   }	   
	   
	   /*Fixa Frame Name*/ 
	   if(_sym.getProperty("CONTROL") != null){
		   if (_sym.hasProperty("CONTROL", "Frame")){
			   properties.put("font-size", "8px");
		   }
	   }
	   
	   if (_sym.hasProperty("CONTROL", "TextBox")){
		   textBoxProperty(_sym, properties);
	   }
	   
	   if (_sym.hasProperty("CONTROL", "MSFlexGrid")){
		   mSFlexGridProperty(_sym, properties);
	   }	   
	   
	   
	   if (_sym.hasProperty("CONTROL", "Frame")){
		   frameProperty(_sym, properties);
	   }
	   
	   if (_sym.hasProperty("CONTROL", "CheckBox")){
		   checkBoxProperty(_sym, properties);
	   }	   
	   
	   /*Fixa Label Border Style*/ 
	   if(_sym.getProperty("CONTROL") != null){
		   if (_sym.hasProperty("CONTROL", "Label") && _sym.getProperty("Caption") != null){
			   labelProperty(_sym, properties);
//			   properties.put("border-style", "hidden");
		   }
	   }
	   
	   ST cssSelectorST = templates.getInstanceOf("cssSelector");
	   styleTagST.add("content", cssSelectorST);
	   
	   String cssSelectorName = "#" +_sym.getName();
	   cssSelectorST.add("selector", cssSelectorName);

	   for(String key : properties.keySet()){
		   ST propertyST = templates.getInstanceOf("property");
		   propertyST.add("propertyName", key);
		   propertyST.add("propertyValue", properties.get(key));
		   cssSelectorST.add("property", propertyST);
	   }
   }
   
   private void fillBody(Symbol_New component){
	   for(StringOptionalCase bc : component.getMembers().keySet()){
//		   Symbol_New sym = compoment.getMembers().get(bc).get(0);
		    Member m = component.getMembers().get(bc);
		    Symbol_New sym = m.getSymbols().iterator().next();

		   auxST = htmlTag("div");
		   peekST().add("content",auxST);
		   pushST(auxST);   
		   String id = "id=" + "\"" + sym.getName() + "\"";
		   peekST().add("attribute", id);
		   setCSS(sym);
		   fillBody((Symbol_New) sym); //ZE
		   popST();
	   }
   }
	   
   private String montaShortCut(String _s, String _shortCutChar){
	   int i = _s.indexOf(_shortCutChar);
	   String caption = _s;
	   if(i < 0) return _s;
	   caption = caption.replace(_shortCutChar, "");
	   
	   if(i == 0){
		   caption =  "<span style=\"text-decoration:underline\">" 
				   + caption.substring(i, i+1) + "</span>" + caption.substring(i+1);
	   }
	   else if(i < _s.length() -1){
		   caption = caption.substring(0, i) + "<span style=\"text-decoration:underline\">" 
				   + caption.substring(i, i+1) + "</span>" + caption.substring(i+1);
	   }
	   else {
		   caption = caption.substring(0, i) + "<span style=\"text-decoration:underline\">" 
				   + caption.substring(i, i+1) + "</span>";
	   }
	   return caption;
   }
   
   private void addButton(Symbol_New _sym,  Map<String,String> _propreties){
	   if(_sym.getProperty("CONTROL") != null){
		   if (_sym.hasProperty("CONTROL", "CommandButton")){
			   String caption = ((String)_sym.getProperty("Caption")).replace("\"", "");
			   peekST().add("content", montaShortCut(caption, "&"));
			   commandButtonProperty(_sym, _propreties);
		   } 
	   }	
   }
   
   private void addCheckBox(Symbol_New _sym,  Map<String,String> _propreties){
		   if (_sym.hasProperty("CONTROL", "CheckBox")){
			   auxST = htmlTag("input");
			   peekST().add("content",auxST);
			   auxST.add("attribute", getAttributeST("type", "checkbox"));
			   auxST.add("attribute", getAttributeST("value", "Show"));
			   auxST.add("content",((String)_sym.getProperty("Caption")).replace("\"", ""));
			   checkBoxProperty(_sym, _propreties);
		   } 
   }
   
   private ST getAttributeST(String _key, String _value){
	   attributeST = templates.getInstanceOf("attribute");
	   attributeST.add("attribName", _key);
	   attributeST.add("attribValue", _value);
	   return attributeST;
   }
   
   private void getFontProperties(PropertyList _fontProperties, Map<String,String> _prop){
	   for(String key : _fontProperties.getProperties().keySet() ){
		   if(key.equalsIgnoreCase("Size")){
			   _prop.put("font-size", (String)_fontProperties.getProperty(key) + "px");
		   }
		   else if(key.equalsIgnoreCase("Weight")){
			   _prop.put("font-weight", (String)_fontProperties.getProperty(key));
		   }
		   else if(key.equalsIgnoreCase("Name")){
			   String fontFamily = (String)_fontProperties.getProperty(key);
			    fontFamily = fontFamily.replace("MS ", "");
//			   _prop.put("font-family", "\"sans-serif\"");
			   _prop.put("font-family", fontFamily);
		   }
		   else if(key.equalsIgnoreCase("Italic")){
			if(!((String)_fontProperties.getProperty(key)).equals("0")){   
			   _prop.put("font-style", "italic");
			}
		   }
	   }
   } 
   
   private void labelProperty(Symbol_New _sym, Map<String,String> _properties){
	   _properties.put("border-style", "hidden");
   }

   private void frameProperty(Symbol_New _sym, Map<String,String> _properties){
	   _properties.put("border-color", "gainsboro");
	   _properties.put("border-style", "solid");
   }

   private void mSFlexGridProperty(Symbol_New _sym, Map<String,String> _properties){
	   _properties.put("border-color", "gainsboro");
	   _properties.put("background-color", "gainsboro");
   }   
   
   private void textBoxProperty(Symbol_New _sym, Map<String,String> _properties){
	   _properties.put("border-style", "inset");
	   _properties.put("background-color", "white");
   }  
   
   private void commandButtonProperty(Symbol_New _sym, Map<String,String> _properties){
	   _properties.put("display", "flex");
	   _properties.put("justify-content", "center");
	   _properties.put("align-items", "center");
	   _properties.put("background-color", "gainsboro");
	   _properties.put("border-style", "outset");
   }   

   private void checkBoxProperty(Symbol_New _sym, Map<String,String> _properties){
	   _properties.put("display", "flex");
	   _properties.put("align-items", "center");
	   _properties.put("border-style", "none");
	   /*  _properties.put("background-color", "gainsboro");*/
   }  
}