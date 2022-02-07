package br.com.bicam.util;

import java.util.ArrayList;
import java.util.List;

public class Target {
	int stmtLocation;
	String stmtText;
	int procLocation;
	String procName;
	String compUnitName;
	List<Target> callers;
	
	
	public Target(int _stmtLocation){
		this.stmtLocation = _stmtLocation;
		callers = new ArrayList<Target>();
	}

	public int getStmtLocation() {
		return stmtLocation;
	}
	
	public void setStmtText(String _text){
		this.stmtText = _text;
	}
	
	public String getStmtText(){
		return stmtText;
	}

	public int getProcLocation() {
		return procLocation;
	}

	public void setProcLocation(int procLocation) {
		this.procLocation = procLocation;
	}

	public String getProcName() {
		return procName;
	}

	public void setProcName(String procName) {
		this.procName = procName;
	}

	public String getCompUnitName() {
		return compUnitName;
	}

	public void setCompUnitName(String compUnitName) {
		this.compUnitName = compUnitName;
	}

	public List<Target> getCallers() {
		return callers;
	}

	public void setCallers(List<Target> callers) {
		this.callers = callers;
	}
}
