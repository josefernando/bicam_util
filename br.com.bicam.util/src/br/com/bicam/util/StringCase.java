package br.com.bicam.util;

import java.util.HashSet;
import java.util.LinkedHashMap;

public class StringCase {
	String realCase; // original case
	boolean caseSensitive;
	
	public StringCase(String _string, boolean _caseSensitive){
		if(_string == null) {
			try {
				throw new Exception();
			} catch (Exception e){
				System.err.println("*** Error: Name cannot be null.");
				e.printStackTrace();
				System.exit(1);
			}
		}
		this.realCase = _string;
		this.caseSensitive = _caseSensitive;
	}
	
	public String getRealCase(){
		return realCase;
	}
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	// Overrides hashCode method
	public int hashCode(){
		return (caseSensitive == true 
				? getRealCase()
						.hashCode() 
						: getRealCase()
						.toUpperCase()
						.hashCode());
	}
	
	// Overrides equals method
	public boolean equals(Object _another){
		StringCase another = (StringCase) _another;
		return (caseSensitive == true ? getRealCase().equals(another.getRealCase()) : getRealCase().equalsIgnoreCase(another.getRealCase()));
	}
	
	public String toString(){
		return realCase;
	}
	
	// unit test
	public static void main (String[] args){
        if (args.length < 3) throw new IllegalArgumentException("# parameters invalid: <String> <String> <0 or 1> ");
//        boolean  sensitive = (args[2].equalsIgnoreCase("0") ? false : true);
        boolean sensitive = true;
        StringCase bc1 = new StringCase(args[0], sensitive);
        StringCase bc2 = new StringCase(args[1], sensitive);
        LinkedHashMap<StringCase, String>  lista = new LinkedHashMap<StringCase, String>();
        HashSet<StringCase> set = new HashSet<StringCase>();
        lista.put(bc1, bc1.toString());
        lista.put(bc2, bc2.toString());
        set.add(bc1);

        set.add(bc2);
        System.out.println(bc1.equals(bc2));
        System.out.println(set);
	}
}