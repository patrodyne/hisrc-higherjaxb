package org.example.checkbook;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransDate
{
	private static java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM-dd-yyyy");
	// Represent a SLF4J logger for this class.
    protected static Logger logger = LoggerFactory.getLogger(TransDate.class);

	public static Date parseDate(String d)
	{
		try
		{
			return df.parse(d);
		}
		catch (java.text.ParseException pe)
		{
			logger.error("parseDate", pe);
			return new Date();
		}
	}

	public static String printDate(Date d)
	{
		return df.format(d);
	}
}
