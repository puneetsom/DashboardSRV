package com.amdocs.dashboard.sql;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amdocs.dashboard.utils.ExpressionParser;
import com.amdocs.dashboard.utils.ExpressionToken;

/**
 * @author Sudhir Bhatt
 *
 */
public class SQLBuilder implements SQLConstants
{
	
	private enum DATEVALUETYPE {TODAY, YESTERDAY, TOMORROW, THISWEEK, LASTWEEK, NEXTWEEK, THISMONTH, LASTMONTH, NEXTMONTH, THISYEAR, LASTYEAR, NEXTYEAR}
	
	@SuppressWarnings("serial")
	private static final Map<String, DATEVALUETYPE> DATE_VALUE_MAP = new HashMap<String, DATEVALUETYPE>(){{
																											put("Today", DATEVALUETYPE.TODAY);
																											put("Yesterday", DATEVALUETYPE.YESTERDAY);
																											put("Tomorrow", DATEVALUETYPE.TOMORROW);
																											put("ThisWeek", DATEVALUETYPE.THISWEEK);
																											put("LastWeek", DATEVALUETYPE.LASTWEEK);
																											put("NextWeek", DATEVALUETYPE.LASTWEEK);
																											put("ThisMonth", DATEVALUETYPE.THISMONTH);
																											put("LastMonth", DATEVALUETYPE.LASTMONTH);
																											put("NextMonth", DATEVALUETYPE.NEXTMONTH);
																											put("ThisYear", DATEVALUETYPE.THISYEAR);
																											put("LastYear", DATEVALUETYPE.LASTYEAR);
																											put("NextYear", DATEVALUETYPE.NEXTYEAR);
																										}};
	public static Map<Object, Object> buildUpdateSql(Map<Object, Object> parms)
	{
		
		Map<Object, Object> sqlQueryVal = new HashMap<Object, Object>();		
		StringBuffer sqlQuery = new StringBuffer();
		List<Object> colToUpdateVal = new ArrayList<Object>();
		List<Object> keyColVal = new ArrayList<Object>();
		
		sqlQuery.append(UPDATE + SPACE + (String)parms.get(TABLE_NAME) + SPACE + LINEBREAKER + SET + SPACE);
        buildColToUpdateAndKey(sqlQuery, (Object[])parms.get(COLUMN_TO_UPDATE), colToUpdateVal, COLUMN_TO_UPDATE);
        sqlQuery.append(SPACE + LINEBREAKER + WHERE + SPACE);
        buildColToUpdateAndKey(sqlQuery, (Object[])parms.get(KEY), keyColVal, KEY);
        
        sqlQueryVal.put(SQL_QUERY, sqlQuery.toString());
        sqlQueryVal.put(COLUMN_TO_UPDATE, colToUpdateVal);
        sqlQueryVal.put(KEY, keyColVal);        
        
		return sqlQueryVal;
	}
	
	public static Map<Object, Object> buildInsertSql(Map<Object, Object> parms, int pkValue)
	{
		
		Map<Object, Object> sqlQueryVal = new HashMap<Object, Object>();		
		StringBuffer sqlQuery = new StringBuffer();
		List<Object> colToInsertVal = new ArrayList<Object>();
		
		sqlQuery.append(INSERT + SPACE + INTO + SPACE+ (String)parms.get(TABLE_NAME) + OPEN_PARENTHESIS + LINEBREAKER);
        buildColToInsert(sqlQuery, (Object[])parms.get(COLUMN_TO_INSERT), colToInsertVal, "COLUMNS", (Object[])parms.get(KEY), pkValue);
        sqlQuery.append(SPACE + LINEBREAKER + VALUES + SPACE + OPEN_PARENTHESIS);
        buildColToInsert(sqlQuery, (Object[])parms.get(COLUMN_TO_INSERT), null, VALUES, null, 0);
        
        sqlQueryVal.put(SQL_QUERY, sqlQuery.toString());
        sqlQueryVal.put(COLUMN_TO_INSERT, colToInsertVal);
        
		return sqlQueryVal;
	}
	

	@SuppressWarnings("unchecked")
	public static String getCustomizeQueryCode(HashMap<String, Object> additionalParams)
	{		
		List<String> cols = null;
		if (additionalParams != null && additionalParams.get(COLUMNS_PARAM) != null)
		{
			if (additionalParams.get(COLUMNS_PARAM) instanceof List)
				cols = (List<String>) additionalParams.get(COLUMNS_PARAM);
			else
				System.err.println("Columns not defined with correct class: " + additionalParams.get(COLUMNS_PARAM));
		} 
		if (additionalParams != null ) 
		{ 
			if(additionalParams.get(RANGE_PARAM) != null)
			{
				cols = new ArrayList<String>();
				cols.add("sql$rnum");
				cols.add("sql$cnt");
			}
			else if(additionalParams.get(SORTS_PARAM) != null)
			{
				cols = new ArrayList<String>();
				cols.add("sql$rnum");
			}
		}
		
		if(cols == null || cols.size() == 0)
			return "";
		
		StringBuffer strBuf = new StringBuffer();
		for(String column : cols)
			strBuf.append("_" + column);
		//System.out.println("-------- getCustomizeQueryCode ------------- "+ strBuf.toString());
		return strBuf.toString();
	}
	
	/**
	 * This method modifies the original code by wrapping around based upon provided 
	 * additional parameters.If not additional parameter are not sent then default
	 * values will be set. For e.g.
	 * 
	 * Original SQL Code:
	 * select a.column_1
     *  	, a.column_2
     *  	, a.column_3
     *  	, a.column_4
	 *     	, a.column_5
	 *      , a.column_6
	 *      , a.column_7  
	 *	from  table a
	 * 
	 * Modified SQL Code:
	 *  
	 *  SELECT b.*
	 *	FROM (SELECT a.*
     *           , row_number() over (partition by 1 order by a.column_1 desc, a.column_2) sql$rnum
	 *         FROM (
	 *       			select a.column_1
	 *      				 	, a.column_2
	 *      				 	, a.column_3
	 *      				 	, a.column_4
	 *      				 	, a.column_5
	 *      				 	, a.column_6
	 *      				 	, a.column_7       
	 *					from table a         
	 *
	 *       		) a
	 *	        WHERE (not(a.column_1 is null))
	 *	        and ((a.column_2 > ?) and (a.column_2 <= ?))
	 *	        and ((a.column_3 = ?) or (a.column_3 = ?) or (a.column_3 = ?) or (a.column_3 = ?))
	 *	        and ((trunc(to_date(a.column_4, 'MM/DD/YYYY'), 'MM') >= trunc(add_months(sysdate, -1),'MM')) and (trunc(to_date(a.column_4, 'MM/DD/YYYY'), 'MM') <= trunc(sysdate,'MM')))
	 *	        and (not((a.column_5 = ?) and (a.column_5 = ?)))
	 *	        and ((trunc(to_date(a.column_6, 'MM/DD/YYYY')) >= to_date(trim(?), 'MM/DD/YYYY')) and (trunc(to_date(a.column_6, 'MM/DD/YYYY')) < to_date(trim(?), 'MM/DD/YYYY')))
	 *	        and ((regexp_like(a.column_7,?,'i')) or (regexp_like(a.column_7,?,'i')))
   	 *		) b
	 *  where b.sql$rnum between 100 and 200  
	 *  
	 * 
	 * @param paramNames
	 * @param params
	 * @param query The original SQL query.
	 * @param additionalParams Contains columns need to be query on;
	 * 						   Sorting information; 
	 * 						   Goes on where clause; 
	 * 						   Number of rows to be filtered on
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public static String customizeQuery(ArrayList<String> paramNames, HashMap<String, Object> params,
									   String query, HashMap<String, Object> additionalParams)	
	{		
		List<String> cols = null;
		if (additionalParams != null && additionalParams.get(COLUMNS_PARAM) != null)
		{
			if (additionalParams.get(COLUMNS_PARAM) instanceof List)
				cols = (List<String>) additionalParams.get(COLUMNS_PARAM);
			else
				System.err.println("Columns not defined with correct class: " + additionalParams.get(COLUMNS_PARAM));
		}
		
		List<Map<String, Object>> sorts = null;
		if (additionalParams != null && additionalParams.get(SORTS_PARAM) != null)
		{
			if (additionalParams.get(SORTS_PARAM) instanceof List)
			{
				sorts = (List<Map<String, Object>>) additionalParams.get(SORTS_PARAM);
				System.out.println("List " + sorts);
			}
			else
			{
				System.err.println("Sorts not defined with correct class: " + additionalParams.get(SORTS_PARAM));
			}
		}
		
		List<Map<String, Object>> conditions = null;
		if (additionalParams != null && additionalParams.get(CONDITIONS_PARAM) != null)
		{
			if (additionalParams.get(CONDITIONS_PARAM) instanceof List)
				conditions = (List<Map<String, Object>>) additionalParams.get(CONDITIONS_PARAM);
			else
				System.err.println("Conditions not defined with correct class: " + additionalParams.get(CONDITIONS_PARAM));
		}
		
		Map<String, Object> range = (Map<String, Object>) additionalParams.get(RANGE_PARAM);
		
		StringBuffer sqlQuery = new StringBuffer();
		sqlQuery.append("select b.* \n");
		sqlQuery.append("from   (select ");
				
		//Set the Column information
		if(cols != null && cols.size() > 0)
		{
			for(int i=0; i < cols.size(); i++)
			{
				String col = (String)cols.get(i);
				sqlQuery.append(col);
				
				if(i < cols.size() - 1) {
	        		sqlQuery.append("\n             , ");     		
	        	}
			}
		} else
			sqlQuery.append("a.*");
		
		//Set the Sorting information
		//System.out.println(sorts);
		sqlQuery.append("\n             , row_number() ");
		sqlQuery.append("\n                   over (partition by 1");
		sqlQuery.append("\n                         order by ");
		
		if(sorts != null && sorts.size() > 0)
		{
			for(int i=0; i < sorts.size(); i++)
			{
				Map<String, Object> sort = (Map<String, Object>)sorts.get(i);
				Object descendingVal = sort.get(DESCENDING);
				boolean descending = false;
				if (descendingVal instanceof String)
					descending = Boolean.valueOf((String) descendingVal);
				else
					descending = (Boolean) sort.get(DESCENDING);
				
				sqlQuery.append(sort.get(DATAFIELD) + (descending?" desc":""));
				if(i < sorts.size() - 1){
	        		sqlQuery.append(", ");        		
	        	}
			}
			
    		sqlQuery.append(") sql$rnum");
    		
		} else {
			//sqlQuery.append("\n\t\t, row_number() over (partition by 1 order by 1) sql$rnum");

    		sqlQuery.append("1) sql$rnum");
		}

		sqlQuery.append("\n             , count(*) over (partition by 1) sql$cnt");
		
		sqlQuery.append("\n        from   (");
		sqlQuery.append("\n" + query);
		sqlQuery.append("\n               ) a");
		
		//Building where clause with Conditions information
		if(conditions != null && conditions.size() > 0)		
			buildWhereClause(sqlQuery, conditions, paramNames, params);		

		sqlQuery.append("\n       ) b");		
		
		//Range information
		if(range != null)
			sqlQuery.append("\nwhere  sql$rnum between " + range.get(START_ROW) + " and " + ((Integer)range.get(START_ROW) + (Integer)range.get(MAX_ROWS) - 1));
		
		//System.out.println("DEBUG:");
		//System.out.println(sqlQuery.toString());
		return sqlQuery.toString();
	}
	
	
	private static List<ExpressionToken> validateTokens(String colType, List<ExpressionToken> tokens, String dateTimePattern)
	{
		List<ExpressionToken> finalTokenList = new ArrayList<ExpressionToken>();
   	 
    	int count = 0;
    	Boolean nextTokenToBeDeleted = false;
		for(ExpressionToken token : tokens)
		{
			if(COL_TYPE_NUMBER.equalsIgnoreCase(colType))
			{
				if(token.isValue() && NULL.equalsIgnoreCase(token.getValue().toString()))
				{
					if(!nextTokenToBeDeleted)
			    		finalTokenList.add(token);
			    	else
			    		nextTokenToBeDeleted = false;
					
				}else if(token.isValue()){
					
					Pattern p = Pattern.compile("[^0-9.]+");
				    Matcher matcher = p.matcher(token.getValue().toString());
				    if(matcher.find() || !isValidFloat(token.getValue().toString()))
					{
				    	int orignalSize = 0;
				    	if(finalTokenList.size() - 1 >= 0 && (finalTokenList.get(finalTokenList.size() - 1).isOperator() || finalTokenList.get(finalTokenList.size() - 1).isCondition()))
				    	{
				    		orignalSize = finalTokenList.size();
				    		finalTokenList.remove(orignalSize - 1);
				    	}
				    	
				    	if(orignalSize - 2 >= 0 && (finalTokenList.get(orignalSize - 2).isOperator() || finalTokenList.get(orignalSize - 2).isCondition()))
				    		finalTokenList.remove(orignalSize - 2);
				    	
				    	if(count + 1 < tokens.size() && (tokens.get(count + 1).isANDCondition() || tokens.get(count + 1).isORCondition()))
				    		nextTokenToBeDeleted = true;
				    }else{
				    	if(!nextTokenToBeDeleted)
				    	{
				    		token.setValue(Float.parseFloat(token.getValue().toString()));
				    		finalTokenList.add(token);
				    	}else
				    		nextTokenToBeDeleted = false;
				    }
				}else{
					if(!nextTokenToBeDeleted)
			    		finalTokenList.add(token);
			    	else
			    		nextTokenToBeDeleted = false;
					
				}
			
			}else if(COL_TYPE_DATE.equalsIgnoreCase(colType)){
				
				if(token.isValue())
				{
					if(token.isValue() && NULL.equalsIgnoreCase(token.getValue().toString()))
					{
						if(!nextTokenToBeDeleted)
				    		finalTokenList.add(token);
				    	else
				    		nextTokenToBeDeleted = false;
						
					}else if(DATE_VALUE_MAP.containsKey(token.getValue()))
						if(!nextTokenToBeDeleted)
				    		finalTokenList.add(token);
				    	else
				    		nextTokenToBeDeleted = false;
					else if (token.getValue().toString().matches("^\\d{1,2}/\\d{1,2}/\\d{4}$") && isValidDate(token.getValue().toString(), dateTimePattern))
					{
						if(!nextTokenToBeDeleted)
				    		finalTokenList.add(token);
				    	else
				    		nextTokenToBeDeleted = false;
						
				    }else{
				    	int orignalSize = 0;
				    	if(finalTokenList.size() - 1 >= 0 && (finalTokenList.get(finalTokenList.size() - 1).isOperator() || finalTokenList.get(finalTokenList.size() - 1).isCondition()))
				    	{
				    		orignalSize = finalTokenList.size();
				    		finalTokenList.remove(orignalSize - 1);
				    	}
				    	
				    	if(orignalSize - 2 >= 0 && (finalTokenList.get(orignalSize - 2).isOperator() || finalTokenList.get(orignalSize - 2).isCondition()))
				    		finalTokenList.remove(orignalSize - 2);
				    	
				    	if(count + 1 < tokens.size() && (tokens.get(count + 1).isANDCondition() || tokens.get(count + 1).isORCondition()))
				    		nextTokenToBeDeleted = true;					    	
				    }
					
				}else{
					if(!nextTokenToBeDeleted)
			    		finalTokenList.add(token);
			    	else
			    		nextTokenToBeDeleted = false;
					
				}
			}
			count++;
		}
		
		Boolean containsAnyValue = false;
		for(ExpressionToken token : finalTokenList)
		{
			if(token.isValue())
			{
				containsAnyValue = true;
				break;
			}
		}
    	if(containsAnyValue)
    		tokens = finalTokenList;
    	else
    		tokens = new ArrayList<ExpressionToken>();
    	
    	return tokens;
	}
	
	private static boolean isValidFloat(String input)
	{		
	     try {
	    	Float.parseFloat(input);
	        return true;
	     }
	     catch(NumberFormatException  e){
	          return false;
	     }
	}
	
	private static boolean isValidDate(String input, String dateTimePattern)
	{
		DateFormat format = new SimpleDateFormat(dateTimePattern);
		format.setLenient(false);
	     try {
	          format.parse(input);
	          return true;
	     }
	     catch(ParseException e){
	          return false;
	     }
	}
	
	private static void buildWhereClause(StringBuffer sqlQuery, List<Map<String, Object>> conditions, ArrayList<String> paramNames, HashMap<String, Object> params)
	{
		sqlQuery.append("\n        where ");
	    int colCnt = 0;
	    String exp = null;
	    String colType = null;
	    String dateTypePattern = null;
		for(Map<String, Object> col : conditions)
	    {
	    	colType = (String)col.get(COL_TYPE);
	    	dateTypePattern = (String)col.get(DATE_TIME_PATTERN);
	    	exp = (String)col.get(EXPRESSION);
	    			    
		    ExpressionParser.createTokens(exp);
	    	if(COL_TYPE_DATE.equalsIgnoreCase(colType) || COL_TYPE_NUMBER.equalsIgnoreCase(colType))
	    		ExpressionParser.setTokens(validateTokens(colType, ExpressionParser.getTokens(), dateTypePattern));
	    	
		    //System.out.println(ExpressionParser.getTokens());
		    
	    	Stack<ExpressionToken> openParenthesisStack = new Stack<ExpressionToken>();
	    	Stack<ExpressionToken> closeParenthesisStack = new Stack<ExpressionToken>();
	    	for(ExpressionToken token : ExpressionParser.getTokens())
			{
				if(token.isOpenParenthesis())
					openParenthesisStack.push(token);
				else if(token.isCloseParenthesis())
					closeParenthesisStack.push(token);					
			}
	    	if(openParenthesisStack.size() != closeParenthesisStack.size())
	    		ExpressionParser.setTokens(new ArrayList<ExpressionToken>());
	    	
	    	if(colCnt > 0)
	    		sqlQuery.append("\n        and   ");  	
	    	
	    	// invalid token.  Define an expression that will not be true.
	    	if(ExpressionParser.getTokens().size() == 0)
	    	{
	    		sqlQuery.append("(1 = 2)");
	    		continue;
	    	}
	    	int countToken = 0;
	    	String tokenOperator = null;
	    	sqlQuery.append(OPEN_PARENTHESIS);			
	    	for(ExpressionToken token : ExpressionParser.getTokens())
		    {	    		
	    		if(token.isOpenParenthesis())
	    		{
	    			sqlQuery.append(OPEN_PARENTHESIS);
	    			if(countToken == 0)
	    			{	    			
	    				sqlQuery.append(OPEN_PARENTHESIS);
	    			}
	    		}
	    		
	    		else if(token.isCloseParenthesis())	    		
	    			sqlQuery.append(CLOSE_PARENTHESIS);
	    		
	    		else if(token.isOperator())
	    		{
	    			if(countToken == 0){	    			
	    				if(COL_TYPE_NUMBER.equalsIgnoreCase(colType))
	    					sqlQuery.append(OPEN_PARENTHESIS + col.get(DATAFIELD) + SPACE);
	    				else if(COL_TYPE_DATE.equalsIgnoreCase(colType))
	    					sqlQuery.append(OPEN_PARENTHESIS);
	    			}
	    			
	    			if(COL_TYPE_NUMBER.equalsIgnoreCase(colType))
	    				sqlQuery.append(token.getValue() + SPACE);
	    			else
	    				tokenOperator = token.getValue().toString();
	    			
	    		}else if(token.isValue()){
	    				    			
	    			if(countToken == 0){	    			
	    				if(COL_TYPE_NUMBER.equalsIgnoreCase(colType))
	    					sqlQuery.append(OPEN_PARENTHESIS + col.get(DATAFIELD) + SPACE);
	    				else
	    					sqlQuery.append(OPEN_PARENTHESIS);
	    			}
	    			
	    			if(COL_TYPE_DATE.equalsIgnoreCase(colType))	    				
	    				buildWhereConditionDateType(col.get(DATAFIELD).toString(), dateTypePattern, sqlQuery, tokenOperator,  token.getValue(), paramNames, params, colCnt, countToken);	    				
	    			else{
		    			
	    				if(countToken - 1 >= 0 && ExpressionParser.getTokens().get(countToken - 1).isOperator())
	    				{
	    					if(COL_TYPE_NUMBER.equalsIgnoreCase(colType))
	    					{
	    						if(NULL.equalsIgnoreCase(token.getValue().toString())){
	    							sqlQuery.append(SPACE + IS_NULL);
	    						}else{
		    						paramNames.add(VALUE_PARAM + "_" + colCnt + "_" + countToken);
		    						params.put(VALUE_PARAM + "_"  + colCnt + "_" + countToken, token.getValue());
		    						sqlQuery.append(BINDING_PARAM);
	    						}
	    					}else
	    						buildWhereConditionStringType(col.get(DATAFIELD).toString(), sqlQuery, tokenOperator,  token.getValue().toString(), paramNames, params, colCnt, countToken);	    						
	    						
	    				}else{
	    					if(COL_TYPE_NUMBER.equalsIgnoreCase(colType))
	    					{
	    						if(NULL.equalsIgnoreCase(token.getValue().toString())){
	    							sqlQuery.append(SPACE + IS_NULL);
	    						}else{
		    						paramNames.add(VALUE_PARAM + "_" + colCnt + "_" + countToken);
		    						params.put(VALUE_PARAM + "_"  + colCnt + "_" + countToken, token.getValue());
		    						sqlQuery.append("= " + BINDING_PARAM);
	    						}
	    						//sqlQuery.append("= " + token.getValue());
	    					}else
	    						buildWhereConditionStringType(col.get(DATAFIELD).toString(), sqlQuery, EQUAL_TO,  token.getValue().toString(), paramNames, params, colCnt, countToken);	    								    				
	    				}
	    			}    
	    			
	    			if(countToken == 0)
	    			{
	    				sqlQuery.append(CLOSE_PARENTHESIS);
	    			}
	    			
	    		}else if(token.isCondition()){
	    			
	    			if(COL_TYPE_NUMBER.equalsIgnoreCase(colType))
	    				sqlQuery.append((token.isNOTCondition() ? "" : SPACE) + token.getValue() + (token.isNOTCondition() ? "" : SPACE) + "(" + col.get(DATAFIELD) + SPACE);
	    			else if(COL_TYPE_DATE.equalsIgnoreCase(colType))
	    				sqlQuery.append((token.isNOTCondition() ? "" : SPACE) + token.getValue() + (token.isNOTCondition() ? "" : SPACE) + OPEN_PARENTHESIS);
	    			else{
	    				if(countToken + 1 < ExpressionParser.getTokens().size() && ExpressionParser.getTokens().get(countToken + 1).isNOTCondition())
	    					sqlQuery.append((token.isNOTCondition() ? "" : SPACE) + token.getValue() + (token.isNOTCondition() ? "" : SPACE));
	    				else if(countToken + 2 < ExpressionParser.getTokens().size() && ExpressionParser.getTokens().get(countToken + 2).isNOTCondition())
	    					sqlQuery.append((token.isNOTCondition() ? "" : SPACE) + token.getValue() + (token.isNOTCondition() ? "" : SPACE));
	    				else
	    					sqlQuery.append((token.isNOTCondition() ? "" : SPACE) + token.getValue() + (token.isNOTCondition() ? "" : SPACE) + OPEN_PARENTHESIS);
	    			}
	    			
	    		}	    		
	    		countToken++;
		    }
	    	sqlQuery.append(CLOSE_PARENTHESIS);	    	
	    	//System.out.println(sqlQuery.toString());
	    	colCnt++;
	    	
	    }//end for loop	
	}
		
	private static void buildWhereConditionStringType(String colName, StringBuffer sqlQuery, String operatorToken, String tokenValue, ArrayList<String> paramNames, HashMap<String, Object> params, int colCnt, int countToken)
	{
		//colName = "a." + colName;		
		if(NULL.equalsIgnoreCase(tokenValue)){
			if(NOT_EQUAL_TO.equals(operatorToken))
				sqlQuery.append(colName + SPACE + IS_NOT_NULL);
			else
				sqlQuery.append(colName + SPACE + IS_NULL);
		}else{
			
			paramNames.add(VALUE_PARAM + "_" + colCnt + "_"  + countToken);
			params.put(VALUE_PARAM + "_" + colCnt + "_"  + countToken, tokenValue);
			
			Pattern p = Pattern.compile("[*]");
		    Matcher matcher = p.matcher(tokenValue);
		    if(matcher.find())
		    {
		    	if(NOT_EQUAL_TO.equals(operatorToken))		    	
		    		sqlQuery.append("Not(regexp_like(" + colName + ", ?, 'i'))");							
		    	else
					sqlQuery.append("regexp_like(" + colName + ", ?, 'i')");
		    	
		    }else
		    	sqlQuery.append(colName + SPACE +  operatorToken + SPACE + BINDING_PARAM);		    
		    	
		}
		operatorToken = null;
	}
	
	private static void buildWhereConditionDateType(String colName, String dateTimePattern, StringBuffer sqlQuery, String operatorToken, Object tokenValue, ArrayList<String> paramNames, HashMap<String, Object> params, int colCnt, int countToken)
	{
		String operator = EQUAL_TO;
		if (operatorToken != null)
			operator = operatorToken;
		
		if(NULL.equalsIgnoreCase(tokenValue.toString())){
			if(NOT_EQUAL_TO.equals(operatorToken))
				sqlQuery.append(colName + SPACE + IS_NOT_NULL);
			else
				sqlQuery.append(colName + SPACE + IS_NULL);
		}else{
		
			DATEVALUETYPE expressionValue =  DATE_VALUE_MAP.get(tokenValue);
			colName = "to_date(" + colName + ", '" + dateTimePattern + "')";
			if(expressionValue != null)
			{
				switch (expressionValue)
				{
					case TODAY:
						sqlQuery.append("trunc(" + colName +")" + SPACE + operator + SPACE);		
						sqlQuery.append("trunc(sysdate)");								
						break;
					case YESTERDAY:
						sqlQuery.append("trunc(" + colName +")" + SPACE + operator + SPACE);				
						sqlQuery.append("trunc(sysdate - 1)");
						break;
					case TOMORROW:
						sqlQuery.append("trunc(" + colName +")" + SPACE + operator + SPACE);
						sqlQuery.append("trunc(sysdate + 1)");
						break;
					case THISWEEK:
						sqlQuery.append("trunc(" + colName + ", 'DAY') " + operator +  " trunc(sysdate,'DAY')");				
						break;
					case LASTWEEK:
						sqlQuery.append("trunc(" + colName + ", 'DAY') " + operator +  " trunc(sysdate,'DAY') - 7");
						break;
					case NEXTWEEK:
						sqlQuery.append("trunc(" + colName + ", 'DAY') " + operator +  " trunc(sysdate,'DAY') + 7");				
						break;
					case THISMONTH:
						sqlQuery.append("trunc(" + colName + ", 'MM') " + operator +  " trunc(sysdate,'MM')");
						break;
					case LASTMONTH:
						sqlQuery.append("trunc(" + colName + ", 'MM') " + operator +  " trunc(add_months(sysdate, -1),'MM')");
						break;
					case NEXTMONTH:
						sqlQuery.append("trunc(" + colName + ", 'MM') " + operator +  " trunc(add_months(sysdate, 1),'MM')");
						break;
					case THISYEAR:
						sqlQuery.append("trunc(" + colName + ", 'YYYY') " + operator +  " trunc(sysdate,'YYYY')");
						break;
					case LASTYEAR:
						sqlQuery.append("trunc(" + colName + ", 'YYYY') " + operator +  " add_months(trunc(sysdate,'YYYY'), -12)");				
						break;
					case NEXTYEAR:
						sqlQuery.append("trunc(" + colName + ", 'YYYY') " + operator +  " add_months(trunc(sysdate,'YYYY'), 12)");
						break;					
				}
			}else{
				paramNames.add(VALUE_PARAM + "_"  + colCnt+ "_"  + countToken);
				params.put(VALUE_PARAM + "_"  + colCnt+ "_"  + countToken, tokenValue);			
				sqlQuery.append("trunc(" + colName + ") " + operator + " to_date(trim(?), '" + dateTimePattern + "')");						
			}
		}
		operatorToken = null;
	}
	
	
	@SuppressWarnings({"unchecked" })
	private static void buildColToUpdateAndKey(StringBuffer sqlQuery, 
											   Object[] arrCol, 
											   List<Object> colValList,
											   String type)	{
		for(int i=0; i < arrCol.length; i++)
		{ 
        	Map<Object, Object> colMap = (Map<Object, Object>)arrCol[i];
        	
        	if(colMap.get(COL_TYPE) != null && COL_TYPE_DATE.equalsIgnoreCase((String)colMap.get(COL_TYPE)))
        		
        		sqlQuery.append((String)colMap.get(NAME_PARAM) + SPACE + EQUAL_TO + SPACE + TO_DATE + OPEN_PARENTHESIS + TRIM + OPEN_PARENTHESIS 
        				+ BINDING_PARAM	+ CLOSE_PARENTHESIS + COMMA + SPACE + DATE_FORMAT + CLOSE_PARENTHESIS);
        	else
        		sqlQuery.append((String)colMap.get(NAME_PARAM) + SPACE + EQUAL_TO + SPACE + BINDING_PARAM); 
        	
        	colValList.add(colMap.get(VALUE_PARAM));
        	
        	if(i != arrCol.length-1){
        		if(!KEY.equalsIgnoreCase(type)){
        			sqlQuery.append(SPACE + LINEBREAKER + SPACE + COMMA + SPACE);
        		}else{
        			sqlQuery.append(SPACE + LINEBREAKER + SPACE + STR_AND + SPACE);
        		}
        		
        	}// end if
        }//end for loop		
	}//end 	buildColToUpdateAndKey
	
	@SuppressWarnings({"unchecked" })
	private static void buildColToInsert(StringBuffer sqlQuery, 
										 Object[] arrCol, 
										 List<Object> colValList,
										 String type,
										 Object[] pkCol,
										 int pkVal)
	{
		
		for(int i=0; i < arrCol.length; i++)
		{ 
			Map<Object, Object> colMap = (Map<Object, Object>)arrCol[i];
        	
			if("COLUMNS".equalsIgnoreCase(type))
			{	        	
	        	sqlQuery.append((String)colMap.get(NAME_PARAM) + SPACE); 
	        	
	        	 boolean keyFound = false;
	        	 for(Object obj : pkCol)
	        	 {
	        		 Map<Object, Object> objMap = (Map<Object, Object>)obj;
	        		 if(((String)objMap.get(NAME_PARAM)).equalsIgnoreCase((String)colMap.get(NAME_PARAM)) && !keyFound)
	        		 {
	        				 colValList.add(pkVal);
	        				 keyFound = true;
	        				 break;
	        		 }
	        	 }
	        	 
	        	 if(!keyFound)
	        		 colValList.add(colMap.get(VALUE_PARAM));	        	
	        	
	        	if(i != arrCol.length-1){
	        		sqlQuery.append(LINEBREAKER + COMMA + SPACE);        		
	        	}else{
	        		sqlQuery.append(CLOSE_PARENTHESIS);
	        	}
	        	
			}else{
				
				if(COL_TYPE_DATE.equalsIgnoreCase((String)colMap.get(COL_TYPE)))
					sqlQuery.append(TO_DATE + OPEN_PARENTHESIS + BINDING_PARAM + COMMA + SPACE + DATE_FORMAT  + CLOSE_PARENTHESIS + SPACE);
				else
					sqlQuery.append(BINDING_PARAM  + SPACE);
				
				if(i != arrCol.length-1){
	        		sqlQuery.append(LINEBREAKER + COMMA + SPACE);        		
	        	}else{
	        		sqlQuery.append(CLOSE_PARENTHESIS);
	        	}
			}
        }//end for loop		
	}//end 	buildColToInsert
}
