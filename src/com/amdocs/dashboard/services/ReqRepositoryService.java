package com.amdocs.dashboard.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amdocs.dashboard.dao.SaveDAO;
import com.amdocs.dashboard.dao.reqrep.RequirementDAO;
import com.amdocs.infra.datatypes.MagicType;
import com.amdocs.infra.querycache.QueryCacheServer;

public class ReqRepositoryService extends EAService {
	
	@SuppressWarnings({ "unused", "unchecked" })
	private UpdateResult saveRequirement(List<Map<Object,Object>> paramsList, Map<String,Object> additionalParams) throws Exception
	{
		UpdateResult result = new UpdateResult();	
					
		result = new RequirementDAO().save(paramsList, additionalParams, result);
		
		if (result.getStatus() == 0)
		{
			List<MagicType> reqObj = QueryCacheServer.getInstance().activateQuery("SELECT_REQ_EDIT", (HashMap<String, Object>)result.getReturnObj(), null);
			result.setReturnObj(reqObj);
		}
		return result;
	}
	
	@SuppressWarnings({ "unused", "unchecked"})
	private UpdateResult saveVersion(List<Map<Object,Object>> paramsList, Map<String,Object> additionalParams) throws Exception
	{
		UpdateResult result = new UpdateResult();	
		
		HashMap<String, Object> returnedSqlUpdate = new HashMap<String, Object>();
		for(Map<Object,Object> obj : paramsList)
		{								
			String queryCode = (String)obj.get("queryCode");				
			HashMap<String,Object> params = (HashMap<String,Object>)obj.get("params");
			if(params.containsKey("release_version"))
			{
				returnedSqlUpdate.put("release_version", params.get("release_version"));
				break;
			}
		}		
		result = new SaveDAO().saveData(paramsList, result);
		
		if (result.getStatus() == 0)
		{
			List<MagicType> reqObj = QueryCacheServer.getInstance().activateQuery("SELECT_VERSION_EDIT", returnedSqlUpdate, null);
			result.setReturnObj(reqObj);
		}
		
		return result; 
	}
	
	@SuppressWarnings({ "unused", "unchecked"})
	private UpdateResult saveEmployee(List<Map<Object,Object>> paramsList, Map<String,Object> additionalParams) throws Exception
	{
		UpdateResult result = new UpdateResult();	
		
		HashMap<String, Object> returnedSqlUpdate = new HashMap<String, Object>();
		for(Map<Object,Object> obj : paramsList)
		{								
			String queryCode = (String)obj.get("queryCode");				
			HashMap<String,Object> params = (HashMap<String,Object>)obj.get("params");
			if(params.containsKey("employee_id"))
			{
				returnedSqlUpdate.put("employee_id", params.get("employee_id"));
				break;
			}
		}		
		result = new SaveDAO().saveData(paramsList, result);
		
		if (result.getStatus() == 0)
		{
			List<MagicType> reqObj = QueryCacheServer.getInstance().activateQuery("SELECT_EMPLOYEE_EDIT", returnedSqlUpdate, null);
			result.setReturnObj(reqObj);
		}
		
		return result; 
	}
}
