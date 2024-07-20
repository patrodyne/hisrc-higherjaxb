package org.jvnet.higherjaxb.mojo.protocol.via;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.xml.catalog.CatalogResolver;

import org.jvnet.higherjaxb.mojo.protocol.AbstractURLConnection;
import org.jvnet.higherjaxb.mojo.resolver.tools.AbstractCatalogResolver;
import org.jvnet.higherjaxb.mojo.resolver.tools.ViaCatalogResolver;
import org.xml.sax.InputSource;

/**
 * Represents a single {@link URL} connection, and implements a protocol to
 * read resources from the {@code via}.
 */
public class ViaURLConnection
   extends AbstractURLConnection
{
	// Represents a CatalogResolver to parse a catalog uri for the {@code via} scheme.
	// See AbstractHigherjaxbBaseMojo#createViaCatalogResolver()
	private ViaCatalogResolver viaCatalogResolver;
	public ViaCatalogResolver getViaCatalogResolver()
	{
		return viaCatalogResolver;
	}
	public void setViaCatalogResolver(ViaCatalogResolver viaCatalogResolver)
	{
		this.viaCatalogResolver = viaCatalogResolver;
	}
	
	/**
	 * Constructs a URL connection to the specified URL. A connection to the
	 * object referenced by the URL is not created.
	 *
	 * @param url The specified URL.
	 * @param catalogResolver An instance of {@link CatalogResolver}.
	 */
	public ViaURLConnection(URL url, AbstractCatalogResolver catalogResolver)
	{
		this(url, catalogResolver, new Properties());
	}
	
	/**
	 * Constructs a URL connection to the specified URL. A connection to the
	 * object referenced by the URL is not created.
	 *
	 * @param url The specified URL.
	 * @param viaResolver An instance of {@link CatalogResolver}.
	 * @param fileSuffixToMimeTypesProperties suffix-to-mime {@link Properties}
	 */
	protected ViaURLConnection(URL url, AbstractCatalogResolver viaResolver,
		Properties fileSuffixToMimeTypesProperties)
	{
		super(url);
		setDoInput(true);
		setDoOutput(false);
		setUseCaches(false);
		if ( viaResolver instanceof ViaCatalogResolver )
			setViaCatalogResolver((ViaCatalogResolver) viaResolver);
		setFileSuffixToMimeTypesProperties(fileSuffixToMimeTypesProperties);
	}

	/**
	 * Opens a communications link to the resource referenced by this URL, if
	 * such a connection has not already been established.
	 * 
	 * <p>
	 * If the {@code connect} method is called when the connection has already
	 * been opened (indicated by the {@code connected} field having the value
	 * {@code true}), the call is ignored.
	 * </p>
	 * 
	 * <p>
	 * URLConnection objects go through two phases: first they are created, then
	 * they are connected. After being created, and before being connected,
	 * various options can be specified (e.g., doInput and UseCaches). After
	 * connecting, it is an error to try to set them. Operations that depend on
	 * being connected, like getContentLength, will implicitly perform the
	 * connection, if necessary.
	 * </p>
	 *
	 * @throws IOException For any failed or interrupted I/O operation.
	 * 
	 * @see java.net.URLConnection#connected
	 * @see #getConnectTimeout()
	 * @see #setConnectTimeout(int)
	 */
	@Override
	public void connect()
		throws IOException
	{
        if (!isConnected())
        {
            try
            {
            	String publicId = null;
            	String systemId = getURL().toURI().toString();
            	
			    // Searches through the catalog entries in the primary
				// and alternative catalogs to attempt to resolve the
				// custom {@code via} scheme.
            	InputSource inputSource = getViaCatalogResolver()
            		.resolveEntity(publicId, systemId);
            	
            	setInputSource(inputSource);
            	
            	//
            	// Empty is defined as follows:
            	//
            	// 1) All of the input sources, including the public identifier,
            	//    system identifier, byte stream, and character stream, are null.
            	//
            	// 2) The public identifier and system identifier are null, and byte
            	//    and character stream are either null or contain no byte or character.
            	//
				if ( !getInputSource().isEmpty() && (getInputSource().getByteStream() != null) )
				{
					String charset = getInputSource().getEncoding();
					
					String contentType = guessContentType(systemId, charset);
					setRequestProperty("content-type", contentType);
					setConnected(true);
				}
				else
					throw new IOException("Cannot connect to " + getInputSource().getSystemId());
            }
            catch (URISyntaxException ex)
            {
                throw new IOException("Cannot connect to " + getURL(), ex);
            }
        }
	}
}
