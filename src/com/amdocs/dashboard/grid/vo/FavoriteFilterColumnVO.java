package com.amdocs.dashboard.grid.vo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoriteFilterColumnVO {

	private Long favoriteId;
	private String dataField;
	private String expression;
	
	public FavoriteFilterColumnVO ()
	{
		
	}
	
	public FavoriteFilterColumnVO(ResultSet rslt) throws SQLException {
		setFromRslt(rslt);
	}

	private void setFromRslt(ResultSet rslt) throws SQLException {
		setFavoriteId(rslt.getLong("FAVORITE_ID"));
		setDataField(rslt.getString("DATA_FIELD"));
		setExpression(rslt.getString("EXPRESSION"));
	}
	
	public Long getFavoriteId() {
		return favoriteId;
	}
	public void setFavoriteId(Long favoriteId) {
		this.favoriteId = favoriteId;
	}
	public String getDataField() {
		return dataField;
	}
	public void setDataField(String dataField) {
		this.dataField = dataField;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
}
