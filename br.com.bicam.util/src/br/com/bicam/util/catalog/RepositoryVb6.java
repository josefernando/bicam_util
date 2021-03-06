package br.com.bicam.util.catalog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.bicam.util.PropertyList;

public class RepositoryVb6 implements IRepository{
	private final String VBP = "VBP";
	
	String srcType;  

	private enum VbExtension {
		FRM, CLS, BAS
	}
	
	private enum InputSourceType {
		FILE
	}	
	
	File inFile;
	Deque<String> visitedDir;
	String currentVbpDir;
	String currentModuleFileDir;
	String currentModuleFile;
	List<File> orderedFileInputs;
	int currentFileInput;


	HashMap<String, List<File>> moduleFileDirList;            // <vbpFileName,modulefle (frm,bas or cls)>
	HashMap<String, File> vbpFileList;
    boolean inVbpDir = false;
	
	public RepositoryVb6(File _file){
		run(_file);
	}
	
	public RepositoryVb6(List<File> listOfFile){
		orderedFileInputs  = listOfFile;
		File primaryFile = orderedFileInputs.get(0);
		run(primaryFile);
	}
	
	private void run(File _file) {
		srcType  = InputSourceType.FILE.toString();
		this.inFile = _file;
		visitedDir = new ArrayDeque<String>();
		vbpFileList = new HashMap<String,File>();
		moduleFileDirList = new HashMap<String,List<File>>();
		findVbpFile(_file);
		findModuleFile(_file);
		System.err.println(toString());		
	}
	
	private String getFileName(File _file){
		try {
			return _file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void findVbpFile(File _inputFile){
		if(_inputFile.isDirectory()){
			visitedDir.push(getFileName(_inputFile));
			for(File file : _inputFile.listFiles()){
				findVbpFile(file);
			}
			visitedDir.pop();
		}
		else {
			if(getFileName(_inputFile).toUpperCase().endsWith(VBP)){
				if(!visitedDir.isEmpty()){
					vbpFileList.put(visitedDir.peek(), _inputFile);
				}
				else { // recupera dir do nome do arquivo
					vbpFileList.put(getFileName(getDirectoryOfFile(_inputFile)), _inputFile);
				}
			}
		}
	}
	
	private void findModuleFile(File _inputFile){
		if(_inputFile.isDirectory()){
			visitedDir.push(getFileName(_inputFile));
			for(File file : _inputFile.listFiles()){
				findModuleFile(file);
			}
			visitedDir.pop();
		}
		else {
			boolean isModuleExtension = false;
			for(VbExtension ext : VbExtension.values()){
				if(_inputFile.getName().toUpperCase().endsWith(ext.toString())) 
					isModuleExtension = true;
			}
			if(isModuleExtension){
				if(!visitedDir.isEmpty()){
					List<File> list = moduleFileDirList.get(visitedDir.peek());
					if(list == null){
						list = new ArrayList<File>();
						moduleFileDirList.put(visitedDir.peek(), list);
					}
					list.add(_inputFile);
				}
				else {
					List<File> list = moduleFileDirList.get(null);
					if(list == null){
						list = new ArrayList<File>();
						moduleFileDirList.put(getFileName(_inputFile), list);
					}
					list.add(_inputFile);
				}
			}
		}
	}
	
   public void findFile(PropertyList _properties) {
	   if(orderedFileInputs.size()==1) return; // N?o h? outros arquivos a procurar
	   for(int i=1; i < orderedFileInputs.size(); i++) {
			_properties.addProperty("FILE", orderedFileInputs.get(i));
			if(findModuleFileByVbName(_properties)) return;
	   }
   }
	
	private boolean findModuleFileByVbName(PropertyList _properties){
		File _inputFile = (File)_properties.getProperty("FILE");
		String vbName = (String)_properties.getProperty("FILTER");
		if(_inputFile.isDirectory()) {
			for(File forFile : _inputFile.listFiles()) {
				_properties.addProperty("FILE", forFile);
				if(findModuleFileByVbName(_properties)) return true;
			}
		}
		try {
			if(vbName.equalsIgnoreCase(getVbName(_inputFile.getCanonicalPath()))) {
				_properties.addProperty("RETURN", _inputFile);
				return true;
			}
			_properties.addProperty("RETURN", null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
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
        		  in.close();
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
	
	private File getDirectoryOfFile(File _file){
		return _file.getParentFile();
	}	
	
	public String getFirstMetadataFile(){
		File dirVbp = new File(getFirstMetadataDir());
		if(dirVbp != null){
			for(File file : dirVbp.listFiles()){
				if(file.getName().toUpperCase().endsWith("VBP"))
					return getFileName(file);
			}
		}
		return null;
	}
	
	public String getFirstMetadataDir(){
		ArrayList<String> vbpList = new ArrayList<String>();
		vbpList.addAll(vbpFileList.keySet());
		if(!vbpList.isEmpty()) 	currentVbpDir = vbpList.get(0);
		else currentVbpDir = null;
		return currentVbpDir;
	}
	
	public String getNextVbpDir(){
		ArrayList<String> vbpList = new ArrayList<String>();
		vbpList.addAll(vbpFileList.keySet());
		if(currentVbpDir == null) return getFirstMetadataDir();
		for(int i=0;i < vbpList.size() - 1;i++){
			if(vbpList.get(i).equals(currentVbpDir)){
				if(i == vbpList.size() - 1)
					currentVbpDir = null;
				else {
					currentVbpDir = vbpList.get(i+1);
				}
				break;
			}
		}
		return currentVbpDir;
	}	
	
	public boolean hasNextVbpDir(){
		ArrayList<String> vbpList = new ArrayList<String>();
		vbpList.addAll(vbpFileList.keySet());
		if(currentVbpDir == null) return false;
		
		for(int i=0;i < vbpList.size() - 1;i++){
			if(vbpList.get(i).equals(currentVbpDir)){
				if(i == vbpList.size() - 1) return false;
				else return true;		
			}
		}
		return false;
	}
	
	public String getFirstModuleDir(){
		ArrayList<String> moduleDirList = new ArrayList<String>();
		moduleDirList.addAll(moduleFileDirList.keySet());
		if(!moduleDirList.isEmpty()) 	currentModuleFileDir = moduleDirList.get(0);
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
				if(i == moduleDirList.size() - 1) // currentModuleFileDir ? o ?ltimo
					currentModuleFileDir = null;
				else {
					currentModuleFileDir = moduleDirList.get(i+1);
				}
				break;
			}
		}
		return currentModuleFileDir;
	}
	
	public String getFirstModule(File _vbpFile){
		String vbpdir = getFileName(getDirectoryOfFile(_vbpFile));
		currentModuleFileDir = vbpdir;
		
		if(currentModuleFileDir ==  null) {
			currentModuleFileDir = getFirstModuleDir();
		}
		if(currentModuleFileDir == null) return null;
		
		currentModuleFile = null;
		if(moduleFileDirList.get(currentModuleFileDir) == null){
			try{
				throw new Exception("*** ERROR - extension file  \n" 
			            + "FRM, CLS, BAS"
			            + " Not Allowed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for(File file : moduleFileDirList.get(currentModuleFileDir)){
			currentModuleFile = getFileName(file); // First module file
			break; // pega o primeiro e sai do loop
		}
		return currentModuleFile;
	}
	
	public String getFirstModule(){
		if(currentModuleFileDir ==  null) {
			currentModuleFileDir = getFirstModuleDir();
		}
		if(currentModuleFileDir == null) return null;
		
		currentModuleFile = null;
		for(File file : moduleFileDirList.get(currentModuleFileDir)){
			currentModuleFile = getFileName(file); // First module file
			break; // pega o primeiro e sai do loop
		}
		return currentModuleFile;
	}	
	
	public boolean hasNextModule(File _file){
		ArrayList<File> lista = new ArrayList<File>();
		lista.addAll(moduleFileDirList.get(currentModuleFileDir));
		for(int i=0;i < lista.size() - 1;i++){
			if(getFileName(lista.get(i)).equals(currentModuleFile)){
				if(i == lista.size() - 1) {
					return false;
				}
				else return true;
			}
		}
		return false;
	}	
	
	public boolean hasNextModule(){
		ArrayList<File> lista = new ArrayList<File>();
		lista.addAll(moduleFileDirList.get(currentModuleFileDir));
		for(int i=0;i < lista.size() - 1;i++){
			if(getFileName(lista.get(i)).equals(currentModuleFile)){
				if(i == lista.size() - 1) { // ?ltimo module file do diret?rio 
					if(getNextModuleDir() != null) return true;
					else return false;
				}
				else return true;
			}
		}
		return false;
	}
	
	public String getNextModule(){
		ArrayList<File> lista = new ArrayList<File>();
		lista.addAll(moduleFileDirList.get(currentModuleFileDir));
		for(int i=0;i < lista.size() - 1;i++){
			if(getFileName(lista.get(i)).equals(currentModuleFile)){
				if(i == lista.size() -1) { // ?ltimo module file do diret?rio 
					if(getNextModuleDir() != null) {
						currentModuleFile = getFirstModule();
					}
					else currentModuleFile = null;
				}
				else {
					currentModuleFile = getFileName(lista.get(i+1));
				}
				return currentModuleFile;
			}
		}
		return null;
	}
	
	public String getNextModule(File _file){
		ArrayList<File> lista = new ArrayList<File>();
		lista.addAll(moduleFileDirList.get(currentModuleFileDir));
		for(int i=0;i < lista.size() - 1;i++){
			if(getFileName(lista.get(i)).equals(currentModuleFile)){
				if(i == lista.size() -1) { // ?ltimo module file do diret?rio 
					currentModuleFile = null;
				}
				else {
					currentModuleFile = getFileName(lista.get(i+1));
				}
				return currentModuleFile;
			}
		}
		return null;
	}	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("VBP file list " + vbpFileList + System.lineSeparator());
		sb.append("MODULE file list " + moduleFileDirList + System.lineSeparator());
		return sb.toString();
	}

	public static void main(String[] args) throws Exception{
//   	File f = new File("C:\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\examples");
//    	File f = new File("C:\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\examples\\ADOListView");
//    	File f = new File("C:\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\PROJETO01");
//    	File f = new File("C:\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TesteParserDir");
//    	File f = new File("C:\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input");
   	 File f = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TAPU00");
    	
    	IRepository repository = new RepositoryVb6(f);
    	System.err.println("\nFirst VBP dir "+ repository.getFirstMetadataDir().toString());
    	
    	System.err.println("\n\nFirst MODULE file "+ repository.getFirstModule().toString());
    	while(repository.hasNextModule()){
        	System.err.println("Next MODULE file " + repository.getNextModule().toString());
    	}      	
	}
}
