package net.webservicex.test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;

import junit.framework.Assert;
import net.webservicex.GetWeather;

public class JAXBContextTest {

	public static final String CONTEXT_PATH = GetWeather.class.getPackage().getName();

	@Test
	public void successfullyCreatesMarshallerAndUnmarshaller() throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(CONTEXT_PATH);
		assertNotNull(context.createMarshaller());
		assertNotNull(context.createUnmarshaller());

	}

}
