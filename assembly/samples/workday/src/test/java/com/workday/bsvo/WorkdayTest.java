package com.workday.bsvo;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
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
			getLogger().debug("Workday Root 1:\n\n" + marshalToString(marshaller, root));
			
			marshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper", NAMESPACE_PREFIX_MAPPER);
			getLogger().debug("Workday Root 2:\n\n" + marshalToString(marshaller, root));
		}
		else
			fail("Object is not instance of Root: " + rootObject);
	}
	
	private String marshalToString(Marshaller marshaller, Root root)
		throws JAXBException, IOException
	{
		String rootXml;
		try ( StringWriter sw = new StringWriter() )
		{
			marshaller.marshal(root, sw);
			rootXml = sw.toString();
		}
		return rootXml;
	}

	public static final NamespacePrefixMapper NAMESPACE_PREFIX_MAPPER = new NamespacePrefixMapper()
	{
		@Override
		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
		{
			return "workday";
		}
	};
}
