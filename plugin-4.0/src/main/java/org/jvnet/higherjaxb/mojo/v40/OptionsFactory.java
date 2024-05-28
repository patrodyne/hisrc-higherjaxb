package org.jvnet.higherjaxb.mojo.v40;

import static java.lang.String.format;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.jvnet.higherjaxb.mojo.CoreOptionsFactory;
import org.jvnet.higherjaxb.mojo.OptionsConfiguration;
import org.jvnet.higherjaxb.mojo.SystemOptionsConfiguration;
import org.jvnet.higherjaxb.mojo.util.StringUtils;
import org.xml.sax.InputSource;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.api.SpecVersion;

public class OptionsFactory implements CoreOptionsFactory<Options>
{
	/**
	 * Set any/all system properties prior to MOJO execution.
	 */
	@Override
	public void setSystemOptions(SystemOptionsConfiguration soc)
	{
		if (soc.getAccessExternalSchema() != null)
			System.setProperty("javax.xml.accessExternalSchema", soc.getAccessExternalSchema());
		if (soc.getAccessExternalDTD() != null)
			System.setProperty("javax.xml.accessExternalDTD", soc.getAccessExternalDTD());
		if (soc.isEnableExternalEntityProcessing())
			System.setProperty("enableExternalEntityProcessing",
				soc.isEnableExternalEntityProcessing().toString());
	}
	
	/**
	 * Creates and initializes an instance of XJC options.
	 */
	@Override
	public Options createOptions(OptionsConfiguration optionsConfiguration)
		throws MojoExecutionException
	{
		setSystemOptions(optionsConfiguration);
		
		final Options options = new Options();
		
		final String encoding = optionsConfiguration.getEncoding();
		if (encoding != null)
			options.encoding = createEncoding(encoding);
		options.setSchemaLanguage(createLanguage(optionsConfiguration.getSchemaLanguage()));
		for (InputSource grammar : optionsConfiguration.getGrammars())
			options.addGrammar(grammar);
		for (InputSource bindFile : optionsConfiguration.getBindFiles())
			options.addBindFile(bindFile);
		options.entityResolver = optionsConfiguration.getEntityResolver();
		options.defaultPackage = optionsConfiguration.getGeneratePackage();
		options.targetDir = optionsConfiguration.getGenerateDirectory();
		options.readOnly = optionsConfiguration.isReadOnly();
		options.packageLevelAnnotations = optionsConfiguration.isPackageLevelAnnotations();
		options.noFileHeader = optionsConfiguration.isNoFileHeader();
		options.enableIntrospection = optionsConfiguration.isEnableIntrospection();
		options.disableXmlSecurity = optionsConfiguration.isDisableXmlSecurity();
		options.contentForWildcard = optionsConfiguration.isContentForWildcard();
		if (optionsConfiguration.isExtension())
			options.compatibilityMode = Options.EXTENSION;
		options.strictCheck = optionsConfiguration.isStrict();
		options.verbose = optionsConfiguration.isVerbose();
		options.debugMode = optionsConfiguration.isDebugMode();
		final List<String> arguments = optionsConfiguration.getArguments();
		try
		{
			options.parseArguments(arguments.toArray(new String[arguments.size()]));
		}
		catch (BadCommandLineException bclex)
		{
			throw new MojoExecutionException(format("Error parsing the command line [%s]", arguments), bclex);
		}
		options.classpaths.addAll(optionsConfiguration.getPlugins());

		options.target = SpecVersion.V3_0;
		
		return options;
	}

	private String createEncoding(String encoding)
		throws MojoExecutionException
	{
		if (encoding == null)
			return null;

		try
		{
			if (!Charset.isSupported(encoding))
				throw new MojoExecutionException(format("Unsupported encoding [%s].", encoding));

			return encoding;
		}
		catch (IllegalCharsetNameException icne)
		{
			throw new MojoExecutionException(format("Unsupported encoding [%s].", encoding));
		}
	}

	private Language createLanguage(String schemaLanguage)
		throws MojoExecutionException
	{
		if (StringUtils.isEmpty(schemaLanguage))
			return null;
		else if ("AUTODETECT".equalsIgnoreCase(schemaLanguage))
			return null; // nothing, it is AUTDETECT by default.
		else if ("XMLSCHEMA".equalsIgnoreCase(schemaLanguage))
			return Language.XMLSCHEMA;
		else if ("DTD".equalsIgnoreCase(schemaLanguage))
			return Language.DTD;
		else if ("WSDL".equalsIgnoreCase(schemaLanguage))
			return Language.WSDL;
		else
			throw new MojoExecutionException(format("Unknown schemaLanguage [%s].", schemaLanguage));
	}
}
