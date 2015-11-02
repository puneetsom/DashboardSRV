/**
 * Created on Dec 24, 2008
 * 
 * Amdocs - Copyright (C) 2008
 * 
 * The purpose of this class is to load Queries from the XML file. 
 * The Queries can also be actual EJB calls to existing ACRM and other Beans
 * 
 * Author: Yan Spevak
 * Supervisor: Adi Rabinovich
 
 * *********************************
 * Change:  #1
 * Programmer:  Sapna Chhajed
 * Supervisor:  Mattthew Hill
 * Description: Changes to support SQL contains the "in" condition.
 *              The varibale of the in condition should be with special sign in the XML file like below:
 *              	- {{variableName}}
 *              The "variableName" should contain the list we are searching for as a string separated with "," sign. 
 * *********************************
 * 
 */
package com.amdocs.infra.utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.xml.sax.SAXNotRecognizedException;

import com.amdocs.dashboard.sql.SQLBuilder;
import com.amdocs.infra.datatypes.MagicType;
import com.amdocs.infra.datatypes.MagicTypeDescriptor;
import com.amdocs.infra.querycache.QueryCacheServer;

public class SQLDescriptor implements Cloneable, Comparable<SQLDescriptor> 
{
	// Code for this query, really important
	String code;
	
	// Code for this query, really important
	//Default to true in case the dtd is not checked to set the value
	Boolean paramsRequired = true;

	// Object Alias to map to the Client object name which should store this query. Can be empty for anonymous
	String clientObject;
	// SQL itself, with embedded parameters in the {param_name} format
	String queryString;
	String queryStringOriginal;//#1

	// Query type, Defaults to "Ref", but can be different for Non-SQL queries 
	String type = "RefSQL";
	// NEW! For debugging, you can activate "trace" on a query, it will print out each request to it!
	boolean debug = true;
	// Array of parameter names to match the question marks in the queryString
	ArrayList<String> paramNames = new ArrayList<String>(); 

	//Start #1
	ArrayList<String> paramNamesPosition = new ArrayList<String>();
	int paramPosition;
	//End #1

	public SQLDescriptor()
	{
	}

	public SQLDescriptor(String iCode, String iClientObject, String iQueryString, String iType)
	{
		code=iCode;
		clientObject=iClientObject;
		queryString=iQueryString;
		type=iType;
	}
	
	// Method called by XML Parser to load attribute by attribute -- rather ugly, I know, but no choice 
	// because parser itself is case sensitive and I wanted the attributes not to be case sensitive
	public void setAttribute(String attrName, String attrValue) throws SAXNotRecognizedException
	{
		if(attrName.equalsIgnoreCase("code"))
			setCode(attrValue);
		else if("paramsRequired".equalsIgnoreCase(attrName))
			setParamsRequired(attrValue);
		else if(attrName.equalsIgnoreCase("clientObject"))
			setClientObject(attrValue);
		else if(attrName.equalsIgnoreCase("type"))
		{
			if(!("Default".equalsIgnoreCase(attrValue)))
				setType(attrValue);
		}
		else if(attrName.equalsIgnoreCase("debug"))
			setDebug(attrValue);
		else if(attrName.equalsIgnoreCase("description"))
			return;		// Ignored!
		else
			throw new SAXNotRecognizedException("FATAL: Unknown attribute "+attrName+" specified for query "+code+"!");
		
	}

	public void setDebug(String attrValue) {
		if(attrValue==null || attrValue.length()==0)
			return;
		attrValue = attrValue.toLowerCase();
		if(attrValue.charAt(0)=='y' || attrValue.equals("true"))
			debug=true;
	}

	/**
	 * Call this method after parsing the SQL Descriptor, to confirm that all mandatory properties
	 * are set correctly, the Type is recognized and that integer things are integer
	 * 
	 * @throws SAXNotRecognizedException
	 */
	public void validate()
		throws SAXNotRecognizedException
	{
		// Check missing things first
		if(queryString==null || queryString.length()==0)
			throw new SAXNotRecognizedException("FATAL: Invalid Query Definition Detected - Query Cannot Be Empty for query "+code+"!");
		if(code==null || code.length()==0)
			throw new SAXNotRecognizedException("FATAL: Invalid Query Definition Detected - Code Attribute is Missing or Empty for query "+code+"!");
	
		// Now to trickier rules
		if("Default".equalsIgnoreCase(type))
			throw new SAXNotRecognizedException("FATAL: Invalid Query Definition Detected - Type Attribute cannot be 'Default' for "+code+"!");

		// Final load step, convert the {} parts of the SQL into ? (question marks) and prepare matching params array
		processSQLParams();
	}
	
	public boolean isBean() {
		return (getType()!=null && getType().endsWith("Bean")); 
	}
	
	
	
	public List<Map<String,Object>> activateExcelSQLQuery(HashMap<String,Object> params, HashMap<String, Object> additionalParams, ConnectionPoolHelper poolHelper) throws SQLException
	{
		queryString = queryStringOriginal;
		
		if(params!=null && params.size()>0)
			params = fixParamMap(params);
		
		reorganiseParamNamesForInClause(params);		
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		
		Connection c = null;
		PreparedStatement prepSql = null;
		ResultSet results = null;
		
		String connectionPoolName = null;
		String customizeQueryCode = null;
		ArrayList<String> addtionalParamNames = new ArrayList<String>();
		
		try {
			connectionPoolName = (poolHelper!=null) ? poolHelper.getConnectionPoolForRequest(this, params) : null;

			if(connectionPoolName==null)
				connectionPoolName = getType();

			c = ConnectionManager.getConnection( connectionPoolName );
			
			customizeQueryCode = getCode() + SQLBuilder.getCustomizeQueryCode(additionalParams);
			
			if(additionalParams != null && !additionalParams.isEmpty())
				setQueryString(SQLBuilder.customizeQuery(addtionalParamNames, params, getQueryString(), additionalParams));
			
			
			if(isDebug())
			{
				System.out.println("----------- " + new java.util.Date() + " [ Executing: "+customizeQueryCode+" ]---------------------------");
				//System.out.println(getQueryString());
			}
				
			
			prepSql = c.prepareStatement(getQueryString());

			// Now prepare the parameters!
			int i=0;
			for(; i < paramNames.size(); i++)
			{
				String paramName = paramNames.get(i);
				Object paramValue = params.get(paramName.toLowerCase());
				if(paramValue == null && isParamsRequired())
				{
					// Handling SPECIAL param (run_date)
					if(paramName.equalsIgnoreCase("run_date"))
					{
						prepSql.setDate(i+1, new Date(System.currentTimeMillis()));
					} else
					{
						//QueryCacheServer.getInstance().logger.warning("WARNING (QueryCache): MISSING PARAMETER "+paramName+" FOR QUERY "+getCode());
						throw new Exception("SEVERE (QueryCache): MISSING PARAMETER "+paramName+" FOR QUERY "+customizeQueryCode );
					}
				} else
				{
					try {
						setParamValue(prepSql, i+1, paramValue);
					} 
					catch (Exception e) {
						throw new Exception("SEVERE: Unrecognized Parameter Type "+paramName+" For Query "+customizeQueryCode+" - Inner: "+e.getMessage()); 
					}
				}
			}
			
			for (int j = 0; j < addtionalParamNames.size(); j++)
			{
				String paramName = addtionalParamNames.get(j);
				Object paramValue = params.get(paramName.toLowerCase());
				setParamValue(prepSql, i + j + 1, paramValue);			
			}
			
			// Finally, ready to execute query (all parameters in-place)
			results = prepSql.executeQuery();

			list = getListMap(results, true);
			
		}catch(Throwable e) {
			StringBuffer str = new StringBuffer();
			str.append("Code: " + getCode()+" FAILED TO EXECUTE on Connection Pool "+connectionPoolName);
			str.append("\n" + getDebugQueryString(params));
			str.append("\nException: ");
			
			QueryCacheServer.getInstance().logger.log(Level.SEVERE, str.toString(), e);
		} 
		finally {
			if(results!=null)
				results.close();
			if(prepSql!=null)
				prepSql.close();
			c.close();

			if(isDebug())
				System.out.println("-----------[ Raw Data for Excel: "+getCode()+" loaded "+list.size()+" records]------------");			
		}

		return list;		
	}	
	
		
	protected List<Map<String,Object>> getListMap(ResultSet rslt, boolean includeNulls) throws SQLException
	{
		List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
		
		ResultSetMetaData md = rslt.getMetaData() ;
		int columnCnt = md.getColumnCount();
		int[] types = new int[columnCnt];
		String[] labels = new String[columnCnt];
		for(int i = 1; i <= columnCnt; i++)
		{
			types[i-1] = md.getColumnType(i);
			labels[i-1] = md.getColumnLabel(i).toLowerCase();
		}
		
		while (rslt.next()) {
			Map<String,Object> row = new HashMap<String, Object>();
			
	        for( int i = 1; i <= columnCnt; i++) {
	        	Object obj = null;
	        	switch(types[i-1])
	    		{
	    			case Types.CHAR:
	    			case Types.VARCHAR: 
	    				obj = rslt.getString(i);
	    				break;
	    			case Types.TIMESTAMP:
	    			case Types.DATE:
	    				obj = rslt.getTimestamp(i);
	    				break;
	    			case Types.SMALLINT:
	    			case Types.INTEGER:
	    				obj = rslt.getInt(i);
	    				break;
	    			case Types.REAL:
	    			case Types.FLOAT:
	    				obj = rslt.getFloat(i);
	    				break;
	    			case Types.NUMERIC:
	    			case Types.DECIMAL:
	    			case Types.DOUBLE:
	    				obj = rslt.getDouble(i);
	    				break;
	    			case Types.BOOLEAN:
	    				obj = rslt.getBoolean(i);
	    				break;
	    			case Types.BIGINT:
	    				obj = rslt.getBigDecimal(i);
	    				break;
	    			default:
	    				obj = rslt.getObject(i);
	    		}
	        		        	
	        	if (includeNulls || !rslt.wasNull())
	        		row.put(labels[i-1], obj);
	        }
	        	
			rows.add(row);
		}
		
		return rows;
	}
	
	
	public List<MagicType> activateSQLQuery(HashMap<String,Object> params, HashMap<String, Object> additionalParams, ConnectionPoolHelper poolHelper)
				throws SQLException 
	{
		queryString = queryStringOriginal; //mateo - always set query back to the original.
		
		if(params!=null && params.size()>0)	// Don't crash for parameterless queries
			params = fixParamMap(params);	// Fix the hashmap for "sure-fire" lookup mode
		
		reorganiseParamNamesForInClause(params); //#1
		
		List<MagicType> list = new ArrayList<MagicType>();
		Connection c = null;
		PreparedStatement prepSql = null;
		ResultSet results = null;
		long queryProcessTime = 0;
		long magicLoadProcessTime = 0;

		String connectionPoolName = null;
		String customizeQueryCode = null;
		ArrayList<String> addtionalParamNames = new ArrayList<String>();
		
		try {
			connectionPoolName = (poolHelper!=null) ? poolHelper.getConnectionPoolForRequest(this, params) : null;

			if(connectionPoolName==null)
				connectionPoolName = getType();

			c = ConnectionManager.getConnection( connectionPoolName );
			
			long startTime = System.currentTimeMillis();
			
			customizeQueryCode = getCode() + SQLBuilder.getCustomizeQueryCode(additionalParams);
			
			if(additionalParams != null && !additionalParams.isEmpty())
				setQueryString(SQLBuilder.customizeQuery(addtionalParamNames, params, getQueryString(), additionalParams));
			
			
			if(isDebug())
			{
				System.out.println("----------- " + new java.util.Date() + " [ Executing: "+customizeQueryCode+" ]---------------------------");
				//System.out.println("--------------[ DEBUG ACTIVE For Query: "+getCode()+" ]---------------------------");
				//System.out.println(getDebugQueryString(params));
				//System.out.println(getDebugQueryString(additionalParams));
				//System.out.println(getQueryString());
				//System.out.println("------------[ END ACTIVE DEBUG For Query: "+getCode()+" ]---------------------------");
			}
			
			prepSql = c.prepareStatement(getQueryString());

			// Now prepare the parameters!
			int i=0;
			for(; i < paramNames.size(); i++)
			{
				String paramName = paramNames.get(i);
				Object paramValue = params.get(paramName.toLowerCase());
				if(paramValue == null && isParamsRequired())
				{
					// Handling SPECIAL param (run_date)
					if(paramName.equalsIgnoreCase("run_date"))
					{
						prepSql.setDate(i+1, new Date(System.currentTimeMillis()));
					} else
					{
						//QueryCacheServer.getInstance().logger.warning("WARNING (QueryCache): MISSING PARAMETER "+paramName+" FOR QUERY "+getCode());
						throw new Exception("SEVERE (QueryCache): MISSING PARAMETER "+paramName+" FOR QUERY "+customizeQueryCode );
					}
				} else
				{
					try {
						setParamValue(prepSql, i+1, paramValue);
					} 
					catch (Exception e) {
						throw new Exception("SEVERE: Unrecognized Parameter Type "+paramName+" For Query "+customizeQueryCode+" - Inner: "+e.getMessage()); 
					}
				}
			}
			
			for (int j = 0; j < addtionalParamNames.size(); j++)
			{
				String paramName = addtionalParamNames.get(j);
				Object paramValue = params.get(paramName.toLowerCase());
				setParamValue(prepSql, i + j + 1, paramValue);			
			}
			
			// Finally, ready to execute query (all parameters in-place)
			results = prepSql.executeQuery();

			queryProcessTime = System.currentTimeMillis() - startTime;		// For instrumentation and debug

			String destClassName = (getClientObject()==null || getClientObject().length()==0) ? getCode() : getClientObject();

			startTime = System.currentTimeMillis(); //reset start time																		
																		
			// Load "Magic Type Descriptor one time ahead of everything -- makes everything faster!
			MagicTypeDescriptor resultsDesc = MagicTypeDescriptor.loadMetaDataForMagicType(customizeQueryCode, destClassName, results);

			// Query executed! Read the results!
			while (results.next())
				list.add(new MagicType(results, resultsDesc));

			magicLoadProcessTime = System.currentTimeMillis() - startTime;		// For instrumentation and debug
			
		} 
		catch(Throwable e) {
			StringBuffer str = new StringBuffer();
			str.append("Code: " + getCode()+" FAILED TO EXECUTE on Connection Pool "+connectionPoolName);
			str.append("\n" + getDebugQueryString(params));
			str.append("\nException: ");
			
			QueryCacheServer.getInstance().logger.log(Level.SEVERE, str.toString(), e);
		} 
		finally {
			if(results!=null)
				results.close();
			if(prepSql!=null)
				prepSql.close();
			c.close();

			if(isDebug() && queryProcessTime > 0)
			{
				System.out.println("-----------[ Query: "+getCode()+" finished in "+queryProcessTime+" ms ]-----------");
				System.out.println("-----------[ Magic: "+getCode()+" loaded "+list.size()+" records in "+magicLoadProcessTime+" ms ]------------");
			}
		}

		return list;
	}

	// Automatically sets parameter by examining 
	private void setParamValue(PreparedStatement prepSql, int i, Object val) throws SQLException 
	{
		if(val == null)
			prepSql.setNull(i, Types.NULL);
		else if(val instanceof String)
			prepSql.setString(i, (String) val);
		else if(val instanceof Long)
			prepSql.setLong(i, ((Long) val).longValue());
		else if(val instanceof Integer)
			prepSql.setInt(i, ((Integer) val).intValue());
		else if(val instanceof Date)
			prepSql.setDate(i, (Date) val);
		else if(val instanceof Character)
			prepSql.setString(i, ((Character) val).toString());
		else if(val instanceof Float)
			prepSql.setFloat(i, ((Float)val).floatValue());
		else if(val instanceof Double)
			prepSql.setDouble(i, ((Double) val).doubleValue());
		else
			System.out.println("SEVERE: Unrecognized parameter type provided from Flex in position("+i+"): "+val.getClass());
	}

	//Start #1
	/**
	 * This method reorganiseParamNames organises the query parameters
	 *  if the query has IN condition in Where clause
	 *  
	 * @param params
	 */
	private void reorganiseParamNamesForInClause(HashMap<String, Object> params)
	{
		ArrayList<String> tmpParamNamesPosition = new ArrayList<String>();

		paramPosition = 0;

		// Reorganise the paramNames in case we have "in" condition in the SQL statement
		
		boolean hasInCondition = false;
		
		for (int h = 0; h < paramNamesPosition.size(); h++) 
		{
			String tmpName = paramNamesPosition.get(h).toLowerCase();
			
			//If we have a special "IN" parameter.
			if (paramNamesPosition.get(h).startsWith("^") && paramNamesPosition.get(h).endsWith("^"))
			{
				tmpName = paramNamesPosition.get(h).substring(1, paramNamesPosition.get(h).length() - 1).toLowerCase();
				hasInCondition = true;
			}

			String tmpOrigin = tmpName;
			Object paramValue = null;
			paramValue = params.get(tmpName);
			int tmpIndex = 0;

			while (paramValue != null)
			{
				tmpParamNamesPosition.add(paramPosition++, tmpName);
				tmpIndex++;
				tmpName = tmpOrigin + tmpIndex;
				paramValue = params.get(tmpName);
			}
		}

		if (hasInCondition)
		{
			if (!paramNames.isEmpty())
				paramNames.clear();
			
			//Make sure the query doen't have any ^^ left in it. The only reason it would
			//is if there is an IN clause with only 1 value.
			try
			{
				finalizeParams();
			}
			catch(SAXNotRecognizedException e)
			{
				System.out.println("Reprocessing the SQL params Failed...see the below reason:");
				System.out.println(e.getMessage());
			}

			for (int n = 0; n < tmpParamNamesPosition.size(); n++) {
				paramNames.add(n, tmpParamNamesPosition.get(n));
			}
		}
	}
	//End Start#1
	
	// Simple internal method to create map where all parameter names are lower-case (for easy matching)
	private HashMap<String,Object> fixParamMap(HashMap<String,Object> params)
	{
		HashMap<String,Object> fixed = new HashMap<String,Object>();
		Iterator<Map.Entry<String, Object>> i = params.entrySet().iterator();

		String keyColumn = ""; //Change #1
		
		while(i.hasNext())
		{
			int phaseNum = 0;
			int iteratorNum=0;
			Map.Entry<String,Object> param = i.next();
			
			//Start #1
			StringTokenizer st = new StringTokenizer(param.getValue().toString(),",");
			while (st.hasMoreTokens())
			{
				phaseNum++;
				st.nextElement();
			}

			if (phaseNum > 1 || param.getValue().toString().endsWith(","))
			{
				keyColumn = param.getKey();
			}

			if (param.getKey().equals(keyColumn))
			{
				st = new StringTokenizer(param.getValue().toString(),",");
				while (st.hasMoreTokens())
				{
					if (iteratorNum==0)
					{
						try
						{
							reprocessSQLParams(phaseNum , keyColumn);
						}
						catch(SAXNotRecognizedException e)
						{
							System.out.println("Reprocessing the SQL params Failed...see the below reason:");
							System.out.println(e.getMessage());
						}

						param.setValue(st.nextToken());
						fixed.put(param.getKey().toLowerCase(), param.getValue());
						iteratorNum++;
					}
					else
					{
						param.setValue(st.nextToken());
						fixed.put(keyColumn.toLowerCase()+iteratorNum, param.getValue());
						iteratorNum++;
					}
				}
			}
			else
				fixed.put(param.getKey().toLowerCase(), param.getValue()); 
		}

		/*	
		while(i.hasNext())
		{
			Map.Entry<String,Object> param = i.next();
			fixed.put(param.getKey().toLowerCase(), param.getValue()); 
		}
		 */		

		return fixed;
	}
	
	// ------- DEFAULT -------- Setters and Getters
	public Boolean isParamsRequired() {
		return paramsRequired;
	}

	public void setParamsRequired(String value) {
		this.paramsRequired = new Boolean(value.trim());
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code.trim();
	}

	public String getClientObject() {
		return clientObject;
	}

	public void setClientObject(String clientObject) {
		this.clientObject = clientObject.trim();
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString.trim();
	}
	
	public String getDebugQueryString(HashMap<String,Object> params) {
		StringBuffer combinedSQL = new StringBuffer(getQueryString());
		
		if (params == null || params.size() == 0)
			return combinedSQL.toString();
		
		// Now prepare the parameters!
		for(String paramName : paramNames)
		{
			Object paramValue = params.get(paramName.toLowerCase());
			if(paramValue==null)
			{
				// Handling SPECIAL param (run_date)
				if(paramName.equalsIgnoreCase("run_date"))
				{
					paramValue = "TO_DATE('"+(new Date(System.currentTimeMillis())).toString()+"','yyyy-mm-dd')";
				} else
				{
					System.out.println("WARNING (QueryCache DEBUG MODE): MISSING PARAMETER "+paramName+" FOR QUERY "+getCode());
					return null;		// FAILURE
				}
			}
			else
				paramValue = "'"+paramValue+"'";
			
			int findQ = combinedSQL.indexOf("?");
			combinedSQL.replace(findQ, findQ+1, paramValue.toString());
		}
		
		return combinedSQL.toString();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type.trim();
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(code);
		str.append(" - ");
		str.append(type);
		str.append(" - Client Object [");
		str.append(clientObject);
		str.append("]\n");
		str.append(queryString);
		str.append('\n');
		str.append("SQL PARAMETER NAMES: ");
		Iterator<String> i=paramNames.iterator();
		while(i.hasNext())
			str.append(i.next()+((i.hasNext()) ? "," : ""));
		return str.toString();
	}

	public int compareTo(SQLDescriptor other) {
		return code.compareTo(other.code);
	}
	
	// Used to generate synchronization string to send to Client to ensure things are in-sync
	public String toSynchStr() {
		StringBuffer ret = new StringBuffer(30);
		ret.append(code);
		ret.append('~');
		ret.append(queryString.hashCode());
		return ret.toString();
	}

	// Used internally to parse the SQL during the load process, to separate parameters and the query
	private void processSQLParams()
		throws SAXNotRecognizedException 
	{
		// Parse query and prepare matching parameters array
		StringBuffer preparedSQL = new StringBuffer(queryString);
		
		int fromIndex = 1;
		int paramFound = -1;
		int specialParamFound = -1;
		do
		{
			paramFound = preparedSQL.indexOf("{", fromIndex);
			//Start #1
			if(paramFound > -1)
			{
				specialParamFound = preparedSQL.indexOf("{{", fromIndex);

				if (specialParamFound == -1 ) specialParamFound=999999;

				if (specialParamFound <= paramFound)
				{
					int endOfSpecialParam = preparedSQL.indexOf("}}", specialParamFound);
					
					if(endOfSpecialParam==-1)
						throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" - Unterminated {{ double curly braces");

					String foundSpecialParam;
					try
					{
						foundSpecialParam = preparedSQL.substring(specialParamFound+2, endOfSpecialParam);
						if(foundSpecialParam==null)
							throw new Exception();
						foundSpecialParam = foundSpecialParam.trim();
						if(foundSpecialParam.length()==0)
							throw new Exception();
					} catch(Throwable e)
					{
						throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" Check for empty parameters in double curly braces");
					}
					
					// No space within parameter name allowed!
					if(foundSpecialParam.indexOf(' ')>-1)
						throw new SAXNotRecognizedException("Invalid Query Parameter detected for query "+getCode()+" No SPACE in parameter name allowed: "+foundSpecialParam);

					// Final step, replacing with Question Mark and Saving param name for later use
					preparedSQL.replace(specialParamFound, specialParamFound+2, "^^");
					preparedSQL.replace(endOfSpecialParam, endOfSpecialParam+2, "^^");
					this.paramNamesPosition.add("^"+foundSpecialParam+"^");
					//End #1	
				}
				else
				{
					int endOfParam = preparedSQL.indexOf("}", paramFound);
					if(endOfParam==-1)
						throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" - Unterminated { curly braces");
					String foundParam;
					try
					{
						foundParam = preparedSQL.substring(paramFound+1, endOfParam);
						if(foundParam==null)
							throw new Exception();
						foundParam = foundParam.trim();
						if(foundParam.length()==0)
							throw new Exception();
					}
					catch(Throwable e)
					{
						throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" Check for empty parameters in curly braces");
					}

					// No space within parameter name allowed!
					if(foundParam.indexOf(' ')>-1)
						throw new SAXNotRecognizedException("Invalid Query Parameter detected for query "+getCode()+" No SPACE in parameter name allowed: "+foundParam);

					// Ok, final step, replacing with Question Mark and Saving param name for later use
					preparedSQL.replace(paramFound, endOfParam+1, "?");
				
					this.paramNamesPosition.add(foundParam); //Change #1
					this.paramNames.add(foundParam);
				}
			}
		} while(paramFound>-1 && fromIndex < preparedSQL.length());

		// Final step, put the prepared SQL back into "this"
		queryString = preparedSQL.toString();
		queryStringOriginal = queryString; // Change #1 - keep the original query string.
	}

	//Start #1
	/**
	 * This method reprocessSQLParams process the query parameters
	 *  if the query has IN condition in Where clause
	 * @param phaseNum
	 * @throws SAXNotRecognizedException
	 * 
	 * 01/13/2012 - parichab - Fix for defect in ST v730: Defect #29267 - ITIC233 - Internet Operations Dashboard - Regions Chart
	 * Added argument for String keyColumn in this method , to decide for which parameter from query 
	 * its required to use replace by ? and at correct position in the preparedSQL .
	 * In the query IN clause , if the input has more than one value this method will be used to parse it.
	 * For example : product in ({{product}}) , i/p value for product is (1,2,3,4...)
	 * Previously this method used to parse/replace ? only on the first parameter from the qry with IN clause , irrespective of 
	 * further such parameters in qry , even if the first parameter has only one value as i/p. Now it will use parameter/column name.
	 */
	private void reprocessSQLParams(int phaseNum , String keyColumn) throws SAXNotRecognizedException 
	{
		// Parse query and prepare matching parameters array
		StringBuffer preparedSQL = new StringBuffer(queryString);
		
		int fromIndex = 1;
		int paramFound = -1;
		int specialParamFound = -1;
		String foundSpecialParam="";
		
		// If input in IN parameter is (4,5) it will change to  ^^4^^, ^^5^^ 
		paramFound = preparedSQL.indexOf("(^^"+keyColumn, fromIndex)+1;
		
		while (paramFound > -1)   //mateo - changed from if to while to process all occurences of this param.
		{
			specialParamFound = preparedSQL.indexOf("^", paramFound+1);
			if (specialParamFound>-1)
			{
				int endOfSpecialParam = preparedSQL.indexOf("^", specialParamFound + 1);
				endOfSpecialParam = preparedSQL.indexOf("^", endOfSpecialParam+1);
			
				if(endOfSpecialParam==-1)
					throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" - Unterminated ?? curly bracers");

				try
				{
					foundSpecialParam = preparedSQL.substring(specialParamFound+1, endOfSpecialParam-1);

					if(foundSpecialParam==null)
						throw new Exception();
					foundSpecialParam = foundSpecialParam.trim();
					if(foundSpecialParam.length()==0)
						throw new Exception();
				}
				catch(Throwable e)
				{
					throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" Check for empty parameters in curly bracers");
				}

				// No space within parameter name allowed
				if(foundSpecialParam.indexOf(' ')>-1)
					throw new SAXNotRecognizedException("Invalid Query Parameter detected for query "+getCode()+" No SPACE in parameter name allowed: "+foundSpecialParam);
				
				// Final step, replacing with Question Mark and Saving param name for later use
				preparedSQL.replace(specialParamFound-1, endOfSpecialParam+1, "?");
				this.paramNames.add(foundSpecialParam);
				int indexOfQuestionMark = preparedSQL.indexOf("?",specialParamFound-1);
			
 				int numberOfTokens = phaseNum - 1;

 				for(int l = 1; l <= numberOfTokens; l++)
				{
					preparedSQL.insert(indexOfQuestionMark+1,",?");
					indexOfQuestionMark = preparedSQL.indexOf("?",specialParamFound-1);
					this.paramNames.add(foundSpecialParam+l);
				}
			}

			//mateo - if there are other instances of this param, process them.
			if(preparedSQL.indexOf(foundSpecialParam, specialParamFound) > -1)
			{
				// v760 ST defect 30434
				// this code is changed to make sure it is finding index of correct IN clause param
				// i.e. , next foundSpecialParam in the sql string if any
				paramFound = preparedSQL.indexOf("^^"+foundSpecialParam, specialParamFound); //change 'em all  
			}
			else
			{
				break;
			}
		}
		queryString = preparedSQL.toString();
	}
	//End #1

	/**
	 * Function to remove any left over IN params.
	 * 
	 */
	private void finalizeParams() throws SAXNotRecognizedException 
	{
		// Parse query and prepare matching parameters array
		StringBuffer preparedSQL = new StringBuffer(queryString);
		
		int fromIndex = 1;
		int paramFound = -1;
		int specialParamFound = -1;
		String foundSpecialParam="";

		paramFound = preparedSQL.indexOf("^", fromIndex);
	
		while (paramFound > -1)
		{
			specialParamFound = preparedSQL.indexOf("^", paramFound+1);
			if (specialParamFound>-1)
			{
				int endOfSpecialParam = preparedSQL.indexOf("^", specialParamFound + 1);
				endOfSpecialParam = preparedSQL.indexOf("^", endOfSpecialParam+1);
		
				if(endOfSpecialParam==-1)
					throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" - Unterminated ?? curly bracers");

				try
				{
					foundSpecialParam = preparedSQL.substring(specialParamFound+1, endOfSpecialParam-1);

					if(foundSpecialParam==null)
						throw new Exception();
					foundSpecialParam = foundSpecialParam.trim();
					if(foundSpecialParam.length()==0)
						throw new Exception();
				}
				catch(Throwable e)
				{
					throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" Check for empty parameters in curly bracers");
				}

				//No space within parameter name allowed
				if(foundSpecialParam.indexOf(' ')>-1)
					throw new SAXNotRecognizedException("Invalid Query Parameter detected for query "+getCode()+" No SPACE in parameter name allowed: "+foundSpecialParam);
			
				// Final step, replacing with Question Mark
				preparedSQL.replace(specialParamFound-1, endOfSpecialParam+1, "?");
			}

			paramFound = preparedSQL.indexOf("^", specialParamFound); //change 'em all  
		}

		queryString = preparedSQL.toString();	
	}
	

	public boolean isDebug() {
		return debug;
	}	
}
