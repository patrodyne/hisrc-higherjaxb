package org.jvnet.higherjaxb.mojo.test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract test for plugins.
 * 
 * @author Aleksei Valikov
 */

public class RunHigherjaxbMojo
{
	/**
	 * Logger.
	 */
	protected Logger log = LoggerFactory.getLogger(RunHigherjaxbMojo.class);

	@Test
	public void testExecute() throws Exception
	{
		final Mojo mojo = initMojo();
		mojo.execute();
	}

	protected AbstractHigherjaxbParmMojo<?> initMojo()
	{
		final AbstractHigherjaxbParmMojo<?> mojo = createMojo();
		configureMojo(mojo);
		return mojo;
	}
	
	protected AbstractHigherjaxbParmMojo<?> createMojo()
	{
		return new HigherjaxbMojo();
	}
	
	protected void configureMojo(final AbstractHigherjaxbParmMojo<?> mojo)
	{
		mojo.setProject(new MavenProject());
		mojo.setSchemaDirectory(getSchemaDirectory());
		mojo.setGenerateDirectory(getGenerateDirectory());
		mojo.setGeneratePackage(getGeneratePackage());
		mojo.setArgs(getArgs());
		mojo.setVerbose(isVerbose());
		mojo.setDebug(isDebug());
		mojo.setWriteCode(isWriteCode());
		
		mojo.setRemoveOldOutput(true);
		mojo.setForceRegenerate(true);
		mojo.setExtension(true);
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
