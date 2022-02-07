package br.com.bicam.util.catalog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.sql.sybase.SqlTransactSybaseBaseListener;
import br.com.bicam.parser.sql.sybase.SqlTransactSybaseParser;
import br.com.bicam.util.BicamSystem;
import br.com.bicam.util.Location;
import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.SymbolTable_New;

public class DatabaseAccess extends SqlTransactSybaseBaseListener {
	
	Map<String,Set<Location>> tbLocations;
	Map<String, Table> tables;
	SymbolTable_New st;
	String compilationUnitName;
	String fullSqlName;
	
	public DatabaseAccess(SymbolTable_New _st) {
		tables = new HashMap<String,Table>();
		tbLocations = new HashMap<String,Set<Location>>();
		this.st = _st;
	}
	
	public DatabaseAccess(PropertyList _properties) {
		tables = new HashMap<String,Table>();
		tbLocations = new HashMap<String,Set<Location>>();
		this.st = (SymbolTable_New) _properties.getProperty("SYMBOLTABLE");
		this.compilationUnitName = (String) _properties.getProperty("COMPILATION_UNIT_NAME");
		this.fullSqlName = (String) _properties.getProperty("FULL_SQL_NAME");
	}
	
	@Override 
	public void enterSqlSingleTable(@NotNull SqlTransactSybaseParser.SqlSingleTableContext ctx) {
/*		Set<Location> locations = tbLocations.get(ctx.getText());
		if (locations == null) {
			locations = new HashSet<Location>();
			tbLocations.put(ctx.getText(), locations);
		}
		Location wLocation = new Location(ctx.start.getText(),ctx);
		wLocation.setCompilationUnit(compilationUnitName);
		locations.add(wLocation);
		tables.put(ctx.getText(), new Table(ctx.getText()));*/
		
		String tbName = BicamSystem.sqlNameToFullQualifiedName(ctx.getText(), fullSqlName);
		
		Set<Location> locations = tbLocations.get(tbName);
		if (locations == null) {
			locations = new HashSet<Location>();
			tbLocations.put(tbName, locations);
		}
		Location wLocation = new Location(ctx.start.getText(),ctx);
		wLocation.setCompilationUnit(compilationUnitName);
		locations.add(wLocation);
		tables.put(tbName, new Table(tbName));		
	}
	
/*	public Set<String> getTableList() {
		return tables.keySet();
	}*/
	
	public Map<String,Set<Location>> getTableList(){
		return  tbLocations;
	}
	
	class Table{
		String tbName;
	
		public Table(String _tbName) {
			this.tbName = _tbName;
		}
		
		public String getTbName() {
			return tbName;
		}
		
		public String toString() {
			return tbName;
		}
		
		public boolean equals(Object _obj) {
			if(!(_obj instanceof Table)) return false;
			Table other = (Table)_obj;
			if(this.tbName.equals(other.tbName)) return true;
			return false;
		}
	}
}
