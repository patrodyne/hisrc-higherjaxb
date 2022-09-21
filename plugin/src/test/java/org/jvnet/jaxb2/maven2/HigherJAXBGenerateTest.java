package org.jvnet.jaxb2.maven2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.maven.project.MavenProject;
import org.jvnet.mjiip.v_2.XJC2Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HigherJAXB Mojo Generate Test.
 */
public class HigherJAXBGenerateTest
{
	protected Logger log = LoggerFactory.getLogger(HigherJAXBGenerateTest.class);

	@Test
	public void testExecute() throws Exception
	{
		final XJC2Mojo mojo = new XJC2Mojo();
		
		mojo.setProject(new MavenProject());
		mojo.setSchemaDirectory(getSchemaDirectory());
		mojo.setGenerateDirectory(getGeneratedDirectory());
		mojo.setGeneratePackage("org.jvnet.higherjaxb.test.po");
		mojo.setArgs(getArgs());
		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setForceRegenerate(true);
		
		mojo.execute();
	}

	private File baseDir;
	private File getBaseDir()
	{
		try
		{
			if ( baseDir == null )
			{
				baseDir = (new File(getClass().getProtectionDomain().getCodeSource()
					.getLocation().toURI())).getParentFile().getParentFile()
					.getAbsoluteFile();
			}
			return baseDir;
		}
		catch (Exception ex)
		{
			throw new AssertionError(ex);
		}
	}

	private File getSchemaDirectory()
	{
		return new File(getBaseDir(), "src/test/resources");
	}

	private File getGeneratedDirectory()
	{
		return new File(getBaseDir(), "target/generated-sources/xjc");
	}

	private List<String> getArgs()
	{
		List<String> args = new ArrayList<>();
		args.add("-XtoString");
		args.add("-Xequals");
		args.add("-XhashCode");
		args.add("-Xcopyable");
		return args;
	}
}
