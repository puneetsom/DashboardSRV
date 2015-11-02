package com.amdocs.dashboard.utils;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ExportToExcelServlet
 */
public class ExportToExcelServlet extends HttpServlet
{
	private static final long serialVersionUID = -613153792216740371L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException 
	{
		String reportData = request.getParameter("reportData");
		String reportName = request.getParameter("reportName");

		response.setContentType("application/ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + reportName);
		response.getOutputStream().print(reportData);
	}

}
