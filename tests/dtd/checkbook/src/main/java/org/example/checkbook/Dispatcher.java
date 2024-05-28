package org.example.checkbook;

import static org.example.checkbook.CheckbookBalance.initializeBalance;

import java.io.File;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

public class Dispatcher
{
	// Represent a SLF4J logger for this class.
    protected static Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    
	// Represents the CheckBook JAXBContext for this instance.
	public final CBContext CB_CONTEXT = new CBContext(); 

	public void register(Class<Checkbook> checkbook, Class<CheckbookBalance> checkbookBalance)
	{
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("unchecked")
	public CheckbookBalance unmarshal(File file)
	{
		CheckbookBalance balance = null;
		try
		{
			StreamSource source = new StreamSource(file);
			Object result = CB_CONTEXT.getUnmarshaller().unmarshal(source, CheckbookBalance.class);
			if ( result instanceof JAXBElement )
				balance = ((JAXBElement<CheckbookBalance>) result).getValue();
			else if ( result instanceof CheckbookBalance )
				balance =(CheckbookBalance) result;
			else
				logger.warn("Input stream is not a CheckbookBalance!");
		}
		catch (JAXBException ex)
		{
			logger.error("Input stream cannot be CheckbookBalance!", ex);
		}
		balance.setBalance(initializeBalance(balance.getBalance()));
		logger.trace("CheckbookBalance: {}", balance.getBalance().getvalue());
		return balance;
	}
}
