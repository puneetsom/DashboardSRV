package com.amdocs.dashboard.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import oracle.jdbc.driver.OraclePreparedStatement;

/*
 * @author Sudhir Bhatt
 */
public class NamedParameterStatement 
{
	/** The statement this object is wrapping. */
    private PreparedStatement statement;
    
    private OraclePreparedStatement oracleStmt;
    
    private String queryString;
    
    private StringBuffer combinedSQL;
    
    /** Maps parameter names to arrays of ints which are the parameter indices. 
     */
    private Map<String, List<Integer>> indexMap;

    private Map<String, Integer> indexParamMap;

    /**
     * Creates a NamedParameterStatement.  Wraps a call to
     * c.{@link Connection#prepareStatement(java.lang.String) 
     * prepareStatement}.
     * @param connection the database connection
     * @param query      the parameterized query
     * @throws SQLException if the statement could not be created
     */
    public NamedParameterStatement(Connection connection, String query) throws SQLException 
    {
    	indexMap = new HashMap<String, List<Integer>>();
    	indexParamMap = new HashMap<String, Integer>();
    	queryString = parse(query, indexMap);
    	combinedSQL = new StringBuffer(queryString);
        statement = connection.prepareStatement(queryString);
        //oracleStmt = (OraclePreparedStatement)statement;
    }


    /**
     * Parses a query with named parameters.  The parameter-index mappings are 
     * put into the map, and the
     * parsed query is returned.
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    private String parse(String query, Map<String, List<Integer>> paramMap)
    {
        // I was originally using regular expressions, but they didn't work well 
    	//for ignoring
        // parameter-like strings inside quotes.
        int length = query.length();
        StringBuffer parsedQuery = new StringBuffer(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index=1;

        for(int i = 0; i < length; i++)
        {
            char c = query.charAt(i);
            
            if(inSingleQuote) {
                if(c == '\'') {
                    inSingleQuote = false;
                }
            } else if(inDoubleQuote) {
                if(c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if(c == '\'') {
                    inSingleQuote = true;
                } else if(c == '"') {
                    inDoubleQuote=true;
                } else if(c == ':' && i+1 < length && Character.isJavaIdentifierStart(query.charAt(i+1))) {
                    int j = i + 2;
                    while(j < length && Character.isJavaIdentifierPart(query.charAt(j)))
                    {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter

                    List<Integer> indexList = paramMap.get(name);
                    if(indexList == null) {
                        indexList = new LinkedList<Integer>();
                        paramMap.put(name, indexList);
                    }
                    indexList.add(new Integer(index));
                    index++;
                }
            }
            parsedQuery.append(c);
        }

        // replace the lists of Integer objects with arrays of ints
        /*for(Iterator itr = paramMap.entrySet().iterator(); itr.hasNext();)
        {
            Map.Entry entry=(Map.Entry)itr.next();
            List<Integer> list=(List<Integer>)entry.getValue();
            int[] indexes=new int[list.size()];
            int i=0;
            for(Iterator<Integer> itr2=list.iterator(); itr2.hasNext();) {
                Integer x=itr2.next();
                indexes[i++]=x.intValue();
            }
            entry.setValue(indexes);
        }*/
        //System.out.println(parsedQuery.toString());
        return parsedQuery.toString();
    }

    public String getDebugQueryString() 
    {				
		return combinedSQL.toString();
	}
    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, java.lang.Object)
     */
    @SuppressWarnings("unused")
	private void setObject(String name, Object value) throws SQLException
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setObject(indexes.get(i), value);
	            
	            String paramValue = "'"+value+"'";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, java.lang.String)
     */
    private void setString(String name, String value) throws SQLException 
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setString(indexes.get(i), value);
	            
	            String paramValue = "'"+value+"'";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNull(java.lang.String, int)
     */
    private void setNull(String name) throws SQLException 
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setNull(indexes.get(i), Types.NULL);
	            
	            String paramValue = "null";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    private void setInt(String name, int value) throws SQLException
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setInt(indexes.get(i), value);
	            
	            String paramValue = "'"+value+"'";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    private void setLong(String name, long value) throws SQLException 
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setLong(indexes.get(i), value);
	            
	            String paramValue = "'"+value+"'";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    @SuppressWarnings("unused")
	private void setTimestamp(String name, Timestamp value) throws SQLException 
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setTimestamp(indexes.get(i), value);
	            
	            String paramValue = "'"+value+"'";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }
    
    private void setDate(String name, Date value) throws SQLException 
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setDate(indexes.get(i), value);
	            
	            String paramValue = "'"+value+"'";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }

    private void setDouble(String name, Double value) throws SQLException 
    {
    	List<Integer> indexes = indexMap.get(name);
    	if(indexes != null)
    	{
	        for(int i=0; i < indexes.size(); i++) {
	            statement.setDouble(indexes.get(i), value);
	            
	            String paramValue = "'"+value+"'";
				combinedSQL.replace(combinedSQL.indexOf("?"), combinedSQL.indexOf("?") + 1, paramValue);
	        }
    	}
    }
    
    // Automatically sets parameter by examining 
	public void setParamValue(String paramName, Object val) throws SQLException 
	{
		if(val == null)
			setNull(paramName);
		else if(val instanceof String)
			setString(paramName, (String) val);
		else if(val instanceof Long)
			setLong(paramName, ((Long) val).longValue());
		else if(val instanceof Integer)
			setInt(paramName, ((Integer) val).intValue());
		else if(val instanceof java.util.Date){			
			java.util.Date utilDate = (java.util.Date)val;
			Date sqlDate = new Date(utilDate.getTime());
			setDate(paramName, sqlDate);
			
			//DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
			//setString(paramName, fmt.format(utilDate));
		}else if(val instanceof Character)
			setString(paramName, ((Character) val).toString());
		else if(val instanceof Double)
			setDouble(paramName, ((Double) val).doubleValue());
		else
			System.out.println("SEVERE: Unrecognized parameter type provided from Flex in position(" + paramName + "): "+val.getClass());
	}
	
	
	// Automatically sets parameter by examining 
	public void setOracleParamValue(String paramName, Object val) throws SQLException 
	{
		if (val == null)
			oracleStmt.setNullAtName(paramName, Types.NULL);
		else if(val instanceof String)
			oracleStmt.setStringAtName(paramName, (String) val);
		else if(val instanceof Long)
			oracleStmt.setLongAtName(paramName, ((Long) val).longValue());
		else if(val instanceof Integer)
			oracleStmt.setIntAtName(paramName, ((Integer) val).intValue());
		else if(val instanceof java.util.Date)
			oracleStmt.setDateAtName(paramName, (Date) val);
		else if(val instanceof Character)
			oracleStmt.setStringAtName(paramName, ((Character) val).toString());
		else if(val instanceof Double)
			oracleStmt.setDoubleAtName(paramName, ((Double) val).doubleValue());
		else
			System.out.println("SEVERE: Unrecognized parameter type provided from Flex in position(" + paramName + "): "+val.getClass());
	}

    /**
     * Returns the underlying statement.
     * @return the statement
     */
    public PreparedStatement getStatement() 
    {
        return statement;
    }

   
    public OraclePreparedStatement getOracleStatement() 
    {
        return oracleStmt;
    }
    
    public Map<String, List<Integer>> getParamMap() 
    {
        return indexMap;
    }

    /**
     * Executes the statement.
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException 
    {
        return statement.execute();
    }
    
    /**
     * Executes the statement.
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean executeOracleStmt() throws SQLException 
    {
        return oracleStmt.execute();
    }


    /**
     * Executes the statement, which must be a query.
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException
    {
        return statement.executeQuery();
    }
    
    /**
     * Executes the statement, which must be a query.
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeOracleStmtQuery() throws SQLException
    {
        return oracleStmt.executeQuery();
    }


    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE 
	 * statement;
     * or an SQL statement that returns nothing, such as a DDL statement.
     * @return number of rows affected
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException
    {
        return statement.executeUpdate();
    }
    
    
    public int executeOracleUpdate() throws SQLException
    {
        return oracleStmt.executeUpdate();
    }


    /**
     * Closes the statement.
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    public void close() throws SQLException
    {
        statement.close();
    }
    
    /**
     * Closes the statement.
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    public void closeOracleStmt() throws SQLException
    {
    	oracleStmt.close();
    }


    /**
     * Adds the current set of parameters as a batch entry.
     * @throws SQLException if something went wrong
     */
    public void addBatch() throws SQLException
    {
        statement.addBatch();
    }


    /**
     * Executes all of the batched statements.
     * 
     * See {@link Statement#executeBatch()} for details.
     * @return update counts for each statement
     * @throws SQLException if something went wrong
     */
    public int[] executeBatch() throws SQLException
    {
        return statement.executeBatch();
    }
}
