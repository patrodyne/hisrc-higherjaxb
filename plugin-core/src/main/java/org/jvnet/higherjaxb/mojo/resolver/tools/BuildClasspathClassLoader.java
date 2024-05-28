package org.jvnet.higherjaxb.mojo.resolver.tools;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;

public class BuildClasspathClassLoader extends ClassLoader
{
	private Log log = null;
	public Log getLog() { return log; }
	public void setLog(Log log) { this.log = log; }

	private File buildClasspath = null;
	public File getBuildClasspath() { return buildClasspath; }
	public void setBuildClasspath(File buildClasspath) { this.buildClasspath = buildClasspath; }

	public BuildClasspathClassLoader(ClassLoader parent, File buildClasspath, Log log)
	{
        super(parent);
        setBuildClasspath(buildClasspath);
        setLog(log);
    }

    @Override
    protected URL findResource(String resourcePath)
    {
		URL result = null;
    	File resourceFile = new File(getBuildClasspath(), resourcePath);
    	getLog().debug("FIND RESOURCE: " + resourceFile);
    	try
		{
    		if ( resourceFile.exists() )
    			if ( resourceFile.canRead() )
    				result = resourceFile.toURI().toURL();
    			else
    				getLog().warn("FIND RESOURCE: cannot read" + resourceFile);
    		else
    			getLog().warn("FIND RESOURCE: does not exist" + resourceFile);
		}
		catch (MalformedURLException ex)
		{
			throw new UncheckedIOException("cannot find resource: " + resourcePath, ex);
		}
    	return result;
    }
};
