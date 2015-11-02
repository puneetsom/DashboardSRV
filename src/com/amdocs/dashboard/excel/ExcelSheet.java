package com.amdocs.dashboard.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.amdocs.dashboard.customDataGrid.DataColumn;

public class ExcelSheet {

	private String sheetName;
	private String type;
	private String headerText;
	private String queryCode;
	private List<?> data;
	private List<DataColumn> columns;
	private HashMap<String, Object> params;
	private HashMap<String, Object> additionalParams;
	private List<Integer> numColumns;
	private List<Integer> numRows;
	
	public ExcelSheet() { }

	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the queryCode
	 */
	public String getQueryCode() {
		return queryCode;
	}

	/**
	 * @param queryCode the queryCode to set
	 */
	public void setQueryCode(String queryCode) {
		this.queryCode = queryCode;
	}

	public List<?> getData() {
		return data;
	}
	public void setData(List<?> data) {
		this.data = data;
	}
	
	public List<DataColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<DataColumn> columns) {
		this.columns = columns;
	}
	public void addColumn(DataColumn column) {
		if (this.columns == null)
			this.columns = new ArrayList<DataColumn>();
		
		this.columns.add(column);
	}
	
	public HashMap<String, Object> getParams() {
		return params;
	}
	public void setParams(HashMap<String, Object> data) {
		this.params = data;
	}
	
	public HashMap<String, Object> getAdditionalParams()
	{		
		return additionalParams;
	}
	
	public void setAdditionalParams(HashMap<String, Object> data) {
		this.additionalParams = data;
	}
	
	/**
	 * Array of number of columns for each grid
	 */
	public List<Integer> getNumColumns() {
		// numColumns may not be set.  If not, define it based on the columns
		if (numColumns == null || numColumns.size() == 0 && columns != null)
		{
			numColumns = new ArrayList<Integer>();
			numColumns.add(columns.size());
		}
		return numColumns;
	}
	public void setNumColumns(List<Integer> numColumns) {			
		this.numColumns = numColumns;
	}
	
	/**
	 * Array of number of rows for each grid
	 */
	public List<Integer> getNumRows() {
		// numColumns may not be set.  If not, define it based on the number of rows
		if (numRows == null || numRows.size() == 0 && data != null)
		{
			numRows = new ArrayList<Integer>();
			numRows.add(data.size());
		}
		return numRows;
	}
	public void setNumRows(List<Integer> numRows) {			
		this.numRows = numRows;
	}

	/**
	 * @param headerText the headerText to set
	 */
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	/**
	 * @return the headerText
	 */
	public String getHeaderText() {
		return headerText;
	}
}
