/**
 * Created on Dec 24, 2008
 * 
 * Amdocs - Copyright (C) 2008
 * 
 * The purpose of this class is to load Queries from the XML file. 
 * The Queries can also be actual EJB calls to existing ACRM and other Beans
 * 
 * Author: Yan Spevak
 * Supervisor: Adi Rabinovich
 * 
 */
package com.amdocs.infra.utils;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

public class SQLS_Loader extends DefaultHandler
{
	// Internal flag to track that <sql> tag was present
	private boolean is_sql = false;
	// Pointer to the external Model which we are loading
	private Map<String,SQLDescriptor> sqls;
	// Internal statement object - points to single statement that is being read right now
	private SQLDescriptor statement;
	// Speed things up! 
	private StringBuffer queryText;
	// Default type as specified inside main <sql> tag
	private String defaultType;

	// Constructor -- NOT Public on purpose
	public SQLS_Loader(Map<String,SQLDescriptor> sqls) {
		this.sqls = sqls;
	}

    public void startElement( String namespace, String localname, String qname, Attributes atts ) 
      throws SAXException
    {
    	// Validate sanity first of all
    	if(!is_sql)
    	{
    		// Wait for SQLS!
    		if(qname.equalsIgnoreCase("sql"))
    		{
    			if(atts==null || atts.getLength()!=1 || !("type".equalsIgnoreCase(atts.getQName(0)) ) )
    				throw new SAXNotRecognizedException("FATAL: Expected type (and only type) attribute within main <sql> tag. Check DTD, this attribute is required!");
    			defaultType = atts.getValue(0);
    			is_sql = true;
    			return;	// Life is good!
    		}
    		else
    			throw new SAXNotRecognizedException("FATAL: Expected <sql> tag. All <statement> tags must be enclosed in <sql> tag");
    	}
    	
    	// Ok, we are past <sql> tag now, lets see what we got
    	if(!qname.equalsIgnoreCase("statement"))
    		throw new SAXNotRecognizedException("FATAL: Expected <statement> tag within <sql> tag. Only Statements are supported");

    	// Prepare statement and confirm that all our Attributes are here!
    	statement = new SQLDescriptor();
    	statement.setType(defaultType);			// Always reset to default global type, upon creation (may be overwritten later)
    	queryText = new StringBuffer(100);		// Who ever seen query shorter than 100 chars?
    	// Because the silly SAX Parser has Case Sensitive Attributes, we are going to have to do things the hard way
    	for(int i=0;i<atts.getLength();i++)
    		statement.setAttribute(atts.getQName(i),atts.getValue(i));
    }
    
   public void endElement( String namespace, String localname, String qname) 
      throws SAXException
   {
	   if(statement!=null)
	   {
		   // Step 1 -- Complete the most important property of the query
		   statement.setQueryString(queryText.toString());
		   // Step 2 -- Validate everything (to confirm all is correct)
		   statement.validate();
		   // Step 3 -- Verify that there are NO DUPLICATES (by code, of course)
		   if( sqls.containsKey(statement.getCode().toLowerCase()) )
				   throw new SAXNotRecognizedException("FATAL: There are Duplicate Queries for code ["+statement.getCode()+"]");
		   sqls.put(statement.getCode().toLowerCase(), statement);
		   statement=null;	// This one processed, so it is not needed now
	   }
   }
   
   public void characters(char[] ch, int start, int len ) {
	   if(queryText!=null)
		   queryText.append(ch,start,len);
   }

   // Due to support for multiple files now, resetting in-between each XML
   public void reset() {
	is_sql = false;
	defaultType = null;
	queryText = null;
	statement = null;
	// Map is not being reset, as we keep loading into the same map!
   }
	
}
  