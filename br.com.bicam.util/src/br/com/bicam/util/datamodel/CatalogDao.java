package br.com.bicam.util.datamodel;

import java.util.Set;

import br.com.bicam.util.catalog.Repository;

public interface CatalogDao extends IDao {
//	public Catalog get(String _id);
	public Set<Repository> getRepositories();
	public Repository getRepository(String _id);
	public void addRepository(Repository _repository);
	public void removeRepository(Repository _repository);
	public void commit();
}