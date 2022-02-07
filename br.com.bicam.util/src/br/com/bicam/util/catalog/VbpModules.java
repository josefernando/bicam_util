package br.com.bicam.util.catalog;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.bicam.parser.visualbasic6.VbpBaseListener;
import br.com.bicam.parser.visualbasic6.VbpParser;

public class VbpModules extends VbpBaseListener{
	Set<String> moduleList;
	
	final String[] modules = new String[] {"CLASS", "FORM", "MODULE"};
	
	public VbpModules() {
		this.moduleList = new HashSet<String>();
	}
	
	@Override 
	public void enterPropriety(@NotNull VbpParser.ProprietyContext ctx) {
		for(String win : modules) {
			if(win.toUpperCase().equalsIgnoreCase(ctx.propertyKey.getText()))
				moduleList.add(ctx.propertyValue.getText());
		}
	}
	
	public List<String> getModuleList() {
		return new ArrayList<String>(moduleList);
	}
}