package br.com.bicam.util.jaxb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.com.bicam.util.datamodel.CatalogItem;

public class SetCatalogItemAdapter extends XmlAdapter<List<CatalogItem>, Set<CatalogItem>>{

	@Override
	public List<CatalogItem> marshal(Set<CatalogItem> v) throws Exception {
		List<CatalogItem> ret = new ArrayList<CatalogItem>();
		for(CatalogItem  c : v) {
			ret.add(c);
		}
		return ret;
	}

	@Override
	public Set<CatalogItem> unmarshal(List<CatalogItem> v) throws Exception {
		Set<CatalogItem> ret = new HashSet<CatalogItem>();
		for(CatalogItem o : v) {
			ret.add(o);
		}
		return ret;
	}
}