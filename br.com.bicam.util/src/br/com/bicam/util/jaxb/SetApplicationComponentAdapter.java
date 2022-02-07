package br.com.bicam.util.jaxb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.com.bicam.util.datamodel.ApplicationComponent;

public class SetApplicationComponentAdapter extends XmlAdapter<List<ApplicationComponent>, Set<ApplicationComponent>>{

	@Override
	public List<ApplicationComponent> marshal(Set<ApplicationComponent> v) throws Exception {
		List<ApplicationComponent> ret = new ArrayList<ApplicationComponent>();
		for(ApplicationComponent  c : v) {
			ret.add(c);
		}
		return ret;
	}

	@Override
	public Set<ApplicationComponent> unmarshal(List<ApplicationComponent> v) throws Exception {
		Set<ApplicationComponent> ret = new HashSet<ApplicationComponent>();
		for(ApplicationComponent o : v) {
			ret.add(o);
		}
		return ret;
	}
}