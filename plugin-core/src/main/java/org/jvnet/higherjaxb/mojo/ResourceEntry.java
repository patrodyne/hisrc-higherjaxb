package org.jvnet.higherjaxb.mojo;

import java.net.URL;

import org.apache.maven.model.FileSet;
import org.apache.maven.model.PatternSet;

/**
 * Represents a resource for file sets, URLs, Maven artifact resources.
 * 
 * <p>A {@link ResourceEntry} contains configuration information for
 * bindings, schemas, etc.</p>
 * 
 * <p>A {@link FileSet} is a {@link PatternSet} of <em>include</em> or
 * <em>exclude</em> patterns for files.</p>
 * 
 * <p>A {@link URL} provides the location of a resource, schema, etc.</p>
 * 
 * <p>A {@link DependencyResource} is a Maven dependency with a system-id.
 */
public class ResourceEntry
{
	private FileSet fileset;
	public FileSet getFileset()
	{
		return fileset;
	}
	public void setFileset(FileSet fileset)
	{
		this.fileset = fileset;
	}

	private String url;
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}

	private DependencyResource dependencyResource;
	public DependencyResource getDependencyResource()
	{
		return dependencyResource;
	}
	public void setDependencyResource(DependencyResource dependencyResource)
	{
		this.dependencyResource = dependencyResource;
	}
	
	/**
	 * Construct with a {@link DependencyResource}.
	 * 
	 * @param dependencyResource A {@link DependencyResource}.
	 */
	public ResourceEntry(DependencyResource dependencyResource)
	{
		setDependencyResource(dependencyResource);
	}
	
	/**
	 * Construct with a {@link FileSet}.
	 * 
	 * @param fileSet A {@link FileSet}.
	 */
	public ResourceEntry(FileSet fileSet)
	{
		setFileset(fileSet);
	}

	/**
	 * Construct with a URL string.
	 * 
	 * @param url A URL string.
	 */
	public ResourceEntry(String url)
	{
		setUrl(url);
	}

	/**
	 * Default constructor.
	 */
	public ResourceEntry()
	{
		super();
	}
	
	@Override
	public String toString()
	{
		if (getFileset() != null)
			return getFileset().toString();
		else if (getUrl() != null)
			return "URL {" + getUrl().toString() + "}";
		else if (getDependencyResource() != null)
			return getDependencyResource().toString();
		else
			return "Empty resource entry {}";
	}
}