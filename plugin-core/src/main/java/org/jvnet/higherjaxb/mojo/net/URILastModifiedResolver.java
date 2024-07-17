package org.jvnet.higherjaxb.mojo.net;

import java.net.URI;

/**
 * Interface for a {@link URI} last modified resolver.
 */
public interface URILastModifiedResolver
{
	/**
	 * Finds out the last modification date for an URI.
	 * 
	 * @param uri URI to find out the last modification date for.
	 * 
	 * @return Last modification date or <code>null</code> if unknown.
	 */
	public Long getLastModified(URI uri);
}
