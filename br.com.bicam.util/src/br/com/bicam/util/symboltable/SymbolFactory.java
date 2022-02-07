package br.com.bicam.util.symboltable;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType_New;

public class SymbolFactory {

	public Symbol_New getSymbol(PropertyList _properties){
		SymbolType_New symType = (SymbolType_New)_properties.getProperty("SYMBOL_TYPE");
		switch (symType){
		case APPLICATION:
			return createApplicationSymbol(_properties);
	        case ALIAS:
	    	return createAliasSymbol(_properties);	
	        case CURSOR:
	    	return createCursorSymbol(_properties);	    	
		    case DATABASE:
		    	return createDatabaseSymbol(_properties);
		    case TRANSACTION:
		    	return createTransactionSymbol(_properties);	
		    case INDEX:
		    	return createIndexSymbol(_properties);		    	
		    case SERVER:
		    	return createServerSymbol(_properties);	
		    case COLUMN:
		    	return createColumnSymbol(_properties);	
		    case TABLE:
		    	return createTableSymbol(_properties);			    	
		    case USERDB:
		    	return createUserSymbol(_properties);		    	
			case COMPILATION_UNIT:
				return createCompilationSymbol(_properties);
			case BUILTIN:
				return createBuiltinSymbol(_properties);
			case PRE_DEFINED:
				return createPreDefinedSymbol(_properties);	
			case REFERENCE:
				return createReferenceSymbol(_properties);
			case VARIABLE:
				return createVariableSymbol(_properties);
			case LABEL:
				return createLabelSymbol(_properties);				
			case GUI:
				return createGuiSymbol(_properties);				
			case TYPE:
				return createTypeSymbol(_properties);
			case ENUM:
				return createEnumSymbol(_properties);				
			case METHOD:
				return createMethodSymbol(_properties);
			case STRUCTURE:
				return createStructureSymbol(_properties);		
			case STORED_PROCEDURE:
				return createStoredProcedureSymbol(_properties);				
			case GLOBAL_SCOPE: // è tratadp como symbol...
				return createGlobalScope(_properties);				
			case LOCAL_SCOPE: // è tratadp como symbol...
				return createLocalScope(_properties);
			case VISIBILITY_SCOPE: // è tratadp como symbol...
				return createVisibilityScope(_properties);				
		default:
			throw new RuntimeException("** Error*** - Invalid Symbol type to create: "  
					+ ((ParserRuleContext)_properties.getProperty("CONTEXT")).getClass().getSimpleName()
					+ " SymbolType: " + symType);			
		}
	}
	
	private Symbol_New createApplicationSymbol(PropertyList _properties){
		ApplicationSymbol_New sym = new ApplicationSymbol_New(_properties);
		return sym;
	}	
	
	private Symbol_New createCompilationSymbol(PropertyList _properties){
		CompilationUnitSymbol_New sym = new CompilationUnitSymbol_New(_properties);
		return sym;
	}
	
	private Symbol_New createServerSymbol(PropertyList _properties){
		ServerSymbol sym = new ServerSymbol(_properties);
		return sym;
	}
	
	private Symbol_New createAliasSymbol(PropertyList _properties){
		AliasSymbol sym = new AliasSymbol(_properties);
		return sym;
	}
	
	private Symbol_New createCursorSymbol(PropertyList _properties){
		AliasSymbol sym = new AliasSymbol(_properties);
		return sym;
	}	
	
	private Symbol_New createDatabaseSymbol(PropertyList _properties){
		DatabaseSymbol sym = new DatabaseSymbol(_properties);
		return sym;
	}
	
	
	private Symbol_New createTransactionSymbol(PropertyList _properties){
		TransactionSymbol sym = new TransactionSymbol(_properties);
		return sym;
	}
	
	private Symbol_New createIndexSymbol(PropertyList _properties){
		IndexSymbol sym = new IndexSymbol(_properties);
		return sym; 
	}	
	
	private Symbol_New createTableSymbol(PropertyList _properties){
		TableSymbol sym = new TableSymbol(_properties);
		return sym;
	}
	
	private Symbol_New createColumnSymbol(PropertyList _properties){
		ColumnSymbol sym = new ColumnSymbol(_properties);
		return sym;
	}	
	
	private Symbol_New createUserSymbol(PropertyList _properties){
		UserSymbol sym = new UserSymbol(_properties);
		return sym;
	}	
	
	public static Symbol_New createBuiltinSymbol(PropertyList _properties){
		BuiltinSymbol_New sym = new BuiltinSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createPreDefinedSymbol(PropertyList _properties){
		PreDefinedSymbol_New sym = new PreDefinedSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createReferenceSymbol(PropertyList _properties){
		ReferenceSymbol_New sym = new ReferenceSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createVariableSymbol(PropertyList _properties){
		VariableSymbol_New sym = new VariableSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createGuiSymbol(PropertyList _properties){
		GuiSymbol_New sym = new GuiSymbol_New(_properties);
		return sym;
	}	
	
	public static Symbol_New createTypeSymbol(PropertyList _properties){
		TypeSymbol_New sym = new TypeSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createEnumSymbol(PropertyList _properties){
		EnumSymbol_New sym = new EnumSymbol_New(_properties);
		return sym;
	}	
	
	public static Symbol_New createMethodSymbol(PropertyList _properties){
		MethodSymbol_New sym = new MethodSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createLabelSymbol(PropertyList _properties){
		LabelSymbol_New sym = new LabelSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createStructureSymbol(PropertyList _properties){
		StructureSymbol_New sym = new StructureSymbol_New(_properties);
		return sym;
	}
	
	public static Symbol_New createStoredProcedureSymbol(PropertyList _properties){
		StoredProcedureSymbol sym = new StoredProcedureSymbol(_properties);
		return sym;
	}
	
	public static Symbol_New createGlobalScope(PropertyList _properties){
		 GlobalScope_New scopeSym = new GlobalScope_New(_properties);
		return scopeSym;
	}	
	
	public static Symbol_New createLocalScope(PropertyList _properties){
		LocalScope_New scopeSym = new LocalScope_New(_properties);
		return scopeSym;
	}
	
	public static Symbol_New createVisibilityScope(PropertyList _properties){
		VisibilityScope scopeSym = new VisibilityScope(_properties);
		return scopeSym;
	}
	
	public static WhereUsed whereUsedDefault(){
		return new WhereUsed();
	}
}
