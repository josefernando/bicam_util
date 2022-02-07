package br.com.bicam.util.jaxb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SetStringAdapter extends XmlAdapter<List<String>, Set<String>>{

	@Override
	public List<String> marshal(Set<String> v) throws Exception {
		List<String> ret = new ArrayList<String>();
		for(String o : v) {
			ret.add(o);
		}
		return ret;
	}

	@Override
	public Set<String> unmarshal(List<String> v) throws Exception {
		Set<String> ret = new HashSet<String>();
		for(String o : v) {
			ret.add(o);
		}
		return ret;
	}
}
