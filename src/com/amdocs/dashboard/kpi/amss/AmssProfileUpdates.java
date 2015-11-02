package com.amdocs.dashboard.kpi.amss;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.quartz.JobDataMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.amdocs.dashboard.kpi.StatefulJobBase;
import com.amdocs.dashboard.utils.AttUtils;
import com.amdocs.dashboard.utils.DashboardDBConfig;
import com.amdocs.infra.utils.Config;
import com.amdocs.infra.utils.DBConfig;
import com.amdocs.infra.utils.XMLWriter;

public class AmssProfileUpdates extends StatefulJobBase  
{
	//-------------------------------------------------------------------------
	//Build the AMSS ProfileUpdates XML file.
	public void buildKpi(JobDataMap m)
	{
		//Query the list of Profile Updates from the AMSS database.
		buildListsOfProfileUpdates();
		
		//Create two XML files... summary and detail.
		buildSummaryAndDetailsFiles();
	}

	//-------------------------------------------------------------------------
	private void buildListsOfProfileUpdates()
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection("A");

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "AMSS-Profile-Updates.sql");  //TODO: get filename from a property (job.xml)

            pstmt = conn.prepareStatement(strQuery);           
            rs = pstmt.executeQuery();
           
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(5);

            	String initiatorName = rs.getString(1);
            	String requestStatus = rs.getString(2);
            	String requestContent = rs.getString(3);
            	String customerId = "";

            	//requestContent is XML. Parse it.
            	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            	DocumentBuilder db = dbf.newDocumentBuilder();
            	Document doc = db.parse(new InputSource(new StringReader(requestContent)));

            	doc.getDocumentElement().normalize();
            	NodeList nodeLst = doc.getElementsByTagName("AMRequestDO");

            	if(nodeLst.getLength() > 0)
            	{
            		Node node = nodeLst.item(0); //should be only 1 node.
            	    
            	    if (node.getNodeType() == Node.ELEMENT_NODE)
            	    {
            	    	try
            	    	{
            	    		Element fstElmnt = (Element)node;
            	    		NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("customerId");
            	    		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
            	    		NodeList fstNm = fstNmElmnt.getChildNodes();
            	    		customerId = ((Node) fstNm.item(0)).getNodeValue();
            	    	}
            	    	catch(Exception exp)
            	    	{
            	    		customerId = "n/a";
            	    	}
            	    }
            	}

            	String area = AttUtils.getRegionByCustomerId(customerId);
            	
            	map.put(KEY_USER_ID, initiatorName);
            	map.put("requestStatus", requestStatus);           	
            	map.put(KEY_AREA, area);           	
            	
            	if (customerId.charAt(0) == '-')
            	{
            		customerId = "Anonymous";
            	}
            	map.put(KEY_CUSTOMER_ID, customerId);

            	switch(AttUtils.regionList.valueOf(area.toLowerCase()))
            	{
            		case southwest: kpiMap_s.put(initiatorName, map); break;
	            	case midwest: kpiMap_m.put(initiatorName, map); break;
	            	case west: kpiMap_w.put(initiatorName, map); break;
	            	case southeast: kpiMap_b.put(initiatorName, map); break;
	            	case other: kpiMap_o.put(initiatorName, map); break;
	            	default: break;
            	}
            }
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("AMSS Profile Updates: Exception querying AMSS info, message: " + e.getMessage());
		}		
	}

	//-------------------------------------------------------------------------
	protected void buildSummaryAndDetailsFiles()
	{
		//Loop over all users (per region) and create count by area and state
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
		TreeMap<String, HashMap<String, String>> profileUpdates = null;
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
					case 's': profileUpdates = kpiMap_s; break;
					case 'm': profileUpdates = kpiMap_m; break;
					case 'w': profileUpdates = kpiMap_w; break;
					case 'b': profileUpdates = kpiMap_b; break;
					case 'o': profileUpdates = kpiMap_o; break;
				}

				String regionName = AttUtils.getRegionName(region);
				
				//Loop over profileUpdates and sum the count.
				int count = 0;

				Collection<HashMap<String, String>> c = profileUpdates.values();        
				Iterator<HashMap<String, String>> itr = c.iterator();
				
				while(itr.hasNext())
				{				
					HashMap<String, String> map = itr.next();
				
					// (1) Calculate the Summary Info
					//
					//the results exist, increment count
					count++;

					// (2) Build the Details XML file.
					//				
					writer.writeEntity("result");
					writer.writeAttribute("region", map.get(KEY_AREA));
					writer.writeAttribute("userId", map.get(KEY_USER_ID));
					writer.writeAttribute("customerId", map.get(KEY_CUSTOMER_ID));
					writer.writeAttribute("requestStatus", map.get("requestStatus"));
					writer.endEntity();
				}
				
				//add an empty map for each region.			
				HashMap<String, String> tmpMap = new HashMap<String, String>(3);

				tmpMap.put(KEY_AREA, regionName);
				tmpMap.put(KEY_COUNT, Integer.toString(count));

				//add it to the results
				results.put(regionName, tmpMap);
			}

			writer.endEntity();
			writer.close();
			fw.close();	
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("Error Processing Profile Updates, exception: " + e.getMessage());
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
	        	
				writer.writeEntity("result");
				writer.writeAttribute("date", getDate());
				writer.writeAttribute("area", map.get(KEY_AREA));
				writer.writeAttribute("count", strCount);
				writer.endEntity();
			}
	
			writer.endEntity();
			writer.close();
			fw.close();
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("Exception creating Profile Updates XML file: " + outputFile + ", exception: " + e.getMessage());
		}
	}

}
