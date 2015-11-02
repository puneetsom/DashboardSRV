package com.amdocs.infra.appusage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.amdocs.dashboard.utils.DashboardDBConfig;

public class AppUsageDAO
{
	// Leaving signature as-is, not Java 5 compliant, since not sure if BlazeDS will be able to call it otherwise
	@SuppressWarnings("unchecked")
	public Boolean updateUsage(String appId, String actionId) throws SQLException
	{
		Logger.getLogger("com.amdocs.infra.appusage").info("Update usage for app: " + appId + ", action : " + actionId + ".");

		Boolean result = true;
        Connection conn = null;      
        PreparedStatement pstmt = null;

		try
		{
			conn = DashboardDBConfig.getInstance().getConnection("EA"); //Executive Advisor DB

			String strQuery = "update APP_USAGE set COUNT = (COUNT + 1)" +
							  " where APP_ID = ?" +
							  "   and ACTION_ID = ?";        	

            pstmt = conn.prepareStatement(strQuery);
            pstmt.setString(1, appId);
            pstmt.setString(2, actionId);

            pstmt.executeUpdate();
		}
		catch(Exception e)
		{
			result = false;
			Logger.getLogger("com.amdocs.infra.appusage").severe("App Usage: Exception logging usage, message: " + e.getMessage());
		}
		
		return result;
	}


	@SuppressWarnings("unchecked")
	public Boolean insertUsageDetails(String appId, String actionId, String userId) throws SQLException
	{
		Logger.getLogger("com.amdocs.infra.appusage").info("Insert usage details for user: " + userId + ", app: " + appId + ", action : " + actionId + ".");

		Boolean result = true;
        Connection conn = null;      
        PreparedStatement pstmt = null;

		try
		{
			conn = DashboardDBConfig.getInstance().getConnection("EA"); //Executive Advisor DB

			String strQuery = "insert into APP_USAGE_DETAILS values (?, ?, ?, sysdate)";
	
            pstmt = conn.prepareStatement(strQuery);
            pstmt.setString(1, appId);
            pstmt.setString(2, actionId);
            pstmt.setString(3, userId);

            pstmt.executeUpdate();
		}
		catch(Exception e)
		{
			result = false;
			Logger.getLogger("com.amdocs.infra.appusage").severe("App Usage: Exception logging usage details, message: " + e.getMessage());
		}
		
		return result;
	}

}
