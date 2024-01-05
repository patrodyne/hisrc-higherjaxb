package org.jvnet.higherjaxb.mojo.tests.rnc;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.resolver.tools.ClasspathCatalogResolver;

import com.sun.tools.xjc.reader.Ring;

public class RunRNCMojo extends AbstractMojoTest
{
	
	@Test
	public void testExecute() throws Exception
	{
		HigherjaxbMojo mojo = new HigherjaxbMojo();
		mojo.setLog(new SLF4JLogger(getLogger()));

		mojo.getRemoteRepos().add(remoteRepository);
		mojo.setRepoSession(repositorySystemSession);
		mojo.setRepoSystem(repositorySystem);
		
		mojo.setProject(createMavenProject());
		
		// final ResourceEntry a_xsd = new ResourceEntry();
		// a_xsd.setUrl("http://www.ab.org/a.xsd");
		
		mojo.setStrict(false);
		mojo.setSchemaLanguage("RELAXNG_COMPACT");
		mojo.setSchemaIncludes(new String[] { "*.rnc" });
		mojo.setGeneratePackage("foo");

		// mojo.setSchemas(new ResourceEntry[] { a_xsd });
		// mojo.setCatalog(fullpath("src/main/resources/catalog.cat"));
		
		mojo.setSchemaDirectory(fullpath("src/test/resources"));
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
