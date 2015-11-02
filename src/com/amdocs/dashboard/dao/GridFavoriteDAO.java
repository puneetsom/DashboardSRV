package com.amdocs.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.NamingException;

import com.amdocs.dashboard.grid.vo.FavoriteColumnVO;
import com.amdocs.dashboard.grid.vo.FavoriteFilterColumnVO;
import com.amdocs.dashboard.grid.vo.FavoriteSortColumnVO;
import com.amdocs.dashboard.grid.vo.FavoriteVO;
import com.amdocs.infra.querycache.QueryCacheServer;
import com.amdocs.infra.utils.ConnectionManager;

/**
 * 
 * Handles selecting, saving, and deleting favorites from Executive Advisor grids.
 * 
 * @author brianse
 *
 */

public class GridFavoriteDAO
{
	private static Logger logger = Logger.getLogger("com.amdocs.dashboard.grid.dao");
	
	// TODO pass SQLDescriptor's "type" to get connection pool?
	private Connection getConn(String conn) throws SQLException, NamingException {
		return ConnectionManager.getConnection(conn);
	}
	
	private QueryCacheServer queryCache = QueryCacheServer.getInstance();
	
	private String getSql(String queryCode) {
		return queryCache.getQueryByCode(queryCode).getQueryString();
	}
	
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<FavoriteVO> selectFavorites(Map<String, Object> params) throws SQLException
	{
		List<FavoriteVO> favorites = new ArrayList<FavoriteVO>();

		Connection conn = null;      
		PreparedStatement pstmt = null;
		ResultSet rslt = null;
		String viewId = null;
		String gridId = null;
		int employeeId = 0;
		
		if (params != null && params.get("viewId") != null)
			viewId = (String) params.get("viewId");
		
		if (params != null && params.get("gridId") != null)
			gridId = (String) params.get("gridId");
		
		if (params != null && params.get("employeeId") != null)
			employeeId = (Integer) params.get("employeeId");
		
		if (viewId == null || gridId == null || employeeId == 0)				
			throw new SQLException("Unable to select favorites for " + viewId + ", " + gridId + ", " + employeeId);
		
		try
		{
			conn = getConn("CdwSQL");

			int col = 1;
			
			pstmt = conn.prepareStatement(getSql("GRID_selectFavorites"));

			pstmt.setString(col++, viewId);
			pstmt.setString(col++, gridId);
			pstmt.setInt(col++, employeeId);
			
			rslt = pstmt.executeQuery();
			FavoriteVO favorite = null;
			while (rslt.next())
			{
				Long favoriteId = rslt.getLong("FAVORITE_ID");
				if (favorite == null || favorite.getFavoriteId().longValue() != favoriteId.longValue())
				{
					if (favorite != null)
						favorites.add(favorite);
					
					favorite = new FavoriteVO(rslt);
				}
				
				String columnId = rslt.getString("COLUMN_ID");
				String columnType = rslt.getString("COLUMN_TYPE");				
				if (columnType != null)
				{
					FavoriteColumnVO column = new FavoriteColumnVO();
					column.setColumnId(columnId);
					column.setColumnOrder(rslt.getInt("COLUMN_ORDER"));
					
					if (columnType.equals("G"))
						favorite.addGroupColumn(column);
					else
					{
						column.setColumnWidth(rslt.getInt("COLUMN_WIDTH"));
						favorite.addColumn(column);
						
						int sortOrder = rslt.getInt("SORT_ORDER");
						if (!rslt.wasNull() && sortOrder > 0)
						{
							FavoriteSortColumnVO sort = new FavoriteSortColumnVO();
							sort.setColumnId(columnId);
							sort.setSortOrder(sortOrder);
							sort.setDescending(rslt.getString("DESCENDING_IND").equals("Y"));							
							favorite.addSortColumn(sort);
						}
					}
				}

				String dataField = rslt.getString("DATA_FIELD");
				String expression = rslt.getString("EXPRESSION");
				if (dataField != null && expression != null)
				{
					FavoriteFilterColumnVO filter = new FavoriteFilterColumnVO();
					filter.setDataField(dataField);
					filter.setExpression(expression);
					favorite.addFilterColumn(filter);
				}
			}
			
			if (favorite != null)
				favorites.add(favorite);
			
		}
		catch(SQLException e)
		{
			logger.severe("GridFavoriteDAO - Error selecting favorites for " + viewId + ", " + gridId + ", " + employeeId);
			e.printStackTrace();
			throw e;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();			
		} 
		finally 
		{
			if (rslt != null)
				rslt.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		}
		
		return favorites;
	}


	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public FavoriteVO selectFavorite(Map<String, Object> params) throws SQLException
	{
		FavoriteVO favorite = null;
		Connection conn = null;      
		PreparedStatement pstmt = null;
		ResultSet rslt = null;
		
		Long favoriteId = 0L;
		if (params != null && params.get("favoriteId") != null)
			favoriteId = (Long) params.get("favoriteId");
		
		if (favoriteId == 0L)				
			throw new SQLException("Unable to select favorite for " + favoriteId + " (" + params + ")");
		
		try
		{
			conn = getConn("CdwSQL");

			int col = 1;
			pstmt = conn.prepareStatement(getSql("GRID_selectFavorite"));
			pstmt.setLong(col++, favoriteId);
			
			rslt = pstmt.executeQuery();
			if (rslt.next())
				favorite = new FavoriteVO(rslt);
			
			rslt.close();
			pstmt.close();
			
			
			
			col = 1;
			pstmt = conn.prepareStatement(getSql("GRID_selectFavoriteColumns"));			
			pstmt.setLong(col++, favorite.getFavoriteId());

			rslt = pstmt.executeQuery();
			while (rslt.next())
			{
				favorite.addColumn(new FavoriteColumnVO(rslt));
				int sortOrder = rslt.getInt("SORT_ORDER");
				if (!rslt.wasNull() && sortOrder > 0)
					favorite.addSortColumn(new FavoriteSortColumnVO(rslt));
			}
			
			rslt.close();
			pstmt.close();
			
			
			
			col = 1;
			pstmt = conn.prepareStatement(getSql("GRID_selectFavoriteGroupColumns"));			
			pstmt.setLong(col++, favorite.getFavoriteId());

			rslt = pstmt.executeQuery();
			while (rslt.next())
				favorite.addGroupColumn(new FavoriteColumnVO(rslt));
			
			rslt.close();
			pstmt.close();
			
			
			
			col = 1;
			pstmt = conn.prepareStatement(getSql("GRID_selectFavoriteFilterColumns"));
			pstmt.setLong(col++, favorite.getFavoriteId());

			rslt = pstmt.executeQuery();
			while (rslt.next())
				favorite.addFilterColumn(new FavoriteFilterColumnVO(rslt));
			
		}
		catch(SQLException e)
		{
			logger.severe("GridFavoriteDAO - Error selecting favorite details for " + favorite.getFavoriteId());
			e.printStackTrace();
			throw e;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();			
		} 
		finally 
		{
			if (rslt != null)
				rslt.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		}
		
		return favorite;
	}
	
	
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int deleteFavorite(Long favoriteId, int employeeId) throws SQLException
	{
		Connection conn = null;      
		
		int deleted = 0;
		
		if (favoriteId == null || favoriteId == 0L || employeeId == 0)				
			throw new SQLException("Unable to delete favorite for " + favoriteId + ", " + employeeId);
		
		try
		{
			conn = getConn("CdwSQL");
			conn.setAutoCommit(false);
			
			// delete favorite
			deleted += deleteFavorite(conn, favoriteId, employeeId);
			
			// only delete records in other tables if record was deleted from favorite
			if (deleted > 0)
			{
				// delete columns
				deleted = deleteFavoriteColumns(conn, favoriteId);
				deleted = deleteFavoriteGroupColumns(conn, favoriteId);
				deleted = deleteFavoriteFilters(conn, favoriteId);
			}
			
			conn.commit();
		}
		catch(SQLException e)
		{
			if (conn != null)
				conn.rollback();
			logger.severe("GridFavoriteDAO - Error selecting favorite details for " + favoriteId);
			e.printStackTrace();
			throw e;
		} 
		catch (Exception e) 
		{
			if (conn != null)
				conn.rollback();
			e.printStackTrace();			
		} 
		finally 
		{
			if (conn != null)
				conn.close();
		}
		
		logger.info("Favorite " + favoriteId + ", " + employeeId + " deleted.  Deleted: " + deleted);
		
		return deleted;
	}
	
	
	/**
	 * 
	 * @param favorite
	 * @return
	 * @throws SQLException
	 */
	public int saveFavorite(FavoriteVO favorite, int employeeId) throws SQLException
	{		
		if (favorite == null || employeeId == 0)				
			throw new SQLException("Unable to save favorite!");
		
		Connection conn = null;      
		
		Long favoriteId = 0L;
		
		int inserted = 0;
		int deleted = 0;
		int updated = 0;
		
		try
		{
			conn = getConn("CdwSQL");
			conn.setAutoCommit(false);
			
			favoriteId = favorite.getFavoriteId();
			// If favoriteId is not defined, then check to see if 
			// favorite exists with the same name
			
			if (favoriteId == null || favoriteId == 0)
				favoriteId = lookupFavoriteId(conn, favorite.getViewId(), favorite.getGridId(), favorite.getFavoriteName(), employeeId);
			
			favorite.setFavoriteId(favoriteId);
			
			// If favorite doesn't then generate new favoriteId
			// and insert new favorite record.
			if (favoriteId == null || favoriteId == 0)
			{
				// Get favorite id for new record
				favoriteId = selectNewFavoriteId(conn);
				
				// store new favoriteId into object
				favorite.setFavoriteId(favoriteId);
				
				inserted = insertFavorite(conn, favoriteId, employeeId, favorite);
				
			}
			// Otherwise if favorite does exist, then update
			// it and delete the records in the other tables
			else
			{
				updated = updateFavorite(conn, favoriteId, employeeId, favorite);
				
				// delete columns (will be inserted again later)
				deleted = deleteFavoriteColumns(conn, favoriteId);
				deleted += deleteFavoriteGroupColumns(conn, favoriteId);
				deleted += deleteFavoriteFilters(conn, favoriteId);
			}
			
			inserted = insertFavoriteColumns(conn, favoriteId, favorite.getColumns());	
			
			updated = updateFavoriteSortColumns(conn, favoriteId, favorite.getSortColumns());
			inserted = insertFavoriteGroupColumns(conn, favoriteId, favorite.getGroupColumns());
			inserted = insertFavoriteFilters(conn, favoriteId, favorite.getFilterColumns());
			
			conn.commit();
		}
		catch(SQLException e)
		{
			if (conn != null)
				conn.rollback();
			logger.severe("GridFavoriteDAO - Error selecting favorite details for " + favorite.getFavoriteId());
			e.printStackTrace();
			throw e;
		} 
		catch (Exception e) 
		{
			if (conn != null)
				conn.rollback();
			e.printStackTrace();			
		} 
		finally 
		{			
			if (conn != null)
				conn.close();
		}
		
		logger.info("Favorite " + favoriteId + " saved.  Inserted: " + inserted + ", Updated: " + updated + ", Deleted: " + deleted);
		
		return inserted + updated;
	}
	
	
	
	private int updateFavorite(Connection conn, Long favoriteId, int employeeId, FavoriteVO favorite) throws SQLException
	{
		PreparedStatement pstmt = null;
		
		int updated = 0;
		int col = 1;
		
		try {
			// update existing favorite
			pstmt = conn.prepareStatement(getSql("GRID_updateFavorite"));
			pstmt.setString(col++, favorite.getViewId());
			pstmt.setString(col++, favorite.getGridId());
			pstmt.setString(col++, favorite.getFavoriteName());
			pstmt.setInt(col++, favorite.getLockedColumnCount());
			pstmt.setString(col++, (favorite.isShowFooterInd()?"Y":"N"));
			pstmt.setString(col++, (favorite.isPublicInd()?"Y":"N"));
			pstmt.setInt(col++, employeeId);
			pstmt.setLong(col++, favoriteId);
			updated += pstmt.executeUpdate();	
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		return updated;
	}
	
	private Long lookupFavoriteId(Connection conn, String viewId, String gridId, String favoriteName, int employeeId) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rslt = null;
		
		Long favoriteId = 0L;
		try
		{
			int col = 1;
			
			// get existing favorite ID based on the favorite name
			pstmt = conn.prepareStatement(getSql("GRID_lookupFavoriteId"));
			pstmt.setString(col++, viewId);
			pstmt.setString(col++, gridId);
			pstmt.setString(col++, favoriteName);
			pstmt.setInt(col++, employeeId);
			
			rslt = pstmt.executeQuery();
			
			if (rslt.next())
				favoriteId = rslt.getLong("FAVORITE_ID");
			
		} catch (SQLException e) {
			throw e;
		} finally {
			if (rslt != null)
				rslt.close();
			if (pstmt != null)
				pstmt.close();
		}
		
		return favoriteId;
	}
	
	private Long selectNewFavoriteId(Connection conn) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rslt = null;
		
		Long favoriteId = 0L;
		try
		{
			pstmt = conn.prepareStatement(getSql("GRID_selectNewFavoriteId"));
			
			rslt = pstmt.executeQuery();
			if (rslt.next())
				favoriteId = rslt.getLong("NEXTVAL");
			else
				throw new SQLException("Error generating new ID for Favorite.");
			
			rslt.close();
			pstmt.close();
			
		} catch (SQLException e) {
			throw e;
		} finally {
			if (rslt != null)
				rslt.close();
			if (pstmt != null)
				pstmt.close();
		}
		
		return favoriteId;
	}
	
	private int insertFavorite(Connection conn, Long favoriteId, int employeeId, FavoriteVO favorite) throws SQLException
	{
		PreparedStatement pstmt = null;
		
		int inserted = 0;
		int col = 1;

		try {
			pstmt = conn.prepareStatement(getSql("GRID_insertFavorite"));
			pstmt.setLong(col++, favoriteId);
			pstmt.setString(col++, favorite.getViewId());
			pstmt.setString(col++, favorite.getGridId());
			pstmt.setString(col++, favorite.getFavoriteName());
			pstmt.setInt(col++, favorite.getLockedColumnCount());
			pstmt.setString(col++, (favorite.isShowFooterInd()?"Y":"N"));
			pstmt.setString(col++, (favorite.isPublicInd()?"Y":"N"));
			pstmt.setInt(col++, employeeId);
			pstmt.setInt(col++, employeeId);
			
			inserted += pstmt.executeUpdate();
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		return inserted;
	}
	
	private int deleteFavorite(Connection conn, Long favoriteId, int employeeId) throws SQLException
	{
		PreparedStatement pstmt = null;
		
		int deleted = 0;
		
		try {
			pstmt = conn.prepareStatement(getSql("GRID_deleteFavorite"));
			pstmt.setLong(1, favoriteId);
			pstmt.setInt(2, employeeId);
			deleted += pstmt.executeUpdate();
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		
		return deleted;
	}
	
	private int insertFavoriteColumns(Connection conn, Long favoriteId, ArrayList<FavoriteColumnVO> columns) throws SQLException 
	{
		if (columns == null)
			return 0;
		
		PreparedStatement pstmt = null;
		
		int inserted = 0;
		int col = 1;
		int[] batchInserted;
		
		try {
			pstmt = conn.prepareStatement(getSql("GRID_insertFavoriteColumn"));
			
			for (FavoriteColumnVO column : columns)
			{
				col = 1;
				pstmt.setLong(col++, favoriteId);
				pstmt.setString(col++, column.getColumnId());
				pstmt.setInt(col++, column.getColumnOrder());
				if(column.getColumnWidth()>500)
				pstmt.setInt(col++, 100);
				else
					pstmt.setInt(col++,column.getColumnWidth());
					
				pstmt.addBatch();
			}
			batchInserted = pstmt.executeBatch();
			for (int batchInsert : batchInserted)
				inserted += batchInsert;
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		
		return inserted;
	}
	
	private int updateFavoriteSortColumns(Connection conn, Long favoriteId, ArrayList<FavoriteSortColumnVO> columns) throws SQLException
	{
		if (columns == null)
			return 0;
		
		PreparedStatement pstmt = null;
		
		int col = 1;
		int updated = 0;
		int[] batchUpdated;

		try {
			pstmt = conn.prepareStatement(getSql("GRID_updateFavoriteSortColumn"));
			
			for (FavoriteSortColumnVO column : columns)
			{
				col = 1;
				pstmt.setInt(col++, column.getSortOrder());
				pstmt.setString(col++, (column.isDescending()?"Y":"N"));
				pstmt.setLong(col++, favoriteId);
				pstmt.setString(col++, column.getColumnId());
				pstmt.addBatch();
			}
			batchUpdated = pstmt.executeBatch();
			for (int batchUpdate : batchUpdated)
				updated += batchUpdate;
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		
		return updated;
	}
	
	private int deleteFavoriteColumns(Connection conn, Long favoriteId) throws SQLException
	{
		PreparedStatement pstmt = null;
		
		int deleted = 0;
		
		try {
			pstmt = conn.prepareStatement(getSql("GRID_deleteFavoriteColumns"));
			pstmt.setLong(1, favoriteId);
			deleted += pstmt.executeUpdate();
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		
		return deleted;
	}
		
	private int insertFavoriteGroupColumns(Connection conn, Long favoriteId, ArrayList<FavoriteColumnVO> columns) throws SQLException 
	{
		if (columns == null)
			return 0;
		
		PreparedStatement pstmt = null;
		
		int inserted = 0;
		int col = 1;
		int[] batchInserted;
		
		try {
			pstmt = conn.prepareStatement(getSql("GRID_insertFavoriteGroupColumn"));
			
			for (FavoriteColumnVO column : columns)
			{
				col = 1;
				pstmt.setLong(col++, favoriteId);
				pstmt.setString(col++, column.getColumnId());
				pstmt.setInt(col++, column.getColumnOrder());
				pstmt.addBatch();
			}
			batchInserted = pstmt.executeBatch();
			for (int batchInsert : batchInserted)
				inserted += batchInsert;
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
				
		return inserted;
	}
	
	private int deleteFavoriteGroupColumns(Connection conn, Long favoriteId) throws SQLException
	{
		PreparedStatement pstmt = null;
		int deleted = 0;

		try {
			pstmt = conn.prepareStatement(getSql("GRID_deleteFavoriteGroupColumns"));
			pstmt.setLong(1, favoriteId);
			deleted += pstmt.executeUpdate();
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		
		return deleted;
	}
		
	private int insertFavoriteFilters(Connection conn, Long favoriteId, ArrayList<FavoriteFilterColumnVO> columns) throws SQLException 
	{
		if (columns == null)
			return 0;
		
		PreparedStatement pstmt = null;
		
		int inserted = 0;
		int col = 1;
		int[] batchInserted;
		
		try {
			pstmt = conn.prepareStatement(getSql("GRID_insertFavoriteFilterColumn"));
			
			for (FavoriteFilterColumnVO column : columns)
			{
				col = 1;
				pstmt.setLong(col++, favoriteId);
				pstmt.setString(col++, column.getDataField());
				pstmt.setString(col++, column.getExpression());
				pstmt.addBatch();
			}
			batchInserted = pstmt.executeBatch();
			for (int batchInsert : batchInserted)
				inserted += batchInsert;
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
				
		return inserted;
	}
	
	private int deleteFavoriteFilters(Connection conn, Long favoriteId) throws SQLException
	{
		PreparedStatement pstmt = null;
		
		int deleted = 0;
		
		try {
			pstmt = conn.prepareStatement(getSql("GRID_deleteFavoriteFilterColumn"));
			pstmt.setLong(1, favoriteId);
			deleted += pstmt.executeUpdate();
		}
		catch (SQLException e) 
		{
			throw e;
		} 
		finally 
		{
			if (pstmt != null)
				pstmt.close();
		}
		
		return deleted;
	}
}
