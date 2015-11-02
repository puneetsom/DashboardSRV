package com.amdocs.dashboard.kpi.amss;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.quartz.JobDataMap;

import com.amdocs.dashboard.kpi.StatefulJobBase;
import com.amdocs.dashboard.utils.AttUtils;
import com.amdocs.dashboard.utils.DashboardDBConfig;
import com.amdocs.infra.utils.Config;
import com.amdocs.infra.utils.DBConfig;

public class AmssRegistrations extends StatefulJobBase 
{
	//-------------------------------------------------------------------------
	//Build the AMSS registrations XML file.
	public void buildKpi(JobDataMap m)
	{
		//Query the list of Registered customers from AMSS.
		buildListsOfRegisteredCustomers();

		//For each customer, query the name, email, and HBD (to get the state code) from ACRM.
		addRegionalACRMInfo();

		//Create two XML files... summary and detail.
		buildAmssSummaryAndDetailsFiles();
	}

	//-------------------------------------------------------------------------
	private void buildListsOfRegisteredCustomers()
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{
			conn = DashboardDBConfig.getInstance().getConnection("A");

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "AMSS-Registrations.sql");  //TODO: get filename from a property (job.xml)
	        
	        //TODO: not using params right now, but we can...
//	        HashMap arr = DBConfig.parseSQL(strQuery, null, null);
//	        strQuery = (String)arr.get("QUERY");

            pstmt = conn.prepareStatement(strQuery);
            
	        //TODO: not using params right now, but we can...            
//            ArrayList paramValues = (ArrayList)arr.get("PARAM_VALUES");           
//            if(paramValues != null)
//            {    
//            	for(int i = 0; i < paramValues.size(); i++)
//            	{
//            		pstmt.setString(i+1, (String)paramValues.get(i));
//            	}
//            }

            rs = pstmt.executeQuery();
           
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(7);

            	String area = rs.getString(1);
            	String customerId = rs.getString(2);

            	map.put(KEY_AREA, area);
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_NAME, rs.getString(3));
            	map.put(KEY_USER_ID, rs.getString(4));

            	//Handle special case for anonymous users with id starting with 1000.
// Removed this code, because these users are being filtered out by the query's where clause.
//            	if(AttUtils.regionList.valueOf(area.toLowerCase()) == AttUtils.regionList.southwest &&
//            	   customerId.startsWith("1000"))
//            	{
//            		map.put(KEY_AREA, "Other");
//            		kpiMap_o.put(customerId, map);
//            	}
//            	else
//            	{
            		switch(AttUtils.regionList.valueOf(area.toLowerCase()))
	            	{
	            		case southwest: kpiMap_s.put(customerId, map); break;
		            	case midwest: kpiMap_m.put(customerId, map); break;
		            	case west: kpiMap_w.put(customerId, map); break;
		            	case southeast: kpiMap_b.put(customerId, map); break;
		            	case other: kpiMap_o.put(customerId, map); break;
		            	default: kpiMap_o.put(customerId, map); break;
	            	}
//            	}
            }
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("AMSS Registrations: Exception building lists, message: " + e.getMessage());
		}
	}

	//-------------------------------------------------------------------------
	protected void addACRMInfo(String region, TreeMap<String, HashMap<String, String>> registeredUsers)
	{
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;

        if(registeredUsers.values().isEmpty())
        	return;
        
		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//setup the array type		
	        String strQuery = "select CUSTOMER.CUSTOMER_ID, LISTING.FINDING_NAME, substr(HBD, 4, 2), " +
	        		          "       (select E_MAIL_ADDRESS from CONTACT WHERE CUSTOMER.CONTACT_ID = CONTACT.CONTACT_ID) as EMAIL" +
	        		          "  from CUSTOMER, LISTING" +
	        		          " where (CUSTOMER.CUSTOMER_ID in (";

	        Collection<HashMap<String, String>> c = registeredUsers.values();        
	        Iterator<HashMap<String, String>> itr = c.iterator();

	        int count = 0;
	        
	        while(itr.hasNext())
	        {
	        	count++;        	
	        	if(count > 999) //Oracle limit
	        	{
	        		strQuery += ") OR CUSTOMER.CUSTOMER_ID in (";
	        		count = 0;
	        	}

	        	HashMap<String, String> map = itr.next();
	        	strQuery += map.get(KEY_CUSTOMER_ID);
	        	
	        	if(itr.hasNext() && count < 999) //Oracle limit
	        	{
	        		strQuery += ",";
	        	}
	        }

	        strQuery += "))" +
	          "   and CUSTOMER.MAIN_MAIN_LISTING_ID = LISTING.listing_id(+)" +
	          "   and LISTING.LAST_VERSION_IND(+) = 'Y'";
	        
            pstmt = conn.prepareStatement(strQuery);           
            rs = pstmt.executeQuery();
           
            while(rs.next())
            {
            	String customerId = rs.getString(1);
            	String businessName = rs.getString(2);
            	String state = rs.getString(3);
           	
            	//if the customer's state is null or isn't inRegion, remove the customer.
            	if(state == null || !AttUtils.isStateInRegion(region, state))
            	{
            		registeredUsers.remove(customerId);
            	}
            	else
            	{
	            	HashMap<String, String> map = registeredUsers.get(customerId);

					if(businessName == null) businessName = "";
					if(state == null || state == "") state = "none";

	            	map.put(KEY_BUSINESS_NAME, businessName);
	            	map.put(KEY_STATE, state);
	            	map.put(KEY_EMAIL, rs.getString(4));            	
	        	}
            }
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("AMSS Registrations: Exception adding ACRM info, message: " + e.getMessage());
		}		
	}

}
