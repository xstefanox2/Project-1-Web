package utilities;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class ConnManager {
	
	private static BasicDataSource ds = new BasicDataSource();
	private static PropertiesReader propRead = PropertiesReader.getInstance();
	
	static {
		PropertiesReader pr = PropertiesReader.getInstance();
		String dbUrl = "jdbc:postgresql://"+pr.getValue("dbHost")+":"+pr.getValue("dbPort")+"/"+pr.getValue("dbName");
		ds.setUrl(dbUrl);
		ds.setUsername(propRead.getValue("dbUser"));
		ds.setPassword(propRead.getValue("dbPassword"));
		ds.setDriverClassName("org.postgresql.Driver");
	}
	
	
	
	public static Connection getConnection()  {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
