package com.amdocs.dashboard.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amdocs.dashboard.dao.GridFavoriteDAO;
import com.amdocs.dashboard.grid.vo.FavoriteVO;

public class GridService extends EAService {
	
	// ---- START select queries ---- //
	
	@SuppressWarnings("unused")
	private List<FavoriteVO> selectFavorites(String serviceKey, Map<String, Object> params) 
	throws SQLException
	{		
		List<FavoriteVO> results = new GridFavoriteDAO().selectFavorites(params);
		
		// store this result in QueryResultCache
		//QueryResultCache.storeResult(serviceKey, results);
		
		return results;
	}
	
	@SuppressWarnings("unused")
	private List<FavoriteVO> selectFavorite(String serviceKey, Map<String, Object> params) 
	throws SQLException
	{		
		FavoriteVO favorite = new GridFavoriteDAO().selectFavorite(params);
		
		List<FavoriteVO> results = new ArrayList<FavoriteVO>();
		results.add(favorite);
		
		// store this result in QueryResultCache
		//QueryResultCache.storeResult(serviceKey, results);
		
		return results;
	}
	
	// ---- END select queries ---- //
	
	
	
	
	// ---- START update queries ---- //	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
 	*/
	@SuppressWarnings("unused")
	private UpdateResult saveFavorite(Map<String, Object> params) throws Exception
	{
		UpdateResult result = new UpdateResult();
		
		FavoriteVO favorite = null;		
		if (!params.containsKey("favorite") || params.get("favorite") == null)
		{
			result.addMessage("Favorite not defined!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
		else if (!(params.get("favorite") instanceof FavoriteVO))
		{
			result.addMessage("Favorite class is wrong (" + params.get("favorite").getClass().getSimpleName() + ")!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
		else
			favorite = (FavoriteVO) params.get("favorite");
		
		
		int employeeId = 0;
		if (!params.containsKey("employeeId") || params.get("employeeId") == null)
		{
			result.addMessage("Employee ID not defined!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
		else if (!(params.get("employeeId") instanceof Integer))
		{
			result.addMessage("Employee ID class is wrong (" + params.get("employeeId").getClass().getSimpleName() + ")!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
		else
		{
			employeeId = (Integer) params.get("employeeId");
		}
		
		
		// don't try to validate anything if anything is wrong with proposal object
		if (result.getStatus() != UpdateResult.UNDEFINED)
			return result;
		
		
		// TODO perform validations before updating any tables?
		
		
		// don't try to save it if anything is wrong after validation
		if (result.getStatus() != UpdateResult.UNDEFINED)
			return result;
		
		
		try {
			// Save favorite
			new GridFavoriteDAO().saveFavorite(favorite, employeeId);
			result.setStatus(UpdateResult.SUCCESS);
			
			// Select saved favorite from DB and return it in the UpdateResult
			Map<String,Object> params1 = new HashMap<String,Object>();
			params1.put("favoriteId", favorite.getFavoriteId());
			
			favorite = new GridFavoriteDAO().selectFavorite(params1);
		} catch (SQLException e) {
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			
			throw e;
		} finally {
			result.setReturnObj(favorite);			
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
 	*/
	@SuppressWarnings("unused")
	private UpdateResult deleteFavorite(Map<String, Object> params) throws Exception
	{
		UpdateResult result = new UpdateResult();
		
		Long favoriteId = 0L;		
		if (!params.containsKey("favoriteId") || params.get("favoriteId") == null)
		{
			System.out.println("Favorite ID not defined!  Contact support for assistance.");
			result.addMessage("Favorite ID not defined!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
		else if (params.get("favoriteId") instanceof Integer)
			favoriteId = ((Integer) params.get("favoriteId")).longValue();
		else if (params.get("favoriteId") instanceof Double)
			favoriteId = ((Double) params.get("favoriteId")).longValue();
		else if (params.get("favoriteId") instanceof Long)
			favoriteId = (Long) params.get("favoriteId");
		else 
		{
			System.out.println("Favorite ID class is wrong (" + params.get("favoriteId").getClass().getSimpleName() + ")!  Contact support for assistance.");
			result.addMessage("Favorite ID class is wrong (" + params.get("favoriteId").getClass().getSimpleName() + ")!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
			
		
		

		int employeeId = 0;
		if (!params.containsKey("employeeId") || params.get("employeeId") == null)
		{
			System.out.println("Employee ID not defined!  Contact support for assistance.");
			result.addMessage("Employee ID not defined!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
		else if (!(params.get("employeeId") instanceof Integer))
		{
			System.out.println("Employee ID class is wrong (" + params.get("employeeId").getClass().getSimpleName() + ")!  Contact support for assistance.");
			result.addMessage("Employee ID class is wrong (" + params.get("employeeId").getClass().getSimpleName() + ")!  Contact support for assistance.");
			result.setStatus(UpdateResult.ERROR);
		}
		else
		{
			employeeId = (Integer) params.get("employeeId");
		}
		
		// don't delete anything if anything is wrong with params
		if (result.getStatus() != UpdateResult.UNDEFINED)
			return result;		
		
		try {
			int deleted = new GridFavoriteDAO().deleteFavorite(favoriteId, employeeId);
			
			if (deleted == 0)
				result.setStatus(UpdateResult.WARNING);
			else
				result.setStatus(UpdateResult.SUCCESS);
		} catch (SQLException e) {
			result.addMessage(e.getMessage());
			result.setStatus(UpdateResult.ERROR);
			throw e;
		}
		
		return result;
	}
}
