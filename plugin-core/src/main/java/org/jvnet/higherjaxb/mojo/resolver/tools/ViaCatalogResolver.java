package org.jvnet.higherjaxb.mojo.resolver.tools;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;
import org.xml.sax.InputSource;

/**
 * A CatalogResolver to parse a catalog uri for a {@code via} scheme.
 * The {@code via} scheme has the form:
 *
 * <pre>
 * via:scheme:/scheme-specific-path
 * </pre>
 *
 */
public class ViaCatalogResolver
	extends AbstractCatalogResolver
{
	private ClasspathCatalogResolver classpathCatalogResolver;
	public ClasspathCatalogResolver getClasspathCatalogResolver()
	{
		return classpathCatalogResolver;
	}
	public void setClasspathCatalogResolver(ClasspathCatalogResolver classpathCatalogResolver)
	{
		this.classpathCatalogResolver = classpathCatalogResolver;
	}

	private MavenCatalogResolver mavenCatalogResolver;
	public MavenCatalogResolver getMavenCatalogResolver()
	{
		return mavenCatalogResolver;
	}
	public void setMavenCatalogResolver(MavenCatalogResolver mavenCatalogResolver)
	{
		this.mavenCatalogResolver = mavenCatalogResolver;
	}

	/**
	 * Construct with a collection of a {@link ClasspathCatalogResolver} instance,
	 * a {@link MavenCatalogResolver} instance, a {@link CatalogFeatures}, and
	 * uri(s) to one or more catalogs and default logger.
	 *
	 * @param mcr A {@link MavenCatalogResolver} instance.
	 * @param ccr A {@link ClasspathCatalogResolver} instance.
	 * @param features A collection of {@link CatalogFeatures}.
	 * @param uris The uri(s) to one or more catalogs
	 */
	public ViaCatalogResolver(MavenCatalogResolver mcr, ClasspathCatalogResolver ccr,
		CatalogFeatures features, URI... uris)
	{
		this(mcr, ccr, NullLog.INSTANCE, features, uris);
	}

	/**
	 * Construct with a {@link ClasspathCatalogResolver} instance,
	 * a {@link MavenCatalogResolver} instance, a {@link Log} instance,
	 * a collection of {@link CatalogFeatures} and uri(s) to one or more
	 * catalogs.
	 *
	 * @param mcr A {@link MavenCatalogResolver} instance.
	 * @param ccr A {@link ClasspathCatalogResolver} instance.
	 * @param log Feedback from the {@link AbstractMojo}, using <code>Maven</code> channels.
	 * @param features A collection of {@link CatalogFeatures}.
	 * @param uris The uri(s) to one or more catalogs.
	 */
	public ViaCatalogResolver(MavenCatalogResolver mcr, ClasspathCatalogResolver ccr,
		Log log, CatalogFeatures features, URI... uris)
	{
		super(features, uris);
		setMavenCatalogResolver(mcr);
		setClasspathCatalogResolver(ccr);
		setLog(log != null ? log : NullLog.INSTANCE);
	}

	/**
     * Implements {@link org.xml.sax.EntityResolver}. The method searches through
     * the catalog entries in the primary and alternative catalogs to attempt to
     * resolve the custom {@code via} scheme.
	 *
	 * <p>First, if the initial systemId is not a {@code via} scheme then attempt to
	 * resolve the <code>systemId</code> from the delegate resolver.
	 * </p>
	 *
	 * <p>Second, if the <code>systemId</code> or the delegate result matches the
	 * {@code via} scheme then the scheme specific part is used to resolve the
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
		// Avoid URI parse exception
		if ( systemId == null )
			systemId = "";

		InputSource inputSource = new InputSource();
		inputSource.setPublicId(publicId);
		inputSource.setSystemId(systemId);
		inputSource.setEncoding(UTF_8.name());

		getLog().debug(format("Resolving publicId [%s], systemId [%s].",
			inputSource.getPublicId(), inputSource.getSystemId()));

		try
		{
			// First, if the initial systemId is not a "via:" scheme then
			// attempt to resolve the systemId from delegate instance method.
			URI systemIdURI = new URI(inputSource.getSystemId());
			if ( !URI_SCHEME_VIA.equals(systemIdURI.getScheme()) )
			{
				InputSource delegateSource = null;

				if ( URI_SCHEME_MAVEN.equals(systemIdURI.getScheme()))
					delegateSource = getMavenCatalogResolver().resolveEntity(publicId, systemId);
				else if ( URI_SCHEME_CLASSPATH.equals(systemIdURI.getScheme()))
					delegateSource = getClasspathCatalogResolver().resolveEntity(publicId, systemId);
				else
					delegateSource = delegateResolveEntity(inputSource.getPublicId(), inputSource.getSystemId());

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

			// Second, if the systemId matches "via:" scheme then examine the resource.
			if ( URI_SCHEME_VIA.equals(systemIdURI.getScheme()) )
			{
				getLog().debug(format("Resolving systemId [%s] as via resource.", inputSource.getSystemId()));
				String schemeSpecificPart = systemIdURI.getSchemeSpecificPart();
				getLog().debug("Resolving path [" + schemeSpecificPart + "].");

				// Recursively resolve entity via scheme specific part as systemId.
				InputSource resolveSource = resolveEntity(inputSource.getPublicId(), schemeSpecificPart);
				if ( resolveSource.getByteStream() != null )
				{
					inputSource = resolveSource;
					getLog().debug(format("RESOLVED systemId [%s] to [%s].",
						systemId, resolveSource.getSystemId()));
				}
				else
					inputSource.setSystemId(resolveSource.getSystemId());
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

}
