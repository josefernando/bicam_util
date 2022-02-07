package br.com.bicam.util.jaxb;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlSetAdapter<T> extends XmlAdapter<SetType<T>, Set<T>> {

	@Override
	public SetType<T> marshal(Set<T> v) throws Exception {
		SetType<T> st = new SetType<T>();
		for(T e : v) {
			st.addEntry(e);
		}
		return st;
	}

	@Override
	public Set<T> unmarshal(SetType<T> v) throws Exception {
		Set<T> st = new HashSet<T>();
		for(T e: v.getEntry()) {
			st.add(e);
		}
		return null;
	}
}