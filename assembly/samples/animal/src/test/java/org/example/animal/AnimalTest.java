package org.example.animal;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;

import org.jvnet.basicjaxb.test.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file.
 */
public class AnimalTest extends AbstractSamplesTest
{
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		JAXBContext jaxbContext = createContext();

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object root = unmarshaller.unmarshal(sample);
		
		if ( root instanceof Animal )
		{
			Animal animal = (Animal) root;

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

			String animalXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				marshaller.marshal(animal, sw);
				animalXml = sw.toString();
			}
			getLogger().debug("Animal:\n\n" + animalXml);
		}
		else
			fail("Object is not instance of Animal: " + root);
	}
}
