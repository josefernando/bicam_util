package br.com.bicam.util;

public class Property {
	String key;
	Object value; // pode ser um valor único(1) ou uma lista(2)
	              // (1) Caption (key) =   "Consulta de Itens / Aditamentos" (value)
	              // ou exclusivo
	              // (2) Font(key) = {Name="MS Sans Serif", Size=8.25, ...Strikethrough=0}
					/* BeginProperty Font  
				    Name            =   "MS Sans Serif"
				    Size            =   8.25
				    Charset         =   0
				    Weight          =   700
				    Underline       =   0   'False
				    Italic          =   0   'False
				    Strikethrough   =   0   'False	*/	
//	List<Property> propertyList;
	
	public Property(String _key, Object _value) {
		this.key = _key;
		this.value = _value;
	}
	
	public String getkey() {
		return key;
	}
	
	public Object getValue() {
		return value;
	}
	
	public boolean ispropertyList() {
		return value == null? true : false;
	}
}