package com.amdocs.dashboard.utils;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardUtils 
{
    
	// Converts Object to a Map
    public static Map<String, Object> objectToMap(Object obj)
    {
           Class<? extends Object> klass = obj.getClass();

           // If klass is a System class then set includeSuperClass to false.
           boolean includeSuperClass = klass.getClassLoader() != null;
           Method[] methods = includeSuperClass? klass.getMethods():klass.getDeclaredMethods();
           Map<String,Object> map = new HashMap<String,Object>();

           for (int i = 0; i < methods.length; i += 1) 
           {
                  try 
                  {
                        Method method = methods[i];
                        
                        if (!Modifier.isPublic(method.getModifiers()))
                               continue;

                        String name = method.getName();
                        String key = "";

                        if (name.startsWith("get")) 
                        {
                               if ("getClass".equals(name) || "getDeclaringClass".equals(name))
                                      key = "";
                               else
                                      key = name.substring(3);
                        } 
                        else if (name.startsWith("is")) 
                        {
                               // skip methods that start with "isSet"
                               if (name.startsWith("isSet"))
                                      continue;
                               
                               key = name.substring(2);
                        }
                        
                        if (key.length() == 0 || !Character.isUpperCase(key.charAt(0)) || method.getParameterTypes().length != 0) 
                               continue;

                        // check if "isSet" method exists.
                        Method isSetMethod = klass.getMethod("isSet" + key);
                        if (isSetMethod != null)
                        {
                               Object isSetResult = isSetMethod.invoke(obj, (Object[])null);
                               // if isSet method returns false, then skip
                               if (isSetResult instanceof Boolean && ((Boolean) isSetResult) == false)
                                      continue;
                        }

                        if (key.length() == 1)
                               key = key.toLowerCase();
                        else
                               key = key.substring(0, 1).toLowerCase() + key.substring(1);

                        Object value = method.invoke(obj, (Object[])null);
                        map.put(key, parseObject(value));
                        
                  } 
                  catch (Exception e) 
                  {
                        // do nothing
                  }
           }
           
           return map;
    }
    
    private static List<Object> parseList(List<?> arr) 
    {
           List<Object> list = new ArrayList<Object>();
           
           for (int i = 0; i < arr.size(); i++)
           {
                  Object value = arr.get(i);

                  if (value instanceof ArrayList)
                        list.add(parseList((List<?>) value));
                  else
                        list.add(parseObject(value));
           }
           
           return list;
    }

 private static Object parseObject(Object object) 
 {
     try 
     {
         if (object == null)
             return null;
         
         if (object instanceof Byte || object instanceof Character
                  || object instanceof Short || object instanceof Integer
                  || object instanceof Long || object instanceof Boolean
                  || object instanceof Float|| object instanceof Double
                  || object instanceof String)
             return object;

         if (object instanceof Enum)
         {
           Method valueMethod = object.getClass().getMethod("value");
           if (valueMethod != null)
                  return valueMethod.invoke(object, (Object[])null);
           return object;
         }
         
         if (object instanceof List)
             return parseList((List<?>) object);
         
         return objectToMap(object);
         
     } 
     catch(Exception exception) 
     {
         return null;
     }
 }
 

 private static final String SPACER = "   ";
 
    public static void printMap(Map<String, Object> map)
    {
           printMap(map, 0);
    }
    
    @SuppressWarnings("unchecked")
    public static void printMap(Map<String, Object> map, int level)
    {
           for (int i = 0; i < level; i++)
                  System.out.print(SPACER);
           System.out.println("{");
           
           int len = map.keySet().size();
           int cnt = 0;
           for (String key : map.keySet())
           {
                  for (int i = 0; i<= level; i++)
                        System.out.print(SPACER);
                  System.out.print(key + ": ");
                  Object value = map.get(key);
                  if (value instanceof Map)
                      printMap((Map<String,Object>) value, level+1);
                  //else if (value instanceof JSONObject)
                  //    printMap(((JSONObject) value).getMap(), level+1);
                  else if (value instanceof List)
                      printList((List<Object>) value, level+1);
                  //else if (value instanceof JSONArray)
                  //      printList(((JSONArray) value).getArrayList(), level+1);
                  else if (value instanceof String)
                        System.out.print("\"" + value + "\"");
                  else
                        System.out.print(value);
                  
                  if (cnt != len-1)
                        System.out.println(",");
                  else
                        System.out.println("");
                  cnt++;
           }

           for (int i = 0; i < level; i++)
                  System.out.print(SPACER);
           System.out.print("}");
           
           if (level == 0)
                  System.out.println("");
    }

    @SuppressWarnings("unchecked")
    public static void printList(List<Object> list, int level) 
    {
           System.out.println("[ ");
           
           int len = list.size();
           int cnt = 0;
           for (Object value : list)
           {                    
                  if (value instanceof Map)
                        printMap((Map<String,Object>) value, level+1);
                  //else if (value instanceof JSONObject)
                  //    printMap(((JSONObject) value).getMap(), level+1);
                  else if (value instanceof List)
                        printList((List<Object>) value, level+1);
                  else if (value instanceof String)
                        System.out.print("\"" + value + "\"");
                  else
                        System.out.print(value);

                  
                  if (cnt != len-1)
                        System.out.println(",");
                  else
                        System.out.println("");
                  cnt++;
           }
           
           for (int i = 0; i < level; i++)
                  System.out.print(SPACER);
           System.out.print("]");
    }

}
