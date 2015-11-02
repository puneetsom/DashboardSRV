/**
 * ConnectionPoolHelper - Copyright (C) Amdocs 2008
 * 
 * This interface must be implemented by any class that wants to help Amdocs BlazeDS infra select 
 * the right JDBC connection pool based on incoming query Request
 * 
 * You are allowed to return null, in which case the Default pool will be chosen.
 * But PLEASE, no throwing any exceptions, for Any reason!
 * 
 * ALSO - Realize that there will be <b>No External Synchornization</b>, so there will only be
 * <b>Single Instance</b> of pool selector helper, here may be called by Many threads concurrently. 
 * Do Internal synchronization, if applicable (or better yet, use no class properties!)
 * Also, PARAMETERLESS constructor is required! 
 * 
 * @author Adi Rabinovich
 *
 */
package com.amdocs.infra.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.UnavailableException;

public interface ConnectionPoolHelper {
	// This method will be activated every time pool name calculation is needed - 
	// so make it as efficient as possible -- Also, keep it internally synchronized (if needed)!
	public String getConnectionPoolForRequest(SQLDescriptor reqDescriptor, HashMap<String, Object> reqParams);
	// This method gives the class a chance to initialize (optional), such as load settings for connection pools
	// Make sure to use context.getResourceAsStream() syntax, since we are in the Web World here
	public void init(ServletConfig config, Logger logger) throws IOException, UnavailableException;
}
