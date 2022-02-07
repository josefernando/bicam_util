package br.com.bicam.util.db;

import java.util.ArrayList;
import java.util.List;

public class ConnectionDb {
	String driver;
	String dataSource;
	List<String> sqlStrings;
	
	public ConnectionDb(){
		sqlStrings = new ArrayList<String>();
	}
	
	public void setDriver(String _driver){
		this.driver = _driver;
	}
	
	public String getDriver(){
		return driver;
	}
	
	public void setDataSource(String _dataSource){
		this.dataSource = _dataSource;
	}
	
	public String getDataSource(){
		return dataSource;
	}	

	public List<String> getSqlStrings(){
		return sqlStrings;
	}	
}