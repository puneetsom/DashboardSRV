package com.amdocs.dashboard.dao;

import com.amdocs.dashboard.services.UpdateResult;
import com.amdocs.dashboard.sql.SQLConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.amdocs.dashboard.sql.SQLBuilder;
import com.amdocs.infra.utils.ConnectionManager;

/**
 * @author Sudhir Bhatt
 *
 */
public class ReqRepositoryDAO implements SQLConstants {
	private static Logger logger = Logger.getLogger("com.amdocs.dashboard.dao");
	// Query type, Defaults to "CdwSQL", but can be different for Non-SQL queries 
	private static String CONNECTION_POOL_NAME = new String("CdwSQL");
		
	@SuppressWarnings("unchecked")
	public UpdateResult updateRequirement(Map<Object, Object> parms) throws SQLException
	{
		int updated = 0;
        Connection conn = null;      
        PreparedStatement pstmt = null;
        long queryProcessTime = 0;
        Map<Object, Object> queryMap = SQLBuilder.buildUpdateSql(parms);	      	
        
        Map<String, Object> returnedSqlUpdate = new HashMap<String, Object>();
        UpdateResult result = new UpdateResult();
        
        try
		{			
			long startTime = System.currentTimeMillis();
        	conn = ConnectionManager.getConnection(CONNECTION_POOL_NAME);
        	
        	int col = 1;
            pstmt = conn.prepareStatement((String)queryMap.get(SQL_QUERY));
            
            List<Object> colToUpdateVal = (List<Object>)queryMap.get(COLUMN_TO_UPDATE);
            List<Object> keyColVal = (List<Object>)queryMap.get(KEY);
            
            
            for(Object obj : colToUpdateVal){ 
            	
            	if(obj instanceof java.util.Date){
            		java.util.Date utilDate = (java.util.Date)obj;
            		pstmt.setDate(col++, new java.sql.Date(utilDate.getTime()));            		
            	}else{
            		pstmt.setObject(col++, obj);
            	}            	
            }
            
            for(Object obj : keyColVal)
            {            	
            	if(obj instanceof java.util.Date){
            		java.util.Date utilDate = (java.util.Date)obj;
            		pstmt.setDate(col++, new java.sql.Date(utilDate.getTime()));            		
            	}else            		
            		pstmt.setObject(col++, obj);
            	
            }            
            updated = pstmt.executeUpdate();
            
            queryProcessTime = System.currentTimeMillis() - startTime;		// For instrumentation and debug
            
            conn.commit();
            
            logger.info("ReqRepositoryDAO.updateAdjustIndicator: " + updated + " row(s) updated (" + queryMap.get(SQL_QUERY) + ") in " + queryProcessTime + " millisecons.");
            
            result.setStatus(UpdateResult.SUCCESS);
			result.setRowsAffected(updated);			
			result.addMessage("Transaction successfully completed.");
			
			returnedSqlUpdate.put("requirement_id", keyColVal.get(0));
			result.setReturnObj(returnedSqlUpdate);
			
		}catch(SQLException e){
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			
			logger.severe("ReqRepositoryDAO.updateAdjustIndicator: Error updating! (" + queryMap.get(SQL_QUERY) + ")");
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			e.printStackTrace();			
		} finally {
			pstmt.close();
			conn.close();
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public UpdateResult insertRequirement(Map<Object, Object> parms) throws SQLException
	{
		int inserted = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        long queryProcessTime = 0;
        Map<Object, Object> queryMap = null;      	
        
        Map<String, Object> returnedSqlUpdate = new HashMap<String, Object>();
        UpdateResult result = new UpdateResult();
        int requirementId = 0;
        try
		{			
			long startTime = System.currentTimeMillis();
        	conn = ConnectionManager.getConnection(CONNECTION_POOL_NAME);
        	
        	requirementId = selectNextRequirementId(conn);
        	
        	queryMap = SQLBuilder.buildInsertSql(parms, requirementId);
        	
        	int col = 1;        	
            pstmt = conn.prepareStatement((String)queryMap.get(SQL_QUERY));            
            List<Object> colToInsertVal = (List<Object>)queryMap.get(COLUMN_TO_INSERT);
            
            for(Object obj : colToInsertVal)
            {             	
            	if(obj instanceof java.util.Date){
            		java.util.Date utilDate = (java.util.Date)obj;
            		pstmt.setDate(col++, new java.sql.Date(utilDate.getTime()));            		
            	}else{
            		pstmt.setObject(col++, obj);
            	}            	
            }
                       
            inserted = pstmt.executeUpdate();
            
            queryProcessTime = System.currentTimeMillis() - startTime;		// For instrumentation and debug
            
            conn.commit();
            
            logger.info("ReqRepositoryDAO.insertAdjustIndicator: " + inserted + " row(s) inserted (" + queryMap.get(SQL_QUERY) + ") in " + queryProcessTime + " millisecons.");
            
            result.setStatus(UpdateResult.SUCCESS);
			result.setRowsAffected(inserted);			
			result.addMessage("Transaction successfully completed.");
			
			returnedSqlUpdate.put("requirement_id", requirementId);
			result.setReturnObj(returnedSqlUpdate);
			
		}catch(SQLException e){
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			
			logger.severe("ReqRepositoryDAO.updateAdjustIndicator: Error updating! (" + queryMap.get(SQL_QUERY) + ")");
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			e.printStackTrace();			
		} finally {
			pstmt.close();
			conn.close();
		}
		
		return result;
	}
	
	
	private int selectNextRequirementId(Connection connection) throws SQLException
	{
		String sql = null;
		PreparedStatement stmt = null;
		ResultSet rslt = null;
		int reqId = 0;

		try {
			
			stmt = connection.prepareStatement("SELECT RR_REQUIREMENT_SEQ.NEXTVAL FROM DUAL");
        	
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
			stmt.close();
			//connection.close();
		} 
		return reqId;
	}
}
