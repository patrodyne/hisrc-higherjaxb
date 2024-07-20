package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.resolver.tools.AbstractCatalogResolver;
import org.jvnet.higherjaxb.mojo.resolver.tools.ClasspathCatalogResolver;
import org.xml.sax.InputSource;

/**
 * A Classpath {@link URI} last modified resolver. 
 * 
 * <p>The "classpath" URI has the form:</p>
 * 
 * <pre>
 * classpath:resource
 * </pre>
 */
public class ClasspathURILastModifiedResolver
	extends	AbstractSchemeAwareURILastModifiedResolver
{
	public static final String SCHEME = "classpath";
	
	// Represents a CatalogResolver to parse a catalog uri for the "classpath:" scheme.
	// See AbstractHigherjaxbBaseMojo#createCatalogResolver()
	private ClasspathCatalogResolver classpathCatalogResolver;
	public ClasspathCatalogResolver getClasspathCatalogResolver()
	{
		return classpathCatalogResolver;
	}
	public void setClasspathCatalogResolver(ClasspathCatalogResolver classpathCatalogResolver)
	{
		this.classpathCatalogResolver = classpathCatalogResolver;
	}
	
	private URILastModifiedResolver parent;
	private URILastModifiedResolver getParent()
	{
		return parent;
	}
	public void setParent(URILastModifiedResolver parent)
	{
		this.parent = parent;
	}
	
	public ClasspathURILastModifiedResolver(AbstractCatalogResolver catalogResolver, Log logger,
		URILastModifiedResolver parent)
	{
		super(SCHEME, logger);
		setParent(requireNonNull(parent));
		if ( catalogResolver instanceof ClasspathCatalogResolver )
			setClasspathCatalogResolver((ClasspathCatalogResolver) catalogResolver);
	}
	
	@Override
	protected Long getLastModifiedForScheme(URI uri)
	{
		Long lastModified = null;
		
		try
		{
			if ( getClasspathCatalogResolver() != null )
			{
				String systemId = uri.toString();
				InputSource entitySource =
					getClasspathCatalogResolver().resolveEntity(null, systemId);
				String resolvedId = entitySource.getSystemId();
				
				getLogger().debug(format(
					"Retrieving the last modification timestamp of the URI [%s] via the resolved URI [%s].", uri, resolvedId));

				if ( !systemId.equals(resolvedId) )
					lastModified = getParent().getLastModified(new URI(resolvedId));
			}
			else
				getLogger().error(format("No ClasspathCatalogResolver is configured for URI [%s].", uri));
		}
		catch (Exception ex)
		{
			getLogger().error(format("Could not create a MavenURLHandler instance from URI [%s].", uri), ex);
		}
		
		if ( lastModified == null )
			getLogger().warn(format("Last modification of the URI [%s] is not known.", uri));
		
		return lastModified;
	}

	public String getMainURI(URI uri)
		throws MalformedURLException, URISyntaxException
	{
		return uri.getSchemeSpecificPart();
	}
}
