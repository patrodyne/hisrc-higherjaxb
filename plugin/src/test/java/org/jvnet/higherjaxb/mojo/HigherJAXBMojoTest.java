package org.jvnet.higherjaxb.mojo;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

public class HigherJAXBMojoTest extends AbstractMojoTest
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
		mojo.setGenerateDirectory(fullpath("target/generated-sources/xjc")); 
		mojo.setGeneratePackage("org.jvnet.higherjaxb.test.po");
		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setForceRegenerate(true);
		mojo.setNoFileHeader(true);
		mojo.setArgs(new ArrayList<>());
		mojo.getArgs().add("-XhashCode");
		mojo.getArgs().add("-Xequals");
		mojo.getArgs().add("-XtoString");
		
		mojo.execute();
	}
}
