package br.com.bicam.util.sql;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class ConnectionFactory{
	private static Properties dbProperties;
	private static String url;
	private static Driver dbDriver;

	static{
		try{
			dbProperties = new Properties();
			dbProperties.load(new FileInputStream("jdbc.config"));
			//This will load all the properties (i.e. key=value) from the jdbc.properties file from the current working directory
			dbDriver = (Driver)Class.forName(dbProperties.getProperty("DriverClassName")).newInstance();
			url = dbProperties.getProperty("url"); //This will return the value of url, if not found return null
		}catch(Exception e){}
	}
	
	public static Connection getConnection() throws Exception{
		return dbDriver.connect(url,dbProperties);
	}

	public static void closeConnection(Connection con, Statement st, ResultSet rs) throws Exception{
		if(con!=null)
			con.close();
		if(st!=null)
			st.close();
		if(rs!=null)
			rs.close();
	}
}