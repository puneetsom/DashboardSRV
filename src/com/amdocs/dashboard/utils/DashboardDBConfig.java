package com.amdocs.dashboard.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.amdocs.infra.utils.Config;
import com.amdocs.infra.utils.DBConfig;

public class DashboardDBConfig
{
	//Singleton
	private static DashboardDBConfig mInstance = null;

	private DashboardDBConfig()
	{
	}
	
	public static DashboardDBConfig getInstance()
	{
        if(mInstance == null)
            mInstance = new DashboardDBConfig();

        return mInstance;
	}

	public Connection getConnection(String dbPrefix) throws SQLException
    {
		//TODO: change this to use connection pools.

        System.out.println("Get Connection for: " + dbPrefix);
    	Config aConfig = Config.getInstance();
        Connection con = DBConfig.getConnection((String)aConfig.getProperty(dbPrefix+"_DB_URL"),
        										(String)aConfig.getProperty(dbPrefix+"_DB_USER"),
        										(String)aConfig.getProperty(dbPrefix+"_DB_PASSWORD"));
        return con;
    } 

}
