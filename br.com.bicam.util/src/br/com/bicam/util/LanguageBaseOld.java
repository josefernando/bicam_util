package br.com.bicam.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

abstract public class LanguageBaseOld implements LanguageOld{
	Map<String,Integer> keywords;
	
	public LanguageBaseOld(){
		 keywords = new HashMap<String,Integer>();
	}
	
	 public Integer getTokenType(String _name){
		 return keywords.get(_name);
	 }
	 
	 public Set<String> getKeywords(String _type){
		 Set<String> ret = null;
		 if(_type.equalsIgnoreCase("Keywords"))
			 return ret;
		 return ret;
	 }	 
		
	public boolean setTokenNames(String[] _tokenNames, boolean _caseSensitive){
		if(keywords.isEmpty()){
			if(!_caseSensitive)
			for(int i=0; i < _tokenNames.length; i++){
				keywords.put(_tokenNames[i].toUpperCase(), i);
			}
			else {
				for(int i=0; i < _tokenNames.length; i++){
					keywords.put(_tokenNames[i], i);
				}				
			}
			return true;
		}
		return false;
	}
}
