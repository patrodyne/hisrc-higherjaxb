package org.jvnet.higherjaxb.mojo.net;

import java.net.URI;

/** 
 * Interface to extend {@link URILastModifiedResolver} and provide method to get
 * the {@link URI} scheme.
 */
public interface SchemeAwareURILastModifiedResolver extends URILastModifiedResolver
{
	/**
	 * The URL scheme ("{@code classpath}, {@code maven}, etc.)".
	 * 
	 * @return The URL/URI protocol/scheme.
	 */
	public String getScheme();
}
