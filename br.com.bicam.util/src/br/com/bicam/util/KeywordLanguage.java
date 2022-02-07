package br.com.bicam.util;

import java.util.Map;
import java.util.Set;

public interface KeywordLanguage {
	public Integer getKeywordType(String _key);
	public String getParameter(String _key);
	public boolean isKeyword(String _key);
	public Integer getUpperCaseKeywordType(String _key);
	public boolean isKeywordOption1(String _key);	
	public void setTokenNames(String[] _tokenNames);
	public void printTokens(); 
	public boolean isStatementKeywordsLanguage(String key);
	public boolean isFunctionKeywordsLanguage(String key);
	public boolean isPropertyKeywordsLanguage(String key);
	public void setStatementKeywordsLanguage();
	public void setParameters();
	public Map<String,String> getParameters();
	public void setKeywordsOption1();
	public Set<String> getKeywords(String _type);
	public boolean isIdentifierKeyword(String _rulename, String _identifier);
	public boolean isCaseSensitive();
	public PropertyList getProperties();
}
