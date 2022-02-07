package br.com.bicam.util.symboltable;

import java.util.ArrayList;
import java.util.List;

public class ParameterList {
	List<Type> orderedParameters;
	
	public ParameterList(){
		orderedParameters = new ArrayList<Type>();
	}
    
	public List<Type> getParameters(){
		return orderedParameters;
	}
	
	public void addParameter(Type _paramType){
		orderedParameters.add(_paramType);
	}
	
	public Type getParameter(int _paramOrder){
		return orderedParameters.get(_paramOrder);
	}
}