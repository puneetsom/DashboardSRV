package com.amdocs.infra.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

public class XMLWriter 
{
	private Writer writer;      // underlying writer
	private Stack stack;        // of xml entity names
	private StringBuffer attrs; // current attribute string
	private boolean empty;      // is the current node empty
	private boolean closed;     // is the current node closed...

	/**
	 * Create an XMLWriter for a generic Writer.
	 */
	public XMLWriter(Writer writer)
	{
		this.writer = writer;
		this.closed = true;
		this.stack = new Stack();
	}

	/**
	 * Start a new entity. 
	 *
	 * @param String name of entity.
	 */
	public void writeXMLHeader() throws IOException
	{
		String version = "1.0"; //TODO: for now, only version 1.0
		String encoding = "utf-8"; //TODO: for now, only utf-8

		this.writer.write("<?xml version=\"");
		this.writer.write(version);		
		this.writer.write("\" encoding=\"");
		this.writer.write(encoding);
		this.writer.write("\"?>");
	}
	
	/**
	 * Start a new entity. 
	 *
	 * @param String name of entity.
	 */
	public XMLWriter writeEntity(String name) throws Exception
	{
		try 
		{
			closeTag();
			this.closed = false;
			this.writer.write("<");
			this.writer.write(name);
			stack.add(name);
			this.empty = true;
			return this;
		}
		catch(IOException e)
		{
			throw new Exception(e);
		}
	}

	// close off the opening tag
	private void closeTag() throws IOException
	{
		if (!this.closed) 
		{
			writeAttributes();
			this.closed = true;
			this.writer.write(">");
		}
	}

	// write out all attributes for the current entity.
	private void writeAttributes() throws IOException
	{
		if (this.attrs != null) {
			this.writer.write(this.attrs.toString());
			this.attrs.setLength(0);
			this.empty = false;
		}
	}

	/**
	 * Write an attribute for the current entity. 
	 * Special characters are encoded.
	 *
	 * @param String name of attribute.
	 * @param String value of attribute.
	 */
	public XMLWriter writeAttribute(String attr, String value) throws Exception 
	{
		if(false)
			throw new Exception();

		if(this.attrs == null)
		{
			this.attrs = new StringBuffer();
		}

		this.attrs.append(" ");
		this.attrs.append(attr);
		this.attrs.append("=\"");
		this.attrs.append(encodeXml(value));
		this.attrs.append("\"");
		return this;
	}

	/**
	 * End the current entity.
	 */
	public XMLWriter endEntity() throws Exception
	{
		try
		{
			if(this.stack.empty()) 
			{
				throw new Exception("Called endEntity too many times.");
			}

			String name = (String)this.stack.pop();
			
			if (name != null) 
			{
				if (this.empty) 
				{
					writeAttributes();
					this.writer.write("/>");
				}
				else 
				{
					this.writer.write("</");
					this.writer.write(name);
					this.writer.write(">");
				}
				this.empty = false;
				this.closed = true; //mateo
			}
			return this;
		}
		catch(IOException e) 
		{
			throw new Exception(e);
		}
	}

	/**
	 * Close this writer.
	 * 
	 * Do not close the underlying writer, but 
	 * throw an exception if there are unclosed tags.
	 */
	public void close() throws Exception
	{
		if(!this.stack.empty()) 
		{
			throw new Exception("Tags are not all closed. Possibly, " + this.stack.pop() + " is unclosed.");
		}
	}

	/**
	 * Output text.  Special characters are encoded. 
	 */
	public XMLWriter writeText(String text) throws Exception 
	{
		try 
		{
			closeTag();
			this.empty = false;
			this.writer.write(encodeXml(text));
			return this;
		}
		catch(IOException e)
		{
			throw new Exception(e);
		}
	}

	//static helper functions

	static public String encodeXml(String str)
	{
		str = replaceString(str, "&", "&amp;");
		str = replaceString(str, "<", "&lt;");
		str = replaceString(str, ">", "&gt;");
		str = replaceString(str, "\"", "&quot;");
		str = replaceString(str, "'", "&apos;");
		return str;
	}  

	static public String replaceString(String text, String repl, String with) 
	{
		return replaceString(text, repl, with, -1);
	}

	/**
	 * Replace a string with another string inside a larger string, for
	 * the first n values of the search string.
	 *
	 * @param text String to do search and replace in
	 * @param repl String to search for
	 * @param with String to replace with
	 * @param n    int    values to replace
	 *
	 * @return String with n values replacEd
	 */
	static public String replaceString(String text, String repl, String with, int max)
	{
		if(text == null)
		{
			return null;
		}

		StringBuffer buffer = new StringBuffer(text.length());
		int start = 0;
		int end = 0;

		while( (end = text.indexOf(repl, start)) != -1 )
		{
			buffer.append(text.substring(start, end)).append(with);
			start = end + repl.length();

			if(--max == 0)
			{
				break;
			}
		}
		buffer.append(text.substring(start));

		return buffer.toString();
	}              

}
