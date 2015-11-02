/**
 * Created in Sep, 2009
 * 
 * Amdocs - Copyright (C) 2009
 * 
 * The purpose of this class is to load XML file locations from the XML file. 
 *
 * The XML files must have the structure like the follwing where attrN can be
 * any name:
 * 
 *   <results>
 *     <result attr1="something" attr2="somethingelse" .../>
 *     ...
 *   </results>
 * 
 * 
 * Author: Matt Hill
 * Supervisor: Buddha
 * 
 */
package com.amdocs.infra.utils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

import com.amdocs.dashboard.utils.DashboardUtils;
import com.amdocs.infra.datatypes.MagicType;
import com.amdocs.infra.datatypes.MagicTypeDescriptor;
import com.amdocs.infra.xmlloader.XmlLoaderServer;

public class XMLDescriptor extends DefaultHandler implements Cloneable, Comparable<XMLDescriptor>
{
	// Code for this xml file, really important
	String code;

	// Object Alias to map to the Client object name which should store this data. Can be empty for anonymous
	String clientObject;

	// Location of XML file
	String location;
	
	// xml type (Unused??) 
	String type = "";
	
	// For debugging, you can activate "trace" on a query, it will print out each request to it!
	boolean debug = true;

	// Array of parameter names to match the question marks in the queryString
	ArrayList<String> paramNames = new ArrayList<String>(); 

	// Results list of MagicType objects
	List<MagicType> list;

	// Descriptor for the current MagicType
	MagicTypeDescriptor resultsDesc;
	
	// Client (ActionScript) class name
	String destClassName = "";

	Boolean firstTime = true;
	
	// Context of XML file
	private String context;
	
	// Path to context directory (i.e. webapps/DashboardSRV)
	private String contextPath;

	// Path to webapps directory
	private String webappsPath;
	
	private Map<String, Object> map;
	
	public XMLDescriptor()
	{
	}

	public XMLDescriptor(String iCode, String iClientObject, String iLocation, String iType, String iContext)
	{
		code=iCode;
		clientObject=iClientObject;
		location=iLocation;
		type=iType;
		context=iContext;
	}
	
	// Method called by XML Parser to load attribute by attribute -- rather ugly, I know, but no choice 
	// because parser itself is case sensitive and I wanted the attributes not to be case sensitive
	public void setAttribute(String attrName, String attrValue) throws SAXNotRecognizedException
	{
		if(attrName.equalsIgnoreCase("code"))
			setCode(attrValue);
		else if(attrName.equalsIgnoreCase("clientObject"))
			setClientObject(attrValue);
		else if(attrName.equalsIgnoreCase("location"))
			setLocation(attrValue);
		else if(attrName.equalsIgnoreCase("type"))
		{
			if(!("Default".equalsIgnoreCase(attrValue)))
				setType(attrValue);
		}
		else if(attrName.equalsIgnoreCase("debug"))
			setDebug(attrValue);
		else if(attrName.equalsIgnoreCase("context"))
			setContext(attrValue);
		else if(attrName.equalsIgnoreCase("description"))
			return;		// Ignored!
		else
			throw new SAXNotRecognizedException("FATAL: Unknown attribute "+attrName+" specified for query "+code+"!");		
	}

	public void setDebug(String attrValue)
	{
		if(attrValue==null || attrValue.length()==0)
			return;
		attrValue = attrValue.toLowerCase();
		if(attrValue.charAt(0)=='y' || attrValue.equals("true"))
			debug=true;
	}

	/**
	 * Call this method after parsing the SQL Descriptor, to confirm that all mandatory properties
	 * are set correctly, the Type is recognized and that integer things are integer
	 * 
	 * @throws SAXNotRecognizedException
	 */
	public void validate()
		throws SAXNotRecognizedException
	{
		// Check missing things first
		if(location == null || location.length() == 0)
			throw new SAXNotRecognizedException("FATAL: Invalid XML Definition Detected - Location cannot be empty for XML "+code+"!");
		if(code == null || code.length() == 0)
			throw new SAXNotRecognizedException("FATAL: Invalid XML Definition Detected - Code Attribute is Missing or Empty for XML "+code+"!");
	
		// Now to trickier rules
		if("Default".equalsIgnoreCase(type))
			throw new SAXNotRecognizedException("FATAL: Invalid XML Definition Detected - Type Attribute cannot be 'Default' for "+code+"!");

		processXMLParams();
	}

	public List<MagicType> loadXmlFile(HashMap<String,Object> params) throws SQLException
	{	
		if(params!=null && params.size()>0)	// Don't crash for parameterless queries
			params = fixParamMap(params);	// Fix the hashmap for "sure-fire" lookup mode

		StringBuffer locationFinal = new StringBuffer(getLocation());
		
		// Now prepare the parameters!
		for(String paramName : paramNames)
		{
			Object paramValue = params.get(paramName.toLowerCase());
			if(paramValue==null)
			{
				// Handling SPECIAL param (run_date)
				if(paramName.equalsIgnoreCase("run_date"))
				{
					paramValue = "TO_DATE('"+(new Date(System.currentTimeMillis())).toString()+"','yyyy-mm-dd')";
				} else
				{
					System.out.println("WARNING (XmlLoad DEBUG MODE): MISSING PARAMETER "+paramName+" FOR QUERY "+getCode());
					return null;		// FAILURE
				}
			}
			
			int findQ = locationFinal.indexOf("?");
			locationFinal.replace(findQ, findQ+1, paramValue.toString());
		}

		if(isDebug())
		{
			System.out.println("--------------[ DEBUG Load XML File: "+getCode()+" ]---------------------------");
			System.out.println(locationFinal);
			System.out.println("------------[ END Load XML File: "+getCode()+" ]---------------------------");
		}
		
		firstTime = true;

		//reset the results list.
		list = new ArrayList<MagicType>();

		//get the client class name
		destClassName = (getClientObject()==null || getClientObject().length()==0) ? getCode() : getClientObject();
		
		long loadProcessTime = 0;

		try
		{
			long startTime = System.currentTimeMillis();

			XmlLoaderServer.getInstance().logger.info("XmlLoaderServer: Loading file: " + locationFinal.toString());

			// Obtaining general parser instance
			SAXParser saxParser = (SAXParserFactory.newInstance()).newSAXParser();		
			saxParser.parse(locationFinal.toString(), this);

			XmlLoaderServer.getInstance().logger.info("XmlLoaderServer: File: " + locationFinal.toString() + " loaded.");

			loadProcessTime = System.currentTimeMillis() - startTime;		// For instrumentation and debug

		} 
		catch(Throwable e) 
		{
			XmlLoaderServer.getInstance().logger.log(Level.SEVERE, "XML: " +getCode()+ " FAILED TO Load, Exception: ", e);
		} 
		finally 
		{
			if(isDebug() && loadProcessTime > 0)
				System.out.println("-----------[ XML: "+getCode()+" loaded in "+loadProcessTime+" Milliseconds ]---------------------------");
		}
		return list;
	}
	
	
	public Map<String, Object> loadXmlFile() throws Exception
	{
		//reset the results list.
		map = new HashMap<String, Object>();

		
		long loadProcessTime = 0;

		
		File file = null;
		try
		{
			long startTime = System.currentTimeMillis();
			
			// If location of file is /data/xml/myfile.xml then look in webapps/data/myfile.xml
			if (getLocation().startsWith("\\") || getLocation().startsWith("/") || getLocation().startsWith("http://"))
				file = new File(webappsPath + getLocation());
			// Otherwise if file is data/xml/myfile.xml then look in webapps/[context]/data/xml/myfile.xml
			else
				file = new File(contextPath + "\\" + getLocation());
			
			JAXBContext jaxbContext = JAXBContext.newInstance(getContext());
			
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
			JAXBElement<?> obj = (JAXBElement<?>) jaxbUnmarshaller.unmarshal(new FileInputStream(file));				
			map = DashboardUtils.objectToMap(obj.getValue());
			//System.out.println(map);
			//DashboardUtils.printMap(map);
			
			loadProcessTime = System.currentTimeMillis() - startTime;		// For instrumentation and debug
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}catch(Throwable e)	{
			XmlLoaderServer.getInstance().logger.log(Level.SEVERE, "XML: " +getCode()+ " FAILED TO Load, Exception: ", e);
		} 
		finally 
		{
			if(isDebug() && loadProcessTime > 0)
				System.out.println("-----------[ XML: "+getCode()+" loaded in "+loadProcessTime+" Milliseconds ]---------------------------");
		}
		return map;
	}

	//-------------------------------------------------------------------------
	// Callback for SAX parser
	public void startElement(String namespace, String localName, String qName, Attributes atts) 
		throws SAXException
	{
		if(qName.equalsIgnoreCase("result"))
		{
			try
			{
				if(firstTime)
				{
					//Load "Magic Type Descriptor one time ahead of everything -- makes everything faster!
					resultsDesc = MagicTypeDescriptor.loadMetaDataForMagicType(getCode(), destClassName, atts);
					firstTime = false;
				}
	
				list.add(new MagicType(atts, resultsDesc));
			}
			catch(Exception e)
			{
				XmlLoaderServer.getInstance().logger.log(Level.SEVERE, "XML parsing error in startElement, Exception: ", e);
			}
		}
	}

	//-------------------------------------------------------------------------
	// Callback for SAX parser
	public void endElement(String namespace, String localname, String qname) 
		throws SAXException
	{
		//TODO: do something??
		
	}

	// Simple internal method to create map where all parameter names are lower-case (for easy matching)
	private HashMap<String,Object> fixParamMap(HashMap<String,Object> params)
	{
		HashMap<String,Object> fixed = new HashMap<String,Object>();
		Iterator<Map.Entry<String, Object>> i = params.entrySet().iterator();
		while(i.hasNext())
		{
			Map.Entry<String,Object> param = i.next();
			fixed.put(param.getKey().toLowerCase(), param.getValue()); 
		}
		return fixed;
	}
	
	// ------- DEFAULT -------- Setters and Getters
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code.trim();
	}

	public String getClientObject() {
		return clientObject;
	}

	public void setClientObject(String clientObject) {
		this.clientObject = clientObject.trim();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String iLocation) {
		this.location = iLocation.trim();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type.trim();
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context.trim();
	}
	
	public String getWebappsPath() {
		return webappsPath;
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String iPath) 
	{
		int webappsIndex = iPath.trim().indexOf("webapps");
		this.webappsPath = iPath.trim().substring(0, webappsIndex + 7);
		
		this.contextPath = iPath.trim();
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(code);
		str.append(" - ");
		str.append(type);
		str.append(" - Client Object [");
		str.append(clientObject);
		str.append("]\n");
		str.append(location);
		str.append('\n');
		str.append("SQL PARAMETER NAMES: ");
		Iterator<String> i=paramNames.iterator();
		while(i.hasNext())
			str.append(i.next()+((i.hasNext()) ? "," : ""));
		return str.toString();
	}

	public int compareTo(XMLDescriptor other) {
		return code.compareTo(other.code);
	}
	
	// Used to generate synchronization string to send to Client to ensure things are in-sync
	public String toSynchStr() {
		StringBuffer ret = new StringBuffer(30);
		ret.append(code);
		ret.append('~');
		ret.append(location.hashCode());
		return ret.toString();
	}

	private void processXMLParams() throws SAXNotRecognizedException 
	{
		// Parse query and prepare matching parameters array
		StringBuffer preparedLocation = new StringBuffer(location);

		int fromIndex = 1;
		int paramFound = -1;
		do
		{
			paramFound = preparedLocation.indexOf("{", fromIndex);
			if(paramFound>-1)
			{
				int endOfParam = preparedLocation.indexOf("}", paramFound);
				if(endOfParam==-1)
					throw new SAXNotRecognizedException("Invalid Parameters detected for XML "+getCode()+" - Unterminated { curly braces");
				String foundParam;
				try
				{
					foundParam = preparedLocation.substring(paramFound+1, endOfParam);

					if(foundParam==null)
						throw new Exception();
					
					foundParam = foundParam.trim();
					
					if(foundParam.length()==0)
						throw new Exception();
				}
				catch(Throwable e)
				{
					throw new SAXNotRecognizedException("Invalid Query Parameters detected for query "+getCode()+" Check for empty parameters in curly bracers");
				}

				// No space within parameter name allowed!
				if(foundParam.indexOf(' ')>-1)
					throw new SAXNotRecognizedException("Invalid Query Parameter detected for query "+getCode()+" No SPACE in parameter name allowed: "+foundParam);

				// Ok, final step, replacing with Question Mark and Saving param name for later use
				preparedLocation.replace(paramFound, endOfParam+1, "?");
				this.paramNames.add(foundParam);
			}
		} while(paramFound>-1 && fromIndex < preparedLocation.length());

		// Final step, put the prepared XML location back into "this"
		location = preparedLocation.toString();
	}

	public boolean isDebug() {
		return debug;
	}
	
}
