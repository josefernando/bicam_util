package br.com.bicam.util.app;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.NodeExplorer;
import br.com.bicam.util.graph.Graph;
import br.com.bicam.util.graph.Node;
import br.com.bicam.util.graph.app.CyclePath;
import br.com.bicam.util.symboltable.SymbolTable_b;
import br.com.bicam.util.symboltable.Symbol_b;
import br.com.bicam.util.symboltable.WhereUsed;

public class FinderX {
	SymbolTable_b st;
    Deque<Symbol_b> toVisit;
	HashSet<Symbol_b> visited;
	Map<Symbol_b, Symbol_b> memberObjectMap; 

    String cmdName;
    String cmdString;
    String currentCompilationUnitName;
    
	Map<Symbol_b, ArrayList<ParserRuleContext>> whereUsed; 
//	Deque<ParserRuleContext> cmdStrings;
	ArrayList<ParserRuleContext> cmdStrings;
	
	ParserRuleContext stringToValueContext;
    
    Graph graph;
    Graph graphReverse;
	
	public FinderX(SymbolTable_b _st, Graph _graph, String _compilationUnitName){
		this.st = _st;
		setGraph(_graph);
		inicialize();
	}
	
	public void inicialize(){
		this.toVisit = new ArrayDeque<Symbol_b>();     // A sequência de localização dos símbolos não importa
		this.visited = new LinkedHashSet<Symbol_b>();	 // A sequência de localização dos símbolos importa
		this.whereUsed = new IdentityHashMap<Symbol_b, ArrayList<ParserRuleContext>>();
		this.memberObjectMap = new IdentityHashMap<Symbol_b, Symbol_b>();  
		this.cmdStrings = new ArrayList<ParserRuleContext>();		
	}
	
	private void findSql(String _cmdSql){
		inicialize();
		cmdName = _cmdSql;
		findSymbolByName(cmdName);
		findWhereSymbolIsUsed();
		for(Symbol_b sym : memberObjectMap.keySet()){
			System.err.format("SYMBOL NAME \"%s\"  OF SCOPE \"%s\"%n", sym.getName(), sym.getEnclosingScope().getName());

			Symbol_b symProc = null;
			Symbol_b symCompilationUnit = null;
			
			StringBuffer sb = new StringBuffer();		
			for(ParserRuleContext ctx : getWhereUsed(sym)){
				symProc = st.getProcedureSymbol(ctx); 
				symCompilationUnit = st.getCompilarionUnitSymbol(ctx); 
				
				// A lista de símbolos é de todo o projeto, por isso filtra-se por compilation unit
//				if(!symCompilationUnit.getName().equalsIgnoreCase(compilationUnitName)) continue;
				if(!st.getCompilationUnitName(ctx).equalsIgnoreCase(currentCompilationUnitName)) continue;
				
				String p = ">> Symbol: " + sym.getName() +  " Used at line " + ctx.start.getLine()
				         + " at position in Line:position in file " 
						 + ctx.start.getCharPositionInLine()+ ":" 
				         + NodeExplorer.getAncestorClass(ctx,"StmtContext").start.getStartIndex() 
				         + " of (procedure/compilationUnit) "
				         + (symProc != null ? symProc.getName() : null)
				         + "/"
				         + (symCompilationUnit != null  ? symCompilationUnit.getName() : "???")
                         + "\n";
				sb.append(p);
			}
			
			if(sb.toString().length() > 0){
				System.err.println(sb.toString());
			}
			else {
				System.err.format("** WARNING: COMPILATION_UNIT \"%s\" HAS NO DB ACCESS WITH %s COMMAND .%n%n", currentCompilationUnitName, cmdName);
			}
		}
		getSqlStmts();		
	}
	
	public void findSql(){
		findSql("Open");	
		ArrayList<ParserRuleContext> cmdStrings_Execute = new ArrayList<ParserRuleContext>();
		cmdStrings_Execute.addAll(cmdStrings);
		findSql("Execute");
		cmdStrings_Execute.addAll(cmdStrings);
		cmdStrings.clear();
		cmdStrings.addAll(cmdStrings_Execute);
/*		reConstructor();
		cmdName = "Execute";
		findSymbolByName(cmdName);
		findWhereSymbolIsUsed(cmdName);
		for(Symbol sym : memberObjectMap.keySet()){
			System.err.format("SYMBOL NAME \"%s\"  OF TYPE \"%s\"%n", sym.getName(), sym.getScope().getName());

			Symbol symProc = null;
			Symbol symCompilationUnit = null;
			
			StringBuffer sb = new StringBuffer();		
			for(ParserRuleContext ctx : getWhereUsed(sym)){
				symProc = st.getProcedureSymbol(ctx); 
				symCompilationUnit = st.getCompilarionUnitSymbol(ctx); 
				
				// A lista de símbolos é de todo o projeto, por isso filtra-se por compilation unit
//				if(!symCompilationUnit.getName().equalsIgnoreCase(compilationUnitName)) continue;
				if(!st.getCompilationUnitName(ctx).equalsIgnoreCase(compilationUnitName)) continue;
				
				String p = ">> Symbol: " + sym.getName() +  " Used at line " + ctx.start.getLine()
				         + " at position in Line:position in file " 
						 + ctx.start.getCharPositionInLine()+ ":" 
				         + NodeExplorer.getAncestorClass(ctx,"StmtContext").start.getStartIndex() 
				         + " of (procedure/compilationUnit) "
				         + (symProc != null ? symProc.getName() : null)
				         + "/"
				         + (symCompilationUnit != null  ? symCompilationUnit.getName() : "???")
                         + "\n";
				sb.append(p);
			}
			
			if(sb.toString().length() > 0){
				System.err.println(sb.toString());
			}
			else {
				System.err.format("** WARNING: COMPILATION_UNIT \"%s\" HAS NO DB ACCESS.%n", compilationUnitName);
			}
		}
		getSqlStmts();*/
	}

	public void findMsg(){
		inicialize();
		cmdName = "MsgBox";
		findSymbolByName(cmdName);
		findWhereSymbolIsUsed();
		getMsgStmts();
	}
	
	public void  findSymbolByName(String _symbolName){
		for (Symbol_b sym : st.getSymbolByName(_symbolName)){
			toVisit.add(sym);
		}
	}
	
	/*
	 * Encontra os objetos conde o comando de db foi aplicado, como em:
	 * "commandText.Execute",  commandText é o objeto onde o comando (_memberContext) "Execute"
	 * foi aplicado.
	 * 
	 */
	private Symbol_b findObjectByMember(ParserRuleContext _memberContext){
		ParserRuleContext context = NodeExplorer.getPrevSibling(_memberContext);
		if(context == null) return null; // Symbol é to tipo  "A" e não "B.A"
		                                 // onde "B" é context 
		Symbol_b symObject = st.getSymbol(context);
		
		// verifica se objeto já foi avaliado. Se sim, descarta.
		if(visited.contains(symObject)) return null;
		return symObject;
	}	
	
	public void findWhereSymbolIsUsed() {
		if(toVisit.isEmpty()) return;
		
		Symbol_b sym = toVisit.pop();
		if(visited.contains(sym)) return; // Símbolo já foi pesquisado
		visited.add(sym);                 // lista de symbolos já pesquisados

		putWhereUsed(sym);
		
		for(ParserRuleContext usedLocationContext : getWhereUsed(sym)){
			Symbol_b symObject = findObjectByMember(usedLocationContext); // busca se sym é member, ex.: object.sym.
			if(symObject != null) {                                     // Se sim, retorna object. Se não, retorna null
				memberObjectMap.put(sym, symObject);					
				toVisit.add(symObject);	
			}			
		}
		findWhereSymbolIsUsed();
	}	
	
	public void setGraph(Graph _graph){
		this.graph = _graph;
		this.graphReverse = graph.reverse();
	}
	
	/*  Parâmetro: compilation Unit, fonte  do programa
	 *  Retorno: Lista dos comandos SQL, representados por Nodes (Stmt)
	 */
//public ArrayList<Node> getSqlStrings (String _compilationUnitName){
	public ArrayList<ParserRuleContext> getCmdStrings(){
		return cmdStrings;
	}
	
	private ArrayList<ParserRuleContext> getSqlStmts(){
		for(Symbol_b sym : visited){
			if(sym.getName().equals(cmdName)) { // É symbol "Execute"  e não, por ex.: "Connection" de "Connection.Execute"
				for(ParserRuleContext ctx : getWhereUsed(sym)){
//					if(!st.getCompilarionUnitSymbol(ctx).getName().equalsIgnoreCase(compilationUnitName)) continue;
					if(!st.getCompilationUnitName(ctx).equalsIgnoreCase(currentCompilationUnitName)) continue;

					ParserRuleContext executeCommandContext = NodeExplorer.getAncestorClass(ctx, "ExprContext");
	                ParserRuleContext realParam = NodeExplorer.getSibling(executeCommandContext, "RealParametersNoParenContext");

	                if(realParam == null){ // É "command.Execute"
//	                	Node wNode = matchExecuteWithSqlString(sym, ctx);
	                	
	                	String strcommand = null;
	                	
	                	if(cmdName.equalsIgnoreCase("Execute")) strcommand = "CommandText";
	                	if(cmdName.equalsIgnoreCase("Open")) strcommand = "ConnectionString";
	                	
	                	if(strcommand ==  null){
	                		try{
	                			throw new Exception();
	                		}
	                		catch(Exception e){
	                			e.printStackTrace();
	                			continue;
	                		}
	                	}
	                	
	                	ParserRuleContext wNode = matchExecuteWithSqlString(sym, ctx, strcommand);

						if(wNode != null) {
	//	                	String usedCompilationUnitName = st.getCompilarionUnitSymbol(executeCommandContext).getName();
							String usedCompilationUnitName = st.getCompilationUnitName(executeCommandContext);
							cmdStrings.add(wNode); // sqlString utilizado no comando execute
						}                    
	                }
	                else {
/* É "Execute" de "connection.Execute sqlString", ou "connection.Open sqlString" ou MsgBox (), porque tem parâmetro 
   ParserRuleContext realParamContext = (ParserRuleContext)NodeExplorer.getSibling(executeCommandContext, "RealParametersNoParenContext");
   executeCommandContext = (ParserRuleContext)NodeExplorer.getNextFirstChildClass(realParamContext, "ExprContext"); 
*/
	                	executeCommandContext = (ParserRuleContext)NodeExplorer.getNextFirstChildClass(realParam, "ExprContext");
	                	String usedCompilationUnitName = st.getCompilarionUnitSymbol(executeCommandContext).getName();
                		System.err.format("**** DEBUG: Comp Unit %s SQL STRING at line %d : %s%n", usedCompilationUnitName, executeCommandContext.start.getLine(), executeCommandContext.getText());
                		cmdStrings.add(executeCommandContext);
	                }
				}
			}
		}
		 return cmdStrings;
	}
	
	private ArrayList<ParserRuleContext> getMsgStmts(){
		for(Symbol_b sym : visited){
			if(sym.getName().equals(cmdName)) { 
				for(ParserRuleContext ctx : getWhereUsed(sym)){
					if(!st.getCompilarionUnitSymbol(ctx).getName().equalsIgnoreCase(currentCompilationUnitName)) {
//						System.err.format("** compilation Unit: %s  X  compilation Unit: %s%n%n"
//								 , (String)st.getCompilarionUnitSymbol(ctx).getProperty("FILE_NAME"), compilationUnitName);
						continue;
					}

// (ImplicitCallStmt (Expr (Identifier)) RealParametersNoParen (Expr....))	)                
					ParserRuleContext executeCommandContext = NodeExplorer.getAncestorClass(ctx, "ExprContext");
	                ParserRuleContext realParam = NodeExplorer.getSibling(executeCommandContext, "RealParametersNoParenContext");
	                if(realParam != null){
	                	executeCommandContext = (ParserRuleContext)NodeExplorer.getNextFirstChildClass(realParam, "ExprContext");
                		System.err.format("**** DEBUG: Comp Unit %s MSG STRING at line %d : %s%n", currentCompilationUnitName, executeCommandContext.start.getLine(), executeCommandContext.getText());
                		cmdStrings.add(executeCommandContext);	
                		continue;
	                }
// (Indentifier (Identifier RealParameters)	)                
	                realParam = NodeExplorer.getSibling(ctx, "RealParametersParenContext");
	                if(realParam != null){
	                	executeCommandContext = (ParserRuleContext)NodeExplorer.getNextFirstChildClass(realParam, "ExprContext");
//	                	String usedCompilationUnitName = (st.getCompilarionUnitSymbol(executeCommandContext)).getName();
                		System.err.format("**** DEBUG: Comp Unit %s MSG STRING at line %d : %s%n", currentCompilationUnitName, executeCommandContext.start.getLine(), executeCommandContext.getText());
                		cmdStrings.add(executeCommandContext);
                		continue;
	                }	                
				}
			}
		}
		 return cmdStrings;
	}
	/*
	 * Associa o comando de chamada ao banco de dados ao commandText
	 */
	private ParserRuleContext matchExecuteWithSqlString(Symbol_b _sym, ParserRuleContext _ctx, String _strCommand){ // _sym => Ex.: "adoObject.Execute" 
		int i = NodeExplorer.getAncestorClass(_ctx,"StmtContext").start.getStartIndex();
		String startPath = Integer.toString(i);
        
		Symbol_b symObject = memberObjectMap.get(_sym); // symObject é command ou connection object
        ArrayList<ArrayDeque<Node>> paths = new ArrayList<ArrayDeque<Node>>();


        if(getWhereUsed(symObject) == null) {
        	System.err.println("********  SYSOBJECT IS NULL: " + symObject.getName() +  " - scope name: " + symObject.getEnclosingScope().getName());
        	return null;
        }
        
		for(ParserRuleContext used_Ctx : getWhereUsed(symObject)){
			if(!st.getCompilarionUnitSymbol(_ctx).equals(st.getCompilarionUnitSymbol(used_Ctx))) continue;
			
			int k = NodeExplorer.getAncestorClass(used_Ctx,"StmtContext").start.getStartIndex();
			
//			if(!isCommand(used_Ctx, "CommandText")) continue; // Não é commando que set sqlString
			if(!isCommand(used_Ctx, _strCommand)) continue; // Não é commando que set sqlString

			String endPath = Integer.toString(k);
			
			if(!endPath.equals(startPath)){ // descarta o próprio comando de chamada Execute
		        CyclePath path = new CyclePath(graphReverse, startPath);
		        path.addKeyProperty("LINE");
		        paths.addAll(path.pathTo(endPath));
			}
		}
		
        int menorPath = 0;
        if(paths.size() > 0){ // Encontrado path entre "command.Text" e "command.Execute" 
	        for(int ix = 0; ix < paths.size() ; ix++){
	        	if(paths.get(ix).size() < paths.get(menorPath).size())  menorPath = ix;
	        }
	        System.err.println("DEBUG - IMPRIMINDO MENOR PATH de 'command.Text' até 'command.execute' - Qte. de Paths: "
	             + paths.size());
	        printPath(paths.get(menorPath));
	        Node retNode = null;
	        
	        // Pega o último node do path, que corresponde à limha decomando sql,
	        // ou, sqkString
	        for(Node n :paths.get(menorPath) ){
	        	retNode = n;
	        }
	        System.err.println(">>>> DEBUG: " + ((ParserRuleContext)retNode.getItem()).start.getLine());
            System.err.println(((ParserRuleContext)retNode.getItem()).getText());
	        System.err.println("\n");
	        return (ParserRuleContext)retNode.getItem();
        }				
		return null;
	}
	
    private  void printPath(ArrayDeque<Node> _path) {
		Iterator<Node> it2 = _path.iterator();
		while(it2.hasNext()){
			Node wNode = it2.next();
			System.err.print(wNode.getId() + "/" + ((ParserRuleContext)wNode.getItem()).start.getLine()
//					+ (keyProperties.size() > 0 ? "-" + getPropertyValues(wNode, keyProperties) : "")
					+" >> ");
		}
    	System.err.println();
    }
	
	/*
	 *  Em adoobject.CommandText, retorna String "CommandText".
	 *  Em adoObject, retorna null
	 */
	
	public boolean isCommand(ParserRuleContext _memberContext, String _memberName){ 
		ParserRuleContext context = NodeExplorer.getLastSibling(_memberContext, "IdentifierContext");
		if(context == null) return false; // Symbol é to tipo  "B" e não "B.A"
		Symbol_b symObject = st.getSymbol(context); 
		
		if(symObject.getName().equals(_memberName)) return true;
		return false;
	}
	
	private ArrayList<ParserRuleContext> getWhereUsed(Symbol_b _sym){ 
		if(whereUsed.get(_sym) == null){
			whereUsed.put(_sym, new ArrayList<ParserRuleContext>());
		}
		return whereUsed.get(_sym);
	}
	
	private void putWhereUsed(Symbol_b _sym){
		if(getWhereUsed(_sym) == null){
			whereUsed.put(_sym, new ArrayList<ParserRuleContext>());
		}
		WhereUsed used = (WhereUsed)_sym.getProperty("WHERE_USED");
		for(ParserRuleContext usedContext : used.used()){
			getWhereUsed(_sym).add(usedContext);
		}
	}
	
	public void setStringToValueContext(ParserRuleContext _stringContext){
		stringToValueContext = _stringContext;
	}
	
	public ParserRuleContext getStringToValueContext(){
		return stringToValueContext;
	}	
}
