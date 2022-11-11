package org.jvnet.higherjaxb.mojo.net;

public interface SchemeAwareURILastModifiedResolver extends
		URILastModifiedResolver {

	public String getScheme();

}
