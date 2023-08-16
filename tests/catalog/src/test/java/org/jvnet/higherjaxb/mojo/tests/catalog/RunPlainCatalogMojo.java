package org.jvnet.higherjaxb.mojo.tests.catalog;

import java.io.File;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherJaxbMojo;

public class RunPlainCatalogMojo extends RunHigherJaxbMojo
{
	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo<?> mojo)
	{
		super.configureMojo(mojo);
		mojo.setCatalog(new File(getBaseDir(), "src/main/resources/catalog.cat"));
		mojo.setForceRegenerate(true);
	}
}
