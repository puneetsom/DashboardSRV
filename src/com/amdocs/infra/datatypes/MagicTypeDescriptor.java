/**
 * MagicType - Copyright (C) Amdocs 2009
 * 
 * This object works in conjunction with MagicTypeProxy (and optionally SQLDescriptor) 
 * to maintain overall description of particular SELECT statement for the purposes of
 * MagicType and MagicTypeProxy.
 * 
 * The idea is to have single MagicTypeDescriptor per SELECT Query (per QueryCode), even though
 * we will have a Lot of MagicType instances, each representing single Row from the SELECT, they
 * will all point to Single instance of MagicTypeDescriptor which describes columns in SELECT
 * 
 * @author AdiR
 *-----------------------------------------------------------------------------
 * Matt (mateo) - September 2009
 * Added handling for using MagicType's with XML files.
 *-----------------------------------------------------------------------------
 */

package com.amdocs.infra.datatypes;

import java.io.Reader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;

public class MagicTypeDescriptor {

	// This is the Object Alias that should match ActionScript alias
	protected String destClassName;
	// This is the Query Code (from SQLDescriptor) that this MagicTypeDescriptor is matching to
	protected String queryCode;

	// ALIGNED ARRAYS + ASSUMPTION: ALL PROPERTY NAMES FORCED TO LOWER CASE
	// This array also matches in order to propValues array in MagicType, since all columns always in same order
	protected ArrayList<String> propNames = null;
	protected int[] propSQLTypes = null; 

	// --------------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------------
	// CACHING Objects -- This one "remembers" queryCodes, to know which columns come back from which query
	// ------------------ Basically, it contains queryCode as a Hashmap Key, and a pointer to instance  
	// ------------------ of our very own matching MagicTypeDescriptor
	static protected HashMap<String,MagicTypeDescriptor> knownMagicTypes = 
							new HashMap<String,MagicTypeDescriptor>();
	
	// Private constructor, see static loadMetaDataForMagicType method, if you want an instance
	private MagicTypeDescriptor(String i_queryCode, String i_destClassName, ResultSet rs) throws SQLException {
		
		this.queryCode = i_queryCode;
		this.destClassName = i_destClassName;
		// Read the columns information from the result set
		try {
			// Read result set row to load into current props
			ResultSetMetaData meta = rs.getMetaData();
			
			int columns = meta.getColumnCount();
			
			// Capturing Column Names (FORCING TO LOWER CASE) and SQL Types 
			propNames = new ArrayList<String>(columns);
			propSQLTypes = new int[columns];
			
			for(int i=1;i<=columns;i++)
			{
				propNames.add(meta.getColumnLabel(i).toLowerCase());
				propSQLTypes[i-1]=meta.getColumnType(i);
			}
			
			// Now we know one more type! Magic!!!
			knownMagicTypes.put(queryCode, this);

		} catch (SQLException e) {
			System.out.println("FATAL: MagicTypeDescriptor failed to read ResultSet for "+queryCode);
			e.printStackTrace();
			throw e;
		}
	}

	//-------------------------------------------------------------------------
	// mateo - 2nd private constructor, for creating MagicType's from XML.
	private MagicTypeDescriptor(String i_xmlCode, String i_destClassName, Attributes atts) throws Exception
	{	
		this.queryCode = i_xmlCode;
		this.destClassName = i_destClassName;

		// Read the columns information from the result set
		try 
		{
			int columns = atts.getLength();
			
			// Capturing Column Names (FORCING TO LOWER CASE) and SQL Types 
			propNames = new ArrayList<String>(columns);
			propSQLTypes = new int[columns];
			
			for(int i=0; i < columns; i++)
			{
				propNames.add(atts.getQName(i).toLowerCase());
				propSQLTypes[i] = Types.VARCHAR;  //For XML type, all properties are String's. (Don't think this is used for XML)
			}
			
			// Now we know one more type! Magic!!!
			knownMagicTypes.put(i_xmlCode, this);
		} 
		catch (Exception e)
		{
			System.out.println("FATAL: MagicTypeDescriptor failed to read Attributes for " + i_xmlCode);
			e.printStackTrace();
			throw e;
		}
	}
	
	// Nice, Neat and otherwise handy Public Methods are Below!
	// --------------------------------------------------------------------------------------------------
	// Main "Constructor" of sorts -- you should only construct through here, to ensure proper Caching
	public static synchronized MagicTypeDescriptor loadMetaDataForMagicType(
			String queryCode, String destClassName, ResultSet rs) throws SQLException 
	{
		// Checking if this type is already "cached"
		MagicTypeDescriptor desc = knownMagicTypes.get(queryCode);
		if(desc != null)
			return desc;
		
		// Create new Descriptor (it will get auto-cached by constructor)
		return new MagicTypeDescriptor(queryCode, destClassName, rs);
	}

	// --------------------------------------------------------------------------------------------------
	// mateo - 2nd Constructor of sorts -- for loading data from XML files. 
	public static synchronized MagicTypeDescriptor loadMetaDataForMagicType(
			String xmlCode, String destClassName, Attributes atts) throws Exception 
	{
		// Checking if this type is already "cached"
		MagicTypeDescriptor desc = knownMagicTypes.get(xmlCode);
		if(desc != null)
			return desc;
		
		// Create new Descriptor (it will get auto-cached by constructor)
		return new MagicTypeDescriptor(xmlCode, destClassName, atts);
	}
	
	// For convenience, returns count of columns in this Descriptor (in the ResultSet that it describes)
	public int size()
	{
		return propNames.size();
	}

	public ArrayList<String> getPropertyNames()
	{
		return propNames;
	}
	
	public String getDestClassName() {
		return destClassName;
	}

	public String getQueryCode() {
		return queryCode;
	}

	// Naturally returns -1 if not found -- ALL PROPERTY NAMES FORCED TO LOWER CASE -- ASSUMPTION
	public int calcPropIndex(String propertyName) {
		return propNames.indexOf(propertyName.toLowerCase());
	}
	
	@Override
	public String toString() {
		String title = "Query Code: "+queryCode+" Destination Class: "+destClassName+"\n";
		return title + "Columns: "+propNames.toString();
	}
	
	// Extra value from Result set, the Column ID notation passed into it 
	// starts with 1 (matching Result Set notation) 
	// DISCLAIMER: This is the HEART OF THE BEAST - It may need revising based on your ERD and DB Server Type
	public Object extractColumnValue(ResultSet rs, int columnId) throws SQLException
	{
		Object result = MagicTypeDescriptor.class;
		// We should know by now all SQL Column Types, so voila!
		switch(propSQLTypes[columnId-1])
		{
			case Types.CHAR:
			case Types.VARCHAR: 
						result = rs.getString(columnId);
						break;
			case Types.TIMESTAMP:
			case Types.DATE:
						result = rs.getTimestamp(columnId);
						break;
			case Types.SMALLINT:
			case Types.INTEGER:
						result = rs.getInt(columnId);		 // Automatic Boxing FTW
						break;
			case Types.REAL:
			case Types.FLOAT:
						result = rs.getFloat(columnId);	   // Automatic Boxing FTW
						break;
			case Types.NUMERIC:
			case Types.DECIMAL:
			case Types.DOUBLE:
						result = rs.getDouble(columnId);   // Automatic Boxing FTW
						break;
			case Types.BOOLEAN:
						result = rs.getBoolean(columnId);  // Automatic Boxing FTW
						break;
			case Types.BIGINT:
						result = rs.getBigDecimal(columnId);
						break;
			case Types.CLOB:
						Clob clob = rs.getClob(columnId);
						result = null;
						if (clob != null)
						{							
							Reader is = clob.getCharacterStream();
							StringBuffer sb = new StringBuffer();
							int length = (int) clob.length();
							if (length > 0) {
								char[] buffer = new char[length];
								try {
									int count = 0;
									while ((count = is.read(buffer)) != -1)
										sb.append(buffer);
									
									result = new String(sb); }
								catch (Exception e) {} 
							}
						}
						break;
		}
		
		// Warning! Nothing was read, so it's just 'this' weird class inside
		if(result == MagicTypeDescriptor.class)
		{
			System.out.println("WARNING: Unrecognized Column Type in query "+queryCode+", column "+propNames.get(columnId-1)+" SQLType: "+propSQLTypes[columnId-1]);
			return (rs.wasNull()) ? null : rs.getString(columnId);
		}
		else		// Null is Null is Null!
			return (rs.wasNull()) ? null : result;
	}
	
}
