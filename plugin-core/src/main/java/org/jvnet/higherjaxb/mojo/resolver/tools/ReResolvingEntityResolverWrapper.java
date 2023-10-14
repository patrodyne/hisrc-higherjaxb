package org.jvnet.higherjaxb.mojo.resolver.tools;

import static java.lang.String.format;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An {@link EntityResolver} to re-resolve {@code publicId} and/or {@code systemId} external
 * entities.
 * 
 * <ul>
 * <li><b>Public Identifiers</b> − Public identifiers provide a mechanism to locate DTD resources.</li>
 * <li><b>System Identifiers</b> − A system identifier enables you to specify the location of an external file containing DTD declarations.</li>
 * </ul>
 * 
 * See: org.jvnet.higherjaxb.mojo.AbstractHigherjaxbBaseMojo.createEntityResolver(CatalogResolver)
 */
public class ReResolvingEntityResolverWrapper implements EntityResolver
{
	private Log log;
	public Log getLog() { return log; }
	public void setLog(Log log) { this.log = log; }
	
	private EntityResolver entityResolver;
	protected EntityResolver getEntityResolver() { return entityResolver; }
	protected void setEntityResolver(EntityResolver entityResolver) { this.entityResolver = entityResolver; }

	/**
	 * Construct this wrapper with the given entity resolver.
	 * 
	 * @param entityResolver The entity resolver to be wrapped.
	 */
	public ReResolvingEntityResolverWrapper(EntityResolver entityResolver, org.apache.maven.plugin.logging.Log log)
	{
		if (entityResolver == null)
			throw new IllegalArgumentException("Provided entity resolver must not be null.");

		setEntityResolver(entityResolver);
		setLog(log != null ? log : NullLog.INSTANCE);
	}

	/**
	 * Allow the application to resolve external entities.
     *
     * <p>The parser will call this method before opening any external
     * entity except the top-level document entity.</p>
     * 
     * <p>Application writers can use this method to redirect external
     * system identifiers to secure and/or local URIs</p>
     * 
     * @param publicId The public identifier of the external entity
     *        being referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity
     *        being referenced.
     *        
     * @return An InputSource object describing the new input source,
     *         or null to request that the parser open a regular
     *         URI connection to the system identifier.
     *         
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @throws java.io.IOException A Java-specific IO exception,
     *            possibly the result of creating a new InputStream
     *            or Reader for the InputSource.
     * @see org.xml.sax.InputSource
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
		throws SAXException, IOException
	{
		getLog().debug(format("RESOLVE publicId [%s], systemId [%s].", publicId, systemId));
		if ( getLog().isDebugEnabled() )
		{
			getLog().debug("RESOLVE from publicId and systemId:");
			getLog().debug(format("  publicId [%s]", publicId));
			getLog().debug(format("  systemId [%s]", systemId));
		}
		
		// Invoke the wrapped entity resolver.
		final InputSource resolvedInputSource = getEntityResolver().resolveEntity(publicId, systemId);
		if (resolvedInputSource == null)
		{
			getLog().debug("RESOLVE to null; thus, open a regular URI connection.");
			if ( getLog().isWarnEnabled() )
			{
				if ( (systemId != null) && systemId.startsWith("http") )
					getLog().warn(format("RESOLVE External systemId [%s]", systemId));
			}
			return null;
		}
		else
		{
			if ( getLog().isInfoEnabled() )
			{
				getLog().info("RESOLVE to publicId and systemId:");
				getLog().info(format("  publicId [%s]", resolvedInputSource.getPublicId()));
				getLog().info(format("  systemId [%s]", resolvedInputSource.getSystemId()));
			}
			
			final String pId = publicId != null ? publicId : resolvedInputSource.getPublicId();
			final String sId = systemId != null ? systemId : resolvedInputSource.getSystemId();
			
			return new ReResolvingInputSourceWrapper(getEntityResolver(), resolvedInputSource, pId, sId);
		}
	}
}
