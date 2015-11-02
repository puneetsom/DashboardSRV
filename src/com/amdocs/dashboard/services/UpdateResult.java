package com.amdocs.dashboard.services;

import java.util.ArrayList;
import java.util.List;

public class UpdateResult 
{	
	public static final int UNDEFINED = -1;
	public static final int SUCCESS = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	
	private int status = UNDEFINED;
	private List<String> messages;
	private int rowsAffected;
	private Object returnObj;
	
	public UpdateResult() { 
		
	}

	public int getStatus() 
	{
		return status;
	}
	
	public void setStatus(int status) 
	{
		this.status = status;
	}
		
	public List<String> getMessages() 
	{
		return messages;
	}	
	public void setMessages(List<String> messages) 
	{
		this.messages = messages;
	}	
	public void addMessage(String message) 
	{
		if (messages == null)
			messages = new ArrayList<String>();
		messages.add(message);
	}
	public boolean hasMessage() 
	{
		return messages.size() > 0;
	}
	
	public int getRowsAffected() 
	{
		return rowsAffected;
	}
	
	public void setRowsAffected(int rowsAffected) 
	{
		this.rowsAffected = rowsAffected;
	}
	
	// Object to return from update (if any)
	public Object getReturnObj() 
	{
		return returnObj;
	}	
	public void setReturnObj(Object returnObj) 
	{
		this.returnObj = returnObj;
	}
	
	public String toString() 
	{
		return "status: " + status + ", rowsAffected: " + rowsAffected + ", messages: " + messages + ", returnObj: " + returnObj;
	}
}
