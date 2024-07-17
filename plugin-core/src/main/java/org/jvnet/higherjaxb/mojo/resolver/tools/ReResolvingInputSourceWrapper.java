package org.jvnet.higherjaxb.mojo.resolver.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A wrapper to re-resolve for a single input source for an XML entity.
 * 
 * <p>At this time, instances of this class are only created by 
 * {@link ReResolvingEntityResolverWrapper}</p>
 */
public class ReResolvingInputSourceWrapper extends InputSource
{
	private EntityResolver entityResolver;
	protected EntityResolver getEntityResolver() { return entityResolver; }
	protected void setEntityResolver(EntityResolver entityResolver) { this.entityResolver = entityResolver; }

	private InputSource resolvedSource;
	public InputSource getResolvedSource() { return resolvedSource; }
	public void setResolvedSource(InputSource resolvedSource) { this.resolvedSource = resolvedSource; }
	
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
		// By default, get the wrapped resolved reader or null if none was supplied.
		Reader resolvedReader = getResolvedSource().getCharacterStream();
		if (resolvedReader != null)
		{
			try
			{
				if ( resolvedReader instanceof StringReader )
				{
					StringReader resetableReader = (StringReader) resolvedReader;
					resetableReader.reset();
				}
				else
				{
					// Re-resolve InputSource using wrapped EntityResolver.
					InputSource reResolvedSource =
						getEntityResolver().resolveEntity( getPublicId(), getSystemId());

					if ( reResolvedSource != null )
					{
						resolvedReader = reResolvedSource.getCharacterStream();
						setResolvedSource(reResolvedSource);
					}
				}
			}
			catch (IOException | SAXException ioex)
			{
				return resolvedReader;
			}				
		}
		
		// Return resolved reader or or null if none was supplied.
		return resolvedReader;
	}

	@Override
	public void setCharacterStream(Reader characterStream)
	{
		getResolvedSource().setCharacterStream(characterStream);
	}

    /**
     * Get the character stream for this input source.
     *
     * @return The character stream, or null if none was supplied.
     */
	@Override
	public InputStream getByteStream()
	{
		// By default, get the wrapper resolved stream or null if none was supplied.
		InputStream resolvedStream = getResolvedSource().getByteStream();
		if ( resolvedStream != null )
		{
			try
			{
				if ( resolvedStream instanceof ByteArrayInputStream )
				{
					ByteArrayInputStream resetableStream = (ByteArrayInputStream) resolvedStream;
					resetableStream.reset();
				}
				else
				{
					// Re-resolve the InputSource using wrapped EntityResolver.
					InputSource reResolvedSource =
						getEntityResolver().resolveEntity( getPublicId(), getSystemId());

					if ( reResolvedSource != null )
					{
						resolvedStream = reResolvedSource.getByteStream();
						setResolvedSource(reResolvedSource);
					}
				}
			}
			catch (IOException | SAXException ioex)
			{
				return resolvedStream;
			}
		}
		
		// Return resolved stream or or null if none was supplied.
		return resolvedStream;
	}

	@Override
	public void setByteStream(InputStream byteStream)
	{
		getResolvedSource().setByteStream(byteStream);
	}
}
