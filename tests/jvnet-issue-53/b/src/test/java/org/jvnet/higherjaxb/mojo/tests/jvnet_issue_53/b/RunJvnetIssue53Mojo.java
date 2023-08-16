package org.jvnet.higherjaxb.mojo.tests.jvnet_issue_53.b;

import java.io.File;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherJaxbMojo;

public class RunJvnetIssue53Mojo extends RunHigherJaxbMojo
{
	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo<?> mojo)
	{
		super.configureMojo(mojo);
		mojo.setCatalog(new File(getBaseDir(), "src/main/resources/catalog.cat"));
		mojo.setUseDependenciesAsEpisodes(true);
		mojo.setForceRegenerate(true);
	}
}
