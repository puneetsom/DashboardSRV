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

/**
 * This class will build all the data files for the 4 Item Gauges on the 
 * Service Manager dashboard.
 * 
 * @author matt
 */
public class Items extends StatefulJobBase
{
	//Maps for holding totals data
	protected HashMap<String, String> totalMap_s;
	protected HashMap<String, String> totalMap_m;
	protected HashMap<String, String> totalMap_w;
	protected HashMap<String, String> totalMap_b;

	int countTotal = 0;
	double revenueTotal = 0.0;
	int countTarget = 0;
	double revenueTarget = 0.0;

	protected String summaryTotalOutputFile = "";
	protected String masterSummaryTotalOutputFile = "";
	
	protected String summaryOutputFilePendingItems = "";
	protected String summaryTotalOutputFilePendingItems = "";
	protected String detailOutputFilePendingItems = "";
	protected String masterSummaryOutputFilePendingItems = "";
	protected String masterSummaryTotalOutputFilePendingItems = "";
	protected String masterDetailOutputFilePendingItems = "";

	protected String summaryOutputFileInProgressItems = "";
	protected String summaryTotalOutputFileInProgressItems = "";
	protected String detailOutputFileInProgressItems = "";
	protected String masterSummaryOutputFileInProgressItems = "";
	protected String masterSummaryTotalOutputFileInProgressItems = "";
	protected String masterDetailOutputFileInProgressItems = "";

	protected String summaryOutputFileQueryItems = "";
	protected String summaryTotalOutputFileQueryItems = "";
	protected String detailOutputFileQueryItems = "";
	protected String masterSummaryOutputFileQueryItems = "";
	protected String masterSummaryTotalOutputFileQueryItems = "";
	protected String masterDetailOutputFileQueryItems = "";

	/**
	 * Build all the Item KPI files for the Service Manager dashboard:
	 *  -- Pending Fulfillment/Waiting Items
	 *  -- In Progress Items
	 *  -- Completed Items
	 *  -- Query Items
	 * 
	 * 
	 * @param m
	 *
	 * @see com.amdocs.dashboard.kpi.StatefulJobBase#buildKpi(org.quartz.JobDataMap)
	 * @author matt
	 */
	public void buildKpi(JobDataMap m)
	{
		masterSummaryTotalOutputFile = m.getString("summaryTotalOutputFile");
		summaryTotalOutputFile = appendDateToFilename(masterSummaryTotalOutputFile);

		masterSummaryOutputFilePendingItems = m.getString("summaryOutputFilePendingItems");
		summaryOutputFilePendingItems = appendDateToFilename(masterSummaryOutputFilePendingItems);
		masterSummaryTotalOutputFilePendingItems = m.getString("summaryTotalOutputFilePendingItems");
		summaryTotalOutputFilePendingItems = appendDateToFilename(masterSummaryTotalOutputFilePendingItems);
		masterDetailOutputFilePendingItems = m.getString("detailOutputFilePendingItems");
		detailOutputFilePendingItems = appendDateToFilename(masterDetailOutputFilePendingItems);
		
		masterSummaryOutputFileInProgressItems = m.getString("summaryOutputFileInProgressItems");
		summaryOutputFileInProgressItems = appendDateToFilename(masterSummaryOutputFileInProgressItems);
		masterSummaryTotalOutputFileInProgressItems = m.getString("summaryTotalOutputFileInProgressItems");
		summaryTotalOutputFileInProgressItems = appendDateToFilename(masterSummaryTotalOutputFileInProgressItems);
		masterDetailOutputFileInProgressItems = m.getString("detailOutputFileInProgressItems");
		detailOutputFileInProgressItems = appendDateToFilename(masterDetailOutputFileInProgressItems);
		
		masterSummaryOutputFileQueryItems = m.getString("summaryOutputFileQueryItems");
		summaryOutputFileQueryItems = appendDateToFilename(masterSummaryOutputFileQueryItems);
		masterSummaryTotalOutputFileQueryItems = m.getString("summaryTotalOutputFileQueryItems");
		summaryTotalOutputFileQueryItems = appendDateToFilename(masterSummaryTotalOutputFileQueryItems);
		masterDetailOutputFileQueryItems = m.getString("detailOutputFileQueryItems");
		detailOutputFileQueryItems = appendDateToFilename(masterDetailOutputFileQueryItems);

		
		totalMap_s = new HashMap<String,String>();
		totalMap_m = new HashMap<String,String>();
		totalMap_w = new HashMap<String,String>();
		totalMap_b = new HashMap<String,String>();

		buildTargetMap(); //since all 4 item gauges have the targets, built the targets once.

		buildCompletedItemsMap();
		buildFilesCompletedItems();

		buildPendingFulWaitingItemsMap();
		buildFilesPendingFulWaitingItems();

		buildInProgressItemsMap();
		buildFilesInProgressItems();
		
		buildQueryItemsMap();
		buildFilesQueryItems();
	}

	//-------------------------------------------------------------------------
	private void buildTargetMap()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			HashMap<String, String> itemTotals = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': itemTotals = totalMap_s; break;
				case 'm': itemTotals = totalMap_m; break;
				case 'w': itemTotals = totalMap_w; break;
				case 'b': itemTotals = totalMap_b; break;
			}

			buildTargets(region, itemTotals);
		}
	}
	
	//-------------------------------------------------------------------------
	private void buildCompletedItemsMap()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> itemsCompleted = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': itemsCompleted = kpiMap_s; break;
				case 'm': itemsCompleted = kpiMap_m; break;
				case 'w': itemsCompleted = kpiMap_w; break;
				case 'b': itemsCompleted = kpiMap_b; break;
			}

			buildItemsCompleted(region, itemsCompleted);
		}
	}

	/**
	 * Build map of Pending Ful/Waiting items.
	 * 
	 */
	private void buildPendingFulWaitingItemsMap()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> itemsPendingFulWaiting = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': itemsPendingFulWaiting = kpiMap_s; break;
				case 'm': itemsPendingFulWaiting = kpiMap_m; break;
				case 'w': itemsPendingFulWaiting = kpiMap_w; break;
				case 'b': itemsPendingFulWaiting = kpiMap_b; break;
			}

			buildPendingFulWaiting(region, itemsPendingFulWaiting);
		}
	}

	/**
	 * Build map of InProgress Items.
	 * 
	 */
	private void buildInProgressItemsMap()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> itemsInProgress = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': itemsInProgress = kpiMap_s; break;
				case 'm': itemsInProgress = kpiMap_m; break;
				case 'w': itemsInProgress = kpiMap_w; break;
				case 'b': itemsInProgress = kpiMap_b; break;
			}

			buildInProgress(region, itemsInProgress);
		}
	}
	
	//-------------------------------------------------------------------------
	private void buildQueryItemsMap()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> itemsQuery = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': itemsQuery = kpiMap_s; break;
				case 'm': itemsQuery = kpiMap_m; break;
				case 'w': itemsQuery = kpiMap_w; break;
				case 'b': itemsQuery = kpiMap_b; break;
			}

			buildQuery(region, itemsQuery);
		}
	}
	
	
	/**
	 * Run the query to get the totals only once per region. It's a bigun.
	 *
	 * @author matt
	 */
	private void buildTargets(String region, HashMap<String, String> itemTotals)
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "the10b-ItemsTotals.sql");

	        Logger.getLogger("com.amdocs.kpi").info(strQuery);

	        pstmt = conn.prepareStatement(strQuery);           

            rs = pstmt.executeQuery();
           
            if(rs.next())
            {
            	String strCount = rs.getString(1);
            	String strRevenue = rs.getString(2);

            	itemTotals.put("countTarget", strCount);
            	itemTotals.put("revenueTarget", strRevenue);
            	
        		countTarget += Integer.parseInt(strCount);
        		revenueTarget += Double.parseDouble(strRevenue);
            }

            Logger.getLogger("com.amdocs.kpi").info("********Totals Read from Database");

		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Items Query: Exception querying ACRM totals, message: " + e.getMessage());
		}		

	}

	
	//-------------------------------------------------------------------------
	private void buildFilesCompletedItems()
	{
		Logger.getLogger("com.amdocs.kpi").info("********Building the Completed Items Files");

		//Loop over all Items (per region) and create count 
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> itemsCompleted = null;

		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		//reset variables for counts & revenue
		countTotal = 0;
		revenueTotal = 0.0;

		//During these iterations:
		//
		//   (1) Calculate the summary info.
		//   (2) Build the Details XML file.
		//
		try
		{
			//Start a results XML file
			File f = new File(detailOutputFile);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
			writer.writeXMLHeader();
			writer.writeEntity("results");
			
			Logger.getLogger("com.amdocs.kpi").info("********Building the Completed Items Detail File");
			while(st.hasMoreTokens())
			{
				String region = st.nextToken();
				switch(region.toLowerCase().toCharArray()[0])
				{
					case 's': itemsCompleted = kpiMap_s; break;
					case 'm': itemsCompleted = kpiMap_m; break;
					case 'w': itemsCompleted = kpiMap_w; break;
					case 'b': itemsCompleted = kpiMap_b; break;			
				}

				String regionName = AttUtils.getRegionName(region);
				
				Collection<HashMap<String, String>> c = itemsCompleted.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
		
				while(itr.hasNext())
				{
					HashMap<String, String> map = itr.next();
					
					String udacType = map.get(KEY_UDAC_TYPE);
					String strRevenue = map.get(KEY_SFA_NISD_AMT);
					double revenue = Double.valueOf(strRevenue.trim()).doubleValue();

					//Increment the totals...
					countTotal++;
					revenueTotal += revenue;

					// (1) Calculate the Summary Info
					//
					
					//get the results for this key
					HashMap<String, String> resMap = results.get(udacType);
					
					//the results exist, increment count
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
						//otherwise, create entity for this udactype
						resMap = new HashMap<String, String>(3);

						resMap.put(KEY_UDAC_TYPE, udacType);						
						resMap.put(KEY_COUNT, "1");
						resMap.put(KEY_REVENUE, String.valueOf(revenue));					
					}

					
					// (2) Build the Details XML file.
					//
					
					String customerId = map.get(KEY_CUSTOMER_ID);		
					String salesRep = map.get(KEY_SALES_REP);
					String salesRepId = map.get(KEY_SALES_REP_ID);
			    	String salesManager = map.get(KEY_SALES_MANAGER);
			    	String salesManagerId = map.get(KEY_SALES_MANAGER_ID);
			    	String generalManager = map.get(KEY_GENERAL_MANAGER);
			    	String generalManagerId = map.get(KEY_GENERAL_MANAGER_ID);
			    	String findingName = map.get(KEY_FINDING_NAME);
			    	String productName = map.get(KEY_PRODUCT_NAME);
			    	String issueDate = map.get(KEY_ISSUE_DATE);
			    	String itemId = map.get(KEY_ITEM_ID);
			    	String pricePlanId = map.get(KEY_PRICEPLAN_ID);
			    	String udac = map.get(KEY_UDAC);
			    	String heading = map.get(KEY_HEADING);
			    	String atn = map.get(KEY_ATN);	
			    	String aliCode = map.get(KEY_ALI_CODE);		
			    	String changeRequest = map.get(KEY_CHANGE_REQUEST);	
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
					writer.writeAttribute("findingName", findingName);
					writer.writeAttribute("productName", productName);
					writer.writeAttribute("issueDate", issueDate);
					writer.writeAttribute("itemId", itemId);
					writer.writeAttribute("pricePlanId", pricePlanId);
					writer.writeAttribute("udac", udac);
					writer.writeAttribute("heading", heading);
					writer.writeAttribute("atn", atn);
					writer.writeAttribute("aliCode", aliCode);
					writer.writeAttribute("changeRequest", changeRequest);
					writer.writeAttribute("sfaBotsAmt", sfaBotsAmt);
					writer.writeAttribute("sfaNisdAmt", sfaNisdAmt);
				    writer.writeAttribute("region", regionName);
					writer.writeAttribute("vertical", vertical);
					writer.writeAttribute("category", category);
					writer.writeAttribute("ppcInd", ppcInd);
					writer.writeAttribute("officeCode", officeCode);
					writer.endEntity();
					
					results.put(udacType, resMap);
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
		buildItemSummaryXMLFileByUdacType(results, summaryOutputFile);
		buildSummaryTotalXMLFile(summaryTotalOutputFile, masterSummaryTotalOutputFile);

		//clear the trees, so we can reuse them.
		kpiMap_s.clear();
		kpiMap_m.clear();
		kpiMap_w.clear();
		kpiMap_b.clear();		
	}


	//-------------------------------------------------------------------------
	private void buildFilesPendingFulWaitingItems()
	{
		Logger.getLogger("com.amdocs.kpi").info("********Building the Pending Items Files");

		//Loop over all Items (per region) and create count 
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
        TreeMap<String, HashMap<String, String>> pendingItems = null;

		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		//reset variables for counts & revenue
		countTotal = 0;
		revenueTotal = 0.0;

		//During these iterations:
		//
		//   (1) Calculate the summary info.
		//   (2) Build the Details XML file.
		//
		try
		{
			//Start a results XML file
			File f = new File(detailOutputFilePendingItems);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
			writer.writeXMLHeader();
			writer.writeEntity("results");
			
			Logger.getLogger("com.amdocs.kpi").info("********Building the Pending Items Detail File");
			while(st.hasMoreTokens())
			{
				String region = st.nextToken();
				switch(region.toLowerCase().toCharArray()[0])
				{
					case 's': pendingItems = kpiMap_s; break;
					case 'm': pendingItems = kpiMap_m; break;
					case 'w': pendingItems = kpiMap_w; break;
					case 'b': pendingItems = kpiMap_b; break;			
				}

				String regionName = AttUtils.getRegionName(region);
				
				Collection<HashMap<String, String>> c = pendingItems.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
		
				while(itr.hasNext())
				{
					HashMap<String, String> map = itr.next();
					
					String udacType = map.get(KEY_UDAC_TYPE);
					String strRevenue = map.get(KEY_SFA_NISD_AMT);
					double revenue = Double.valueOf(strRevenue.trim()).doubleValue();

					//Increment the totals...
					countTotal++;
					revenueTotal += revenue;

					// (1) Calculate the Summary Info
					//
					
					//get the results for this key
					HashMap<String, String> resMap = results.get(udacType);
					
					//the results exist, increment count
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
						//otherwise, create entity for this udactype
						resMap = new HashMap<String, String>(3);

						resMap.put(KEY_UDAC_TYPE, udacType);						
						resMap.put(KEY_COUNT, "1");
						resMap.put(KEY_REVENUE, String.valueOf(revenue));					
					}


					// (2) Build the Details XML file.
					
					String customerId = map.get(KEY_CUSTOMER_ID);		
					String salesRep = map.get(KEY_SALES_REP);
					String salesRepId = map.get(KEY_SALES_REP_ID);
	            	String salesManager = map.get(KEY_SALES_MANAGER);
	            	String salesManagerId = map.get(KEY_SALES_MANAGER_ID);
	            	String generalManager = map.get(KEY_GENERAL_MANAGER);
	            	String generalManagerId = map.get(KEY_GENERAL_MANAGER_ID);
	            	String pendingStatus = map.get(KEY_PENDING_STATUS);
	            	String openQuery = map.get(KEY_OPEN_QUERY);            	
	            	String findingName = map.get(KEY_FINDING_NAME);
	            	String productName = map.get(KEY_PRODUCT_NAME);
	            	String issueDate = map.get(KEY_ISSUE_DATE);
	            	String itemId = map.get(KEY_ITEM_ID);
	            	String pricePlanId = map.get(KEY_PRICEPLAN_ID);
	            	String udac = map.get(KEY_UDAC);
	            	String heading = map.get(KEY_HEADING);
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
					writer.writeAttribute("pendingStatus", pendingStatus);
					writer.writeAttribute("openQuery", openQuery);
					writer.writeAttribute("findingName", findingName);
					writer.writeAttribute("productName", productName);
					writer.writeAttribute("issueDate", issueDate);
					writer.writeAttribute("itemId", itemId);
					writer.writeAttribute("pricePlanId", pricePlanId);
					writer.writeAttribute("udac", udac);
					writer.writeAttribute("heading", heading);
					writer.writeAttribute("atn", atn);
					writer.writeAttribute("aliCode", aliCode);
					writer.writeAttribute("sfaBotsAmt", sfaBotsAmt);
					writer.writeAttribute("sfaNisdAmt", sfaNisdAmt);
					writer.writeAttribute("region", regionName);
					writer.writeAttribute("vertical", vertical);
					writer.writeAttribute("category", category);
					writer.writeAttribute("ppcInd", ppcInd);
					writer.writeAttribute("udacType", udacType);
					writer.writeAttribute("officeCode", officeCode);
					writer.endEntity();
					
					results.put(udacType, resMap);
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


		//TODO: will the following work as-is?
		
		//Build the output XML files
		buildItemSummaryXMLFileByUdacType(results, summaryOutputFilePendingItems);
		buildSummaryTotalXMLFile(summaryTotalOutputFilePendingItems, masterSummaryTotalOutputFilePendingItems);

		//clear the trees, so we can reuse them.
		kpiMap_s.clear();
		kpiMap_m.clear();
		kpiMap_w.clear();
		kpiMap_b.clear();
	}

	//-------------------------------------------------------------------------
	private void buildFilesInProgressItems()
	{
		Logger.getLogger("com.amdocs.kpi").info("********Building the In Progress Items Files");

		//Loop over all Items (per region) and create count 
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> itemsInProgress = null;

		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		//reset variables for counts & revenue
		countTotal = 0;
		revenueTotal = 0.0;

		//During these iterations:
		//
		//   (1) Calculate the summary info.
		//   (2) Build the Details XML file.
		//
		try
		{
			//Start a results XML file
			File f = new File(detailOutputFileInProgressItems);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
			writer.writeXMLHeader();
			writer.writeEntity("results");
			
			Logger.getLogger("com.amdocs.kpi").info("********Building the In Progress Items Detail File");
			while(st.hasMoreTokens())
			{
				String region = st.nextToken();
				switch(region.toLowerCase().toCharArray()[0])
				{
					case 's': itemsInProgress = kpiMap_s; break;
					case 'm': itemsInProgress = kpiMap_m; break;
					case 'w': itemsInProgress = kpiMap_w; break;
					case 'b': itemsInProgress = kpiMap_b; break;			
				}

				String regionName = AttUtils.getRegionName(region);
				
				Collection<HashMap<String, String>> c = itemsInProgress.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
		
				while(itr.hasNext())
				{
					HashMap<String, String> map = itr.next();
					
					String udacType = map.get(KEY_UDAC_TYPE);
					String strRevenue = map.get(KEY_SFA_NISD_AMT);
					double revenue = Double.valueOf(strRevenue.trim()).doubleValue();

					//Increment the totals...
					countTotal++;
					revenueTotal += revenue;

					// (1) Calculate the Summary Info
					//
					
					//get the results for this key
					HashMap<String, String> resMap = results.get(udacType);
					
					//the results exist, increment count
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
						//otherwise, create entity for this udactype
						resMap = new HashMap<String, String>(3);

						resMap.put(KEY_UDAC_TYPE, udacType);						
						resMap.put(KEY_COUNT, "1");
						resMap.put(KEY_REVENUE, String.valueOf(revenue));					
					}


					// (2) Build the Details XML file.
					
					String customerId = map.get(KEY_CUSTOMER_ID);		
					String salesRep = map.get(KEY_SALES_REP);
					String salesRepId = map.get(KEY_SALES_REP_ID);
	            	String salesManager = map.get(KEY_SALES_MANAGER);
	            	String salesManagerId = map.get(KEY_SALES_MANAGER_ID);
	            	String generalManager = map.get(KEY_GENERAL_MANAGER);
	            	String generalManagerId = map.get(KEY_GENERAL_MANAGER_ID);
	            	String inProgressStatus = map.get(KEY_PROGRESS_STATUS);	
	            	String findingName = map.get(KEY_FINDING_NAME);
	            	String productName = map.get(KEY_PRODUCT_NAME);
	            	String issueDate = map.get(KEY_ISSUE_DATE);
	            	String itemId = map.get(KEY_ITEM_ID);
	            	String pricePlanId = map.get(KEY_PRICEPLAN_ID);
	            	String udac = map.get(KEY_UDAC);
	            	String heading = map.get(KEY_HEADING);
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
					writer.writeAttribute("inProgressStatus", inProgressStatus);
					writer.writeAttribute("findingName", findingName);
					writer.writeAttribute("productName", productName);
					writer.writeAttribute("issueDate", issueDate);
					writer.writeAttribute("itemId", itemId);
					writer.writeAttribute("pricePlanId", pricePlanId);
					writer.writeAttribute("udac", udac);
					writer.writeAttribute("heading", heading);
					writer.writeAttribute("atn", atn);
					writer.writeAttribute("aliCode", aliCode);
					writer.writeAttribute("sfaBotsAmt", sfaBotsAmt);
					writer.writeAttribute("sfaNisdAmt", sfaNisdAmt);
					writer.writeAttribute("region", regionName);
					writer.writeAttribute("vertical", vertical);
					writer.writeAttribute("category", category);
					writer.writeAttribute("ppcInd", ppcInd);
					writer.writeAttribute("udacType", udacType);
					writer.writeAttribute("officeCode", officeCode);
					writer.endEntity();
					
					results.put(udacType, resMap);
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
		
		
		//TODO: will the following work as-is?
		
		//Build the output XML files
		buildItemSummaryXMLFileByUdacType(results, summaryOutputFileInProgressItems);
		buildSummaryTotalXMLFile(summaryTotalOutputFileInProgressItems, masterSummaryTotalOutputFileInProgressItems);

		//clear the trees, so we can reuse them.
		kpiMap_s.clear();
		kpiMap_m.clear();
		kpiMap_w.clear();
		kpiMap_b.clear();
	}

	//-------------------------------------------------------------------------
	private void buildFilesQueryItems()
	{
		Logger.getLogger("com.amdocs.kpi").info("********Building the Query Items Files");

		//Loop over all Items (per region) and create count 
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
        TreeMap<String, HashMap<String, String>> queriedItems = null;

		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		//reset variables for counts & revenue
		countTotal = 0;
		revenueTotal = 0.0;

		//During these iterations:
		//
		//   (1) Calculate the summary info.
		//   (2) Build the Details XML file.
		//
		try
		{
			//Start a results XML file
			File f = new File(detailOutputFileQueryItems);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
			writer.writeXMLHeader();
			writer.writeEntity("results");
			
			Logger.getLogger("com.amdocs.kpi").info("********Building the Query Items Detail File");
			while(st.hasMoreTokens())
			{
				String region = st.nextToken();
				switch(region.toLowerCase().toCharArray()[0])
				{
					case 's': queriedItems = kpiMap_s; break;
					case 'm': queriedItems = kpiMap_m; break;
					case 'w': queriedItems = kpiMap_w; break;
					case 'b': queriedItems = kpiMap_b; break;			
				}

				String regionName = AttUtils.getRegionName(region);
				
				Collection<HashMap<String, String>> c = queriedItems.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
		
				while(itr.hasNext())
				{
					HashMap<String, String> map = itr.next();
					
					String udacType = map.get(KEY_UDAC_TYPE);
					String strRevenue = map.get(KEY_SFA_NISD_AMT);
					double revenue = Double.valueOf(strRevenue.trim()).doubleValue();

					//Increment the totals...
					countTotal++;
					revenueTotal += revenue;

					// (1) Calculate the Summary Info
					//
					
					//get the results for this key
					HashMap<String, String> resMap = results.get(udacType);
					
					//the results exist, increment count
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
						//otherwise, create entity for this udactype
						resMap = new HashMap<String, String>(3);

						resMap.put(KEY_UDAC_TYPE, udacType);						
						resMap.put(KEY_COUNT, "1");
						resMap.put(KEY_REVENUE, String.valueOf(revenue));					
					}

					// (2) Build the Details XML file.

					String customerId = map.get(KEY_CUSTOMER_ID);		
					String salesRep = map.get(KEY_SALES_REP);
					String salesRepId = map.get(KEY_SALES_REP_ID);
	            	String salesManager = map.get(KEY_SALES_MANAGER);
	            	String salesManagerId = map.get(KEY_SALES_MANAGER_ID);
	            	String generalManager = map.get(KEY_GENERAL_MANAGER);
	            	String generalManagerId = map.get(KEY_GENERAL_MANAGER_ID);
	            	String queryCode = map.get(KEY_QUERY_CODE);
	            	String queryGroup = map.get(KEY_QUERY_GROUP);
	            	String queryLevel = map.get(KEY_QUERY_LEVEL);	            	
	            	String findingName = map.get(KEY_FINDING_NAME);
	            	String productName = map.get(KEY_PRODUCT_NAME);
	            	String issueDate = map.get(KEY_ISSUE_DATE);
	            	String itemId = map.get(KEY_ITEM_ID);
	            	String pricePlanId = map.get(KEY_PRICEPLAN_ID);
	            	String udac = map.get(KEY_UDAC);
	            	String heading = map.get(KEY_HEADING);
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
					writer.writeAttribute("queryCode", queryCode);
					writer.writeAttribute("queryGroup", queryGroup);
					writer.writeAttribute("queryLevel", queryLevel);
					writer.writeAttribute("findingName", findingName);
					writer.writeAttribute("productName", productName);
					writer.writeAttribute("issueDate", issueDate);
					writer.writeAttribute("itemId", itemId);
					writer.writeAttribute("pricePlanId", pricePlanId);
					writer.writeAttribute("udac", udac);
					writer.writeAttribute("heading", heading);
					writer.writeAttribute("atn", atn);
					writer.writeAttribute("aliCode", aliCode);
					writer.writeAttribute("sfaBotsAmt", sfaBotsAmt);
					writer.writeAttribute("sfaNisdAmt", sfaNisdAmt);
					writer.writeAttribute("region", regionName);
					writer.writeAttribute("vertical", vertical);
					writer.writeAttribute("category", category);
					writer.writeAttribute("ppcInd", ppcInd);
					writer.writeAttribute("udacType", udacType);
					writer.writeAttribute("officeCode", officeCode);
					writer.endEntity();
					
					results.put(udacType, resMap);
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

		
		//TODO: will the following work as-is?
		
		//Build the output XML files
		buildItemSummaryXMLFileByUdacType(results, summaryOutputFileQueryItems);
		buildSummaryTotalXMLFile(summaryTotalOutputFileQueryItems, masterSummaryTotalOutputFileQueryItems);

		//clear the trees, so we can reuse them.
		kpiMap_s.clear();
		kpiMap_m.clear();
		kpiMap_w.clear();
		kpiMap_b.clear();
	}
	
	
	/**
	 * Build a summary results file based on Udac Type.
	 * 
	 * TODO: We might be able to use this method for the other Item types...
	 * 
	 * 
	 * @param results
	 * @param outputFile
	 */
	protected void buildItemSummaryXMLFileByUdacType(HashMap<String, HashMap<String, String>> results, String outputFile)
	{
		try
		{
			Logger.getLogger("com.amdocs.kpi").info("********Building the Completed Items Summary File");
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
	
	        	String udacType = map.get(KEY_UDAC_TYPE);  
	        	String strCount = map.get(KEY_COUNT);
	        	String strRevenue = map.get(KEY_REVENUE);
        	 	
				writer.writeEntity("result");
				writer.writeAttribute("udacType", udacType);
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
	protected void buildSummaryTotalXMLFile(String outputFile, String masterFile)
	{
		try
		{
			Logger.getLogger("com.amdocs.kpi").info("********Building the Summary Total File");
	        //Actually build the XML file
			File f = new File(outputFile);
			FileWriter fw = new FileWriter(f);
			XMLWriter writer = new XMLWriter(fw);
	
			writer.writeXMLHeader();
			writer.writeEntity("results");

			//Add a special result entity for the totals.
			writer.writeEntity("result");
			writer.writeAttribute("udacType", "ALL");
			writer.writeAttribute("countTotal", String.valueOf(countTotal));
			writer.writeAttribute("revenueTotal", String.valueOf(revenueTotal));
			writer.writeAttribute("countTarget", String.valueOf(countTarget));
			writer.writeAttribute("revenueTarget", String.valueOf(revenueTarget));
       		writer.endEntity();

			writer.endEntity();
			writer.close();
			fw.close();

			Logger.getLogger("com.amdocs.kpi").info("Copying file " + outputFile + " to " + masterFile + ".");		
			copyFile(new File(outputFile), new File(masterFile));
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Exception creating XML file: " + outputFile + ", exception: " + e.getMessage() );
		}
	}
	
	
	/**
	 * Build the map's for Completed Items.
	 * 
	 * @param region
	 * @param itemsCompleted
	 * 
	 * @author matt
	 */
	private void buildItemsCompleted(String region, TreeMap<String, HashMap<String, String>> itemsCompleted)
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "the10b-CompletedItems.sql");

	        Logger.getLogger("com.amdocs.kpi").info(strQuery);

	        pstmt = conn.prepareStatement(strQuery);
            pstmt.setFetchSize(1000); //mateo - bring bigger chunks

            rs = pstmt.executeQuery();

            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(27);

            	String customerId = rs.getString(1);
            	String salesRep = rs.getString(2);
            	String salesRepId = rs.getString(3);
            	String salesManager = rs.getString(4);
            	String salesManagerId = rs.getString(5);
            	String generalManager = rs.getString(6);
            	String generalManagerId = rs.getString(7);
            	String findingName = rs.getString(8);
            	String productName = rs.getString(9);
            	String issueDate = rs.getString(10);
            	String itemId = rs.getString(11);
            	String pricePlanId = rs.getString(12);
            	String udac = rs.getString(13);
            	String heading = rs.getString(14);
            	String atn = rs.getString(15);
            	String aliCode = rs.getString(16);
            	String changeRequest = rs.getString(17);
            	String sfaBotsAmt = rs.getString(18);
            	String sfaNisdAmt = rs.getString(19);
            	String vertical = rs.getString(21);
            	if(vertical == null) vertical = " ";
            	String category = rs.getString(22);
            	if(category == null) category = " ";
            	String ppcInd = rs.getString(23);
            	if(ppcInd == null) ppcInd = " ";
            	String udacType = rs.getString(24);
            	String officeCode = rs.getString(25);

				if(findingName == null) findingName = "";
				
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_SALES_REP, salesRep);
            	map.put(KEY_SALES_REP_ID, salesRepId);
            	map.put(KEY_SALES_MANAGER, salesManager);
            	map.put(KEY_SALES_MANAGER_ID, salesManagerId);
				map.put(KEY_GENERAL_MANAGER, generalManager);
				map.put(KEY_GENERAL_MANAGER_ID, generalManagerId);
            	map.put(KEY_FINDING_NAME, findingName);
            	map.put(KEY_PRODUCT_NAME, productName);
            	map.put(KEY_ISSUE_DATE, issueDate);
            	map.put(KEY_ITEM_ID, itemId);
            	map.put(KEY_PRICEPLAN_ID, pricePlanId);
            	map.put(KEY_UDAC, udac);
            	map.put(KEY_HEADING, heading);
            	map.put(KEY_ATN, atn);
            	map.put(KEY_ALI_CODE, aliCode);
            	map.put(KEY_CHANGE_REQUEST, changeRequest);
            	map.put(KEY_SFA_BOTS_AMT, sfaBotsAmt);
            	map.put(KEY_SFA_NISD_AMT, sfaNisdAmt);
            	map.put(KEY_VERTICAL, vertical);
            	map.put(KEY_CATEGORY, category);
            	map.put(KEY_PPCIND, ppcInd);
            	map.put(KEY_UDAC_TYPE, udacType);
            	map.put(KEY_OFFICECODE, officeCode);
    			
    			itemsCompleted.put(customerId, map);
             }
            
            Logger.getLogger("com.amdocs.kpi").info("********Complete Items Records Read from Database");
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Completed Item Status: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}

	
	/**
	 * Build the maps for the Pending Fulfillment / Waiting items.
	 * 
	 * @param region
	 * @param pendingItems
	 */
	private void buildPendingFulWaiting(String region, TreeMap<String, HashMap<String, String>> pendingItems)
	{
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "the10b-PendingItems.sql"); //TODO: write this query. File was empty...

	        Logger.getLogger("com.amdocs.kpi").info(strQuery);

	        pstmt = conn.prepareStatement(strQuery);
            pstmt.setFetchSize(1000); //mateo - bring bigger chunks

            rs = pstmt.executeQuery();
            
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(26);

                String customerId = rs.getString(1);
            	String salesRep = rs.getString(2);
            	String salesRepId = rs.getString(3);
            	String salesManager = rs.getString(4);
            	String salesManagerId = rs.getString(5);
            	String generalManager = rs.getString(6);
            	String generalManagerId = rs.getString(7);
            	String pendingStatus = rs.getString(8);
            	String openQuery = rs.getString(9);
            	String findingName = rs.getString(10);
            	String productName = rs.getString(11);
            	String issueDate = rs.getString(12);
            	String itemId = rs.getString(13);
            	String pricePlanId = rs.getString(14);
            	String udac = rs.getString(15);
            	String heading = rs.getString(16);
            	String atn = rs.getString(17);
            	String aliCode = rs.getString(18);
            	String sfaBotsAmt = rs.getString(19);
            	String sfaNisdAmt = rs.getString(20);
            	String vertical = rs.getString(21);
            	if(vertical == null) vertical = " ";
            	String category = rs.getString(22);
            	if(category == null) category = " ";
            	String ppcInd = rs.getString(23);
            	if(ppcInd == null) ppcInd = " ";
            	String udacType = rs.getString(24);
            	String officeCode = rs.getString(25);
           	
				if(findingName == null) findingName = "";
				
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_SALES_REP, salesRep);
            	map.put(KEY_SALES_REP_ID, salesRepId);
            	map.put(KEY_SALES_MANAGER, salesManager);
            	map.put(KEY_SALES_MANAGER_ID, salesManagerId);
				map.put(KEY_GENERAL_MANAGER, generalManager);
				map.put(KEY_GENERAL_MANAGER_ID, generalManagerId);
				map.put(KEY_PENDING_STATUS, pendingStatus);
				map.put(KEY_OPEN_QUERY, openQuery);
            	map.put(KEY_FINDING_NAME, findingName);
            	map.put(KEY_PRODUCT_NAME, productName);
            	map.put(KEY_ISSUE_DATE, issueDate);
            	map.put(KEY_ITEM_ID, itemId);
            	map.put(KEY_PRICEPLAN_ID, pricePlanId);
            	map.put(KEY_UDAC, udac);
            	map.put(KEY_HEADING, heading);
            	map.put(KEY_ATN, atn);
            	map.put(KEY_ALI_CODE, aliCode);
            	map.put(KEY_SFA_BOTS_AMT, sfaBotsAmt);
            	map.put(KEY_SFA_NISD_AMT, sfaNisdAmt);
            	map.put(KEY_VERTICAL, vertical);
            	map.put(KEY_CATEGORY, category);
            	map.put(KEY_PPCIND, ppcInd);
            	map.put(KEY_UDAC_TYPE, udacType);
            	map.put(KEY_OFFICECODE, officeCode);
    			
    			pendingItems.put( customerId, map);
            }
            
            Logger.getLogger("com.amdocs.kpi").info("********Pending Item Records Read from Database");
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Pending Item Status: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}


	/**
	 * Build the maps for the In Progress items.
	 *  
	 * @param region
	 * @param itemsInProgress
	 */
	private void buildInProgress(String region, TreeMap<String, HashMap<String, String>> itemsInProgress)
	{
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "the10b-InProgressItems.sql");

	        Logger.getLogger("com.amdocs.kpi").info(strQuery);

	        pstmt = conn.prepareStatement(strQuery);
            pstmt.setFetchSize(1000); //mateo - bring bigger chunks

            rs = pstmt.executeQuery();
            
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(25);

                String customerId = rs.getString(1);
            	String salesRep = rs.getString(2);
            	String salesRepId = rs.getString(3);
            	String salesManager = rs.getString(4);
            	String salesManagerId = rs.getString(5);
            	String generalManager = rs.getString(6);
            	String generalManagerId = rs.getString(7);
            	String inProgressStatus = rs.getString(8);
            	String findingName = rs.getString(9);
            	String productName = rs.getString(10);
            	String issueDate = rs.getString(11);
            	String itemId = rs.getString(12);
            	String pricePlanId = rs.getString(13);
            	String udac = rs.getString(14);
            	String heading = rs.getString(15);
            	String atn = rs.getString(16);
            	String aliCode = rs.getString(17);
            	String sfaBotsAmt = rs.getString(18);
            	String sfaNisdAmt = rs.getString(19);
            	String vertical = rs.getString(20);
            	if(vertical == null) vertical = " ";
            	String category = rs.getString(21);
            	if(category == null) category = " ";
            	String ppcInd = rs.getString(22);
            	if(ppcInd == null) ppcInd = " ";
            	String udacType = rs.getString(23);
            	String officeCode = rs.getString(24);
           	
				if(findingName == null) findingName = "";
				
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_SALES_REP, salesRep);
            	map.put(KEY_SALES_REP_ID, salesRepId);
            	map.put(KEY_SALES_MANAGER, salesManager);
            	map.put(KEY_SALES_MANAGER_ID, salesManagerId);
				map.put(KEY_GENERAL_MANAGER, generalManager);
				map.put(KEY_GENERAL_MANAGER_ID, generalManagerId);
            	map.put(KEY_PROGRESS_STATUS, inProgressStatus);
            	map.put(KEY_FINDING_NAME, findingName);
            	map.put(KEY_PRODUCT_NAME, productName);
            	map.put(KEY_ISSUE_DATE, issueDate);
            	map.put(KEY_ITEM_ID, itemId);
            	map.put(KEY_PRICEPLAN_ID, pricePlanId);
            	map.put(KEY_UDAC, udac);
            	map.put(KEY_HEADING, heading);
            	map.put(KEY_ATN, atn);
            	map.put(KEY_ALI_CODE, aliCode);
            	map.put(KEY_SFA_BOTS_AMT, sfaBotsAmt);
            	map.put(KEY_SFA_NISD_AMT, sfaNisdAmt);
            	map.put(KEY_VERTICAL, vertical);
            	map.put(KEY_CATEGORY, category);
            	map.put(KEY_PPCIND, ppcInd);
            	map.put(KEY_UDAC_TYPE, udacType);
            	map.put(KEY_OFFICECODE, officeCode);
    			
    			itemsInProgress.put(itemId, map);
            }
            
            Logger.getLogger("com.amdocs.kpi").info("********In Progress Item Records Read from Database");
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("In Progress Item Status: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}
	
	/**
	 * Build the maps for the Query items.
	 * 
	 * @param region
	 * @param queriedItems
	 */
	private void buildQuery(String region, TreeMap<String, HashMap<String, String>> queriedItems)
	{
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection(region); //regional db connection

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "the10b-QueriedItems.sql");

	        Logger.getLogger("com.amdocs.kpi").info(strQuery);

	        pstmt = conn.prepareStatement(strQuery);
            pstmt.setFetchSize(1000); //mateo - bring bigger chunks

            rs = pstmt.executeQuery();
            
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(26);

                String customerId = rs.getString(1);
            	String salesRep = rs.getString(2);
            	String salesRepId = rs.getString(3);
            	String salesManager = rs.getString(4);
            	String salesManagerId = rs.getString(5);
            	String generalManager = rs.getString(6);
            	String generalManagerId = rs.getString(7);
            	String queryCode = rs.getString(8);
            	String queryGroup = rs.getString(9);
            	String queryLevel = rs.getString(10);
            	String findingName = rs.getString(11);
            	String productName = rs.getString(12);
            	String issueDate = rs.getString(13);
            	String itemId = rs.getString(14);
            	String pricePlanId = rs.getString(15);
            	String udac = rs.getString(16);
            	String heading = rs.getString(17);
            	String atn = rs.getString(18);
            	String aliCode = rs.getString(19);
            	String sfaBotsAmt = rs.getString(20);
            	String sfaNisdAmt = rs.getString(21);
            	String vertical = rs.getString(23);
            	if(vertical == null) vertical = " ";
            	String category = rs.getString(24);
            	if(category == null) category = " ";
            	String ppcInd = rs.getString(25);
            	if(ppcInd == null) ppcInd = " ";
            	String udacType = rs.getString(26);
            	String officeCode = rs.getString(27);
           	
				if(findingName == null) findingName = "";
				
            	map.put(KEY_CUSTOMER_ID, customerId);
            	map.put(KEY_SALES_REP, salesRep);
            	map.put(KEY_SALES_REP_ID, salesRepId);
            	map.put(KEY_SALES_MANAGER, salesManager);
            	map.put(KEY_SALES_MANAGER_ID, salesManagerId);
				map.put(KEY_GENERAL_MANAGER, generalManager);
				map.put(KEY_GENERAL_MANAGER_ID, generalManagerId);
				map.put(KEY_QUERY_CODE, queryCode);
				map.put(KEY_QUERY_GROUP, queryGroup);
				map.put(KEY_QUERY_LEVEL, queryLevel);
            	map.put(KEY_FINDING_NAME, findingName);
            	map.put(KEY_PRODUCT_NAME, productName);
            	map.put(KEY_ISSUE_DATE, issueDate);
            	map.put(KEY_ITEM_ID, itemId);
            	map.put(KEY_PRICEPLAN_ID, pricePlanId);
            	map.put(KEY_UDAC, udac);
            	map.put(KEY_HEADING, heading);
            	map.put(KEY_ATN, atn);
            	map.put(KEY_ALI_CODE, aliCode);
            	map.put(KEY_SFA_BOTS_AMT, sfaBotsAmt);
            	map.put(KEY_SFA_NISD_AMT, sfaNisdAmt);
            	map.put(KEY_VERTICAL, vertical);
            	map.put(KEY_CATEGORY, category);
            	map.put(KEY_PPCIND, ppcInd);
            	map.put(KEY_UDAC_TYPE, udacType);
            	map.put(KEY_OFFICECODE, officeCode);
              			
    			queriedItems.put( customerId, map);
            }
            
            Logger.getLogger("com.amdocs.kpi").info("********Query Item Records Read from Database");
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Query Item Status: Exception querying ACRM info, message: " + e.getMessage());
		}		
	}
	
}
