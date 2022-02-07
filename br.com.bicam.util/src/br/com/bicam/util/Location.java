package br.com.bicam.util;

import org.antlr.v4.runtime.ParserRuleContext;

public class Location {
	String  name;
	ParserRuleContext context;
	Integer line;
	Integer positionInLine;
	Integer positionInCompilationUnit;
	String  compilationUnitName;
	Location nextCallableProcedure; 
	
	public Location(String _name, ParserRuleContext ctx) {
		this.context = ctx;
		this.line = ctx.start.getLine();
		this.positionInLine = ctx.start.getCharPositionInLine();
		this.positionInCompilationUnit = ctx.start.getStartIndex();
	}
	
	public String getName() {
		return name;
	}
	
	public void settName(String _name) {
		this.name = _name;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getPositionInLine() {
		return positionInLine;
	}

	public void setPositionInLine(Integer positionInLine) {
		this.positionInLine = positionInLine;
	}

	public Integer getPositionInCompilationUnit() {
		return positionInCompilationUnit;
	}

	public void setPositionInCompilationUnit(Integer positionInCompilationUnit) {
		this.positionInCompilationUnit = positionInCompilationUnit;
	}

	public String getCompilationUnit() {
		return compilationUnitName;
	}

	public void setCompilationUnit(String _compilationUnitName) {
		compilationUnitName = _compilationUnitName;
	}
	
	public ParserRuleContext getContext() {
		return this.context;
	}

	public void setContext(ParserRuleContext _context) {
		this.context = _context;
	}

	public Location getNextCallableProcedure() {
		return nextCallableProcedure;
	}

	public void setNextCallableProcedure(Location nextCallableProcedure) {
		this.nextCallableProcedure = nextCallableProcedure;
	}
	
	public String toString() {
		return "Line: " + line + " Compilation Unit: " + compilationUnitName;
	}
}