package br.com.bicam.util.symboltable;

import static br.com.bicam.util.constant.PropertyName.NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.StringOptionalCase;

public class Member {
	private StringOptionalCase name;
	private List<Symbol_New> symbols;
	private int symbolRetrieved = Integer.MAX_VALUE;
	
	public Member( StringOptionalCase _name, Symbol_New symbol) {
		this.name = _name;
		this.symbols = new ArrayList<Symbol_New>();
		symbols.add(symbol);
	}
	
	public String getName() {
		if(name.isCaseSensitive())
			return name.getRealCase();
		else 
			return name.getRealCase().toUpperCase();
	}
	
	public void add(Symbol_New symbol) {
		symbols.add(symbol);
	}
	
	public int size() {
		return symbols.size();
	}
	
	public Collection<Symbol_New> getSymbols(){
		return symbols;
	}
	
	public boolean hasNext() {
		return symbolRetrieved < symbols.size() ? true : false;
	}
	
	// posiciona para percorrer symbolos
	public void findSymbol() {
		if(symbols.size() > 0)
			symbolRetrieved =  0;
		else
			symbolRetrieved = Integer.MAX_VALUE; 
	}
	
	public Symbol_New getFirstSymbol() {
		findSymbol();
		return getNextSymbol();
	}
	
	public Symbol_New getNextSymbol() {
		if (!hasNext()) {
			findSymbol();
			return null;
		}
		return symbols.get(symbolRetrieved++);
	}
	
	// unitary test
	public static void main(String[] args) {
		boolean caseSensitive = false;
		
		ArrayList<Symbol_New> symbols = new ArrayList<Symbol_New>();
		
		PropertyList propertiesa = new PropertyList();
		propertiesa.addProperty(NAME,"a");
		TypeSymbol_New a = new TypeSymbol_New(propertiesa);
		symbols.add(a);
		
		PropertyList propertiesA = new PropertyList();

		propertiesA.addProperty(NAME,"A");
		TypeSymbol_New A = new TypeSymbol_New(propertiesA);
		symbols.add(A);

		PropertyList propertiesB = new PropertyList();
		propertiesB.addProperty(NAME,"B");
		TypeSymbol_New B = new TypeSymbol_New(propertiesB);
		symbols.add(B);
		
		Map<StringOptionalCase,Member> lista = new HashMap<StringOptionalCase,Member>();

		for(Symbol_New s : symbols) {
			Member m = lista.get(new StringOptionalCase(s.getName(),caseSensitive));
			
			if(m == null) {
				System.err.println("Novo member para symbol: " + s.getName());
				StringOptionalCase name = new StringOptionalCase(s.getName(),caseSensitive);
				m = new Member(new StringOptionalCase(s.getName(),caseSensitive),s);
				lista.put(name, m);
			}
			else {
				m.add(s);
				System.err.println("Adicionado member para symbol: " + s.getName());
			}
		}
		
		for(Member mm : lista.values()) {
			System.err.println("Member Name: " + mm.getName());
			
			mm.findSymbol();
			while(mm.hasNext()) {
				System.err.println(" Symbol while Name: " + mm.getNextSymbol().getName());
			}
			
/*			for(Symbol_New s : mm.symbols) {
				System.err.println(" Symbol For Name: " + s.getName());
			}*/
		}
	}
}
