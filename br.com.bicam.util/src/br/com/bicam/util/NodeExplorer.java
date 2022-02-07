package br.com.bicam.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import br.com.bicam.util.symboltable.IScope;
import br.com.bicam.util.symboltable.SymbolList;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;

public class NodeExplorer {
	final static String[] sulfixTypeIndicator = new String[]{"&","%","#","!","@","$","\""};
	
    public static boolean hasAncestorClass(RuleContext _ctx, String _className){
    	if(_ctx.parent == null){
    		return false;
    	}
    	if(_ctx.parent.getClass().getSimpleName().equals(_className)){
    		return true;
    	}
    	return hasAncestorClass(_ctx.parent, _className);
    }
    
    public static boolean hasChildClass(RuleContext _ctx, String _className){
    	boolean ret = false;
    	for(int i = 0; i < _ctx.getChildCount(); i++){
        	if(_ctx.getChild(i).getClass().getSimpleName().equals(_className)){
        		return true;
        	}
       		if(_ctx.getChild(i).getChildCount() > 0){
       			ret = hasChildClass((RuleContext)_ctx.getChild(i), _className);
       			if (ret) return true;
       		}
    	}
        return ret;
    }
    
    public static Object getFirstChildInstanceOf(RuleContext _ctx, Class _class){
    	for(int i = 0; i < _ctx.getChildCount(); i++){
        	if(_class.isAssignableFrom(_ctx.getChild(i).getClass())){
        		return _ctx.getChild(i);
        	}
    	}
        return null;
    }
    
    public static Object getFirstChildClass(RuleContext _ctx, String _className){
    	for(int i = 0; i < _ctx.getChildCount(); i++){
        	if(_ctx.getChild(i).getClass().getSimpleName().equals(_className)){
        		return _ctx.getChild(i);
        	}
       		if(_ctx.getChild(i).getChildCount() > 0){
       			Object cls = getFirstChildClass((RuleContext)_ctx.getChild(i), _className);
       			if (cls != null) return cls;
       		}
    	}
        return null;
    }    

    public static Object getNextFirstChildClass(RuleContext _ctx, String _className){
    	for(int i = 0; i < _ctx.getChildCount(); i++){
        	if(_ctx.getChild(i).getClass().getSimpleName().equals(_className)){
        		return _ctx.getChild(i);
        	}
    	}
        return null;
    }  

    public static Object getDepthFirstChildClass(RuleContext _ctx, String _className){
    	if(_ctx == null){
    		try{
    			throw new IllegalArgumentException("*** ERROR - Context can not be null");
    		} catch (IllegalArgumentException e){
    			e.printStackTrace();
    		}
    		return null;
    	}
    	for(int i = 0; i < _ctx.getChildCount(); i++){
        	if(_ctx.getChild(i).getClass().getSimpleName().equals(_className)){
        		return _ctx.getChild(i);
        	}
        	else {
        		if((_ctx.getChild(i) instanceof RuleContext)){
        			Object o = getDepthFirstChildClass((RuleContext)_ctx.getChild(i), _className);
        		if( o != null)
        		   return getDepthFirstChildClass((RuleContext)_ctx.getChild(i), _className);
        		else continue;
        		}
        	}
    	}
        return null;
    } 
    
    public static RuleContext getBreathFirstChildClass(RuleContext _ctx, String _className){
    	
    	Deque<RuleContext> queue = new ArrayDeque<RuleContext>();
    	
    	if(_ctx == null){
    		try{
    			throw new IllegalArgumentException("*** ERROR - Context can not be null");
    		} catch (IllegalArgumentException e){
    			e.printStackTrace();
    		}
    		return null;
    	}
    	
    	for(int i = 0; i < _ctx.getChildCount(); i++){
        	if(_ctx.getChild(i).getClass().getSimpleName().equals(_className)){
        		return (RuleContext)_ctx.getChild(i);
        	}
        	else if(_ctx.getChild(i) instanceof RuleContext) {
        		queue.add((RuleContext)_ctx.getChild(i));
        	}
    	}
    	
    	for(RuleContext r : queue){
    		RuleContext ret = getBreathFirstChildClass(r, _className);
    		if (ret != null) return ret;
    	}
    	
    	return null;
    } 
    
    public static ArrayList<ParseTree> getDepthAllChildClass(RuleContext _ctx, String _className){
    	ArrayList<ParseTree> targetList = new ArrayList<ParseTree>();
    	if(_ctx == null){
    		try{
    			throw new IllegalArgumentException("*** ERROR - Context can not be null");
    		} catch (IllegalArgumentException e){
    			e.printStackTrace();
    		}
    		return null;
    	}
    	
    	for(int i = 0; i < _ctx.getChildCount(); i++){
        	if(_ctx.getChild(i).getClass().getSimpleName().equals(_className)){
        		targetList.add(_ctx.getChild(i));
        	}

       		if((_ctx.getChild(i) instanceof RuleContext)){
       			targetList.addAll(getDepthAllChildClass((RuleContext)_ctx.getChild(i), _className));
       		}
    	}
        return targetList;
    }    
    
    public static Object getNextLastChildClass(RuleContext _ctx, String _className){
    	for(int i = _ctx.getChildCount() -1 ; i > -1; i--){
        	if(_ctx.getChild(i).getClass().getSimpleName().equals(_className)){
        		return _ctx.getChild(i);
        	}
    	}
        return null;
    }  
    
    
    
    /*
     *  Se _inclusive == true =>  Se _ctx == _className retorna _ctx
     */
    public static ParserRuleContext getAncestorClass(RuleContext _ctx, String _className, boolean ..._inclusive){
    	if( _ctx.parent == null){
    		return null;
    	}
    	
    	if(_inclusive.length > 0 ? _inclusive[0] : false){
	    	if(_ctx.getClass().getSimpleName().equals(_className))
	    		return (ParserRuleContext)_ctx;
    	}
    	
    	if(_ctx.parent.getClass().getSimpleName().equals(_className)){
    		return (ParserRuleContext)_ctx.parent;
    	}
    	else{
    		return getAncestorClass(_ctx.parent, _className);
    	}
    }

    public static Symbol_b getAncestorByProperty(Symbol_b _sym, String _property_key, String _propertyValue){
    	if( _sym.getEnclosingScope() !=null && _sym.getEnclosingScope() instanceof Symbol_b){
    		   Symbol_b s = (Symbol_b)_sym.getEnclosingScope();
    		   if(s.hasProperty(_property_key, _propertyValue)){
    			   return s;
    		   }
    		   if(_propertyValue.equals("*")){ // não leva em consideraçãoo valor da propriedade
    			   if(s.getProperty(_property_key) != null) return s;
    		   }
    		   if(_sym.getEnclosingScope() == null) return null;
    		   if(!(_sym.getEnclosingScope() instanceof Symbol_b)) return null;
    		   return getAncestorByProperty((Symbol_b)_sym.getEnclosingScope(), _property_key, _propertyValue);
    	}
    	return null;
    }

    public static Symbol_b getBreathFirstChildSymbolByProperty(CopyOnWriteArrayList<Symbol_b> _queue, Symbol_b _sym, String _property_key, String _propertyValue){
   	
    	
    	if(_queue.isEmpty()){
    		System.err.println("************** ERROR: Symbol Not Exist");
    		return null;
    	}
    	
    	if (!(_queue.get(0) instanceof IScope)) return null;
    	
    	for(SymbolList symList : ((IScope)_queue.remove(0)).getMembers().values()){
        	for(Symbol_b ss : symList.getSymbols()){
        		if(ss.getProperties().hasProperty(_property_key, _propertyValue, true))
        			return ss;
        		else _queue.add(ss);
        	}
        }
    	
    	for(Symbol_b s : _queue){
    		Symbol_b ret = getBreathFirstChildSymbolByProperty(_queue, s, _property_key, _propertyValue);
    		if (ret != null) return ret;
    		if(_queue.isEmpty()) break;
    	}        
      
    	return null;
    }    
    
    public static ParserRuleContext getNextAncestorClass(RuleContext _ctx, String _className){
    	if( _ctx.parent == null){
    		return null;
    	}
    	
    	if(_ctx.parent.getClass().getSimpleName().equals(_className)){
    		return (ParserRuleContext)_ctx.parent;
    	}
    	return null;
    }    
    
    
    public static ParserRuleContext getAncestorClass(RuleContext _ctx, 
    							String _className,
    							String _startText){
    	if( _ctx.parent == null){
    		return null;
    	}
    	if(_ctx.parent.getClass().getSimpleName().equals(_className)){
    		if(((ParserRuleContext)_ctx.parent).start.getText().contains(_startText))
    			return (ParserRuleContext)_ctx.parent;
    	}
    		return getAncestorClass(_ctx.parent, _className);
    }    
    
    public static boolean isNextChildClass(RuleContext _ctx, String _className){
    	if(_ctx.getChildCount() == 0) return false;
    	
    	return (_ctx.getChild(0).getClass().getSimpleName().equals(_className)
    			 == true ? true : false);
    }
    
    
    public static boolean isFirstChildClass(RuleContext _ctx, String _className){
    	if(_ctx.getChildCount() == 0) return false;
    	
    	return (_ctx.getChild(0).getClass().getSimpleName().equals(_className)
    			 == true ? true : false);
    }    
    
    public static boolean hasNextChildClass(RuleContext _ctx, String _className){
    	for(int i=0; i < _ctx.getChildCount(); i++){
    		if(!(_ctx.getChild(i) instanceof ParserRuleContext)) continue;
    		ParserRuleContext prc = (ParserRuleContext)_ctx.getChild(i);
    		if(prc.getClass().getSimpleName().equalsIgnoreCase(_className)) return true;
    	}
    	return false;
    }
    
    public static ParserRuleContext getChildClass(ParserRuleContext _ctx, String _className){
		for(int i = 0; i < _ctx.getChildCount(); i++){
			if(_ctx.getChild(i).getClass().getSimpleName().equals(_className))
				return (ParserRuleContext)_ctx.getChild(i);
		}

       	for(ParseTree obj : _ctx.children){
    		if(!(obj instanceof ParserRuleContext)) {
    			continue;
    		}
//    		return getChildClass((ParserRuleContext)obj, _className);
    		ParserRuleContext resultCtx = getChildClass((ParserRuleContext)obj, _className);
    		if(resultCtx != null) return resultCtx;
    	}
       	return null;
    }    
    
/*	public boolean isSibling(RuleContext _ctx){
		RuleContext parent = _ctx.parent;
		if(parent == null) return false;
		for(int i = 0; i < parent.getChildCount(); i++){
			if(parent.getChild(i).getClass().getSimpleName().equalsIgnoreCase("AccessMemberOpContext"))
				return true;
		}
		return false;
	}*/
	
	public static ParserRuleContext getFunction(FunctionSearch _f, ParserRuleContext _funContext){
		String name = _f.getName();
		String funName = removeTypeIndicator(_funContext.getChild(0).getText());
		
		if(!funName.equalsIgnoreCase(name)) return null;
		
		if (_f.getParameters().size() == 0 )
			return (ParserRuleContext)_funContext; // pesquisa apenas nome de function 
		
		Iterator<ParseTree> it = _funContext.children.iterator();
		while(it.hasNext())	{
			Object obj = it.next();
			if(!(obj instanceof ParserRuleContext)) continue;
			ParserRuleContext prc = (ParserRuleContext)obj;
			if(prc.getClass().getSimpleName().equalsIgnoreCase("ArgListContext")){
				if(getFunctionWithParm(_f, prc))
						return _funContext;
			}
		}
		return null;
	}
	
	private static boolean getFunctionWithParm(FunctionSearch _f, ParserRuleContext _argListContext){
		String name = _f.getName();
		int parmOrder = 0;
		
		String[] parms = new String[_f.getParameters().size()];
		
		for(int i = 0; i < _f.getParameters().size(); i++ ){
			Integer ii = i;
			parms[i] = _f.getParameters().get(ii);
		}
		
		Iterator<ParseTree> it = _argListContext.children.iterator();
		
		while(it.hasNext() && parmOrder < parms.length)	{
			Object obj = it.next();
			if((obj instanceof TerminalNodeImpl)) continue;
			String argName = removeTypeIndicator(((ParserRuleContext)obj).getText());

			if(!argName.equalsIgnoreCase(parms[parmOrder]))
				if(!parms[parmOrder].equalsIgnoreCase("void"))
					return false;
			parmOrder++;
		}
		return true;
	}	
	
	private static String removeTypeIndicator(String name) {
		for (String s : sulfixTypeIndicator){
			if(name.endsWith(s)){
				name = name.replace(s, ""); // A$ -> A
				break;
			}
		}
		return name;
	}
	
	public static IScope getScope(ParserRuleContext _ctx, SymbolTable_b _st){
		IScope scope = _st.getScope(_ctx);
		if(scope != null) return scope;
		if(_ctx.getParent() != null) return getScope(_ctx.getParent(), _st);
		return null;
	}	
	
	public static ParserRuleContext getSibling(RuleContext _ctx, String _className){
		return getFirstSibling( _ctx, _className);
	}
	
	
	public static ParserRuleContext getFirstSibling(RuleContext _ctx, String _className){
		RuleContext parent = _ctx.parent;
		if(parent == null) return null;
		for(int i = 0; i < parent.getChildCount(); i++){
			if(parent.getChild(i).getClass().getSimpleName().equals(_className))
				return (ParserRuleContext)parent.getChild(i);
		}
		return null;
	}
	
	public static boolean hasSibling(RuleContext _ctx, String _className){
		RuleContext parent = _ctx.parent;
		if(parent == null) return false;
		for(int i = 0; i < parent.getChildCount(); i++){
			if(parent.getChild(i).getClass().getSimpleName().equals(_className))
				if(!parent.getChild(i).equals(_ctx))
					return true;
		}
		return false;
	}	
	
	public static ParserRuleContext getLastSibling(RuleContext _ctx){
		RuleContext parent = _ctx.parent;
		if(parent == null) return null;
		return getSiblingByIndex(_ctx, parent.getChildCount()-1);
	}
	
	public static ParserRuleContext getFirstSibling(RuleContext _ctx){
		RuleContext parent = _ctx.parent;
		if(parent == null) return null;
		return getSiblingByIndex(_ctx, 0);
	}
	
	public static ParserRuleContext getLastSibling(RuleContext _ctx, String _className){
		RuleContext parent = _ctx.parent;
		if(parent == null) return null;
		for(int i = parent.getChildCount() -1 ; i > -1; i--){
			if(parent.getChild(i).getClass().getSimpleName().equals(_className))
				return (ParserRuleContext)parent.getChild(i);
		}
		return null;
	}
	
	public static int getSiblingIndex(RuleContext _ctx){
		RuleContext parent = _ctx.parent;
		if(parent == null) return -1;
		for(int i = 0; i < parent.getChildCount(); i++){
			if(parent.getChild(i).equals(_ctx))
				return i;
		}
		return -1;
	}
	
	public static ParserRuleContext getSiblingByIndex(RuleContext _ctx, int _idx){
		RuleContext parent = _ctx.parent;
		if(parent == null) return null;
		for(int i = 0; i < parent.getChildCount(); i++){
			if(i == _idx) return (ParserRuleContext)parent.getChild(i);
		}
		return null;
	}
	
	public static ParserRuleContext getNextSibling(RuleContext _ctx){
		return getSiblingByIndex(_ctx, getSiblingIndex(_ctx)+1);
	}
	
	public static ParserRuleContext getPrevSibling(RuleContext _ctx){
		return getSiblingByIndex(_ctx, getSiblingIndex(_ctx)-2);  /// ze tratar ser -1
	}	

	public static boolean isClass(RuleContext _ctx, String _className){
    	if(_ctx.getClass().getSimpleName().equalsIgnoreCase(_className))
    		return true;
    	return false;
    }
    
    public static RuleContext getContext(RuleContext _ctx, String _text){
    	for(int i=0; i < _ctx.getChildCount(); i++){
    		if(!(_ctx.getChild(i) instanceof RuleContext)) continue;
    		RuleContext ruleCtx = (RuleContext) _ctx.getChild(i);
    		if(ruleCtx.getChildCount() > 0)
    			getContext(ruleCtx, _text);
    		if(isContext(_ctx,_text))
    			return ruleCtx;
    	}
    	return null;
    }
    
    public static List<IScope> getSibling(IScope _scope){
    	List<IScope> sibligList = new LinkedList<IScope>();
    	if(_scope.getEnclosingScope() != null){
    		IScope enclosing = _scope.getEnclosingScope();
    		for(SymbolList siblingList : enclosing.getMembers().values()){
    			for(Symbol_b sym: siblingList.getSymbols()){
    				if(sym instanceof IScope){
    					sibligList.add((IScope)sym);
    				}
    			}
    		}
    	}
    	return sibligList;    	
    } 
    
    public static boolean isContext(RuleContext _ctx, String _text){
    	if(_ctx.getText().equalsIgnoreCase(_text))
    		return true;
    	return false;
    }
    
    public static boolean isNextLevelChildClass(RuleContext _ctx, ArrayList<String> _classNames){
    	if(_ctx.getChildCount() == 0) return false;
    	
    	ParseTree tree = null;
    	for(int i=0; i < _ctx.getChildCount(); i++){
    		tree = _ctx.getChild(i);

    		for(String className : _classNames){
	    		if(tree.getClass().getSimpleName().equals(className)){
	    			return true;
	    		}
    		}
    	}
    	return false;
    }
    
    public static ArrayList<ParserRuleContext> setSymbolToParent(ParserRuleContext _ctx, Symbol_b _sym, PropertyList _prop){
    	// Lista de context que tiver símbolos associados
    	ArrayList<ParserRuleContext> resultContextList = (ArrayList<ParserRuleContext>)_prop.getProperty("RESULT_CONTEXT_LIST");
    	String memberAccessOper = (String)_prop.getProperty("MEMBER_ACCESS_OPER");
    	SymbolTable_b st = (SymbolTable_b)_prop.getProperty("SYMBOLTABLE");
    	// Lista de context que são passíveis de ter símbolos associados. Ex: ExprContext e IdentifierContext
    	ArrayList<String> classNameToSetList = (ArrayList<String>)_prop.getProperty("CLASS_NAME_TO_SET_LIST");
//    	ArrayList<String> validOperList = (ArrayList<String>)_prop.getProperty("VALID_OPER_LIST");

    	boolean isClassToSet = false;
    	for(String classToSet : classNameToSetList){
			if( _ctx.getParent().getClass().getSimpleName().equals(classToSet)){
				isClassToSet = true;
				break;
			}
    	}
    	
    	if(!isClassToSet) return resultContextList; // Não É ExprContext ou IdentifierContext
    	
    	String operContext = null;
    	for(ParseTree p : ((ParserRuleContext)_ctx.parent).children){
    		if(p.getClass().getSimpleName().endsWith("OperContext")){
    			operContext = p.getClass().getSimpleName();
    			break;
    		}
    	}
    	
    	if(operContext == null) { //Não OperContext as a child
    		    st.setSymbol((ParserRuleContext)_ctx.parent, _sym);
        		resultContextList.add((ParserRuleContext)_ctx.parent);
        		return resultContextList;    			
    	}
    	
    	if(!operContext.equals(memberAccessOper) 
    			&& !operContext.equals("NewOperContext")) return resultContextList; 
    	
		if(NodeExplorer.getSiblingIndex(_ctx) // member é última parte do nome qualificado
				== NodeExplorer.getSiblingIndex(NodeExplorer.getLastSibling(_ctx, _ctx.getClass().getSimpleName()))){
		    st.setSymbol((ParserRuleContext)_ctx.parent, _sym);
			resultContextList.add((ParserRuleContext)_ctx.parent);
    		return resultContextList;    			
		}

    	return resultContextList;
    }
}
