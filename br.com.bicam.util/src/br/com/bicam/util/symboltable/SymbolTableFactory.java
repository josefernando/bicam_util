package br.com.bicam.util.symboltable;

import org.antlr.v4.runtime.ParserRuleContext;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.constant.SymbolType;

public class SymbolTableFactory {
	
//	static PropertyList properties;
	
	public Symbol_b getSymbol(PropertyList _properties){
		SymbolType symType = (SymbolType)_properties.getProperty("SYMBOL_TYPE");
		switch (symType){
			case COMPILATION_UNIT:
				return createCompilationSymbol(_properties);
			case APPLICATION:
				return createApplicationSymbol(_properties);				
			case BUILTIN_TYPE:
				return createBuiltinTypeSymbol(_properties);
			case PRIMITIVE_TYPE:
				return createBuiltinTypeSymbol(_properties);
			case REFERENCE:
				return createReferenceSymbol(_properties);
			case COMPONENT:
				return createComponentSymbol(_properties);				
			case GUI:
				return createGuiSymbol(_properties);
			case DB:
				return createDbSymbol(_properties);				
			case VARIABLE:
				return createVariableSymbol(_properties);
			case OBJECT:
				return createObjectSymbol(_properties);				
			case STRUCT:
				return createStructSymbol(_properties);
			case PROCEDURE:
				return createProcedureSymbol(_properties);
			case LOCAL_SCOPE: // è tratadp como symbol...
				return createLocalScope(_properties);
			case LABEL: // è tratadp como symbol...
				return createLabel(_properties);				
		default:
			throw new RuntimeException("** Error*** - Invalid Symbol type to create: "  
					+ ((ParserRuleContext)_properties.getProperty("CONTEXT")).getClass().getSimpleName());			
		}
	}
	
	private Symbol_b createCompilationSymbol(PropertyList _properties){
		CompilationUnit_b_Symbol sym = new CompilationUnit_b_Symbol(_properties);
		return sym;
	}
	
	private Symbol_b createApplicationSymbol(PropertyList _properties){
		Application_b_Symbol sym = new Application_b_Symbol(_properties);
		return sym;
	}	
	
	public static Symbol_b createBuiltinTypeSymbol(PropertyList _properties){
		BuiltinType_b_Symbol sym = new BuiltinType_b_Symbol(_properties);
		return sym;
	}
	
	public static Symbol_b createGuiSymbol(PropertyList _properties){
		GuiSymbol_b sym = new GuiSymbol_b(_properties);
		return sym;
	}
	
	public static Symbol_b createReferenceSymbol(PropertyList _properties){
		Reference_b_Symbol sym = new Reference_b_Symbol(_properties);
		return sym;
	}
	
	public static Symbol_b createComponentSymbol(PropertyList _properties){
		Component_b_Symbol sym = new Component_b_Symbol(_properties);
		return sym;
	}	
	
	public static Symbol_b createVariableSymbol(PropertyList _properties){
		VariableSymbol_b sym = new VariableSymbol_b(_properties);
		return sym;
	}
	
	public static Symbol_b createObjectSymbol(PropertyList _properties){
		ObjectSymbol sym = new ObjectSymbol(_properties);
		return sym;
	}	
	
	public static Symbol_b createDbSymbol(PropertyList _properties){
		DbSymbol sym = new DbSymbol(_properties);
		return sym;
	}	
	
	public static Symbol_b createLabelSymbol(PropertyList _properties){
		LabelSymbol sym = new LabelSymbol(_properties);
		return sym;
	}
	
	public static Symbol_b createStructSymbol(PropertyList _properties){
		StructSymbol_b sym = new StructSymbol_b(_properties);
		return sym;
	}
	
	public static Symbol_b createProcedureSymbol(PropertyList _properties){
		MethodSymbol_b sym = new MethodSymbol_b(_properties);
		return sym;
	}
	
	public static Symbol_b createLocalScope(PropertyList _properties){
		LocalScope_b scopeSym = new LocalScope_b(_properties);
		return scopeSym;
	}
	
	public static Symbol_b createLabel(PropertyList _properties){
		LabelSymbol scopeSym = new LabelSymbol(_properties);
		return scopeSym;
	}	
	
	public static WhereUsed whereUsedDefault(){
		return new WhereUsed();
	}
/*	private static boolean ispropertyNull(Object obj){
		return obj == null ? true : false;
	}*/	
}
