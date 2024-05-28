package org.jvnet.higherjaxb.mojo.resolver.tools;

import static com.sun.org.apache.xml.internal.resolver.CatalogManager.getStaticManager;
import static java.lang.String.format;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.DependencyResource;
import org.jvnet.higherjaxb.mojo.DependencyResourceResolver;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;
import org.jvnet.higherjaxb.mojo.resolver.EntityKey;

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
public class MavenCatalogResolver
	extends com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver
{
	public static final String URI_SCHEME_FILE = "file";
	public static final String URI_SCHEME_MAVEN = "maven";
	
	private Log log;
	public Log getLog() { return log; }
	public void setLog(Log log) { this.log = log; }
	
	private DependencyResourceResolver dependencyResourceResolver;
	public DependencyResourceResolver getDependencyResourceResolver()
	{
		return dependencyResourceResolver;
	}
	public void setDependencyResourceResolver(DependencyResourceResolver dependencyResourceResolver)
	{
		this.dependencyResourceResolver = dependencyResourceResolver;
	}

	private CatalogManager catalogManager;
	public CatalogManager getCatalogManager()
	{
		return catalogManager;
	}
	public void setCatalogManager(CatalogManager catalogManager)
	{
		this.catalogManager = catalogManager;
	}

	private Map<EntityKey, String> resolvedEntities;
	public Map<EntityKey, String> getResolvedEntities()
	{
		return resolvedEntities;
	}
	public void setResolvedEntities(Map<EntityKey, String> resolvedEntities)
	{
		this.resolvedEntities = resolvedEntities;
	}
	
	/**
	 * Construct with a CatalogManager, DependencyResourceResolver and NullLog.
	 * 
	 * @param catalogManager An interface to the catalog properties
	 * @param dependencyResourceResolver An interface to resolve a dependency resource
	 */
	public MavenCatalogResolver(CatalogManager catalogManager, DependencyResourceResolver dependencyResourceResolver)
	{
		this(catalogManager, dependencyResourceResolver, NullLog.INSTANCE);
	}

	/**
	 * Construct with a CatalogManager, DependencyResourceResolver and NullLog.
	 * 
	 * @param catalogManager An interface to the catalog properties
	 * @param dependencyResourceResolver An interface to resolve a dependency resource
	 * @param log An interface for providing feedback to the user from the <code>Mojo</code>.
	 */
	public MavenCatalogResolver(CatalogManager catalogManager, DependencyResourceResolver dependencyResourceResolver, Log log)
	{
		super(catalogManager != null ? catalogManager : getStaticManager());
		setCatalogManager(catalogManager != null ? catalogManager : getStaticManager());
		if (dependencyResourceResolver == null)
			throw new IllegalArgumentException("Dependency resource resolver must not be null.");
		setDependencyResourceResolver(dependencyResourceResolver);
		setLog(log != null ? log : NullLog.INSTANCE);
		setResolvedEntities(new HashMap<>());
	}

	@Override
	public String getResolvedEntity(String publicId, String systemId)
	{
		EntityKey entityKey = new EntityKey(publicId, systemId);
		if ( getResolvedEntities().containsKey(entityKey) )
			return getResolvedEntities().get(entityKey);
		else
		{
			String resolvedEntity = resolveEntity(entityKey);
			if ( resolvedEntity != null )
				getResolvedEntities().put(entityKey, resolvedEntity);
			return resolvedEntity;
		}
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
	 * Typically, this method is used by the caller to get an {@link InputStream} using
	 * {@link URL#openStream()} for a <code>systemId</code> with the <code>maven:</code>
	 * scheme using the {@link java.net.JarURLConnection} path syntax <code>jar:url!/{entry}</code>.
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
	private String resolveEntity(EntityKey entityKey)
	{
		String resolvedEntity = null;
		
		String publicId = entityKey.getPublicId();
		String systemId = entityKey.getSystemId();
		
		getLog().debug(format("Resolving publicId [%s], systemId [%s].", publicId, systemId));

		// First, this method attempts to resolve the systemId from super class method.
		final String superResolvedEntity = superResolveEntity(publicId, systemId);
		
		if ( superResolvedEntity != null )
			systemId = superResolvedEntity;
		
		if ( systemId != null )
		{
			try
			{
				// Second, if the systemId matches the "maven:" scheme then use a dependency resource.
				final URI systemIdURI = new URI(systemId);
				if ( URI_SCHEME_FILE.equals(systemIdURI.getScheme()))
				{
					String systemFilename = (systemIdURI.getPath() == null) ? systemIdURI.getPath() : systemIdURI.getSchemeSpecificPart();
					if ( systemFilename != null )
					{
						File systemFile =new File(systemFilename);
						if ( systemFile.exists() )
						{
							if ( systemFile.canRead() )
							{
								resolvedEntity = systemId;
								getLog().debug(format("Resolved systemId [%s].", resolvedEntity));
							}
							else
							{
								getLog().warn(format(
									"Cannot read systemId [%s] as '%s:' resource.\n\tReturning parent resolver result [%s].",
									systemId, URI_SCHEME_FILE, superResolvedEntity));
								resolvedEntity = superResolvedEntity;
							}
						}
						else
						{
							getLog().warn(format(
								"Local systemId [%s] does not exist as '%s:' resource.\n\tReturning parent resolver result [%s].",
								systemId, URI_SCHEME_FILE, superResolvedEntity));
							resolvedEntity = superResolvedEntity;
						}
					}
					else
					{
						getLog().warn(format(
							"Local systemId [%s] does not parse as '%s:' resource.\n\tReturning parent resolver result [%s].",
							systemId, URI_SCHEME_FILE, superResolvedEntity));
						resolvedEntity = superResolvedEntity;
					}
				}
				else if (URI_SCHEME_MAVEN.equals(systemIdURI.getScheme()))
				{
					getLog().debug(format("Resolving systemId [%s] as Maven dependency resource.", systemId));
					final String schemeSpecificPart = systemIdURI.getSchemeSpecificPart();
					getLog().debug("Resolving path [" + schemeSpecificPart + "].");
					try
					{
						final URL url = resolveResource(schemeSpecificPart);
						if ( url != null )
						{
							resolvedEntity = url.toString();
							getLog().debug(format("Resolved systemId [%s] to [%s].", systemId, resolvedEntity));
						}
						else
						{
							getLog().debug(format("NOT Resolved systemId [%s]\n\tReturning parent resolver result [%s].",
								systemId, superResolvedEntity));
							resolvedEntity = superResolvedEntity;
						}
					}
					catch (IllegalArgumentException iaex)
					{
						//getLog().error(format("Error parsing dependency descriptor [%s].", schemeSpecificPart), iaex);
						getLog().warn(format(
							"Failed to resolve systemId [%s] as dependency resource.\n\tReturning parent resolver result [%s].",
							systemId, superResolvedEntity));
						resolvedEntity = superResolvedEntity;
					}
				}
				else
				{
					getLog().debug(format(
						"SystemId [%s] is a URI with a '%s:' scheme.\n\tReturning parent resolver result [%s].",
						systemId, systemIdURI.getScheme(), superResolvedEntity));
					resolvedEntity = superResolvedEntity;
				}
			}
			catch (URISyntaxException urisex)
			{
				getLog().warn(format(
					"Could not parse the systemId [%s] as URI.\n\tReturning parent resolver result [%s].",
					systemId, superResolvedEntity));
				resolvedEntity = superResolvedEntity;
			}
		}
		else
		{
			getLog().warn(format(
				"Could not parse the systemId [%s].\n\tReturning parent resolver result [%s].",
				systemId, superResolvedEntity));
			resolvedEntity = superResolvedEntity;
		}

		return resolvedEntity;
	}
	
	private String superResolveEntity(String publicId, String systemId)
	{
		String superResolvedEntity = super.getResolvedEntity(publicId, systemId);
		if ( superResolvedEntity != null )
		{
			getLog().debug(format("Parent resolver resolved publicId [%s], systemId [%s] to [%s].",
				publicId, systemId, superResolvedEntity));
		}
		else
		{
			getLog().debug(format("Parent resolver did not resolve publicId [%s], systemId [%s].",
				publicId, systemId));
		}
		return superResolvedEntity;
	}
	
	/**
	 * Resolve a resource from a Maven dependency such as an XML schema
	 * from a JAR artifact.
	 * 
	 * @param resource A DependencyResource value to resolve.
	 * 
	 * @return A URL representing a dependency resource or null when the resource cannot be resolved.
	 */
	public URL resolveResource(String resource)
	{
		URL url = null;
		final DependencyResource dependencyResource = DependencyResource.valueOf(resource);
		try
		{
			if ((dependencyResource.getVersion()) != null && dependencyResource.getVersion().startsWith("!") )
				getLog().warn("Resource path may not be delimited properly with '!/'");
			url = getDependencyResourceResolver().resolveDependencyResource(dependencyResource);
		}
		catch (Exception ex)
		{
			getLog().error(format("Error resolving dependency resource [%s].", dependencyResource), ex);
		}
		return url;
	}
}
