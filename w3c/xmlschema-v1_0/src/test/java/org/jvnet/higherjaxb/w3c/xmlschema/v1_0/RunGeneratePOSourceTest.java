package org.jvnet.higherjaxb.w3c.xmlschema.v1_0;

import java.util.ArrayList;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

@Order(2)
public class RunGeneratePOSourceTest extends AbstractMojoTest
{
	@Test
	public void testExecute() throws Exception
	{
		HigherjaxbMojo mojo = new HigherjaxbMojo();
		mojo.setLog(new SLF4JLogger(getLogger()));

		mojo.getRemoteRepos().add(REMOTE_REPOSITORY);
		mojo.setRepoSession(REPOSITORY_SYSTEM_SESSION);
		mojo.setRepoSystem(repositorySystem);
		
		mojo.setProject(createMavenProject());
		mojo.setSchemaDirectory(fullpath("src/test/resources"));
		mojo.setSchemaIncludes(new String[] { "*.xsd" });
		mojo.setGenerateDirectory(fullpath("target/generated-test-sources/xjc")); 
		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setForceRegenerate(true);
		mojo.setNoFileHeader(true);
		mojo.setExtension(true);
		mojo.setArgs(new ArrayList<>());
		mojo.getArgs().add("-XfixedValue");
		mojo.getArgs().add("-XfluentAPI");
		mojo.getArgs().add("-XsimpleHashCode");
		mojo.getArgs().add("-XsimpleEquals");
		mojo.getArgs().add("-XsimpleToString");
		mojo.getArgs().add("-XsimpleToString-showFieldNames=true");
		mojo.getArgs().add("-XsimpleToString-showChildItems=false");
		mojo.getArgs().add("-XsimpleToString-fullClassName=false");
		
		mojo.execute();
	}
}
