package br.com.bicam.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.model.util.PropertyListSerializable;
import br.com.bicam.util.catalog.InputGraph;
import br.com.bicam.util.graph.BicamNode;
import br.com.bicam.util.graph.NodeList;
import br.com.bicam.util.symboltable.SymbolTable_New;
import br.com.bicam.util.symboltable.Symbol_New;

public class BicamSystem {
	static final String INPUT_STREAM = "INPUT_STREAM";
	static final String FILE_NAME = "FILE_NAME";
	static final String XML_FILE = "XML_FILE";

	 static final String[] sulfixTypeIndicator = new String[] { "&", "%", "#", "!", "@", "$" };

	 static final Map<Character,String> toHtmlEntity = new HashMap<Character,String>() {{
			put(' ',"&nbsp;");
			put('\'',"&quot;");				
			put('(',"&lpar;");
			put(')',"&rpar;");
			put('#',"&num;");
			put('$',"&dollar;");
			put('%',"&percnt;");
			put('\'',"&apos;");
			put('*',"&ast;");
			put('+',"&plus;");
			put('.',"&period;");
			put('/',"&sol;");
			put(':',"&colon;");
			put(';',"&semi;");
			put('=',"&equals;");
			put('>',"&equals;");
			put('?',"&quest;");
			put('@',"&commat;");
			put('[',"&lsqb;");
			put(']',"&rsqb;");
			put('\\',"&bsol;");
			put('^',"&hat;");
			put('_',"&lowbar;");
			put('{',"&lcub;");
			put('}',"&rcub;");

			put('Á',"&Aacute;");			
			put('É',"&Eacute;");
			put('Í',"&Iacute;");
			put('Ó',"&Oacute;");
			put('Ú',"&Uacute;");
			put('á',"&aacute;");
			put('é',"&eacute;");
			put('í',"&iacute;");
			put('ó',"&oacute;");
			put('ú',"&uacute;");
			put('Â',"&Acirc;");
			put('Ê',"&Ecirc;");
//			put('Ô',"&Ocirc;");
			put('â',"&acirc;");
			put('ê',"&ecirc;");
//			put('ô',"&ocirc;");
//			put('À',"&Agrave;");
//			put('È',"&Egrave;");
//			put('à',"&agrave;");
//			put('è',"&egrave;");
//			put('Ü',"&Uuml;");
//			put('ü',"&uuml;");
			put('Ç',"&Ccedil;");			
			put('ç',"&ccedil;");
			put('Ã',"&Atilde;");			
			put('ã',"&atilde;");
			put('Õ',"&Otilde;");			
			put('õ',"&otilde;");
//			put('Ñ',"&Ntilde;");			
//			put('ñ',"&ntilde;");
	 }};
	 
	 
		
	public static String toHtml(SymbolTable_New _st, ParserRuleContext _ctx, String _char) {
		String c = toHtmlEntity.get(_char);
		if(c == null) {
			printLog(_st, _ctx, "ERROR", "CHARACTER "  + _char + " NOT FOUND TO CONVERT HTML ENTITY");
		}
		return c;
	}

	public static String toHtml(String _char) {
		String c = toHtmlEntity.get(_char);
		if(c == null) {
			return _char;
		}
		return c;
	}
	
	public static  void printLog(SymbolTable_New _st, ParserRuleContext _ctx, String _severity, String _msg, Exception... e) {
		System.err.format("*** %s - %s at line %d in compilation unit %s%n",
				   _severity, _msg,  _ctx.start.getLine(),_st.getCompilationUnitName(_ctx));
		if(_severity.equalsIgnoreCase("ERROR")) {
			printStackTrace(e.length > 0 ? e[0]: null);
			System.exit(1);
		}
	}	
	
	public static  void printLog(String _severity, String _msg) {
		try {
			throw new Exception();
		}catch (Exception e) {
			printLog(_severity,_msg,e);
		}
	}
	
	public static  void printLog(String _severity, String _msg, Exception e) {
		System.err.format("*** %s - %s%n",
				   _severity, _msg);
		if(_severity.equalsIgnoreCase("ERROR")) {
			e.printStackTrace();
			System.exit(1);
		}	
	}
	
	private static void printStackTrace(Exception e) {
		if(e != null) {
			e.printStackTrace();
		}
	}
	
	public static File toHtml(String _fileInName, String _fileOutName, PropertyList... _properties) {
		Integer rangeStartLineMark = null;
		Integer rangeEndLineMark = null;

		Integer pathStartLine = null;
		Integer pathTargetLine = null;

		Map<Integer,String> lineToNodeId = null;
		
		Set<Integer> pathMarkedLines = null;
		
		if(_properties.length > 0) {
			rangeStartLineMark = (Integer)_properties[0].getProperty("START_LINE_MARK");
			rangeEndLineMark = (Integer)_properties[0].getProperty("END_LINE_MARK");
			lineToNodeId  = (Map)_properties[0].getProperty("LINE_TO_NODE");
			
			if(rangeStartLineMark == null) {
				pathStartLine = (Integer)_properties[0].getProperty("PATH_START_LINE");
				pathMarkedLines = (Set)_properties[0].getProperty("PATH_MARKED_LINES");
				pathTargetLine = (Integer)_properties[0].getProperty("PATH_TARGET_LINE");
			}
        }
		
		File fileOut = null;
		try {
			Pattern.compile(".*(\\r\\n|\\r|\\n)|.+\\z"); 
			
			fileOut = new File(_fileOutName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			StringBuffer sbLine= new StringBuffer();

			sbLine.append("<table style=\"float: left;width: 100%;\">"+System.lineSeparator());
			int lineCount = 0;

			String line;
	        String tdColNumberwidth          = "width: 10%; align: right;";
	        String tdColMarkwidth            = "width: 5%; align: right;";
	        String tdColTextwidth            = "width: 85%; align: left;";
	        String tdColTargetImg            = "<img src=\"file:///C:/Users/josez/Downloads/placeholder-filled-point-red.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left;\">";
//	        String tdColStartPathImg         = "<img src=\"file:///C:/Users/josez/Downloads/right-arrow.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left;\">";
	        String tdColStartPathImg         = "<img src=\"file:///C:/Users/josez/Downloads/prohibited.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left;\">";

	        String tdColMarkedPathImg        = "<img src=\"file:///C:/Users/josez/Downloads/footsteps-silhouette-variant.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left; transform: rotate(180deg\">";

	        String tdColTextMarkedBgColor    = "background-color:#91DC5A;";
	        String tdUnmarkbleLineBgColor    = "background-color:#f0f0f5;";

			StringBuffer lineHtml = new StringBuffer();
			try (Scanner in = new Scanner(Paths.get(_fileInName), "Cp1252")) {
				in.useDelimiter(System.lineSeparator());
				while(in.hasNext()) {
					line = in.next();
					lineCount++;
					
					String cssId = null;

					if(lineToNodeId == null || lineToNodeId.get(lineCount) == null) {
						cssId = " ";
					}
					else {
						cssId = "id=\"" +  lineToNodeId.get(lineCount) + "\" ";
					}
					
			    	line = line.replaceAll("\\t", "    "); // tab spaces
			    	
			    	for (int i=0; i < line.length(); i++) {
			    		String c = toHtmlEntity.get(line.charAt(i)); 
			    		if (c== null) {
			    			c = Character.toString(line.charAt(i));
			    		}
			    		lineHtml.append(c);
			    	}
			    	  
					if(rangeStartLineMark != null && lineCount>= rangeStartLineMark && lineCount<= rangeEndLineMark ) {
						sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
						sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + "" + "</td>"+System.lineSeparator());
						sbLine.append("<td class=\"textCol\" style=\"" + tdColTextwidth + tdColTextMarkedBgColor + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
					} else if(pathMarkedLines != null) {
						if(pathMarkedLines.contains(lineCount)) {
							if(pathTargetLine == lineCount) {
								sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
								sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + tdColTargetImg + "</td>"+System.lineSeparator());
							}
							else if(pathStartLine == lineCount) {
								sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
								sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + tdColStartPathImg + "</td>"+System.lineSeparator());
							}
							else {
								sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
								sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + tdColMarkedPathImg + "</td>"+System.lineSeparator());
							}
							sbLine.append("<td class=\"textCol\" style=\"" + tdColTextwidth + tdColTextMarkedBgColor + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
						}
						else {
							sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
							sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + "" + "</td>"+System.lineSeparator());
							sbLine.append("<td class=\"textCol\"  style=\"" + tdColTextwidth + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
						}
					}
					else {
						sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
						sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + "" + "</td>"+System.lineSeparator());
						sbLine.append("<td  class=\"textCol\" style=\"" + tdColTextwidth + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
					}
					
			    	lineHtml.setLength(0);
				}
				} catch (IOException e) {
					e.printStackTrace();
				}		
			sbLine.append("</table>"+System.lineSeparator());
			
			out.write("<!DOCTYPE html>"+System.lineSeparator()
			+"<html>"+System.lineSeparator()
			+"<head>"+System.lineSeparator()
			+"<meta charset=\"UTF-8\">"+System.lineSeparator()
			+"<script src=\"jquery-3.3.1.js\"></script>"+System.lineSeparator()
			+getJQueryScript()+System.lineSeparator()
			+"</head>"+System.lineSeparator()
			+ "<body style=\"font-family:courier;\">"+System.lineSeparator()
			+ "<button type=\"button\">Show start/end</button>" + System.lineSeparator()
			+"<div>");
			out.write(sbLine.toString());
			out.write("</div>"+System.lineSeparator()
			+"</body>"+System.lineSeparator()
			+"</html>");
			
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileOut; 
	}
//==============
	public static File toHtml(InputStream _inputStream, String _fileOutName, PropertyList... _properties) {
		Integer rangeStartLineMark = null;
		Integer rangeEndLineMark = null;

		Integer pathStartLine = null;
		Integer pathTargetLine = null;

		Map<Integer,String> lineToNodeId = null;
		
		Set<Integer> pathMarkedLines = null;
		
		if(_properties.length > 0) {
			rangeStartLineMark = (Integer)_properties[0].getProperty("START_LINE_MARK");
			rangeEndLineMark = (Integer)_properties[0].getProperty("END_LINE_MARK");
			lineToNodeId  = (Map)_properties[0].getProperty("LINE_TO_NODE");
			
			if(rangeStartLineMark == null) {
				pathStartLine = (Integer)_properties[0].getProperty("PATH_START_LINE");
				pathMarkedLines = (Set)_properties[0].getProperty("PATH_MARKED_LINES");
				pathTargetLine = (Integer)_properties[0].getProperty("PATH_TARGET_LINE");
			}
        }
		
		File fileOut = null;
		try {
			Pattern.compile(".*(\\r\\n|\\r|\\n)|.+\\z"); 
			
			fileOut = new File(_fileOutName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			StringBuffer sbLine= new StringBuffer();

			sbLine.append("<table style=\"float: left;width: 100%;\">"+System.lineSeparator());
			int lineCount = 0;

			String line;
	        String tdColNumberwidth          = "width: 10%; align: right;";
	        String tdColMarkwidth            = "width: 5%; align: right;";
	        String tdColTextwidth            = "width: 85%; align: left;";
	        String tdColTargetImg            = "<img src=\"file:///C:/Users/josez/Downloads/placeholder-filled-point-red.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left;\">";
	        String tdColStartPathImg         = "<img src=\"file:///C:/Users/josez/Downloads/prohibited.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left;\">";

	        String tdColMarkedPathImg        = "<img src=\"file:///C:/Users/josez/Downloads/footsteps-silhouette-variant.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left; transform: rotate(180deg\">";

	        String tdColTextMarkedBgColor    = "background-color:#91DC5A;";
	        String tdUnmarkbleLineBgColor    = "background-color:#f0f0f5;";

			StringBuffer lineHtml = new StringBuffer();
			try (Scanner in = new Scanner(_inputStream, "Cp1252")) {
				in.useDelimiter(System.lineSeparator());
				while(in.hasNext()) {
					line = in.next();
					lineCount++;
					
					String cssId = null;

					if(lineToNodeId == null || lineToNodeId.get(lineCount) == null) {
						cssId = " ";
					}
					else {
						cssId = "id=\"" +  lineToNodeId.get(lineCount) + "\" ";
					}
					
			    	line = line.replaceAll("\\t", "    "); // tab spaces
			    	
			    	for (int i=0; i < line.length(); i++) {
			    		String c = toHtmlEntity.get(line.charAt(i)); 
			    		if (c== null) {
			    			c = Character.toString(line.charAt(i));
			    		}
			    		lineHtml.append(c);
			    	}
			    	  
					if(rangeStartLineMark != null && lineCount>= rangeStartLineMark && lineCount<= rangeEndLineMark ) {
						sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
						sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + "" + "</td>"+System.lineSeparator());
						sbLine.append("<td class=\"textCol\" style=\"" + tdColTextwidth + tdColTextMarkedBgColor + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
					} else if(pathMarkedLines != null) {
						if(pathMarkedLines.contains(lineCount)) {
							if(pathTargetLine == lineCount) {
								sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
								sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + tdColTargetImg + "</td>"+System.lineSeparator());
							}
							else if(pathStartLine == lineCount) {
								sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
								sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + tdColStartPathImg + "</td>"+System.lineSeparator());
							}
							else {
								sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
								sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + tdColMarkedPathImg + "</td>"+System.lineSeparator());
							}
							sbLine.append("<td class=\"textCol\" style=\"" + tdColTextwidth + tdColTextMarkedBgColor + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
						}
						else {
							sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
							sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + "" + "</td>"+System.lineSeparator());
							sbLine.append("<td class=\"textCol\"  style=\"" + tdColTextwidth + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
						}
					}
					else {
						sbLine.append("<tr><td " + cssId  + " class=\"column\" style=\"" + tdColNumberwidth + "\">" + lineCount + "</td>"+System.lineSeparator());
						sbLine.append("<td class=\"markCol\" style=\"" + tdColMarkwidth + "\">" + "" + "</td>"+System.lineSeparator());
						sbLine.append("<td  class=\"textCol\" style=\"" + tdColTextwidth + "\">" + lineHtml + "</td></tr>"+System.lineSeparator());
					}
					
			    	lineHtml.setLength(0);
				}
				}		
			sbLine.append("</table>"+System.lineSeparator());
			
			out.write("<!DOCTYPE html>"+System.lineSeparator()
			+"<html>"+System.lineSeparator()
			+"<head>"+System.lineSeparator()
			+"<meta charset=\"UTF-8\">"+System.lineSeparator()
			+"<script src=\"jquery-3.3.1.js\"></script>"+System.lineSeparator()
			+getJQueryScript()+System.lineSeparator()
			+"</head>"+System.lineSeparator()
			+ "<body style=\"font-family:courier;\">"+System.lineSeparator()
			+ "<button type=\"button\">Show start/end</button>" + System.lineSeparator()
			+"<div>");
			out.write(sbLine.toString());
			out.write("</div>"+System.lineSeparator()
			+"</body>"+System.lineSeparator()
			+"</html>");
			
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileOut; 
	}	
	
	/*
	 * Dado um arquivo com a linha para formação de grapho... a b c
	 * onde: a e b são vértices e c é o peso da aresta
	 */
	public static PropertyList graphVertices(File _file, String _separator) {
		Set<String> vertices = new HashSet<String>();
		List<String> nameList;
		String[] vertexToName;
		Map<String,Integer> nameToVertex = new HashMap<String,Integer>();
		
		String line = null;
		try {
		Scanner	scanner = new Scanner(_file);

		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			String[] parts = line.split(_separator);
			if(parts[0].length() == 0) continue; // linha em branco
			if(parts.length > 3 ) {
				BicamSystem.printLog("WARNING", "INVALID INPUT VERTICES. DISCARDED." + line);
				continue;
			}
			for(int i = 0; i < parts.length && i < 2; i++) {
				vertices.add(parts[i]);
			}
		}
		scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		nameList = new ArrayList<String>(vertices);
		
		vertexToName = new String[vertices.size()];
		
		for(int i = 0; i < vertexToName.length; i++){
			vertexToName[i] = nameList.get(i);
			nameToVertex.put(nameList.get(i), i);
		}
		
		PropertyList properties = new PropertyList();
		properties.addProperty("VERTICES", vertices);
		properties.addProperty("VERTEX_TO_NAME", vertexToName);
		properties.addProperty("NAME_TO_VERTEX", nameToVertex);
		return properties;
	}

	/*
	 * Dado um arquivo com a linha para formação de grapho... a b c
	 * onde: a e b são vértices e c é o peso da aresta
	 */
	public static PropertyList graphVertices(List<InputGraph> _inputGraph, String _separator) {
		Set<String> vertices = new HashSet<String>();
		List<String> nameList;
		String[] vertexToName;
		Map<String,Integer> nameToVertex = new HashMap<String,Integer>();

		for(InputGraph ig : _inputGraph) {
			if(ig.getHeadNode() != null) vertices.add(ig.getHeadNode());
			if(ig.getTailNode() != null) vertices.add(ig.getTailNode());
		}
		
		nameList = new ArrayList<String>(vertices);
		
		vertexToName = new String[vertices.size()];
		
		for(int i = 0; i < vertexToName.length; i++){
			vertexToName[i] = nameList.get(i);
			nameToVertex.put(nameList.get(i), i);
		}
		
		PropertyList properties = new PropertyList();
		properties.addProperty("VERTICES", vertices);
		properties.addProperty("VERTEX_TO_NAME", vertexToName);
		properties.addProperty("NAME_TO_VERTEX", nameToVertex);
		
		return properties;
	}
	
	public static PropertyList graphVertices(NodeList _nodeList, String _separator) {
		Set<String> vertices = new HashSet<String>();
		List<String> nameList;
		String[] vertexToName;
		Map<String,Integer> nameToVertex = new HashMap<String,Integer>();
		
		for(BicamNode node : _nodeList.getNodes()) {
			vertices.add(node.getId());
		}
		
		nameList = new ArrayList<String>(vertices);
		
		vertexToName = new String[vertices.size()];
		
		for(int i = 0; i < vertexToName.length; i++){
			vertexToName[i] = nameList.get(i);
			nameToVertex.put(nameList.get(i), i);
		}
		
		PropertyList properties = new PropertyList();
		properties.addProperty("VERTICES", vertices);
		properties.addProperty("VERTEX_TO_NAME", vertexToName);
		properties.addProperty("NAME_TO_VERTEX", nameToVertex);
		
		return properties;
	}	
	
	public static String[] removeParen(String[] _nameParts) { // A(i) -> A
		int parenCount = 0;
		String _name = _nameParts[0];
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < _name.length(); i++) {
			if (_name.substring(i, i + 1).equals("(")) {
				parenCount++;
				continue;
			}
			if (_name.substring(i, i + 1).equals(")")) {
				parenCount--;
				continue;
			}
			if (parenCount == 0) {
				s = s.append(_name.substring(i, i + 1));
			}
		}
		_nameParts[0] = s.toString();
		return _nameParts;
	}

	public static String[] removeParenETypeIndicator(String _name) {
		return removeParen(removeTypeIndicator(_name));
	}

	public static String[] removeTypeIndicator(String name) {
		ArrayList<String> parts = new ArrayList<String>();
		for (String s : sulfixTypeIndicator) {
			if (name.endsWith(s)) {
				name = name.replace(s, ""); // A$ -> A
				parts.add(name);
				parts.add(s);
				break;
			}
		}
		if (parts.size() == 0)
			parts.add(name);

		String[] ret = new String[parts.size()];
		for (int ix = 0; ix < parts.size(); ix++) {
			ret[ix] = parts.get(ix);
		}
		return ret;
	}
	
	public static String sqlNameToFullQualifiedName(String _name, String..._qualifier) {
		final int NUMPARTS = _name.split("\\.").length;
		String q = null;
		if(_qualifier.length == 0) {
			q = "SERVER_DEFAULT" + "." + "DATABASE_DEFAULT" + "." + "DB_OWNER_DEFAULT" + ".";
		}
		else {
			q = _qualifier[0];
		}

		switch(NUMPARTS) {
			case 1: return simpleName(_name,q);     // object
			case 2: return twoPartName(_name,q);    // owner.object or tempdb.object
			case 3: return threePartName(_name,q);  // database.owner.object or database..object
			case 4: return fourPartName(_name,q);   // server.database.owner.object or missing database or owner
			default:
				try {
					throw new Exception();
				}
				catch(Exception e){
					BicamSystem.printLog("ERROR", "Invalid sql name", e);
				}
		}
		return null;
	}

	private static String simpleName(String _name, String _q) {
		if(_name.startsWith("#")) {
			return "SERVER_TEMPDB" + "." + "TEMPDB" + "." + "DB_OWNER_DEFAULT" + "." + _name;
		}
//		return "SERVER_DEFAULT" + "." + "DATABASE_DEFAULT" + "." + "DB_OWNER_DEFAULT" + "." + _name;
		return _q.split("\\.")[0] + "." + _q.split("\\.")[1] + "." + _q.split("\\.")[2] + "." + _name;
	}
	
	private static String twoPartName(String _name, String _q) {
		if(_name.split("\\.")[0].equalsIgnoreCase("TEMPDB")) {
			return "SERVER_TEMPDB" + "." + "TEMPDB" + "." + "DB_OWNER_DEFAULT" + "." + _name;
		}
//		return "SERVER_DEFAULT" + "." + "DATABASE_DEFAULT" + "." + _name.split("\\.")[0] + "." + _name.split("\\.")[1];
		return _q.split("\\.")[0] + "." + _q.split("\\.")[1] + "." + _name.split("\\.")[0] + "." + _name.split("\\.")[1];
	}
	
	private static String threePartName(String _name, String _q) {
		if(_name.split("\\.")[1].length() == 0) { // db..objet
//			return "SERVER_DEFAULT" + "." + _name.split("\\.")[0] + "." + "DB_OWNER_DEFAULT" + "." + _name.split("\\.")[2];
			return _q.split("\\.")[0] + "." + _name.split("\\.")[0] + "." + _q.split("\\.")[2] + "." + _name.split("\\.")[2];
		}
		else { // db.ownerdb.object
		//	return "SERVER_DEFAULT" + "." + _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + _name.split("\\.")[2];
		return _q.split("\\.")[0] + "." + _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + _name.split("\\.")[2];
		}
	}	
	
	private static String fourPartName(String _name, String _q) { 
		if(_name.split("\\.")[1].length() == 0 && _name.split("\\.")[2].length() > 0) { // server..owner.object
//			return _name.split("\\.")[0] + "." + "DATABASE_DEFAULT" + "." + _name.split("\\.")[2] + "." + _name.split("\\.")[3];
			return _name.split("\\.")[0] + "." + _q.split("\\.")[1] + "." + _name.split("\\.")[2] + "." + _name.split("\\.")[3];
		}
		else if(_name.split("\\.")[2].length() == 0 && _name.split("\\.")[1].length() > 0) { // server.database..object
//			return _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + "DB_OWNER_DEFAULT" + "." + _name.split("\\.")[3];
			return _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + _q.split("\\.")[2] + "." + _name.split("\\.")[3];
		}
		else if(_name.split("\\.")[1].length() == 0 && _name.split("\\.")[2].length() == 0) { // server...object
//			return _name.split("\\.")[0] + "." + "DATABASE_DEFAULT" + "." + "DB_OWNER_DEFAULT" + "." + _name.split("\\.")[3];
			return _name.split("\\.")[0] + "." + _q.split("\\.")[1] + "." + _q.split("\\.")[2] + "." + _name.split("\\.")[3];
		}
//		return _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + _name.split("\\.")[2] + "." + _name.split("\\.")[3];
		return _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + _name.split("\\.")[2] + "." + _name.split("\\.")[3];
	}
	
/*	private static String fourPartName(String _name, String _compUnitFullSqlname) { x
		String parts[] = _compUnitFullSqlname.split("\\.");
		String serverName = parts[0];
		String dbNameName = parts[1];
		String dbOwnerName = parts[2];
		String sqlName = parts[3];
		
		if(_name.split("\\.")[1].length() == 0 && _name.split("\\.")[2].length() > 0) { // server..owner.object
			return _name.split("\\.")[0] + "." + dbNameName + "." + _name.split("\\.")[2] + "." + _name.split("\\.")[3];
		}
		else if(_name.split("\\.")[2].length() == 0 && _name.split("\\.")[1].length() > 0) { // server.database..object
			return _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + dbOwnerName + "." + _name.split("\\.")[3];
		}
		else if(_name.split("\\.")[1].length() == 0 && _name.split("\\.")[2].length() == 0) { // server...object
			return _name.split("\\.")[0] + "." + dbNameName + "." + dbOwnerName + "." + _name.split("\\.")[3];
		}
		return _name.split("\\.")[0] + "." + _name.split("\\.")[1] + "." + _name.split("\\.")[2] + "." + _name.split("\\.")[3];
	}*/
	
	public static String getJQueryScript() {
		return "<script>"+System.lineSeparator()
		+"var startPath, endPath;"+System.lineSeparator()
		+"var cssStart = null;"+System.lineSeparator()
		+"var cssEnd   = null;"+System.lineSeparator()
		+""+System.lineSeparator()
		+"//function setStartPath(id) {"+System.lineSeparator()
		+"//    document.getElementById(id).style.cssText = 'background-color:#91DC5A';"+System.lineSeparator()
		+"//	startPath = id;"+System.lineSeparator()
		+"//}"+System.lineSeparator()
		+""+System.lineSeparator()
		+"$(document).ready(function(){"+System.lineSeparator()
		+"    $(\"button\").click(function(){"+System.lineSeparator()
		+"		window.alert(startPath + '/' + endPath)"+System.lineSeparator()
		+"    });"+System.lineSeparator()
		+"    $(\"td.textCol\").dblclick(function(){"+System.lineSeparator()
		+"		window.alert(startPath + '/' + endPath)"+System.lineSeparator()
		+"    });"+System.lineSeparator()		
		+""+System.lineSeparator()
		+"	$(\"td.column\").click(function () {"+System.lineSeparator()
		+"		var $this = $(this);"+System.lineSeparator()
		+"		if ($this.hasClass('clicked')){"+System.lineSeparator()
		+"			$this.removeClass('clicked'); "+System.lineSeparator()
		+"		//----- code for double click"+System.lineSeparator()
		+"			endPath = $this.parent().children(\".column\").attr('id');"+System.lineSeparator()
		+"			"+System.lineSeparator()
		+"			if(endPath == startPath){"+System.lineSeparator()
		+"					startPath = undefined;"+System.lineSeparator()
		+"			}"+System.lineSeparator()
		+""+System.lineSeparator()
		+"			$this.parent().children().css('background-color', '#99c2ff'); "+System.lineSeparator()
		+"          $this.parent().children().siblings('.markCol').html('<img src=\"file:///C:/Users/josez/Downloads/placeholder-filled-point-red.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left;\">');" + System.lineSeparator()
		+"			$(cssEnd).parent().children().css('background-color', '');			"+System.lineSeparator()
		+"			$(cssEnd).parent().children().siblings(\".markCol\").html('');\r\n" + System.lineSeparator()
		+"			cssEnd = $this;"+System.lineSeparator()
		+"		}else{"+System.lineSeparator()
		+"			 $this.addClass('clicked');"+System.lineSeparator()
		+"			 setTimeout(function() { "+System.lineSeparator()
		+"				 if ($this.hasClass('clicked')){"+System.lineSeparator()
		+"					 $this.removeClass('clicked'); "+System.lineSeparator()
		+"		//----- code for single click"+System.lineSeparator()
		+"				startPath = $this.parent().children(\".column\").attr('id');"+System.lineSeparator()
		+"				if(startPath == endPath){"+System.lineSeparator()
		+"					endPath = undefined;"+System.lineSeparator()
		+"				}"+System.lineSeparator()
		+"				$this.parent().children().css('background-color', '#99c2ff');"+System.lineSeparator()
		+"              $this.parent().children().siblings('.markCol').html('<img src=\"file:///C:/Users/josez/Downloads/prohibited.svg\" style=\"max-width:10pt;max-height:10pt;border:0;width: auto;height: auto;align: left;\">');" + System.lineSeparator()
		+"				$(cssStart).parent().children().css('background-color', '');"+System.lineSeparator()
		+"			    $(cssStart).parent().children().siblings(\".markCol\").html('');\r\n" + System.lineSeparator()
		+"				cssStart = $this;				"+System.lineSeparator()
		+"				 }"+System.lineSeparator()
		+"			 }, 500);          "+System.lineSeparator()
		+"		}	"+System.lineSeparator()
		+"	});"+System.lineSeparator()
		+"});"+System.lineSeparator()
		+"</script>";
		}
	
	public static Object jaxbAdapterString(String _text) {
		String text = _text;
		Deque<Object> stack = new ArrayDeque();
		
		while(text.length() > 0) {
			String token = text.substring(0, 1);
			switch (token) {
			case "{" :
				text = startPropertyListSer(text, stack);
			    break; 
			case "}" :
				text = endPropertyListSer(text, stack);
			    break;			    
			case "[" :
				text = startArrayList(text, stack);
			    break; 			    
			case "=" :
				text = createValue(text.replaceFirst("=", ""), stack);
			    break; 	
			case " " :
				text = text.substring(1);
			    break; 
			case "," :
				text = text.substring(1);
			    break; 			    
			default:
				text = createKey(text, stack);
				break;
			}
		}
		return null;
	}
	
	public static String startPropertyListSer(String text, Deque<Object> stack) {
		PropertyListSerializable pSer = new PropertyListSerializable();
		stack.push(pSer);
		text = text.substring(1);
		return text;
	}
	
	public static String endPropertyListSer(String text, Deque<Object> stack) {
		if(!PropertyListSerializable.class.isInstance(stack.pop())) {
			Object value = stack.pop();
			String key   = (String)stack.pop();
			((PropertyListSerializable)stack.peek()).addProperty(key, value);
		}
		text = text.substring(1);
		return text;
	}
//===============================================
	public static void toXml(Object obj, PropertyList _properties) throws Exception {
		
//		InputStream inputStream = (InputStream) _properties.getProperty(INPUT_STREAM);
		File xmlFile = (File)_properties.getProperty(XML_FILE);
		
		//properties -> uso futuro
        JAXBContext jc = JAXBContext.newInstance(obj.getClass());

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        marshaller.marshal(obj, System.out);

        marshaller.marshal(obj, new PrintWriter(xmlFile));
//        marshaller.marshal(obj, new PrintWriter(System.out));
        marshaller.marshal(obj, new PrintWriter(System.err));
        
        Object copy = (Object) readObjectAsXmlFrom(new FileReader(xmlFile.getAbsolutePath()), obj.getClass());

  	    System.out.println("=================>> \n\n\n");
  	    marshaller.marshal(copy, new PrintWriter(System.out));
  	    
  	    System.err.println("*************************** " + obj.getClass().getName());
  	    System.err.println(obj);
  	    System.err.println("=========================== COPY");
  	    System.err.println(copy); 
	}	
//===============================================
	public static void xml(Object obj) throws Exception {
		//properties -> uso futuro
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
    
    public static <T> T readObjectAsXmlFrom(Reader reader, Class<T> c) throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(c);

        XMLStreamReader xmlReader =
          XMLInputFactory.newInstance().createXMLStreamReader(reader);

        Unmarshaller xmlInterpreter = jaxb.createUnmarshaller();

        return xmlInterpreter.unmarshal(xmlReader, c).getValue();
      }	
    
    private static <T> T readObjectAsXmlFrom(InputStream _inputStream, Class<T> c) throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(c);

        XMLStreamReader xmlReader =
          XMLInputFactory.newInstance().createXMLStreamReader(_inputStream);

        Unmarshaller xmlInterpreter = jaxb.createUnmarshaller();

        return xmlInterpreter.unmarshal(xmlReader, c).getValue();
      }
    
    public static <T> T readObjectAsJaxbXml(InputStream _inputStream, Class<T> c) throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(c);

        XMLStreamReader xmlReader =
          XMLInputFactory.newInstance().createXMLStreamReader(_inputStream);

        Unmarshaller xmlInterpreter = jaxb.createUnmarshaller();

        return xmlInterpreter.unmarshal(xmlReader, c).getValue();
      }      
//===============================================	
	
	public static String startArrayList(String text, Deque<Object> stack) {
		ArrayList ar = new ArrayList();
		stack.push(ar);
		text = text.substring(1);
		return text;
	}
	
	public static String getInputStreamHash(InputStream is) throws IOException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
//			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
//		InputStream is = new FileInputStream(file);
		byte[] buf = new byte[1024];
		try {
		  is = new DigestInputStream(is, md);
		  // read stream to EOF as normal...
		  while(is.read(buf) > 0);
		}
		finally {
		  is.close();
		}
		byte[] digest = md.digest();
//		return new String(digest); binary format não é transmitido via http
		return Base64.getEncoder().encodeToString(digest);
	}
	
	public static String getBuitinCompilationUnit(Symbol_New _sym) {
		if(_sym.getEnclosingScope().getEnclosingScope() == null) {
			if(!_sym.getProperties().hasProperty("CATEGORY", "APPLICATION")) {
				return (String)_sym.getProperties().getProperty("LANGUAGE") + "." 
					    + (String)_sym.getProperties().getProperty("CATEGORY_TYPE") + "." 
						+ _sym.getName();
			}
			else {
				return _sym.getName();
			}
		}
		else {
			return getBuitinCompilationUnit((Symbol_New)_sym.getEnclosingScope()) + "." +_sym.getName();
		}
	}
	

	
	public static String endArrayList(String text, Deque<Object> stack) {
		ArrayList ar = new ArrayList();
		stack.push(ar);
		text = text.substring(1);
		return text;
	}	
	
	public static String createKey(String text, Deque<Object> stack) {
		String key = text.split("=")[0];
		stack.push(key);
		text = text.substring(text.indexOf("="));
		return text;
	}

	public static String createValue(String text, Deque<Object> stack) {
		Object value = jaxbAdapterString(text);
		return null; // não é null  arrumar
	}
	
	public static InputStream getInputStream(URL _urlName) throws IOException {
        URLConnection site = null;
			site = _urlName.openConnection();

        InputStream is = null;;
			is = site.getInputStream();

		return is;
	}
	
	public static void main(String[] args) {
		
//		jaxbAdapterString("{REPOSITORY_PROPERTY03=s3, REPOSITORY_PROPERTY04=s4, FORM=[{DT_CREATED=13 ABR 2018, NOME=Form01}, {DT_CREATED=01 JAN 2018, NOME=Form02}], ENDERECO={NUMERO=125, AVENIDA=AV. 25 DE MARÇO}}");
		
		String  in  = "C:\\workspace\\workspace_desenv_java8\\sybase\\antlr4.transactSql\\input\\PR_VBP_RMPET001_G09156.QRY";
		String  out = "C:\\workspace\\workspace_desenv_java8\\util\\br.com.bicam.util\\src\\input\\PR_VBP_RMPET001_G09156.HTML";

		PropertyList properties= new PropertyList();
//		properties.addProperty("START_LINE_MARK", 69);
//		properties.addProperty("END_LINE_MARK", 78);
		
//		File htmlFile = BicamSystem.toHtml(in, out,properties);
		File htmlFile = BicamSystem.toHtml(in, out);
	}
}
