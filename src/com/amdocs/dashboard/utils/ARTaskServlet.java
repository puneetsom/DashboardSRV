package com.amdocs.dashboard.utils;

/**
 * 01/20/2011
 * @author Brian
 *
 * Servlet used to generate ARTask shortcut from Dashboard
 * 
 * Pass ENTRY_ID with "entryId" parameter 
 * Example:  /servlet/ARTaskServlet?entryId=INC000000123456
 * 
 * NOTE: Should pass ENTRY_ID and not INCIDENT_NUMBER!
 *
 * @TODO Allow "Name" and "Server" to be parameters as well?  Store in properties file?
 * 
 */

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ARTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1096254065815863261L;
	
    public ARTaskServlet() {
        super();
    }
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		
		response.sendRedirect("http://atts.yp.att.com/arsys/servlet/ViewFormServlet?form=HPD:Help Desk&server=flpv0006.ffdc.sbc.com&eid=" + request.getParameter("entryId"));
		/*response.setContentType("application/octet-stream; name=ARNotification.ARTask");
		response.addHeader("Content-ID", "ARNotification.ARTask");
		response.addHeader("Content-Disposition", "attachment; filename=ARNotification.ARTask");
	    PrintWriter out = response.getWriter();
	    
	    out.println("[Shortcut]");
	    out.println("Name = HPD:Help Desk");
	    out.println("Type = 0");
	    out.println("Server = flpv0006.ffdc.sbc.com");
	    out.println("Join = 0");
	    out.println("Ticket = " + request.getParameter("entryId"));
	    
	    out.close();*/
	}
}