package com.amdocs.dashboard.customDataGrid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;

public class DataColumn  implements Externalizable {
    private static final String NUMBER = "number";    
    private static final String CURRENCY = "currency";
    private static final String PERCENT = "percent";
    //private static final String DATE = "date";
    //private static final String DATETIME = "datetime";
    //private static final String TIME = "time";
    
	private String id;
	private String dataField;
	private String headerLabel;
	private String monthlyHeaderLabel;
	private String monthLbl;
	private boolean groupingReq;
	private String type;
	private int integerDigits;
	private int fractionalDigits;
	private String formatString;
	private String textAlign;
	private String dateTimePattern;
	private String itemRenderer;
	private boolean useGrouping;
	private boolean multiplyBy100;
	private int groupingLevel = 1;
	private boolean isVisible;
	private String fontWeight;
	
	private int width;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDataField() {
		return dataField;
	}
	public void setDataField(String dataField) {
		this.dataField = dataField;
	}
	public String getHeaderLabel() {
		return headerLabel;
	}
	public void setHeaderLabel(String headerLabel) {
		this.headerLabel = headerLabel;
	}
	public String getMonthlyHeaderLabel() {
		return monthlyHeaderLabel;
	}
	public void setMonthlyHeaderLabel(String monthlyHeaderLabel) {
		this.monthlyHeaderLabel = monthlyHeaderLabel;
	}
	public String getMonthLbl() {
		return monthLbl;
	}
	public void setMonthLbl(String monthLbl) {
		this.monthLbl = monthLbl;
	}
	public boolean isGroupingReq() {
		return groupingReq;
	}
	public void setGroupingReq(boolean groupingReq) {
		this.groupingReq = groupingReq;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getIntegerDigits() {
		return integerDigits;
	}
	public void setIntegerDigits(int integerDigits) {
		this.integerDigits = integerDigits;
	}
	public int getFractionalDigits() {
		return fractionalDigits;
	}
	public void setFractionalDigits(int fractionalDigits) {
		this.fractionalDigits = fractionalDigits;
	}
	public String getFormatString() {
		return formatString;
	}
	public void setFormatString(String formatString) {
		this.formatString = formatString;
	}
	public String getTextAlign() {
		return textAlign;
	}
	public void setTextAlign(String textAlign) {
		this.textAlign = textAlign;
	}
	public String getDateTimePattern() {
		return dateTimePattern;
	}
	public void setDateTimePattern(String dateTimePattern) {
		this.dateTimePattern = dateTimePattern;
	}
	public String getItemRenderer() {
		return itemRenderer;
	}
	public void setItemRenderer(String itemRenderer) {
		this.itemRenderer = itemRenderer;
	}
	public boolean isUseGrouping() {
		return useGrouping;
	}
	public void setUseGrouping(boolean useGrouping) {
		this.useGrouping = useGrouping;
	}
	
	public void setMultiplyBy100(boolean multiplyBy100) {
		this.multiplyBy100 = multiplyBy100;
	}
	public boolean isMultiplyBy100() {
		return multiplyBy100;
	}
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException 
	{		
		id = (String) input.readObject();
		dataField = (String) input.readObject();
		headerLabel = (String) input.readObject();
		monthlyHeaderLabel = (String) input.readObject();
		monthLbl = (String) input.readObject();
		groupingReq = input.readBoolean();
		type = (String) input.readObject();
		fractionalDigits = input.readInt();
		formatString = (String) input.readObject();
		textAlign = (String) input.readObject();
		dateTimePattern = (String) input.readObject();
		itemRenderer = (String) input.readObject();
		useGrouping = input.readBoolean();
		multiplyBy100 = input.readBoolean();
		isVisible = input.readBoolean();
		fontWeight = (String) input.readObject();
		width = input.readInt();
	}
	
	public void writeExternal(ObjectOutput output) throws IOException 
	{
		output.writeObject(id);
		output.writeObject(dataField);
		output.writeObject(headerLabel);
		output.writeObject(monthlyHeaderLabel);
		output.writeObject(monthLbl);
		output.writeBoolean(groupingReq);
		output.writeObject(type);
		output.writeInt(fractionalDigits);
		output.writeObject(formatString);
		output.writeObject(textAlign);
		output.writeObject(dateTimePattern);
		output.writeObject(itemRenderer);
		output.writeBoolean(useGrouping);
		output.writeBoolean(multiplyBy100);
		output.writeBoolean(isVisible);
		output.writeObject(fontWeight);
		output.writeInt(width);
	}
	

	
	public void setDefaults() {
		if (getTextAlign() != null) {
			if (type.equals("date"))
				setTextAlign("center");
			else if (isNumberField())
				setTextAlign("right"); 
		}
		
		if (getDateTimePattern() == null)
			if (type.equals("date"))
				setDateTimePattern("MM/dd/yyyy");
			else if (type.equals("time"))
				setDateTimePattern("hh:mm:ss AM Z");
			else if (type.equals("datetime"))
				setDateTimePattern("MM/dd/yyyy HH:mm:ss zzz");
		
	}

	//public CAWFormatter getFormatter() {
	//	if (formatter == null) formatter = new CAWFormatter(this);
	//	return formatter;
	//}

	
	public String getCellStyleName() {
		StringBuffer sb = new StringBuffer();
		
		if (this.getType() != null && this.getType().length() > 0)
			sb.append(this.getType().toUpperCase());
		else
			sb.append("STRING");
		
		if (this.isNumberField())
			sb.append("_" + this.getIntegerDigits() + "_" + this.getFractionalDigits());
		else if ((this.getType().equals("datetime") 
				|| this.getType().equals("date") 
				|| this.getType().equals("time")) 
				&& this.getDateTimePattern() != null)
			sb.append("_" + this.getDateTimePattern());
		
		if (this.getTextAlign() != null)
			sb.append("_ALIGN_" + this.getTextAlign().toUpperCase());
		
		if (this.isNumberField() && !this.isUseGrouping())
			sb.append("_NO_GROUPING");
		
		if (this.getItemRenderer() != null)
			sb.append("_" + this.getItemRenderer());
			
		return sb.toString();
	}
	
	private String getNumberFormat() {
		NumberFormat formatter =  getNumberFormatter();
		
		String numberFormat = "0";
		if(PERCENT.equalsIgnoreCase(this.type)){
			formatter.setMinimumFractionDigits(2);
			formatter.setMaximumFractionDigits(2);
		}
		numberFormat = repeatChar('0', formatter.getMinimumIntegerDigits());
		
		if (formatter.isGroupingUsed()) {
			if (numberFormat.length() < 4)
				numberFormat = repeatChar('#', (4 - numberFormat.length())) + numberFormat;
			
			// add "," if groupings are to be used
			numberFormat = numberFormat.replaceAll("(?=\\B(?:\\S\\S\\S)++$)", ",");
		}
		
		if (formatter.getMinimumFractionDigits() > 0)
			numberFormat = numberFormat + "." + repeatChar('0', formatter.getMinimumFractionDigits()) + repeatChar('#', (formatter.getMaximumFractionDigits() - formatter.getMinimumFractionDigits()));			
		
		//logger.debug(attribute.getType() + " " + numberFormat);
		
		return numberFormat;
	}
	
	private NumberFormat getNumberFormatter() {
        // Determine formatting locale
        Locale loc = Locale.getDefault();
        
        return getNumberFormatter(loc);
	}
	private NumberFormat getNumberFormatter(Locale loc) {
		NumberFormat formatter = null;
    	//if ((this.getPattern() != null) && !this.getPattern().equals("")) {
    	//	// if 'attribute.getPattern()' is specified, 'type' is ignored
    	//	DecimalFormatSymbols symbols = new DecimalFormatSymbols(loc);
    	//	formatter = new DecimalFormat(this.getPattern(), symbols);
    	//} else {
    		formatter = createNumberFormatter(loc);
    	//}
    	//if (((this.getPattern() != null) && !this.getPattern().equals("")) || CURRENCY.equalsIgnoreCase(this.getType())) {
    	//	try {
		//		setCurrency(formatter);
		//	} catch (Exception e) {
		//		logger.error(e.getMessage() + " " + attribute.getType());
		//	}
    	//}
    	configureFormatter(formatter);
    	
    	return formatter;
	}
	
	private String repeatChar(char c, int count) {
		if (count < 0)
			return "";
		
		char[] s = new char[count];
		
		for (int i = 0; i < count; i++)
			s[i] = c;
		
		return new String(s).intern();
	}
	
    private NumberFormat createNumberFormatter(Locale loc) {
    	NumberFormat formatter = null;
	
		if (this.getType() == null || NUMBER.equalsIgnoreCase(this.getType())) {
			formatter = NumberFormat.getNumberInstance(loc);
		} else if (CURRENCY.equalsIgnoreCase(this.getType())) {
			formatter = NumberFormat.getCurrencyInstance(loc);
		} else if (PERCENT.equalsIgnoreCase(this.getType())) {
			formatter = NumberFormat.getPercentInstance(loc);
		} else {
			System.err.println("Invalid number formatter. (" + this.getType() + ")");
		}
		
		return formatter;
    }

    
    /*
     * Applies the 'groupingUsed', 'maxIntegerDigits', 'attribute.getMinIntegerDigits()',
     * 'attribute.getMaxFractionDigits()', and 'attribute.getMinFractionDigits()' attributes to the given
     * formatter.
     */
    private void configureFormatter(NumberFormat formatter) {
    	if (!this.isUseGrouping())
    		formatter.setGroupingUsed(this.isUseGrouping());
    	//if (this.isMaxIntegerDigitsSpecified())
    	//	formatter.setMaximumIntegerDigits(this.getMaxIntegerDigits());
    	//if (this.isMinIntegerDigitsSpecified())
    	//	formatter.setMinimumIntegerDigits(this.getMinIntegerDigits());
    	//if (this.isMaxFractionDigitsSpecified())
    	//	formatter.setMaximumFractionDigits(this.getMaxFractionDigits());
    	//if (this.isMaxFractionDigitsSpecified())
    	//	formatter.setMinimumFractionDigits(this.getMinFractionDigits());
    }
	
	private String getExcelFormat() {
		StringBuffer sb = new StringBuffer();
		
		if (this.getType().equals("currency"))
			return "$ " + getNumberFormat() + "_);[Red]($ " + getNumberFormat() + ")";
		if (this.getType().equals("percent"))
			return getNumberFormat() + "%";
		if (this.isNumberField())
			return getNumberFormat();
		
		if (this.getType().equals("date")) {
			if (this.getDateTimePattern() != null)
				return this.getDateTimePattern().toLowerCase();

			return "mm/dd/yyyy";
		}
		
		if (this.getType().equals("datetime")) {
			String dateStyle = "mm/dd/yyyy";
			String timeStyle = "h:mm AM/PM";
			if (this.getDateTimePattern() != null)
				return this.getDateTimePattern().toLowerCase();
			
			return dateStyle + " " + timeStyle;
		}
		
		return sb.toString();
	}

	public short getDataFormat(XSSFDataFormat formatter) {
		String excelFormat = getExcelFormat();
		//System.out.println("DataColumn.getDataFormat " + excelFormat);
		if (excelFormat != null && excelFormat.length() > 0)
			return formatter.getFormat(excelFormat);
		
		return 0;
	}	
	
	/**
	 * TODO 
	 * @param wb Worbook style will be created in.
	 * @return new CellStyle
	 */
	public XSSFCellStyle setCellStyle(XSSFCellStyle cellStyle, XSSFDataFormat df) {
		cellStyle.setDataFormat(this.getDataFormat(df));
		
		//if (attribute.isWhiteSpaceSpecified() && attribute.getWhiteSpace().equals("nowrap"))
		//	cellStyle.setWrapText(false);
		
		//else if ((attribute.isWhiteSpaceSpecified() && !attribute.getWhiteSpace().equals("nowrap")) || attribute.isEllipseSpecified())
		//	cellStyle.setWrapText(true);
		
		if (this.getTextAlign() != null)
		{
			if (this.getTextAlign().equals("center"))
				cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			else if (this.getTextAlign().equals("right"))
				cellStyle.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			else if (this.getTextAlign().equals("left"))
				cellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			else if (this.getTextAlign().equals("justify"))
				cellStyle.setAlignment(XSSFCellStyle.ALIGN_JUSTIFY);
		}
		else if (this.getType().equals("date"))
			cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					
		//if (attribute.isVerticalAlignSpecified())
		//	if (attribute.getVerticalAlign().equals("top"))
		//		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		//	else if (attribute.getVerticalAlign().equals("middle"))
		//		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		//	else if (attribute.getVerticalAlign().equals("bottom"))
		//		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
		
		return cellStyle;
	}
	
	public boolean isIntegerField() {
		return this.type.equalsIgnoreCase("number") && getFractionalDigits() == 0;
	}
	
	public boolean isFractionField() {
		return (this.type.equalsIgnoreCase("number") && getFractionalDigits() > 0) 
		     || this.type.equalsIgnoreCase("currency") 
		     || this.type.equalsIgnoreCase("percent");
	}
	
	public boolean isNumberField() {
		return this.type.equalsIgnoreCase("number")
		    || this.type.equalsIgnoreCase("currency") 
		    || this.type.equalsIgnoreCase("percent");
	}
	
	public String getJavaClass() {
		if (this.type != null
				&& (this.type.equals("number") 
						|| this.type.equals("currency") 
						|| this.type.equals("percent") 
						|| this.type.equals("date"))) {
			//System.out.println(getDataField() + " " + getType() + " " + isIntegerField() + " " + isFractionField());
			if (isIntegerField()) {
				//if (this.getMaxIntegerDigits() >  9)
					return "java.lang.Long";
				//else
				//	return "java.lang.Integer";

			} else if (this.isFractionField()) {
				return "java.lang.Double";
			} else if (this.type.equalsIgnoreCase("date")) {
				return "java.util.Date";
			} else if (this.type.equalsIgnoreCase("datetime")) {
				return "java.sql.TimeStamp";
			}
		}

		return "java.lang.String";
	}
	/**
	 * @param groupingLevel the groupingLevel to set
	 */
	public void setGroupingLevel(int groupingLevel) {
		this.groupingLevel = groupingLevel;
	}
	/**
	 * @return the groupingLevel
	 */
	public int getGroupingLevel() {
		return groupingLevel;
	}
	/**
	 * @param isVisible the isVisible to set
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	/**
	 * @return the isVisible
	 */
	public boolean isVisible() {
		return isVisible;
	}
	/**
	 * @param fontWeight the fontWeight to set
	 */
	public void setFontWeight(String fontWeight) {
		this.fontWeight = fontWeight;
	}
	/**
	 * @return the fontWeight
	 */
	public String getFontWeight() {
		return fontWeight;
	}
	/**
	 * @param excelColWidth the excelColWidth to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * @return the excelColWidth
	 */
	public int getWidth() {
		return width;
	}
	
}
