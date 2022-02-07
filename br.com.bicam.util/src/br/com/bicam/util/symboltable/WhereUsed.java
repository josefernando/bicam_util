package br.com.bicam.util.symboltable;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.PropertyList;

public class WhereUsed {
	
	Set<ParserRuleContext> used; 
	
	PropertyList properties;
	
	public WhereUsed(){
		used = new HashSet<ParserRuleContext>();
		properties = new PropertyList();
	}
	
	public void add(ParserRuleContext _ctx){
		used.add(_ctx);
	}
	
/*	public void add(ParserRuleContext _ctx, SymbolTable_b _st){
		used.add(_ctx);
		if(_st != null){
			String compUnitName = _st.getCompilarionUnitSymbol(_ctx).getName();
			List<Position> p = (List)usedPosition.getProperty(compUnitName);
			if(p == null){
				p = new ArrayList<Position>();
				usedPosition.getProperties().put(compUnitName,p);
			}
			p.add(new Position(_ctx.start.getStartIndex(), _ctx.start.getLine()));
		}
	}*/
	
	public Set<ParserRuleContext> used(){
		return used;
	}
	
	public void setProperty(String key, Object value){
		 properties.getProperties().put(key, value);
	}	
	
	public String getProperty(String key, Object value){
		return (String)properties.getProperty(key);
	}
	
	public String toString(){
		return properties.toString();
	}
}

class Position {
	int compilationUnitPosition;
	int compilationUnitLine;
	
	public Position(int _compUnitPosition, int _compUnitLine){
		this.compilationUnitPosition = _compUnitPosition;
		this.compilationUnitLine = _compUnitLine;
	}
	
	public int getCompilationUnitPosition(){
		return compilationUnitPosition;
	}
	
	public int getCompilationUnitLine(){
		return compilationUnitLine;
	}
}