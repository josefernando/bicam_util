package br.com.bicam.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Position {
	public Integer offSet;
	public Integer length;
	public Integer line;
	
	public Position(Integer offset, Integer length, Integer line){
		setOffSet(offset);
		setLength(length);
		setLine(line);
	}
	
	public Position(ParserRuleContext _node) {
		setOffSet(_node.getStart().getStartIndex());
		setLength(_node.getStart().getStopIndex() - getOffSet() + 1);
		setLine(_node.getStart().getLine());
	}
	
	public Position(Token _node) {
		setOffSet(_node.getStartIndex());
		setLength(_node.getStopIndex() - getOffSet() + 1);
		setLine(_node.getLine());
	}	
	
	public Integer getOffSet() {
		return offSet;
	}
	public void setOffSet(int offSet) {
		this.offSet = offSet;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public Integer getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	
	public String toString(){
		return "offSet=" + Integer.toString(getOffSet())
			  +",length=" + Integer.toString(getLength())
			  +",line=" + Integer.toString(getLine());
	}
}