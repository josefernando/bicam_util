package br.com.bicam.util.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import br.com.bicam.util.PropertyList;
import br.com.bicam.util.symboltable.Symbol_New;

@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlRootElement
public class Vb6Application extends ApplicationComponent{
	public Vb6Application() {}
	public Vb6Application(Symbol_New _symbol) {
		super(_symbol);
	}
	
	public void bindSymbol() {
		addFormComponents();
		addModuleComponents();
		addClassComponents();
		addReferencies();
		addObjects();
	}
	
	private void addFormComponents() {
		List<PropertyList> forms = (ArrayList)getProperties().getProperty("Form");
		if(forms ==  null) return;
		for(PropertyList p : forms) {
			String name = (String)p.getProperty("NAME");
			ApplicationComponent app = new Vb6Form(name);
			app.getProperties().setProperties(p.getProperties());
			addComponent(app);
		}
	}
	
	private void addModuleComponents() {
		List<PropertyList> modules = (ArrayList)getProperties().getProperty("Module");
		if(modules ==  null) return;
		for(PropertyList  p: modules) {
			String name = (String)p.getProperty("NAME");
			ApplicationComponent app = new Vb6Module(name);
			app.getProperties().setProperties(p.getProperties());
			addComponent(app);			
		}		
	}
	
	private void addClassComponents() {
		List<PropertyList> classes = (ArrayList)getProperties().getProperty("Class");
		if(classes ==  null) return;
		for(PropertyList p : classes) {
			String name = (String)p.getProperty("NAME");
			ApplicationComponent app = new Vb6Class(name);
			app.getProperties().setProperties(p.getProperties());
			addComponent(app);	
		}		
	}
	
	private void addReferencies() {
		List<PropertyList> references = (ArrayList)getProperties().getProperty("Reference");
		if(references ==  null) return;
		for(PropertyList p : references) {
			String name = (String)p.getProperty("NAME");
			ApplicationComponent app = new Vb6Reference(name);
			app.getProperties().setProperties(p.getProperties());
			addComponent(app);		
		}		
	}
	
	private void addObjects() {
		List<PropertyList> objects = (ArrayList)getProperties().getProperty("Object");
		if(objects ==  null) return;
		for(PropertyList p : objects) {
			String name = (String)p.getProperty("NAME");
			ApplicationComponent app = new Vb6Object(name);
			app.getProperties().setProperties(p.getProperties());
			addComponent(app);
		}
	}
}