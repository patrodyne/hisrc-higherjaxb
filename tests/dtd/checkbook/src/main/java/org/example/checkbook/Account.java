package org.example.checkbook;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBException;

abstract public class Account implements Serializable
{
	private static final long serialVersionUID = 20240501L;
	// Represent a SLF4J logger for this class.
    protected static Logger logger = LoggerFactory.getLogger(Account.class);

	// Represents the CheckBook JAXBContext for this instance.
	private static final CBContext CB_CONTEXT = new CBContext(); 

	protected Transactions unmarshal(File file)
	{
		Transactions transactions = null;
		try
		{
			Object result = CB_CONTEXT.getUnmarshaller().unmarshal(file);
			if ( result instanceof Transactions )
				transactions =(Transactions) result;
			else
				logger.warn("Input stream is not a Transactions!");
		}
		catch (JAXBException ex)
		{
			logger.error("Input stream cannot be unmarshaled!", ex);
		}
		return transactions;
	}
	
	protected void marshal(File file)
	{
		marshal(this, file);
	}
	
	protected void marshal(Object element, File file)
	{
		try
		{
			CB_CONTEXT.getMarshaller().marshal(element, file);
		}
		catch (JAXBException ex)
		{
			logger.error("Element cannot be marshaled: {}", element, ex);
		}
	}
	
	public void validate()
	{
		// TODO Auto-generated method stub
	}
}
