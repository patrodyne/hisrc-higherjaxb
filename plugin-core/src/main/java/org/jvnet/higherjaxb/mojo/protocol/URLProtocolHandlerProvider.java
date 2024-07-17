package org.jvnet.higherjaxb.mojo.protocol;

import static org.jvnet.higherjaxb.mojo.resolver.tools.AbstractCatalogResolver.URI_SCHEME_CLASSPATH;
import static org.jvnet.higherjaxb.mojo.resolver.tools.AbstractCatalogResolver.URI_SCHEME_MAVEN;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

import org.jvnet.higherjaxb.mojo.protocol.classpath.ClasspathURLHandler;
import org.jvnet.higherjaxb.mojo.protocol.maven.MavenURLHandler;

/**
 * URL stream handler service-provider class for <code>classpath:</code> protocol.
 * 
 * <p>A {@link URL} stream handler provider identifies itself with a configuration
 * file named <code>java.net.spi.URLStreamHandlerProvider</code> in the resource
 * directory META-INF/services.</p>
 */
@Deprecated
public class URLProtocolHandlerProvider extends URLStreamHandlerProvider
{
    /**
     * Creates a new {@code URLStreamHandler} instance with the specified
     * protocol.
     *
     * @param protocol The URL scheme ("{@code classpath}, {@code maven}, etc.)".
     * 
     * @return  a {@code URLStreamHandler} for the specific protocol, or {@code
     *          null} if this factory cannot create a handler for the specific
     *          protocol
     */
	@Override
	public URLStreamHandler createURLStreamHandler(String protocol)
	{
		URLStreamHandler handler = null;
		
		switch ( protocol )
		{
			case URI_SCHEME_CLASSPATH:
				handler = new ClasspathURLHandler(null);
				break;
			case URI_SCHEME_MAVEN:
				handler = new MavenURLHandler(null);
				break;
			default:
				handler = null;
				break;
		}
		
		return handler;
	}
}
