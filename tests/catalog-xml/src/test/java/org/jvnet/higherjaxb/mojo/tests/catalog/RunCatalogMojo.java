package org.jvnet.higherjaxb.mojo.tests.catalog;

import java.io.File;

import org.jvnet.higherjaxb.mojo.resolver.tools.ClasspathCatalogResolver;
import org.jvnet.higherjaxb.mojo.test.RunHigherJaxbMojo;
import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;

public class RunCatalogMojo extends RunHigherJaxbMojo
{
	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo<?> mojo)
	{
		super.configureMojo(mojo);
		mojo.setCatalog(new File(getBaseDir(), "src/main/resources/catalog.cat"));
		mojo.setCatalogResolver(ClasspathCatalogResolver.class.getName());
		mojo.setForceRegenerate(true);
	}
}
