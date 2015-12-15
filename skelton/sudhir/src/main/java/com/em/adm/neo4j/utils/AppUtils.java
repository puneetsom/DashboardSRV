/**
 * 
 */
package com.em.adm.neo4j.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author sudhir
 *
 */
public class AppUtils {
	
	public static <E> Collection<E> makeCollection(Iterable<E> iter) {
	    Collection<E> list = new ArrayList<E>();
	    for (E item : iter) {
	        list.add(item);
	    }
	    return list;
	}
}
