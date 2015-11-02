package com.amdocs.dashboard.excel;

import java.util.ArrayList;
import java.util.List;


public class ExcelWorkbook {

	private String filename;
	private List<ExcelSheet> sheets;
	private String workbookObject;
	
	public ExcelWorkbook() { }
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public List<ExcelSheet> getSheets() {
		return sheets;
	}
	public void setSheets(List<ExcelSheet> sheets) {
		this.sheets = sheets;
	}
	public void addSheet(ExcelSheet sheet) {
		if (this.sheets == null)
			this.sheets = new ArrayList<ExcelSheet>();
		
		this.sheets.add(sheet);
	}

	public void setWorkbookObject(String workbookObject) {
		this.workbookObject = workbookObject;
	}
	public String getWorkbookObject() {
		return workbookObject;
	}
	
}
