package org.jvnet.higherjaxb.mojo;

import static java.lang.String.format;
import static org.jvnet.higherjaxb.mojo.util.ArtifactUtils.mergeDependencyWithDefaults;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogResolver;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jvnet.higherjaxb.mojo.protocol.ConfigurableStreamHandlerFactory;
import org.jvnet.higherjaxb.mojo.resolver.tools.ReResolvingEntityResolverWrapper;
import org.jvnet.higherjaxb.mojo.resolver.tools.ViaCatalogResolver;
import org.jvnet.higherjaxb.mojo.util.IOUtils;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;
import org.xml.sax.EntityResolver;

/**
 * This abstract higherjaxb 'parameter' mojo provides common parameterized
 * properties and methods for AbstractHigherjaxbBaseMojo.
 * 
 * @param <O> Stores invocation options for XJC.
 * 
 * @see <a href="https://cwiki.apache.org/confluence/display/MAVEN/Maven+Ecosystem+Cleanup">Cleanup</a>
 * @see <a href="https://cwiki.apache.org/confluence/display/MAVEN/Notes+For+Maven+3.9.x+Plugin+Developers">Notes</a>
 */
public abstract class AbstractHigherjaxbParmMojo<O> extends AbstractMojo
	implements DependencyResourceResolver
{
	public static final String HIGHERJAXB_MOJO_PREFIX = "org.jvnet.higherjaxb.mojo.xjc";
	
	public static final File DEFAULT_USER_LOCAL_REPO = org.apache.maven.repository.RepositorySystem.defaultUserLocalRepository;
	public static final String DEFAULT_REMOTE_REPO_ID = org.apache.maven.repository.RepositorySystem.DEFAULT_REMOTE_REPO_ID;
	public static final String DEFAULT_REMOTE_REPO_URL = org.apache.maven.repository.RepositorySystem.DEFAULT_REMOTE_REPO_URL;
	public static final String DEFAULT_REMOTE_REPO_TYPE = "default";
	
    // Catalog feature values for the prefer property
	public static final String CATALOG_FEATURE_PREFER_SYSTEM = "system";
	public static final String CATALOG_FEATURE_PREFER_PUBLIC = "public";

    // Catalog feature values for the defer property
	public static final String CATALOG_FEATURE_DEFER_TRUE = "true";
	public static final String CATALOG_FEATURE_DEFER_FALSE = "false";
	
    // Catalog feature values for the resolve property
    public static final String CATALOG_FEATURE_RESOLVE_STRICT = "strict";
    public static final String CATALOG_FEATURE_RESOLVE_CONTINUE = "continue";
    public static final String CATALOG_FEATURE_RESOLVE_IGNORE = "ignore";

	/**
	 * Creates and initializes an instance of XJC options.
	 * @return An OptionsFactory to create XJC options per specification.
	 */
	protected abstract CoreOptionsFactory<O> getOptionsFactory();
	
	private Date executeStartTime = null;
	protected Date getExecuteStartTime() { return executeStartTime; }
	protected void setExecuteStartTime(Date executeStartTime) { this.executeStartTime = executeStartTime; }
	
	private Date executeFinishTime = null;
	protected Date getExecuteFinishTime() { return executeFinishTime; }
	protected void setExecuteFinishTime(Date executeFinishTime) { this.executeFinishTime = executeFinishTime; }

	// Represents com.sun.tools.xjc.outline.Outline
	private Object outline;
	public Object getOutline() { return outline; }
	public void setOutline(Object outline) { this.outline = outline; }

	@Parameter( defaultValue = "${project}", readonly = true )
	private MavenProject project;
	public MavenProject getProject()
	{
		if ( project == null )
			setProject((MavenProject) getPluginContext().get("project"));
		return project;
	}
    public void setProject(MavenProject project) { this.project = project; }
	
	/**
     * The entry point to Maven Artifact Resolver, i.e. the component doing all the work.
     */
    @Component
    private RepositorySystem repoSystem;
    public RepositorySystem getRepoSystem() { return repoSystem; }
	public void setRepoSystem(RepositorySystem repoSystem) { this.repoSystem = repoSystem; }

	/**
     * The current repository/network configuration of Maven.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;
	public RepositorySystemSession getRepoSession() { return repoSession; }
	public void setRepoSession(RepositorySystemSession repoSession) { this.repoSession = repoSession; }
	
    /**
     * The project's remote repositories to use for the resolution.
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;
	public List<RemoteRepository> getRemoteRepos()
	{
		if ( remoteRepos == null )
			remoteRepos = new ArrayList<RemoteRepository>();
		return remoteRepos;
	}
	
	@Parameter(defaultValue = "${settings}", readonly = true)
	private Settings settings;
	public Settings getSettings() { return settings; }
	public void setSettings(Settings settings) { this.settings = settings; }

	/**
	 * If set to <code>true</code>, passes Maven's active proxy settings to XJC.
	 * Default value is <code>false</code>. Proxy settings are passed using the
	 * <code>-httpproxy</code> argument in the form
	 * <code>[user[:password]@]proxyHost[:proxyPort]</code>. This sets both HTTP
	 * as well as HTTPS proxy.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".useActiveProxyAsHttpproxy", defaultValue = "false")
	private boolean useActiveProxyAsHttpproxy = false;
	public boolean isUseActiveProxyAsHttpproxy() { return this.useActiveProxyAsHttpproxy; }
	public void setUseActiveProxyAsHttpproxy(boolean useActiveProxyAsHttpproxy) { this.useActiveProxyAsHttpproxy = useActiveProxyAsHttpproxy; }

	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".proxyHost")
	private String proxyHost;
	public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; }
	public String getProxyHost() { return this.proxyHost; }

	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".proxyPort")
	private int proxyPort;
	public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }
	public int getProxyPort() { return this.proxyPort; }

	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".proxyUsername")
	private String proxyUsername;
	public void setProxyUsername(String proxyUsername) { this.proxyUsername = proxyUsername; }
	public String getProxyUsername() { return this.proxyUsername; }

	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".proxyPassword")
	private String proxyPassword;
	public void setProxyPassword(String proxyPassword) { this.proxyPassword = proxyPassword; }
	public String getProxyPassword() { return this.proxyPassword; }

	/**
	 * Encoding for the generated sources, defaults to
	 * ${project.build.sourceEncoding}.
	 */
	@Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}")
	private String encoding;
	public String getEncoding() { return encoding; }
	public void setEncoding(String encoding) { this.encoding = encoding; }

	/**
	 * Locale for the generated sources.
	 */
	@Parameter(property = "locale")
	private String locale;
	public String getLocale() { return locale; }
	public void setLocale(String locale) { this.locale = locale; }

	/**
	 * Type of input schema language. One of: DTD, XMLSCHEMA, RELAXNG,
	 * RELAXNG_COMPACT, WSDL, AUTODETECT. If unspecified, it is assumed
	 * AUTODETECT. See com.sun.tools.xjc.Language.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".schemaLanguage")
	private String schemaLanguage;
	public String getSchemaLanguage() { return schemaLanguage; }
	public void setSchemaLanguage(String schemaLanguage) { this.schemaLanguage = schemaLanguage; }

	/**
	 * The source directory containing *.xsd schema files. Notice that binding
	 * files are searched by default in this directory.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".schemaDirectory", defaultValue = "src/main/resources", required = true)
	private File schemaDirectory;
	public File getSchemaDirectory() { return schemaDirectory; }
	public void setSchemaDirectory(File schemaDirectory) { this.schemaDirectory = schemaDirectory; }

	/**
	 * <p>
	 * A list of regular expression file search patterns to specify the schemas
	 * to be processed. Searching is based from the root of
	 * <code>schemaDirectory</code>.
	 * </p>
	 * <p>
	 * If left undefined, then all <code>*.xsd</code> files in
	 * <code>schemaDirectory</code> will be processed.
	 * </p>
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".schemaIncludes", defaultValue = "*.xsd")
	private String[] schemaIncludes = new String[] { "*.xsd" };
	public String[] getSchemaIncludes() { return schemaIncludes; }
	public void setSchemaIncludes(String[] schemaIncludes) { this.schemaIncludes = schemaIncludes; }

	/**
	 * <p>A list of regular expression file search patterns to specify the schemas
	 * to be excluded from the <code>schemaIncludes</code> list. Searching is
	 * based from the root of schemaDirectory.</p>
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".schemaExcludes")
	private String[] schemaExcludes;
	public String[] getSchemaExcludes() { return schemaExcludes; }
	public void setSchemaExcludes(String[] schemaExcludes) { this.schemaExcludes = schemaExcludes; }

	/**
	 * A list of schema resources which could includes file sets, URLs, Maven
	 * artifact resources.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".schemas")
	private ResourceEntry[] schemas = new ResourceEntry[0];
	public ResourceEntry[] getSchemas() { return schemas; }
	public void setSchemas(ResourceEntry[] schemas) { this.schemas = schemas; }

	/**
	 * <p>
	 * The source directory containing the *.xjb binding files.
	 * </p>
	 * <p>
	 * If left undefined, then the <code>schemaDirectory</code> is assumed.
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".bindingDirectory")
	private File bindingDirectory;
	public File getBindingDirectory() { return bindingDirectory != null ? bindingDirectory : getSchemaDirectory(); }
	public void setBindingDirectory(File bindingDirectory) { this.bindingDirectory = bindingDirectory; }

	/**
	 * The source directory containing <code>catalog.xml</code> catalog files. 
	 * Defaults to the <code>schemaDirectory</code>.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogDirectory")
	private File catalogDirectory;
	public File getCatalogDirectory() { return catalogDirectory != null ? catalogDirectory : getSchemaDirectory(); }
	public void setCatalogDirectory(File catalogDirectory) { this.catalogDirectory = catalogDirectory; }

	/**
	 * <p>
	 * A list of regular expression file search patterns to specify the binding
	 * files to be processed. Searching is based from the root of
	 * <code>bindingDirectory</code>.
	 * </p>
	 * <p>
	 * If left undefined, then all *.xjb files in schemaDirectory will be
	 * processed.
	 * </p>
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".bindingIncludes", defaultValue = "*.xjb")
	private String[] bindingIncludes = new String[] { "*.xjb" };
	public String[] getBindingIncludes() { return bindingIncludes; }
	public void setBindingIncludes(String[] bindingIncludes) { this.bindingIncludes = bindingIncludes; }

	/**
	 * A list of regular expression file search patterns to specify the binding
	 * files to be excluded from the <code>bindingIncludes</code>. Searching is
	 * based from the root of bindingDirectory.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".bindingExcludes")
	private String[] bindingExcludes;
	public String[] getBindingExcludes() { return bindingExcludes; }
	public void setBindingExcludes(String[] bindingExcludes) { this.bindingExcludes = bindingExcludes; }

	/**
	 * A list of binding resources which could includes file sets, URLs, Maven
	 * artifact resources.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".bindings")
	private ResourceEntry[] bindings = new ResourceEntry[0];
	public ResourceEntry[] getBindings() { return bindings; }
	public void setBindings(ResourceEntry[] bindings) { this.bindings = bindings; }

	/**
	 * If 'true', maven's default excludes are NOT added to all the excludes
	 * lists.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".disableDefaultExcludes", defaultValue = "false")
	private boolean disableDefaultExcludes;
	public boolean getDisableDefaultExcludes() { return disableDefaultExcludes; }
	public void setDisableDefaultExcludes(boolean disableDefaultExcludes) { this.disableDefaultExcludes = disableDefaultExcludes; }
	
	/**
	 * Indicates the preference between the public and system identifiers. The default value is public.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogFeaturePrefer", defaultValue = "public")
	private String catalogFeaturePrefer;
	public String getCatalogFeaturePrefer()
	{
		if ( catalogFeaturePrefer == null )
			setCatalogFeaturePrefer("public");
		return catalogFeaturePrefer;
	}
	public void setCatalogFeaturePrefer(String catalogFeaturePrefer)
	{
		this.catalogFeaturePrefer = catalogFeaturePrefer;
	}

	/**
	 * Indicates that the alternative catalogs including those specified in delegate entries or 
	 * nextCatalog are not read until they are needed. The default value is true.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogFeatureDefer", defaultValue = "true")
	private String catalogFeatureDefer = "true";
	public String getCatalogFeatureDefer()
	{
		if ( catalogFeatureDefer == null )
			setCatalogFeatureDefer("true");
		return catalogFeatureDefer;
	}
	public void setCatalogFeatureDefer(String catalogFeatureDefer)
	{
		this.catalogFeatureDefer = catalogFeatureDefer;
	}

	/**
	 * Determines the action if there is no matching entry found after all of the specified
	 * catalogs are exhausted. The default is the value of the XJC strict parameter:
	 * if the value of the XJC strict parameter is <code>true</code> then the default for
	 * this parameter is "strict"; otherwise, it is "continue" with original URI.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogFeatureResolve")
	private String catalogFeatureResolve;
	public String getCatalogFeatureResolve()
	{
		if ( catalogFeatureResolve == null )
		{
			setCatalogFeatureResolve(getStrict()
				? CATALOG_FEATURE_RESOLVE_STRICT
				: CATALOG_FEATURE_RESOLVE_CONTINUE);
		}
		return catalogFeatureResolve;
	}
	public void setCatalogFeatureResolve(String catalogFeatureResolve)
	{
		this.catalogFeatureResolve = catalogFeatureResolve;
	}

	/**
	 * <p>
	 * Specify the catalog file to resolve external entity references
	 * (xjc's "-catalog" option).
	 * </p>
	 * 
	 * <p>
	 * The system property {@code javax.xml.catalog.files}, as defined
	 * in {@link CatalogFeatures}, will be read to locate the initial
     * list of catalog files.
	 * </p>
     * 
	 * <p>
	 * CatalogReader from 'javax.xml.catalog' handles SAX events while
	 * parsing through a 'catalog.xml' file to create a catalog object.
	 * The legacy TR9401 file format is not supported.
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalog")
	private File catalog;
	public File getCatalog() { return catalog; }
	public void setCatalog(File catalog) { this.catalog = catalog; }

	/**
	 * <p>
	 * A list of regular expression file search patterns to specify
	 * the catalogs to be processed. Searching is based from the root
	 * of <code>catalogDirectory</code>.
	 * </p>
	 * <p>
	 * If left undefined, then all <code>catalog*.xml</code> files in
	 * <code>catalogDirectory</code> will be processed.
	 * </p>
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogIncludes", defaultValue = "catalog*.xml")
	private String[] catalogIncludes = new String[] { "catalog*.xml" };
	public String[] getCatalogIncludes() { return catalogIncludes; }
	public void setCatalogIncludes(String[] catalogIncludes) { this.catalogIncludes = catalogIncludes; }

	/**
	 * A list of regular expression file search patterns to specify the catalogs
	 * to be excluded from the <code>catalogIncludes</code> list. Searching is
	 * based from the root of <code>catalogDirectory</code>.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogExcludes")
	private String[] catalogExcludes;
	public String[] getCatalogExcludes() { return catalogExcludes; }
	public void setCatalogExcludes(String[] catalogExcludes) { this.catalogExcludes = catalogExcludes; }

	/**
	 * A list of catalog resources which could includes file sets, URLs, Maven
	 * artifact resources.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogs")
	private ResourceEntry[] catalogs = new ResourceEntry[0];
	public ResourceEntry[] getCatalogs() { return catalogs; }
	public void setCatalogs(ResourceEntry[] catalogs) { this.catalogs = catalogs; }

	/**
	 * Provides the full class name of a catalog (entity) resolver:
	 * 
	 * <ul>
	 * <li>org.jvnet.higherjaxb.mojo.resolver.tools.ViaCatalogResolver</li>
	 * <li>org.jvnet.higherjaxb.mojo.resolver.tools.MavenCatalogResolver</li>
	 * <li>org.jvnet.higherjaxb.mojo.resolver.tools.ClasspathCatalogResolver</li>
	 * </ul>
	 * 
	 * <p>
	 * The full class name of a catalog resolver must be an extension of
	 * {@link javax.xml.catalog.CatalogResolver}. Then the class for the given
	 * name is loaded by this plugin at build time and an instance is
	 * constructed with a reference to this {@link Mojo} and its {@link Log}.
	 * </p>
	 * 
	 * <p>
	 * The {@link CatalogResolver} instance is passed to the constructor of {@link URLStreamHandler}
	 * instance(s) which is used to construct a {@link ConfigurableStreamHandlerFactory} instance.
	 * The factory implements {@link URLStreamHandlerFactory} and is used to <em>statically</em>
	 * configure the JVM's {@link URL} class using its {@code setURLStreamHandlerFactory(factory)}
	 * method. That factory provides custom protocol handler(s) like {@code "maven:"} and 
	 * {@code "classpath:"} to any/all XML parsers in the JVM.
	 * </p>
	 * 
	 * <p>
	 * In addition, {@link CatalogResolver} extends {@link EntityResolver} and the
	 * resolver instance created from this parameter is used to configure XJC, too;
	 * but not directly. Instead, an instance of {@link ReResolvingEntityResolverWrapper}
	 * is constructed to wrap this resolve instance as an {@link EntityResolver}. The
	 * instance of {@link ReResolvingEntityResolverWrapper} is used to set the 
	 * <code>entityResolver</code> property on {@link OptionsConfiguration} and then
	 * the <code>entityResolver</code> field on {@code com.sun.tools.xjc.Options}.
	 * </p>
	 * 
	 * <p>
	 * defaultValue = {@code "org.jvnet.higherjaxb.mojo.resolver.tools.ViaCatalogResolver"}
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".catalogResolver",
		defaultValue="org.jvnet.higherjaxb.mojo.resolver.tools.ViaCatalogResolver")
	private String catalogResolver = ViaCatalogResolver.class.getName();
	public String getCatalogResolver() { return catalogResolver; }
	public void setCatalogResolver(String catalogResolver) { this.catalogResolver = catalogResolver; }

	/**
	 * <p>
	 * The generated classes will all be placed under this Java package (xjc's
	 * -p option), unless otherwise specified in the schemas.
	 * </p>
	 * <p>
	 * If left unspecified, the package will be derived from the schemas only.
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".generatePackage")
	private String generatePackage;
	public String getGeneratePackage() { return generatePackage; }
	public void setGeneratePackage(String generatePackage) { this.generatePackage = generatePackage; }

	/**
	 * <p>
	 * Generated code will be written under this directory.
	 * </p>
	 * <p>
	 * For instance, if you specify <code>generateDirectory="doe/ray"</code> and
	 * <code>generatePackage="org.here"</code>, then files are generated to
	 * <code>doe/ray/org/here</code>.
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".generateDirectory", defaultValue = "${project.build.directory}/generated-sources/xjc", required = true)
	private File generateDirectory;
	public File getGenerateDirectory() { return generateDirectory; }
	public void setGenerateDirectory(File generateDirectory)
	{
		this.generateDirectory = generateDirectory;
		if (getEpisodeFile() == null)
		{
			final File episodeFile = new File(getGenerateDirectory(), "META-INF" + File.separator + "sun-jaxb.episode");
			setEpisodeFile(episodeFile);
		}
	}

	/**
	 * If set to true (default), adds target directory as a compile source root
	 * of this Maven project.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".addCompileSourceRoot", defaultValue = "true", required = false)
	private boolean addCompileSourceRoot = true;
	public boolean getAddCompileSourceRoot() { return addCompileSourceRoot; }
	public void setAddCompileSourceRoot(boolean addCompileSourceRoot) { this.addCompileSourceRoot = addCompileSourceRoot; }

	/**
	 * If set to true, adds target directory as a test compile source root of
	 * this Maven project. Default value is false.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".addTestCompileSourceRoot", defaultValue = "false", required = false)
	private boolean addTestCompileSourceRoot = false;
	public boolean getAddTestCompileSourceRoot() { return addTestCompileSourceRoot; }
	public void setAddTestCompileSourceRoot(boolean addTestCompileSourceRoot) { this.addTestCompileSourceRoot = addTestCompileSourceRoot; }

	/**
	 * If 'true', the generated Java source files are set as read-only (xjc's
	 * -readOnly option).
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".readOnly", defaultValue = "false")
	private boolean readOnly;
	public boolean getReadOnly() { return readOnly; }
	public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }

	/**
	 * If 'false', suppresses generation of package level annotations
	 * (package-info.java), xjc's -npa option.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".packageLevelAnnotations", defaultValue = "true")
	private boolean packageLevelAnnotations = true;
	public boolean getPackageLevelAnnotations() { return packageLevelAnnotations; }
	public void setPackageLevelAnnotations(boolean packageLevelAnnotations) { this.packageLevelAnnotations = packageLevelAnnotations; }

	/**
	 * If 'true', suppresses generation of a file header with timestamp, xjc's
	 * -no-header option.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".noFileHeader", defaultValue = "false")
	private boolean noFileHeader = false;
	public boolean getNoFileHeader() { return noFileHeader; }
	public void setNoFileHeader(boolean noFileHeader) { this.noFileHeader = noFileHeader; }

	/**
	 * If 'true', enables correct generation of Boolean getters/setters to
	 * enable Bean Introspection APIs; xjc's -enableIntrospection option.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".enableIntrospection", defaultValue = "false")
	private boolean enableIntrospection = false;
	public boolean getEnableIntrospection() { return enableIntrospection; }
	public void setEnableIntrospection(boolean enableIntrospection) { this.enableIntrospection = enableIntrospection; }

	/**
	 * If 'true', disables XML security features when parsing XML documents;
	 * xjc's -disableXmlSecurity option.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".disableXmlSecurity", defaultValue = "true")
	private boolean disableXmlSecurity = true;
	public boolean getDisableXmlSecurity() { return disableXmlSecurity; }
	public void setDisableXmlSecurity(boolean disableXmlSecurity) { this.disableXmlSecurity = disableXmlSecurity; }

	/**
	 * Restrict access to the protocols specified for external reference set by
	 * the schemaLocation attribute, Import and Include element. Value: a list
	 * of protocols separated by comma. A protocol is the scheme portion of a
	 * {@link java.net.URI}, or in the case of the JAR protocol, "jar" plus the
	 * scheme portion separated by colon. The keyword "all" grants permission to
	 * all protocols. Other protocols are "file", "http", "all".
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".accessExternalSchema", defaultValue = "all")
	private String accessExternalSchema = "all";
	public String getAccessExternalSchema() { return accessExternalSchema; }
	public void setAccessExternalSchema(String accessExternalSchema) { this.accessExternalSchema = accessExternalSchema; }

	/**
	 * Restricts access to external DTDs and external Entity References to the
	 * protocols specified. Value: a list of protocols separated by comma. A
	 * protocol is the scheme portion of a {@link java.net.URI}, or in the case
	 * of the JAR protocol, "jar" plus the scheme portion separated by colon.
	 * The keyword "all" grants permission to all protocols. Other protocols are
	 * "file", "http", "all".
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".accessExternalDTD", defaultValue = "all")
	private String accessExternalDTD = "all";
	public String getAccessExternalDTD() { return accessExternalDTD; }
	public void setAccessExternalDTD(String accessExternalDTD) { this.accessExternalDTD = accessExternalDTD; }
	
	/**
	 * Enables external entity processing. Allows an OSGI generated <code>com.sun.xml.dtdparser.InputEntity</code> to
	 * use <code>XmlReader</code> to create a <code>Reader</code> for a <code>URL</code> referencing a 
	 * <code>systemId</code>. The dependency is <code>com.sun.xml.dtd-parser:dtd-parser.jar</code>. When
	 * disabled, an attempt to read from an external URL will throw a <code>SAXParseException</code> with
	 * code <code>"P-082"</code>.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".enableExternalEntityProcessing", defaultValue = "true")
	private boolean enableExternalEntityProcessing;
	public boolean isEnableExternalEntityProcessing() { return enableExternalEntityProcessing; }
	public void setEnableExternalEntityProcessing(boolean enableExternalEntityProcessing) { this.enableExternalEntityProcessing = enableExternalEntityProcessing; }

	/**
	 * If <code>true</code>, generates content property for types with multiple <code>xs:any</code>
	 * derived elements; corresponds to the XJC <code>-contentForWildcard</code> option.
	 */
	@Parameter(defaultValue="false")
	private boolean contentForWildcard;
	public boolean getContentForWildcard() { return contentForWildcard; }
	public void setContentForWildcard(boolean contentForWildcard) { this.contentForWildcard = contentForWildcard; }

	/**
	 * If 'true', the XJC binding compiler will run in the extension mode (xjc's
	 * -extension option). Otherwise, it will run in the strict conformance
	 * mode.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".extension", defaultValue = "true")
	private boolean extension;
	public boolean getExtension() { return extension; }
	public void setExtension(boolean extension) { this.extension = extension; }

	/**
	 * If 'true' (default), Perform strict validation of the input schema
	 * (disabled by the xjc's -nv option).
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".strict", defaultValue = "true")
	private boolean strict = true;
	public boolean getStrict() { return strict; }
	public void setStrict(boolean strict) { this.strict = strict; }

	/**
	 * If 'false', the plugin will not write the generated code to disk.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".writeCode", defaultValue = "true")
	private boolean writeCode = true;
	public boolean getWriteCode() { return writeCode; }
	public void setWriteCode(boolean writeCode) { this.writeCode = writeCode; }

	/**
	 * <p>
	 * If 'true', the plugin and the XJC compiler are both set to verbose mode
	 * (xjc's -verbose option).
	 * </p>
	 * <p>
	 * It is automatically set to 'true' when maven is run in debug mode (mvn's
	 * -X option).
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".verbose", defaultValue = "false")
	private boolean verbose;
	public boolean getVerbose() { return verbose; }
	public void setVerbose(boolean verbose) { this.verbose = verbose; }

	/**
	 * <p>
	 * If 'true', the XJC compiler is set to debug mode (xjc's -debug option).
	 * </p>
	 * <p>
	 * It is automatically set to 'true' when maven is run in debug mode (mvn's
	 * -X option).
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".debug", defaultValue = "false")
	private boolean debug;
	public boolean getDebug() { return debug; }
	public void setDebug(boolean debug) { this.debug = debug; }

	/**
	 * <p>
	 * A list of extra XJC's command-line arguments (items must include the dash
	 * '-'). Use this argument to enable the JAXB2 plugins you want to use.
	 * </p>
	 * <p>
	 * Arguments set here take precedence over other mojo parameters.
	 * </p>
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".args")
	private List<String> args = new LinkedList<String>();
	public List<String> getArgs() { return args; }
	public void setArgs(List<String> args) { this.args.addAll(args); }

	/**
	 * If true, no up-to-date check is performed and the XJC always re-generates
	 * the sources. Otherwise schemas will only be recompiled if anything has
	 * changed.
	 *
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".forceRegenerate", defaultValue = "false")
	private boolean forceRegenerate;
	public boolean getForceRegenerate() { return forceRegenerate; }
	public void setForceRegenerate(boolean forceRegenerate) { this.forceRegenerate = forceRegenerate; }

	/**
	 * <p>
	 * If 'true', the generateDirectory will be deleted before the XJC binding
	 * compiler recompiles the source files. Default is false.
	 * </p>
	 * <p>
	 * Note that if set to 'false', the up-to-date check might not work, since
	 * XJC does not regenerate all files (i.e. files for "any" elements under
	 * 'xjc/org/w3/_2001/xmlschema' directory).
	 * </p>
	 *
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".removeOldOutput", defaultValue = "false")
	private boolean removeOldOutput;
	public boolean getRemoveOldOutput() { return removeOldOutput; }
	public void setRemoveOldOutput(boolean removeOldOutput) { this.removeOldOutput = removeOldOutput; }

	/**
	 * <p>
	 * If 'true', package directories will be cleaned before the XJC binding
	 * compiler generates the source files.
	 * </p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".cleanPackageDirectories", defaultValue = "true")
	private boolean cleanPackageDirectories = true;
	public boolean getCleanPackageDirectories() { return cleanPackageDirectories; }
	public void setCleanPackageDirectories(boolean cleanPackageDirectories) { this.cleanPackageDirectories = cleanPackageDirectories; }

	/**
	 * Specifies patterns of files produced by this plugin. This is used to
	 * check if produced files are up-to-date. Default value is ** /*.*, **
	 * /*.java, ** /bgm.ser, ** /jaxb.properties.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".produces")
	private String[] produces = new String[] { "**/*.*", "**/*.java", "**/bgm.ser", "**/jaxb.properties" };
	public String[] getProduces() { return produces; }
	public void setProduces(String[] produces) { this.produces = produces; }

	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".otherDependsIncludes")
	private String[] otherDependsIncludes;
	public String[] getOtherDependsIncludes() { return otherDependsIncludes; }
	public void setOtherDependsIncludes(String[] otherDependsIncludes) { this.otherDependsIncludes = otherDependsIncludes; }

	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".otherDependsExcludes")
	private String[] otherDependsExcludes;
	public String[] getOtherDependsExcludes() { return otherDependsExcludes; }
	public void setOtherDependsExcludes(String[] otherDependsExcludes) { this.otherDependsExcludes = otherDependsExcludes; }

	/**
	 * Target location of the episode file. By default it is
	 * target/generated-sources/xjc/META-INF/sun-jaxb.episode so that the
	 * episode file will appear as META-INF/sun-jaxb.episode in the JAR -
	 * just as XJC wants it.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".episodeFile")
	private File episodeFile;
	public File getEpisodeFile() { return episodeFile; }
	public void setEpisodeFile(File episodeFile) { this.episodeFile = episodeFile; }

	/**
	 * If true, the episode file (describing mapping of elements and types to
	 * classes for the compiled schema) will be generated.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".episode", defaultValue = "true")
	private boolean episode = true;
	public boolean getEpisode() { return episode; }
	public void setEpisode(boolean episode) { this.episode = episode; }

	/**
	 * If <code>true</code> (default), adds <code>if-exists="true"</code>
	 * attributes to the <code>bindings</code> elements associated with schemas
	 * (via <code>scd="x-schema::..."</code>) in the generated episode files.
	 * This is necessary to avoid the annoying `SCD "x-schema::tns" didn't
	 * match any schema component` errors.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".addIfExistsToEpisodeSchemaBindings", defaultValue = "true")
	private boolean addIfExistsToEpisodeSchemaBindings = true;
	public boolean isAddIfExistsToEpisodeSchemaBindings() { return this.addIfExistsToEpisodeSchemaBindings; }
	public void setAddIfExistsToEpisodeSchemaBindings(boolean addIfExistsToEpisodeSchemaBindings) { this.addIfExistsToEpisodeSchemaBindings = addIfExistsToEpisodeSchemaBindings; }

	/**
	 * If true, marks generated classes using a @Generated annotation - i.e.
	 * turns on XJC -mark-generated option. Default is false.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".markGenerated", defaultValue = "false")
	private boolean markGenerated = false;
	public boolean getMarkGenerated() { return markGenerated; }
	public void setMarkGenerated(boolean markGenerated) { this.markGenerated = markGenerated; }

	/**
	 * XJC plugins to be made available to XJC. They still need to be activated
	 * by using &lt;args/&gt; and enable plugin activation option.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".plugins")
	private Dependency[] plugins;
	public Dependency[] getPlugins() { return plugins; }
	public void setPlugins(Dependency[] plugins) { this.plugins = plugins; }

	/**
	 * Plugin artifacts.
	 *
	 * Plugins can inspect their effective runtime class path via the expressions
	 * ${plugin.artifacts} or ${plugin.artifactMap} to have a list or map, respectively,
	 * of resolved artifacts injected from the PluginDescriptor.
	 */
	@Parameter(defaultValue = "${plugin.artifacts}", required = true)
	private List<org.apache.maven.artifact.Artifact> pluginArtifacts;
	public List<org.apache.maven.artifact.Artifact> getPluginArtifacts() { return pluginArtifacts; }
	public void setPluginArtifacts(List<org.apache.maven.artifact.Artifact> plugingArtifacts) { this.pluginArtifacts = plugingArtifacts; }

	/**
	 * If you want to use existing artifacts as episodes for separate
	 * compilation, configure them as episodes/episode elements. It is assumed
	 * that episode artifacts contain an appropriate META-INF/sun-jaxb.episode resource.
	 * 
	 * An "META-INF/sun-jaxb.episode" file is generated by the XJC (XML Schema to Java) compiler.
	 * It is a schema bindings that associates schema types with existing classes.
	 * It is useful when you have one XML schema that is imported by other schemas, as it prevents
	 * the model from being regenerated. XJC will scan JARs for '*.episode files', then use them
	 * as binding files automatically.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".episodes")
	private Dependency[] episodes;
	public Dependency[] getEpisodes() { return episodes; }
	public void setEpisodes(Dependency[] episodes) { this.episodes = episodes; }

	/**
	 * Use all of the compile-scope project dependencies as episode artifacts.
	 * It is assumed that episode artifacts contain an appropriate
	 * META-INF/sun-jaxb.episode resource. Default is false.
	 * 
	 * <p>For -Dname, use a comma separated list of values.</p>
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".useDependenciesAsEpisodes")
	private boolean useDependenciesAsEpisodes = false;
	public boolean getUseDependenciesAsEpisodes() { return useDependenciesAsEpisodes; }
	public void setUseDependenciesAsEpisodes(boolean useDependenciesAsEpisodes) { this.useDependenciesAsEpisodes = useDependenciesAsEpisodes; }

	/**
	 * Scan all compile-scoped project dependencies for XML binding files.
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".scanDependenciesForBindings", defaultValue = "false")
	private boolean scanDependenciesForBindings = false;
	public boolean getScanDependenciesForBindings() { return scanDependenciesForBindings; }
	public void setScanDependenciesForBindings( boolean scanDependenciesForBindings) { this.scanDependenciesForBindings = scanDependenciesForBindings; }

	/**
	 * Version of the JAXB specification (ex. 2.0, 2.1, 2.2, 2.3 or 3.0).
	 */
	@Parameter(property = HIGHERJAXB_MOJO_PREFIX + ".specVersion", defaultValue = "3.0")
	private String specVersion = "3.0";
	public String getSpecVersion() { return specVersion; }
	public void setSpecVersion(String specVersion) { this.specVersion = specVersion; }

	protected void logConfiguration() throws MojoExecutionException
	{
		logApiConfiguration();

		getLog().info("XJC pluginArtifacts = " + getPluginArtifacts());
		getLog().info("XJC specVersion = " + getSpecVersion());
		getLog().info("XJC encoding = " + getEncoding());
		getLog().info("XJC locale = " + getLocale());
		getLog().info("XJC schemaLanguage = " + getSchemaLanguage());
		getLog().info("XJC schemaDirectory = " + getSchemaDirectory());
		getLog().info("XJC schemaIncludes = " + Arrays.toString(getSchemaIncludes()));
		getLog().info("XJC schemaExcludes = " + Arrays.toString(getSchemaExcludes()));
		getLog().info("XJC schemas = " + Arrays.toString(getSchemas()));
		getLog().info("XJC bindingDirectory = " + getBindingDirectory());
		getLog().info("XJC bindingIncludes = " + Arrays.toString(getBindingIncludes()));
		getLog().info("XJC bindingExcludes = " + Arrays.toString(getBindingExcludes()));
		getLog().info("XJC bindings = " + Arrays.toString(getBindings()));
		getLog().info("XJC disableDefaultExcludes = " + getDisableDefaultExcludes());
		getLog().info("XJC catalog = " + getCatalog());
		getLog().info("XJC catalogResolver = " + getCatalogResolver());
		getLog().info("XJC generatePackage = " + getGeneratePackage());
		getLog().info("XJC generateDirectory = " + getGenerateDirectory());
		getLog().info("XJC readOnly = " + getReadOnly());
		getLog().info("XJC extension = " + getExtension());
		getLog().info("XJC strict = " + getStrict());
		getLog().info("XJC writeCode = " + getWriteCode());
		getLog().info("XJC verbose = " + getVerbose());
		getLog().info("XJC debug = " + getDebug());
		getLog().info("XJC args = " + getArgs());
		getLog().info("XJC forceRegenerate = " + getForceRegenerate());
		getLog().info("XJC removeOldOutput = " + getRemoveOldOutput());
		getLog().info("XJC produces = " + Arrays.toString(getProduces()));
		getLog().info("XJC otherDependIncludes = " + getOtherDependsIncludes());
		getLog().info("XJC otherDependExcludes = " + getOtherDependsExcludes());
		getLog().info("XJC episodeFile = " + getEpisodeFile());
		getLog().info("XJC episode = " + getEpisode());
		getLog().info("XJC episodes = " + Arrays.toString(getEpisodes()));
		getLog().info("XJC plugins = " + Arrays.toString(getPlugins()));
		getLog().info("XJC useDependenciesAsEpisodes = " + getUseDependenciesAsEpisodes());
		getLog().info("XJC scanDependenciesForBindings = " + getScanDependenciesForBindings());
	}

	private static final String XML_SCHEMA_CLASS_NAME = "XmlSchema";
	private static final String XML_SCHEMA_CLASS_QNAME_JAKARTA = "jakarta.xml.bind.annotation." + XML_SCHEMA_CLASS_NAME;
	private static final String XML_SCHEMA_CLASS_QNAME_JAVAX = "javax.xml.bind.annotation." + XML_SCHEMA_CLASS_NAME;
	private static final String XML_SCHEMA_RESOURCE_NAME = XML_SCHEMA_CLASS_NAME + ".class";
	private static final String XML_SCHEMA_RESOURCE_QNAME_JAKARTA = "/jakarta/xml/bind/annotation/" + XML_SCHEMA_RESOURCE_NAME;
	private static final String XML_SCHEMA_RESOURCE_QNAME_JAVAX = "/javax/xml/bind/annotation/" + XML_SCHEMA_RESOURCE_NAME;
	private static final String XML_ELEMENT_REF_CLASS_NAME = "XmlElementRef";
	private static final String XML_ELEMENT_REF_CLASS_QNAME_JAVAX = "javax.xml.bind.annotation." + XML_ELEMENT_REF_CLASS_NAME;
	private static final String JAXB_CONTEXT_FACTORY_CLASS_NAME = "JAXBContextFactory";
	private static final String JAXB_CONTEXT_FACTORY_CLASS_QNAME_JAVAX = "javax.xml.bind." + JAXB_CONTEXT_FACTORY_CLASS_NAME;

	/**
	 * Location of the local repository.
	 */
//	@Parameter(defaultValue = "${localRepository}", required = true)
//	private LocalRepository localRepository;
//	public LocalRepository getLocalRepository() { return localRepository; }
//	public void setLocalRepository(LocalRepository localRepository) { this.localRepository = localRepository; }
	
	@Component
	private BuildContext buildContext = new DefaultBuildContext();
	public BuildContext getBuildContext() { return buildContext; }
	public void setBuildContext(BuildContext buildContext) { this.buildContext = buildContext; }

	protected void logApiConfiguration()
	{
		try
		{
			final Class<?> xmlSchemaClass = Class.forName(XML_SCHEMA_CLASS_QNAME_JAKARTA);
			final URL resource = xmlSchemaClass.getResource(XML_SCHEMA_RESOURCE_NAME);
			final String draftLocation = resource.toExternalForm();
			
			final String location;
			if (draftLocation.endsWith(XML_SCHEMA_RESOURCE_QNAME_JAKARTA))
				location = draftLocation.substring(0, draftLocation.length() - XML_SCHEMA_RESOURCE_QNAME_JAKARTA.length());
			else
				location = draftLocation;
			
			getLog().info("JAXB API is loaded from the [" + location + "].");
			getLog().info("Detected JAXB API version 3+ (jakarta).");
		}
		catch (ClassNotFoundException cnfex)
		{
			getLog().warn("Could not find JAXB 3+ API classes. Checking for JAXB 2.x API ...");
			logApiConfigurationLegacy();
		}
	}
	
	protected void logApiConfigurationLegacy()
	{
		try
		{
			final Class<?> xmlSchemaClass = Class.forName(XML_SCHEMA_CLASS_QNAME_JAVAX);
			final URL resource = xmlSchemaClass.getResource(XML_SCHEMA_RESOURCE_NAME);
			final String draftLocation = resource.toExternalForm();
			
			final String location;
			if (draftLocation.endsWith(XML_SCHEMA_RESOURCE_QNAME_JAVAX))
				location = draftLocation.substring(0, draftLocation.length() - XML_SCHEMA_RESOURCE_QNAME_JAVAX.length());
			else
				location = draftLocation;
			
			getLog().info("JAXB API is loaded from the [" + location + "].");

			try
			{
				// Since JAXB 2.3, factory that creates new JAXBContext instances.
				Class.forName(JAXB_CONTEXT_FACTORY_CLASS_QNAME_JAVAX);
				getLog().info("Detected JAXB API version [2.3] or greater.");
			}
			catch (ClassNotFoundException cnfex)
			{
				try
				{
					// Since JAXB 2.1, The location method returns a value could be:
					//   1) Any absolute URI, like {@code http://example.org/some.xsd};
					//   2) The empty string, to indicate the location is the responsibility of the reader of the generate schema to locate it;
					//   3) The {@code "##generate"} default indicates that the schema generator generates components for this namespace.
					// For JAXB 2.0 and below "NoSuchMethodException" is thrown.
					xmlSchemaClass.getMethod("location");

					try
					{
						// Since JAXB 2.0, maps a JavaBean property to a XML element derived from property's type.
						final Class<?> xmlElementRefClass = Class.forName(XML_ELEMENT_REF_CLASS_QNAME_JAVAX);
						xmlElementRefClass.getMethod("required");
						getLog().info("Detected JAXB API version [2.2].");
					}
					catch (NoSuchMethodException nsmex2)
					{
						getLog().info("Detected JAXB API version [2.1].");
					}
				}
				catch (NoSuchMethodException nsmex1)
				{
					getLog().info("Detected JAXB API version [2.0].");
				}
			}
		}
		catch (ClassNotFoundException cnfex)
		{
			getLog().error("Could not find JAXB 2.x/3+ API classes. Make sure JAXB 2.x or 3+ API is on the classpath.");
		}
	}

	protected void cleanPackageDirectory(final File packageDirectory)
	{
		// Accept files to delete.
		final File[] files = packageDirectory.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				boolean accept = false;
				if ( file.isFile() )
				{
					if ( file.lastModified() < getExecuteStartTime().getTime() )
						accept = true;
				}
				return accept;
			}
		});

		// Delete files.
		if (files != null)
		{
			for (File file : files)
			{
				getLog().debug(format("CLEAN: Deleting file [%s]", file));
				file.delete();
			}
		}
	}
	
	/**
	 * Create the list of catalog locations configured by any of these parameters:
	 * 
	 * <ul>
	 * <li><code>HIGHERJAXB_MOJO_PREFIX + ".catalog"</code></li>
	 * <li><code>HIGHERJAXB_MOJO_PREFIX + ".catalogs"</code></li>
	 * <li><code>HIGHERJAXB_MOJO_PREFIX + ".catalogDirectory"</code></li>
	 * <li><code>HIGHERJAXB_MOJO_PREFIX + ".catalogIncludes"</code></li>
	 * <li><code>HIGHERJAXB_MOJO_PREFIX + ".catalogExcludes"</code></li>
	 * </ul>
	 * 
	 * @return The list of primary and alternative catalog locations.
	 * 
	 * @throws MojoExecutionException When the list cannot be created.
	 */
	protected List<URI> createCatalogURIs() throws MojoExecutionException
	{
		final List<URI> catalogURIs = new ArrayList<URI>();
		
		final File catalog = getCatalog();
		if (catalog != null)
			catalogURIs.add(getCatalog().toURI());
		
		final ResourceEntry[] catalogs = getCatalogs();
		for (ResourceEntry resourceEntry : catalogs)
		{
			catalogURIs.addAll
			(
				createResourceEntryURIs
				(
					resourceEntry,
					getCatalogDirectory().getAbsolutePath(),
					getCatalogIncludes(),
					getCatalogExcludes()
				)
			);
		}
		return catalogURIs;
	}
	
	protected List<URI> createResourceEntryURIs(ResourceEntry resourceEntry,
		String defaultDirectory, String[] defaultIncludes,
		String[] defaultExcludes) throws MojoExecutionException
	{
		if (resourceEntry == null)
			return Collections.emptyList();
		else
		{
			final List<URI> uris = new LinkedList<URI>();
			
			if (resourceEntry.getFileset() != null)
			{
				final FileSet fileset = resourceEntry.getFileset();
				uris.addAll(createFileSetURIs(fileset, defaultDirectory, defaultIncludes, defaultExcludes));
			}
			
			if (resourceEntry.getUrl() != null)
			{
				String urlDraft = resourceEntry.getUrl();
				uris.add(createURI(urlDraft));
			}
			
			if (resourceEntry.getDependencyResource() != null)
			{
				final String systemId = resourceEntry.getDependencyResource().getSystemId();
				try
				{
					URI uri = new URI(systemId);
					uris.add(uri);
				}
				catch (URISyntaxException ex)
				{
					throw new MojoExecutionException(
						format("Could not create the resource entry URI from the following system id: [%s].", systemId), ex);
				}
			}
			return uris;
		}
	}
	
	private URI createURI(String uriString) throws MojoExecutionException
	{
		try
		{
			final URI uri = new URI(uriString);
			return uri;
		}
		catch (URISyntaxException urisex)
		{
			throw new MojoExecutionException(format(
				"Could not create the URI from string [%s].",
				uriString), urisex);
		}
	}

	private List<URI> createFileSetURIs(final FileSet fileset, String defaultDirectory, String[] defaultIncludes, String defaultExcludes[])
		throws MojoExecutionException
	{
		final String draftDirectory = fileset.getDirectory();
		final String directory = draftDirectory == null ? defaultDirectory : draftDirectory;
		
		final List<String> includes;
		final List<String> draftIncludes = fileset.getIncludes();
		if (draftIncludes == null || draftIncludes.isEmpty())
			includes = defaultIncludes == null ? Collections.<String> emptyList() : Arrays.asList(defaultIncludes);
		else
			includes = draftIncludes;

		final List<String> excludes;
		final List<String> draftExcludes = fileset.getExcludes();
		if (draftExcludes == null || draftExcludes.isEmpty())
			excludes = defaultExcludes == null ? Collections.<String> emptyList() : Arrays.asList(defaultExcludes);
		else
			excludes = draftExcludes;

		String[] includesArray = includes.toArray(new String[includes.size()]);
		String[] excludesArray = excludes.toArray(new String[excludes.size()]);

		try
		{
			final List<File> files = IOUtils.scanDirectoryForFiles(
					getBuildContext(), new File(directory), includesArray,
					excludesArray, !getDisableDefaultExcludes());

			final List<URI> uris = new ArrayList<URI>(files.size());

			for (final File file : files)
			{
				final URI uri = file.toURI();
				uris.add(uri);
			}
			return uris;
		}
		catch (IOException ioex)
		{
			throw new MojoExecutionException(format(
				"Could not scan directory [%s] for files with inclusion [%s] and exclusion [%s].",
				directory, includes, excludes));
		}
	}
	
	/** {@link org.jvnet.higherjaxb.mojo.resolver.tools.MavenCatalogResolver} */
	@Override
	public URL resolveDependencyResource(DependencyResource dependencyResource)
		throws MojoExecutionException
	{
		//
		// Guard Dependency getManagementKey(): groupId:artifactId:type[:classifier]
		//
		if (dependencyResource.getGroupId() == null)
		{
			throw new MojoExecutionException(format(
				"Dependency resource [%s] does not define the groupId.",
				dependencyResource));
		}

		if (dependencyResource.getArtifactId() == null)
		{
			throw new MojoExecutionException(format(
				"Dependency resource [%s] does not define the artifactId.",
				dependencyResource));
		}

		if (dependencyResource.getType() == null)
		{
			throw new MojoExecutionException(format(
				"Dependency resource [%s] does not define the type.",
				dependencyResource));
		}

		//
		// Merge dependencyResource with matching known dependencies.
		//
		
		if (getProject().getDependencyManagement() != null)
		{
			final List<Dependency> dependencies =
				getProject().getDependencyManagement().getDependencies();
			merge(dependencyResource, dependencies);
		}

		List<Dependency> dependencies = getProjectDependencies();
		if (!dependencies.isEmpty())
			merge(dependencyResource, dependencies);
		
		if ( getEpisodes() != null )
			merge(dependencyResource, getEpisodes());
		
		if ( getPlugins() != null )
			merge(dependencyResource, getPlugins());
		
		List<Dependency> schemaDependencyResources = new ArrayList<>();
		for ( ResourceEntry re : getSchemas() )
		{
			if ( re.getDependencyResource() != null )
				schemaDependencyResources.add(re.getDependencyResource());
		}
		if ( !schemaDependencyResources.isEmpty() )
			merge(dependencyResource, schemaDependencyResources);
		
		//
		// Resolve dependencyResource using path or repository.
		//

		if (dependencyResource.getResource() == null)
		{
			throw new MojoExecutionException(format(
				"Dependency resource [%s] does not define the resource.",
				dependencyResource));
		}
		
		if ( dependencyResource.getSystemPath() != null )
		{
			try
			{
				URL artifactURL = null;
				if ( dependencyResource.getSystemPath().toLowerCase().startsWith("file:") )
					artifactURL = new URI(dependencyResource.getSystemPath()).toURL();
				else
					artifactURL = Path.of(dependencyResource.getSystemPath()).toUri().toURL();
				
				File artifactFile = new File(artifactURL.getFile());
				return createArtifactResourceURL(artifactFile, dependencyResource.getResource());
			}
			catch (MalformedURLException | URISyntaxException muex)
			{
				throw new MojoExecutionException(format(
					"Dependency resource [%s] has a malformed systemPath.",
					dependencyResource), muex);
			}
		}
		else
		{
			if (dependencyResource.getVersion() == null)
			{
				throw new MojoExecutionException(format(
					"Dependency resource [%s] does not define the version.",
					dependencyResource));
			}
			
			try
			{
	            DefaultArtifact resourceArtifact = new DefaultArtifact
	            (
	            	dependencyResource.getGroupId(),
	            	dependencyResource.getArtifactId(),
	            	dependencyResource.getClassifier(),
	            	dependencyResource.getType(),
	            	dependencyResource.getVersion()
	            );
				
	            ArtifactRequest artifactRequest = new ArtifactRequest();
	            artifactRequest.setArtifact(resourceArtifact);
	            artifactRequest.setRepositories(getRemoteRepos());
	            
				// Resolves the path for a resource artifact.
				// The artifact will be downloaded to the local repository, if necessary.
				// An artifact that is already resolved will be skipped and is not re-resolved.
	            ArtifactResult artifactResult =
	            	getRepoSystem().resolveArtifact(getRepoSession(), artifactRequest);

				if ( !artifactResult.isResolved() )
				{
					for ( Exception ex : artifactResult.getExceptions() )
						getLog().error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
					
					throw new MojoExecutionException(format(
						"Dependency resource [%s] is not resolved.", dependencyResource));
				}
				
				// Create a URL for a "file:" or "jar" reference.
				final URL resourceURL =
					createArtifactResourceURL(artifactResult, dependencyResource.getResource());
				
				getLog().debug(format(
					"Resolved dependency resource [%s] to resource URL [%s].",
					dependencyResource, resourceURL));
				
				return resourceURL;
			}
			catch (org.eclipse.aether.resolution.ArtifactResolutionException arex)
			{
				throw new MojoExecutionException(format(
					"Could not resolve artifact for dependency [%s].",
					dependencyResource));
			}
		}
	}

	protected List<Dependency> getProjectDependencies()
	{
		final Set<org.apache.maven.artifact.Artifact> artifacts = getProject().getArtifacts();

		if (artifacts == null)
			return Collections.emptyList();
		else
		{
			final List<Dependency> dependencies = new ArrayList<Dependency>(artifacts.size());
			for (org.apache.maven.artifact.Artifact artifact : artifacts)
			{
				final Dependency dependency = new Dependency();
				dependency.setGroupId(artifact.getGroupId());
				dependency.setArtifactId(artifact.getArtifactId());
				dependency.setVersion(artifact.getVersion());
				dependency.setClassifier(artifact.getClassifier());
				dependency.setScope(artifact.getScope());
				dependency.setType(artifact.getType());
				dependencies.add(dependency);
			}
			return dependencies;
		}
	}
	
	private URL createArtifactResourceURL(ArtifactResult artifactResult, String resource)
		throws MojoExecutionException
	{
		File artifactFile = null;
		
		if ( artifactResult.isResolved() )
		{
			if ( artifactResult.getLocalArtifactResult() != null )
			{
				if ( artifactResult.getLocalArtifactResult().isAvailable() )
					artifactFile = artifactResult.getLocalArtifactResult().getFile();
			}
			
			if ( artifactFile == null )
				artifactFile = artifactResult.getArtifact().getFile();
		}
		
		if ( artifactFile != null )
			return createArtifactResourceURL(artifactFile, resource);
		else
		{
			throw new MojoExecutionException(format(
				"Could not create an URL for artifact result [%s] and resource [%s].",
				artifactResult, resource));
		}
	}
	
	private URL createArtifactResourceURL(File artifactFile, String resource)
		throws MojoExecutionException
	{
		if ( artifactFile.isDirectory() )
		{
			final File resourceFile = new File(artifactFile, resource);
			try
			{
				return resourceFile.toURI().toURL();
			}
			catch (MalformedURLException murlex)
			{
				throw new MojoExecutionException(format(
					"Could not create an URL for dependency directory [%s] and resource [%s].",
					artifactFile, resource));
			}
		}
		else
		{
			try
			{
				return new URL("jar:" + artifactFile.toURI().toURL().toExternalForm() + "!/" + resource);
			}
			catch (MalformedURLException murlex)
			{
				throw new MojoExecutionException(format(
					"Could not create an URL for dependency file [%s] and resource [%s].",
					artifactFile, resource));
			}
		}
	}

	private void merge(Dependency dependency, final Dependency[] managedDependencies)
	{
		for (Dependency managedDependency : managedDependencies)
		{
			if (dependency.getManagementKey().equals(managedDependency.getManagementKey()))
				mergeDependencyWithDefaults(dependency, managedDependency);
		}
	}
	
	private void merge(Dependency dependency, final List<Dependency> managedDependencies)
	{
		for (Dependency managedDependency : managedDependencies)
		{
			if (dependency.getManagementKey().equals(managedDependency.getManagementKey()))
				mergeDependencyWithDefaults(dependency, managedDependency);
		}
	}
}
