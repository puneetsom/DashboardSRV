package com.amdocs.dashboard.excel;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

import com.amdocs.dashboard.customDataGrid.DataColumn;
import com.amdocs.infra.querycache.QueryCacheServer;


public class ExcelGenerator 
{
	private static final String XML_ENCODING = "UTF-8";

	private Map<String,XSSFCellStyle> styles = new HashMap<String,XSSFCellStyle>();
	private List<File> sheetFiles = new ArrayList<File>();
	
	private static XSSFColor DARK_GREY = new XSSFColor(new java.awt.Color(66, 66, 66));
	private static XSSFColor GREY = new XSSFColor(new java.awt.Color(200, 200, 200));
	private static XSSFColor LIGHT_GREY = new XSSFColor(new java.awt.Color(240, 240, 240));
	
	private static String FORM_VALUE = new String("formValue");
	private static String TYPE_FORM = new String("form");
	private static String TYPE_DATA_GRID = new String("dataGrid");
	private static String EMPTY_STR = new String("");
	private static String FORM_HEADER = new String("FORM_HEADER");
	private static String FORM_VALUES = new String("FORM_VALUES");
	private static String HEADER_CENTER = new String("HEADER_CENTER");
	
	private static String LETTER_I = new String("i");
	private static String LETTER_H = new String("h");
	private static String LETTER_K = new String("k");
	private static String LETTER_M = new String("m");
	
	private static String LETTER_G = new String("G");
	private static String LETTER_R = new String("R");
	private static String LETTER_O = new String("O");
	private static String LETTER_Y = new String("Y");
	
	private static String GREEN = new String("Green");
	private static String RED = new String("Red");
	private static String ORANGE = new String("Orange");
	private static String YELLOW = new String("Yellow");
	
	private static String DOUBLE = new String("java.lang.Double");
	private static String INTEGER = new String("java.lang.Integer");
	private static String LONG = new String("java.lang.Long");
	private static String DATE = new String("java.util.Date");
	
	private static String VERDANA = new String("Verdana");	
	private static String WINGDINGS_3 = new String("Wingdings 3");
	private static String UNDERSCORE = new String("_");
	private static String ZERO_STR = new String("0");
	private static String ONE_STR = new String("1");
	
	private int rownum = 0;
	private int colnum = 0;
	private List<Integer> headerTextList = new ArrayList<Integer>();
	
	// any workbook with more than 10000 rows or 300000 cells not be auto-formatted
	//private boolean shouldBeAutoSize = true;
	//private boolean shouldBeMerge = true;
	
	private long startTime = System.currentTimeMillis();
	
	public String requestGenerateExcel(ExcelWorkbook data) throws Exception
	{
		//System.out.println(data);
		try {
			//System.out.println(timeSince() + " - Files created.");
			populateSheetWithData(data);
			File exportFile = createWorkbook(data);
			
			System.out.println("requestGenerateExcel file: " + exportFile.getAbsolutePath());
			return exportFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;	
		}
	}
	
	
	private void populateSheetWithData(ExcelWorkbook data) throws Exception
	{
		for (ExcelSheet sheet : data.getSheets()) 
		{
			if(sheet.getQueryCode() != null && sheet.getQueryCode() != "")
			{
				List<Map<String,Object>> dataProvider = null;
				try {
					
					dataProvider = (List<Map<String,Object>>)QueryCacheServer.getInstance().activateExcelQuery(sheet.getQueryCode(), sheet.getParams(), sheet.getAdditionalParams());					
					
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new Exception(e.getMessage());
				}
				sheet.setData(dataProvider);
			}
		}
	}
	

	public byte[] requestExcelBytes(ExcelWorkbook data) throws Exception
	{
		try {
			
			populateSheetWithData(data);			
			// create xlsx file for export
			File exportFile = createWorkbook(data);

			// Create byte[] for output from export.xlsx
			//System.out.println(timeSince() + " - start bytes read.");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(exportFile);
			byte[] buf = new byte[1024];
			for (int readNum; (readNum = fis.read(buf)) != -1;)
				bos.write(buf, 0, readNum); //no doubt here is 0
			//System.out.println(timeSince() + " - end bytes read.");  
			
			byte[] byteData = bos.toByteArray();
			fis.close();
			bos.close();
			
			// delete export files since it is no longer needed on the server
			exportFile.delete();
			//System.out.println(timeSince() + " - file deleted.");
			
			return byteData;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;	
		}		
	}
	
	
	private File createWorkbook(ExcelWorkbook data) throws Exception
	{
		// create xlsx file for export
		File templateFile = File.createTempFile("template",".xlsx");
		File exportFile = File.createTempFile("export",".xlsx");
		
		// cell styles, number formats, etc.
		XSSFWorkbook wb = new XSSFWorkbook();

		// create styles based on sheet's DataColumns
		createStyles(wb, data);
		//System.out.println(timeSince() + " - styles created. (" + styles.size() + ")");

		// create sheets for workbook
		createWorkbookSheets(wb, data, null);
		//System.out.println(timeSince() + " - sheets created.");

		// save the template
		FileOutputStream os = new FileOutputStream(templateFile);
		wb.write(os);
		os.close();
		//System.out.println(timeSince() + " - template saved.");

		// Generates sheet XML files
		populateSheetData(data);
		//System.out.println(timeSince() + " - sheets populated.");

		// Substitute sheets
		substituteMultiSheet(templateFile, exportFile);
		//System.out.println(timeSince() + " - sheets subtituted.");
		//System.out.println("shouldBeAutoSize : " + shouldBeAutoSize);
		//System.out.println("shouldBeMerge : " + shouldBeMerge);
		
		/*if (shouldBeAutoSize && shouldBeMerge)
		{
			//formatWorkbookWithMerge(exportFile, data);
			System.out.println(timeSince() + " - workbook formatted and merge");
		}else if(shouldBeAutoSize){
			//formatWorkbook(exportFile, data);
			System.out.println(timeSince() + " - workbook formatted.");
		}else if(shouldBeMerge){
			//mergeColumnGroups(exportFile, data);
			System.out.println(timeSince() + " - workbook merged.");
		}*/
		
		
		// cleanup files
		templateFile.delete();
		for (File sheetFile: sheetFiles) 
			sheetFile.delete();
		
		return exportFile;
	}
	
	/*private void mergeColumnGroups(File exportFile, ExcelWorkbook data) throws InvalidFormatException, IOException {

		FileInputStream fis = new FileInputStream(exportFile);
		XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(fis);
		//System.out.println(timeSince() + " - workbook read for formatting.");
		
		int sheetIndex = 0;
		for (ExcelSheet obj : data.getSheets()) 
		{
			XSSFSheet sheet = wb.getSheetAt(sheetIndex);
			String type = obj.getType();
			if (type.equals(TYPE_DATA_GRID))
			{
				List<DataColumn> columns = obj.getColumns();
				merge(sheet, columns, sheetIndex);
			}
			//System.out.println(timeSince() + " - processed sheet " + sheet.getSheetName() + ".");
			sheetIndex++;
		}

		FileOutputStream fileOut = new FileOutputStream(exportFile);
		wb.write(fileOut);
		fileOut.close();
	}
	
	private void formatWorkbookWithMerge(File exportFile, ExcelWorkbook data) throws InvalidFormatException, IOException {

		FileInputStream fis = new FileInputStream(exportFile);
		XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(fis);
		//System.out.println(timeSince() + " - workbook read for formatting.");
		
		int sheetIndex = 0;
		for (ExcelSheet obj : data.getSheets()) 
		{
			XSSFSheet sheet = wb.getSheetAt(sheetIndex);
			String type = obj.getType();
			if (type.equals(TYPE_DATA_GRID))
			{
				List<DataColumn> columns = obj.getColumns();
				merge(sheet, columns, sheetIndex);
				// increase header row height
				sheet.getRow(0).setHeightInPoints(2*16);
				
				for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++)
				{
					DataColumn dc = columns.get(columnIndex);
					if(!EMPTY_STR.equalsIgnoreCase(dc.getHeaderLabel())){
						sheet.autoSizeColumn(columnIndex);
						// Don't allow column widths to be larger than 100 pts
						if (sheet.getColumnWidth(columnIndex) > 100 * 256)
							sheet.setColumnWidth(columnIndex, 100 * 256);
					}					
				}
			}else if(TYPE_FORM.equalsIgnoreCase(type)){
				sheet.autoSizeColumn(0);
				sheet.autoSizeColumn(1);
				
				int cnt = 1;
				for(Integer formChangeIndex : formChangeIndexes){
					int columnIndex = cnt * 3;
					sheet.autoSizeColumn(columnIndex);
					sheet.autoSizeColumn(columnIndex + 1);
					cnt++;
				}
				
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> formData = (List<Map<String,Object>>) obj.getData();
				int formRowNum = 0;
				for(@SuppressWarnings("unused") Map<String,Object> formElement : formData){
					if(obj.getFormsPerSheet() > 1 || formChangeIndexes.size() > 0){
						if(formRowNum < formChangeIndexes.get(0))
						sheet.getRow(formRowNum).setHeightInPoints(2*16);
						formRowNum++;
					}else{
						sheet.getRow(formRowNum).setHeightInPoints(2*16);
						formRowNum++;
					}						
				}
			}
			//System.out.println(timeSince() + " - processed sheet " + sheet.getSheetName() + ".");
			sheetIndex++;
		}

		FileOutputStream fileOut = new FileOutputStream(exportFile);
		wb.write(fileOut);
		fileOut.close();
	}
	
	
	private void formatWorkbook(File exportFile, ExcelWorkbook data) throws InvalidFormatException, IOException {

		FileInputStream fis = new FileInputStream(exportFile);
		XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(fis);
		//System.out.println(timeSince() + " - workbook read for formatting.");
		
		int sheetIndex = 0;
		for (ExcelSheet obj : data.getSheets()) 
		{
			XSSFSheet sheet = wb.getSheetAt(sheetIndex);
			String type = obj.getType();
			if (type.equals(TYPE_DATA_GRID))
			{
				List<DataColumn> columns = obj.getColumns();
				sheet.getRow(0).setHeightInPoints(2*16);
				
				for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++)
				{
					DataColumn dc = columns.get(columnIndex);
					if(!EMPTY_STR.equalsIgnoreCase(dc.getHeaderLabel())){
						sheet.autoSizeColumn(columnIndex);
						// Don't allow column widths to be larger than 100 pts
						if (sheet.getColumnWidth(columnIndex) > 100 * 256)
							sheet.setColumnWidth(columnIndex, 100 * 256);
					}					
				}
			}else if(TYPE_FORM.equalsIgnoreCase(type)){
				sheet.autoSizeColumn(0);
				sheet.autoSizeColumn(1);
				
				int cnt = 1;
				for(Integer formChangeIndex : formChangeIndexes){
					int columnIndex = cnt * 3;
					sheet.autoSizeColumn(columnIndex);
					sheet.autoSizeColumn(columnIndex + 1);
					cnt++;
				}
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> formData = (List<Map<String,Object>>) obj.getData();
				int formRowNum = 0;
				for(@SuppressWarnings("unused") Map<String,Object> formElement : formData){
					if(obj.getFormsPerSheet() > 1 || formChangeIndexes.size() > 0){
						if(formRowNum < formChangeIndexes.get(0))
						sheet.getRow(formRowNum).setHeightInPoints(2*16);
						formRowNum++;
					}else{
						sheet.getRow(formRowNum).setHeightInPoints(2*16);
						formRowNum++;
					}
				}
			}
			//System.out.println(timeSince() + " - processed sheet " + sheet.getSheetName() + ".");
			sheetIndex++;
		}

		FileOutputStream fileOut = new FileOutputStream(exportFile);
		wb.write(fileOut);
		fileOut.close();
	}
	
	
	private void merge(XSSFSheet sheet, List<DataColumn> columns, int sheetIndex)
	{
		int firstRow = 0;		
		if(headerTextList.size() > 0 && headerTextList.get(sheetIndex) != null){
			sheet.addMergedRegion(new CellRangeAddress(
					firstRow, //first row (0-based)
					firstRow, //last row  (0-based)
					0, 
					columns.size()-1
			));
			firstRow++;
		}		
		
		//To Check the presence of group columns. If not then no merging required
		boolean hasAnyGroupCol = false;
		for (DataColumn column : columns)
		{
			if(column.isGroupingReq()) {
				hasAnyGroupCol = true;
				break;
			}			
		}
		
		if (!hasAnyGroupCol)
			return;
		
		int colIndex = 0;
		Map<Integer, DataColumn> unsortedGroupColMap = new HashMap<Integer, DataColumn>();		
		for (DataColumn column : columns)
		{
			if (column.isGroupingReq())
				unsortedGroupColMap.put(colIndex, column);
			else
				sheet.addMergedRegion(new CellRangeAddress(
						firstRow, //first row (0-based)
						firstRow+1, //last row  (0-based)
						colIndex, 
						colIndex 
				));
			
			colIndex++;
		}

		Map<Integer, DataColumn> groupedCols = new TreeMap<Integer, DataColumn>(unsortedGroupColMap);
		
		int groupedColCnt = 0;
		Object[] groupedColArr = groupedCols.values().toArray();	
		for (int count = 0; count < groupedColArr.length; count++)
		{
			if (count+1 >= groupedColArr.length)
				break;
			
			DataColumn groupedCell = (DataColumn)groupedColArr[count];			
			String currVal =  groupedCell.getMonthLbl();
			
			DataColumn nextGroupedCell = (DataColumn)groupedColArr[count+1];
			String nextVal =  nextGroupedCell.getMonthLbl();
			if (currVal.equalsIgnoreCase(nextVal))
			{
				groupedColCnt++;
				continue;
			} 
			else			
				break;
			
		}//for loop

		groupedColArr = groupedCols.keySet().toArray();
		for(int count = 0; count < groupedColArr.length;count++){
			Integer index = (Integer)groupedColArr[count]; 

			sheet.addMergedRegion(new CellRangeAddress(
					firstRow, //first row (0-based)
					firstRow, //last row  (0-based)
					index, 
					index+groupedColCnt
			));
			count = count + groupedColCnt;
		}//for loop	
	}*/
	

	// Creates XSSFSheets in XSSFWorkbook
	private void createWorkbookSheets(XSSFWorkbook wb, ExcelWorkbook data, Map<String, File> sheetRefs) throws IOException 
	{		
		for (ExcelSheet obj : data.getSheets()) 
		{
			XSSFSheet sheet = (XSSFSheet) wb.createSheet(obj.getSheetName());
			String sheetRef = sheet.getPackagePart().getPartName().getName();
			if(sheetRefs != null){
				File tmp = File.createTempFile("sheet_" + obj.getSheetName() + UNDERSCORE,".xml");
				sheetRefs.put(sheetRef, tmp);
			}
		}
	}
	
	
	private void populateSheetData(ExcelWorkbook data) throws Exception
	{
		int sheetIndex = 0;
		for (ExcelSheet sheet : data.getSheets()) 
		{
			String type = sheet.getType();

			File sheetFile = File.createTempFile("sheet", ".xml");
			sheetFiles.add(sheetFile);
			Writer fw = new OutputStreamWriter(new FileOutputStream(sheetFile), XML_ENCODING);
			SpreadsheetWriter sw = new SpreadsheetWriter(fw);
			
			sw.resetColumnWidthMap();
			formatSheet(sw, sheet);			
			sw.beginSheet();	
			
			rownum = 0;
			colnum = 0;
			
			// generate sheet based on CustomDataGrid and spark DataGrid.
			if (type.equals(TYPE_DATA_GRID))
			{
				generateDataGrid(sw, sheet, sheetIndex);
				
			} 
			else if(type.equals(TYPE_FORM)) 
			{
				
			    @SuppressWarnings("unchecked")
				List<Map<String,Object>> formData = (List<Map<String,Object>>) sheet.getData();
			    
				for(Map<String,Object> formElement : formData)
				{				
					sw.setRowHeight(2 * 16);					
					sw.insertRow(rownum++);
					int colnum = 0;
					
					
					DataColumn column = (DataColumn) formElement.get("column");
					
					//if (column.getHeaderLabel() != null && (column.getHeaderLabel().contains("&") || column.getHeaderLabel().contains("<") || column.getHeaderLabel().contains(">"))) {
					//	sw.createCell(colnum++,"<![CDATA[" + column.getHeaderLabel() + "]]>", styles.get(FORM_HEADER).getIndex());
					//} else {
					sw.createCell(colnum++,column.getHeaderLabel(), styles.get(FORM_HEADER).getIndex());
					//}
					if(formElement.get(FORM_VALUE)==null){
						if(column.getType().equalsIgnoreCase("string") && EMPTY_STR.equalsIgnoreCase((String)formElement.get(FORM_VALUE)))
							sw.createCell(colnum++,EMPTY_STR,styles.get(FORM_HEADER).getIndex());
					}
					else
					{
						String formStr = formElement.get(FORM_VALUE).toString();
						
						short styleIndex = styles.get(FORM_VALUES + UNDERSCORE + column.getCellStyleName()).getIndex();
						
						
						if (column.getJavaClass().equals(DOUBLE)) {
							if(EMPTY_STR.equalsIgnoreCase(formStr))
								sw.createCell(colnum++,formStr, styleIndex);
							else
								sw.createCell(colnum++, new Double(formStr)/(column.getType().equals("percent") && !column.isMultiplyBy100()?100:1), styleIndex);							
						} else if (column.getJavaClass().equals(LONG)) {
							if(EMPTY_STR.equalsIgnoreCase(formStr))
								sw.createCell(colnum++,formStr, styleIndex);
							else
								sw.createCell(colnum++, new Long(formStr)/(column.getType().equals("percent") && !column.isMultiplyBy100()?100:1) , styleIndex);								
						} else if (column.getJavaClass().equals(INTEGER)) {
							if(EMPTY_STR.equalsIgnoreCase(formStr))
								sw.createCell(colnum++,formStr, styleIndex);
							else
								sw.createCell(colnum++, new Integer(formStr)/(column.getType().equals("percent") && !column.isMultiplyBy100()?100:1), styleIndex);
						} else if (column.getJavaClass().equals(DATE)) {
							sw.createCell(colnum++, (java.util.Date) formElement.get(FORM_VALUE), styleIndex);
						//} else if (formStr.contains("&") || formStr.contains("<") || formStr.contains(">")) {
						//	sw.createCell(colnum++,"<![CDATA[" + formStr + "]]>", styleIndex);
						} else {
							sw.createCell(colnum++,formStr, styleIndex);
						}										
					}
					//if(!isCompelete)
						//handleMultipleFormPerSheet(sw, sheet, formData, colnum);
					sw.endRow();					
				}
				
			}//end elseif
			
			mergeColumns(sw, sheet.getColumns(), sheetIndex);
			
			sw.endSheet();
			fw.close();
			
			sheetIndex++;
		}
	}
	
	private void formatSheet(SpreadsheetWriter sw, ExcelSheet sheet) throws InvalidFormatException, IOException 
	{	
		String type = sheet.getType();
		if (type.equals(TYPE_DATA_GRID))
		{
			List<DataColumn> columns = sheet.getColumns();
			
			for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++)
			{
				DataColumn dc = columns.get(columnIndex);
				if(!EMPTY_STR.equalsIgnoreCase(dc.getHeaderLabel())){
					if(dc.getWidth() == 0)
						sw.setCellWidth(columnIndex+1, 100/7.5);
					else
						sw.setCellWidth(columnIndex+1, dc.getWidth()/7.5);					
				}					
			}
		}else if(TYPE_FORM.equalsIgnoreCase(type)){
			sw.setCellWidth(1, 15);
			sw.setCellWidth(2, 25);			
		}
			
	}
	
	private void mergeColumns(SpreadsheetWriter sw, List<DataColumn> columns, int sheetIndex)
	{
		int firstRow = 0;		
		if(headerTextList.size() > 0 && headerTextList.get(sheetIndex) != null){
			sw.addMergedRegion(new CellRangeAddress(
					firstRow, //first row (0-based)
					firstRow, //last row  (0-based)
					0, 
					columns.size()-1
			));
			firstRow++;
		}		
		
		//To Check the presence of group columns. If not then no merging required
		boolean hasAnyGroupCol = false;
		for (DataColumn column : columns)
		{
			if(column.isGroupingReq()) {
				hasAnyGroupCol = true;
				break;
			}			
		}
		
		if (!hasAnyGroupCol)
			return;
		
		int colIndex = 0;
		Map<Integer, DataColumn> unsortedGroupColMap = new HashMap<Integer, DataColumn>();		
		for (DataColumn column : columns)
		{
			if (column.isGroupingReq())
				unsortedGroupColMap.put(colIndex, column);
			else
				sw.addMergedRegion(new CellRangeAddress(
						firstRow, //first row (0-based)
						firstRow+1, //last row  (0-based)
						colIndex, 
						colIndex 
				));
			
			colIndex++;
		}

		Map<Integer, DataColumn> groupedCols = new TreeMap<Integer, DataColumn>(unsortedGroupColMap);
		
		int groupedColCnt = 0;
		Object[] groupedColArr = groupedCols.values().toArray();	
		for (int count = 0; count < groupedColArr.length; count++)
		{
			if (count+1 >= groupedColArr.length)
				break;
			
			DataColumn groupedCell = (DataColumn)groupedColArr[count];			
			String currVal =  groupedCell.getMonthLbl();
			
			DataColumn nextGroupedCell = (DataColumn)groupedColArr[count+1];
			String nextVal =  nextGroupedCell.getMonthLbl();
			if (currVal.equalsIgnoreCase(nextVal))
			{
				groupedColCnt++;
				continue;
			} 
			else			
				break;
			
		}//for loop

		groupedColArr = groupedCols.keySet().toArray();
		for(int count = 0; count < groupedColArr.length;count++){
			Integer index = (Integer)groupedColArr[count]; 

			sw.addMergedRegion(new CellRangeAddress(
					firstRow, //first row (0-based)
					firstRow, //last row  (0-based)
					index, 
					index+groupedColCnt
			));
			count = count + groupedColCnt;
		}//for loop	
	}
	
		
	/*private void handleMultipleFormPerSheet(SpreadsheetWriter sw, 
											ExcelSheet sheet, 
											List<Map<String,Object>> formData,
											int columnNum) throws Exception
	{
		for(Integer formChangeIndex : formChangeIndexes){
			if(sheet.getFormsPerSheet() > 1 && (formChangeIndex+rownum) < formData.size()){
				
				columnNum++;
				Map<String,Object> formElement1 = formData.get(formChangeIndex+rownum);
				DataColumn column1 = (DataColumn)formElement1.get("column");
				
				if(EMPTY_STR.equalsIgnoreCase(column1.getHeaderLabel())){
					isCompelete = true;
					break;					
				}			
				
				sw.createCell(columnNum++,column1.getHeaderLabel(), styles.get(FORM_HEADER).getIndex());
				
				if(formElement1.get(FORM_VALUE)==null){
					if(column1.getType().equalsIgnoreCase("string") && EMPTY_STR.equalsIgnoreCase((String)formElement1.get(FORM_VALUE)))
						sw.createCell(columnNum++,EMPTY_STR,styles.get(FORM_HEADER).getIndex());
				}
				else
				{
					String formStr = formElement1.get(FORM_VALUE).toString();
					
					short styleIndex = styles.get(FORM_VALUES + UNDERSCORE + column1.getCellStyleName()).getIndex();
					
					
					if (column1.getJavaClass().equals(DOUBLE)) {
						if(EMPTY_STR.equalsIgnoreCase(formStr))
							sw.createCell(columnNum++,formStr, styleIndex);
						else
							sw.createCell(columnNum++, new Double(formStr)/(column1.getType().equals("percent") && !column1.isMultiplyBy100()?100:1), styleIndex);							
					} else if (column1.getJavaClass().equals(LONG)) {
						if(EMPTY_STR.equalsIgnoreCase(formStr))
							sw.createCell(columnNum++,formStr, styleIndex);
						else
							sw.createCell(columnNum++, new Long(formStr)/(column1.getType().equals("percent") && !column1.isMultiplyBy100()?100:1) , styleIndex);								
					} else if (column1.getJavaClass().equals(INTEGER)) {
						if(EMPTY_STR.equalsIgnoreCase(formStr))
							sw.createCell(columnNum++,formStr, styleIndex);
						else
							sw.createCell(columnNum++, new Integer(formStr)/(column1.getType().equals("percent") && !column1.isMultiplyBy100()?100:1), styleIndex);
					} else if (column1.getJavaClass().equals(DATE)) {
						sw.createCell(columnNum++, (java.util.Date) formElement1.get(FORM_VALUE), styleIndex);
					//} else if (formStr.contains("&") || formStr.contains("<") || formStr.contains(">")) {
					//	sw.createCell(columnNum++,"<![CDATA[" + formStr + "]]>", styleIndex);
					} else {
						sw.createCell(columnNum++,formStr, styleIndex);
					}										
				}// end else
	
			}// end if
		}//end for loop
	}*/
	
	private void generateDataGrid(SpreadsheetWriter sw,  ExcelSheet sheet, int sheetIndex) throws Exception 
	{
		if (sheet.getColumns() == null || sheet.getColumns().size() == 0)
			return;
		//Create criteria/header text
		if(sheet.getHeaderText() != null && !EMPTY_STR.equalsIgnoreCase(sheet.getHeaderText())){
			sw.setRowHeight(2 * 16);			
			sw.insertRow(rownum++);
			//sw.createCell(colnum++, "<![CDATA[" + sheet.getHeaderText() + "]]>");	
			sw.createCell(colnum++, sheet.getHeaderText());			
			sw.endRow();
			headerTextList.add(new Integer(sheetIndex));
			colnum = 0;
		}
		
		// Create header row
		sw.setRowHeight(2 * 16);
		
		sw.insertRow(rownum++);
		List<Integer> gridSeperators = new ArrayList<Integer>();
		List<Integer> numColumnsArr = sheet.getNumColumns();
		int gridColumnNum = 0;
		int totalGridColumns = 0;
		int currentGrid = 0;
		boolean hasGroupedColumn = false;	
		
		for (DataColumn column : sheet.getColumns())
		{
			if (currentGrid < numColumnsArr.size())
				totalGridColumns = numColumnsArr.get(currentGrid);
			
			if(column.isGroupingReq()) {
				sw.createCell(colnum++, column.getMonthLbl(), styles.get(HEADER_CENTER).getIndex());
				hasGroupedColumn = true;
			}
			else{
				//if (column.getHeaderLabel() != null && (column.getHeaderLabel().contains("&") || column.getHeaderLabel().contains("<") || column.getHeaderLabel().contains(">"))) {
				//	sw.createCell(colnum++,"<![CDATA[" + column.getHeaderLabel() + "]]>", styles.get(HEADER_CENTER).getIndex());
				//} else {
				sw.createCell(colnum++,column.getHeaderLabel(), styles.get(HEADER_CENTER).getIndex());
				//}
			}
			
			gridColumnNum++;
			if (gridColumnNum >= totalGridColumns)
			{
				gridSeperators.add(colnum);
				sw.createCell(colnum++, EMPTY_STR);
				gridColumnNum = 0;
				currentGrid++;
			}
		}
		
		if (gridSeperators.size() == 0)
		{
			System.out.println("gridSeperators was empty! " + colnum);
			gridSeperators.add(colnum);
		}
		
		sw.endRow();
		
		currentGrid = 0;
		int nextSeperator = gridSeperators.get(currentGrid);
		
		// create grouped columns (if defined)
		if (hasGroupedColumn)
		{
			sw.setRowHeight(2 * 35);			
			sw.insertRow(rownum++);
			colnum = 0;
			for (DataColumn column : sheet.getColumns())
			{
				for(int cnt= 0; cnt < column.getGroupingLevel(); cnt++){
					if(column.isGroupingReq()) 
					{
						//if (column.getHeaderLabel() != null && (column.getHeaderLabel().contains("&") || column.getHeaderLabel().contains("<") || column.getHeaderLabel().contains(">"))) {
						//	sw.createCell(colnum++,"<![CDATA[" + column.getHeaderLabel() + "]]>", styles.get("HEADER_ROTATE_CENTER").getIndex());
						//} else {
						sw.createCell(colnum++,column.getHeaderLabel(), styles.get("HEADER_ROTATE_CENTER").getIndex());
						//}					
					}
					else
						sw.createCell(colnum++, EMPTY_STR, styles.get(HEADER_CENTER).getIndex());
				}
				if (colnum == nextSeperator && gridSeperators.size() < currentGrid)
				{
					sw.createCell(colnum++, EMPTY_STR);
					currentGrid++;
					nextSeperator = gridSeperators.get(currentGrid);
				}
					
			}
			sw.endRow();
		}	

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> dataProvider = (List<Map<String,Object>>) sheet.getData();
		List<Integer> numRowsArr = sheet.getNumRows();
		int gridRowNum = 0;

		// create data rows/cells and style them according to their DataColumn
		for (Map<String,Object> data : dataProvider)
		{
			if(data == null)
				continue;
			
			//if (rownum % 1000 == 0)
			//	System.out.println(timeSince() + " - processed " + rownum + " rows");
			
			boolean isBoldRow = (data.get("fontWeight") != null && data.get("fontWeight").toString().equals("bold"));
			sw.setRowHeight(15);			
			sw.insertRow(rownum++);
			colnum = 0;
			currentGrid = 0;
			nextSeperator = gridSeperators.get(currentGrid);
			int totalGridRows = numRowsArr.get(currentGrid);
			
			for (DataColumn column : sheet.getColumns())
			{
				boolean isBoldColumn = (column.getFontWeight() != null && column.getFontWeight().equals("bold"));
				short styleIndex = styles.get(column.getCellStyleName()  + (isBoldRow||isBoldColumn?"Bold":"") + UNDERSCORE + (rownum%2)).getIndex();
				
				Object dataVal = null;
				if (column.getDataField() != null)
					dataVal = data.get(column.getDataField());
					

				try {
					if (column.getDataField() == null || gridRowNum > totalGridRows)
						sw.createCell(colnum++,EMPTY_STR);
					else if (dataVal == null)
						sw.createCell(colnum++,EMPTY_STR, styleIndex);
					/*
					else if("customer_id".equalsIgnoreCase(column.getDataField())||"contract_id".equalsIgnoreCase(column.getDataField())||"css_product_id".equalsIgnoreCase(column.getDataField()))
					{
						if(EMPTY_STR.equalsIgnoreCase(dataStr))
							sw.createCell(colnum++,dataStr, styleIndex);
						else
							sw.createCell(colnum++, new Double(dataStr) / (column.getType().equals("percent") && !column.isMultiplyBy100()?100:1), styleIndex);							
					}
					*/
					else if (handleItemRenderer(sw, column, dataVal, styleIndex))
						; // do nothing if itemRenderer was found
					else if (dataVal instanceof String) {
						//System.out.println(column.getDataField() + ": " + dataVal + " (String)");
						String dataStr = (String) dataVal;
						//if (dataStr.contains("&") || dataStr.contains("<") || dataStr.contains(">")) {
						//	sw.createCell(colnum++,"<![CDATA[" + dataStr + "]]>", styleIndex);
						//}else 
						if(dataStr.length() == 0 || dataStr.codePointAt(0) == 0)
						{
							sw.createCell(colnum++,EMPTY_STR, styleIndex);
						}
						else{
							sw.createCell(colnum++,dataStr, styleIndex);
						}
					} else if (dataVal instanceof Double) {
						//System.out.println(column.getDataField() + ": " + dataVal + " (Double)");
						Double dataNum = (Double) dataVal;
						if (dataNum.isNaN() || dataNum.isInfinite())
							sw.createCell(colnum++, "", styleIndex);							
						else
							sw.createCell(colnum++, dataNum / (column.getType().equals("percent") && !column.isMultiplyBy100()?100:1), styleIndex);
					} else if (dataVal instanceof Long) {
						//System.out.println(column.getDataField() + ": " + dataVal + " (Long)");
						Long dataNum = (Long) dataVal;
						sw.createCell(colnum++, (double) dataNum / (column.getType().equals("percent") && !column.isMultiplyBy100()?100:1), styleIndex);								
					} else if (dataVal instanceof Integer) {
						//System.out.println(column.getDataField() + ": " + dataVal + " (Integer)");
						Integer dataNum = (Integer) dataVal;
						sw.createCell(colnum++, (double) dataNum / (column.getType().equals("percent") && !column.isMultiplyBy100()?100:1), styleIndex);
					} else if (dataVal instanceof java.util.Date) {
						//System.out.println(column.getDataField() + ": " + dataVal + " (Date)");
						sw.createCell(colnum++, (java.util.Date) dataVal, styleIndex);				
					} else if (dataVal instanceof Calendar) {
						//System.out.println(column.getDataField() + ": " + dataVal + " (Calendar)");
						sw.createCell(colnum++, (Calendar) dataVal, styleIndex);				
					} else {
						System.out.println("WARNING - " + dataVal + " not handled in ExcelGenerator!");
						sw.createCell(colnum++,dataVal.toString(), styleIndex);
					}
				} catch (java.lang.ClassCastException e) {
					System.err.println(column.getDataField() + " " + column.getJavaClass() + " " + column.getType() + ": " + dataVal + " - " + e.getMessage());
					//throw e;
				} catch (java.lang.NumberFormatException e) {
					System.err.println(column.getDataField() + " " + column.getJavaClass() + " " + column.getType() + ": " + dataVal + " - " + e.getMessage());
					//throw e;
				}
				
				// If at the end of a data grid, add an extra column
				if (colnum >= nextSeperator)
				{
					sw.createCell(colnum++, EMPTY_STR);
					currentGrid++;
					if (currentGrid < gridSeperators.size()) {
						nextSeperator = gridSeperators.get(currentGrid);
						totalGridRows = numRowsArr.get(currentGrid);
					}
				}
			}

			gridRowNum++;
			sw.endRow();
		}
		
		//if (rownum > 10000 || (sheet.getData().size() * sheet.getColumns().size()) > 300000)
			//shouldBeAutoSize = false;
		//if (rownum > 10000 || (sheet.getData().size() * sheet.getColumns().size()) > 600000)
			//shouldBeMerge = false;
	}


	/**
	 * @param sw
	 * @param column
	 * @param dataStr
	 * @param styleIndex
	 * @return true if column should continue to be processed
	 * @throws Exception
	 */
	private boolean handleItemRenderer(SpreadsheetWriter sw, DataColumn column, Object dataObj, short styleIndex) throws Exception 
	{
		if (column.getItemRenderer() == null || column.getItemRenderer().length() == 0)
			return false;
		
		if (column.getItemRenderer().contains("MC2ReportItemRenderer") && dataObj instanceof String) 
		{
			if (GREEN.equalsIgnoreCase((String) dataObj)){
				styleIndex = styles.get(column.getCellStyleName() + UNDERSCORE + + (rownum%2) + LETTER_G).getIndex();
				sw.createCell(colnum++,LETTER_H, styleIndex);									
			} else if (RED.equalsIgnoreCase((String) dataObj)){
				styleIndex = styles.get(column.getCellStyleName() + UNDERSCORE + + (rownum%2) + LETTER_R).getIndex();
				sw.createCell(colnum++,LETTER_I, styleIndex);									
			} else if (ORANGE.equalsIgnoreCase((String) dataObj)){
				styleIndex = styles.get(column.getCellStyleName() + UNDERSCORE + + (rownum%2) + LETTER_O).getIndex();
				sw.createCell(colnum++,LETTER_M, styleIndex);									
			} else if (YELLOW.equalsIgnoreCase((String) dataObj)){
				styleIndex = styles.get(column.getCellStyleName() + UNDERSCORE + + (rownum%2) + LETTER_Y).getIndex();
				sw.createCell(colnum++,LETTER_K, styleIndex);
			} // endif MC2ReportItemRenderer

			return true;
		}

		return false;
	}	

	private void createStyles(XSSFWorkbook wb, ExcelWorkbook data) 
	{
		XSSFDataFormat df = wb.createDataFormat();

		XSSFFont headerFont = wb.createFont();
		headerFont.setFontHeightInPoints((short) 9);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		headerFont.setBold(true);
		headerFont.setFontName(VERDANA);
		
		XSSFFont boldFont = wb.createFont();
		boldFont.setFontHeightInPoints((short) 11);
		boldFont.setColor(IndexedColors.BLACK.getIndex());
		boldFont.setBold(true);

		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setWrapText(true);
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFillForegroundColor(DARK_GREY);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		styles.put(HEADER_CENTER, headerStyle);
		
		XSSFCellStyle headerRotateStyle = wb.createCellStyle();
		headerRotateStyle.setFont(headerFont);
		headerRotateStyle.setRotation((short)90);
		headerRotateStyle.setAlignment(HorizontalAlignment.CENTER);
		headerRotateStyle.setWrapText(true);
		headerRotateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerRotateStyle.setFillForegroundColor(DARK_GREY);
		headerRotateStyle.setBorderTop(BorderStyle.THIN);
		headerRotateStyle.setBorderRight(BorderStyle.THIN);
		headerRotateStyle.setBorderBottom(BorderStyle.THIN);
		headerRotateStyle.setBorderLeft(BorderStyle.THIN);
		styles.put("HEADER_ROTATE_CENTER", headerRotateStyle);
		
		for (ExcelSheet sheet : data.getSheets()) 
		{
			List<DataColumn> columns = sheet.getColumns();
			if (columns == null)
				continue;
			
			if(TYPE_FORM.equalsIgnoreCase(sheet.getType())){
				
				XSSFFont formHeaderFont = wb.createFont();
				formHeaderFont.setFontHeightInPoints((short) 11);
				formHeaderFont.setColor(IndexedColors.BLACK.getIndex());
				formHeaderFont.setBold(true);
				
				XSSFCellStyle formHeaderStyle = wb.createCellStyle();
				formHeaderStyle.setFont(formHeaderFont);
				formHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
				formHeaderStyle.setWrapText(false);
				formHeaderStyle.setBorderTop(BorderStyle.THIN);
				formHeaderStyle.setBorderRight(BorderStyle.THIN);
				formHeaderStyle.setBorderBottom(BorderStyle.THIN);
				formHeaderStyle.setBorderLeft(BorderStyle.THIN);
				formHeaderStyle.setBorderColor(BorderSide.TOP, DARK_GREY);
				formHeaderStyle.setBorderColor(BorderSide.RIGHT, DARK_GREY);
				formHeaderStyle.setBorderColor(BorderSide.BOTTOM, DARK_GREY);
				formHeaderStyle.setBorderColor(BorderSide.LEFT, DARK_GREY);				
				styles.put(FORM_HEADER, formHeaderStyle);				
				
				XSSFCellStyle formValStyle = null;
				
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> formData = (List<Map<String,Object>>) sheet.getData();
				
				for(Map<String,Object> formElement : formData)
				{
					DataColumn column = (DataColumn)formElement.get("column");
					if (styles.containsKey(FORM_VALUES + UNDERSCORE + column.getCellStyleName()))
						continue;
					
					formValStyle = wb.createCellStyle();
					formValStyle.setDataFormat(column.getDataFormat(df));
					formValStyle.setAlignment(HorizontalAlignment.LEFT);
					formValStyle.setWrapText(false);
					formValStyle.setBorderTop(BorderStyle.THIN);
					formValStyle.setBorderRight(BorderStyle.THIN);
					formValStyle.setBorderBottom(BorderStyle.THIN);
					formValStyle.setBorderLeft(BorderStyle.THIN);
					formValStyle.setBorderColor(BorderSide.TOP, DARK_GREY);
					formValStyle.setBorderColor(BorderSide.RIGHT, DARK_GREY);
					formValStyle.setBorderColor(BorderSide.BOTTOM, DARK_GREY);
					formValStyle.setBorderColor(BorderSide.LEFT, DARK_GREY);
					styles.put(FORM_VALUES + UNDERSCORE + column.getCellStyleName(), formValStyle);					
				}				
				
			}
			
			for (DataColumn col : columns) {
				if (styles.containsKey(col.getCellStyleName() + UNDERSCORE + ZERO_STR)) {
					continue;
				}
				XSSFCellStyle cellStyle0 = wb.createCellStyle();
				col.setCellStyle(cellStyle0, df);
				cellStyle0.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyle0.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				cellStyle0.setBorderTop(BorderStyle.THIN);
				cellStyle0.setBorderRight(BorderStyle.THIN);
				cellStyle0.setBorderBottom(BorderStyle.THIN);
				cellStyle0.setBorderLeft(BorderStyle.THIN);
				cellStyle0.setBorderColor(BorderSide.TOP, GREY);
				cellStyle0.setBorderColor(BorderSide.RIGHT, GREY);
				cellStyle0.setBorderColor(BorderSide.BOTTOM, GREY);
				cellStyle0.setBorderColor(BorderSide.LEFT, GREY);
				styles.put(col.getCellStyleName() + UNDERSCORE + ZERO_STR, cellStyle0);
				
				XSSFCellStyle cellStyle1 = wb.createCellStyle();
				col.setCellStyle(cellStyle1, df);	
				cellStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyle1.setFillForegroundColor(LIGHT_GREY);
				cellStyle1.setBorderTop(BorderStyle.THIN);
				cellStyle1.setBorderRight(BorderStyle.THIN);
				cellStyle1.setBorderBottom(BorderStyle.THIN);
				cellStyle1.setBorderLeft(BorderStyle.THIN);
				cellStyle1.setBorderColor(BorderSide.TOP, GREY);
				cellStyle1.setBorderColor(BorderSide.RIGHT, GREY);
				cellStyle1.setBorderColor(BorderSide.BOTTOM, GREY);
				cellStyle1.setBorderColor(BorderSide.LEFT, GREY);
				styles.put(col.getCellStyleName() + UNDERSCORE + ONE_STR, cellStyle1);
				
				XSSFCellStyle cellStyleBold0 = wb.createCellStyle();
				col.setCellStyle(cellStyleBold0, df);
				cellStyleBold0.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyleBold0.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				cellStyleBold0.setBorderTop(BorderStyle.THIN);
				cellStyleBold0.setBorderRight(BorderStyle.THIN);
				cellStyleBold0.setBorderBottom(BorderStyle.THIN);
				cellStyleBold0.setBorderLeft(BorderStyle.THIN);
				cellStyleBold0.setBorderColor(BorderSide.TOP, GREY);
				cellStyleBold0.setBorderColor(BorderSide.RIGHT, GREY);
				cellStyleBold0.setBorderColor(BorderSide.BOTTOM, GREY);
				cellStyleBold0.setBorderColor(BorderSide.LEFT, GREY);
				cellStyleBold0.setFont(boldFont);
				styles.put(col.getCellStyleName() + "Bold" + UNDERSCORE + ZERO_STR, cellStyleBold0);
				
				XSSFCellStyle cellStyleBold1 = wb.createCellStyle();
				col.setCellStyle(cellStyleBold1, df);
				cellStyleBold1.setFont(boldFont);
				cellStyleBold1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyleBold1.setFillForegroundColor(LIGHT_GREY);
				cellStyleBold1.setBorderTop(BorderStyle.THIN);
				cellStyleBold1.setBorderRight(BorderStyle.THIN);
				cellStyleBold1.setBorderBottom(BorderStyle.THIN);
				cellStyleBold1.setBorderLeft(BorderStyle.THIN);
				cellStyleBold1.setBorderColor(BorderSide.TOP, GREY);
				cellStyleBold1.setBorderColor(BorderSide.RIGHT, GREY);
				cellStyleBold1.setBorderColor(BorderSide.BOTTOM, GREY);
				cellStyleBold1.setBorderColor(BorderSide.LEFT, GREY);
				styles.put(col.getCellStyleName() + "Bold" + UNDERSCORE + ONE_STR, cellStyleBold1);
				
				if (col.getItemRenderer() != null && col.getItemRenderer().contains("MC2ReportItemRenderer"))
				{					
					XSSFFont trendFont = wb.createFont();
					trendFont.setBold(true);
					trendFont.setColor(IndexedColors.GREEN.getIndex());
					trendFont.setFontName(WINGDINGS_3);				
					
					XSSFCellStyle cellStyleTrend = wb.createCellStyle();
					col.setCellStyle(cellStyleTrend, df);
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ZERO_STR + LETTER_G, cellStyleTrend);
					
					cellStyleTrend = wb.createCellStyle();
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(LIGHT_GREY);
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ONE_STR + LETTER_G, cellStyleTrend);
					
					trendFont = wb.createFont();
					trendFont.setBold(true);
					trendFont.setColor(IndexedColors.DARK_YELLOW.getIndex());
					trendFont.setFontName(WINGDINGS_3);				
					
					cellStyleTrend = wb.createCellStyle();
					col.setCellStyle(cellStyleTrend, df);
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ZERO_STR + LETTER_Y, cellStyleTrend);
					
					cellStyleTrend = wb.createCellStyle();
					col.setCellStyle(cellStyleTrend, df);
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(LIGHT_GREY);
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ONE_STR + LETTER_Y, cellStyleTrend);
					
					trendFont = wb.createFont();
					trendFont.setBold(true);
					trendFont.setColor(IndexedColors.ORANGE.getIndex());
					trendFont.setFontName(WINGDINGS_3);				
					
					
					cellStyleTrend = wb.createCellStyle();
					col.setCellStyle(cellStyleTrend, df);
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ZERO_STR + LETTER_O, cellStyleTrend);
					
					cellStyleTrend = wb.createCellStyle();
					col.setCellStyle(cellStyleTrend, df);
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(LIGHT_GREY);
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ONE_STR + LETTER_O, cellStyleTrend);
					
					trendFont = wb.createFont();
					trendFont.setBold(true);
					trendFont.setColor(IndexedColors.RED.getIndex());
					trendFont.setFontName(WINGDINGS_3);				
					
					cellStyleTrend = wb.createCellStyle();
					col.setCellStyle(cellStyleTrend, df);
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ZERO_STR + LETTER_R, cellStyleTrend);
					
					cellStyleTrend = wb.createCellStyle();
					col.setCellStyle(cellStyleTrend, df);
					cellStyleTrend.setFont(trendFont);
					cellStyleTrend.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					col.setCellStyle(cellStyleTrend, df);	
					cellStyleTrend.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellStyleTrend.setFillForegroundColor(LIGHT_GREY);
					cellStyleTrend.setBorderTop(BorderStyle.THIN);
					cellStyleTrend.setBorderRight(BorderStyle.THIN);
					cellStyleTrend.setBorderBottom(BorderStyle.THIN);
					cellStyleTrend.setBorderLeft(BorderStyle.THIN);
					cellStyleTrend.setBorderColor(BorderSide.TOP, GREY);
					cellStyleTrend.setBorderColor(BorderSide.RIGHT, GREY);
					cellStyleTrend.setBorderColor(BorderSide.BOTTOM, GREY);
					cellStyleTrend.setBorderColor(BorderSide.LEFT, GREY);
					styles.put(col.getCellStyleName() + UNDERSCORE + ONE_STR + LETTER_R, cellStyleTrend);
				}
				
			}

		}
	}
	
	/**
	 *
	 * @param zipfile the template file
	 * @param outFile the final excel file
	 */
	@SuppressWarnings("unchecked")
	private  void substituteMultiSheet(File zipfile, File outFile) throws IOException {
		FileOutputStream out = new FileOutputStream(outFile);
		
		ZipFile zip = new ZipFile(zipfile);

		ZipOutputStream zos = new ZipOutputStream(out);
		
		Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
		while (en.hasMoreElements()) {
			ZipEntry ze = en.nextElement();
			Pattern p = Pattern.compile("worksheet");
			Matcher matcher = p.matcher(ze.getName());
			if(!matcher.find()){
				zos.putNextEntry(new ZipEntry(ze.getName()));
				InputStream is = zip.getInputStream(ze);
				copyStream(is, zos);
				is.close();
			}
		}
		en = (Enumeration<ZipEntry>) zip.entries();
		
		int i = 0;
		while (en.hasMoreElements()) {
			ZipEntry ze = en.nextElement();
			Pattern p = Pattern.compile("worksheet");
			Matcher matcher = p.matcher(ze.getName());           
			if(matcher.find()){
				//System.out.println("substituteMultiSheet2 : " + ze.getName());
				zos.putNextEntry(new ZipEntry(ze.getName()));
				InputStream is = new FileInputStream(sheetFiles.get(i));
				copyStream(is, zos);
				is.close();
				i++;
			}
		}		
		zos.close();
		out.close();
	}

	/**
	 *
	 * @param zipfile the template file
	 * @param tmpSheetfile the XML file with the sheet data
	 * @param entry the name of the sheet entry to substitute, e.g. xl/worksheets/sheet1.xml
	 * @param out the stream to write the result to
	 */
	@SuppressWarnings("unused")
	private void substitute(File zipfile, File tmpSheetfile, String entry, OutputStream out) throws IOException {
		ZipFile zip = new ZipFile(zipfile);

		ZipOutputStream zos = new ZipOutputStream(out);

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
		while (en.hasMoreElements()) {
			ZipEntry ze = en.nextElement();
			if(!ze.getName().equals(entry)){
				zos.putNextEntry(new ZipEntry(ze.getName()));
				InputStream is = zip.getInputStream(ze);
				copyStream(is, zos);
				is.close();
			}
		}
		zos.putNextEntry(new ZipEntry(entry));
		InputStream is = new FileInputStream(tmpSheetfile);
		copyStream(is, zos);
		is.close();

		zos.close();
	}

	private void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] chunk = new byte[1024];
		int count;
		while ((count = in.read(chunk)) >=0 ) {
			out.write(chunk,0,count);
		}
	}

	private double timeSince() {
		return ((double) (System.currentTimeMillis() - startTime))/1000;
	}
}
