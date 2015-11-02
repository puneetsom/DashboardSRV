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
import com.amdocs.dashboard.utils.DashboardDBConfig;
import com.amdocs.infra.utils.Config;
import com.amdocs.infra.utils.DBConfig;
import com.amdocs.infra.utils.XMLWriter;

public class AmssImpersonations extends StatefulJobBase  
{
	//-------------------------------------------------------------------------
	//Build the AMSS Impersonations XML file.
	public void buildKpi(JobDataMap m)
	{
		//Query the list of Impersonations from the AMSS database.
		buildListsOfImpersonations();
		
		//Create two XML files... summary and detail.
		buildSummaryAndDetailsFiles();
	}

	//-------------------------------------------------------------------------
	private void buildListsOfImpersonations()
	{
        Connection conn = null;      
        ResultSet rs = null;
        PreparedStatement pstmt = null;

		try
		{		
			conn = DashboardDBConfig.getInstance().getConnection("A");

			//Read query from a file
	        String strPath = (String)Config.getInstance().getProperty("SQL_PATH");
	        String strQuery = DBConfig.readSQL(strPath + "\\" + "AMSS-Impersonations.sql");  //TODO: get filename from a property (job.xml)

            pstmt = conn.prepareStatement(strQuery);           
            rs = pstmt.executeQuery();
           
            while(rs.next())
            {
            	HashMap<String, String> map = new HashMap<String, String>(5);

            	String initiatorName = rs.getString(1);
            	String requestStatus = rs.getString(2);
            	String requestContent = rs.getString(3);
            	String operatorId = "";

            	//requestContent is XML. Parse it.
            	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            	DocumentBuilder db = dbf.newDocumentBuilder();
            	Document doc = db.parse(new InputSource(new StringReader(requestContent)));

            	doc.getDocumentElement().normalize();
            	NodeList nodeLst = doc.getElementsByTagName("RequestDO"); //TODO: get the name of this from the XML.

            	if(nodeLst.getLength() > 0)
            	{
            		Node node = nodeLst.item(0); //should be only 1 node.
            	    
            	    if (node.getNodeType() == Node.ELEMENT_NODE)
            	    {
            	    	Element fstElmnt = (Element)node;
            	    	NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("operatorId");
            	    	Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
            	    	NodeList fstNm = fstNmElmnt.getChildNodes();
            	    	operatorId = ((Node) fstNm.item(0)).getNodeValue();
            	    }
            	}
          	
            	map.put("name", initiatorName);
            	map.put("requestStatus", requestStatus);           	
            	map.put(KEY_USER_ID, operatorId);

            	kpiMap_o.put(initiatorName, map);
            }
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("AMSS Impersonations: Exception querying AMSS info, message: " + e.getMessage());
		}
	}

	//-------------------------------------------------------------------------
	protected void buildSummaryAndDetailsFiles()
	{
		//Loop over all users and create count by area and state
        HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>(50);
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

			//Loop over impersonations and sum the count.
			int count = 0;

			Collection<HashMap<String, String>> c = kpiMap_o.values();        
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
				writer.writeAttribute("name", map.get("name"));
				writer.writeAttribute("userId", map.get(KEY_USER_ID));
				writer.writeAttribute("requestStatus", map.get("requestStatus"));
				writer.endEntity();
			}
			
			//add an empty map for each region.			
			HashMap<String, String> tmpMap = new HashMap<String, String>(3);

			tmpMap.put(KEY_COUNT, Integer.toString(count));

			//add it to the results
			results.put("o", tmpMap);


			writer.endEntity();
			writer.close();
			fw.close();	
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("Error Processing Impersonations, exception: " + e.getMessage());
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
				writer.writeAttribute("count", strCount);
				writer.endEntity();
			}
	
			writer.endEntity();
			writer.close();
			fw.close();
		}
		catch(Exception e)
		{
			Logger.getLogger("com.amdocs.kpi").info("Exception creating Impersonations XML file: " + outputFile + ", exception: " + e.getMessage());
		}
	}

}
