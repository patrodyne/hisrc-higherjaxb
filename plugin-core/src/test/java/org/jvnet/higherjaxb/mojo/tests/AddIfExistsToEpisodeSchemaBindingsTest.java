package org.jvnet.higherjaxb.mojo.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.jvnet.higherjaxb.mojo.AbstractHigherjaxbBaseMojo.ADD_IF_EXISTS_TO_EPISODE_SCHEMA_BINDINGS_TRANSFORMATION_RESOURCE_NAME;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbBaseMojo;

public class AddIfExistsToEpisodeSchemaBindingsTest
{
	@Test
	public void transformationResourceIsAccessible()
	{
		try ( InputStream is = getResourceAsStream(ADD_IF_EXISTS_TO_EPISODE_SCHEMA_BINDINGS_TRANSFORMATION_RESOURCE_NAME) )
		{
			assertNotNull(is);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private InputStream getResourceAsStream(String name)
	{
		return AbstractHigherjaxbBaseMojo.class.getResourceAsStream(name);
	}
}
