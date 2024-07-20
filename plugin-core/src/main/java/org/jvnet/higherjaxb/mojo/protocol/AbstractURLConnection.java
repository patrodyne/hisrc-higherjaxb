package org.jvnet.higherjaxb.mojo.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Represents a single {@link URL} connection, and implements a protocol to
 * read resources from the classpath.
 */
abstract public class AbstractURLConnection
   extends URLConnection
{
    private static Logger logger = LoggerFactory.getLogger(AbstractURLConnection.class);
    public static Logger getLogger() { return logger; }

	public static final ConfigurableStreamHandlerFactory CONFIGURABLE_STREAM_HANDLER_FACTORY =
		new ConfigurableStreamHandlerFactory();
	private static boolean CONFIGURABLE_STREAM_HANDLER_FACTORY_IS_SET;
    private static final Object STREAM_HANDLER_LOCK = new Object();
	static
	{
		// Sets an application's URLStreamHandlerFactory.
		// This method can be called at most once in a given Java Virtual Machine.
		//
		// The URLStreamHandlerFactory instance is used to construct a stream
		// protocol handler from a URI scheme name.
		synchronized (STREAM_HANDLER_LOCK)
		{
			if ( !CONFIGURABLE_STREAM_HANDLER_FACTORY_IS_SET )
			{
				try
				{
					URL.setURLStreamHandlerFactory(CONFIGURABLE_STREAM_HANDLER_FACTORY);
					CONFIGURABLE_STREAM_HANDLER_FACTORY_IS_SET = true;
				}
				catch ( Throwable t)
				{
					String msg = t.getClass().getSimpleName() + t.getMessage();
					getLogger().warn("Cannot set stream handler factory: {}", msg);
				}
			}
		}
	}
	
	public Properties fileSuffixToMimeTypesProperties;
	public Properties getFileSuffixToMimeTypesProperties()
	{
		return fileSuffixToMimeTypesProperties;
	}
	public void setFileSuffixToMimeTypesProperties(Properties fileSuffixToMimeTypesProperties)
	{
		this.fileSuffixToMimeTypesProperties = fileSuffixToMimeTypesProperties;
	}
	
	// Represents a single input source for an XML entity.
	private InputSource inputSource;
	public InputSource getInputSource()
	{
		return inputSource;
	}
	public void setInputSource(InputSource inputSource)
	{
		this.inputSource = inputSource;
	}
	
	/**
	 * Constructs a URL connection to the specified URL. A connection to the
	 * object referenced by the URL is not created.
	 *
	 * @param url The specified URL.
	 */
	public AbstractURLConnection(URL url)
	{
		super(url);
	}

	/**
	 * If {@code false}, this connection object has not created a
	 * communications link to the specified URL. If {@code true},
	 * the communications link has been established.
	 */
	public boolean isConnected() { return connected; }
	/** Set connection object when communications link has been established. */
	public void setConnected(boolean connected)	{ super.connected = connected; }
	
	@Override
	public InputStream getInputStream()
		throws IOException
	{
		connect();
		if ( isConnected() )
		{
			// Return the connection to this URL.
			InputStream inputStream = getInputSource().getByteStream();
			return inputStream;
		}
		else
			throw new IOException("not connected: " + getURL());
	}
	
	protected String guessContentType(String systemId, String charset)
		throws IOException
	{
		int systemIdSuffixIndex = systemId.lastIndexOf('.');
		String systemIdSuffix = systemId.substring(systemIdSuffixIndex + 1);
		String mimeType = getFileSuffixToMimeTypesProperties().getProperty(systemIdSuffix);
		if ( mimeType == null )
			mimeType = getFileNameMap().getContentTypeFor(getInputSource().getSystemId());
		if ( mimeType == null )
			mimeType = guessContentTypeFromStream(getInputSource().getByteStream());
		
		String contentType = mimeType + "; charset=" + charset;
		return contentType;
	}
}
