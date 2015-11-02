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

public class AmssPayments extends StatefulJobBase  
{
	//-------------------------------------------------------------------------
	//Build the AMSS payments XML file.
	public void buildKpi(JobDataMap m)
	{
		//Query the list of Payments from the Regional ACRM databases.
		buildListsOfPayments();
		
		//Create two XML files... summary and detail.
		buildSummaryAndDetailsFiles();
	}

	//-------------------------------------------------------------------------
	private void buildListsOfPayments()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> payments = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': payments = kpiMap_s; break;
				case 'm': payments = kpiMap_m; break;
				case 'w': payments = kpiMap_w; break;
				case 'b': payments = kpiMap_b; break;
			}

			getRegionalACRMData(region, payments);			
		}
	}
	
	//-------------------------------------------------------------------------
	private void getRegionalACRMData(String region, TreeMap<String, HashMap<String, String>> payments)
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "AMSS-Payments.sql");  //TODO: get filename from a property (job.xml)

            pstmt = conn.prepareStatement(strQuery);           
            rs = pstmt.executeQuery();
           
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(5);

            	String billingAccountId = rs.getString(1);
            	String billingName = rs.getString(2);
            	String amount = rs.getString(3);
            	String creationDate = rs.getString(4);
            	String dueDate = rs.getString(5);          	
            	
				if(billingName == null) billingName = "";
            	
            	map.put(KEY_BILLING_ACCOUNT_ID, billingAccountId);
				map.put(KEY_BILLING_NAME, billingName);		
				map.put(KEY_AMOUNT, amount);
				map.put(KEY_CREATION_DATE, creationDate);
				map.put(KEY_DUE_DATE, dueDate);

    			switch(region.toLowerCase().toCharArray()[0])
    			{
    				case 's': map.put(KEY_AREA, "Southwest"); break;
    				case 'm': map.put(KEY_AREA, "Midwest"); break;
    				case 'w': map.put(KEY_AREA, "West"); break;
    				case 'b': map.put(KEY_AREA, "Southeast"); break;
    			}

           		payments.put(billingAccountId, map);
            }
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("AMSS Payments: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}

	//-------------------------------------------------------------------------
	protected void buildSummaryAndDetailsFiles()
	{
		//Loop over all users (per region) and create count by area and state
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> payments = null;

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
					case 's': payments = kpiMap_s; break;
					case 'm': payments = kpiMap_m; break;
					case 'w': payments = kpiMap_w; break;
					case 'b': payments = kpiMap_b; break;
				}

				String regionName = AttUtils.getRegionName(region);
				
				//Loop over payments and sum the count and amount.
				int count = 0;
				Float amount = new Float(0);

				Collection<HashMap<String, String>> c = payments.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
				
				while(itr.hasNext())
				{				
					HashMap<String, String> map = itr.next();
				
					// (1) Calculate the Summary Info
					//
					//the results exist, increment count and amount
					count++;

					String strAmount = map.get(KEY_AMOUNT);
					if(strAmount != null) amount += Float.parseFloat(strAmount); 			


					// (2) Build the Details XML file.
					//
	            	String billingAccountId = map.get(KEY_BILLING_ACCOUNT_ID);
	            	String billingName = map.get(KEY_BILLING_NAME);
	            	String creationDate = map.get(KEY_CREATION_DATE);
	            	String dueDate = map.get(KEY_DUE_DATE);

					if(billingName == null)
						billingName = "";
				
					writer.writeEntity("result");
					writer.writeAttribute("region", regionName);
					writer.writeAttribute("billingAccountId", billingAccountId);
					writer.writeAttribute("billingName", billingName);
					writer.writeAttribute("amount", strAmount);
					writer.writeAttribute("creationDate", creationDate);
					writer.writeAttribute("dueDate", dueDate);					
					writer.endEntity();
				}
				
				//add an empty map for each region.			
				HashMap<String, String> tmpMap = new HashMap<String, String>(3);

				tmpMap.put(KEY_AREA, regionName);
				tmpMap.put(KEY_COUNT, Integer.toString(count));
				tmpMap.put(KEY_AMOUNT, amount.toString());			

				//add it to the results
				results.put(regionName, tmpMap);
			}

			writer.endEntity();
			writer.close();
			fw.close();	
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("Error Processing Payments, exception: " + e.getMessage());
		}
		
		//Build the output XML file
		buildSummaryXMLFile(results, summaryOutputFile);
	}

	//-------------------------------------------------------------------------
	protected void buildSummaryXMLFile(HashMap<String, HashMap<String, String>> results, String outputFile)
	{
		try
		{
	        //Actually build the XML file
			File f = new File(outputFile);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
	
			writer.writeXMLHeader();
			writer.writeEntity("results");
		
			Collection<HashMap<String, String>> xmlC = results.values();        
	        Iterator<HashMap<String, String>> xml_itr = xmlC.iterator();
	        
	        while(xml_itr.hasNext())
			{
	        	HashMap<String, String> map = xml_itr.next();
	        	
	        	String strCount = map.get(KEY_COUNT);
	        	String strAmount = map.get(KEY_AMOUNT);
	        	
				writer.writeEntity("result");
				writer.writeAttribute("date", getDate());
				writer.writeAttribute("area", map.get(KEY_AREA));
				writer.writeAttribute("count", strCount);
				writer.writeAttribute("amount", strAmount);       		        	
				writer.endEntity();
			}
	
			writer.endEntity();
			writer.close();
			fw.close();
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("Exception creating Payments XML file: " + outputFile + ", exception: " + e.getMessage());
		}
	}

}
