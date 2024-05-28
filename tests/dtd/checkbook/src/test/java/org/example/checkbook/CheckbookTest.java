package org.example.checkbook;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBException;

public class CheckbookTest extends AbstractSamplesTest
{
	@BeforeEach
	public void beforeEach() throws JAXBException
	{
		CBContext cbContext = new CBContext();
		setJaxbContext(cbContext.getJaxbContext());
	}
	
	@Override
	protected void checkSample(File sample) throws Exception
	{
		final Object object = getUnmarshaller().unmarshal(sample);
		if ( object instanceof Checkbook )
		{
			Checkbook cb1 = (Checkbook) object;
			String cb1xml = marshalToString(cb1);
			getLogger().debug("CB1: {}\n{}", cb1, cb1xml);
		}
		else if ( object instanceof Transactions )
		{
			Transactions tx1 = (Transactions) object;
			String tx1xml = marshalToString(tx1);
			getLogger().debug("TX1: {}\n{}", tx1, tx1xml);
		}
	}
}
