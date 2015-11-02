package com.amdocs.dashboard.kpi.amss;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.quartz.JobDataMap;

import com.amdocs.dashboard.kpi.StatefulJobBase;
import com.amdocs.dashboard.utils.AttUtils;
import com.amdocs.dashboard.utils.DashboardDBConfig;
import com.amdocs.infra.utils.Config;
import com.amdocs.infra.utils.DBConfig;
import com.amdocs.infra.utils.XMLWriter;

public class AmssEBillSubscribers extends StatefulJobBase
{
	//-------------------------------------------------------------------------
	//Build the AMSS eBill Subscribers XML file.
	public void buildKpi(JobDataMap m)
	{
		//Query the list of EBill subscribers from the Regional ACRM databases.
		buildListsOfEBillSubscribers();

		//Create two XML files... summary and detail.
		buildSummaryAndDetailsFiles();
	}

	//-------------------------------------------------------------------------
	private void buildListsOfEBillSubscribers()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> eBillSubscribers = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': eBillSubscribers = kpiMap_s; break;
				case 'm': eBillSubscribers = kpiMap_m; break;
				case 'w': eBillSubscribers = kpiMap_w; break;
				case 'b': eBillSubscribers = kpiMap_b; break;
			}

			getRegionalACRMData(region, eBillSubscribers);			
		}
	}
	
	//-------------------------------------------------------------------------
	private void getRegionalACRMData(String region, TreeMap<String, HashMap<String, String>> eBillSubscribers)
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "AMSS-eBill-Subscribers.sql");  //TODO: get filename from a property (job.xml)

            pstmt = conn.prepareStatement(strQuery);           
            rs = pstmt.executeQuery();
           
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(5);

            	String customerId = rs.getString(1);
            	String billingAccountId = rs.getString(2);
            	String billingName = rs.getString(3);
            	String state = rs.getString(4); //statecode
            	String suppressionReason = rs.getString(5);
            	String email = rs.getString(6);
           	
				if(billingName == null) billingName = "";
				if(state == null || state == "") state = "none";
				if(email == null) email = "";
            	
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_BILLING_ACCOUNT_ID, billingAccountId);
				map.put(KEY_BILLING_NAME, billingName);
            	map.put(KEY_STATE, state);
            	map.put(KEY_SUPPRESSION_REASON, suppressionReason);
            	map.put(KEY_EMAIL, email);

    			switch(region.toLowerCase().toCharArray()[0])
    			{
    				case 's': map.put(KEY_AREA, "Southwest"); break;
    				case 'm': map.put(KEY_AREA, "Midwest"); break;
    				case 'w': map.put(KEY_AREA, "West"); break;
    				case 'b': map.put(KEY_AREA, "Southeast"); break;
    			}

            	//Only add the customer if their state is inRegion.
//            	if(AttUtils.isStateInRegion(region, state))
//            	{
            		eBillSubscribers.put(customerId, map);
//            	}   			
            }
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("AMSS eBill Subscribers: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}

	//-------------------------------------------------------------------------
	private void buildSummaryAndDetailsFiles()
	{
		//Loop over all users (per region) and create count by area and state
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> eBillSubscribers = null;

		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		//During these iterations:
		//
		//   (1) Calculate the summary info.
		//   (2) Build the Details XML file.
		//
		try
		{
			//Start the details XML file
			File f = new File(detailOutputFile);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
			writer.writeXMLHeader();
			writer.writeEntity("results");

			while(st.hasMoreTokens())
			{
				String region = st.nextToken();
				switch(region.toLowerCase().toCharArray()[0])
				{
					case 's': eBillSubscribers = kpiMap_s; break;
					case 'm': eBillSubscribers = kpiMap_m; break;
					case 'w': eBillSubscribers = kpiMap_w; break;
					case 'b': eBillSubscribers = kpiMap_b; break;
				}

				//add an empty map for each state in this region.			
				String regionName = AttUtils.getRegionName(region);
				String states = AttUtils.getStatesInRegion(region); 
				
				StringTokenizer st2 = new StringTokenizer(states, ",");
				
				while(st2.hasMoreTokens())
				{
					HashMap<String, String> tmpMap = new HashMap<String, String>(3);

					String stateAbbr = st2.nextToken();

					tmpMap.put(KEY_AREA, regionName);
					tmpMap.put(KEY_STATE, stateAbbr);
					tmpMap.put(KEY_COUNT, "0");

					//add it to the results
					results.put(regionName + stateAbbr, tmpMap);
				}			

				Collection<HashMap<String, String>> c = eBillSubscribers.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
	
				while(itr.hasNext())
				{
					HashMap<String, String> map = itr.next();
					String area = map.get(KEY_AREA);
					String state = map.get(KEY_STATE);

					// (1) Calculate the Summary Info
					//
					String resKey = area + state; //key is AREA || STATE
	
					//get the results for this key
					HashMap<String, String> resMap = results.get(resKey);
	
					//the results exist, increment count
					if(resMap != null)
					{
						String strCount = resMap.get(KEY_COUNT);
						int count = Integer.parseInt(strCount);
						count++;
						resMap.put(KEY_COUNT, String.valueOf(count));
					}
					else
					{
						//otherwise, create entity for this area/state
						resMap = new HashMap<String, String>(3);
						resMap.put(KEY_AREA, area);
						resMap.put(KEY_STATE, state);
						resMap.put(KEY_COUNT, "1");
	
						//add it to the results
						results.put(resKey, resMap);
					}
	
					// (2) Build the Details XML file.
					//
					String customerId = "n/a";
	            	String billingAccountId = map.get(KEY_BILLING_ACCOUNT_ID);
	            	String billingName = map.get(KEY_BILLING_NAME);
	            	String suppressionReason = map.get(KEY_SUPPRESSION_REASON);
	            	String email = map.get(KEY_EMAIL);				
					
					if(area.compareToIgnoreCase("Other") != 0)
						customerId = map.get(KEY_CUSTOMER_ID);

					if(billingName == null)
						billingName = "";

					if(email == null)
						email = "";

					writer.writeEntity("result");
					writer.writeAttribute("region", area);
					writer.writeAttribute("state", AttUtils.getStateName(state));
					writer.writeAttribute("customerId", customerId);
					writer.writeAttribute("billingAccountId", billingAccountId);
					writer.writeAttribute("billingName", billingName);

					if(suppressionReason.compareToIgnoreCase("EB") == 0)
						suppressionReason = "eBill Only";
					else if(suppressionReason.compareToIgnoreCase("PE") == 0)
						suppressionReason = "Paper & eBill";
					else
						suppressionReason = "unknown";

					writer.writeAttribute("eBill", suppressionReason);
					writer.writeAttribute("email", email);
					writer.endEntity();
				}
			}

			writer.endEntity();
			writer.close();
			fw.close();	
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("Error Processing Data, exception: " + e.getMessage());
		}
		
		//Build the output XML file
		buildSummaryXMLFile(results, summaryOutputFile);
	}

}
