package br.com.bicam.util;

import java.util.LinkedHashMap;

public class StringOptionalCase {
	String realCase; // original case
	boolean caseSensitive;
	
	public StringOptionalCase(String _string, boolean _caseSensitive){
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
	
	public int hashCode(){
		return (caseSensitive == true 
				? getRealCase()
						.hashCode() 
						: getRealCase()
						.toUpperCase()
						.hashCode());
	}
	
	public boolean equals(Object _string){
		if(!(_string instanceof StringOptionalCase)) return false;
		StringOptionalCase another = (StringOptionalCase)_string;
//		return (caseSensitive == true ? getRealCase().equals(another.toString()) : getRealCase().equalsIgnoreCase(another.toString()));
		return (caseSensitive == true ? getRealCase().equals(another.getRealCase()) : getRealCase().equalsIgnoreCase(another.getRealCase()));
	}
	
	public String toString(){
//		return realCase + "=>" + (caseSensitive==true ? realCase :  realCase.toUpperCase());
		return realCase;
	}
	
	// unit test
	public static void main (String[] args){
        if (args.length < 3) throw new IllegalArgumentException("# parameters invalid: <String> <String> <0 or 1> ");
        boolean  sensitive = (args[2].equalsIgnoreCase("0") ? false : true);
        StringOptionalCase bc1 = new StringOptionalCase(args[0], sensitive);
        StringOptionalCase bc2 = new StringOptionalCase(args[1], sensitive);
        LinkedHashMap<StringOptionalCase, String>  lista = new LinkedHashMap<StringOptionalCase, String>();
        lista.put(bc1, bc1.toString());
        System.out.println(bc1.equals(bc2));
        System.out.println(lista.get(bc2));
	}
}