package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.net.URI;

import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.logging.Log;

/**
 * An {@link URI} scheme and "last modified" aware abstract class with logger.
 */
public abstract class AbstractSchemeAwareURILastModifiedResolver
	implements SchemeAwareURILastModifiedResolver
{
	// Represents the Maven logger.
	private final Log logger;
	// Represents the URI scheme.
	private final String scheme;

	/**
	 * Construct with the given schema and logger.
	 * 
	 * @param scheme The URL scheme ("{@code classpath}, {@code maven}, etc.)".
	 * @param logger The Maven logger instance.
	 */
	public AbstractSchemeAwareURILastModifiedResolver(String scheme, Log logger)
	{
		this.scheme = requireNonNull(scheme);
		this.logger = requireNonNull(logger);
	}

	/**
	 * The URL scheme ("{@code classpath}, {@code maven}, etc.)".
	 * 
	 * @return The URL/URI protocol/scheme.
	 */
	@Override
	public String getScheme()
	{
		return scheme;
	}

	/**
	 * Get the Maven logger instance.
	 * 
	 * @return A Maven logger instance.
	 */
	protected Log getLogger()
	{
		return logger;
	}

	@Override
	public Long getLastModified(URI uri)
	{
		final String scheme = getScheme();
		Validate.isTrue(scheme.equalsIgnoreCase(uri.getScheme()),
			format("Invalid scheme [%s] expected [%s].", uri.getScheme(), scheme));
		return getLastModifiedForScheme(uri);
	}

	/**
	 * Get the last modified time for the given {@link URI}.
	 * 
	 * @param uri The {@link URI} to examine.
	 * 
	 * @return The last modified time as a {@link Long} value.
	 */
	protected abstract Long getLastModifiedForScheme(URI uri);
}
