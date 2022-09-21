package org.jvnet.jaxb2.maven2.test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.jvnet.jaxb2.maven2.AbstractXJC2Mojo;
import org.jvnet.mjiip.v_2.XJC2Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract test for plugins.
 * 
 * @author Aleksei Valikov
 */

public class RunXJC2Mojo
{
	/**
	 * Logger.
	 */
	protected Logger log = LoggerFactory.getLogger(RunXJC2Mojo.class);

	@Test
	public void testExecute() throws Exception
	{
		final Mojo mojo = initMojo();
		mojo.execute();
	}

	protected AbstractXJC2Mojo initMojo()
	{
		final AbstractXJC2Mojo mojo = createMojo();
		configureMojo(mojo);
		return mojo;
	}
	
	protected AbstractXJC2Mojo createMojo()
	{
		return new XJC2Mojo();
	}
	
	protected void configureMojo(final AbstractXJC2Mojo mojo)
	{
		mojo.setProject(new MavenProject());
		mojo.setSchemaDirectory(getSchemaDirectory());
		mojo.setGenerateDirectory(getGenerateDirectory());
		mojo.setGeneratePackage(getGeneratePackage());
		mojo.setArgs(getArgs());
		mojo.setVerbose(isVerbose());
		mojo.setDebug(isDebug());
		mojo.setWriteCode(isWriteCode());
	}
	
	public void check() throws Exception
	{
	}

	public List<String> getArgs()
	{
		return Collections.emptyList();
	}

	public String getGeneratePackage()
	{
		return null;
	}

	public boolean isDebug()
	{
		return true;
	}

	public boolean isVerbose()
	{
		return true;
	}

	public boolean isWriteCode()
	{
		return true;
	}

	public File getSchemaDirectory()
	{
		return getDirectory("src/main/resources");
	}
	
	public File getGenerateDirectory()
	{
		return getDirectory("target/generated-test-sources/xjc");
	}
	
	protected File getDirectory(String path)
	{
		return new File(getBaseDir(), path);
	}

	protected File getBaseDir()
	{
		try
		{
			return (new File(getClass().getProtectionDomain().getCodeSource()
				.getLocation().toURI())).getParentFile().getParentFile()
				.getAbsoluteFile();
		}
		catch (Exception ex)
		{
			throw new AssertionError(ex);
		}
	}
}
