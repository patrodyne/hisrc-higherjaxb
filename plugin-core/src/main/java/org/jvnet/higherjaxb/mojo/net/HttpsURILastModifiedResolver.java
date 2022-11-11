package org.jvnet.higherjaxb.mojo.net;

import org.apache.maven.plugin.logging.Log;

public class HttpsURILastModifiedResolver extends AbstractHTTPURILastModifiedResolver{

	public static final String SCHEME = "https";
	
	public HttpsURILastModifiedResolver(Log logger) {
		super(SCHEME, logger);
	}

}
