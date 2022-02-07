package br.com.bicam.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public abstract class Program {
	BufferedReader reader;
	LinkedList<String> code;
	
	HashMap<String,Integer> keywordsLanguage;
	Set<String> statementKeywordsLanguage;
	HashMap<String,Integer> upperCaseKeywordsLanguage;
	
	public Program(InputStream is){
		reader = new BufferedReader(new InputStreamReader(is));
		code = new LinkedList<String>();

		keywordsLanguage = new HashMap<String, Integer>();
		statementKeywordsLanguage = new HashSet<String>();
		upperCaseKeywordsLanguage = new HashMap<String, Integer>();
		this.setStatementKeywordsLanguage();
	}
	
	protected LinkedList<String> reverse(InputStream is) throws IOException{
		reader = new BufferedReader(new InputStreamReader(is));
		code = new LinkedList<String>();
		String line = reader.readLine();
		while(line != null){
			code.addLast(line+System.lineSeparator());
		}
		return code;
	}
	
	private LinkedList<String> reverse() throws IOException{
		String line = reader.readLine();
		while(line != null){
			code.addLast(line+System.lineSeparator());
		}
		return code;
	}	
	
	
	public Integer getKeywordType(String _key) {
		return keywordsLanguage.get(_key);
	}

	public abstract Integer getUpperCaseKeywordType(String _key);
	
	public abstract void setStatementKeywordsLanguage();
	
	public abstract boolean isStatementKeywordsLanguage(String key);
	
	public abstract void setTokenNames(String[] _tokenNames);

	public abstract void printTokens();
}