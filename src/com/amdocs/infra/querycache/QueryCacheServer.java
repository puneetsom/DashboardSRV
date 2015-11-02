package com.amdocs.infra.querycache;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.amdocs.infra.datatypes.MagicType;
import com.amdocs.infra.utils.ConnectionPoolHelper;
import com.amdocs.infra.utils.SQLDescriptor;
import com.amdocs.infra.utils.SQLS_Loader;

public class QueryCacheServer implements Serializable
{
	/**
	 * Generated serialization ID
	 */
	private static final long serialVersionUID = 4905411051568413596L;

	// This is the main store for the queries to be executed
	HashMap<String, SQLDescriptor> sqls = new HashMap<String, SQLDescriptor>();
	
	// Singleton code
	private static QueryCacheServer server;
	
	// Not so singleton (but still is, of course)
	public Logger logger;
	private ConnectionPoolHelper poolHelper;

	public QueryCacheServer(ServletConfig config, ArrayList<String> sqlsXMLs, 
			                String poolHelperClassName, Logger i_logger)
			throws ParserConfigurationException, SAXException,
			FactoryConfigurationError, IOException, UnavailableException
	{
		this.logger = i_logger; 		// Global logger for the server
		
		ServletContext context = config.getServletContext();
				
		// First, try to setup poolHelper
		if(poolHelperClassName!=null)
			setupPoolHelper(poolHelperClassName,config);

		// Obtaining general parser instance
		SAXParser saxParser = (SAXParserFactory.newInstance()).newSAXParser();
		XMLReader parser = saxParser.getXMLReader();
		// Need to load multiple files, put can use the same loader
		SQLS_Loader loader = new SQLS_Loader(sqls);
		parser.setContentHandler(loader);
		parser.setEntityResolver(new EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId,
					String systemId) throws org.xml.sax.SAXException,
					java.io.IOException {
				if(systemId.toLowerCase().endsWith("dtd"))		// DTD is ignored, others are loaded via "web server"
					return new InputSource(new java.io.StringReader(""));
				
				return null;	// Force default handling on other resources		
			}
		});

		for(String xmlFile : sqlsXMLs) 
		{
			logger.info("QueryCacheServer: Loading " + xmlFile);
			loader.reset();
			InputStream xmlFileStream = context.getResourceAsStream(xmlFile);
			parser.parse(new InputSource(xmlFileStream));
		}
		logger.info("QueryCacheServer: All of the XMLs were successfully loaded! Server Active!");
		// All pre-loading done! Ready to serve requests now!
		// DEBUG ONLY -- START -- Print out what loaded!
		//printAllQueries();
		// DEBUG ONLY --  END  -- 
	}

	private void setupPoolHelper(String poolHelperClassName, ServletConfig config)
			throws UnavailableException, IOException {

		try {
			poolHelper = (ConnectionPoolHelper) (Class.forName(poolHelperClassName)).newInstance();
		} catch (ClassCastException e) {
			throw new UnavailableException(
					"FATAL: Failed to create "
							+ poolHelperClassName
							+ ", make sure it has default constructor and implements ConnectionPoolHelper");
		} catch (ClassNotFoundException e) {
			throw new UnavailableException(
					"FATAL: Provided Pool Helper Class Not Found - "
							+ poolHelperClassName);
		} catch (InstantiationException e) {
			throw new UnavailableException(
					"FATAL: Failed to create "
							+ poolHelperClassName
							+ ", make sure it has a public default constructor!");
		} catch (IllegalAccessException e) {
			throw new UnavailableException(
					"FATAL: Failed to create "
							+ poolHelperClassName
							+ ", make sure it has a PUBLIC default constructor!");
		}
		// Initialize it now
		if(poolHelper!=null)
			poolHelper.init(config,logger);
	}

	/**
	 * Handy method that simply prints out all Queries that were loaded from XML
	 * (Just Codes, but no SQLs)
	 */
	public void printAllQueries() {
		System.out.println("---------------- LOADED FOLLOWING QUERIES ----- END -----");
		for(SQLDescriptor sql : sqls.values())
			System.out.println(sql);
		System.out.println("----- END ------ LOADED FOLLOWING QUERIES ----- END -----");
	}

	public SQLDescriptor getQueryByCode(String iCode) {
		return sqls.get(iCode.toLowerCase());
	}

	/**
	 * Main method for activating SQLs inside the XML that was loaded.
	 * 
	 * @param queryCode
	 * @param params
	 * @throws SQLException
	 */
	public List<MagicType> activateQuery(String queryCode, HashMap<String,Object> params, HashMap<String,Object> additionalParams)
			throws SQLException 
	{
		// Step 1 -- Locate descriptor for given queryCode
		SQLDescriptor sql = getQueryByCode(queryCode);

		if (sql == null) 
		{
			logger.warning("QueryCacheServer: Ignored attempt to activate UNDEFINED query: " + queryCode);
			return null;
		}

		if (!sql.isBean())
			return sql.activateSQLQuery(params, additionalParams, poolHelper);
		else
			return null; // TODO: Other types unsupported for now
	}
	
	public List<Map<String,Object>> activateExcelQuery(String queryCode, HashMap<String,Object> params, HashMap<String,Object> additionalParams)
			throws SQLException 
	{
		// Step 1 -- Locate descriptor for given queryCode
		SQLDescriptor sql = getQueryByCode(queryCode);

		if (sql == null) 
		{
			logger.warning("QueryCacheServer: Ignored attempt to activate UNDEFINED query: " + queryCode);
			return null;
		}

		if (!sql.isBean())
			return sql.activateExcelSQLQuery(params, additionalParams, poolHelper);
		else
			return null; // TODO: Other types unsupported for now
	}
	
	/**
	 * This method will need further revision, to convert into service that all
	 * clients will call upon activation. Following things are on TODO: 1. Make
	 * the generated string of all SQLs "pre-generated" after every XML load,
	 * and cached in memory 2. Have input parameters of Length and Hash for
	 * pre-generated string, so that if client's length/hash matches our stored
	 * length/hash, response is sent empty (no changes) 3. Have special
	 * "CONTROL" code and parameters pre-pended. It will have hash (without
	 * specials) and expiration hours, assume that 0 means always re-check no
	 * startup. The length will be calculated on client/server separately
	 * 
	 * @return Generated string in style code~sql_hash~hours_to_cache~....
	 *         format, for client to put in SQLite
	 */
	public String synchSQLs()
	{
		// Because this string is crucial to staying in synch between client & server,
		// we want to play it safe and sort the collection, to insulate against natural
		// randomness of HashMap
		ArrayList<SQLDescriptor> sortedSqls = new ArrayList<SQLDescriptor>(sqls.values());
		Collections.sort(sortedSqls);

		StringBuffer bulkSyncLines = new StringBuffer();
		for(SQLDescriptor statement : sortedSqls)
		{
			bulkSyncLines.append(statement.toSynchStr());
			bulkSyncLines.append('~');
		}
		return bulkSyncLines.toString();
	}

	// SINGLETON Access to main server, there needs to be only one actual instance...
	public static QueryCacheServer getInstance()
	{
		if (server == null)
			System.out.println("FATAL !!!! You must initialize QueryCacheServer during Web-Server startup through use of QueryCacheInit servlet");

		return server;
	}

	/**
	 * @param xmlFile
	 * @throws IOException
	 * @throws FactoryConfigurationError
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws UnavailableException 
	 */
	public static synchronized void init(ServletConfig config, ArrayList<String> xmlFiles, 
										 String poolHelperClassName, Logger logger )
			throws ParserConfigurationException, SAXException,
			FactoryConfigurationError, IOException, UnavailableException
	{
		server = new QueryCacheServer(config, xmlFiles, poolHelperClassName, logger);
	}

}
