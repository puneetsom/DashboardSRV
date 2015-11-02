/**
 * ConnectionManager - Copyright (C) Amdocs 2008
 * 
 * Static helper to assist figuring out how to use specific server connection pool (Tomcat or WebLogic) 
 * 
 * @author Adi Rabinovich
 *
 */
package com.amdocs.infra.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionManager {
	public static Connection getConnection(String dbPoolName) throws SQLException, NamingException {
	     // Obtain our environment naming context
		InitialContext initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");

        // Look up our data source
        DataSource ds = (DataSource)
          envCtx.lookup("jdbc/"+dbPoolName);

        // Allocate and use a connection from the pool
        return ds.getConnection();
	}

}
