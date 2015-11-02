package com.amdocs.dashboard.grid.vo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoriteSortColumnVO {

	private Long favoriteId;
	private String columnId;
	private int sortOrder;
	private boolean descending;
	
	public FavoriteSortColumnVO ()
	{
		
	}
	
	public FavoriteSortColumnVO(ResultSet rslt) throws SQLException {
		setFromRslt(rslt);
	}

	private void setFromRslt(ResultSet rslt) throws SQLException {
		setFavoriteId(rslt.getLong("FAVORITE_ID"));
		setColumnId(rslt.getString("COLUMN_ID"));
		setSortOrder(rslt.getInt("SORT_ORDER"));
		setDescending(rslt.getString("DESCENDING_IND").equals("Y"));
	}
	
	public Long getFavoriteId() {
		return favoriteId;
	}
	public void setFavoriteId(Long favoriteId) {
		this.favoriteId = favoriteId;
	}
	public String getColumnId() {
		return columnId;
	}
	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}
	public int getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	public boolean isDescending() {
		return descending;
	}
	public void setDescending(boolean descending) {
		this.descending = descending;
	}
}
