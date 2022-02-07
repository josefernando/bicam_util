package br.com.bicam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class KeywordLanguageBase implements KeywordLanguage{
	
	
	//Carregado em <Language>Lexer.g4 => VB6Lexer.g4 
	HashMap<String,Integer> keywordsLanguage;
	HashMap<String,Integer> upperCaseKeywordsLanguage;
	HashMap<String,String> parameters;

	List<String> keywords;
	
	PropertyList properties;
	
	
	//keywordsOption1: Usado de acordo com a necessidade da Linguagem
	// Ex.: Em Sybase é usado para os datatypes
	Set<String> keywordsOption1;
	Set<String> firstKeywordOfStatementLanguage;
	Set<String> statementKeywordsLanguage;
	Set<String> LanguageFunctionKeywordsLanguage;
	Set<String> propertyKeywordsLanguage;
	Set<String> methodKeywords;
	Set<String> languageObjectKeywordsLanguage;
	Set<String> dataTypeKeywordsLanguage;
	Set<String> classConstantLanguage;
	Set<String> controlLanguage;
	Set<String> languageLib;
	Set<String> database;
	Set<String> userDatabase;
	Set<String> systemClass;
	Set<String> rootSystemClass;
	Set<String> classMember;
	Set<String> userClass;
	Set<String> userMethodAndProperty;
	
	Map<String,Set<String>> keywordByRule;
	
	public KeywordLanguageBase() {
//==================NV==========================
		 keywords = new ArrayList<String>();
//==============================================		
		 parameters = new HashMap<String,String>();
		 
		 properties = new PropertyList();
		 
		 keywordsLanguage = new HashMap<String,Integer>();
		 statementKeywordsLanguage = new HashSet<String>();		 
		 upperCaseKeywordsLanguage = new HashMap<String,Integer>();
		 keywordsOption1 = new HashSet<String>();
		 LanguageFunctionKeywordsLanguage = new HashSet<String>();		 
		 propertyKeywordsLanguage = new HashSet<String>();
		 methodKeywords = new HashSet<String>();		 
		 dataTypeKeywordsLanguage = new HashSet<String>();
		 languageObjectKeywordsLanguage = new HashSet<String>();
		 firstKeywordOfStatementLanguage = new HashSet<String>();
		 classConstantLanguage = new HashSet<String>();
		 languageLib = new HashSet<String>();
		 database = new HashSet<String>();
		 userDatabase = new HashSet<String>();

		 controlLanguage = new HashSet<String>();
		 classMember = new HashSet<String>();
		 systemClass = new HashSet<String>();
		 rootSystemClass = new HashSet<String>();
		 userClass = new HashSet<String>();
		 userMethodAndProperty = new HashSet<String>();
		 keywordByRule = new HashMap<String,Set<String>>();
		 this.setStatementKeywordsLanguage();
	}

	public Integer getKeywordType(String _key) {
		return keywordsLanguage.get(_key);
	}

	public String getParameter(String _key) {
		return parameters.get(_key);
	}

	public void setParameters() {
	}

	public Integer getUpperCaseKeywordType(String _key) {
		return upperCaseKeywordsLanguage.get(_key.toUpperCase());
	}

	@Override
	public boolean isKeywordOption1(String _key) {
		if (keywordsOption1.contains(_key) == false){
			return keywordsOption1.contains(_key.toUpperCase());
		}
		return keywordsOption1.contains(_key);
	}
	
	@Override
	public boolean isFunctionKeywordsLanguage(String _key) {
		if (LanguageFunctionKeywordsLanguage.contains(_key) == false){
			return LanguageFunctionKeywordsLanguage.contains(_key.toUpperCase());
		}
		return LanguageFunctionKeywordsLanguage.contains(_key);
	}
	
	@Override
	public boolean isPropertyKeywordsLanguage(String _key) {
		if (propertyKeywordsLanguage.contains(_key) == false){
			return propertyKeywordsLanguage.contains(_key.toUpperCase());
		}
		return propertyKeywordsLanguage.contains(_key);
	}	
	
	public boolean isStatementKeywordsLanguage(String key){
		return statementKeywordsLanguage.contains(key);
	}
	
	public void setTokenNames(String[] _tokenNames){
		for(int i=0; i < _tokenNames.length; i++){
			keywordsLanguage.put(_tokenNames[i], i);
			upperCaseKeywordsLanguage.put(_tokenNames[i].toUpperCase(), i);
		}
	}
	 public void printTokens(){
		 System.err.println(keywordsLanguage);
		 System.err.println("\n" + upperCaseKeywordsLanguage);
	 }
	 
	 public Set<String> getKeywords(String _type){
		 Set<String> ret = null;
		 switch (_type.toUpperCase()) {
			 case "LANGUAGE_FUNCTION":
				 ret = LanguageFunctionKeywordsLanguage;
				 return ret;
			 case "PROPERTY":
				 ret = propertyKeywordsLanguage;
				 return ret;			 
			 case "DATA_TYPE": //User Interface é virtual type
				 ret = dataTypeKeywordsLanguage;
					 return ret;
			 case "LANGUAGE_OBJECT": //User Interface é virtual type
				 ret = languageObjectKeywordsLanguage;
				 return ret;
			 case "FIRSTKEYWORDOFSTATEMENT": //User Interface é virtual type
				 ret = firstKeywordOfStatementLanguage;			 
				 return ret;
			 case "STATEMENTKEYWORDS": //User Interface é virtual type
				 ret = statementKeywordsLanguage;			 
				 return ret;				 
			 case "CLASSCONSTANT": //User Interface é virtual type
				 ret = classConstantLanguage;
				 return ret;
			 case "LANGUAGE_LIB": //User Interface é virtual type
				 ret = languageLib;		 
				 return ret;
			 case "CONTROL": //User Interface é virtual type
				 ret = controlLanguage;	
				 return ret;
			 case "CLASSMEMBER": //User Interface é virtual type
				 ret = classMember;
				 return ret;
			 case "METHOD": //User Interface é virtual type
				 ret = methodKeywords;
				 return ret;
			 case "USERCLASS": //User Interface é virtual type
				 ret = userClass;
				 return ret;			 
			 case "USERMETHOD": //User Interface é virtual type
				 ret = userMethodAndProperty;
				 return ret;		 
			 case "DATABASE": //User Interface é virtual type
				 ret = database;
				 return ret;
			 case "USERDATABASE": //User Interface é virtual type
				 ret = userDatabase;
				 return ret;			 				
			default:
				return ret;
		}
	 }

	public boolean isIdentifierKeyword(String _rulename, String _identifier) {
		if(!keywordByRule.isEmpty() && keywordByRule.get(_rulename) != null){
			return keywordByRule.get(_rulename).contains(_identifier);
		}
		else {
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public Map<String,String> getParameters(){
		return parameters;
	}
	
	@Override
	public boolean isKeyword(String _key) {
		return keywords.contains(_key);
	}	
	
	public PropertyList getProperties() {
		return properties;
	}
}