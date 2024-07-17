package org.jvnet.higherjaxb.mojo.resolver.tools;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;
import org.xml.sax.InputSource;

/**
 * A CatalogResolver to parse a catalog uri for a "classpath" scheme.
 * The "classpath" scheme has the form:
 * 
 * <pre>
 * classpath:/resource/path/in/jar/schema.xsd
 * </pre>
 * 
 */
public class ClasspathCatalogResolver 
	extends AbstractCatalogResolver
{
	private ClassLoader classloader;
	public ClassLoader getClassloader()
	{
		return classloader;
	}
	public void setClassloader(ClassLoader classloader)
	{
		this.classloader = classloader;
	}
	
	/**
	 * Construct with the thread context {@link ClassLoader} and default logger.
	 * 
	 * @param features A collection of {@link CatalogFeatures}.
	 * @param uris The uri(s) to one or more catalogs
	 */
	public ClasspathCatalogResolver(CatalogFeatures features, URI... uris)
	{
		this(currentThread().getContextClassLoader(), NullLog.INSTANCE, features, uris);
	}

	/**
	 * Construct with a {@link ClassLoader} and a {@link Log} instance.
	 * 
	 * #param classLoader Object responsible for loading classes and reading resources.
	 * @param log Feedback from the {@link AbstractMojo}, using <code>Maven</code> channels.
	 * @param features A collection of {@link CatalogFeatures}.
	 * @param uris The uri(s) to one or more catalogs
	 */
	public ClasspathCatalogResolver(ClassLoader classLoader, Log log,
		CatalogFeatures features, URI... uris)
	{
		super(features, uris);
		setClassloader(classLoader);
		setLog(log != null ? log : NullLog.INSTANCE);
	}
	
	/**
     * Implements {@link org.xml.sax.EntityResolver}. The method searches through
     * the catalog entries in the primary and alternative catalogs to attempt to
     * resolve the custom "classpath:" scheme.
     *
	 * <p>First, if the <code>systemId</code> matches the "classpath:" scheme then a
	 * {@link ClassLoader} is used to resolve the <code>systemId</code>
	 * to a {@link URI} representing a local resource location; otherwise, the resolved
	 * <code>systemId</code> is returned, as an {@link InputSource} property.</p>
	 * 
	 * <p>Second, if a "classpath:" scheme cannot be resolved, attempt to resolve the
	 * <code>systemId</code> from the delegate instance method, as an
	 * {@link InputSource} property.</p>
	 * 
	 * <p>First, if the initial systemId is not a "classpath:" scheme then attempt to
	 * resolve the <code>systemId</code> from the delegate resolver.
	 * </p>
	 * 
	 * <p>Second, if the <code>systemId</code> or the delegate result matches the
	 * "classpath:" scheme then a {@link ClassLoader} is used to resolve the
	 * <code>systemId</code> to a {@link URI} representing a local resource location;
	 * otherwise, the resolved <code>systemId</code> is returned.
	 * </p>
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
			// First, if the initial systemId is not a "classpath:" scheme then
			// attempt to resolve the systemId from delegate instance method.
			URI systemIdURI = new URI(inputSource.getSystemId());
			if ( !URI_SCHEME_CLASSPATH.equals(systemIdURI.getScheme()) )
			{
				// The delegate result may resolve to a "classpath:", etc. scheme when
				// the delegate resolves catalog entries!
				InputSource delegateSource = delegateResolveEntity(inputSource.getPublicId(), inputSource.getSystemId());
				if ( delegateSource != null )
				{
					if ( delegateSource.getByteStream() != null )
					{
						inputSource = delegateSource;
						getLog().debug(format("RESOLVED systemId [%s] to [%s].",
							inputSource.getSystemId(), delegateSource.getSystemId()));
					}
					else
					{
						getLog().debug(format("Resolving delegate systemId [%s] result.",
							inputSource.getSystemId()));
						
						// Attempt to resolve the systemIdURI as a file.
						systemIdURI = new URI(delegateSource.getSystemId());
			
						if ( URI_SCHEME_FILE.equals(systemIdURI.getScheme()))
							delegateSource = resolveFileStream(systemIdURI, delegateSource);
						
						if ( inputSource.getByteStream() != null )
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
			
			// Second, if the systemId matches "classpath:" scheme then examine the resource.
			if ( URI_SCHEME_CLASSPATH.equals(systemIdURI.getScheme()) )
			{
				getLog().debug(format("Resolving systemId [%s] as classpath resource.", inputSource.getSystemId()));
				String schemeSpecificPart = systemIdURI.getSchemeSpecificPart();
				getLog().debug("Resolving path [" + schemeSpecificPart + "].");
				
				try
				{
					// Resolve a resource from the classpath.
					final URL resourceIdURL = resolveResource(schemeSpecificPart, null);
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
						getLog().warn(format("NOT RESOLVED Classpath [%s]\n\tReturning delegate resolver result [%s].",
							systemIdURI, inputSource.getSystemId()));
					}
				}
				catch (IllegalArgumentException | IOException ex)
				{
					getLog().warn(format(
						"Failed to resolve location [%s] as '%s:' because %s.\n\tReturning delegate resolver result [%s].",
						systemIdURI, URI_SCHEME_CLASSPATH, ex.getMessage(), inputSource.getSystemId()));
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
	 * Resolve a locator to an {@link URI}. The locator may represent a
	 * URL or a File. If the URL protocol is "classpath:" then the URI
	 * will be located as a resource.
	 * 
	 * If the class parameter is null, a {@link ClassLoader} is used to
	 * resolve classpath resources; thus, a "classpath:" locator must
	 * provide the full path relative to the classpath root and any
	 * leading '/' will be ignored.
	 * 
	 * @param resource The location of a file or resource.
	 * @param clazz A classpath location for relative locators.
	 * 
	 * @return A {@link URL} representing the locator.
	 */
	private URL resolveResource(String resource, Class<?> clazz)
	{
		URL resourceURL = null;
		try
		{
			if ( resource != null )
			{
				if ( clazz != null )
					resourceURL = clazz.getResource(resource);	
				else
				{
					if ( resource.startsWith("/") )
						resource = resource.substring(1);
					resourceURL = getClassloader().getResource(resource);
				}

				if ( resourceURL != null )
					getLog().info(format("RESOLVED: '%s' to '%s'", resource, resourceURL));
				else
					getLog().warn(format("NOT RESOLVED: '%s'", resource));
			}
			else
				getLog().warn("resolveLocator: locator is null");
		}
		catch (Exception ex)
		{
			getLog().warn(ex.getClass().getSimpleName() + ": cannot resolve " + resource);
		}
		return resourceURL;
	}
}
