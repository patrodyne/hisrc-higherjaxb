package org.jvnet.higherjaxb.mojo.net;

import java.net.URI;

import org.apache.maven.plugin.logging.Log;

/**
 * An HTTPS {@link URI} last modified resolver.
 */
public class HttpsURILastModifiedResolver
	extends AbstractHTTPURILastModifiedResolver
{
	public static final String SCHEME = "https";

	public HttpsURILastModifiedResolver(Log logger)
	{
		super(SCHEME, logger);
	}
}
