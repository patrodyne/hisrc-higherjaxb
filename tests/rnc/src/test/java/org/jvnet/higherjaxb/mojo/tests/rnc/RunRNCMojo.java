package org.jvnet.higherjaxb.mojo.tests.rnc;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

import com.sun.tools.xjc.reader.Ring;


public class RunRNCMojo extends RunHigherjaxbMojo {

	@Override
	protected void configureMojo(AbstractHigherjaxbMojo mojo) {
		super.configureMojo(mojo);

		// final ResourceEntry a_xsd = new ResourceEntry();
		// a_xsd.setUrl("http://www.ab.org/a.xsd");
		mojo.setStrict(false);
		mojo.setSchemaLanguage("RELAXNG_COMPACT");
		mojo.setSchemaIncludes(new String[] { "*.rnc" });
		mojo.setGeneratePackage("foo");
		// mojo.setSchemas(new ResourceEntry[] { a_xsd });
		// mojo.setCatalog(new File(getBaseDir(),
		// "src/main/resources/catalog.cat"));
		mojo.setForceRegenerate(true);
//		Ring.begin();
	}

}
