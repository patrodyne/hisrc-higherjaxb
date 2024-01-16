package org.example.notype;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;

import org.example.model.Version;
import org.junit.jupiter.api.Test;
import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file(s).
 */
public class NoTypeTest extends AbstractSamplesTest
{
	protected String getContextPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ObjectFactory.class.getPackage().getName());
		sb.append(":");
		sb.append(Version.class.getPackage().getName());
		return sb.toString();
	}
	
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		JAXBContext jaxbContext = createContext();

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object root = unmarshaller.unmarshal(sample);
		
		if ( root instanceof ParentElement )
		{
			ParentElement parentElement = (ParentElement) root;

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

			String parentElementXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				marshaller.marshal(parentElement, sw);
				parentElementXml = sw.toString();
			}
			getLogger().debug("ParentElement:\n\n{}", parentElementXml);
			assertNotNull(parentElement.getVersion());
			getLogger().info("Version: {}", parentElement.getVersion());
		}
		else if ( root instanceof SomeOtherElement )
		{
			SomeOtherElement someOtherElement = (SomeOtherElement) root;

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

			String someOtherElementXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				marshaller.marshal(someOtherElement, sw);
				someOtherElementXml = sw.toString();
			}
			getLogger().debug("SomeOtherElement:\n\n{}", someOtherElementXml);
			assertNotNull(someOtherElement.getVersion());
			getLogger().info("Version: {}", someOtherElement.getVersion());
		}
		else
			fail("Object is not a known root: " + root);
	}
	
	@Test
	public void testVersion()
	{
		Version version = new Version("5.1");
		assertEquals("5", version.getMajor(), "major");
		assertEquals("1", version.getMinor(), "minor");
		assertEquals("5.1", version.toString(), "string");
	}
}
