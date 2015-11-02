package com.amdocs.dashboard.grid.vo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoriteColumnVO {

	private Long favoriteId;
	private String columnId;
	private int columnOrder;
	private int columnWidth;
	
	public FavoriteColumnVO ()
	{
		
	}
	
	public FavoriteColumnVO(ResultSet rslt) throws SQLException {
		setFromRslt(rslt);
	}

	private void setFromRslt(ResultSet rslt) throws SQLException {
		setFavoriteId(rslt.getLong("FAVORITE_ID"));
		setColumnId(rslt.getString("COLUMN_ID"));
		setColumnOrder(rslt.getInt("COLUMN_ORDER"));
		setColumnWidth(rslt.getInt("COLUMN_WIDTH"));
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
	public int getColumnOrder() {
		return columnOrder;
	}
	public void setColumnOrder(int columnOrder) {
		this.columnOrder = columnOrder;
	}
	public int getColumnWidth() {
		return columnWidth;
	}
	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}
}
