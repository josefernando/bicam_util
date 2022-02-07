package br.com.bicam.util;

import java.util.LinkedHashMap;

public class BCString {
	String bcString;
	boolean caseSensitive;
	
	public BCString(String _bcString, boolean _caseSensitive){
		this.bcString = _bcString;
		this.caseSensitive = _caseSensitive;
	}
	
	public String getBcString(){
		return bcString;
	}
	
	public int hashCode(){
		return (caseSensitive == true 
				? getBcString()
						.hashCode() 
						: getBcString()
						.toUpperCase()
						.hashCode());
	}
	
	public boolean equals(Object _bcString){
		if(!(_bcString instanceof BCString)) return false;
		BCString another = (BCString)_bcString;
		return (caseSensitive == true ? getBcString().equals(another.toString()) : getBcString().equalsIgnoreCase(another.toString()));
	}
	
	public String toString(){
		return bcString;
	}
	
	// unit test
	public static void main (String[] args){
        if (args.length < 3) throw new IllegalArgumentException("# parameters invalid: <String> <String> <0 or 1> ");
        boolean  sensitive = (args[2].equalsIgnoreCase("0") ? false : true);
        BCString bc1 = new BCString(args[0], sensitive);
        BCString bc2 = new BCString(args[1], sensitive);
        LinkedHashMap<BCString, String>  lista = new LinkedHashMap<BCString, String>();
        lista.put(bc1, bc1.toString());
        System.out.println(bc1.equals(bc2));
        System.out.println(lista.get(bc2));
	}
}