/**
 * ConnectionManager - Copyright (C) Amdocs 2008
 * 
 * AT&T Specific logic implementation to identify connection pool based on parameters in SQL (cust Id, Rep, etc) 
 * 
 * @author Adi Rabinovich
 *
 */
package com.amdocs.dashboard.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.UnavailableException;

import com.amdocs.infra.utils.ConnectionPoolHelper;
import com.amdocs.infra.utils.SQLDescriptor;


public class AttConnectionPoolHelper implements ConnectionPoolHelper
{	
	// See this properties file to read about how this class works
	public static final String att_pool_configuration = "/WEB-INF/flex/att-connection-pools.properties";
	
	// Some more static property names
	public static final String known_cust_id_params_name = "CUSTOMER_PARAMETER_NAMES";
	public static final String known_cust_id_pool_name = "CUSTOMER_POOL";
	public static final String home_db_hint_param = "home_data_base";
	
	// Logger for troubles and Information
	public static Logger logger;
	
	// Loaded during init call, from parameters
	private String[] known_cust_id_params;
	
	private String[] customer_pools;

	// Initialization that container will activate to give this helper a chance to load settings file
	public void init(ServletConfig config, Logger i_logger) throws IOException, UnavailableException {
		Properties prop = new Properties();
		logger=i_logger;

		prop.load(config.getServletContext().getResourceAsStream(att_pool_configuration));

		// Loading default "sniffing" parameters
		known_cust_id_params = loadNamesFromProperty(prop, known_cust_id_params_name);
		// Now loading pool mappings
		customer_pools = new String[9];		// Will need to shift things, allas
		for(int i=0;i<9;i++)
		{
			customer_pools[i]=prop.getProperty(known_cust_id_pool_name+(i+1));
			if(customer_pools[i]==null)
				throw new UnavailableException("AttConnectionPoolHelper: Failed to load property "+known_cust_id_pool_name+(i+1));
		}
//		System.out.println("AttConnectionPoolHelper for QueryCache Server - Loaded succesfully. Configuration is:");
//		System.out.println(this);
		logger.info("AttConnectionPoolHelper for QueryCache Server - Loaded succesfully. Configuration is:");
		logger.info(this.toString());
	}
	
	private String[] loadNamesFromProperty(Properties prop, String propName) throws UnavailableException {
		String propValue = prop.getProperty(propName); 
		if(propValue==null)
			throw new UnavailableException("AttConnectionPoolHelper: Failed to load required property: "+propName+" from "+att_pool_configuration);
	
       	StringTokenizer resultTok = new StringTokenizer(propValue,";,: \t\n\r\f");
       	if(resultTok.countTokens()==0)
       		throw new UnavailableException("AttConnectionPoolHelper: Parameter "+propName+" cannot be empty!");

       	String[] result = new String[resultTok.countTokens()];
       	for(int i=0;i<result.length;i++)
       		// BECAUSE All Parameters are always sent here in Lower Case!!
    		result[i]=resultTok.nextToken().trim().toLowerCase();
       	
       	return result;
	}

	// Main method for the Implements, returns a JDBC pool name to use for request
	public String getConnectionPoolForRequest(SQLDescriptor reqDescriptor,
											  HashMap<String, Object> reqParams) 
	{
		if("CoreSQL".equalsIgnoreCase(reqDescriptor.getType()))
		{
			Object foundIdVal = null;
			
			// Before everything else, trying to see if there is "hint" about the DB in the params
			foundIdVal = reqParams.get(home_db_hint_param);
			if(foundIdVal!=null)
				return "CoreSQL_"+foundIdVal;
			
			// Attempt to locate customerID in the input params (assumes already fixed by fixParamMap)
			for(String paramName : known_cust_id_params)
				if((foundIdVal=reqParams.get(paramName))!=null)
					break;
			
			// Found customer ID?
			if(foundIdVal!=null)
				return "CoreSQL_"+identifyConnectionPoolByCustomer(foundIdVal);
			
			return null;
		}

		return null;		// Hmmmm.. no idea! You figure it out!
	}

	private String identifyConnectionPoolByCustomer(Object foundIdVal) {
		int firstDigit = (foundIdVal.toString()).charAt(0)-'0';
		return customer_pools[firstDigit-1];		// Almost forgot the shift!!!
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Known Customer Params: "+Arrays.toString(known_cust_id_params)+"\n");
		str.append("Customer First Digit Pools [1 to 9]: "+Arrays.toString(customer_pools)+"\n");
		str.append("Or client can provide "+home_db_hint_param+" parameter with proper DB letter\n");
		return str.toString();
	}

}
