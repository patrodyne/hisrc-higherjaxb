package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import org.apache.maven.plugin.logging.Log;

/**
 * Abstract HTTP {@link URI} "last modified" resolver.
 * 
 * <p>Getting the last modification timestamp is only possible when the URL is accessible.</p>
 */
public abstract class AbstractHTTPURILastModifiedResolver
	extends AbstractSchemeAwareURILastModifiedResolver
{
	public AbstractHTTPURILastModifiedResolver(String scheme, Log logger)
	{
		super(scheme, logger);
	}

	@Override
	protected Long getLastModifiedForScheme(URI uri)
	{
		getLogger()
			.warn(format("The URI [%s] seems to represent an absolute HTTP or HTTPS URL. "
				+ "Getting the last modification timestamp is only possible "
				+ "if the URL is accessible "
				+ "and if the server returns the [Last-Modified] header correctly. "
				+ "This method is not reliable and is likely to fail. "
				+ "In this case the last modification timestamp will be assumed to be unknown.",
				uri));
		try
		{
			final URL url = uri.toURL();
			try
			{
				final URLConnection urlConnection = url.openConnection();
				if ( urlConnection instanceof HttpURLConnection )
				{
					final HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
					httpURLConnection.setInstanceFollowRedirects(true);
					final long lastModified = httpURLConnection.getLastModified();
					if ( lastModified == 0 )
					{
						getLogger().warn(format(
							"Could not retrieve the last modification timestamp for the URI [%s] from the HTTP URL connection. "
							+ "The [Last-Modified] header was probably not set correctly.",
							uri));
					}
					else
					{
						getLogger().debug(MessageFormat.format(
							"HTTP connection to the URI [{0}] returned the last modification timestamp [{1,date,yyyy-MM-dd HH:mm:ss.SSS}].",
							uri, lastModified));
						return lastModified;
					}
				}
				else
				{
					getLogger().warn(format(
						"URL connection for the URI [%s] is not a HTTP or HTTPS connection, can't read the [Last-Modified] header.",
						uri));
				}
			}
			catch (IOException ioex)
			{
				getLogger().error(format("Error opening the URL connection for the URI [%s].", uri), ioex);
			}
		}
		catch (MalformedURLException murlex)
		{
			getLogger().error(format("URI [%s].", uri), murlex);
		}
		
		getLogger().warn(format("Last modification of the URI [%s] is not known.", uri));
		return null;
	}
}
