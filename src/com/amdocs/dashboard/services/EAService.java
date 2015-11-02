package com.amdocs.dashboard.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Controller for all queries and update requests
 * 
 * @TODO tie in with Validators and DAOs
 * 
 * @author bs3932
 *
 */
public class EAService 
{
	private static Logger logger = Logger.getLogger(EAService.class);
	private String className;
	
	public EAService () {
		className = this.getClass().getSimpleName();
	}
	
	/**
	 * Generates serviceKey based on methodName and parameters
	 * 
	 * @param  methodName
	 * @param  params
	 * @return unique serviceKey
	 */
	protected String getServiceKey(String methodName, Map<String, Object> params)
	{
		StringBuffer mapKey = new StringBuffer();
		mapKey.append(className + "|" + methodName);
		if (params != null)
		{
			for (String key: params.keySet())
			{
				mapKey.append("|" + key + "=" + params.get(key));
			}
		}
		return mapKey.toString();
	}	
	
	/**
	 * Entry point for all query service requests
	 * 
	 * @param  methodName The method to be called within this class
	 * @param  params     Parameters to be used in query
	 * @return the query results in List
	 * @throws Throwable 
	 */
	@SuppressWarnings("unchecked")
	public List<?> activateQuery(String methodName, Map<String, Object> params, Map<String,Object> additionalParams) throws Throwable
	{
		long startTime = System.currentTimeMillis();
		String serviceKey = getServiceKey(methodName, params);
		System.out.println("EAService.activateQuery - serviceKey: " + serviceKey);
		List<?> cache = QueryResultCache.getResult(serviceKey);
		System.out.println("EAService.activateQuery - cache: " + cache);
		//if (cache != null)
		//	return cache;
		
		List<Object> result = null;
		try {
		    Class<?> c = this.getClass();
			Method m = c.getDeclaredMethod(methodName, new Class[] { String.class, Map.class });
			m.setAccessible(true);
			Object i = c.newInstance();
			result = (List<Object>) m.invoke(i, new Object[] { serviceKey, params });
		} catch (InvocationTargetException e) {
			logger.error(methodName + " " + params);
			e.getTargetException().printStackTrace();
			throw e.getTargetException();
		} catch (Exception e) {
			logger.error(methodName + " " + params); 
			e.printStackTrace();
			throw e;
		}
		
		int length = result.size();
		long endTime = System.currentTimeMillis();

		System.out.println ("-----------[ " + className + ": " + methodName + " loaded " + length + " record" + (length!=1?"s":"")+ " in " + (endTime - startTime) + " ms ]-----------");
		
		return result; 
	}
	
	/**
	 * Entry point for all update service requests
	 * 
	 * @param  methodName The method to be called within this class
	 * @param  params     Parameters to be used in query
	 * @return the update result object
	 * @throws Throwable 
	 */
	public UpdateResult activateUpdate(String methodName, Map<String, Object> params) throws Throwable
	{
		long startTime = System.currentTimeMillis();
		UpdateResult result = new UpdateResult();
		
		try {
		    Class<?> c = this.getClass();
			Method m = c.getDeclaredMethod(methodName, new Class[] { Map.class });
			m.setAccessible(true);
			Object i = c.newInstance();
			result = (UpdateResult) m.invoke(i, new Object[] { params });
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			logger.error(methodName + " " + params);
			throw e.getTargetException();
		} catch (Exception e) {
			logger.error(methodName + " " + params);
			e.printStackTrace();
			throw e;
		}

		long endTime = System.currentTimeMillis();
		System.out.println ("-----------[ " + className + ".activateUpdate: " + methodName + " affected " + result.getRowsAffected() + " record" + (result.getRowsAffected()!=1?"s":"")+ " in " + (endTime - startTime) + " ms ]-----------");
		
		return result; 
	}
	
	/**
	 * Entry point for all update service requests
	 * 
	 * @param  methodName The method to be called within this class
	 * @param  params     Parameters to be used in query
	 * @return the update result object
	 * @throws Throwable 
	 */
	public UpdateResult activateListUpdate(String methodName, List<Map<Object,Object>> params, Map<String,Object> additionalParams) throws Throwable
	{
		long startTime = System.currentTimeMillis();
		UpdateResult result = new UpdateResult();
		
		try {
		    Class<?> c = this.getClass();
			Method m = c.getDeclaredMethod(methodName, new Class[] { List.class, Map.class });
			m.setAccessible(true);
			Object i = c.newInstance();
			result = (UpdateResult) m.invoke(i, new Object[] { params, additionalParams });
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			logger.error(methodName + " " + params);
			throw e.getTargetException();
		} catch (Exception e) {
			logger.error(methodName + " " + params);
			e.printStackTrace();
			throw e;
		}

		long endTime = System.currentTimeMillis();
		System.out.println ("-----------[ " + className + ".activateListUpdate: " + methodName + " affected " + result.getRowsAffected() + " record" + (result.getRowsAffected()!=1?"s":"")+ " in " + (endTime - startTime) + " ms ]-----------");
		
		return result; 
	}
}
