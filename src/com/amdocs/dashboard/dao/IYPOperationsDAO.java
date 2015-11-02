package com.amdocs.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.amdocs.dashboard.utils.DashboardDBConfig;

/**
 * @TODO Change to use connection pool!
 * 
 * @author Brian Seyfert
 *
 */
public class IYPOperationsDAO
{
	private static Logger logger = Logger.getLogger("com.amdocs.dashboard.dao");
	
	public int updateExcludeIndicator(long customerId, String productCode, int productIssueNum, int itemId, 
			int cssProductId, String excludeIndictator, String featureCode,
			String workflowType, String changeLevel, String imvItemProcessId, String componentProcessId) throws SQLException
	{
		int updated = 0;
        Connection conn = null;      
        PreparedStatement pstmt = null;
        
        try
		{
			conn = DashboardDBConfig.getInstance().getConnection("EA"); //Executive Advisor DB

			String strQuery = "update IOCT_CYCLE_TIME_ALL \n" +
							  "set    exclude_indicator  = ? \n" +
							  "where  CUSTOMER_ID  = ? \n" +
							  "and    nvl(PRODUCT_CODE,' ') = nvl(?, ' ') \n" +
							  "and    nvl(PRODUCT_ISSUE_NUM,0) = ? \n" +
							  "and    nvl(ITEM_ID, 0) = ? \n" +
							  "and    nvl(FEATURE_CODE,' ') = nvl(?, ' ') \n" +
							  "and    nvl(CSS_PRODUCT_ID, 0) = ? \n" +
							  "and    nvl(WORKFLOW_TYPE,' ') = nvl(?, ' ') \n" +
							  "and    nvl(CHANGE_LEVEL,' ') = nvl(?, ' ') \n" +
							  "and    nvl(IMV_ITEM_PROCESS_ID,' ') = nvl(?, ' ') \n" +
							  "and    nvl(COMPONENT_PROCESS_ID,' ') = nvl(?, ' ') "
							  ; 
			
			int col = 1;
            pstmt = conn.prepareStatement(strQuery);
            pstmt.setString(col++, excludeIndictator);
            pstmt.setLong(col++, customerId);
            if (productCode == null || productCode.equals("null"))
            	pstmt.setNull(col++, java.sql.Types.VARCHAR);
            else
            	pstmt.setString(col++, productCode);
            pstmt.setInt(col++, productIssueNum);
            pstmt.setInt(col++, itemId);
            if (featureCode == null || featureCode.equals("null"))
            	pstmt.setNull(col++, java.sql.Types.VARCHAR);
            else
            	pstmt.setString(col++, featureCode);

            pstmt.setInt(col++, cssProductId);
            
            pstmt.setString(col++, workflowType);
            pstmt.setString(col++, changeLevel);
            pstmt.setString(col++, imvItemProcessId);
            pstmt.setString(col++, componentProcessId);
            
            updated += pstmt.executeUpdate();
            
            conn.commit();
            
            logger.info("IYPOperationsDAO.updateExcludeIndicator: " + updated + " row(s) updated (" + customerId + ", " + productCode + ", " + productIssueNum + ", " + itemId + ", " + cssProductId + ", " + excludeIndictator +", " +featureCode+ ")");
		}
		catch(SQLException e)
		{
			logger.severe("IYPOperationsDAO.updateExcludeIndicator: Error updating! (" + customerId + ", " + productCode + ", " + productIssueNum + ", " + itemId + ", " + excludeIndictator +", " +featureCode+ ")");
			e.printStackTrace();
			throw e;
		} finally {
			pstmt.close();
			conn.close();
		}
		
		return updated;
	}
}

