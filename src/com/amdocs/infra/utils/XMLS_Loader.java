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

public class XMLS_Loader extends DefaultHandler
{
	// Internal flag to track that <xml-files> tag was present
	private boolean is_xml = false;

	// Pointer to the external Model which we are loading
	private Map<String,XMLDescriptor> xmls;

	// Internal xmlFile object - points to single xmlFile that is being read right now
	private XMLDescriptor xmlFile;

	// Default type as specified inside main <xml-files> tag
	private String defaultType;

	// Constructor -- NOT Public on purpose
	public XMLS_Loader(Map<String,XMLDescriptor> xmls)
	{
		this.xmls = xmls;
	}

	public void startElement(String namespace, String localname, String qname, Attributes atts) 
	throws SAXException
	{
		// Validate sanity first of all
		if(!is_xml)
		{
			// Wait for xmls!
			if(qname.equalsIgnoreCase("xml-files"))
			{
				if(atts==null || atts.getLength()!=1 || !("type".equalsIgnoreCase(atts.getQName(0)) ) )
					throw new SAXNotRecognizedException("FATAL: Expected type (and only type) attribute within main <xml-files> tag. Check DTD, this attribute is required!");

				defaultType = atts.getValue(0);
				is_xml = true;
				return;	// Life is good!
			}
			else
				throw new SAXNotRecognizedException("FATAL: Expected <xml-files> tag. All <xml-file> tags must be enclosed in <xml-files> tag.");
		}

		// Ok, we are past <xml-files> tag now, lets see what we got
		if(!qname.equalsIgnoreCase("xml-file"))
			throw new SAXNotRecognizedException("FATAL: Expected <xml-file> tag within <xml-files> tag.");

		// Prepare xmlFile and confirm that all our Attributes are here!
		xmlFile = new XMLDescriptor();
		xmlFile.setType(defaultType);			// Always reset to default global type, upon creation (may be overwritten later)

		// Because the silly SAX Parser has Case Sensitive Attributes, we are going to have to do things the hard way
		for(int i=0;i<atts.getLength();i++)
			xmlFile.setAttribute(atts.getQName(i), atts.getValue(i));
	}

	public void endElement(String namespace, String localname, String qname) 
	throws SAXException
	{
		if(xmlFile!=null)
		{
			//Validate everything (to confirm all is correct)
			xmlFile.validate();
			
			//Verify that there are NO DUPLICATES (by code, of course)
			if(xmls.containsKey(xmlFile.getCode().toLowerCase()))
				throw new SAXNotRecognizedException("FATAL: There are Duplicate XML definitions for code ["+xmlFile.getCode()+"]");

			xmls.put(xmlFile.getCode().toLowerCase(), xmlFile);

			xmlFile = null;	// This one processed, so it is not needed now
		}
	}

	// Due to support for multiple files now, resetting in-between each XML
	public void reset()
	{
		is_xml = false;
		defaultType = null;
		xmlFile = null;
		// Map is not being reset, as we keep loading into the same map!
	}

}
