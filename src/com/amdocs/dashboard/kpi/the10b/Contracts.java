package com.amdocs.dashboard.kpi.the10b;

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

public class Contracts extends StatefulJobBase
{
	//TODO: We might need to handle everything per region and create a file per region...
	//      Look at Items.java to see how I used a HashMap to store totals per region.

	int countTotal = 0;
	double revenueTotal = 0.0;
	int countTarget = 0;
	double revenueTarget = 0.0;
	
	protected String summaryTotalOutputFile = "";
	protected String masterSummaryTotalOutputFile = "";
	
	//-------------------------------------------------------------------------
	//Build the Contracts XML file.
	public void buildKpi(JobDataMap m)
	{
		masterSummaryTotalOutputFile = m.getString("summaryTotalOutputFile");
		summaryTotalOutputFile = appendDateToFilename(masterSummaryTotalOutputFile);
		
		//Query the list of Contracts from the Regional ACRM databases.
		buildListsOfContracts();

		//Create two XML files... summary and detail.
		buildSummaryAndDetailsFiles();
	}

	//-------------------------------------------------------------------------
	private void buildListsOfContracts()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> contracts = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': contracts = kpiMap_s; break;
				case 'm': contracts = kpiMap_m; break;
				case 'w': contracts = kpiMap_w; break;
				case 'b': contracts = kpiMap_b; break;
			}

			getRegionalACRMData(region, contracts);			
		}
	}
	
	//-------------------------------------------------------------------------
	private void getRegionalACRMData(String region, TreeMap<String, HashMap<String, String>> contracts)
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "the10b-Contracts.sql");  //TODO: get filename from a property (job.xml)

	        Logger.getLogger("com.amdocs.kpi").info(strQuery);
	        
            pstmt = conn.prepareStatement(strQuery);
            pstmt.setFetchSize(1000); //mateo - bring bigger chunks

            rs = pstmt.executeQuery();

            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(22);

                String customerId = rs.getString(1);
                String contractId = rs.getString(2);
            	String salesRep = rs.getString(3);
            	String salesRepId = rs.getString(4);
            	String salesManager = rs.getString(5);
            	String salesManagerId = rs.getString(6);
            	String generalManager = rs.getString(7);
            	String generalManagerId = rs.getString(8);
            	String contractStatus = rs.getString(9); 
            	String findingName = rs.getString(10);
            	String productName = rs.getString(11);
            	String issueDate = rs.getString(12);
            	String atn = rs.getString(13);
            	String aliCode = rs.getString(14);
            	String sfaBotsAmt = rs.getString(15);
            	String sfaNisdAmt = rs.getString(16);
            	String vertical = rs.getString(17);
            	if(vertical == null) vertical = " ";
            	String category = rs.getString(18);
            	if(category == null) category = " ";
            	String ppcInd = rs.getString(19);
            	if(ppcInd == null) ppcInd = " ";
            	String officeCode = rs.getString(20);
           	
				if(findingName == null) findingName = "";
				
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_CONTRACT_ID,contractId );
            	map.put(KEY_SALES_REP, salesRep);
            	map.put(KEY_SALES_REP_ID, salesRepId);
            	map.put(KEY_SALES_MANAGER, salesManager);
            	map.put(KEY_SALES_MANAGER_ID, salesManagerId);
				map.put(KEY_GENERAL_MANAGER, generalManager);
				map.put(KEY_GENERAL_MANAGER_ID, generalManagerId);
            	map.put(KEY_CONTRACT_STATUS, contractStatus);
            	map.put(KEY_FINDING_NAME, findingName);
            	map.put(KEY_PRODUCT_NAME, productName);
            	map.put(KEY_ISSUE_DATE, issueDate);
            	map.put(KEY_ATN, atn);
            	map.put(KEY_ALI_CODE, aliCode);
            	map.put(KEY_SFA_BOTS_AMT, sfaBotsAmt);
            	map.put(KEY_SFA_NISD_AMT, sfaNisdAmt);
            	map.put(KEY_VERTICAL, vertical);
            	map.put(KEY_CATEGORY, category);
            	map.put(KEY_PPCIND, ppcInd);
            	map.put(KEY_OFFICECODE, officeCode);
    			
    			contracts.put(contractId, map);

             }
            Logger.getLogger("com.amdocs.kpi").info("********Records Read from Database");
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("The10b Contracts: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}

	//-------------------------------------------------------------------------
	private void buildSummaryAndDetailsFiles()
	{
		Logger.getLogger("com.amdocs.kpi").info("**************Building the Contracts Files");
		//Loop over all contracts (per region) and create count 
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> contracts = null;

		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		//reset variables for counts & targets
		countTotal = 0;
		revenueTotal = 0.0;
		countTarget = 0;
		revenueTarget = 0.0;

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

			Logger.getLogger("com.amdocs.kpi").info("********Building the Contracts Detail File");
			while(st.hasMoreTokens())
			{
				String region = st.nextToken();
				switch(region.toLowerCase().toCharArray()[0])
				{
					case 's': contracts = kpiMap_s; break;
					case 'm': contracts = kpiMap_m; break;
					case 'w': contracts = kpiMap_w; break;
					case 'b': contracts = kpiMap_b; break;
				}

				String regionName = AttUtils.getRegionName(region);
								
				Collection<HashMap<String, String>> c = contracts.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();

				while(itr.hasNext())
				{
					HashMap<String, String> map = itr.next();
					
					String contractStatus = map.get(KEY_CONTRACT_STATUS);				
					String strRevenue = map.get(KEY_SFA_NISD_AMT);
					double revenue = Double.valueOf(strRevenue.trim()).doubleValue();

					//Logger.getLogger("com.amdocs.kpi").info("Status: " + contractStatus + ", Rev: " + strRevenue);
					
					//Increment the targets... (mateo)
					countTarget++;
					revenueTarget += revenue;

					//if handled, increment the totals... (mateo)
					if ("Handled(Boost)".equalsIgnoreCase(contractStatus) || "Handled(Closed)".equalsIgnoreCase(contractStatus))
					{
						countTotal++;
						revenueTotal += revenue;
					}

					// (1) Calculate the Summary Info
					//
	
					//get the results for this key
					HashMap<String, String> resMap = results.get(contractStatus);
	
					//the results exist, increment count and revenue
					if(resMap != null)
					{
						String strCount = resMap.get(KEY_COUNT);
						int tmpCount = Integer.parseInt(strCount);
						tmpCount++;
						resMap.put(KEY_COUNT, String.valueOf(tmpCount));

						String strExistRevenue = resMap.get(KEY_REVENUE);
						double existRevenue = Double.parseDouble(strExistRevenue);
						existRevenue += revenue;
						resMap.put(KEY_REVENUE, String.valueOf(existRevenue));						
					}
					else
					{
						//otherwise, create entity for this contractStatus
						resMap = new HashMap<String, String>(3);

						resMap.put(KEY_CONTRACT_STATUS, contractStatus);						
						resMap.put(KEY_COUNT, "1");
						resMap.put(KEY_REVENUE, String.valueOf(revenue));					
					}

					
					// (2) Build the Details XML file.
						
					String customerId = map.get(KEY_CUSTOMER_ID);	
					//String contractId = map.get(KEY_CONTRACT_ID);
					String salesRep = map.get(KEY_SALES_REP);
					String salesRepId = map.get(KEY_SALES_REP_ID);
	            	String salesManager = map.get(KEY_SALES_MANAGER);
	            	String salesManagerId = map.get(KEY_SALES_MANAGER_ID);
	            	String generalManager = map.get(KEY_GENERAL_MANAGER);
	            	String generalManagerId = map.get(KEY_GENERAL_MANAGER_ID);
	            	String findingName = map.get(KEY_FINDING_NAME);
	            	String productName = map.get(KEY_PRODUCT_NAME);
	            	String issueDate = map.get(KEY_ISSUE_DATE);
	            	String atn = map.get(KEY_ATN);	
	            	String aliCode = map.get(KEY_ALI_CODE);	
	            	String sfaBotsAmt = map.get(KEY_SFA_BOTS_AMT);	
	            	String sfaNisdAmt = map.get(KEY_SFA_NISD_AMT);	
	            	String vertical = map.get(KEY_VERTICAL);	
	            	String category = map.get(KEY_CATEGORY);	
	            	String ppcInd = map.get(KEY_PPCIND);	
	            	String officeCode = map.get(KEY_OFFICECODE);	
					
					if(findingName == null)
						findingName = "";

					writer.writeEntity("result");
					writer.writeAttribute("customerId", customerId);
					writer.writeAttribute("salesRep", salesRep);
					writer.writeAttribute("salesRepId", salesRepId);
					writer.writeAttribute("salesManager", salesManager);
					writer.writeAttribute("salesManagerId", salesManagerId);
					writer.writeAttribute("generalManager", generalManager);
					writer.writeAttribute("generalManagerId", generalManagerId);
					writer.writeAttribute("contractStatus", contractStatus);
					writer.writeAttribute("findingName", findingName);
					writer.writeAttribute("productName", productName);
					writer.writeAttribute("issueDate", issueDate);
					writer.writeAttribute("atn", atn);
					writer.writeAttribute("aliCode", aliCode);
					writer.writeAttribute("sfaBotsAmt", sfaBotsAmt);
					writer.writeAttribute("sfaNisdAmt", sfaNisdAmt);
					writer.writeAttribute("region", regionName);
					writer.writeAttribute("vertical", vertical);
					writer.writeAttribute("category", category);
					writer.writeAttribute("ppcInd", ppcInd);
					writer.writeAttribute("officeCode", officeCode);
					writer.endEntity();
					
					results.put(contractStatus, resMap);
				}
			}

			writer.endEntity();
			writer.close();
			fw.close();	
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Error Processing Data, exception: " + e.getMessage());
		}
		
		//Build the output XML files
		buildContractSummaryXMLFile(results, summaryOutputFile);
		buildSummaryTotalXMLFile();
	}
	
	
	//-------------------------------------------------------------------------
	protected void buildContractSummaryXMLFile(HashMap<String, HashMap<String, String>> results, String outputFile)
	{
		try
		{
			Logger.getLogger("com.amdocs.kpi").info("********Building the Contracts Summary File");
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
	
	        	String contractStatus = map.get(KEY_CONTRACT_STATUS);  
	        	String strCount = map.get(KEY_COUNT);
	        	String strRevenue = map.get(KEY_REVENUE);
	        	 	
				writer.writeEntity("result");
				writer.writeAttribute("contractStatus", contractStatus);
				writer.writeAttribute("count", strCount);  
				writer.writeAttribute("revenue", strRevenue);  
	       		writer.endEntity();
			}
	
			writer.endEntity();
			writer.close();
			fw.close();
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Exception creating XML file: " + outputFile + ", exception: " + e.getMessage() );
		}
	}

	
	/**
	 * Special method for creating a Totals XML file. 
	 *  
	 * @author matt
	 */
	protected void buildSummaryTotalXMLFile()
	{
		try
		{
			Logger.getLogger("com.amdocs.kpi").info("********Building the Contracts Summary Total File");
	        //Actually build the XML file
			File f = new File(summaryTotalOutputFile);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
	
			writer.writeXMLHeader();
			writer.writeEntity("results");

			//Add a special result entity for the totals.
			writer.writeEntity("result");
			writer.writeAttribute("contractStatus", "ALL");
			writer.writeAttribute("countTotal", String.valueOf(countTotal));
			writer.writeAttribute("revenueTotal", String.valueOf(revenueTotal));
			writer.writeAttribute("countTarget", String.valueOf(countTarget));
			writer.writeAttribute("revenueTarget", String.valueOf(revenueTarget));
       		writer.endEntity();

			writer.endEntity();
			writer.close();
			fw.close();

			Logger.getLogger("com.amdocs.kpi").info("Copying file " + summaryTotalOutputFile + " to " + masterSummaryTotalOutputFile + ".");		
			copyFile(new File(summaryTotalOutputFile), new File(masterSummaryTotalOutputFile));
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Exception creating XML file: " + summaryTotalOutputFile + ", exception: " + e.getMessage() );
		}
	}
	
}
