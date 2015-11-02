package com.amdocs.dashboard.dao.reqrep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.amdocs.dashboard.dao.NamedParameterStatement;
import com.amdocs.dashboard.dao.SaveDAO;
import com.amdocs.dashboard.services.UpdateResult;

import flex.messaging.io.amf.ASObject;

public class RequirementDAO extends SaveDAO 
{
	private static Logger logger = Logger.getLogger("com.amdocs.dashboard.dao.reqrep");
		
	@SuppressWarnings({ "unused", "unchecked" })
	public UpdateResult save(List<Map<Object,Object>> paramsList, Map<String, Object> additionalParams, UpdateResult result) throws Exception 
	{
		Map<String, Object> returnedSqlUpdate = new HashMap<String, Object>();
		
		Connection con = null;		
		NamedParameterStatement namedPreStmt = null;
		int count = 0;
		long queryProcessTime = 0;
		long startTime = System.currentTimeMillis();
		String sql = null;
		int reqId = 0;
		HashMap<String,Object> params = null;
		
		try {			
											
			if(con == null)
			{
				con = getConn(CONECTION_POOL_NAME);
				con.setAutoCommit(false);
			}
			
			for(Map<Object,Object> obj : paramsList)
			{								
				String queryCode = (String)obj.get(QUERY_CODE);				
				params = (HashMap<String,Object>)obj.get(PARAMS);
				
				sql = getSql(queryCode);
				
				if(additionalParams != null && !additionalParams.isEmpty())
					sql = customizeQuery(queryCode, sql, additionalParams);
				
				namedPreStmt = new NamedParameterStatement(con, sql);
				
				if(sql == null) 
				{
					logger.warning("QueryCacheServer: Ignored attempt to activate UNDEFINED query: " + queryCode);
					return null;
				}
				
				if("INSERT_REQ".equalsIgnoreCase(queryCode))
				{
					reqId = selectNextId(con, "SELECT RR_REQUIREMENT_SEQ.NEXTVAL FROM DUAL");
					params.put("requirement_id", reqId);
										
					Set<String> keys = namedPreStmt.getParamMap().keySet();
					Iterator<String> itr = keys.iterator();
					while(itr.hasNext())
					{
						String paramName = itr.next();
						if (!params.containsKey(paramName))
						{
							//System.out.println("Parameter " + paramName + " not found.");
							params.put(paramName, null);
						}
					}
				}
				else if("INSERT_LINK".equalsIgnoreCase(queryCode))	{
					
					int linkId = selectNextId(con, "SELECT RR_LINK_SEQ.NEXTVAL FROM DUAL");
					params.put("link_id", linkId);
					if(params.containsKey("requirement_id") && reqId != 0)
						params.put("requirement_id", reqId);
					
				}
				
				activateSQLInsertOrUpdate(namedPreStmt, sql, params);
				
				//System.out.println(namedPreStmt.getDebugQueryString());
				// execute query
				count = namedPreStmt.executeUpdate();
				queryProcessTime = System.currentTimeMillis() - startTime;				
			}		
			
			con.commit();
			
			//logger.info("Transaction successfully completed.");
			
			result.setStatus(UpdateResult.SUCCESS);
			result.setRowsAffected(count);
			
			if(additionalParams != null && !additionalParams.isEmpty())
			{
				Object[] additonalParamsList = (Object[])additionalParams.get("updatableColumns");
				for(Object obj : additonalParamsList)
				{
					ASObject column = (ASObject)obj;
					result.addMessage("'" + column.get("headerLabel").toString() + "' updated.");					
				}
			}
				
			returnedSqlUpdate.put("requirement_id",	params.get("requirement_id"));
			result.setReturnObj(returnedSqlUpdate);
			
		} catch (SQLException e){			
			con.rollback();
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			logger.severe("RequirementDAO - Error while saving " + e.getMessage());			
			
		}catch(Exception e){			
			con.rollback();
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			logger.severe("RequirementDAO - Error while saving " + e.getMessage());		
			
		}finally {
			if (namedPreStmt != null)
				namedPreStmt.close();
			
			if(con != null)
				con.close();
		}
		
		return result;
	}	
	
	
	private static String customizeQuery(String queryCode, String query, Map<String, Object> additionalParams) throws Exception
	{
		Object[] additonalParamsList = (Object[])additionalParams.get("updatableColumns");
		StringBuffer colToBeUpdated = new StringBuffer();
		int count = 0;
		for(Object obj : additonalParamsList)
		{
			ASObject column = (ASObject)obj;
			String colType = column.get(COL_TYPE).toString();
			if(count > 0)
				colToBeUpdated.append(COMMA + SPACE);
			
			if(COL_TYPE_DATE.equalsIgnoreCase(colType))
			{
				String dateTimePattern = (String) column.get(DATE_TIME_PATTERN);
				if (dateTimePattern == null)
					dateTimePattern = "MM/DD/YYYY";
				
				colToBeUpdated.append(column.get(DATAFIELD) + " = to_date(:" + column.get(DATAFIELD) + COMMA + SINGLE_QUOTES + dateTimePattern + "')");
			}
			else
				colToBeUpdated.append(column.get(DATAFIELD) + " = :" + column.get(DATAFIELD));
			
			count++;
		}
		
		StringBuffer preparedSQL = new StringBuffer(query);
		int paramFound = -1;
		int fromIndex = 1;
		//do{
		paramFound = preparedSQL.indexOf("[", fromIndex);		
		if(paramFound > -1)
		{
			int endOfParam = preparedSQL.indexOf("]", paramFound);
			if(endOfParam==-1)
				throw new Exception("Invalid Query Parameters detected for query "+ queryCode + " - Unterminated ] curly braces");
			String foundParam;
			try
			{
				foundParam = preparedSQL.substring(paramFound + 1, endOfParam);
				if(foundParam == null)
					throw new Exception("Invalid Query Parameters detected for query "+ queryCode + " - null parameter name");
				foundParam = foundParam.trim();
				if(foundParam.length() == 0)
					throw new Exception("Invalid Query Parameters detected for query "+ queryCode + " - empty parameter name");
			}
			catch(Throwable e)
			{
				throw new Exception("Invalid Query Parameters detected for query " + queryCode +  " Check for empty parameters in curly braces");
			}

			// No space within parameter name allowed!
			if(foundParam.indexOf(' ') > -1)
				throw new Exception("Invalid Query Parameter detected for query " + queryCode + " No SPACE in parameter name allowed: "+foundParam);

			// Ok, final step, replacing with Question Mark and Saving param name for later use
			preparedSQL.replace(paramFound, endOfParam + 1, colToBeUpdated.toString());		
		}
		//} while(paramFound>-1 && fromIndex < preparedSQL.length());
		
		return preparedSQL.toString();
	}

	private int selectNextId(Connection connection, String sqlCode) throws SQLException
	{
		String sql = null;
		PreparedStatement stmt = null;
		ResultSet rslt = null;
		int reqId = 0;

		try {
			
			stmt = connection.prepareStatement(sqlCode);
        	
			rslt = stmt.executeQuery();
			
			if (rslt.next()){
				reqId = rslt.getInt(1);
			}

		} catch (SQLException e) {
			logger.severe("sql: " + sql);
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("\n" + sql);
		} finally {
			rslt.close();
			stmt.close();
			//connection.close();
		} 
		return reqId;
	}
	
}
