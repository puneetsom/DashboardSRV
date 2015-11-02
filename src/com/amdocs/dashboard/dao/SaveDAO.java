package com.amdocs.dashboard.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.NamingException;

import com.amdocs.dashboard.services.UpdateResult;
import com.amdocs.dashboard.sql.SQLConstants;
import com.amdocs.infra.querycache.QueryCacheServer;
import com.amdocs.infra.utils.ConnectionManager;


/**
 * TODO move to GridService, rename saveData to updateGrid
 */
public class SaveDAO implements SQLConstants
{
	
	private static Logger logger = Logger.getLogger("com.amdocs.dashboard.dao");
	
	public static String CONECTION_POOL_NAME = "CdwSQL";
	
	public Connection getConn(String conn) throws SQLException, NamingException 
	{
		return ConnectionManager.getConnection(conn);
	}
	
	public String getSql(String queryCode) {
		return QueryCacheServer.getInstance().getQueryByCode(queryCode).getQueryString();
	}

	@SuppressWarnings({ "unused", "unchecked" })
	public UpdateResult saveData(List<Map<Object,Object>> paramsList, UpdateResult result) throws Exception 
	{
		
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
				String queryCode = (String)obj.get("queryCode");				
				params = (HashMap<String,Object>)obj.get("params");
				
				sql = getSql(queryCode);
				namedPreStmt = new NamedParameterStatement(con, sql);
				
				if(sql == null) 
				{
					logger.warning("QueryCacheServer: Ignored attempt to activate UNDEFINED query: " + queryCode);
					return null;
				}
				
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
				
				activateSQLInsertOrUpdate(namedPreStmt, sql, params);
				
				// execute query
				count = namedPreStmt.executeUpdate();
				queryProcessTime = System.currentTimeMillis() - startTime;				
			}		
			
			con.commit();
			
			logger.info("Transaction successfully completed.");
			
			result.setStatus(UpdateResult.SUCCESS);
			result.setRowsAffected(count);
			
		} catch (SQLException e){			
			con.rollback();
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			e.printStackTrace();
			logger.severe("SaveDAO - Error while saving " + e.getMessage());			
			
		}catch(Exception e){			
			con.rollback();
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			e.printStackTrace();
			logger.severe("SaveDAO - Error while saving " + e.getMessage());		
			
		}finally {
			if (namedPreStmt != null)
				namedPreStmt.close();
			
			if(con != null)
				con.close();
		}
		
		return result;
	}
		
	public void activateSQLInsertOrUpdate(NamedParameterStatement stmt, 
										   String queryString, 
										   HashMap<String, Object> params) throws Exception {
	
		Set<String> keys = params.keySet();
		Iterator<String> itr = keys.iterator();			
		while(itr.hasNext())
		{
			String paramName = itr.next();
			Object paramValue = params.get(paramName);				
			
			try {
				
				List<Integer> indexes = stmt.getParamMap().get(paramName);
		    	if(indexes != null)				    	
		    		stmt.setParamValue(paramName, paramValue);							
		    	
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(
						"SEVERE: Unrecognized Parameter Type "
								+ paramName + " For Query " + queryString
								+ " - Inner: " + e.getMessage());
			}							
		}	
	}
}
