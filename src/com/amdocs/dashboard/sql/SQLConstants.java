package com.amdocs.dashboard.sql;

/**
 * @author Sudhir Bhatt
 *
 */
public interface SQLConstants
{
	public static final String SELECT = new String("SELECT");
	public static final String UPDATE = new String("UPDATE");
	public static final String INSERT = new String("INSERT");
	public static final String SET = new String("SET");
	public static final String FROM = new String("FROM");
	public static final String WHERE = new String("WHERE");
	public static final String SPACE = new String(" ");
	public static final String BLANK = new String("");
	public static final String VALUES = new String("VALUES");
	public static final String INTO = new String("INTO");
	
	public static final String COLUMN_TO_UPDATE = new String("colToUpdate");
	public static final String COLUMN_TO_INSERT = new String("colToInsert");
	public static final String TABLE_NAME = new String("tableName");
	public static final String KEY = new String("key");
	public static final String SQL_QUERY = new String("sqlquery");
	public static final String PARAMS = new String("params");
	public static final String QUERY_CODE = new String("queryCode");
	public static final String COLUMNS_PARAM = new String("columns");
	public static final String SORTS_PARAM = new String("sorts");
	public static final String CONDITIONS_PARAM = new String("conditions");
	public static final String RANGE_PARAM = new String("range");
	
	public static final String BINDING_PARAM = new String("?");
	public static final String COMMA = new String(",");
	public static final String SINGLE_QUOTES = new String("'");
	public static final String STR_AND = new String("and");
	public static final String STR_OR = new String("or");
	public static final String STR_NOT = new String("not");
	
	public static final String NAME_PARAM = new String("name");
	public static final String VALUE_PARAM = new String("value");
	public static final String COL_TYPE= new String("type");
	public static final String DATE_TIME_PATTERN= new String("dateTimePattern");
	
	public static final String EQUAL_TO = new String("=");
	public static final String NOT_EQUAL_TO = new String("!=");
	public static final String NULL = new String("null");
	public static final String IS_NULL = new String("is null");
	public static final String IS_NOT_NULL = new String("is not null");
	public static final String SQL_NOT = new String("Not");
	
	public static final String OPEN_PARENTHESIS = new String("(");
	public static final String CLOSE_PARENTHESIS = new String(")");
	public static final String DATE_FORMAT = new String("'MM/DD/YYYY'");
	
	public static final String LINEBREAKER = new String("\n");
	public static final String LEFT_INDENT = new String("\t");
	public static final String STAR = new String("*");
	public static final String ROWNUM_COL_NAME = new String("sql$rnum");
	public static final String ROWNUM_STMT = new String("row_number() over (partition by 1 order by ");
	public static final String DEFAULT_ROWNUM_STMT = new String("row_number() over (partition by 1 order by 1");
	public static final String REQEXP_LIKE = new String("regexp_like");
	public static final String TO_DATE = new String("to_date");
	public static final String TRIM = new String("trim");
	public static final String CASE_INSENSITIVE = new String("i");
	public static final String BETWEEN = new String("between");
	public static final String DESCENDING = new String("descending");
	public static final String SORT_DESC = new String("desc");
	public static final String DATAFIELD = new String("dataField");
	public static final String EXPRESSION = new String("expression");
	public static final String START_ROW = new String("startRow");
	public static final String MAX_ROWS = new String("maxRows");
	
	public static final String MAIN_SQL_ALIAS = new String("a");
	public static final String TOP_LEVEL_ALIAS = new String("b");	
	public static final String DOT_OPERATOR = new String(".");
	
	public static final String COL_TYPE_DATE = new String("date");
	public static final String COL_TYPE_NUMBER = new String("number");
}
