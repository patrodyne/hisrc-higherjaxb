package org.example.trade;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Conversion methods for various data types.</p>
 * 
 * <p>Used by Xml Java-binding Schema (XJS) Document Type Definition (DTD)
 * for &lt;conversion&gt; declarations:</p>
 * 
 * <ul>
 * 	<li><b>parse</b> - describes how to convert a string into a value of the target type.</li>
 * 	<li><b>print</b> - describes how to convert a value of the target type back into a string.</li>
 * </ul>
 * 
 * @author Rick O'Sullivan
 */
public class Bind
{
	// Represent a SLF4J logger for this class.
    private static Logger log = LoggerFactory.getLogger(Bind.class);
    // Represent a (thread-safe?) DatatypeFactory instance.
    public static DatatypeFactory DTF = null;
    static
    {
		try
		{
			DTF = DatatypeFactory.newInstance();
		}
		catch (DatatypeConfigurationException ex)
		{
			log.error("Cannot create DatatypeFactory instance!", ex);
		}
    }
	
    /**
     * <p>Create a new XMLGregorianCalendar by parsing the value as a ISO-8601 representation.</p>
     * 
     * @param value a string in ISO-8601 format.
     * @return An XMLGregorianCalendar instance with the given value.
     */
	public static XMLGregorianCalendar parseXGC(String value)
	{
		if ( DTF != null )
			return DTF.newXMLGregorianCalendar(value);
		else
		{
			log.error("No DatatypeFactory instance!");
			return null;
		}
	}
	
	/**
	 * <p>Return the ISO-8601 representation of <code>this</code> instance.</p>
	 * 
	 * @param value An XMLGregorianCalendar instance.
	 * @return An ISO-8601 representation of the given instance.
	 */
	public static String print(XMLGregorianCalendar value)
	{
		return (value != null) ? value.toXMLFormat() : null;
	}
	
	/**
	 * Create a new XMLGregorianCalendar with optional offset.
	 * @param offset Lexical representation of offset, "PnYnMnDTnHnMnS".
	 * @return A new XMLGregorianCalendar instance.
	 */
	public static XMLGregorianCalendar newXMLGregorianCalendar(String offset)
	{
		GregorianCalendar gc = new GregorianCalendar();
		XMLGregorianCalendar timestamp = DTF.newXMLGregorianCalendar(gc);
		if (offset != null)
			timestamp.add(DTF.newDuration(offset));
		return timestamp;
	}
	
	/**
	 * Create a new XMLGregorianCalendar with current time.
	 * @return A new XMLGregorianCalendar instance.
	 */
	public static XMLGregorianCalendar newXMLGregorianCalendar()
	{
		return newXMLGregorianCalendar(null);
	}
}
