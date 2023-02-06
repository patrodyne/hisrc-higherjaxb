package org.jvnet.higherjaxb.mojo.resolver.tools;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ReResolvingEntityResolverWrapper implements EntityResolver
{
	private Logger logger = LoggerFactory.getLogger(ReResolvingEntityResolverWrapper.class);
	public Logger getLogger() { return logger; }
	
	private final EntityResolver entityResolver;

	public ReResolvingEntityResolverWrapper(EntityResolver entityResolver)
	{
		if (entityResolver == null)
			throw new IllegalArgumentException("Provided entity resolver must not be null.");

		this.entityResolver = entityResolver;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
		throws SAXException, IOException
	{
		getLogger().debug(MessageFormat.format("Resolving publicId [{0}], systemId [{1}].",
			publicId, systemId));
		
		final InputSource resolvedInputSource = this.entityResolver.resolveEntity(publicId, systemId);
		if (resolvedInputSource == null)
		{
			getLogger().debug("Resolution result is null.");
			return null;
		}
		else
		{
			getLogger().debug(MessageFormat.format("Resolved to publicId [{0}], systemId [{1}].",
				resolvedInputSource.getPublicId(), resolvedInputSource.getSystemId()));
			
			final String pId = publicId != null ? publicId : resolvedInputSource.getPublicId();
			final String sId = systemId != null ? systemId : resolvedInputSource.getSystemId();
			return new ReResolvingInputSourceWrapper(this.entityResolver, resolvedInputSource, pId, sId);
		}
	}
}
