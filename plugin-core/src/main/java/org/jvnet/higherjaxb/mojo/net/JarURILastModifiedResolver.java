package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;

public class JarURILastModifiedResolver extends
		AbstractSchemeAwareURILastModifiedResolver {

	public static final String SCHEME = "jar";
	public static final String SEPARATOR = "!/";

	private URILastModifiedResolver parent;

	public JarURILastModifiedResolver(Log logger, URILastModifiedResolver parent) {
		super(SCHEME, logger);
		this.parent = requireNonNull(parent);
	}

	private URILastModifiedResolver getParent() {
		return parent;
	}

	@Override
	protected Long getLastModifiedForScheme(URI uri) {
		try {
			final URI mainURI = getMainURI(uri);
			getLogger().debug(format("Retrieving the last modification timestamp of the URI [%s] via the main URI [%s].",
				uri, mainURI));
			return getParent().getLastModified(mainURI);
		} catch (Exception ex) {
			getLogger()
					.error(format("Could not retrieve the main URI from the Jar URI [%s].", uri), ex);
			getLogger().warn(format("Last modification of the URI [{0}] is not known.", uri));
			return null;
		}
	}

	public URI getMainURI(URI uri) throws MalformedURLException,
			URISyntaxException {
		final String uriString = uri.toString();
		final URL url;
		if (uriString.indexOf(SEPARATOR) < 0) {
			url = new URI(uriString + SEPARATOR).toURL();
		} else {
			url = uri.toURL();
		}
		final String spec = url.getFile();
		final int separatorPosition = spec.indexOf(SEPARATOR);
		if (separatorPosition == -1) {
			throw new MalformedURLException(format("No [!/] found in url spec [%s].", spec));
		}
		final String mainURIString = separatorPosition < 0 ? spec : spec
				.substring(0, separatorPosition);
		return new URI(mainURIString);
	}

}
