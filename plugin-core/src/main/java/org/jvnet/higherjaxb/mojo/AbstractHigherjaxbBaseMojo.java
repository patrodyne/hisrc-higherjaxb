package org.jvnet.higherjaxb.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.jvnet.higherjaxb.mojo.net.CompositeURILastModifiedResolver;
import org.jvnet.higherjaxb.mojo.net.FileURILastModifiedResolver;
import org.jvnet.higherjaxb.mojo.net.URILastModifiedResolver;
import org.jvnet.higherjaxb.mojo.resolver.tools.ClasspathCatalogResolver;
import org.jvnet.higherjaxb.mojo.resolver.tools.MavenCatalogResolver;
import org.jvnet.higherjaxb.mojo.resolver.tools.ReResolvingEntityResolverWrapper;
import org.jvnet.higherjaxb.mojo.util.ArtifactUtils;
import org.jvnet.higherjaxb.mojo.util.CollectionUtils;
import org.jvnet.higherjaxb.mojo.util.CollectionUtils.Function;
import org.jvnet.higherjaxb.mojo.util.IOUtils;
import org.jvnet.higherjaxb.mojo.util.LocaleUtils;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;

/**
 * This Maven abstract higherjaxb 'base' mojo provides common properties and methods
 * to the concrete version specific implementations.
 * 
 * @param <O> Stores invocation options for XJC.
 * 
 * @author Aleksei Valikov (valikov@gmx.net)
 */
public abstract class AbstractHigherjaxbBaseMojo<O> extends AbstractHigherjaxbParmMojo<O> {

	public static final String ADD_IF_EXISTS_TO_EPISODE_SCHEMA_BINDINGS_TRANSFORMATION_RESOURCE_NAME =
		"/"	+ AbstractHigherjaxbBaseMojo.class.getPackage().getName().replace('.', '/') + "/addIfExistsToEpisodeSchemaBindings.xslt";

	private Collection<Artifact> xjcPluginArtifacts;
	public Collection<Artifact> getXjcPluginArtifacts() { return xjcPluginArtifacts; }
	public void setXjcPluginArtifacts(Collection<Artifact> xjcPluginArtifacts) { this.xjcPluginArtifacts = xjcPluginArtifacts; }

	private Collection<File> xjcPluginFiles;
	public Collection<File> getXjcPluginFiles() { return xjcPluginFiles; }
	public void setXjcPluginFiles(Collection<File> xjcPluginFiles) { this.xjcPluginFiles = xjcPluginFiles; }

	private List<URL> xjcPluginURLs;
	public List<URL> getXjcPluginURLs() { return xjcPluginURLs; }
	public void setXjcPluginURLs(List<URL> xjcPluginURLs) { this.xjcPluginURLs = xjcPluginURLs; }
	
	private Collection<Artifact> episodeArtifacts;
	public Collection<Artifact> getEpisodeArtifacts() { return episodeArtifacts; }
	public void setEpisodeArtifacts(Collection<Artifact> episodeArtifacts) { this.episodeArtifacts = episodeArtifacts; }

	private Collection<File> episodeFiles;
	public Collection<File> getEpisodeFiles() { return episodeFiles; }
	public void setEpisodeFiles(Collection<File> episodeFiles) { this.episodeFiles = episodeFiles; }

	private List<File> schemaFiles;
	public List<File> getSchemaFiles() { return schemaFiles; }
	public void setSchemaFiles(List<File> schemaFiles) { this.schemaFiles = schemaFiles; }

	private List<URI> schemaURIs;
	protected List<URI> getSchemaURIs()
	{
		if (schemaURIs == null)
			throw new IllegalStateException("Schema URIs were not set up yet.");
		return schemaURIs;
	}
	protected void setSchemaURIs(List<URI> schemaURIs) { this.schemaURIs = schemaURIs; }
	
	private List<URI> resolvedSchemaURIs;
	protected List<URI> getResolvedSchemaURIs()
	{
		if (resolvedSchemaURIs == null)
			throw new IllegalStateException("Resolved schema URIs were not set up yet.");
		return resolvedSchemaURIs;
	}
	protected void setResolvedSchemaURIs(List<URI> resolvedSchemaURIs) { this.resolvedSchemaURIs = resolvedSchemaURIs; }
	
	private List<InputSource> grammars;
	protected List<InputSource> getGrammars()
	{
		if (grammars == null)
			throw new IllegalArgumentException("Grammars were not set up yet.");
		return grammars;
	}
	protected void setGrammars(List<InputSource> grammars) { this.grammars = grammars; }
	
	private void setupSchemas() throws MojoExecutionException
	{
		setSchemaURIs(createSchemaURIs());
		setResolvedSchemaURIs(resolveURIs(getSchemaURIs()));
		setGrammars(createGrammars());
	}

	private List<URI> createSchemaURIs() throws MojoExecutionException
	{
		final List<File> schemaFiles = getSchemaFiles();
		final List<URI> schemaURIs = new ArrayList<URI>(schemaFiles.size());
		for (final File schemaFile : schemaFiles)
		{
			final URI schema = schemaFile.toURI();
			schemaURIs.add(schema);
		}
		final ResourceEntry[] schemas = getSchemas();
		if (schemas != null)
		{
			for (ResourceEntry resourceEntry : schemas)
			{
				schemaURIs.addAll(createResourceEntryUris(resourceEntry, getSchemaDirectory().getAbsolutePath(),
					getSchemaIncludes(), getSchemaExcludes()));
			}
		}
		return schemaURIs;
	}

	private List<InputSource> createGrammars() throws MojoExecutionException
	{
		try
		{
			final List<URI> schemaURIs = getSchemaURIs();
			return createInputSources(schemaURIs);
		}
		catch (IOException ioex)
		{
			throw new MojoExecutionException("Could not resolve grammars.", ioex);
		}
		catch (SAXException ioex)
		{
			throw new MojoExecutionException("Could not resolve grammars.", ioex);
		}
	}

	private List<File> bindingFiles;
	public List<File> getBindingFiles() { return bindingFiles; }
	public void setBindingFiles(List<File> bindingFiles) { this.bindingFiles = bindingFiles; }
	
	private List<URI> bindingURIs;
	protected List<URI> getBindingURIs()
	{
		if (bindingURIs == null)
			throw new IllegalStateException("Binding URIs were not set up yet.");
		return bindingURIs;
	}
	protected void setBindingURIs(List<URI> bindingURIs) { this.bindingURIs = bindingURIs; }

	private List<URI> resolvedBindingURIs;
	protected List<URI> getResolvedBindingURIs()
	{
		if (resolvedBindingURIs == null)
			throw new IllegalStateException("Resolved binding URIs were not set up yet.");
		return resolvedBindingURIs;
	}
	protected void setResolvedBindingURIs(List<URI> resolvedBindingURIs) { this.resolvedBindingURIs = resolvedBindingURIs; }
	
	private List<InputSource> bindFiles;
	protected List<InputSource> getBindFiles()
	{
		if (bindFiles == null)
			throw new IllegalStateException("BindFiles were not set up yet.");
		return bindFiles;
	}
	protected void setBindFiles(List<InputSource> bindFiles) { this.bindFiles = bindFiles; }

	private void setupBindings() throws MojoExecutionException
	{
		setBindingURIs(createBindingURIs());
		setResolvedBindingURIs(resolveURIs(getBindingURIs()));
		setBindFiles(createBindFiles());
	}

	protected List<URI> createBindingURIs() throws MojoExecutionException
	{
		final List<File> bindingFiles = new LinkedList<File>();
		bindingFiles.addAll(getBindingFiles());

		for (final File episodeFile : getEpisodeFiles())
		{
			getLog().debug(MessageFormat.format("Checking episode file [{0}].", episodeFile.getAbsolutePath()));
			if (episodeFile.isDirectory())
			{
				final File episodeMetaInfFile = new File(episodeFile, "META-INF");
				if (episodeMetaInfFile.isDirectory())
				{
					final File episodeBindingsFile = new File(episodeMetaInfFile, "sun-jaxb.episode");
					if (episodeBindingsFile.isFile())
						bindingFiles.add(episodeBindingsFile);
				}
			}
		}

		final List<URI> bindingUris = new ArrayList<URI>(bindingFiles.size());
		for (final File bindingFile : bindingFiles)
		{
			URI uri;
			uri = bindingFile.toURI();
			bindingUris.add(uri);
		}
		
		if (getBindings() != null)
		{
			for (ResourceEntry resourceEntry : getBindings())
			{
				bindingUris.addAll(createResourceEntryUris(resourceEntry, getBindingDirectory().getAbsolutePath(),
					getBindingIncludes(), getBindingExcludes()));
			}
		}

		if (getScanDependenciesForBindings())
			collectBindingUrisFromDependencies(bindingUris);

		return bindingUris;
	}

	private List<InputSource> createBindFiles() throws MojoExecutionException
	{
		try
		{
			final List<URI> bindingURIs = getBindingURIs();
			return createInputSources(bindingURIs);
		}
		catch (IOException ioex)
		{
			throw new MojoExecutionException("Could not resolve binding files.", ioex);
		}
		catch (SAXException ioex)
		{
			throw new MojoExecutionException("Could not resolve binding files.", ioex);
		}
	}

	private List<URI> dependsURIs;
	public List<URI> getDependsURIs() { return dependsURIs; }
	public void setDependsURIs(List<URI> dependsURIs) { this.dependsURIs = dependsURIs; }
	
	private List<URI> producesURIs;
	public List<URI> getProducesURIs() { return producesURIs; }
	public void setProducesURIs(List<URI> producesURIs) { this.producesURIs = producesURIs; }

	private static final Object lock = new Object();

	/**
	 * Execute the maven2 mojo to invoke the XJC compiler based on any
	 * configuration settings.
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		synchronized (lock)
		{
			injectDependencyDefaults();
			resolveArtifacts();

			// Install project dependencies into classloader's class path
			// and execute XJC.
			final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			final ClassLoader classLoader = createClassLoader(currentClassLoader);
			Thread.currentThread().setContextClassLoader(classLoader);
			final Locale currentDefaultLocale = Locale.getDefault();
			
			try
			{
				final Locale locale = LocaleUtils.valueOf(getLocale());
				Locale.setDefault(locale);
				//
				doExecute();

			}
			finally
			{
				Locale.setDefault(currentDefaultLocale);
				// Set back the old classloader
				Thread.currentThread().setContextClassLoader(currentClassLoader);
			}
		}
	}

	/**
	 * ************************************************************************* *
	 */

	protected void injectDependencyDefaults()
	{
		injectDependencyDefaults(getPlugins());
		injectDependencyDefaults(getEpisodes());
	}

	protected void injectDependencyDefaults(Dependency[] dependencies)
	{
		if (dependencies != null)
		{
			final Map<String, Dependency> dependencyMap = new TreeMap<String, Dependency>();
			
			for (final Dependency dependency : dependencies)
			{
				if (dependency.getScope() == null)
					dependency.setScope(Artifact.SCOPE_RUNTIME);
				dependencyMap.put(dependency.getManagementKey(), dependency);
			}

			final DependencyManagement dependencyManagement = getProject().getDependencyManagement();

			if (dependencyManagement != null)
				merge(dependencyMap, dependencyManagement.getDependencies());
			
			merge(dependencyMap, getProjectDependencies());
		}
	}

	private void merge(final Map<String, Dependency> dependencyMap, final List<Dependency> managedDependencies)
	{
		for (final Dependency managedDependency : managedDependencies)
		{
			final String key = managedDependency.getManagementKey();
			final Dependency dependency = (Dependency) dependencyMap.get(key);
			if (dependency != null)
			{
				ArtifactUtils.mergeDependencyWithDefaults(dependency, managedDependency);
			}
		}
	}

	protected void resolveArtifacts() throws MojoExecutionException {
		try
		{
			resolveXJCPluginArtifacts();
			resolveEpisodeArtifacts();
		}
		catch (ArtifactResolutionException arex)
		{
			throw new MojoExecutionException("Could not resolve the artifact.", arex);
		}
		catch (ArtifactNotFoundException anfex)
		{
			throw new MojoExecutionException("Artifact not found.", anfex);
		}
		catch (InvalidDependencyVersionException idvex)
		{
			throw new MojoExecutionException("Invalid dependency version.", idvex);
		}
	}

	protected void resolveXJCPluginArtifacts()
		throws ArtifactResolutionException, ArtifactNotFoundException, InvalidDependencyVersionException
	{
		setXjcPluginArtifacts(ArtifactUtils.resolveTransitively(getArtifactFactory(), getArtifactResolver(),
			getLocalRepository(), getArtifactMetadataSource(), getPlugins(), getProject()));
		setXjcPluginFiles(ArtifactUtils.getFiles(getXjcPluginArtifacts()));
		setXjcPluginURLs(CollectionUtils.apply(getXjcPluginFiles(), IOUtils.GET_URL));
	}

	protected void resolveEpisodeArtifacts()
		throws ArtifactResolutionException, ArtifactNotFoundException, InvalidDependencyVersionException
	{
		setEpisodeArtifacts(new LinkedHashSet<Artifact>());
		{
			final Collection<Artifact> episodeArtifacts = ArtifactUtils.resolve(getArtifactFactory(),
				getArtifactResolver(), getLocalRepository(), getArtifactMetadataSource(), getEpisodes(),
				getProject());
			getEpisodeArtifacts().addAll(episodeArtifacts);
		}
		{
			if (getUseDependenciesAsEpisodes())
			{
				final Collection<Artifact> projectArtifacts = getProject().getArtifacts();
				final AndArtifactFilter filter = new AndArtifactFilter();
				filter.add(new ScopeArtifactFilter(DefaultArtifact.SCOPE_COMPILE));
				filter.add(new TypeArtifactFilter("jar"));
				for (Artifact artifact : projectArtifacts)
				{
					if (filter.include(artifact))
						getEpisodeArtifacts().add(artifact);
				}
			}
		}
		setEpisodeFiles(ArtifactUtils.getFiles(getEpisodeArtifacts()));
	}

	protected ClassLoader createClassLoader(ClassLoader parent)
	{
		final Collection<URL> xjcPluginURLs = getXjcPluginURLs();
		return new ParentFirstClassLoader(xjcPluginURLs.toArray(new URL[xjcPluginURLs.size()]), parent);
	}

	protected void doExecute() throws MojoExecutionException
	{
		setupLogging();
		if (getVerbose())
			getLog().info("Started execution.");
		setupBindInfoPackage();
		setupEpisodePackage();
		setupMavenPaths();
		setupCatalogResolver();
		setupEntityResolver();
		setupSchemaFiles();
		setupBindingFiles();
		setupSchemas();
		setupBindings();
		setupDependsURIs();
		setupProducesURIs();
		setupURILastModifiedResolver();
		if (getVerbose()) {
			logConfiguration();
		}

		final OptionsConfiguration optionsConfiguration = createOptionsConfiguration();

		if (getVerbose())
			getLog().info("MOJO optionsConfiguration:" + optionsConfiguration);

		checkCatalogsInStrictMode();

		if (getGrammars().isEmpty())
			getLog().warn("No schemas to compile. Skipping XJC execution. ");
		else
		{
			final O options = getOptionsFactory().createOptions(optionsConfiguration);

			if (getForceRegenerate())
			{
				getLog().warn("You are using forceRegenerate=true in your configuration.\n"
					+ "This configuration setting is deprecated and not recommended "
					+ "as it causes problems with incremental builds in IDEs.\n"
					+ "Please refer to the following link for more information:\n"
					+ "https://github.com/highsource/maven-jaxb2-plugin/wiki/Do-Not-Use-forceRegenerate\n"
					+ "Consider removing this setting from your plugin configuration.\n");
				getLog().info("The [forceRegenerate] switch is turned on, XJC will be executed.");
			}
			else
			{
				final boolean isUpToDate = isUpToDate();
				if (!isUpToDate)
					getLog().info("Sources are not up-to-date, XJC will be executed.");
				else
				{
					getLog().info("Sources are up-to-date, XJC will be skipped.");
					return;
				}
			}

			setupDirectories();
			doExecute(options);
			addIfExistsToEpisodeSchemaBindings();
			final BuildContext buildContext = getBuildContext();
			getLog().debug(MessageFormat.format("Refreshing the generated directory [{0}].",
					getGenerateDirectory().getAbsolutePath()));
			buildContext.refresh(getGenerateDirectory());
		}

		if (getVerbose())
			getLog().info("Finished execution.");
	}

	abstract protected String getJaxbNamespaceURI();
	abstract protected String[] getXmlSchemaNames(final Class<?> packageInfoClass);

	protected void setupBindInfoPackage()
	{
		String packageInfoClassName = "com.sun.tools.xjc.reader.xmlschema.bindinfo.package-info";
		try
		{
			final Class<?> packageInfoClass = Class.forName(packageInfoClassName);
			final String[] xmlSchemaNames = getXmlSchemaNames(packageInfoClass);
			final String xmlSchemaNamespace = xmlSchemaNames[0];
			final String xmlSchemaClassname = xmlSchemaNames[1];
			if (xmlSchemaClassname == null)
			{
				getLog().warn(MessageFormat.format(
					"Class [{0}] is missing the [{1}] annotation. Processing bindings will probably fail.",
					packageInfoClassName, xmlSchemaClassname));
			}
			else
			{
				if (!getJaxbNamespaceURI().equals(xmlSchemaNamespace))
				{
					getLog().warn(MessageFormat.format(
						"Namespace of the [{0}] annotation is [{1}] and does not match [{2}]. Processing bindings will probably fail.",
						xmlSchemaClassname, xmlSchemaNamespace, getJaxbNamespaceURI()));
				}
			}
		}
		catch (ClassNotFoundException cnfex)
		{
			getLog()
				.warn(MessageFormat.format("Class [{0}] could not be found. Processing bindings will probably fail.",
					packageInfoClassName), cnfex);
		}
	}

	abstract protected String getEpisodePackageName();
	abstract protected String[] getXmlNamespaceNames(final Class<?> packageInfoClass);
	
	private void setupEpisodePackage()
	{
		String packageInfoClassName = "org.glassfish.jaxb.core.v2.schemagen.episode.package-info";
		try
		{
			final Class<?> packageInfoClass = Class.forName(getEpisodePackageName());
			final String[] xmlSchemaNames = getXmlNamespaceNames(packageInfoClass);
			final String xmlSchemaNamespace = xmlSchemaNames[0];
			final String xmlSchemaClassname = xmlSchemaNames[1];
			if (xmlSchemaNamespace == null)
			{
				getLog().warn(MessageFormat.format(
					"Class [{0}] is missing the [{1}] annotation. Processing bindings will probably fail.",
					packageInfoClassName, xmlSchemaClassname));
			}
			else
			{
				if (!getJaxbNamespaceURI().equals(xmlSchemaNamespace))
				{
					getLog().warn(MessageFormat.format(
						"Namespace of the [{0}] annotation is [{1}] and does not match [{2}]. Processing bindings will probably fail.",
						xmlSchemaClassname, xmlSchemaNamespace, getJaxbNamespaceURI()));
				}
			}
		}
		catch (ClassNotFoundException cnfex)
		{
			getLog()
				.warn(MessageFormat.format("Class [{0}] could not be found. Processing bindings will probably fail.",
					packageInfoClassName), cnfex);
		}
	}

	private void addIfExistsToEpisodeSchemaBindings() throws MojoExecutionException
	{
		if (!getEpisode() || !isAddIfExistsToEpisodeSchemaBindings())
			return;
		
		final File episodeFile = getEpisodeFile();
		if (!episodeFile.canWrite())
		{
			getLog().warn(MessageFormat.format(
				"Episode file [{0}] is not writable, could not add if-exists attributes.",
				episodeFile));
			return;
		}
		
		try ( InputStream is = getClass().getResourceAsStream(ADD_IF_EXISTS_TO_EPISODE_SCHEMA_BINDINGS_TRANSFORMATION_RESOURCE_NAME) )
		{
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer addIfExistsToEpisodeSchemaBindingsTransformer = transformerFactory
				.newTransformer(new StreamSource(is));
			final DOMResult result = new DOMResult();
			addIfExistsToEpisodeSchemaBindingsTransformer.transform(new StreamSource(episodeFile), result);
			final DOMSource source = new DOMSource(result.getNode());
			final Transformer identityTransformer = transformerFactory.newTransformer();
			identityTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
			identityTransformer.transform(source, new StreamResult(episodeFile));
			getLog().info(MessageFormat.format(
				"Episode file [{0}] was augmented with if-exists=\"true\" attributes.",
				episodeFile));
		}
		catch (TransformerException | IOException ex)
		{
			throw new MojoExecutionException(MessageFormat.format(
				"Error augmenting the episode file [{0}] with if-exists=\"true\" attributes. Transformation failed with an unexpected error.",
				episodeFile), ex);
		}
	}

	private URILastModifiedResolver uriLastModifiedResolver;
	protected URILastModifiedResolver getURILastModifiedResolver()
	{
		if (uriLastModifiedResolver == null)
			throw new IllegalStateException("URILastModifiedResolver was not set up yet.");
		return uriLastModifiedResolver;
	}
	protected void setURILastModifiedResolver(URILastModifiedResolver uriLastModifiedResolver) { this.uriLastModifiedResolver = uriLastModifiedResolver; }
	
	private void setupURILastModifiedResolver()
	{
		setURILastModifiedResolver(new CompositeURILastModifiedResolver(getLog()));
	}

	private void checkCatalogsInStrictMode()
	{
		if (getStrict() && !getCatalogURIs().isEmpty())
		{
			getLog().warn("The plugin is configured to use catalogs and strict mode at the same time.\n"
				+ "Using catalogs to resolve schema URIs in strict mode is known to be problematic and may fail.\n"
				+ "Please refer to the following link for more information:\n"
				+ "https://github.com/highsource/maven-jaxb2-plugin/wiki/Catalogs-in-Strict-Mode\n"
				+ "Consider setting <strict>false</strict> in your plugin configuration.\n");
		}
	}

	public abstract void doExecute(O options) throws MojoExecutionException;

	/**
	 * Initializes logging. If Maven is run in debug mode (that is, debug level is
	 * enabled in the log), turn on the verbose mode in Mojo. Further on, if vebose
	 * mode is on, set the <code>com.sun.tools.xjc.Options.findServices</code>
	 * system property on to enable debuggin of XJC plugins.
	 * 
	 */
	protected void setupLogging()
	{
		setVerbose(getVerbose() || getLog().isDebugEnabled());

		if (getVerbose())
			System.setProperty("com.sun.tools.xjc.Options.findServices", "true");
	}

	/**
	 * Augments Maven paths with generated resources.
	 */
	protected void setupMavenPaths()
	{
		if (getAddCompileSourceRoot())
			getProject().addCompileSourceRoot(getGenerateDirectory().getPath());
		
		if (getAddTestCompileSourceRoot())
			getProject().addTestCompileSourceRoot(getGenerateDirectory().getPath());
		
		if (getEpisode() && getEpisodeFile() != null)
		{
			final String episodeFilePath = getEpisodeFile().getAbsolutePath();
			final String generatedDirectoryPath = getGenerateDirectory().getAbsolutePath();

			if (episodeFilePath.startsWith(generatedDirectoryPath + File.separator))
			{
				final String path = episodeFilePath.substring(generatedDirectoryPath.length() + 1);

				final Resource resource = new Resource();
				resource.setDirectory(generatedDirectoryPath);
				resource.addInclude(path);
				
				if (getAddCompileSourceRoot())
					getProject().addResource(resource);
				
				if (getAddTestCompileSourceRoot())
					getProject().addTestResource(resource);
			}
		}
	}

	protected void setupDirectories()
	{
		final File generateDirectory = getGenerateDirectory();
		if (getRemoveOldOutput() && generateDirectory.exists())
		{
			try
			{
				FileUtils.deleteDirectory(this.getGenerateDirectory());
			}
			catch (IOException ex)
			{
				getLog().warn("Failed to remove old generateDirectory [" + generateDirectory + "].", ex);
			}
		}

		// Create the destination path if it does not exist.
		if (generateDirectory != null && !generateDirectory.exists())
			generateDirectory.mkdirs();

		final File episodeFile = getEpisodeFile();
		if (getEpisode() && episodeFile != null)
		{
			final File parentFile = episodeFile.getParentFile();
			parentFile.mkdirs();
		}
	}

	protected void setupSchemaFiles() throws MojoExecutionException
	{
		try
		{
			final File schemaDirectory = getSchemaDirectory();
			if (schemaDirectory == null || !schemaDirectory.exists())
			{
				setSchemaFiles(Collections.emptyList());
			}
			else if (schemaDirectory.isDirectory())
			{
				setSchemaFiles(IOUtils.scanDirectoryForFiles(getBuildContext(), schemaDirectory,
					getSchemaIncludes(), getSchemaExcludes(), !getDisableDefaultExcludes()));

			}
			else
			{
				setSchemaFiles(Collections.emptyList());
				getLog().warn(MessageFormat.format(
					"Schema directory [{0}] is not a directory.", 
					schemaDirectory.getPath()));
			}
		}
		catch (IOException ioex)
		{
			throw new MojoExecutionException("Could not set up schema files.", ioex);
		}
	}

	protected void setupBindingFiles() throws MojoExecutionException
	{
		try
		{
			final File bindingDirectory = getBindingDirectory();
			if (bindingDirectory == null || !bindingDirectory.exists())
				setBindingFiles(Collections.emptyList());
			else if (bindingDirectory.isDirectory())
			{
				setBindingFiles(IOUtils.scanDirectoryForFiles(getBuildContext(), bindingDirectory,
					getBindingIncludes(), getBindingExcludes(), !getDisableDefaultExcludes()));
			}
			else
			{
				setBindingFiles(Collections.emptyList());
				getLog().warn(MessageFormat.format("Binding directory [{0}] is not a directory.",
						bindingDirectory.getPath()));
			}
		}
		catch (IOException ioex)
		{
			throw new MojoExecutionException("Could not set up binding files.", ioex);
		}
	}

	protected void setupDependsURIs() throws MojoExecutionException
	{
		final List<URI> dependsURIs = new LinkedList<URI>();

		dependsURIs.addAll(getResolvedCatalogURIs());
		dependsURIs.addAll(getResolvedSchemaURIs());
		dependsURIs.addAll(getResolvedBindingURIs());
		final File projectFile = getProject().getFile();
		
		if (projectFile != null)
			dependsURIs.add(projectFile.toURI());
		
		if (getOtherDependsIncludes() != null)
		{
			try
			{
				List<File> otherDependsFiles = IOUtils.scanDirectoryForFiles(getBuildContext(),
					getProject().getBasedir(), getOtherDependsIncludes(), getOtherDependsExcludes(),
					!getDisableDefaultExcludes());
				
				for (File file : otherDependsFiles)
				{
					if (file != null)
						dependsURIs.add(file.toURI());
				}
			}
			catch (IOException ioex)
			{
				throw new MojoExecutionException("Could not set up [otherDepends] files.", ioex);
			}
		}
		setDependsURIs(dependsURIs);
	}

	private void setupProducesURIs() throws MojoExecutionException
	{
		setProducesURIs(createProducesURIs());
	}

	protected List<URI> createProducesURIs() throws MojoExecutionException
	{
		final List<URI> producesURIs = new LinkedList<URI>();
		try
		{
			final List<File> producesFiles = IOUtils.scanDirectoryForFiles(getBuildContext(), getGenerateDirectory(),
				getProduces(), new String[0], !getDisableDefaultExcludes());
			
			if (producesFiles != null)
			{
				for (File producesFile : producesFiles)
				{
					if (producesFile != null)
						producesURIs.add(producesFile.toURI());
				}
			}
			return producesURIs;
		}
		catch (IOException ioex)
		{
			throw new MojoExecutionException("Could not set up produced files.", ioex);
		}
	}

	/**
	 * Log the configuration settings. Shown when exception thrown or when verbose is true.
	 */
	@Override
	protected void logConfiguration() throws MojoExecutionException
	{
		super.logConfiguration();
		getLog().info("catalogURIs (calculated):" + getCatalogURIs());
		getLog().info("resolvedCatalogURIs (calculated):" + getResolvedCatalogURIs());
		getLog().info("schemaFiles (calculated):" + getSchemaFiles());
		getLog().info("schemaURIs (calculated):" + getSchemaURIs());
		getLog().info("resolvedSchemaURIs (calculated):" + getResolvedSchemaURIs());
		getLog().info("bindingFiles (calculated):" + getBindingFiles());
		getLog().info("bindingURIs (calculated):" + getBindingURIs());
		getLog().info("resolvedBindingURIs (calculated):" + getResolvedBindingURIs());
		getLog().info("xjcPluginArtifacts (resolved):" + getXjcPluginArtifacts());
		getLog().info("xjcPluginFiles (resolved):" + getXjcPluginFiles());
		getLog().info("xjcPluginURLs (resolved):" + getXjcPluginURLs());
		getLog().info("episodeArtifacts (resolved):" + getEpisodeArtifacts());
		getLog().info("episodeFiles (resolved):" + getEpisodeFiles());
		getLog().info("dependsURIs (resolved):" + getDependsURIs());
	}

	private void collectBindingUrisFromDependencies(List<URI> bindingUris) throws MojoExecutionException
	{
		final Collection<Artifact> projectArtifacts = getProject().getArtifacts();
		final List<Artifact> compileScopeArtifacts = new ArrayList<Artifact>(projectArtifacts.size());
		final ArtifactFilter filter = new ScopeArtifactFilter(DefaultArtifact.SCOPE_COMPILE);
		
		for (Artifact artifact : projectArtifacts)
		{
			if (filter.include(artifact))
				compileScopeArtifacts.add(artifact);
		}

		for (Artifact artifact : compileScopeArtifacts)
		{
			getLog().debug(MessageFormat.format("Scanning artifact [{0}] for JAXB binding files.", artifact));
			collectBindingUrisFromArtifact(artifact.getFile(), bindingUris);
		}
	}

	void collectBindingUrisFromArtifact(File file, List<URI> bindingUris) throws MojoExecutionException
	{
		try (JarFile jarFile = new JarFile(file))
		{
			final Enumeration<JarEntry> jarFileEntries = jarFile.entries();
			while (jarFileEntries.hasMoreElements())
			{
				JarEntry entry = jarFileEntries.nextElement();
				if (entry.getName().endsWith(".xjb"))
				{
					try
					{
						bindingUris.add(new URI("jar:" + file.toURI() + "!/" + entry.getName()));
					}
					catch (URISyntaxException urisex)
					{
						throw new MojoExecutionException(MessageFormat.format(
							"Could not create the URI of the binding file from [{0}]",
							entry.getName()), urisex);
					}
				}
			}
		}
		catch (IOException ioex)
		{
			throw new MojoExecutionException(
				"Unable to read the artifact JAR file [" + file.getAbsolutePath() + "].", ioex);
		}
	}

	private List<URI> catalogURIs;
	protected List<URI> getCatalogURIs()
	{
		if (catalogURIs == null)
			throw new IllegalStateException("Catalog URIs were not set up yet.");
		return catalogURIs;
	}
	protected void setCatalogURIs(List<URI> catalogURIs) { this.catalogURIs = catalogURIs; }

	private List<URI> resolvedCatalogURIs;
	protected List<URI> getResolvedCatalogURIs()
	{
		if (resolvedCatalogURIs == null)
			throw new IllegalStateException("Resolved catalog URIs were not set up yet.");
		return resolvedCatalogURIs;
	}
	protected void setResolvedCatalogURIs(List<URI> resolvedCatalogURIs) { this.resolvedCatalogURIs = resolvedCatalogURIs; }

	private CatalogResolver catalogResolverInstance;
	protected CatalogResolver getCatalogResolverInstance() {
		if (catalogResolverInstance == null)
			throw new IllegalStateException("Catalog resolver was not set up yet.");
		return catalogResolverInstance;
	}
	protected void setCatalogResolverInstance(CatalogResolver catalogResolverInstance) { this.catalogResolverInstance = catalogResolverInstance; }

	private void setupCatalogResolver() throws MojoExecutionException
	{
		setCatalogResolverInstance(createCatalogResolver());
		setCatalogURIs(createCatalogURIs());
		setResolvedCatalogURIs(resolveURIs(getCatalogURIs()));
		parseResolvedCatalogURIs();
	}

	private EntityResolver entityResolver;
	protected EntityResolver getEntityResolver()
	{
		if (entityResolver == null)
			throw new IllegalStateException("Entity resolver was not set up yet.");
		return entityResolver;
	}
	protected void setEntityResolver(EntityResolver entityResolver) { this.entityResolver = entityResolver; }

	private void setupEntityResolver()
	{
		setEntityResolver(createEntityResolver(getCatalogResolverInstance()));
	}

	protected EntityResolver createEntityResolver(CatalogResolver catalogResolver)
	{
		final EntityResolver entityResolver = new ReResolvingEntityResolverWrapper(catalogResolver);
		return entityResolver;
	}

	/**
	 * Creates an instance of catalog resolver.
	 * 
	 * @return Instance of the catalog resolver.
	 * @throws MojoExecutionException
	 *			   If catalog resolver cannot be instantiated.
	 */
	protected CatalogResolver createCatalogResolver() throws MojoExecutionException
	{
		CatalogResolver catalogResolver = null;
		
		final CatalogManager catalogManager = new CatalogManager();
		
		catalogManager.setIgnoreMissingProperties(true);
		catalogManager.setUseStaticCatalog(false);
		
		// Enable verbose logging
		if (getLog().isDebugEnabled())
		{
			// CatalogManager lazily reads its properties...
			int cmVerbosity = catalogManager.getVerbosity();
			catalogManager.setVerbosity(cmVerbosity);
			
			getLog().debug("CatalogManager (system, file)");
			getLog().debug("  xml.catalog.ignoreMissing, NONE.................: " + catalogManager.getIgnoreMissingProperties());
			getLog().debug("  xml.catalog.files, catalogs.....................: " + catalogManager.getCatalogFiles());
			getLog().debug("  NONE, relative-catalogs.........................: " + catalogManager.getRelativeCatalogs());
			getLog().debug("  xml.catalog.verbosity, verbosity................: " + catalogManager.getVerbosity());
			getLog().debug("  xml.catalog.prefer, prefer......................: " + catalogManager.getPreferPublic());
			getLog().debug("  xml.catalog.staticCatalog, static-catalog.......: " + catalogManager.getUseStaticCatalog());
			getLog().debug("  xml.catalog.allowPI, allow-oasis-xml-catalog-pi.: " + catalogManager.getAllowOasisXMLCatalogPI());
			getLog().debug("  xml.catalog.className, catalog-class-name.......: " + catalogManager.getCatalogClassName());
		}
		
		if (getCatalogResolver() == null)
			catalogResolver =  new MavenCatalogResolver(catalogManager, this, getLog());
		else
		{
			final String catalogResolverClassName = getCatalogResolver().trim();
			catalogResolver =  createCatalogResolverByClassName(catalogResolverClassName);
			if ( catalogResolver instanceof MavenCatalogResolver )
				((MavenCatalogResolver) catalogResolver).setLog(getLog());
			else if ( catalogResolver instanceof ClasspathCatalogResolver )
				((ClasspathCatalogResolver) catalogResolver).setLog(getLog());
		}
		
		return catalogResolver;
	}

	private CatalogResolver createCatalogResolverByClassName(final String catalogResolverClassName)
		throws MojoExecutionException
	{
		try
		{
			final Class<?> draftCatalogResolverClass = Thread.currentThread().getContextClassLoader()
				.loadClass(catalogResolverClassName);

			if (!CatalogResolver.class.isAssignableFrom(draftCatalogResolverClass))
			{
				throw new MojoExecutionException(MessageFormat.format(
					"Specified catalog resolver class [{0}] could not be casted to [{1}].",
					catalogResolver, CatalogResolver.class));
			}
			else
			{
				@SuppressWarnings("unchecked")
				final Class<? extends CatalogResolver> catalogResolverClass = (Class<? extends CatalogResolver>) draftCatalogResolverClass;
				final CatalogResolver catalogResolverInstance = catalogResolverClass.getDeclaredConstructor().newInstance();
				return catalogResolverInstance;
			}
		}
		catch (ClassNotFoundException cnfex)
		{
			throw new MojoExecutionException(
				MessageFormat.format("Could not find specified catalog resolver class [{0}].", catalogResolver), cnfex);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex )
		{
			throw new MojoExecutionException(
				MessageFormat.format("Could not instantiate catalog resolver class [{0}].", catalogResolver), ex);
		}
	}

	/**
	 * @return true to indicate results are up-to-date, that is, when the latest
	 *		   from input files is earlier than the younger from the output files
	 *		   (meaning no re-execution required).
	 */
	protected boolean isUpToDate()
	{
		final List<URI> dependsURIs = getDependsURIs();
		final List<URI> producesURIs = getProducesURIs();

		getLog().debug(MessageFormat.format("Up-to-date check for source resources [{0}] and target resources [{1}].",
			dependsURIs, producesURIs));

		boolean itIsKnownThatNoDependsURIsWereChanged = true;
		{
			for (URI dependsURI : dependsURIs)
			{
				if (FileURILastModifiedResolver.SCHEME.equalsIgnoreCase(dependsURI.getScheme()))
				{
					final File dependsFile = new File(dependsURI);
					if (getBuildContext().hasDelta(dependsFile))
					{
						if (getVerbose())
						{
							getLog().debug(MessageFormat.format(
								"File [{0}] might have been changed since the last build.",
								dependsFile.getAbsolutePath()));
						}
						// It is known that something was changed.
						itIsKnownThatNoDependsURIsWereChanged = false;
					}
				}
				else
				{
					// If this is not a file URI, we can't be sure
					itIsKnownThatNoDependsURIsWereChanged = false;
				}
			}
		}
		
		if (itIsKnownThatNoDependsURIsWereChanged)
		{
			getLog().info("According to the build context, all of the [dependURIs] are up-to-date.");
			return true;
		}

		final Function<URI, Long> LAST_MODIFIED = new Function<URI, Long>()
		{
			@Override
			public Long eval(URI uri)
			{
				return getURILastModifiedResolver().getLastModified(uri);
			}
		};

		getLog().debug(MessageFormat.format(
			"Checking the last modification timestamp of the source resources [{0}].",
			dependsURIs));

		final Long dependsTimestamp = CollectionUtils.bestValue(dependsURIs, LAST_MODIFIED,
			CollectionUtils.<Long>gtWithNullAsGreatest());

		getLog().debug(MessageFormat.format(
			"Checking the last modification timestamp of the target resources [{0}].",
			producesURIs));

		final Long producesTimestamp = CollectionUtils.bestValue(producesURIs, LAST_MODIFIED,
			CollectionUtils.<Long>ltWithNullAsSmallest());

		if (dependsTimestamp == null)
		{
			getLog().debug("Latest timestamp of the source resources is unknown. Assuming that something was changed.");
			return false;
		}
		
		if (producesTimestamp == null)
		{
			getLog().debug(MessageFormat.format(
				"Latest Timestamp of the source resources is [{0,date,yyyy-MM-dd HH:mm:ss.SSS}], however the earliest timestamp of the target resources is unknown. Assuming that something was changed.",
				dependsTimestamp));
			return false;
		}

		getLog().info(MessageFormat.format(
			"Latest timestamp of the source resources is [{0,date,yyyy-MM-dd HH:mm:ss.SSS}], earliest timestamp of the target resources is [{1,date,yyyy-MM-dd HH:mm:ss.SSS}].",
			dependsTimestamp, producesTimestamp));
		
		final boolean upToDate = dependsTimestamp < producesTimestamp;
		return upToDate;
	}

	protected String getCustomHttpproxy()
	{
		final String proxyHost = getProxyHost();
		final int proxyPort = getProxyPort();
		final String proxyUsername = getProxyUsername();
		final String proxyPassword = getProxyPassword();
		return proxyHost != null ? createXJCProxyArgument(proxyHost, proxyPort, proxyUsername, proxyPassword) : null;
	}

	protected String getActiveProxyAsHttpproxy()
	{
		if (getSettings() == null)
			return null;

		final Settings settings = getSettings();

		final Proxy activeProxy = settings.getActiveProxy();
		if (activeProxy == null || activeProxy.getHost() == null)
			return null;

		return createXJCProxyArgument(activeProxy.getHost(), activeProxy.getPort(), activeProxy.getUsername(),
			activeProxy.getPassword());
	}

	private String createXJCProxyArgument(String host, int port, String username, String password)
	{
		if (host == null)
		{
			if (port != -1)
			{
				getLog().warn(MessageFormat.format(
					"Proxy port is configured to [{0,number,#}] but proxy host is missing. "
					+ "Proxy port will be ignored.", port));
			}
			
			if (username != null)
			{
				getLog().warn(MessageFormat.format("Proxy username is configured to [{0}] but proxy host is missing. "
					+ "Proxy username will be ignored.", username));
			}
			
			if (password != null)
			{
				getLog().warn(MessageFormat.format(
					"Proxy password is set but proxy host is missing. " + "Proxy password will be ignored.",
					password));
			}
			return null;
		}
		else
		{
			// The XJC proxy argument should be on the form
			// [user[:password]@]proxyHost[:proxyPort]
			final StringBuilder proxyStringBuilder = new StringBuilder();
			if (username != null)
			{
				// Start with the username.
				proxyStringBuilder.append(username);
				// Append the password if provided.
				if (password != null)
					proxyStringBuilder.append(":").append(password);
				proxyStringBuilder.append("@");
			}
			else
			{
				if (password != null)
				{
					getLog().warn(MessageFormat.format(
						"Proxy password is set but proxy username is missing. " + "Proxy password will be ignored.",
						password));
				}
			}

			// Append hostname and port.
			proxyStringBuilder.append(host);

			if (port != -1)
				proxyStringBuilder.append(":").append(port);
			
			return proxyStringBuilder.toString();
		}
	}

	/**
	 * Returns array of command line arguments for XJC. These arguments are based on
	 * the configured arguments (see {@link #getArgs()}) but also include episode
	 * arguments.
	 * 
	 * @return String array of XJC command line options.
	 */
	protected List<String> getArguments()
	{
		final List<String> arguments = new ArrayList<String>(getArgs());

		final String httpproxy = getHttpproxy();
		if (httpproxy != null)
		{
			arguments.add("-httpproxy");
			arguments.add(httpproxy);
		}

		if (getEpisode() && getEpisodeFile() != null)
		{
			arguments.add("-episode");
			arguments.add(getEpisodeFile().getAbsolutePath());
		}

		if (getMarkGenerated())
			arguments.add("-mark-generated");

		for (final File episodeFile : getEpisodeFiles())
		{
			if (episodeFile.isFile())
				arguments.add(episodeFile.getAbsolutePath());
		}
		
		return arguments;
	}

	protected String getHttpproxy()
	{
		final String httpproxy;
		final String activeHttpproxy = getActiveProxyAsHttpproxy();
		final String customHttpproxy = getCustomHttpproxy();
		
		if (isUseActiveProxyAsHttpproxy())
		{
			if (customHttpproxy != null)
			{
				getLog().warn(MessageFormat.format(
					"Both [useActiveProxyAsHttpproxy=true] as well as custom proxy [{0}] are configured. " +
					"Please remove either [useActiveProxyAsHttpproxy=true] or custom proxy configuration.",
					customHttpproxy));

				getLog().debug(MessageFormat.format("Using custom proxy [{0}].", customHttpproxy));

				httpproxy = customHttpproxy;
			}
			else if (activeHttpproxy != null)
			{
				getLog().debug(MessageFormat.format("Using active proxy [{0}] from Maven settings.", activeHttpproxy));
				httpproxy = activeHttpproxy;
			}
			else
			{
				getLog().warn(MessageFormat.format(
					"Configured [useActiveProxyAsHttpproxy=true] but no active proxy is configured in Maven settings. " +
					"Please configure an active proxy in Maven settings or remove [useActiveProxyAsHttpproxy=true].",
					customHttpproxy));
				httpproxy = activeHttpproxy;
			}
		}
		else
		{
			if (customHttpproxy != null)
			{
				getLog().debug(MessageFormat.format("Using custom proxy [{0}].", customHttpproxy));
				httpproxy = customHttpproxy;
			}
			else
				httpproxy = null;
		}
		return httpproxy;
	}

	public OptionsConfiguration createOptionsConfiguration() throws MojoExecutionException
	{
		final OptionsConfiguration optionsConfiguration = new OptionsConfiguration
		(
			getEncoding(),
			getSchemaLanguage(),
			getGrammars(),
			getBindFiles(),
			getEntityResolver(),
			getGeneratePackage(),
			getGenerateDirectory(),
			getReadOnly(),
			getPackageLevelAnnotations(),
			getNoFileHeader(),
			getEnableIntrospection(),
			getDisableXmlSecurity(),
			getAccessExternalSchema(),
			getAccessExternalDTD(),
			isEnableExternalEntityProcessing(), 
			getContentForWildcard(),
			getExtension(),
			getStrict(),
			getVerbose(),
			getDebug(),
			getArguments(),
			getXjcPluginURLs(),
			getSpecVersion()
		);
		return optionsConfiguration;
	}

	private List<URI> resolveURIs(final List<URI> uris)
	{
		final List<URI> resolvedURIs = new ArrayList<URI>(uris.size());
		for (URI uri : uris)
		{
			final String URI = getCatalogResolverInstance().getResolvedEntity(null, uri.toString());
			if (URI != null)
			{
				try
				{
					uri = new URI(URI);
				}
				catch (URISyntaxException ignored)
				{
					// Ignored
				}
			}
			resolvedURIs.add(uri);
		}
		return resolvedURIs;
	}

	private void parseResolvedCatalogURIs() throws MojoExecutionException
	{
		for (URI catalogURI : getResolvedCatalogURIs())
		{
			if (catalogURI != null)
			{
				try
				{
					getCatalogResolverInstance().getCatalog().parseCatalog(catalogURI.toURL());
				}
				catch (IOException ioex)
				{
					throw new MojoExecutionException(
						MessageFormat.format("Error parsing catalog [{0}].", catalogURI.toString()), ioex);
				}
			}
		}
	}

	private List<InputSource> createInputSources(final List<URI> uris) throws IOException, SAXException
	{
		final List<InputSource> inputSources = new ArrayList<InputSource>(uris.size());
		for (final URI uri : uris)
		{
			InputSource inputSource = IOUtils.getInputSource(uri);
			final InputSource resolvedInputSource =
				getEntityResolver().resolveEntity(inputSource.getPublicId(), inputSource.getSystemId());

			if (resolvedInputSource != null)
				inputSource = resolvedInputSource;
			
			inputSources.add(inputSource);
		}
		return inputSources;
	}
}
