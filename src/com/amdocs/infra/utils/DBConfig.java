/*
 * DBConfig.java
 *
 * Created on October 25, 2007, 2:36 PM
 *
 * Stores DB configuration. A utility class that is used for connections.
 */

package com.amdocs.infra.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Prithvi K P
 */
public class DBConfig 
{
    static 
    {
        try
        {        
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }        
    
    public static Connection getConnection(String url, String userName, String password) throws SQLException
    {
    	//System.out.println("Get Connection: " + url + ", " + userName + ", " + password);
        return DriverManager.getConnection(url, userName, password);
    }

    public static String readSQL(String sqlFile) throws IOException
    {
        String str = ""; 
        String strFile = ""; 
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(sqlFile));
           
            while ((str = in.readLine()) != null) 
            {
                strFile = strFile + str;
            }
            in.close();
        } 
        catch (IOException e) 
        {
            throw e;
        }
        
        return strFile;
    }
    
    public static HashMap parseSQL(String strQuery, HashMap defaultParams)
    {
        return parseSQL(strQuery, defaultParams, null);
    }
    /*
     * 
     */       
    public static HashMap parseSQL(String strQuery, HashMap defaultParams, HashMap reqParams)
    {
        String strParsedQuery = "";
        HashMap arr = new HashMap();
        ArrayList paramValues = new ArrayList();
        ArrayList params = new ArrayList();
                
        boolean found = false;
        String strToken = "";
        for(int i = 0; i < strQuery.length(); i++)
        {
            char ch = strQuery.charAt(i);
            if(ch != ':' && found == false)
                strParsedQuery = strParsedQuery + ch;
            else if(ch == ':')
                found = true;
            
            if(found && ch != ' ' && ch != ':')
                strToken = strToken + ch;
            if(found && ch == ' ' && ch != ':')
            {
                params.add(strToken);
                strParsedQuery = strParsedQuery + " ? ";
                found = false;
                strToken = "";
            }            
        }
        
        if(found)
        {
           params.add(strToken);
           strParsedQuery = strParsedQuery + " ? ";
        }    
        
        arr.put("QUERY", strParsedQuery);
        //System.out.println(strParsedQuery);
        Iterator iter = params.iterator();        
        while(iter.hasNext()) 
        {
            String key = (String)iter.next();
            String value = "";
            //System.out.println(key);
            
            if(reqParams != null)
            {    
                if(reqParams.containsKey(key))
                    value = (String)reqParams.get(key);
                else
                    value = (String)defaultParams.get(key);
            }
            arr.put(key, value);  
            paramValues.add(value);
        }
        arr.put("PARAM_VALUES", paramValues);
        return arr;
    }
}
