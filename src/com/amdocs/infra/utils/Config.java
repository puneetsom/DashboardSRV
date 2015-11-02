/*
 * Config.java
 *
 * Created on October 25, 2007, 2:36 PM
 *
 * Stores DB configuration. A singleton class that is used for saving properties.
 */

package com.amdocs.infra.utils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 *
 * @author Prithvi K P
 */
public class Config 
{   
	// amdocs.dir no longer necessary
	// Changed to get config.properties from /WEB-INF/classes
    //public static final String DIRECTORY_NAME = "amdocs.dir";
    public static final String PROPERTY_NAME  = "config";

    private static Properties mMap = null;
    private static Config mInstance = null;

    /** Creates a new instance of Config */
    private Config() 
    {
    }
    
    public static Config getInstance()
    {
        if(mInstance == null)
        {
            mInstance = new Config();
            
            try {
            	loadProperties(PROPERTY_NAME);
            } catch (Exception e) {
            	System.out.println("Config: Failed to load properties, message: " + e.getMessage());
            }
            System.out.println(mMap);
            
            /*
            try
            {
            	mInstance.readProperties();
            }
            catch(Exception e)
            {
            	System.out.println("Config: Failed to load properties, message: " + e.getMessage());
            }
            */
        }

        return mInstance;
    }
    
    public void setProperty(String name, Object value)
    {
        mMap.put(name, value);  
    }
    
    public Object getProperty(String name)
    {
        return mMap.get(name);  
    }
    
    //public void readProperties() throws IOException
    //{
    //    mMap = new Properties();
    //    FileInputStream fis = new FileInputStream(System.getProperty(DIRECTORY_NAME) + File.separator + PROPERTY_NAME);
    //    mMap.load(fis);
    //}
    
    

	public static Properties loadProperties(String baseFileName) throws IOException {  
		Properties prop = new Properties();
		ResourceBundle bundle = ResourceBundle.getBundle(baseFileName);
		Enumeration en = bundle.getKeys();
		String key = null;
		
		while (en.hasMoreElements()) {
			key = (String) en.nextElement();
			prop.put( key, bundle.getObject(key));
		}
		//logger.debug(prop);
		mMap = prop;
		
		return prop;
	}
}
