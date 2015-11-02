package com.amdocs.infra.xmlloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.amdocs.infra.datatypes.MagicType;
import com.amdocs.infra.utils.XMLDescriptor;
import com.amdocs.infra.utils.XMLS_Loader;

public class XmlLoaderServer implements Serializable
{
	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = 4905411051568413597L;
	
	private static final String EMPTY_STR = new String("");

	// This is the main store for the queries to be executed
	HashMap<String, XMLDescriptor> xmls = new HashMap<String, XMLDescriptor>();

	// Singleton code
	private static XmlLoaderServer server;

	// Not so singleton (but still is, of course)
	public Logger logger;
	
	// Singleton code
	private static ServletContext context;

	//-------------------------------------------------------------------------
	public XmlLoaderServer(ServletConfig config, ArrayList<String> xmlXMLs, Logger i_logger)
		throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException, UnavailableException
	{
		this.logger = i_logger; 		// Global logger for the server

		context = config.getServletContext();

		// Obtaining general parser instance
		SAXParser saxParser = (SAXParserFactory.newInstance()).newSAXParser();
		XMLReader parser = saxParser.getXMLReader();

		// Need to load multiple files, put can use the same loader
		XMLS_Loader loader = new XMLS_Loader(xmls);
		parser.setContentHandler(loader);

		parser.setEntityResolver(new EntityResolver() 
		{
			public org.xml.sax.InputSource resolveEntity(String publicId,
					String systemId) throws org.xml.sax.SAXException,
					java.io.IOException {
				if(systemId.toLowerCase().endsWith("dtd"))		// DTD is ignored, others are loaded via "web server"
					return new InputSource(new java.io.StringReader(""));

				return null;	// Force default handling on other resources		
			}
		});

		for(String xmlFile:xmlXMLs) 
		{
			logger.info("XmlLoaderServer: Loading " + xmlFile);
			loader.reset();
			InputStream xmlFileStream = context.getResourceAsStream(xmlFile);
			parser.parse(new InputSource(xmlFileStream));
		}
		logger.info("XmlLoaderServer: All of the XMLs were successfully loaded! Server Active!");
		// All pre-loading done! Ready to serve requests now!

		// DEBUG ONLY -- START -- Print out what loaded!
		//printAllXmlFiles();
		// DEBUG ONLY --  END  -- 
	}

	/**
	 * Handy method that simply prints out all Queries that were loaded from XML
	 * (Just Codes, but no XML file locations)
	 */
	public void printAllXmlFiles()
	{
		System.out.println("---------------- LOADED FOLLOWING XML FILES ----- END -----");
		for(XMLDescriptor xml : xmls.values())
			System.out.println(xml.getLocation() + " : " + xml.getCode() + " : " + xml.getContext());
		
		System.out.println("----- END ------ LOADED FOLLOWING XML FILES ----- END -----");
	}

	public XMLDescriptor getLocationByCode(String iCode)
	{
		return xmls.get(iCode.toLowerCase());
	}

	/**
	 * Main method for Loading XML's by code.
	 * 
	 * @param xmlCode
	 * @param params
	 * @throws SQLException
	 */
	public Map<String, Object> loadXml(String xmlCode, HashMap<String,Object> params) throws Exception
	{
		//Locate xml file for given xmlCode
		XMLDescriptor xml = getLocationByCode(xmlCode);

		if (xml == null)
		{
			logger.warning("XmlLoaderServer: Ignored attempt to load UNDEFINED xml: " + xmlCode);
			return null;
		}
		
		xml.setContextPath(context.getRealPath(""));
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(xml.getContext() == null)
			resultMap.put("returnObj", xml.loadXmlFile(params));
		else
			resultMap = xml.loadXmlFile();
		
		return resultMap;
	}

	
	/**
	 * Main method for Loading a Date Range of XML's by code.
	 * 
	 * This assumes that the params HashMap includes a parameter named "dateRange" that is equal
	 *  to W (weekly), M (monthly), or Y (yearly).
	 * 
	 * @param xmlCode
	 * @param params
	 * @throws SQLException
	 */
	public List<MagicType> loadRangeOfXml(String xmlCode, HashMap<String,Object> params) throws SQLException
	{
		// Step 1 -- Locate xml file for given xmlCode
		XMLDescriptor xml = getLocationByCode(xmlCode);

		if (xml == null)
		{
			logger.warning("XmlLoaderServer: Ignored attempt to load UNDEFINED xml: " + xml);
			return null;
		}

		List<MagicType> results = new ArrayList<MagicType>(); 

		//calculate all the dates to load.
		String dateRange = params.get("dateRange").toString();
		HashMap<String,Object> dateParam = new HashMap<String, Object>();

		int count = 7;
		
		switch(dateRange.toLowerCase().toCharArray()[0])
		{
			case 'w': count =   7 + 1; break;   //+ 1, because we usually don't have data for today.
			case 'm': count =  30 + 1; break;
			case 'y': count = 365 + 1; break;
		}	

		Calendar theDate = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		for(int i=0; i < count; i++)
		{
		    String strDate = sdf.format(theDate.getTime());
			dateParam.put("date", strDate);
			results.addAll(xml.loadXmlFile(dateParam));
			theDate.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		return results;
	}

	
	//-------------------------------------------------------------------------
	// SINGLETON Access to main server, there needs to be only one actual instance...
	public static XmlLoaderServer getInstance()
	{
		if (server == null)
			System.out.println("FATAL !!!! You must initialize XmlLoaderServer during Web-Server startup through use of XmlLoaderInit servlet");

		return server;
	}

	/**
	 * @param xmlFile
	 * @throws IOException
	 * @throws FactoryConfigurationError
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws UnavailableException 
	 */
	public static synchronized void init(ServletConfig config, ArrayList<String> xmlFiles, Logger logger )
	throws ParserConfigurationException, SAXException,
	FactoryConfigurationError, IOException, UnavailableException
	{
		server = new XmlLoaderServer(config, xmlFiles, logger);
	}

}
