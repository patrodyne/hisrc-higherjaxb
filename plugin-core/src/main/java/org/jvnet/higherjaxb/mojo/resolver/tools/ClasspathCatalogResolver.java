package org.jvnet.higherjaxb.mojo.resolver.tools;

import static java.lang.String.format;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.resolver.EntityKey;

/**
 * A CatalogResolver to parse a catalog uri for a "classpath" scheme.
 * The "classpath" scheme has the form:
 * 
 * <pre>
 * classpath:/resource/path/in/jar/schema.xsd
 * </pre>
 * 
 * Note: The resource path delimiter is <code>'!/'</code>.
 */
public class ClasspathCatalogResolver extends com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver
{
	public static final String URI_SCHEME_CLASSPATH = "classpath";

	private Log log;
	public Log getLog() { return log; }
	public void setLog(Log log) { this.log = log; }
	
	private Map<EntityKey, String> resolvedEntities;
	public Map<EntityKey, String> getResolvedEntities()
	{
		return resolvedEntities;
	}
	public void setResolvedEntities(Map<EntityKey, String> resolvedEntities)
	{
		this.resolvedEntities = resolvedEntities;
	}
	
	/** Constructor: default */
	public ClasspathCatalogResolver()
	{
		super();
		setResolvedEntities(new HashMap<>());
	}

	/**
	 * Construct with a {@link Log} instance.
	 * 
	 * @param log A {@link Log} instance.
	 */
	public ClasspathCatalogResolver(Log log)
	{
		this();
		setLog(log);
	}
	
	@Override
	public String getResolvedEntity(String publicId, String systemId)
	{
		EntityKey entityKey = new EntityKey(publicId, systemId);
		if ( getResolvedEntities().containsKey(entityKey) )
			return getResolvedEntities().get(entityKey);
		else
		{
			String resolvedEntity = resolveEntity(entityKey);
			if ( resolvedEntity != null )
				getResolvedEntities().put(entityKey, resolvedEntity);
			return resolvedEntity;
		}
	}
	
	private String resolveEntity(EntityKey entityKey)
	{
		String publicId = entityKey.getPublicId();
		String systemId = entityKey.getSystemId();

		getLog().debug("Resolving [" + publicId + "], [" + systemId + "].");
		final String result = super.getResolvedEntity(publicId, systemId);
		getLog().debug("Resolved to [" + result+ "].");
		
		if (result == null)
		{
			getLog().error(format("Could not resolve publicId [%s], systemId [%s]",
				 publicId, systemId));
			return null;
		}
		
		try
		{
			final URI uri = new URI(result);
			if (URI_SCHEME_CLASSPATH.equals(uri.getScheme()))
			{
				final String schemeSpecificPart = uri.getSchemeSpecificPart();
				getLog().debug("Resolve [" + schemeSpecificPart + "].");
				final URL resource = Thread.currentThread().getContextClassLoader().getResource(schemeSpecificPart);
				if (resource == null)
				{
					getLog().error("Returning [" + null + "].");
					return null;
				}
				else
				{
					getLog().debug("Returning to [" + resource.toString()+ "].");
					return resource.toString();
				}
			}
			else
			{
				getLog().debug("Returning to [" + result+ "].");
				return result;
			}
		}
		catch (URISyntaxException urisex)
		{
			getLog().debug("Returning to [" + result+ "].");
			return result;
		}
	}
}
