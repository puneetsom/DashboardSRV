package com.amdocs.dashboard.services;

import java.util.List;
import java.util.Map;

import com.amdocs.dashboard.dao.SaveDAO;


public class SaveService extends EAService 
{
	
	@SuppressWarnings({ "unused"})
	private UpdateResult save(List<Map<Object,Object>> paramsList) throws Exception
	{
		return new SaveDAO().saveData(paramsList, new UpdateResult());		
	}	
}
