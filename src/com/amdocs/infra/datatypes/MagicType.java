/**
 * MagicType - Copyright (C) Amdocs 2008
 * 
 * This object works in conjunction with MagicTypeProxy to "pretend" and become any class that Flex wants to receive
 * 
 * @author AdiR
 *-----------------------------------------------------------------------------
 * Matt (mateo) - September 2009
 * Added handling for using MagicType's with XML files.
 *-----------------------------------------------------------------------------
 */
package com.amdocs.infra.datatypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.xml.sax.Attributes;

import flex.messaging.io.PropertyProxyRegistry;

public class MagicType {
	
	// Initialize proxy for this class as an "Official" Serializer! This is what makes the Magic happen!
	static {
		PropertyProxyRegistry.getRegistry().register(MagicType.class, new MagicTypeProxy());
	}

	// An Array holding actual values of this object, as returned from DB or wherever
	protected ArrayList<Object> propValues = new ArrayList<Object>();

	// A pointer to the MagicTypeDescriptor of this particular MagicType
	protected MagicTypeDescriptor propDefs;
	
	public MagicType(ResultSet rs, MagicTypeDescriptor queryDef) throws SQLException
	{
		propDefs = queryDef;
		// Load Meta Data
		for(int i=0;i<propDefs.size();i++)
			propValues.add(propDefs.extractColumnValue(rs, i+1));
	}

	//-------------------------------------------------------------------------
	// mateo - added handling XML MagicType's. 
	public MagicType(Attributes atts, MagicTypeDescriptor queryDef) throws SQLException
	{
		propDefs = queryDef;
		// Load Meta Data
		for(int i=0; i < propDefs.size(); i++)
			propValues.add(atts.getValue(i));
	}

	public ArrayList<String> getPropertyNames()
	{
		return propDefs.getPropertyNames();
	}

	public Object getPropertyValue(String propertyName) 
	{
		int i = propDefs.calcPropIndex(propertyName);
		if(i>-1)
			return propValues.get(i);
		// Default end-all
		return null;
	}

	public String getAlias() 
	{
		return propDefs.getDestClassName();
	}
}
