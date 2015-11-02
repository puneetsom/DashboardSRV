package com.amdocs.infra.xmlloader;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlLoaderDAO
{
	// Leaving signature as-is, not Java 5 compliant, since not sure if BlazeDS will be able to call it otherwise
	@SuppressWarnings("unchecked")
	public Map<String, Object> loadXml(String xmlCode, HashMap params) throws Exception
	{
		// TODO: Remove DEBUG Statement
		System.out.println("DEBUG: Load XML "+xmlCode+" activated, with ");
		return XmlLoaderServer.getInstance().loadXml(xmlCode, null);
	}


	@SuppressWarnings("unchecked")
	public List loadRangeOfXml(String xmlCode, HashMap params) throws SQLException
	{
		// TODO: Remove DEBUG Statement
		System.out.println("DEBUG: Load Range of XML "+xmlCode+" activated, with ");
		return XmlLoaderServer.getInstance().loadRangeOfXml(xmlCode, params);
	}
	
}
