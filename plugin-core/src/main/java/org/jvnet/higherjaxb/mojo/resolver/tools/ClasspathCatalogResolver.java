package org.jvnet.higherjaxb.mojo.resolver.tools;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.maven.plugin.logging.Log;

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

	@Override
	public String getResolvedEntity(String publicId, String systemId)
	{
		getLog().debug("Resolving [" + publicId + "], [" + systemId + "].");
		final String result = super.getResolvedEntity(publicId, systemId);
		getLog().debug("Resolved to [" + result+ "].");
		
		if (result == null)
		{
			getLog().error(MessageFormat.format(
				 "Could not resolve publicId [{0}], systemId [{1}]",
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
