package org.jvnet.higherjaxb.mojo.tests.jaxb_ri_1044;

import java.io.File;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.ResourceEntry;
import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

public class RunJaxbRi1044Mojo extends RunHigherjaxbMojo {

	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo mojo) {
		super.configureMojo(mojo);

		final ResourceEntry a_xsd = new ResourceEntry();
		a_xsd.setUrl("http://www.ab.org/a.xsd");
		mojo.setStrict(false);
		mojo.setSchemaIncludes(new String[] {});
		mojo.setSchemas(new ResourceEntry[] { a_xsd });
		mojo.setCatalog(new File(getBaseDir(), "src/main/resources/catalog.cat"));
		mojo.setForceRegenerate(true);
	}

}
