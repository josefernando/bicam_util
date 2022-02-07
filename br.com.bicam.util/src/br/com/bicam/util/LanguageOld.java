package br.com.bicam.util;

import java.util.Set;

public interface LanguageOld {
	public Integer getTokenType(String _name);
	public Set<String> getKeywords(String _type);
	public boolean setTokenNames(String[] _tokenNames, boolean _caseSensitive);
}
