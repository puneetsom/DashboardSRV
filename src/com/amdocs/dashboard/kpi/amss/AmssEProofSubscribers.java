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

public class AmssEProofSubscribers extends StatefulJobBase 
{
	//-------------------------------------------------------------------------
	//Build the AMSS eProof Subscribers XML file.
	public void buildKpi(JobDataMap m)
	{
		//Query the list of EProof subscribers from the Regional ACRM databases.
		buildListsOfEProofSubscribers();

		//Create two XML files... summary and detail.
		buildSummaryAndDetailsFiles();
	}

	//-------------------------------------------------------------------------
	private void buildListsOfEProofSubscribers()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> eProofSubscribers = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': eProofSubscribers = kpiMap_s; break;
				case 'm': eProofSubscribers = kpiMap_m; break;
				case 'w': eProofSubscribers = kpiMap_w; break;
				case 'b': eProofSubscribers = kpiMap_b; break;
			}

			getRegionalACRMData(region, eProofSubscribers);			
		}
	}
	
	//-------------------------------------------------------------------------
	private void getRegionalACRMData(String region, TreeMap<String, HashMap<String, String>> eProofSubscribers)
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "AMSS-eProof-Subscribers.sql");  //TODO: get filename from a property (job.xml)
        
            pstmt = conn.prepareStatement(strQuery);           
            rs = pstmt.executeQuery();
           
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(5);

            	String customerId = rs.getString(1);
            	String businessName = rs.getString(2);
            	String state = rs.getString(3); //statecode
            	String email = rs.getString(4);
           	
				if(businessName == null) businessName = "";
				if(state == null || state == "") state = "none";
				if(email == null) email = "";
            	
            	map.put(KEY_BUSINESS_NAME, businessName);
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_EMAIL, email);
            	map.put(KEY_STATE, state);

    			switch(region.toLowerCase().toCharArray()[0])
    			{
    				case 's': map.put(KEY_AREA, "Southwest"); break;
    				case 'm': map.put(KEY_AREA, "Midwest"); break;
    				case 'w': map.put(KEY_AREA, "West"); break;
    				case 'b': map.put(KEY_AREA, "Southeast"); break;
    			}

            	//Only add the customer if their state is inRegion.
            	if(AttUtils.isStateInRegion(region, state))
            	{
            		eProofSubscribers.put(customerId, map);
            	}
            }
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("AMSS eProof Subscribers: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}

	//-------------------------------------------------------------------------
	protected void buildSummaryAndDetailsFiles()
	{
		//Loop over all users (per region) and create count by area and state
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> eProofSubscribers = null;

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
					case 's': eProofSubscribers = kpiMap_s; break;
					case 'm': eProofSubscribers = kpiMap_m; break;
					case 'w': eProofSubscribers = kpiMap_w; break;
					case 'b': eProofSubscribers = kpiMap_b; break;
				}

				//add an empty map for each state in this region.
				String regionName = AttUtils.getRegionName(region);
				String states = AttUtils.getStatesInRegion(region); 
				
				StringTokenizer st2 = new StringTokenizer(states, ",");
				
				while(st2.hasMoreTokens())
				{
					HashMap<String, String> tmpMap = new HashMap<String, String>(3);
					
					String stateName = st2.nextToken();

					tmpMap.put(KEY_AREA, regionName);
					tmpMap.put(KEY_STATE, stateName);
					tmpMap.put(KEY_COUNT, "0");

					//add it to the results
					results.put(regionName + stateName, tmpMap);
				}			
				
				Collection<HashMap<String, String>> c = eProofSubscribers.values();        
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
					String businessName = map.get(KEY_BUSINESS_NAME);
					String email = map.get(KEY_EMAIL);
					
					if(area.compareToIgnoreCase("Other") != 0)
						customerId = map.get(KEY_CUSTOMER_ID);

					if(businessName == null)
						businessName = "";

					if(email == null)
						email = "";

					writer.writeEntity("result");
					writer.writeAttribute("region", area);
					writer.writeAttribute("state", AttUtils.getStateName(state));
					writer.writeAttribute("customerId", customerId);
					writer.writeAttribute("businessName", businessName);
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
