package org.jvnet.jaxb2.maven2.net.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;
import org.jvnet.jaxb2.maven2.net.CompositeURILastModifiedResolver;
import org.jvnet.jaxb2.maven2.net.JarURILastModifiedResolver;
import org.jvnet.jaxb2.maven2.net.URILastModifiedResolver;

public class URILastModifiedResolverTest {

	@Test
	public void getsFileURIFromJarFileURICorrectly() throws URISyntaxException,
			MalformedURLException, IOException {
		final URI jarURI = Test.class.getResource("Test.class").toURI();
		final String jarURIString = jarURI.toString();
		System.out.println(jarURIString);
		final String partJarURIString = jarURIString.substring(0,
				jarURIString.indexOf(JarURILastModifiedResolver.SEPARATOR));
		final URI partJarURI = new URI(partJarURIString);
		final URILastModifiedResolver resolver = new CompositeURILastModifiedResolver(
				new SystemStreamLog());
		final URI fileURI = getClass().getResource(
				getClass().getSimpleName() + ".class").toURI();

		assertNotNull(resolver.getLastModified(jarURI));
		assertNotNull(resolver.getLastModified(partJarURI));
		assertNotNull(resolver.getLastModified(fileURI));

		// Switch to true to tests HTTP/HTTPs
		boolean online = false;
		if (online) {
			final URI httpsURI = new URI("https://ya.ru/");
			final URI httpURI = new URI("http://schemas.opengis.net/ogc_schema_updates.rss");
			assertNotNull(resolver.getLastModified(httpsURI));
			assertNotNull(resolver.getLastModified(httpURI));
		}
	}
}
