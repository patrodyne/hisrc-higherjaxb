package org.jvnet.higherjaxb.mojo.resolver.tools;

import static com.sun.org.apache.xml.internal.resolver.CatalogManager.getStaticManager;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.jvnet.higherjaxb.mojo.DependencyResourceResolver;
import org.jvnet.higherjaxb.mojo.plugin.logging.NullLog;
import org.jvnet.higherjaxb.mojo.resolver.EntityKey;

import com.sun.org.apache.xml.internal.resolver.CatalogManager;

/**
 * A CatalogResolver to parse a catalog uri for a "classpath" scheme.
 * The "classpath" scheme has the form:
 * 
 * <pre>
 * classpath:/resource/path/in/jar/schema.xsd
 * </pre>
 * 
 */
public class ClasspathCatalogResolver 
	extends com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver
{
	public static final String URI_SCHEME_FILE = "file";
	public static final String URI_SCHEME_CLASSPATH = "classpath";

	private Log log;
	public Log getLog() { return log; }
	public void setLog(Log log) { this.log = log; }
	
	private ClassLoader classloader;
	public ClassLoader getClassloader()
	{
		return classloader;
	}
	public void setClassloader(ClassLoader classloader)
	{
		this.classloader = classloader;
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
	
	/** Constructor: default */
	public ClasspathCatalogResolver()
	{
		this(getStaticManager());
	}
	
	/**
	 * Construct with a{@link CatalogManager} and default logger.
	 * 
	 * @param catalogManager Provides an interface to the catalog methods.
	 */
	public ClasspathCatalogResolver(CatalogManager catalogManager)
	{
		this(catalogManager, currentThread().getContextClassLoader(), NullLog.INSTANCE);
	}

	/**
	 * Construct with a {@link Log} instance.
	 * 
	 * @param catalogManager Provides an interface to the catalog properties.
	 * #param classLoader Object responsible for loading classes and reading resources.
	 * @param log A {@link Log} instance.
	 */
	public ClasspathCatalogResolver(CatalogManager catalogManager, ClassLoader classLoader, Log log)
	{
		super(catalogManager != null ? catalogManager : getStaticManager());
		setCatalogManager(catalogManager != null ? catalogManager : getStaticManager());
		setClassloader(classLoader);
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
	 * to resolve the custom "classpath:" scheme. First, this method attempts to resolve the
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
				// Second, if the systemId matches the "classpath:" scheme then use a classpath resource.
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
				else if ( URI_SCHEME_CLASSPATH.equals(systemIdURI.getScheme()) )
				{
					getLog().debug(format("Resolving systemId [%s] as classpath resource.", systemId));
					final String schemeSpecificPart = systemIdURI.getSchemeSpecificPart();
					getLog().debug("Resolving path [" + schemeSpecificPart + "].");
					
					try
					{
						URI indicator = resolveResource(schemeSpecificPart, null);
						if ( indicator != null )
						{
							resolvedEntity = indicator.toURL().toString();
							getLog().debug(format("Resolved systemId [%s] to [%s].", systemId, resolvedEntity));
						}
						else
						{
							getLog().debug(format("NOT Resolved systemId [%s]\n\tReturning parent resolver result [%s].",
								systemId, superResolvedEntity));
							resolvedEntity = superResolvedEntity;
						}
					}
					catch (IllegalArgumentException | MalformedURLException ex)
					{
						//getLog().error(format("Error parsing resource descriptor [%s].", schemeSpecificPart), ex);
						getLog().warn(format(
							"Failed to resolve systemId [%s] as '%s:' resource.\n\tReturning parent resolver result [%s].",
							systemId, URI_SCHEME_CLASSPATH, superResolvedEntity));
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
	 * Resolve a locator to an {@link URI}. The locator may represent a
	 * URL or a File. If the URL protocol is "classpath:" then the URI
	 * will be located as a resource.
	 * 
	 * If the class parameter is null, a {@link ClassLoader} is used to
	 * resolve classpath resources; thus, a "classpath:" locator must
	 * provide the full path relative to the classpath root and any
	 * leading '/' will be ignored.
	 * 
	 * @param resource The location of a file or resource.
	 * @param clazz A classpath location for relative locators.
	 * 
	 * @return A {@link URI} representing the locator.
	 */
	public URI resolveResource(String resource, Class<?> clazz)
	{
		URI resourceURI = null;
		try
		{
			URL resourceURL = null;
			
			if ( resource != null )
			{
				if ( clazz != null )
					resourceURL = clazz.getResource(resource);	
				else
				{
					if ( resource.startsWith("/") )
						resource = resource.substring(1);
					resourceURL = getClassloader().getResource(resource);
				}

				if ( resourceURL != null )
				{
					resourceURI = resourceURL.toURI();
					getLog().debug(format("Resolved: '%s' to '%s'", resource, resourceURI));
				}
				else
					getLog().warn(format("NOT Resolved: '%s'", resource));
			}
			else
				getLog().warn("resolveLocator: locator is null");
		}
		catch (Exception ex)
		{
			getLog().warn(ex.getClass().getSimpleName() + ": cannot resolve " + resource);
		}
		return resourceURI;
	}
}
