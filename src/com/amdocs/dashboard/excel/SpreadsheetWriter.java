package com.amdocs.dashboard.excel;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

public  class SpreadsheetWriter {
	private static final String XML_ENCODING = "UTF-8";
    private final Writer _out;
    private int _rownum;
    private List<String> mergedRegionList=new ArrayList<String>();    
	private Map<Integer,Double> columnWidthMap = new TreeMap<Integer,Double>();
	private int _rowHeight = 15;
	

    public SpreadsheetWriter(Writer out){
        _out = out;
    }

    public void beginSheet() throws IOException {
        _out.write("<?xml version=\"1.0\" encoding=\""+XML_ENCODING+"\"?>" +
                "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" );
        if(!columnWidthMap.isEmpty()){ 
			_out.write("<cols>");  
			Set<Integer> keySet=columnWidthMap.keySet();  
			Iterator<?> iterator=keySet.iterator();  
			while (iterator.hasNext()) {  
				Integer col = (Integer)iterator.next();  
				_out.write("<col min=\""+col+"\" max=\""+col+"\" width=\""+columnWidthMap.get(col)+"\" customWidth=\"1\"/>");   
			}  
			_out.write("</cols>");  
		} 
        _out.write("<sheetData>\n");
    }

    public void endSheet() throws IOException {
        _out.write("</sheetData>");
        if(!mergedRegionList.isEmpty()){  
			_out.write("<mergeCells>");  
			ExcelUtil.sortList(mergedRegionList);  
			for(int i=0, l = mergedRegionList.size(); i<l; i++){ 
			_out.write("<mergeCell ref=\""+mergedRegionList.get(i)+"\"/>");  
			} 
		_out.write("</mergeCells>");  
		}
        _out.write("</worksheet>");
    }

    /**
     * Insert a new row
     *
     * @param rownum 0-based row number
     */
    public void insertRow(int rownum) throws IOException 
    {    	 
        _out.write("<row r=\""+(rownum+1)+ "\" ht=\""+_rowHeight+ "\" customHeight=\"1\">\n");
        this._rownum = rownum;
    }

    /**
     * Insert row end marker
     */
    public void endRow() throws IOException {
        _out.write("</row>\n");
    }

    public void createCell(int columnIndex, String value, int styleIndex) throws IOException {
        String ref = new CellReference(_rownum, columnIndex).formatAsString();
        _out.write("<c r=\""+ref+"\" t=\"inlineStr\"");
        if(styleIndex != -1) _out.write(" s=\""+styleIndex+"\"");
        _out.write(">");
        _out.write("<is><t>"+convertAsciiToXml(value)+"</t></is>");
        _out.write("</c>");
    }

    public void createCell(int columnIndex, String value) throws IOException {
        createCell(columnIndex, value, -1);
    }

    public void createCell(int columnIndex, double value, int styleIndex) throws IOException {
        String ref = new CellReference(_rownum, columnIndex).formatAsString();
        _out.write("<c r=\""+ref+"\" t=\"n\"");
        if(styleIndex != -1) _out.write(" s=\""+styleIndex+"\"");
        _out.write(">");
        _out.write("<v>"+value+"</v>");
        _out.write("</c>");
    }

    public void createCell(int columnIndex, double value) throws IOException {
        createCell(columnIndex, value, -1);
    }

    public void createCell(int columnIndex, Calendar value, int styleIndex) throws IOException {
        createCell(columnIndex, DateUtil.getExcelDate(value, false), styleIndex);
    }

    public void createCell(int columnIndex, Date value, int styleIndex) throws IOException {
        createCell(columnIndex, DateUtil.getExcelDate(value, false), styleIndex);
    }
    
    public void addMergedRegion(CellRangeAddress range){
		mergedRegionList.add(range.formatAsString());
	}    
	
	public void setCellWidth(int columnIndex, double width){
		this.columnWidthMap.put(Integer.valueOf(columnIndex), Double.valueOf(width));
	}
	
	public void setRowHeight(int height){
		this._rowHeight = height;
	}
	
	public void resetColumnWidthMap()
	{
		columnWidthMap.clear();
	}
	
	public void close() throws IOException{  
		if(_out!=null)  
			_out.close();
	}
	
    private static String convertAsciiToXml(String string) {
        if (string == null || string.equals(""))
            return "";

        return string.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}