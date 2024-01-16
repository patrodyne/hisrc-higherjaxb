package org.example.element;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file.
 */
public class ElementTest extends AbstractSamplesTest
{
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		JAXBContext jaxbContext = createContext();

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object root = unmarshaller.unmarshal(sample);
		
		if ( root instanceof Element )
		{
			Element element = (Element) root;

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

			String elementXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				marshaller.marshal(element, sw);
				elementXml = sw.toString();
			}
			getLogger().debug("Element:\n\n" + elementXml);
			
			getLogger().debug("Element Content Size: [" + element.getContent().size() + "]");
			for ( int index=0; index < element.getContent().size(); ++index )
			{
				Object value = element.getContent().get(index);
				if ( value instanceof JAXBElement )
					value = ((JAXBElement<?>) value).getValue();
				getLogger().debug(index + ") Element Content Value Type: [" + value.getClass() + "]");
				getLogger().debug(index + ") Element Content Value: [" + value + "]");
			}
		}
		else
			fail("Object is not instance of Element: " + root);
	}
}
