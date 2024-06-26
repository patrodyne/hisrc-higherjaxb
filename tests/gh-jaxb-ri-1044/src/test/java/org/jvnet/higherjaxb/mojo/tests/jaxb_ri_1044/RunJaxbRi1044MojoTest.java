package org.jvnet.higherjaxb.mojo.tests.jaxb_ri_1044;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.ResourceEntry;
import org.jvnet.higherjaxb.mojo.resolver.tools.MavenCatalogResolver;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

public class RunJaxbRi1044MojoTest extends AbstractMojoTest
{
	@Test
	public void testExecute() throws Exception
	{
		final ResourceEntry a_xsd = new ResourceEntry();
		a_xsd.setUrl("http://www.ab.org/a.xsd");
		getLogger().debug("ResourceEntry: " + a_xsd);
		
		HigherjaxbMojo mojo = new HigherjaxbMojo();
		mojo.setLog(new SLF4JLogger(getLogger()));

		mojo.getRemoteRepos().add(REMOTE_REPOSITORY);
		mojo.setRepoSession(REPOSITORY_SYSTEM_SESSION);
		mojo.setRepoSystem(repositorySystem);
		
		mojo.setProject(createMavenProject());
		mojo.setCatalogResolver(MavenCatalogResolver.class.getName());
		mojo.setCatalog(fullpath("src/main/resources/catalog.cat"));
		mojo.setStrict(false);

		mojo.setSchemaDirectory(fullpath("src/main/resources"));
		mojo.setSchemaIncludes(new String[] {});
		mojo.setSchemas(new ResourceEntry[] { a_xsd });

		mojo.setGenerateDirectory(fullpath("target/generated-sources/xjc")); 

		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setForceRegenerate(true);
		mojo.setNoFileHeader(true);
		mojo.setArgs(new ArrayList<>());
		
		mojo.execute();
	}
}
