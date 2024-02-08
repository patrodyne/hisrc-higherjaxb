package com.workday.bsvo;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import com.workday.Root;

/**
 * A JUnit test to check a sample XML file.
 */
public class WorkdayTest extends AbstractSamplesTest
{
	protected String getContextPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ObjectFactory.class.getPackage().getName());
		sb.append(":");
		sb.append(Root.class.getPackage().getName());
		return sb.toString();
	}
	
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		JAXBContext jaxbContext = createContext();

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object rootObject = unmarshaller.unmarshal(sample);
		
		if ( rootObject instanceof Root )
		{
			Root root = (Root) rootObject;

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

			String rootXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				marshaller.marshal(root, sw);
				rootXml = sw.toString();
			}
			getLogger().debug("Workday Root:\n\n" + rootXml);
		}
		else
			fail("Object is not instance of Root: " + rootObject);
	}
}
