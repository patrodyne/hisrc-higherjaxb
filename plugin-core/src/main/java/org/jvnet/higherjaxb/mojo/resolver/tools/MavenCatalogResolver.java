package org.jvnet.higherjaxb.mojo.resolver.tools;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.DependencyResource;
import org.jvnet.higherjaxb.mojo.DependencyResourceResolver;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;
import org.xml.sax.InputSource;

/**
 * A CatalogResolver to parse a catalog URI for a {@code maven} scheme.
 * The {@code maven} scheme has the form:
 * 
 * <pre>
 * maven:groupId:artifactId:type:classifier:version!/resource/path/in/jar/schema.xsd
 * </pre>
 * 
 * Note: The resource path delimiter is <code>'!/'</code>.
 */
public class MavenCatalogResolver
	extends AbstractCatalogResolver
{
	private DependencyResourceResolver dependencyResourceResolver;
	public DependencyResourceResolver getDependencyResourceResolver()
	{
		return dependencyResourceResolver;
	}
	public void setDependencyResourceResolver(DependencyResourceResolver dependencyResourceResolver)
	{
		this.dependencyResourceResolver = dependencyResourceResolver;
	}
	
	/**
	 * Construct with a DependencyResourceResolver and NullLog.
	 * 
	 * @param dependencyResourceResolver An interface to resolve a dependency resource
	 * @param features A collection of {@link CatalogFeatures}.
	 * @param uris The uri(s) to one or more catalogs
	 * 
	 * @throws IllegalArgumentException if either the URIs are not absolute
     *         or do not have a URL protocol handler for the URI scheme.
     * @throws CatalogException If an error occurs while parsing the catalog.
     * @throws SecurityException if access to the resource is denied by the security manager
	 *
	 */
	public MavenCatalogResolver(DependencyResourceResolver dependencyResourceResolver,
		CatalogFeatures features, URI... uris)
	{
		this(dependencyResourceResolver, NullLog.INSTANCE, features, uris);
	}

	/**
	 * Construct with a DependencyResourceResolver and NullLog.
	 * 
	 * @param dependencyResourceResolver An interface to resolve a dependency resource
	 * @param log An interface for providing feedback to the user from the <code>Mojo</code>.
	 * @param features A collection of {@link CatalogFeatures}.
	 * @param uris The uri(s) to one or more catalogs
	 * 
	 * @throws IllegalArgumentException if either the URIs are not absolute
     *         or do not have a URL protocol handler for the URI scheme.
     * @throws CatalogException If an error occurs while parsing the catalog.
     * @throws SecurityException if access to the resource is denied by the security manager
	 *
	 */
	public MavenCatalogResolver(DependencyResourceResolver dependencyResourceResolver, Log log,
		CatalogFeatures features, URI... uris)
	{
		super(features, uris);
		if (dependencyResourceResolver == null)
			throw new IllegalArgumentException("Dependency resource resolver must not be null.");
		setDependencyResourceResolver(dependencyResourceResolver);
		setLog(log != null ? log : NullLog.INSTANCE);
	}
	
	/**
     * Implements {@link org.xml.sax.EntityResolver}. The method searches through
     * the catalog entries in the primary and alternative catalogs to attempt to
     * resolve the custom {@code maven} scheme.
     *
	 * <p>First, if the initial systemId is not a {@code maven} scheme then attempt to
	 * resolve the <code>systemId</code> from the delegate instance resolver.
	 * </p>
	 * 
	 * <p>Second, if the <code>systemId</code> or the delegate result matches the
	 * {@code maven} scheme then a {@link DependencyResourceResolver} is used to resolve the
	 * <code>systemId</code> to an <em>artifact resource URL</em> representing a directory
	 * or a JAR location; otherwise, the resolved <code>systemId</code> is returned, as an
	 * {@link InputSource} property.</p>
	 * 
	 * <p>Note: XML requires a system identifier on all external entities; thus,
	 *          <code>systemId</code> value is always specified.</p>
	 *
     * <p>If no mapping is found,</p>
     * 
     * <ul>
     * <li>If the {@code javax.xml.catalog.resolve} property is set to {@code continue},
     * returns null.</li>
     * <li>If the {@code javax.xml.catalog.resolve} property is set to {@code ignore} 
     * returns a {@link org.xml.sax.InputSource} object containing an empty
     * {@link java.io.Reader}.</li>
     * <li>If the {@code javax.xml.catalog.resolve} property is set to {@code strict},
     * throws a {@link CatalogException}.</li>
     * </ul>
     *
     * @param publicId The public identifier of the external entity being referenced,
     *                 or null if none was supplied
     *
     * @param systemId The system identifier of the external entity being referenced.
     *                 A system identifier is required on all external entities.
     *
     * @return a {@link org.xml.sax.InputSource} object if a mapping is found.
     * 
     * @throws CatalogException if no mapping is found and {@code javax.xml.catalog.resolve}
     *         is specified as {@code strict}.
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
	{
		InputSource inputSource = new InputSource();
		inputSource.setPublicId(publicId);
		inputSource.setSystemId(systemId);
		inputSource.setEncoding(UTF_8.name());
		
		getLog().debug(format("Resolving publicId [%s], systemId [%s].",
			inputSource.getPublicId(), inputSource.getSystemId()));

		try
		{
			// First, if the initial systemId is not a "maven:" scheme then
			// attempt to resolve the systemId from delegate instance method.
			URI systemIdURI = new URI(inputSource.getSystemId());
			if ( !URI_SCHEME_MAVEN.equals(systemIdURI.getScheme()) )
			{
				// The delegate result may resolve to a "maven:", etc. scheme when
				// the delegate resolves catalog entries!
				InputSource delegateSource = delegateResolveEntity(inputSource.getPublicId(), inputSource.getSystemId());
				if ( delegateSource != null )
				{
					if ( delegateSource.getByteStream() != null )
					{
						inputSource = delegateSource;
						getLog().debug(format("RESOLVED systemId [%s] to [%s].",
							systemId, delegateSource.getSystemId()));
					}
					else
					{
						getLog().debug(format("Resolving delegate systemId [%s] result.",
							delegateSource.getSystemId()));
						
						// Attempt to resolve the systemIdURI as a file.
						systemIdURI = new URI(delegateSource.getSystemId());
						
						if ( URI_SCHEME_FILE.equals(systemIdURI.getScheme()))
							delegateSource = resolveFileStream(systemIdURI, delegateSource);
						
						if ( delegateSource.getByteStream() != null )
						{
							inputSource = delegateSource;
							getLog().debug(format("RESOLVED systemId [%s] to [%s].",
								systemId, delegateSource.getSystemId()));
						}
						else
							inputSource.setSystemId(delegateSource.getSystemId());
					}
				}				
			}

			// Second, if the systemId matches "maven:" scheme then examine the resource.
			if (URI_SCHEME_MAVEN.equals(systemIdURI.getScheme()))
			{
				getLog().debug(format("Resolving systemId [%s] as Maven dependency resource.", inputSource.getSystemId()));
				String schemeSpecificPart = systemIdURI.getSchemeSpecificPart();
				getLog().debug("Resolving path [" + schemeSpecificPart + "].");
				
				try
				{
					// Resolve a dependency resource from a Maven dependency
					// such as an XML schema from a JAR artifact.
					final URL resourceIdURL = resolveResource(schemeSpecificPart);
					if ( resourceIdURL != null )
					{
						String resourceId = resourceIdURL.toString();
						inputSource.setPublicId(null);
						inputSource.setSystemId(resourceId);
						try ( InputStream resourceStream = resourceIdURL.openStream() )
						{
							inputSource.setByteStream(toByteArrayInputStream(resourceStream));
							getResolvedSources().put(inputSource.getSystemId(), inputSource);
						}
						getLog().info(format("RESOLVED systemId [%s] to [%s].", systemIdURI, resourceId));
					}
					else
					{
						getLog().warn(format("NOT RESOLVED Maven path [%s]\n\tReturning delegate resolver result [%s].",
							systemIdURI, inputSource.getSystemId()));
					}
				}
				catch (IllegalArgumentException | IOException ex)
				{
					getLog().warn(format(
						"Failed to resolve Maven path [%s] as dependency resource because %s.\n\tReturning delegate resolver result [%s].",
						systemIdURI, ex.getMessage(), inputSource.getSystemId()));
				}
			}
			else
			{
				getLog().debug(format(
					"SystemId [%s] is a URI with a '%s:' scheme.\n\tReturning delegate resolver result [%s].",
					systemIdURI, systemIdURI.getScheme(), inputSource.getSystemId()));
			}
		}
		catch (URISyntaxException ex)
		{
			getLog().warn(format(
				"Failed to parse systemId [%s] as a URL because %s.\n\tReturning delegate resolver result.",
				inputSource.getSystemId(), ex.getMessage()));
		}
		
		return inputSource;
	}
	
	/**
	 * Resolve a resource from a Maven dependency such as an XML schema
	 * from a JAR artifact.
	 * 
	 * @param resource A DependencyResource value to resolve.
	 * 
	 * @return A URL representing a dependency resource or null when the resource cannot be resolved.
	 */
	private URL resolveResource(String resource)
	{
		URL url = null;
		final DependencyResource dependencyResource = DependencyResource.valueOf(resource);
		try
		{
			if ((dependencyResource.getVersion()) != null && dependencyResource.getVersion().startsWith("!") )
				getLog().warn("Resource path may not be delimited properly with '!/'");
			url = getDependencyResourceResolver().resolveDependencyResource(dependencyResource);
		}
		catch (Exception ex)
		{
			getLog().error(format("Error resolving dependency resource [%s].", dependencyResource), ex);
		}
		return url;
	}
}
