package org.jvnet.higherjaxb.mojo;

import static java.lang.String.format;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Mojo options configuration used to create the XJC options.
 */
public class OptionsConfiguration extends SystemOptionsConfiguration
{
	private final String encoding;
	public String getEncoding() { return encoding; }
	
	private final String schemaLanguage;
	public String getSchemaLanguage() { return schemaLanguage; }

	private List<InputSource> grammars;
	public List<InputSource> getGrammars() { return grammars; }

	private List<InputSource> bindFiles;
	public List<InputSource> getBindFiles() { return bindFiles; }

	private final EntityResolver entityResolver;
	public EntityResolver getEntityResolver() { return entityResolver; }

	private final String generatePackage;
	public String getGeneratePackage() { return generatePackage; }

	private final File generateDirectory;
	public File getGenerateDirectory() { return generateDirectory; }

	private final boolean readOnly;
	public boolean isReadOnly() { return readOnly; }

	private final boolean packageLevelAnnotations;
	public boolean isPackageLevelAnnotations() { return packageLevelAnnotations; }

	private final boolean noFileHeader;
	public boolean isNoFileHeader() { return noFileHeader; }

	private final boolean enableIntrospection;
	public boolean isEnableIntrospection() { return enableIntrospection; }

	private final boolean disableXmlSecurity;
	public boolean isDisableXmlSecurity() { return disableXmlSecurity; }

	private final boolean contentForWildcard;
	public boolean isContentForWildcard() { return contentForWildcard; }

	private final boolean extension;
	public boolean isExtension() { return extension; }

	private final boolean strict;
	public boolean isStrict() { return strict; }

	private final boolean verbose;
	public boolean isVerbose() { return verbose; }

	private final boolean debugMode;
	public boolean isDebugMode() { return debugMode; }

	private final List<String> arguments;
	public List<String> getArguments() { return arguments; }

	private final List<URL> plugins;
	public List<URL> getPlugins() { return plugins; }
	
	private final String specVersion;
	public String getSpecVersion() { return specVersion; }

	public OptionsConfiguration
	(
		String accessExternalSchema,
		String accessExternalDTD,
		boolean enableExternalEntityProcessing,
		String encoding,
		String schemaLanguage,
		List<InputSource> grammars,
		List<InputSource> bindFiles,
		EntityResolver entityResolver,
		String generatePackage,
		File generateDirectory,
		boolean readOnly,
		boolean packageLevelAnnotations,
		boolean noFileHeader,
		boolean enableIntrospection,
		boolean disableXmlSecurity,
		boolean contentForWildcard,
		boolean extension,
		boolean strict,
		boolean verbose,
		boolean debugMode,
		List<String> arguments,
		List<URL> plugins,
		String specVersion
	)
	{
		super
		(
			accessExternalSchema,
			accessExternalDTD,
			enableExternalEntityProcessing
		);
		this.encoding = encoding;
		this.schemaLanguage = schemaLanguage;
		this.grammars = grammars;
		this.bindFiles = bindFiles;
		this.entityResolver = entityResolver;
		this.generatePackage = generatePackage;
		this.generateDirectory = generateDirectory;
		this.readOnly = readOnly;
		this.packageLevelAnnotations = packageLevelAnnotations;
		this.noFileHeader = noFileHeader;
		this.enableIntrospection = enableIntrospection;
		this.disableXmlSecurity = disableXmlSecurity;
		this.contentForWildcard = contentForWildcard;
		this.extension = extension;
		this.strict = strict;
		this.verbose = verbose;
		this.debugMode = debugMode;
		this.arguments = arguments;
		this.plugins = plugins;
		this.specVersion = specVersion;
	}

	@Override
	public String toString()
	{
		return format
		(
			"OptionsConfiguration [" +
				super.toString() + "\n" +
				"specVersion=%s\n " +
				"generateDirectory=%s\n " +
				"generatePackage=%s\n " +
				"schemaLanguage=%s\n " +
				"grammars.systemIds=%s\n " +
				"bindFiles.systemIds=%s\n " +
				"plugins=%s\n " +
				"readOnly=%s\n " +
				"packageLevelAnnotations=%s\n " +
				"noFileHeader=%s\n " +
				"enableIntrospection=%s\n " +
				"disableXmlSecurity=%s\n " +
				"contentForWildcard=%s\n " +
				"extension=%s\n " +
				"strict=%s\n " +
				"verbose=%s\n " +
				"debugMode=%s\n " +
				"arguments=%s" +
			"]",
			specVersion,
			generateDirectory,
			generatePackage,
			schemaLanguage,
			getSystemIds(grammars),
			getSystemIds(bindFiles),
			plugins,
			readOnly,
			packageLevelAnnotations,
			noFileHeader,
			enableIntrospection,
			disableXmlSecurity,
			contentForWildcard,
			extension,
			strict,
			verbose,
			debugMode,
			arguments
		);

	}

	private List<String> getSystemIds(List<InputSource> inputSources)
	{
		List<String> systemIds = null;
		if ( inputSources != null )
		{
			systemIds = new ArrayList<String>(inputSources.size());
			for (InputSource inputSource : inputSources)
				systemIds.add( (inputSource == null) ? null : inputSource .getSystemId() );
		}
		return systemIds;
	}
}
