package com.amdocs.dashboard.services;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Some results from services are cached so it is not necessary
 * to go to the DB every time data is needed.  The data cached should 
 * only be those things which can be stored that do not change often such as
 * domains, offices, branches, etc.
 * 
 * Each result will expire at midnight by default.
 * 
 * @author bs3932
 *
 */
public class QueryResultCache implements Serializable 
{
	private static Logger logger = Logger.getLogger(QueryResultCache.class);
	private static final long serialVersionUID = 5013199092267923434L;
	
	private static QueryResultCache _instance;	
	private static Map<String, QueryResult> resultCache;
	
	public static QueryResultCache getInstance()
	{
		if (_instance == null)
			logger.error("FATAL !!!! You must initialize QueryResultCache during Web-Server startup through use of QueryResultCacheInit servlet");

		return _instance;
	}
	
	private QueryResultCache() 
	{
		resultCache = new HashMap<String, QueryResult>();
	}
	
	public static synchronized void init()
	{
		if (_instance == null)
		{
			_instance = new QueryResultCache();
			logger.info("QueryResultCache initialized");
		}
		else
		{
			resultCache = new HashMap<String, QueryResult>();
			logger.info("QueryResultCache re-initialized");
		}
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException(); 
	}
	
	public static List<?> getResult(String key)
	{		
		QueryResult cachedResult = resultCache.get(key);
		// if cachedResult is null or cachedResult is expired, return null
		if (cachedResult == null || cachedResult.isExpired())
			return null;

		//logger.debug(key + " result retrieved from cache (" + cachedResult.getResult().size() + ")");
		return cachedResult.getResult();
	}
	
	public static void storeResult(String key, List<?> result)
	{
		// Default expiration for all cached results to be midnight
		storeResult(key, result, QueryResult.Expiration.MIDNIGHT);
	}
		
	public static void storeResult(String key, List<?> result, QueryResult.Expiration expiration)
	{
		// remove result if it exists
		resultCache.remove(key);

		// create ServiceResult object with result and expiration date
		QueryResult cachedResult = new QueryResult(result, expiration);
		
		// store ServiceResult object in cachedResult Map
		resultCache.put(key, cachedResult);
		
		logger.debug(new Date() + " " + key + " result cached (" + result.size() + ")");
	}
	
	public static boolean hasResult(String key)
	{
		QueryResult cachedResult = resultCache.get(key);
		return (cachedResult != null && !cachedResult.isExpired());		
	}	
}
