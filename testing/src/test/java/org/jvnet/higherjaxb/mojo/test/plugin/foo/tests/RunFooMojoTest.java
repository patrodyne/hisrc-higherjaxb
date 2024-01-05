package org.jvnet.higherjaxb.mojo.test.plugin.foo.tests;

import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;
import org.jvnet.higherjaxb.mojo.testing.foo.FooMojo;

public class RunFooMojoTest extends AbstractMojoTest
{
	@Test
	public void testExecute() throws Exception
	{
		FooMojo mojo = new FooMojo();
		
		mojo.setLog(new SLF4JLogger(getLogger()));
		mojo.getRemoteRepos().add(REMOTE_REPOSITORY);
		mojo.setRepoSession(REPOSITORY_SYSTEM_SESSION);
		mojo.setRepoSystem(repositorySystem);
		mojo.setProject(createMavenProject());
		
		mojo.execute();
	}
}
