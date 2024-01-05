package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.net.URI;

import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.logging.Log;

public abstract class AbstractSchemeAwareURILastModifiedResolver implements
		SchemeAwareURILastModifiedResolver {

	private final Log logger;
	private final String scheme;

	public AbstractSchemeAwareURILastModifiedResolver(String scheme, Log logger) {
		this.scheme = requireNonNull(scheme);
		this.logger = requireNonNull(logger);
	}

	@Override
	public String getScheme() {
		return scheme;
	}
	
	protected Log getLogger() {
		return logger;
	}

	@Override
	public Long getLastModified(URI uri) {
		final String scheme = getScheme();
		Validate.isTrue(scheme.equalsIgnoreCase(uri.getScheme()),
			format("Invalid scheme [%s] expected [%s].", uri.getScheme(), scheme));
		return getLastModifiedForScheme(uri);
	}

	protected abstract Long getLastModifiedForScheme(URI uri);

}
