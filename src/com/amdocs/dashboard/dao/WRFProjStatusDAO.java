package com.amdocs.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.amdocs.dashboard.sql.SQLBuilder;
import com.amdocs.dashboard.sql.SQLConstants;
import com.amdocs.dashboard.utils.DashboardDBConfig;

public class WRFProjStatusDAO implements SQLConstants{

	/**
	 * @author Parichab
	 *
	 */
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
		}
		catch(SQLException e)
		{
			logger.severe("WRFProjStatusDAO.update: Error updating! (" + queryMap.get(SQL_QUERY) + ")");
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
