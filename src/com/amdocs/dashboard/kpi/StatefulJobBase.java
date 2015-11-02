/**
 * Base class for a StatefulJob in the Quartz scheduler.
 */
package com.amdocs.dashboard.kpi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import com.amdocs.dashboard.utils.AttUtils;
import com.amdocs.infra.utils.XMLWriter;

public abstract class StatefulJobBase implements StatefulJob
{
	//general
	protected static final String KEY_AREA = "area";
	protected static final String KEY_CUSTOMER_ID = "customerid";
	protected static final String KEY_NAME = "name";
	protected static final String KEY_USER_ID = "userid";
	protected static final String KEY_BUSINESS_NAME = "businessname";
	protected static final String KEY_STATE = "state";
	protected static final String KEY_EMAIL = "email";

	//eBill
	protected static final String KEY_BILLING_ACCOUNT_ID = "billingaccountid";
	protected static final String KEY_BILLING_NAME = "billingname";
	protected static final String KEY_SUPPRESSION_REASON = "suppressionreason";

	//Payments
	protected static final String KEY_AMOUNT = "amount";
	protected static final String KEY_CREATION_DATE = "creationdate";
	protected static final String KEY_DUE_DATE = "duedate";

	//Summary
	protected static final String KEY_COUNT = "count";
	protected static final String KEY_TARGET = "countTarget";
	protected static final String KEY_REVENUE = "revenue";
	protected static final String KEY_REVENUE_TARGET = "revenueTarget";

    // Service Manager General
    protected static final String KEY_SALES_REP  ="salesRep";
    protected static final String KEY_SALES_REP_ID  ="salesRepId";
    protected static final String KEY_SALES_MANAGER = "salesManager";
    protected static final String KEY_SALES_MANAGER_ID = "salesManagerId";
	protected static final String KEY_GENERAL_MANAGER = "generalManager";
	protected static final String KEY_GENERAL_MANAGER_ID = "generalManagerId";
    protected static final String KEY_FINDING_NAME = "findingName";
    protected static final String KEY_PRODUCT_NAME = "productName";
    protected static final String KEY_ISSUE_DATE = "issueDate";
    protected static final String KEY_ATN = "atn";
    protected static final String KEY_ALI_CODE = "aliCode";
    protected static final String KEY_LISTED_TN = "listedTn";
    protected static final String KEY_SFA_BOTS_AMT = "sfaBotsAmt";
    protected static final String KEY_SFA_NISD_AMT = "sfaNisdAmt";
    protected static final String KEY_REGION = "region";
    protected static final String KEY_VERTICAL = "vertical";
    protected static final String KEY_CATEGORY = "category";
    protected static final String KEY_PPCIND = "ppcInd";
    protected static final String KEY_OFFICECODE = "officeCode";
    
    // Service Manager Contract
    protected static final String KEY_CONTRACT_STATUS ="contractStatus";
    protected static final String KEY_CONTRACT_ID ="contractId";
    
    // Service Manager item
    protected static final String KEY_PROGRESS_STATUS = "inProgressStatus";
    protected static final String KEY_ITEM_ID = "itemId";
    protected static final String KEY_PRICEPLAN_ID = "pricePlanId";
    protected static final String KEY_UDAC = "udac";
    protected static final String KEY_HEADING = "heading";
    protected static final String KEY_CHANGE_REQUEST = "changeRequest";
    protected static final String KEY_QUERY_CODE = "queryCode";
    protected static final String KEY_QUERY_GROUP = "queryGroup";
    protected static final String KEY_QUERY_LEVEL = "queryLevel";
    protected static final String KEY_UDAC_TYPE = "udacType";
    protected static final String KEY_PENDING_STATUS = "pendingStatus";
    protected static final String KEY_OPEN_QUERY = "openQuery";

    
	//Maps for holding data
	protected TreeMap<String, HashMap<String, String>> kpiMap_s;
	protected TreeMap<String, HashMap<String, String>> kpiMap_m;
	protected TreeMap<String, HashMap<String, String>> kpiMap_w;
	protected TreeMap<String, HashMap<String, String>> kpiMap_b;
	protected TreeMap<String, HashMap<String, String>> kpiMap_o;

	//Default job params
	protected String jobName = "";
	protected String regions = ""; 
	protected String summaryOutputFile = "";
	protected String masterSummaryOutputFile = "";
	protected String detailOutputFile = "";
	protected String masterDetailOutputFile = "";

	//TODO: create these based on properties (from job.xml maybe?)
	//      or get them from somewhere else, like the database.
    private String targetAR;
    private String targetAZ;
    private String targetCT;
    private String targetKS;
    private String targetMO;
    private String targetOK;
    private String targetTX;
    private String targetIL;
    private String targetIN;
    private String targetMI;
    private String targetOH;
    private String targetWI;
    private String targetCA;
    private String targetNV;
    private String targetAL;
    private String targetFL;
    private String targetGA;
    private String targetKY;
    private String targetLA;
    private String targetMS;
    private String targetNC;
    private String targetSC;
    private String targetTN;
    private String targetANON;
	
	//-------------------------------------------------------------------------
	// Method called by the Quartz scheduler.
	public void execute(JobExecutionContext context) throws JobExecutionException 
	{
		jobName = context.getJobDetail().getName();

		Logger.getLogger("com.amdocs.kpi").info(jobName + " job started.");

		boolean success = true;
		JobDataMap m = context.getMergedJobDataMap();

		//Default params
		regions = m.getString("regions"); 
		masterSummaryOutputFile = m.getString("summaryOutputFile");
		summaryOutputFile = appendDateToFilename(masterSummaryOutputFile);
		masterDetailOutputFile = m.getString("detailOutputFile");
		detailOutputFile = appendDateToFilename(masterDetailOutputFile);	
		
		if((m.getString("targets") != null) && (m.getString("targets").compareToIgnoreCase("true") == 0))
		{
			setTargets(m);
		}

		createMaps();

		try
		{
			buildKpi(m);
		}
		catch (Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe(jobName + " encountered the following problem: " + e.getMessage());
			success = false;
		}

		if(success)
		{
			copyToMaster();
			Logger.getLogger("com.amdocs.kpi").info(jobName + " job completed successfully.");
		}
		else
		{
			Logger.getLogger("com.amdocs.kpi").info(jobName + " job completed with errors.");
		}
	}

	//-------------------------------------------------------------------------
	abstract protected void buildKpi(JobDataMap m) throws Exception;

	//-------------------------------------------------------------------------
	protected void copyToMaster()
	{
		try
		{
			Logger.getLogger("com.amdocs.kpi").info("Copying file " + summaryOutputFile + " to " + masterSummaryOutputFile + ".");
			copyFile(new File(summaryOutputFile), new File(masterSummaryOutputFile));

			Logger.getLogger("com.amdocs.kpi").info("Copying file " + detailOutputFile + " to " + masterDetailOutputFile + ".");		
			copyFile(new File(detailOutputFile), new File(masterDetailOutputFile));
		}
		catch (Exception e) 
		{
			Logger.getLogger("com.amdocs.kpi").severe(jobName + ": error writing master file: " + e.getMessage());
		}		
	}
	
	//-------------------------------------------------------------------------
	protected void createMaps()
	{
        //TODO: make the TreeMaps's generic - not hard coded names
        kpiMap_s = new TreeMap<String, HashMap<String,String>>();
    	kpiMap_m = new TreeMap<String, HashMap<String,String>>();
    	kpiMap_w = new TreeMap<String, HashMap<String,String>>();
    	kpiMap_b = new TreeMap<String, HashMap<String,String>>();
    	kpiMap_o = new TreeMap<String, HashMap<String,String>>(); //TODO: don't create if not needed.
	}

	//-------------------------------------------------------------------------
	protected void addRegionalACRMInfo()
	{
		//for all Regional Databases.
		StringTokenizer st = new StringTokenizer(regions, ",");

		boolean addInfo = true;

		while(st.hasMoreTokens())
		{
			String region = st.nextToken();

			TreeMap<String, HashMap<String, String>> kpiMap = null;

			switch(region.toLowerCase().toCharArray()[0])
			{
				case 's': kpiMap = kpiMap_s; break;
				case 'm': kpiMap = kpiMap_m; break;
				case 'w': kpiMap = kpiMap_w; break;
				case 'b': kpiMap = kpiMap_b; break;
				case 'o': addInfo = false; break; //No ACRM info for Other (anonymous) users
			}

			if(addInfo)
				addACRMInfo(region, kpiMap);
			else
				addInfo = true;
		}
	}

	//-------------------------------------------------------------------------
	protected void addACRMInfo(String region, TreeMap<String, HashMap<String, String>> kpiMap)
	{
		//Not truly abstract, because KPI's don't need to have ACRM info.
		Logger.getLogger("com.amdocs.kpi").severe(jobName + " MUST overload addACRMInfo() method -or- stop calling addRegionalACRMInfo().");
	}

	
	//-------------------------------------------------------------------------
	protected void setTargets(JobDataMap m)
	{
		//Read targets from jobs.xml for now.
		targetAR = m.getString("targetAR");
		targetAZ = m.getString("targetAZ");
		targetCT = m.getString("targetCT");
		targetKS = m.getString("targetKS");
		targetMO = m.getString("targetMO");
		targetOK = m.getString("targetOK");
		targetTX = m.getString("targetTX");
		targetIL = m.getString("targetIL");
		targetIN = m.getString("targetIN");
		targetMI = m.getString("targetMI");
		targetOH = m.getString("targetOH");
		targetWI = m.getString("targetWI");
		targetCA = m.getString("targetCA");
		targetNV = m.getString("targetNV");		
		targetAL = m.getString("targetAL");
		targetFL = m.getString("targetFL");
		targetGA = m.getString("targetGA");
		targetKY = m.getString("targetKY");
		targetLA = m.getString("targetLA");
		targetMS = m.getString("targetMS");
		targetNC = m.getString("targetNC");
		targetSC = m.getString("targetSC");
		targetTN = m.getString("targetTN");
		targetANON = m.getString("targetANON");
	}

	//-------------------------------------------------------------------------
	protected String getStateTarget(String state)
	{
		String strTarget = "";
		
		//TODO: make this dynamic.
    	switch(AttUtils.inRegionStateList.valueOf(state.toUpperCase()))
    	{
        	//Southwest states
        	case AR: strTarget = targetAR; break;
        	case AZ: strTarget = targetAZ; break;
        	case CT: strTarget = targetCT; break;
        	case KS: strTarget = targetKS; break;
        	case MO: strTarget = targetMO; break;
        	case OK: strTarget = targetOK; break;
        	case TX: strTarget = targetTX; break;
        	//Midwest states
        	case IL: strTarget = targetIL; break;
        	case IN: strTarget = targetIN; break;
        	case MI: strTarget = targetMI; break;
        	case OH: strTarget = targetOH; break;
        	case WI: strTarget = targetWI; break;
        	//West states
        	case CA: strTarget = targetCA; break;
        	case NV: strTarget = targetNV; break;
        	//Southeast states
        	case AL: strTarget = targetAL; break;
        	case FL: strTarget = targetFL; break;
        	case GA: strTarget = targetGA; break;
        	case KY: strTarget = targetKY; break;
        	case LA: strTarget = targetLA; break;
        	case MS: strTarget = targetMS; break;
        	case NC: strTarget = targetNC; break;
        	case SC: strTarget = targetSC; break;
        	case TN: strTarget = targetTN; break;

        	case NONE: default: strTarget = targetANON; break;
    	}

    	return strTarget;
	}

	//-------------------------------------------------------------------------
	protected String getDate()
	{
		//YYYY-MM-DD
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    return sdf.format(cal.getTime());
	}
	
	//-------------------------------------------------------------------------
	protected String appendDateToFilename(String fileName)
	{
	    String newFileName = fileName.substring(0, fileName.indexOf(".xml")) + "-" + getDate() + ".xml";
		return newFileName;
	}

	//-------------------------------------------------------------------------
    protected void copyFile(File src, File dst) throws IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

	//-------------------------------------------------------------------------
	protected void buildAmssSummaryAndDetailsFiles()
	{
		//Loop over all users (per region) and create count by area and state
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> kpiMap;

		//for all regions
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
					case 's': kpiMap = kpiMap_s; break;
					case 'm': kpiMap = kpiMap_m; break;
					case 'w': kpiMap = kpiMap_w; break;
					case 'b': kpiMap = kpiMap_b; break;
					case 'o': kpiMap = kpiMap_o; break;
					default: kpiMap = kpiMap_o; break;
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
				
				Collection<HashMap<String, String>> c = kpiMap.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
	
				while(itr.hasNext())
				{
					HashMap<String, String> map = itr.next();
					String area = map.get(KEY_AREA);
					String state = map.get(KEY_STATE);
		        	if(state == null) state = "none";

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

					if(businessName == null) businessName = "";
					if(email == null) email = "";

					writer.writeEntity("result");
					writer.writeAttribute("region", area);
					writer.writeAttribute("state", AttUtils.getStateName(state));
					writer.writeAttribute("customerId", customerId);
					writer.writeAttribute("businessName", businessName);
					writer.writeAttribute("email", email);
					writer.writeAttribute("memberId", map.get(KEY_USER_ID));
					writer.endEntity();
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
	
	        	String area = map.get(KEY_AREA);
	        	if(area == null || area == "") area = "Other";
	        	String stateCode = map.get(KEY_STATE);
	        	if(stateCode == null || stateCode == "") stateCode = "none";
	        	String state = AttUtils.getStateName(stateCode);

	        	String strCount = map.get(KEY_COUNT);
	        	String strTarget = getStateTarget(stateCode);
	        	
				writer.writeEntity("result");
				writer.writeAttribute("area", area);
				writer.writeAttribute("value", strCount);        		
				writer.writeAttribute("state", state);

	        	int count = Integer.parseInt(strCount);
	        	int target = Integer.parseInt(strTarget);
	        	int diff = target - count;
	        	
				writer.writeAttribute("target", strTarget);
				writer.writeAttribute("difference", String.valueOf(diff));
				writer.endEntity();
			}
	
			writer.endEntity();
			writer.close();
			fw.close();
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").severe("Exception creating XML file: " + outputFile + ", exception: " + e.getMessage());
		}
	}


}
