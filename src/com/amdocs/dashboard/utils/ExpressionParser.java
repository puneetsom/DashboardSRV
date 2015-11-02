package com.amdocs.dashboard.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExpressionParser 
{
	private static List<ExpressionToken> tokens;
	
	public static List<ExpressionToken> getTokens()
	{
		return tokens;
	}
	
	public static void setTokens(List<ExpressionToken> values)
	{
		tokens = values;
	}
	
	/**
	 * Take expression and split it into an array of tokens
	 * 
	 * Examples:
	 *   "WRF* or ITIC*"               -> [ "WRF*", "OR", "ITIC*" ]
	 *   "'Ready to Test' or Assigned" -> [ "Ready to Test", "OR", "Assigned" ]
	 *   "> 1000"                      -> [ ">", "1000" ]
	 */
	public static void createTokens(String expression)
	{
		tokens = new ArrayList<ExpressionToken>();
		
		String operatorRegExp = "^[(|)|=|!|<|>]";//new String("^(\\(|\\)|=|!=|<>|<=|>=|>|<| ).*");
		
		Boolean isQuotedString = false;
		String quoteChar = null;
		List<Object> currentTokenArr = new ArrayList<Object>();
		String prevChar = null;
		
		for (int i = 0; i < expression.length(); i++)
		{
			ExpressionToken expressionToken = null;
			
			String currChar = new String(new char[]{expression.charAt(i)});
			
			// determine if next value is an operator (=,!=,<>,<=,>=,>, or <) or parenthesis
			//String[] operatorArr = expression.substring(i).split(operatorRegExp);
			//if(operatorArr != null && operatorArr.length > 1)
				//operator = operatorArr[1];
			String operator = null;			
			Pattern p = Pattern.compile(operatorRegExp);
		    Matcher matcher = p.matcher(expression.substring(i));
		    while (matcher.find()) {
		    	operator = matcher.group();
		    }
			
			if (!isQuotedString && (" ".equalsIgnoreCase(currChar) || operator != null))
			{
				if (currentTokenArr.size() > 0)
				{
					String str = "";
					for(Object obj : currentTokenArr)
						str = str + obj;
					expressionToken = new ExpressionToken(str, false);
					tokens.add(expressionToken);
				}
				
				currentTokenArr = new ArrayList<Object>();
				
				if (operator != null)
				{
					String operator1 = null;
					p = Pattern.compile("^[=|<|>]");
				    matcher = p.matcher(expression.substring(i + operator.length()));
				    while (matcher.find()) {
				    	operator1 = matcher.group();
				    }
				    if(operator1 != null && !operator.equals("(") && !operator.equals(")"))
				    	operator = operator + operator1;
				    
					expressionToken = new ExpressionToken(operator, false);
					tokens.add(expressionToken);
					i += operator.length() - 1;
				}
			}
			else if (!isQuotedString && ("'".equalsIgnoreCase(currChar) || "\"".equalsIgnoreCase(currChar)))
			{
				isQuotedString = true;
				quoteChar = currChar;
			}
			else if (isQuotedString && currChar.equalsIgnoreCase(quoteChar) &&  !"\\".equalsIgnoreCase(prevChar))
			{
				String str = "";
				for(Object obj : currentTokenArr)
					str = str + obj;
				expressionToken = new ExpressionToken(str, true);
				tokens.add(expressionToken);
				
				isQuotedString = false;
				quoteChar = null;
				currentTokenArr = new ArrayList<Object>(); 
			}
			else
			{
				currentTokenArr.add(currChar);					
			}
			
			prevChar = currChar;				
		}
		
		if (currentTokenArr.size() > 0)
		{
			String str = "";
			for(Object obj : currentTokenArr)
				str = str + obj;
			tokens.add(new ExpressionToken(str, false));
		}		
	}
}