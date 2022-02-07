package br.com.bicam.util.catalog;

public interface IMetadata {
	public void accept(MetadataVisitor visitor);
}
