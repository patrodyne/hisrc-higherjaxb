package org.jvnet.higherjaxb.mojo.resolver.tools.tests;

import static javax.xml.catalog.CatalogManager.catalogResolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogResolver;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

public class MavenCatalogResolverTest
{
	private static final String TEST_PATH = "src/test/resources/org/jvnet/higherjaxb/mojo/resolver/tools/tests";
	
	@Test
	public void checkReenterability() throws URISyntaxException
	{
		URI aCatalogURI = new File(TEST_PATH+"/a/catalog.xml").toURI();
		CatalogResolver cra = catalogResolver(CatalogFeatures.defaults(), aCatalogURI);
		InputSource aXLinkSource = cra.resolveEntity(null, "http://www.w3.org/1999/xlink.xsd");
		assertNotNull(aXLinkSource);
		URI aXLinkURI1 = new File(TEST_PATH+"/a/w3c/1999/xlink.xsd").toURI();
		URI aXLinkURI2 = new URI(aXLinkSource.getSystemId());
		assertEquals(aXLinkURI1, aXLinkURI2);
		
		URI bCatalogURI = new File(TEST_PATH+"/b/catalog.xml").toURI();
		CatalogResolver crb = catalogResolver(CatalogFeatures.defaults(), bCatalogURI);
		InputSource bXLinkSource = crb.resolveEntity(null, "http://www.w3.org/2005/atom-author-link.xsd");
		assertNotNull(bXLinkSource);
		URI bXLinkURI1 = new File(TEST_PATH+"/b/w3c/2005/atom-author-link.xsd").toURI();
		URI bXLinkURI2 = new URI(bXLinkSource.getSystemId());
		assertEquals(bXLinkURI1, bXLinkURI2);
	}
}
