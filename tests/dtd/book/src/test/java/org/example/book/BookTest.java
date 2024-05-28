package org.example.book;

import static org.example.book.Main.CONTEXT;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBException;

public class BookTest extends AbstractSamplesTest
{
	@BeforeEach
	public void beforeEach() throws JAXBException
	{
		setJaxbContext(CONTEXT.getJaxbContext());
		setCatalogURI(CONTEXT.getCatalogURI());
	}
	
	@Override
	protected void checkSample(File sample) throws Exception
	{
		setFailFast(true);
		final Object object = unmarshal(sample);
		if ( object instanceof Book )
		{
			Book b1 = (Book) object;
			String b1xml = marshalToString(b1);
			getLogger().debug("B1: {}\n{}", b1, b1xml);
		}
	}
}
