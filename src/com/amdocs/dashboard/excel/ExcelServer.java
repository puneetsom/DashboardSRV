package com.amdocs.dashboard.excel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExcelServer extends HttpServlet {
	private static final long serialVersionUID = 292664775165252874L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		
		File file = new File(request.getParameter("xlsFile"));
		String filename = request.getParameter("filename");
		
		if (filename == null || filename.length() == 0)
			filename = file.getName();
		
		if (!file.exists())
		{
		    response.setContentType("text/html");
		    PrintWriter out = response.getWriter();
		    out.println("<html>");
		    out.println("<title>File does not exists!</title>");
		    out.println("<body>");
		    out.println("<h3>File does not exist!</h3>");
		    out.println("<p>Unable to find " + request.getParameter("xlsFile") + " on server.</p>");
		    out.println("<p>Context support for assistance.</p>");
		    out.println("</body>");
		    out.println("</html>");
		    out.close();
		    return;
		}
		
		response.setContentType("application/octet-stream");
	    response.addHeader("Content-Length", String.valueOf(file.length()));
	    response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

	    BufferedInputStream input = null;
	    BufferedOutputStream output = null;

	    //System.out.println(timeSince(startTime) + " - streaming" + file.getAbsolutePath());
	    // output excel file
	    try {
	        input = new BufferedInputStream(new FileInputStream(file));
	        output = new BufferedOutputStream(response.getOutputStream());

	        byte[] buffer = new byte[8192];
	        for (int length = 0; (length = input.read(buffer)) > 0;) {
	            output.write(buffer, 0, length);
	        }
	    } finally {
	        if (output != null) try { output.close(); } catch (IOException ignore) { }
	        if (input != null) try { input.close(); } catch (IOException ignore) {}
	    }
	    //System.out.println(timeSince(startTime) + " - file streamed");
	    
	    // delete file if it is in the tmp directory and it has been served
	    if (System.getProperty("java.io.tmpdir") != null && file.getAbsolutePath().startsWith(System.getProperty("java.io.tmpdir")))
	    {
	    	file.delete();
	    	//System.out.println(timeSince(startTime) + " - deleted " + file.getAbsolutePath());
	    }
	}

	private double timeSince(long startTime) {
		return ((double) (System.currentTimeMillis() - startTime))/1000;
	}
}
