package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.resolver.tools.AbstractCatalogResolver;
import org.jvnet.higherjaxb.mojo.resolver.tools.MavenCatalogResolver;
import org.xml.sax.InputSource;

/**
 * A Maven JAR {@link URI} last modified resolver. 
 * 
 * <p>The "maven" URI has the form:</p>
 * 
 * <pre>
 * maven:groupId:artifactId:type:classifier:version!/resource/path/in/jar/schema.xsd
 * </pre>
 */
public class MavenURILastModifiedResolver
	extends	AbstractSchemeAwareURILastModifiedResolver
{
	public static final String SCHEME = "maven";
	public static final String SEPARATOR = "!/";
	
	// Represents a CatalogResolver to parse a catalog uri for the "maven:" scheme.
	// See AbstractHigherjaxbBaseMojo#createMavenCatalogResolver()
	private MavenCatalogResolver mavenCatalogResolver;
	public MavenCatalogResolver getMavenCatalogResolver()
	{
		return mavenCatalogResolver;
	}
	public void setMavenCatalogResolver(MavenCatalogResolver mavenCatalogResolver)
	{
		this.mavenCatalogResolver = mavenCatalogResolver;
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
	
	public MavenURILastModifiedResolver(AbstractCatalogResolver catalogResolver, Log logger,
		URILastModifiedResolver parent)
	{
		super(SCHEME, logger);
		setParent(requireNonNull(parent));
		if ( catalogResolver instanceof MavenCatalogResolver )
			setMavenCatalogResolver((MavenCatalogResolver) catalogResolver);
	}
	
	@Override
	protected Long getLastModifiedForScheme(URI uri)
	{
		Long lastModified = null;
		
		try
		{
			if ( getMavenCatalogResolver() != null )
			{
				String systemId = uri.toString();
				InputSource entitySource = getMavenCatalogResolver().resolveEntity(null, systemId);
				String resolvedId = entitySource.getSystemId();
				
				getLogger().debug(format(
					"Retrieving the last modification timestamp of the URI [%s] via the resolved URI [%s].", uri, resolvedId));

				if ( !systemId.equals(resolvedId) )
					lastModified = getParent().getLastModified(new URI(resolvedId));
			}
			else
				getLogger().error(format("No MavenCatalogResolver is configured for URI [%s].", uri));
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
		final String uriString = uri.toString();
		final URL url;
		
		if ( uriString.indexOf(SEPARATOR) < 0 )
			url = new URI(uriString + SEPARATOR).toURL();
		else
			url = uri.toURL();
		
		final String spec = url.getFile();
		final int separatorPosition = spec.indexOf(SEPARATOR);
		
		if ( separatorPosition == -1 )
			throw new MalformedURLException(format("No [!/] found in url spec [%s].", spec));
		
		final String mainURIString = separatorPosition < 0 ? spec : spec.substring(0, separatorPosition);
		
		return mainURIString;
	}
}
