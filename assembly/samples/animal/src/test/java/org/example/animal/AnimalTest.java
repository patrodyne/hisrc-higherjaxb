package org.example.animal;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

/**
 * A JUnit test to check a sample XML file.
 */
public class AnimalTest extends AbstractSamplesTest
{
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		Object root = getUnmarshaller().unmarshal(sample);
		
		if ( root instanceof Animal )
		{
			Animal animal = (Animal) root;

			String animalXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				getMarshaller().marshal(animal, sw);
				animalXml = sw.toString();
			}
			getLogger().info("Animal:\n\n" + animalXml);
		}
		else
			fail("Object is not instance of Animal: " + root);
	}
}
