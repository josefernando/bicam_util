package br.com.bicam.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputSourceProcedure {
//	private final String VBP = "VBP";
	
	String inputsrcType;
	
	List<File> orderedFileInputs;

	private enum fileExtension {
		QRY
	}
	
	private enum InputSourceType {
		FILE
	}	
	
	File inFile;
	String currentModuleFileDir;
	String currentModuleFile;

	HashMap<String, List<Procedure>> moduleFileDirList;            // <vbpFileName,modulefle (frm,bas or cls)>
    boolean inVbpDir = false;
	
	public InputSourceProcedure(File _file){
		inputsrcType  = InputSourceType.FILE.toString();
		this.inFile = _file;
		moduleFileDirList = new HashMap<String,List<Procedure>>();
		run(_file);
	}
	
	public InputSourceProcedure(List<File> listOfFile){
		orderedFileInputs  = listOfFile;
		moduleFileDirList = new HashMap<String,List<Procedure>>();
		File primaryFile = orderedFileInputs.get(0);
		run(primaryFile);
	}
	
	private void run(File _file) {
			for(File forFile : _file.listFiles()) {
				if(forFile.isDirectory()) run(forFile);
				else {
					for(fileExtension ext : fileExtension.values()){
						if(forFile.getName().toUpperCase().endsWith(ext.toString())) 
							includeFileInInputSource(forFile);
					}				
				}
			}
	}
	
	private void includeFileInInputSource(File _winnerFile) {
		String dbName = getDbName(_winnerFile);
		System.err.format("%nDB NAME....: %s%n", dbName);
		String procName = getProcName(_winnerFile);
		System.err.format("PROC NAME..: %s%n", procName);
		System.err.format("FILE NAME..: %s%n", _winnerFile);
		
		String fileName = null;
		try {
			fileName = _winnerFile.getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		if(dbName == null) {
			try {
				throw new Exception();
			} catch (Exception e) {
				System.err.format("*** ERROR: 'USE DB' MISSING IN FILE '%s' %n", fileName);
			}
			return;
		}
		
		getDbObjects(_winnerFile);

	}
	
	public File getFile(String _procName) {
		return moduleFileDirList.get(_procName).get(0).getFile();
	}
	
	private String getProcName(File _winnerFile) {
		String procName = null;
		try {
			String fileName = _winnerFile.getCanonicalPath();
			//=============== TUTORIAL  REGEX ==========================
			// "CREATE PROC PROCXXXX", identifica PROCXXXX como procName
			// ^ começo de palavra
			// \\s* 0 mais de 0 espaços
			// (?i) próxima palavra caseinsensitive
			// \\b  match palavra
			// ?: não inclui em group matched
			// | alternative
			// \\w match letra a-z,A-Z,_
			// \\d match número 0-9
			// .* match 0 ou mais de 0 qualquer caracter
			String REGEX = "^\\s*(?i)CREATE\\s+\\b(?:(?i)PROCEDURE|(?i)PROC)\\b\\s+([\\d\\w]+).*";
			// _file =>
			// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
			Pattern p = Pattern.compile(REGEX);
			BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				Matcher m = p.matcher(line);
				if ((m.matches())) {
					return procName = m.group(1);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return procName;
	}	
	
	private String getDbName(File _winnerFile) {
		String dbname = null;
		try {
			String fileName = _winnerFile.getCanonicalPath();
			// "use dbxxxxx", identifica dbxxxxx como group
			String REGEX = "^\\s*(?i)USE\\s+([\\d\\w]+).*";
			// _file =>
			// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
			Pattern p = Pattern.compile(REGEX);
			BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				Matcher m = p.matcher(line);
				if ((m.matches())) {
					return dbname = m.group(1);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dbname;
	}
	
	private void getDbObjects(File _winnerFile) {
		String dbName = null;
		String procName = null;

		try {
			String fileName = _winnerFile.getCanonicalPath();
			// "use dbxxxxx", identifica dbxxxxx como group
			String REGEX_DB   = "^\\s*(?i)USE\\s+([\\d\\w]+).*";
			String REGEX_PROC = "^\\s*(?i)CREATE\\s+\\b(?:(?i)PROCEDURE|(?i)PROC)\\b\\s+([\\d\\w]+).*";

			// _file =>
			// "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
			Pattern pDB = Pattern.compile(REGEX_DB);
			Pattern pPROC = Pattern.compile(REGEX_PROC);
			
			BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				Matcher mDB = pDB.matcher(line);
				if ((mDB.matches())) {
					dbName = mDB.group(1);
				}
				Matcher mPROC = pPROC.matcher(line);
				if ((mPROC.matches())) {
					procName = mPROC.group(1);
					Procedure procedure = new Procedure(procName);
					procedure.setDbName(dbName);
					procedure.setFile(_winnerFile);
					List<Procedure> list = moduleFileDirList.get(procedure.getName());
					if(list == null) {
						list = new ArrayList<Procedure>();
						moduleFileDirList.put(procedure.getName(), list);
					}
					boolean hasProcedure = false;
					for(Procedure forProc : list) {
						if(forProc.equals(procedure)) {
							hasProcedure = true;
							System.err.format("%n*** WARNING DUPLICATE PROCEDURES:%n" +
							                  " => INCLUDED:  %s%n" +
									          " => DISCARTED: %s%n", forProc,procedure);
						}
					}
					if(!hasProcedure)	list.add(procedure);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public String getFirstModuleDir(){
		ArrayList<String> moduleDirList = new ArrayList<String>();
		moduleDirList.addAll(moduleFileDirList.keySet());
		if(!moduleDirList.isEmpty()) 	{
			List<Procedure> list = moduleFileDirList.get(moduleDirList.get(0));
			if(list.size() > 1) {
				System.err.format("*** WARNING: CHOSEN 1 OF %d PROCEDURES: %s IN FILE %s%n",
						list.size(), list.get(0).getName(), list.get(0));
			}
			currentModuleFileDir = moduleDirList.get(0);
		}
		else currentModuleFileDir = null;
		return currentModuleFileDir;
	}
	
	public boolean hasNextModuleDir(){
		ArrayList<String> moduleDirList = new ArrayList<String>();
		moduleDirList.addAll(moduleFileDirList.keySet());
		
		for(int i=0;i < moduleDirList.size() - 1;i++){
			if(moduleDirList.get(i).equals(currentModuleFileDir)){
				if(i == moduleDirList.size() - 1) return false;
				else return true;		
			}
		}
		return false;
	}
	
	public String getNextModuleDir(){
		ArrayList<String> moduleDirList = new ArrayList<String>();
		moduleDirList.addAll(moduleFileDirList.keySet());
		if(moduleFileDirList == null) return null; //getFirstModuleDir();
		for(int i=0;i < moduleDirList.size() - 1;i++){
			if(moduleDirList.get(i).equals(currentModuleFileDir)){
				if(i == moduleDirList.size() - 1) // currentModuleFileDir é o último
					currentModuleFileDir = null;
				else {
					currentModuleFileDir = moduleDirList.get(i+1);
				}
				break;
			}
		}
		return currentModuleFileDir;
	}	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("MODULE file list " + moduleFileDirList + System.lineSeparator());
		return sb.toString();
	}

	public static void main(String[] args) throws Exception{
    	File f = new File("C:\\workspace\\workspace_desenv_java8\\sybase\\antlr4.transactSql\\input");
    	
    	InputSourceProcedure appSource = new InputSourceProcedure(f);
    	System.err.println(appSource.toString());
    	
    	System.err.println(appSource.getFirstModuleDir());
    	while(appSource.hasNextModuleDir()) {
        	System.err.println(appSource.getNextModuleDir());
    	}
	}      	
}

 class Procedure{
	 String name;
	 String dbName;
	 File   file;
	 
	 Procedure(String _name){
		 this.name = _name.toUpperCase();
	 }
	 
	 protected String getName() {
		 return this.name;
	 }
	 
	 protected String getDbName() {
		 return this.dbName;
	 }
	 
	 protected void setDbName(String _dbName) {
		 this.dbName = _dbName.toUpperCase();
	 }	
	 
	 protected File getFile() {
		 return this.file;
	 }
	 
	 protected void setFile(File _file) {
		 this.file = _file;
	 }
	 
	 public String toString() {
		 StringBuffer sb = new StringBuffer();
		 sb.append("Name=" + getName() + ",");
		 sb.append("DbName=" + getDbName() + ",");
		 try {
			sb.append("FileName=" + getFile().getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return sb.toString();
	 }
	 
	public boolean equals(Procedure other){
		if(!this.name.equals(other.name)) return false;
		if(!this.dbName.equals(other.dbName)) return false;
		return true;
	}
 }