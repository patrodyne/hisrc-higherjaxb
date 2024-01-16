package com.models;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import com.models.request.Request;
import com.models.response.BCResponse;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file.
 */
public class PublisherTest extends AbstractSamplesTest
{
	/**
	 * Configure the JAXB context path in AbstractSamplesTest
	 * to include all classes from two packages.
	 */
	@Override
	protected String getContextPath()
	{
		return "com.models.request:com.models.response";
	}
	
	/**
	 * Override the test method in AbstractSamplesTest to read
	 * this project's sample files and assert expectations.
	 */
	@Override
	protected void checkSample(File sample)	throws Exception
	{
		JAXBContext jaxbContext = createContext();

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object root = unmarshaller.unmarshal(sample);
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

		if ( root instanceof Request )
		{
			Request request = (Request) root;
			String requestXml = marshal(marshaller, request);
			getLogger().debug("Request:\n\n" + requestXml);
			assertNotNull(request.getLegalEntity(), "LegalEntity expected.");
			assertNotNull(request.getLegalEntity().getPartyId(), "PartyId expected.");
		}
		else if ( root instanceof BCResponse )
		{
			BCResponse bcResponse = (BCResponse) root;
			String bcResponseXml = marshal(marshaller, bcResponse);
			getLogger().debug("BCResponse:\n\n" + bcResponseXml);
			assertNotNull(bcResponse.getPublisher(), "Publisher expected.");
			assertNotNull(bcResponse.getContent(), "Content expected.");
			assertNotNull(bcResponse.getContent().getResponse(), "Response expected.");
			assertNotNull(bcResponse.getContent().getResponse().getResponseStatus(), "ResponseStatus expected.");
			assertNotNull(bcResponse.getContent().getResponse().getResponseStatus().getStatusType(), "StatusType expected.");
		}
		else
			fail("Object is not instance of Request or BCResponse: " + root);
	}

	// Marshall a JAXB object into a String for logging, etc.
	private String marshal(Marshaller marshaller, Object jaxbObject)
		throws JAXBException, IOException
	{
		String xml;
		try ( StringWriter sw = new StringWriter() )
		{
			marshaller.marshal(jaxbObject, sw);
			xml = sw.toString();
		}
		return xml;
	}
}
