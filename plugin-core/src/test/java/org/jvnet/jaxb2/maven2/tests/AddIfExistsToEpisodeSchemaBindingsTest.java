package org.jvnet.jaxb2.maven2.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.junit.jupiter.api.Test;
import org.jvnet.jaxb2.maven2.RawXJC2Mojo;

public class AddIfExistsToEpisodeSchemaBindingsTest {

	@Test
	public void transformationResourceIsAccessible() {
		InputStream is = RawXJC2Mojo.class
				.getResourceAsStream(RawXJC2Mojo.ADD_IF_EXISTS_TO_EPISODE_SCHEMA_BINDINGS_TRANSFORMATION_RESOURCE_NAME);
		assertNotNull(is);
		IOUtil.close(is);
	}
}
