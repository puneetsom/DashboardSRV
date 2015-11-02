package com.amdocs.dashboard.dao;

/**
 * @author PUNEETSH
 *
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.amdocs.dashboard.sql.SQLBuilder;
import com.amdocs.dashboard.sql.SQLConstants;
import com.amdocs.dashboard.utils.DashboardDBConfig;

public class RejectCBPBManHandledDAO implements SQLConstants
{
	private static Logger logger = Logger.getLogger("com.amdocs.dashboard.dao");

	@SuppressWarnings("unchecked")
	public int update(Map<Object, Object> parms) throws SQLException
	{
		int updated = 0;
		Connection conn = null;      
		PreparedStatement pstmt = null;
		Map<Object, Object> queryMap = SQLBuilder.buildUpdateSql(parms);	      	

		try
		{
			conn = DashboardDBConfig.getInstance().getConnection("EA"); //Executive Advisor DB
			System.out.print("conn %%%%%%%%%%%%%%% "+conn);
			int col = 1;
			pstmt = conn.prepareStatement((String)queryMap.get(SQL_QUERY));
			System.out.println("pstmt : "+pstmt);
			List<Object> colToUpdateVal = (List<Object>)queryMap.get(COLUMN_TO_UPDATE);
			System.out.println("colToUpdateVal : "+colToUpdateVal);
			List<Object> keyColVal = (List<Object>)queryMap.get(KEY);
			
			System.out.println("keyColVal : "+keyColVal);
			

			for(Object obj : colToUpdateVal){ 

				if(obj instanceof java.util.Date){
					java.util.Date utilDate = (java.util.Date)obj;
					pstmt.setDate(col++, new java.sql.Date(utilDate.getTime()));            		
				}else{
					pstmt.setObject(col++, obj);
				}            	
			}
			for(Object obj : keyColVal){ 

				if(obj instanceof java.util.Date){
					java.util.Date utilDate = (java.util.Date)obj;
					pstmt.setDate(col++, new java.sql.Date(utilDate.getTime()));            		
				}else{
					pstmt.setObject(col++, obj);
				}
			}            
			updated += pstmt.executeUpdate();

			conn.commit();

			logger.info("RejectCBPBManHandledDAO.updateHandledIndicator: " + updated + " row(s) updated (" + queryMap.get(SQL_QUERY) + ")");
		}
		catch(SQLException e)
		{
			logger.severe("RejectCBPBManHandledDAO.updateHandledIndicator: Error updating! (" + queryMap.get(SQL_QUERY) + ")");
			e.printStackTrace();
			throw e;
		}catch(Exception e)
		{
			e.printStackTrace();			
		} finally {
			pstmt.close();
			conn.close();
		}

		return updated;
	}
}
