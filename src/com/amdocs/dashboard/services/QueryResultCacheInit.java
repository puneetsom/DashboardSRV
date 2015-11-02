package com.amdocs.dashboard.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QueryResultCacheInit extends HttpServlet 
{
	private static final long serialVersionUID = -7924647092704537429L;

	/**
     *  Called once at startup
     */
    public void init(ServletConfig config) throws ServletException, UnavailableException
    {
    	super.init(config);
    	QueryResultCache.init();
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (request.getParameter("reinitialize") != null)
		{
	    	QueryResultCache.init();
	    	response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	        out.println("<html><title>QueryResultCacheInit</title><body><h2>QueryResultCache reinitialized</h2></body></html>");
	        out.close();
		}
		// TODO show cached data?
	}
}