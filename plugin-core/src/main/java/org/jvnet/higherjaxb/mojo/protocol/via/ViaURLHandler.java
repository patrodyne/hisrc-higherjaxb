package org.jvnet.higherjaxb.mojo.protocol.via;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Properties;

import javax.xml.catalog.CatalogResolver;

import org.jvnet.higherjaxb.mojo.resolver.tools.AbstractCatalogResolver;

/**
 * Protocol: {@code via:}
 * 
 * <p>
 * An extension of {@link URLStreamHandler} to manage a {@link URLConnection}
 * to resolve entities using other resolvers: maven, classpath, etc..
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
public class ViaURLHandler
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
	public ViaURLHandler(AbstractCatalogResolver catalogResolver,
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
	public ViaURLHandler(AbstractCatalogResolver catalogResolver)
	{
		this(catalogResolver, new Properties());
	}
	
    /**
     * Opens a connection to the {@code via} referenced by the given {@link URL}.
     *
     * @param url A URL for the {@code via} resource connection.
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
    * Opens a connection to the {@code via} referenced by the given {@link URL}.
    *
    * @param url A URL for the {@code via} resource connection.
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
	   // ViaURLConnection does not support proxy at this time.
	   return new ViaURLConnection(url, getCatalogResolver(), getFileSuffixToMimeTypesProperties());
   }
}
