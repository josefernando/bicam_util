package br.com.bicam.util;

import org.antlr.v4.runtime.ParserRuleContext;

public class ParserRuleChain {
	
	public static boolean isparent(String _parentRule, ParserRuleContext _ctx){
		boolean ret = false;
		ParserRuleContext prc = _ctx;
		
		while(prc != null){
			String className = prc.getClass().getSimpleName();
			if(className.equals(_parentRule)){
				ret = true;
				break;
			}
			prc = prc.getParent();
		}
		return ret;
	}

}
