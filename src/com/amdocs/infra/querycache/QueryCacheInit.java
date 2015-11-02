package com.amdocs.infra.querycache;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;


public class QueryCacheInit
        extends HttpServlet
{
    /**
	 * Generated version UID
	 */
	private static final long serialVersionUID = 183994971798454517L;
	
	/**
     *  Called once at startup
     */
    public void init(ServletConfig config)
        throws ServletException,UnavailableException
    {
    	// PARAMETERS - xmlFilesStr is REQUIRED, and poolHelperName is OPTIONAL
    	String xmlFilesStr=config.getInitParameter("QueryXmlFiles");
    	String poolHelperName=config.getInitParameter("ConnectionPoolHelper");
    	// END PARAMETERS

    	if(xmlFilesStr==null)
    		throw new UnavailableException("FATAL: QueryCache Mechanism requires QueryXmlFiles parameter to star properly");
    	// Separate single parameter into multiple files provided
    	try {
        	ArrayList<String> xmlFiles = new ArrayList<String>();
        	StringTokenizer xmlFilesTokenizer = new StringTokenizer(xmlFilesStr,";,: \t\n\r\f");
        	while(xmlFilesTokenizer.hasMoreTokens())
        	{
//        		String foundFile = xmlFilesTokenizer.nextToken();
//        		String foundRealFile = config.getServletContext().getRealPath(foundFile);
//        		if(foundRealFile == null)
//        			// File not found!!!
//        			throw new UnavailableException("FATAL: QueryCache Mechanism Can't Find XML to load: "+foundFile);
        		xmlFiles.add(xmlFilesTokenizer.nextToken());
        	}
	    	QueryCacheServer.init(config, xmlFiles, poolHelperName, Logger.getLogger(config.getServletName()) );
		} catch (Throwable e) {
    		System.out.println("FATAL: QueryCache Mechanism FAILED to initialize, due to exception below:");
    		e.printStackTrace();
    		throw new UnavailableException("FATAL: QueryCache Mechanism Failed to Initialize due to "+e.getMessage());
		}
    }
    
}