package org.jvnet.higherjaxb.mojo.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbBaseMojo;

public class AddIfExistsToEpisodeSchemaBindingsTest {

	@Test
	public void transformationResourceIsAccessible() {
		InputStream is = AbstractHigherjaxbBaseMojo.class
				.getResourceAsStream(AbstractHigherjaxbBaseMojo.ADD_IF_EXISTS_TO_EPISODE_SCHEMA_BINDINGS_TRANSFORMATION_RESOURCE_NAME);
		assertNotNull(is);
		IOUtil.close(is);
	}
}
