package com.matrixcos.conditionaldisplay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/*
 * This class will be removed once whole logic is integrate with main code.
 */
public class DBConnection 
{
	final static Logger logger = Logger.getLogger(DBConnection.class);
	
	public static Connection connection(String dbUrl, String userName, String password) throws ClassNotFoundException, SQLException
	{
		Connection connection = null;
		try{
		Class.forName("oracle.jdbc.driver.OracleDriver"); 
		connection = DriverManager.getConnection(dbUrl, userName, password);
		}
		catch(ClassNotFoundException cnfe){connection = null; cnfe.printStackTrace();}
		catch(SQLException sqle){connection = null; sqle.printStackTrace();}
		catch(Exception e){connection = null; e.printStackTrace();}
		return connection;
	}
}
