package org.jvnet.higherjaxb.mojo.protocol;

import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Implement a factory for {@code URL} stream protocol handlers with a
 * {@link Map} of configured protocol-handler pairs.
 * 
 */
public class ConfigurableStreamHandlerFactory implements URLStreamHandlerFactory
{
	private Map<String, URLStreamHandler> protocolHandlers;
	protected Map<String, URLStreamHandler> getProtocolHandlers()
	{
		if ( protocolHandlers == null )
			setProtocolHandlers(new HashMap<String, URLStreamHandler>());
		return protocolHandlers;
	}
	protected void setProtocolHandlers(Map<String, URLStreamHandler> protocolHandlers)
	{
		this.protocolHandlers = protocolHandlers;
	}

	/**
	 * Add a protocol and handler to the configuration map.
	 * 
	 * @param protocol A protocol and handler.
	 * @param urlHandler A {@link URLStreamHandler} to manage a {@link URLConnection}.
	 */
	public void addHandler(String protocol, URLStreamHandler urlHandler)
	{
		getProtocolHandlers().put(protocol, urlHandler);
	}

	/**
	 * Construct without any handlers.
	 */
	public ConfigurableStreamHandlerFactory()
	{
		super();
	}
	
	/**
	 * Construct with the given protocol and handler plus default handlers.
	 * 
	 * @param protocol The URL scheme ("{@code classpath}, {@code maven}, etc.)".
	 * @param urlHandler A {@link URLStreamHandler} to manage a {@link URLConnection}.
	 */
	public ConfigurableStreamHandlerFactory(String protocol, URLStreamHandler urlHandler)
	{
		this();
		addHandler(protocol, urlHandler);
	}

    /**
     * Creates a new {@code URLStreamHandler} instance with the specified
     * protocol.
     *
     * @param protocol The URL scheme ("{@code classpath}, {@code maven}, etc.)".
     * 
     * @return  a {@code URLStreamHandler} for the specific protocol, or {@code
     *          null} if this factory cannot create a handler for the specific
     *          protocol.
     */
	@Override
	public URLStreamHandler createURLStreamHandler(String protocol)
	{
		return getProtocolHandlers().get(protocol);
	}
}
