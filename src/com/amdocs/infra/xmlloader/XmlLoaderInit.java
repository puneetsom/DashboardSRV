package com.amdocs.infra.xmlloader;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;


public class XmlLoaderInit extends HttpServlet
{
    /**
	 * Generated version UID
	 */
	private static final long serialVersionUID = 183994971798454518L;
	
	/**
     *  Called once at startup
     */
    public void init(ServletConfig config) throws ServletException,UnavailableException
    {
    	// PARAMETERS - xmlFilesStr is REQUIRED
    	String xmlFilesStr = config.getInitParameter("XmlLoaderXmlFiles");
    	// END PARAMETERS

    	if(xmlFilesStr == null)
    		throw new UnavailableException("FATAL: XmlLoader Mechanism requires XmlLoaderXmlFiles parameter to start properly");

    	// Separate single parameter into multiple files provided
    	try {
        	ArrayList<String> xmlFiles = new ArrayList<String>();
        	StringTokenizer xmlFilesTokenizer = new StringTokenizer(xmlFilesStr,";,: \t\n\r\f");

        	while(xmlFilesTokenizer.hasMoreTokens())
        	{
        		xmlFiles.add(xmlFilesTokenizer.nextToken());
        	}

        	XmlLoaderServer.init(config, xmlFiles, Logger.getLogger(config.getServletName()));
		}
    	catch (Throwable e) 
    	{
    		System.out.println("FATAL: XmlLoader Mechanism FAILED to initialize, due to exception below:");
    		e.printStackTrace();
    		throw new UnavailableException("FATAL: XmlLoader Mechanism Failed to Initialize due to "+e.getMessage());
		}
    }
    
}