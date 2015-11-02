package com.amdocs.dashboard.utils;

import java.util.Date;

public class ExpressionToken
{	
	// private enumerations
	private enum TOKENTYPE {UNKNOWN, PARENTHESIS, OPERATOR, CONDITION, DATE, INTEGER, LONG, FLOAT, DOUBLE, STRING}
	
	private static final String EQUAL = new String("=");
	private static final String UNEQUAL = new String("!=");
	private static final String UNEQUAL_SQL = new String("<>");
	private static final String SMALLER = new String("<");
	private static final String LARGER = new String(">");
	private static final String SMALLEREQ = new String("<=");
	private static final String LARGEREQ = new String(">=");
	private static final String OPEN_PARENTHESIS = new String("(");
	private static final String CLOSE_PARENTHESIS = new String(")");
	
	private static final String STR_AND = new String("and");
	private static final String STR_OR = new String("or");
	private static final String STR_NOT = new String("not");
	private static final String AND = new String("&&");
	private static final String OR = new String("||");
	private static final String NOT = new String("!");
	
	private Object value;
	private TOKENTYPE type;	
	
	public ExpressionToken(String value, Boolean isLiteral)
	{
		this.value = value;
		calculateType(isLiteral);
	}
	
	public Object getValue()
	{
		return value;
	}
	
	public void setValue(Object value)
	{
		this.value = value;
		calculateType(true);
	}
	
	private void calculateType(Boolean isLiteral)
	{
		if (!isLiteral && value instanceof String && (OPEN_PARENTHESIS.equals(value.toString())  || CLOSE_PARENTHESIS.equals(value.toString())))
			type = TOKENTYPE.PARENTHESIS;
		else if (!isLiteral && (NOT.equalsIgnoreCase(value.toString()) || EQUAL.equals(value) || UNEQUAL.equals(value) || UNEQUAL_SQL.equals(value) || LARGER.equals(value) || LARGEREQ.equals(value) || SMALLER.equals(value) || SMALLEREQ.equals(value))){
			type = TOKENTYPE.OPERATOR;						
		}else if (!isLiteral && (STR_NOT.equalsIgnoreCase(value.toString()) || STR_AND.equalsIgnoreCase(value.toString()) || STR_OR.equalsIgnoreCase(value.toString()) || AND.equals(value.toString()) || OR.equals(value.toString()))){
			value = value.toString().toLowerCase();
			if(AND.equals(value.toString()))
				value = STR_AND;
			if(OR.equals(value.toString()))
				value = STR_AND;
			type = TOKENTYPE.CONDITION;
		}else{
			if (value instanceof Date)
				type = TOKENTYPE.DATE;
			else if (value instanceof Integer)
				type = TOKENTYPE.INTEGER;
			else if (value instanceof Long)
				type = TOKENTYPE.LONG;
			else if (value instanceof Float)
				type = TOKENTYPE.FLOAT;
			else if (value instanceof Double)
				type = TOKENTYPE.DOUBLE;
			else
				type = TOKENTYPE.STRING;
		}
	}
	
	public Boolean isValue() { return (type.equals(TOKENTYPE.DATE) || type.equals(TOKENTYPE.INTEGER) || type.equals(TOKENTYPE.LONG) || type.equals(TOKENTYPE.FLOAT) || type.equals(TOKENTYPE.DOUBLE) ||  type.equals(TOKENTYPE.STRING)); }
	
	public Boolean isOperator() { return type.equals(TOKENTYPE.OPERATOR); }
	
	public Boolean isParenthesis() { return type.equals(TOKENTYPE.PARENTHESIS); }
	
	public Boolean isOpenParenthesis() { return (isParenthesis() && OPEN_PARENTHESIS.equals(value.toString())); }
	
	public Boolean isCloseParenthesis() { return (isParenthesis() && CLOSE_PARENTHESIS.equals(value.toString())); }
	
	public Boolean isCondition() { return type.equals(TOKENTYPE.CONDITION); }
	
	public Boolean isANDCondition() { return (isCondition() && STR_AND.equals(value.toString())); }
	
	public Boolean isORCondition() { return (isCondition() && STR_OR.equals(value.toString())); }
	
	public Boolean isNOTCondition() { return (isCondition() && STR_NOT.equals(value.toString())); }
	
	public String toString() { return value + " (" + type + ")"; }
}
