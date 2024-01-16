package org.example.response;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;
import org.w3c.dom.Element;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample file. If the sample file represents
 * a <code>org.example.response.RESPONSE</code> element then its content
 * is examined.
 */
public class ContentTest extends AbstractSamplesTest
{
	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		JAXBContext jaxbContext = createContext();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object root = unmarshaller.unmarshal(sample);
		
		if ( root instanceof RESPONSE )
		{
			RESPONSE response = (RESPONSE) root;

			String responseXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				marshaller.marshal(response, sw);
				responseXml = sw.toString();
			}
			getLogger().debug("Response:\n\n" + responseXml);
			
			INFO info = response.getINFO();
			assertNotNull(info, "INFO is required");
			
			Object contentRaw = response.getCONTENT();
			if ( contentRaw != null )
			{
				if ( contentRaw instanceof Element )
					getLogger().debug("content interface: " + Element.class);
				
				String contentXml = null;
				try ( StringWriter sw = new StringWriter() )
				{
					JAXBElement<Object> contentJE = OBJECT_FACTORY.createCONTENT(contentRaw);
					marshaller.marshal(contentJE, sw);
					contentXml = sw.toString();
				}
				
				getLogger().debug("Content:\n\n" + contentXml);
				
				Class<?> contentType = contentType(contentXml);
				getLogger().debug("Content Type: " + contentType);
				
				if ( contentType != null )
				{
					String retagXml = retag(contentXml, contentType);
					getLogger().debug("Retag:\n\n" + retagXml);
					
					Object content = unmarshaller.unmarshal(new StringReader(retagXml));
					if ( content instanceof CONTENTAddress )
					{
						CONTENTAddress contentAddress = (CONTENTAddress) content;
						getLogger().debug("contentAddress: " + contentAddress);
						response.setCONTENT(contentAddress);
					}
					else if ( content instanceof CONTENTSalary )
					{
						CONTENTSalary contentSalary = (CONTENTSalary) content;
						getLogger().debug("contentSalary: " + contentSalary);
						response.setCONTENT(contentSalary);
					}
					else
						fail("Unmarshalled content is not " + contentType);
					
					// Log response after resolving content.
					getLogger().debug("response: " + response);
				}
			}
		}
		else
			fail("Object is not instance of RESPONSE: " + root);
	}

	private String retag(String contentXml, Class<?> contentType)
	{
		return contentXml.replaceAll("CONTENT>", contentType.getSimpleName() + ">");
	}

	private Class<?> contentType(String contentXml)
	{
		Class<?> contentType = null;
		
		Pattern CONTENTAddressPattern = Pattern.compile(".*<address.*", Pattern.DOTALL);
		Matcher CONTENTAddressMatcher = CONTENTAddressPattern.matcher(contentXml);
		
		Pattern CONTENTSalaryPattern = Pattern.compile(".*<salary.*", Pattern.DOTALL);
		Matcher CONTENTSalaryMatcher = CONTENTSalaryPattern.matcher(contentXml);
		
		if ( contentXml != null )
		{
			if ( CONTENTAddressMatcher.matches() )
				contentType = CONTENTAddress.class;
			else if ( CONTENTSalaryMatcher.matches() )
				contentType = CONTENTSalary.class;
		}
		return contentType;
	}
}
