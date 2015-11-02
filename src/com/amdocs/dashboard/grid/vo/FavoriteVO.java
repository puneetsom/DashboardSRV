package com.amdocs.dashboard.grid.vo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class FavoriteVO {
	private Long favoriteId;
	private String viewId;
	private String gridId;
	private String favoriteName;
	private int lockedColumnCount;
	private boolean showFooterInd;
	private boolean publicInd;
	private Timestamp createDate;
	private Integer createOperId;
	private String createOperName;
	private Timestamp updateDate;
	private Integer updateOperId;
	private String updateOperName;

	private ArrayList<FavoriteColumnVO> columns;
	private ArrayList<FavoriteColumnVO> groupColumns;
	private ArrayList<FavoriteSortColumnVO> sortColumns;
	private ArrayList<FavoriteFilterColumnVO> filterColumns;
	
	public FavoriteVO()
	{
		
	}

	public FavoriteVO(ResultSet rslt) throws SQLException {
		setFromRslt(rslt);
	}

	public Long getFavoriteId() {
		return favoriteId;
	}

	public void setFavoriteId(Long favoriteId) {
		this.favoriteId = favoriteId;
	}

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public String getGridId() {
		return gridId;
	}

	public void setGridId(String gridId) {
		this.gridId = gridId;
	}

	public String getFavoriteName() {
		return favoriteName;
	}

	public void setFavoriteName(String favoriteName) {
		this.favoriteName = favoriteName;
	}

	public int getLockedColumnCount() {
		return lockedColumnCount;
	}

	public void setLockedColumnCount(int lockedColumnCount) {
		this.lockedColumnCount = lockedColumnCount;
	}

	public boolean isShowFooterInd() {
		return showFooterInd;
	}

	public void setShowFooterInd(boolean showFooterInd) {
		this.showFooterInd = showFooterInd;
	}

	public boolean isPublicInd() {
		return publicInd;
	}

	public void setPublicInd(boolean publicInd) {
		this.publicInd = publicInd;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public Integer getCreateOperId() {
		return createOperId;
	}

	public void setCreateOperId(Integer createOperId) {
		this.createOperId = createOperId;
	}

	public String getCreateOperName() {
		return createOperName;
	}

	public void setCreateOperName(String createOperName) {
		this.createOperName = createOperName;
	}

	public Timestamp getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}

	public Integer getUpdateOperId() {
		return updateOperId;
	}

	public void setUpdateOperId(Integer updateOperId) {
		this.updateOperId = updateOperId;
	}

	public String getUpdateOperName() {
		return updateOperName;
	}

	public void setUpdateOperName(String updateOperName) {
		this.updateOperName = updateOperName;
	}

	public ArrayList<FavoriteColumnVO> getColumns() {
		return columns;
	}

	public void setColumns(ArrayList<FavoriteColumnVO> columns) {
		this.columns = columns;
	}

	public ArrayList<FavoriteColumnVO> getGroupColumns() {
		return groupColumns;
	}

	public void setGroupedColumns(ArrayList<FavoriteColumnVO> groupColumns) {
		this.groupColumns = groupColumns;
	}

	public ArrayList<FavoriteSortColumnVO> getSortColumns() {
		return sortColumns;
	}

	public void setSortColumns(ArrayList<FavoriteSortColumnVO> sortColumns) {
		this.sortColumns = sortColumns;
	}

	public ArrayList<FavoriteFilterColumnVO> getFilterColumns() {
		return filterColumns;
	}

	public void setFilterColumns(ArrayList<FavoriteFilterColumnVO> filterColumns) {
		this.filterColumns = filterColumns;
	}

	public void setFromRslt(ResultSet rslt) throws SQLException 
	{
		setFavoriteId(rslt.getLong("FAVORITE_ID"));
		setViewId(rslt.getString("VIEW_ID"));
		setGridId(rslt.getString("GRID_ID"));
		setFavoriteName(rslt.getString("FAVORITE_NAME"));
		setLockedColumnCount(rslt.getInt("LOCKED_COLUMN_COUNT"));
		setShowFooterInd((rslt.getString("SHOW_FOOTER_IND").equals("Y")));
		setPublicInd((rslt.getString("PUBLIC_IND").equals("Y")));
		setCreateDate(rslt.getTimestamp("CREATE_DATE"));
		setCreateOperId(rslt.getInt("CREATE_OPER_ID"));
		setCreateOperName("CREATE_OPER_NAME");
		//setUpdateOperId(rslt.getInt("UPDATE_OPER_ID"));
		//setUpdateOperName("Not defined!");
		//setUpdateDate(rslt.getTimestamp("UPDATE_DATE"));
	}

	public void addColumn(FavoriteColumnVO column) {
		if (columns == null)
			columns = new ArrayList<FavoriteColumnVO>();
		columns.add(column);
	}

	public void addGroupColumn(FavoriteColumnVO column) {
		if (groupColumns == null)
			groupColumns = new ArrayList<FavoriteColumnVO>();
		groupColumns.add(column);
	}

	public void addSortColumn(FavoriteSortColumnVO column) {
		if (sortColumns == null)
			sortColumns = new ArrayList<FavoriteSortColumnVO>();
		sortColumns.add(column);	
	}

	public void addFilterColumn(FavoriteFilterColumnVO column) {
		if (filterColumns == null)
			filterColumns = new ArrayList<FavoriteFilterColumnVO>();
		filterColumns.add(column);
	}
	
	
	
}
