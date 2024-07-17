package org.jvnet.higherjaxb.mojo.net;

import java.net.URI;

import org.apache.maven.plugin.logging.Log;

/**
 * An HTTP {@link URI} last modified resolver.
 */
public class HttpURILastModifiedResolver
	extends AbstractHTTPURILastModifiedResolver
{
	public static final String SCHEME = "http";

	public HttpURILastModifiedResolver(Log logger)
	{
		super(SCHEME, logger);
	}
}
