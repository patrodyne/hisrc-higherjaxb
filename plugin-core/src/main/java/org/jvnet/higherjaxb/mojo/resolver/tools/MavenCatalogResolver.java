package org.jvnet.higherjaxb.mojo.resolver.tools;

import static java.lang.String.format;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.DependencyResource;
import org.jvnet.higherjaxb.mojo.DependencyResourceResolver;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;

import com.sun.org.apache.xml.internal.resolver.CatalogManager;

/**
 * A CatalogResolver to parse a catalog URI for a "maven" scheme.
 * The "maven" scheme has the form:
 * 
 * <pre>
 * maven:groupId:artifactId:type:classifier:version!/resource/path/in/jar/schema.xsd
 * </pre>
 * 
 * Note: The resource path delimiter is <code>'!/'</code>.
 */
public class MavenCatalogResolver extends com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver
{
	public static final String URI_SCHEME_MAVEN = "maven";
	private final DependencyResourceResolver dependencyResourceResolver;
	private final CatalogManager catalogManager;
	
	private Log log;
	public Log getLog() { return log; }
	public void setLog(Log log) { this.log = log; }
	
	/**
	 * Construct with a CatalogManager, DependencyResourceResolver and NullLog.
	 * @param catalogManager An interface to the catalog properties
	 * @param dependencyResourceResolver An interface to resolve a dependency resource
	 */
	public MavenCatalogResolver(CatalogManager catalogManager, DependencyResourceResolver dependencyResourceResolver)
	{
		this(catalogManager, dependencyResourceResolver, NullLog.INSTANCE);
	}

	/**
	 * Construct with a CatalogManager, DependencyResourceResolver and NullLog.
	 * @param catalogManager An interface to the catalog properties
	 * @param dependencyResourceResolver An interface to resolve a dependency resource
	 * @param log An interface for providing feedback to the user from the <code>Mojo</code>.
	 */
	public MavenCatalogResolver(CatalogManager catalogManager, DependencyResourceResolver dependencyResourceResolver, Log log)
	{
		super(catalogManager);
		this.catalogManager = catalogManager;
		if (dependencyResourceResolver == null)
			throw new IllegalArgumentException("Dependency resource resolver must not be null.");
		this.dependencyResourceResolver = dependencyResourceResolver;
		this.log = log != null ? log : NullLog.INSTANCE;
	}

	protected CatalogManager getCatalogManager()
	{
		return catalogManager;
	}

	/**
	 * <p>Extend the {@link com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver}
	 * to resolve the custom "maven:" scheme. First, this method attempts to resolve the
	 * <code>systemId</code> from super class method. Second, if the <code>systemId</code>
	 * matches the "maven:" scheme then a {@link DependencyResourceResolver} is used to
	 * resolve the <code>systemId</code> to an <em>artifact resource URL</em> representing
	 * a directory or a JAR location; otherwise, the resolved <code>systemId</code> is returned.</p>
	 * 
	 * <p>
	 * Typically, this method is used to get an {@link InputStream} using {@link URL#openStream()}
	 * for a <code>systemId</code> with the <code>maven:</code> scheme using the
	 * {@link java.net.JarURLConnection} path syntax <code>jar:url!/{entry}</code>.
	 * </p>
	 * 
	 * @param publicId  The public identifier for the entity in question. This may be null.
	 * @param systemId  The system identifier for the entity in question.
	 * 
	 * <p>Note: XML requires a system identifier on all external entities, so this
	 * value is always specified.</p>
	 *
	 * @return The resolved identifier (a URI reference).
	 */
	@Override
	public String getResolvedEntity(String publicId, String systemId)
	{
		getLog().debug(format("Resolving publicId [%s], systemId [%s].", publicId, systemId));
		final String superResolvedEntity = super.getResolvedEntity(publicId, systemId);
		getLog().debug(format("Parent resolver has resolved publicId [%s], systemId [%s] to [%s].",
			publicId, systemId, superResolvedEntity));
		
		if (superResolvedEntity != null)
			systemId = superResolvedEntity;
		
		if (systemId == null)
			return null;

		try
		{
			final URI uri = new URI(systemId);
			if (URI_SCHEME_MAVEN.equals(uri.getScheme()))
			{
				getLog().debug(format("Resolving systemId [%s] as Maven dependency resource.", systemId));
				final String schemeSpecificPart = uri.getSchemeSpecificPart();
				try
				{
					final DependencyResource dependencyResource = DependencyResource.valueOf(schemeSpecificPart);
					
					if ((dependencyResource.getVersion()) != null && dependencyResource.getVersion().startsWith("!") )
						getLog().warn("Resource path may not be delimited properly with '!/'");
					
					try
					{
						final URL url = dependencyResourceResolver.resolveDependencyResource(dependencyResource);
						String resolved = url.toString();
						getLog().debug(format("Resolved systemId [%s] to [%s].", systemId, resolved));
						return resolved;
					}
					catch (Exception ex)
					{
						getLog().error(format("Error resolving dependency resource [%s].", dependencyResource));
					}
				}
				catch (IllegalArgumentException iaex)
				{
					getLog().error(format("Error parsing dependency descriptor [%s].", schemeSpecificPart));
				}
				getLog().error(format(
					"Failed to resolve systemId [%s] as dependency resource. Returning parent resolver result [%s].",
					systemId, superResolvedEntity));
				return superResolvedEntity;
			}
			else
			{
				getLog().debug(format(
					"SystemId [%s] is not a URI with a 'maven:' scheme. Returning parent resolver result [%s].",
					systemId, superResolvedEntity));
				return superResolvedEntity;
			}
		}
		catch (URISyntaxException urisex)
		{
			getLog().debug(format(
				"Could not parse the systemId [%s] as URI. Returning parent resolver result [%s].",
				systemId, superResolvedEntity));
			return superResolvedEntity;
		}
	}
}
