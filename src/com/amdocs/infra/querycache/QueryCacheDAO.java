/**
 * Query Cache DAO - Adapter to activate relevant method inside the QueryCacheServer itself
 * 
 * The QueryCacheServer is singleton right now, synchronization is assumed to be internal to 
 * queryCacheServer (if even needed)
 */
package com.amdocs.infra.querycache;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * @author bhryxx2
 *
 */
public class QueryCacheDAO 
{
	// Synchronization of Queries parameters (expiration, keys, etc)
	// First 2 returned parameters are to speed up subsequent Sync requests, in case no changes
	public String syncSQLs(int lastLength, int lastHash)
	{
		String currentSyncSQL = QueryCacheServer.getInstance().synchSQLs();
		if(currentSyncSQL.length() == lastLength &&
		   currentSyncSQL.hashCode() == lastHash)
			return "";		// No changes! EZ Does it!
		else
			return ""+currentSyncSQL.length()+"~"+currentSyncSQL.hashCode()+"~"+currentSyncSQL;
	}
	
	// Leaving signature as-is, not Java 5 compliant, since not sure if BlazeDS will be able to call it otherwise
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List activateQuery(String queryCode, HashMap params, HashMap additionalParams) throws SQLException
	{
		//System.out.println("DEBUG: Query "+queryCode+" activated, with ");

		return QueryCacheServer.getInstance().activateQuery(queryCode, params, additionalParams);
		
//		if(params==null || params.size()==0)
//			{
//				System.out.println("NO parameters");
//			}
//		else
//		{
//			Iterator i = params.entrySet().iterator();
//			while(i.hasNext())
//			{
//				Map.Entry param = (Entry) i.next();
//				System.out.println(param.getKey()+"="+param.getValue());
//			}
//		}
//		
//		ArrayList lst = new ArrayList();
//		lst.add(new String("TEST ONLY"));
//		return lst;
	}
}
