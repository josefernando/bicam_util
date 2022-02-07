package br.com.bicam.util.catalog;

import java.io.File;

public interface IRepository {
	public String getFirstMetadataDir();
	public String getFirstModule(File _vbpFile);
	public String getFirstModule();
	public boolean hasNextModule(File _file);
	public String getNextModule();
	public boolean hasNextModule();
}
