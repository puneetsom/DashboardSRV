package com.amdocs.dashboard.services;

import java.util.Calendar;
import java.util.List;

public class QueryResult {
	private List<?> result;
	private Expiration expiration;
	private Calendar expirationDate; 
	
	// predefined expiration times for results.
	public static enum Expiration 
	{
		ONE_MINUTE, 
		FIVE_MINUTES, 
		FIFTEEN_MINUTES, 
		ONE_HOUR, 
		SIX_HOURS, 
		TWELVE_HOURS, 
		ONE_DAY, 
		ONE_WEEK,
		MIDNIGHT
	}
	
	public QueryResult(List<?> result) 
	{
		this(result, Expiration.MIDNIGHT);
	}
	
	public QueryResult(List<?> result, Expiration expiration) 
	{
		setResult(result);
		setExpiration(expiration);
		
		setExpirationDate();
	}

	public void setResult(List<?> result) {
		this.result = result;
	}
	public List<?> getResult() {
		return result;
	}
	
	public Expiration getExpiration() {
		return expiration;
	}

	public void setExpiration(Expiration expiration) {
		this.expiration = expiration;
	}
	
	private void setExpirationDate() 
	{		
		expirationDate = Calendar.getInstance();
		switch (expiration)
		{
			case ONE_MINUTE:
				expirationDate.add(Calendar.MINUTE, 1);
				break;
			case FIVE_MINUTES:
				expirationDate.add(Calendar.MINUTE, 5);
				break;
			case FIFTEEN_MINUTES:
				expirationDate.add(Calendar.MINUTE, 15);
				break;
			case ONE_HOUR:
				expirationDate.add(Calendar.HOUR_OF_DAY, 1);
				break;
			case SIX_HOURS:
				expirationDate.add(Calendar.HOUR_OF_DAY, 6);
				break;
			case TWELVE_HOURS:
				expirationDate.add(Calendar.HOUR_OF_DAY, 12);
				break;
			case ONE_DAY:
				expirationDate.add(Calendar.DAY_OF_MONTH, 1);
				break;
			case ONE_WEEK:
				expirationDate.add(Calendar.WEEK_OF_MONTH, 1);
				break;
			case MIDNIGHT:
				// Expire at midnight by default
				expirationDate = Calendar.getInstance();
				expirationDate.add(Calendar.DAY_OF_MONTH, 1);
				expirationDate.set(Calendar.HOUR_OF_DAY, 0);
				expirationDate.set(Calendar.MINUTE, 0);
				expirationDate.set(Calendar.SECOND, 0);
				expirationDate.set(Calendar.MILLISECOND, 0);
				break;
		}
	}

	public boolean isExpired() {
		return (expirationDate == null || Calendar.getInstance().after(expirationDate));
	}
}
