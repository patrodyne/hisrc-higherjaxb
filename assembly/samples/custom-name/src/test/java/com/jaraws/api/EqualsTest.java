package com.jaraws.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;

import org.jvnet.basicjaxb.test.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

/**
 * A JUnit test to check a sample file. If the sample file represents
 * a com.jaraws.api.Vehicle element then its engine is verified for
 * the given vehicle number.
 */
public class EqualsTest extends AbstractSamplesTest
{
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		JAXBContext jaxbContext = createContext();
		Object obj = jaxbContext.createUnmarshaller().unmarshal(sample);
		if ( obj instanceof Vehicle )
		{
			Vehicle vehicle = (Vehicle) obj;
			try ( StringWriter vehicleXml = new StringWriter() )
			{
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(vehicle, vehicleXml);
				logger.info("Vehicle:\n\n" + vehicleXml);
			}
			switch ( vehicle.getNumber() )
			{
				case "KT12356": assertEquals("DIESEL", vehicle.getEngine()); break;
				default: fail("Unknown vehicle number: " + vehicle.getNumber()); break;
			}
		}
		else
			fail("Object is not instance of Vehicle: " + obj);
	}
}
