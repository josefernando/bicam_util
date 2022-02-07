package br.com.bicam.util.catalog;

import java.util.HashSet;
import java.util.Stack;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import br.com.bicam.util.symboltable.SymbolTable_New;

public abstract class UIToHtml{

   protected STGroup templates;
   protected Stack<ST> hierarchyChainST;
   protected ST rootST;
   protected ST htmlDocST;
   protected ST htmlTagST;
   protected ST styleTagST;
   protected ST attributeST;
   protected ST contentST;
   protected ST auxST;

   protected SymbolTable_New symTable;
   
   HashSet<String> htmlFiles;
   
   public UIToHtml(String groupTemplates, SymbolTable_New _symTable){
	   	 this.symTable = _symTable;
	     this.templates = new STGroupFile(groupTemplates);
		 this.hierarchyChainST = new Stack<ST>();
		 this.htmlFiles = new HashSet();
	     run();
   }
   
   abstract void run();
   
   protected void pushST(ST stack){
	   hierarchyChainST.add(stack); // ST[3]={stack,attribute,content}
   }
   
   protected ST popST(){
	   return hierarchyChainST.pop();
   }
   
   protected ST peekST(){
	   return hierarchyChainST.peek();
   }
   
   protected void clearST(){
		 hierarchyChainST.clear();
   }    
}