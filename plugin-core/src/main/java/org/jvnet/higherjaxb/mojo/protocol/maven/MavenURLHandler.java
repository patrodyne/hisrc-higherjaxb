package org.jvnet.higherjaxb.mojo.protocol.maven;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Properties;

import javax.xml.catalog.CatalogResolver;

import org.jvnet.higherjaxb.mojo.resolver.tools.AbstractCatalogResolver;

/**
 * Protocol: {@code maven:}
 * 
 *  <p>
 * An extension of {@link URLStreamHandler} to manage a {@link URLConnection}
 * to read Maven dependency resources from a local or remote repository.
 * </p>
 * 
 * <p>
 * The first time a protocol name is encountered when constructing a {@code URL},
 * the appropriate stream protocol handler is automatically loaded.
 * </p>
 * 
 * <p>
 * The JVM locates packages containing protocol handlers using the system
 * property {@code java.protocol.handler.pkgs}. The value of this property
 * is a list of package names; if more than one package is in the list,
 * use a vertical bar {@code "foo|bar"} to separate the package names.
 * </p>
 */
public class MavenURLHandler
   extends URLStreamHandler
{
	private AbstractCatalogResolver catalogResolver;
	public AbstractCatalogResolver getCatalogResolver() { return catalogResolver; }
	public void setCatalogResolver(AbstractCatalogResolver catalogResolver) { this.catalogResolver = catalogResolver; }

	public Properties fileSuffixToMimeTypesProperties;
	public Properties getFileSuffixToMimeTypesProperties()
	{
		return fileSuffixToMimeTypesProperties;
	}
	public void setFileSuffixToMimeTypesProperties(Properties fileSuffixToMimeTypesProperties)
	{
		this.fileSuffixToMimeTypesProperties = fileSuffixToMimeTypesProperties;
	}
	 
	/**
	 * Construct handler with the given {@link AbstractCatalogResolver} and
	 * suffix-to-mime {@link Properties}.
	 * 
	 * @param catalogResolver An instance of {@link CatalogResolver}.
	 * @param fileSuffixToMimeTypesProperties suffix-to-mime {@link Properties}
	 */
	public MavenURLHandler(AbstractCatalogResolver catalogResolver,
		Properties fileSuffixToMimeTypesProperties)
	{
		setCatalogResolver(catalogResolver);
		setFileSuffixToMimeTypesProperties(fileSuffixToMimeTypesProperties);
	}
	
	/**
	 * Construct handler with the given {@link AbstractCatalogResolver}.
	 * 
	 * @param catalogResolver An instance of {@link CatalogResolver}.
	 */
	public MavenURLHandler(AbstractCatalogResolver catalogResolver)
	{
		this(catalogResolver, new Properties());
	}
	
	/**
	 * Opens a connection to a Maven artifact referenced by the given {@link URL}.
	 *
	 * @param url A URL for the Maven artifact connection.
	 * 
	 * @return A {@link URLConnection} instance for the {@link URL}.
	 * 
	 * @throws IOException For any failed or interrupted I/O operation.
	 */
	@Override
	protected URLConnection openConnection(URL url)
		throws IOException
	{
		// Open connection for the given <code>url</code> and without a proxy.
		return openConnection(url, Proxy.NO_PROXY);
	}
	  
	/**
	 * Opens a connection to a Maven artifact referenced by the given {@link URL}.
	 *
	 * @param url A URL for the Maven artifact connection.
	 * @param proxy A proxy through which the connection will be made.
	 * 
	 * @return A {@link URLConnection} instance for the {@link URL}.
	 * 
	 * @throws IOException For any failed or interrupted I/O operation.
	 */
	@Override
	protected URLConnection openConnection(URL url, Proxy proxy)
		throws IOException
	{
		// MavenURLConnection does not support proxy at this time.
		return new MavenURLConnection(url, getCatalogResolver(), getFileSuffixToMimeTypesProperties());
	}
}
