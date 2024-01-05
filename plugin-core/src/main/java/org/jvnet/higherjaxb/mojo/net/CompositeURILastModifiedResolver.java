package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;

public class CompositeURILastModifiedResolver implements
		URILastModifiedResolver {

	private final Map<String, URILastModifiedResolver> resolvers = new HashMap<String, URILastModifiedResolver>(
			10);

	private final Log logger;

	public CompositeURILastModifiedResolver(Log logger) {
		this.logger = requireNonNull(logger);
		addResolvers(new FileURILastModifiedResolver(logger),
				new JarURILastModifiedResolver(logger, this),
				new HttpURILastModifiedResolver(logger),
				new HttpsURILastModifiedResolver(logger));
	}

	public CompositeURILastModifiedResolver() {
		this(NullLog.INSTANCE);
	}

	private Log getLogger() {
		return logger;
	}

	private void addResolvers(SchemeAwareURILastModifiedResolver... resolvers) {
		Validate.noNullElements(resolvers);
		for (final SchemeAwareURILastModifiedResolver resolver : resolvers) {
			this.resolvers.put(resolver.getScheme().toLowerCase(), resolver);
		}
	}

	private URILastModifiedResolver getResolver(String scheme) {
		return this.resolvers.get(scheme);
	}

	@Override
	public Long getLastModified(URI uri) {
		final String scheme = uri.getScheme();
		if (scheme == null) {
			getLogger()
				.error(format("URI [%s] does not provide the scheme part.", uri));
		}
		final URILastModifiedResolver resolver = getResolver(scheme);
		if (resolver == null) {

			getLogger()
				.error(format("Could not resolve the last modification of the URI [%s] with the scheme [%s].",
					uri, scheme));
		} else {
			return resolver.getLastModified(uri);
		}
		getLogger().warn(format("Last modification of the URI [%s] is not known.",	uri));
		return null;
	}
}
