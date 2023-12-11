package org.jvnet.higherjaxb.mojo.resolver.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A wrapper to re-resolve for a single input source for an XML entity.
 */
public class ReResolvingInputSourceWrapper extends InputSource
{
	private EntityResolver entityResolver;
	protected EntityResolver getEntityResolver() { return entityResolver; }
	protected void setEntityResolver(EntityResolver entityResolver) { this.entityResolver = entityResolver; }

	private InputSource resolvedSource;
	protected InputSource getResolvedSource() { return resolvedSource; }
	protected void setResolvedSource(InputSource resolvedSource) { this.resolvedSource = resolvedSource; }
	
	/**
	 * Construct with an {@link EntityResolver}, an {@link InputSource},
	 * a {@code publicId} and a {@code systemId}.
	 * 
	 * @param entityResolver The {@link EntityResolver} to be wrapped.
	 * @param resolvedSource The {@link InputSource} to be wrapped.
	 * @param publicId The original public identifier.
	 * @param systemId The original system identifier.
	 */
	public ReResolvingInputSourceWrapper(EntityResolver entityResolver,
		InputSource resolvedSource, String publicId, String systemId)
	{
		setEntityResolver(entityResolver);
		setResolvedSource(resolvedSource);
		setPublicId(publicId);
		setSystemId(systemId);
	}

    /**
     * Get the character reader for this input source.
     *
     * @return The character reader, or null if none was supplied.
     */
	@Override
	public Reader getCharacterStream()
	{
		// Get the resolved reader or or null if none was supplied.
		final Reader resolvedReader = getResolvedSource().getCharacterStream();
		if (resolvedReader == null)
			return null;
		else
		{
			try
			{
				InputSource resolvedEntity = getEntityResolver().resolveEntity( getPublicId(), getSystemId());
				if (resolvedEntity != null)
					return resolvedEntity.getCharacterStream();
				else
					return resolvedReader;
			}
			catch (IOException | SAXException ioex)
			{
				return resolvedReader;
			}
		}
	}

	/** No operation. */
	@Override
	public void setCharacterStream(Reader characterStream)
	{
	}

    /**
     * Get the character stream for this input source.
     *
     * @return The character stream, or null if none was supplied.
     */
	@Override
	public InputStream getByteStream()
	{
		final InputStream resolvedStream = getResolvedSource().getByteStream();
		if (resolvedStream == null)
			return null;
		else
		{
			try
			{
				InputSource resolvedEntity = getEntityResolver().resolveEntity( getPublicId(), getSystemId());

				if (resolvedEntity != null)
					return resolvedEntity.getByteStream();
				else
					return resolvedStream;
			}
			catch (IOException | SAXException ioex)
			{
				return resolvedStream;
			}
		}
	}

	/** No operation. */
	@Override
	public void setByteStream(InputStream byteStream)
	{
	}
}
