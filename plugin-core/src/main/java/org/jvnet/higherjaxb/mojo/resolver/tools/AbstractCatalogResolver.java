package org.jvnet.higherjaxb.mojo.resolver.tools;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jvnet.higherjaxb.mojo.AbstractHigherjaxbBaseMojo.CATALOGS_IN_STRICT_MODE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.catalog.Catalog;
import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.transform.Source;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.resolver.ResolveType;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

/**
 * Abstract implementation of {@link CatalogResolver}.
 *
 * <p>
 * This implementation delegates to an instance of {@link javax.xml.catalog.CatalogResolver}.
 * </p>
 */
abstract public class AbstractCatalogResolver implements CatalogResolver
{
	public static final String URI_SCHEME_FILE = "file";
	public static final String URI_SCHEME_CLASSPATH = "classpath";
	public static final String URI_SCHEME_MAVEN = "maven";
	public static final String URI_SCHEME_VIA = "via";

	private Log log;
	public Log getLog() { return log; }
	public void setLog(Log log) { this.log = log; }

	private CatalogFeatures catalogFeatures = null;
	public CatalogFeatures getCatalogFeatures()
	{
		if ( catalogFeatures == null )
			setCatalogFeatures(CatalogFeatures.defaults());
		return catalogFeatures;
	}
	public void setCatalogFeatures(CatalogFeatures catalogFeatures)
	{
		this.catalogFeatures = catalogFeatures;
	}

	private URI[] catalogFiles = null;
	public URI[] getCatalogFiles()
	{
		if ( catalogFiles == null )
			setCatalogFiles(new URI[0]);
		return catalogFiles;
	}
	public void setCatalogFiles(URI[] catalogFiles)
	{
		this.catalogFiles = catalogFiles;
	}

	private Catalog catalog = null;
	/**
	 * When <code>catalog</code> is not set, this accessor initializes
	 * the field using {@link CatalogManager} to create a {@link Catalog}
	 * instance with the configured features and files.
	 *
	 * <p>The {@link Catalog} instance loads the root catalog file and
	 * parses its entries.</p>
	 *
	 * <p>In addition to the above entry types, a catalog may define
	 * <code>nextCatalog</code> entries to add additional catalog entry
	 * files.</p>
	 *
	 * @return An instance of {@link Catalog} with a root catalog file and
	 * a list of catalog entries from that root.
	 *
	 * @throws IllegalArgumentException if either the URIs are not absolute
     *         or do not have a URL protocol handler for the URI scheme.
     * @throws CatalogException If an error occurs while parsing the catalog.
     * @throws SecurityException if access to the resource is denied by the security manager
	 *
	 */
	public Catalog getCatalog()
	{
		if ( catalog == null )
		{
			catalog = CatalogManager.catalog(getCatalogFeatures(), getCatalogFiles());
			if ( getLog().isDebugEnabled() )
			{
				getLog().debug("Catalog loaded: " + catalog);
				catalog.catalogs().forEach(cat -> getLog().debug("Alt Catalog loaded: " + cat));
			}
		}
		return catalog;
	}
	/**
	 * Set the {@link Catalog} field with the given instance.
	 *
	 * @param catalog A {@link Catalog} instance.
	 */
	public void setCatalog(Catalog catalog)
	{
		this.catalog = catalog;
	}

	private CatalogResolver delegator = null;
	public CatalogResolver getDelegator()
	{
		if ( delegator == null )
			setDelegator(createCatalogResolver());
		return delegator;
	}
	public void setDelegator(CatalogResolver delegator)
	{
		this.delegator = delegator;
	}

	private Map<String, InputSource> resolvedSources;
	public Map<String, InputSource> getResolvedSources()
	{
		if ( resolvedSources == null )
			setResolvedSources(new HashMap<>());
		return resolvedSources;
	}
	public void setResolvedSources(Map<String, InputSource> resolvedSources)
	{
		this.resolvedSources = resolvedSources;
	}

	/**
	 * Create a {@link CatalogResolver} using the current {@link Catalog} instance.
	 *
	 * <p>The {@link Catalog} instance contains entries loaded catalog file plus any
	 * <code>nextCatalog</code> entries from optional alternative catalog file(s).
	 * </p>
	 *
	 * @return An instance of {@link CatalogResolver}.
	 *
	 * @throws IllegalArgumentException if either the URIs are not absolute
     *         or do not have a URL protocol handler for the URI scheme.
     * @throws CatalogException If an error occurs while parsing the catalog.
     * @throws SecurityException if access to the resource is denied by the security manager
	 */
	public CatalogResolver createCatalogResolver()
	{
		return CatalogManager.catalogResolver(getCatalog());
	}

	/**
	 * Construct an instance of {@link CatalogResolver} with the default
	 * {@link CatalogFeatures} and catalog file {@link URI}s.
	 *
	 * <p>
     * The system property {@code javax.xml.catalog.files}, as defined in
     * {@link CatalogFeatures}, will be read to locate the initial list of
     * catalog files.
     * </p>
	 */
	public AbstractCatalogResolver()
	{
		super();
	}

	/**
	 * Construct an instance of {@link CatalogResolver} with the given
	 * {@link CatalogFeatures} and catalog file {@link URI}s.
	 *
	 * <p>
     * If {@code catalogURIs} is empty, system property {@code javax.xml.catalog.files},
     * as defined in {@link CatalogFeatures}, will be read to locate the initial
     * list of catalog files.
     * </p>
	 *
	 * @param features Holds a collection of features and properties.
	 * @param catalogURIs Identifies XML catalog files to be parsed.
	 */
	public AbstractCatalogResolver(CatalogFeatures features, URI... catalogURIs)
	{
		setCatalogFeatures(features);
		setCatalogFiles(catalogURIs);
	}

	@Override
	public Source resolve(String href, String base)
	{
		return getDelegator().resolve(href, base);
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
	{
		return delegateResolveEntity(publicId, systemId);
	}

	@Override
	public InputStream resolveEntity(String publicId, String systemId, String baseUri, String namespace)
	{
		return getDelegator().resolveEntity(publicId, systemId, baseUri, namespace);
	}

	@Override
	public LSInput resolveResource(String type, String namespaceUri, String publicId, String systemId, String baseUri)
	{
		return getDelegator().resolveResource(type, namespaceUri, publicId, systemId, baseUri);
	}

	/**
	 * Resolve XML entity by delegation,
	 *
	 * <p>First, prior to delegation, internally attempt resolution of local files.</p>
	 *
	 * <p>Second, if not resolved then attempt to resolve by delegation.</p>
	 *
     * @param publicId The public identifier of the external entity being
     *                 referenced, or null if none was supplied
     *
     * @param systemId The system identifier of the external entity being
     *                 referenced.
     *
     * @return A {@link InputSource} object if a mapping is found.
     *         If no mapping is found, returns a {@link InputSource} object
     *         containing an empty {@link Reader} if the {@code javax.xml.catalog.resolve}
     *         property is set to {@code ignore}; returns null if the
     *         {@code javax.xml.catalog.resolve} property is set to {@code continue}.
   	 */
	protected InputSource delegateResolveEntity(String publicId, String systemId)
	{
		InputSource inputSource = new InputSource();
		inputSource.setPublicId(publicId);
		inputSource.setSystemId(systemId);
		inputSource.setEncoding(UTF_8.name());

		// First, prior to delegation, internally resolve local files.
		try
		{
			// Attempt to resolve the systemIdURI as a file.
			URI systemIdURI = new URI(inputSource.getSystemId());
			if ( URI_SCHEME_FILE.equals(systemIdURI.getScheme()))
				inputSource = resolveFileStream(systemIdURI, inputSource);
		}
		catch (URISyntaxException ex)
		{
			getLog().warn(format(
				"Failed to parse systemId [%s] as a URL because %s.\n\tReturning delegate resolver result.",
				inputSource.getSystemId(), ex.getMessage()));
		}

		// The delegator result infers how it managed the ResolveType.
		// It its ResolveType is "strict" then it may throw an exception.
		ResolveType resolveType = ResolveType.UNKNOWN;

		// Second, if not resolved then attempt to resolve by delegation.
		if ( inputSource.getByteStream() == null )
		{
			InputSource delegateSource = null;
			try
			{
				delegateSource = getDelegator()
					.resolveEntity(inputSource.getPublicId(), inputSource.getSystemId());
			}
			catch ( CatalogException ex)
			{
				throw new CatalogException("\n"+CATALOGS_IN_STRICT_MODE, ex);
			}

			if ( delegateSource != null )
			{
				if ( delegateSource.getEncoding() == null )
					delegateSource.setEncoding(UTF_8.name());

				if ( delegateSource.getCharacterStream() != null )
				{
					if ( delegateSource.getCharacterStream() instanceof StringReader )
					{
						StringReader stringReader = (StringReader) delegateSource.getCharacterStream();
						if ( stringReader.toString().isEmpty() )
						{
							// An empty stringReader indicates "javax.xml.catalog.GroupEntry.ResolveType.IGNORE"
							resolveType = ResolveType.IGNORE;
						}
					}

					if ( ! ResolveType.IGNORE.equals(resolveType) )
					{
						if ( (delegateSource.getByteStream() == null) )
						{
							Reader reader = delegateSource.getCharacterStream();
							try
							{
								if ( reader instanceof FileReader )
								{
									String charset = ((FileReader) reader).getEncoding();
									if ( charset != null )
										delegateSource.setEncoding(charset);
								}

								ResetableStringReader resetableReader =
									toResetableStringReader(reader);
								delegateSource.setCharacterStream(resetableReader);

								ByteArrayInputStream inputStream = toByteArrayInputStream(resetableReader);
								if ( delegateSource.getByteStream() == null )
									delegateSource.setByteStream(inputStream);
							}
							catch ( IOException ex )
							{
								getLog().warn(format(
									"Failed to convert reader to stream because %s.\n\tReturning delegate resolver result [%s].",
									ex.getMessage(), delegateSource.getSystemId()));
							}
						}
					}
				}
				// Returning delegate resolver result.
				inputSource = delegateSource;
			}
			else
			{
				// A null delegateSource indicates "javax.xml.catalog.GroupEntry.ResolveType.CONTINUE"
				resolveType = ResolveType.CONTINUE;
			}
		}

		if ( inputSource.getByteStream() != null )
		{
			getResolvedSources().put(inputSource.getSystemId(), inputSource);
			getLog().info(format("RESOLVED systemId [%s] to [%s].", systemId, inputSource.getSystemId()));
		}
		else
		{
			if ( resolveType == ResolveType.CONTINUE )
			{
				getLog().debug(format("RESOLVE systemId [%s]"
					+ "\n\tReturning non-delegate resolver result [%s]."
					+ "\n\tFor ResolveType: [%s]",
					systemId, inputSource.getSystemId(), resolveType));

			}
			else
			{
				getLog().debug(format("RESOLVE systemId [%s]"
					+ "\n\tReturning delegate resolver result [%s]."
					+ "\n\tFor ResolveType: [%s]",
					systemId, inputSource.getSystemId(), resolveType));
			}
		}

		return inputSource;
	}

	protected InputSource resolveFileStream(URI systemIdURI, InputSource inputSource)
	{
		InputSource resolvedSource = getResolvedSources().get(inputSource.getSystemId());
		if ( resolvedSource == null )
		{
			// Build a file name from the systemId's URI instance.
			String systemFilename = (systemIdURI.getPath() != null)
				? systemIdURI.getPath()
				: systemIdURI.getSchemeSpecificPart();

			if ( systemFilename != null && (resolvedSource == null) )
			{
				File systemFile = new File(systemFilename);
				if ( systemFile.exists() )
				{
					if ( systemFile.canRead() )
					{
						try ( FileReader fileReader = new FileReader(systemFile) )
						{
							String charset = fileReader.getEncoding();
							if ( charset != null )
								inputSource.setEncoding(charset);
							ResetableStringReader resetableReader = toResetableStringReader(fileReader);
							inputSource.setCharacterStream(resetableReader);
							inputSource.setByteStream(toByteArrayInputStream(resetableReader));
							getResolvedSources().put(inputSource.getSystemId(), inputSource);
						}
						catch ( IOException ex)
						{
							getLog().warn(format(
								"Cannot read systemFile [%s] as '%s:' resource.\n\tReturning delegate resolver result.",
								systemFile, URI_SCHEME_FILE));
						}
					}
					else
					{
						getLog().warn(format(
							"Cannot read systemId [%s] as '%s:' resource.\n\tReturning delegate resolver result.",
							inputSource.getSystemId(), URI_SCHEME_FILE));
					}
				}
				else
				{
					getLog().warn(format(
						"Local systemId [%s] does not exist as '%s:' resource.\n\tReturning delegate resolver result.",
						inputSource.getSystemId(), URI_SCHEME_FILE));
				}

				// Default to original input source.
				resolvedSource = inputSource;
			}
		}

		// Return resolved or original source.
		return resolvedSource;
	}

	private ResetableStringReader toResetableStringReader(Reader reader) throws IOException
    {
		ResetableStringReader resetableReader = null;

		if ( reader instanceof ResetableStringReader )
			resetableReader = (ResetableStringReader) reader;
		else
		{
			char[] charBuffer = new char[8 * 1024];
		    StringBuilder sb = new StringBuilder();

		    int numCharsRead;
		    while ( (numCharsRead = reader.read(charBuffer, 0, charBuffer.length)) != -1 )
		        sb.append(charBuffer, 0, numCharsRead);

		    resetableReader = new ResetableStringReader(sb.toString());
		}

	    return resetableReader;
    }

	private ByteArrayInputStream toByteArrayInputStream(ResetableStringReader resetableReader) throws IOException
	{
		ByteArrayInputStream byteArrayInputStream = null;

		char[] charBuffer = new char[8 * 1024];
	    StringBuilder sb = new StringBuilder();

	    int numCharsRead;
	    while ( (numCharsRead = resetableReader.read(charBuffer, 0, charBuffer.length)) != -1 )
	        sb.append(charBuffer, 0, numCharsRead);

	    // Reset to mark position (normally the mark position is 0).
	    resetableReader.reset();

	    byteArrayInputStream =  new ByteArrayInputStream(sb.toString().getBytes(UTF_8));

	    return byteArrayInputStream;
	}

	protected ByteArrayInputStream toByteArrayInputStream(InputStream inputStream)
		throws IOException
	{
		ByteArrayInputStream byteArrayInputStream = null;
		if ( inputStream != null )
		{
			if ( inputStream instanceof ByteArrayInputStream )
				byteArrayInputStream = (ByteArrayInputStream) inputStream;
			else
			{
				byte[] bytes = inputStream.readAllBytes();
				byteArrayInputStream = new ByteArrayInputStream(bytes);
			}
		}
		return byteArrayInputStream;
	}
}
