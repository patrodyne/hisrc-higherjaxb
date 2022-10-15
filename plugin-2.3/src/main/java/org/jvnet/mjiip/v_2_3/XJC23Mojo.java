package org.jvnet.mjiip.v_2_3;

import static com.sun.tools.xjc.Language.DTD;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlSchema;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jvnet.jaxb2.maven2.RawXJC2Mojo;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.ModelLoader;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.outline.Outline;

import com.sun.tools.xjc.generator.bean.ImplStructureStrategy;

import com.sun.xml.txw2.annotation.XmlNamespace;

/**
 * JAXB 2.x Mojo.
 * 
 * @author Aleksei Valikov (valikov@gmx.net)
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class XJC23Mojo extends RawXJC2Mojo<Options> {

	private static final String JAXB_NSURI = "http://java.sun.com/xml/ns/jaxb";
	private static final String JAXB_EPISODE_PKG_NAME = "com.sun.xml.bind.v2.schemagen.episode.package-info";

	@Override
	public String getSpecVersion()
	{
		return "2.3";
	}
	
	private final org.jvnet.jaxb2.maven2.OptionsFactory<Options> optionsFactory = new OptionsFactory();
	@Override
	protected org.jvnet.jaxb2.maven2.OptionsFactory<Options> getOptionsFactory() {
		return optionsFactory;
	}

	@Override
	public void doExecute(Options options) throws MojoExecutionException {
		final Model model = loadModel(options);
		final Outline outline = generateCode(model);
		writeCode(outline);

	}
	
	@Override
	protected String getJaxbNamespaceURI()
	{
		return JAXB_NSURI;
	}

	@Override
	protected String[] getXmlSchemaNames(final Class<?> packageInfoClass)
	{
		String[] xmlSchemaNames = null;
		String xmlSchemaClassName = XmlSchema.class.getName();
		final XmlSchema xmlSchema = packageInfoClass.getAnnotation(XmlSchema.class);
		if ( xmlSchema != null )
		{
			String xmlSchemaNamespace = xmlSchema.namespace();
			xmlSchemaNames = new String[] { xmlSchemaNamespace, xmlSchemaClassName };
		}
		else
			xmlSchemaNames = new String[] { null, xmlSchemaClassName };
		return xmlSchemaNames;
	}
	
	@Override
	protected String getEpisodePackageName()
	{
		return JAXB_EPISODE_PKG_NAME;
	}
	
	@Override
	protected String[] getXmlNamespaceNames(final Class<?> packageInfoClass)
	{
		String xmlNamespaceClassName = XmlNamespace.class.getName();
		String[] xmlNamespaceNames = null;
		final XmlNamespace xmlNamespace = packageInfoClass.getAnnotation(XmlNamespace.class);
		if ( xmlNamespace != null )
		{
			String xmlNamespaceValue = xmlNamespace.value();
			xmlNamespaceNames = new String[] { xmlNamespaceValue, xmlNamespaceClassName };
		}
		else
			xmlNamespaceNames = new String[] { null, xmlNamespaceClassName };
		return xmlNamespaceNames;
	}
	
	protected Model loadModel(Options options) throws MojoExecutionException {
		if (getVerbose()) {
			getLog().info("Parsing input schema(s)...");
		}
		final Model model = ModelLoader.load(options, new JCodeModel(),
				new LoggingErrorReceiver("Error while parsing schema(s).",
						getLog(), getVerbose()));

		if (model != null)
		{
			// Ensure DTD 'value' property name(s) conforms to a get/set name.
			if (  options.getSchemaLanguage() == DTD )
			{
				for ( Entry<NClass, CClassInfo> beanEntry : model.beans().entrySet() )
				{
					if ( beanEntry.getValue() != null )
					{
						for ( CPropertyInfo property : beanEntry.getValue().getProperties() )
						{
							if ( property instanceof CValuePropertyInfo )
								property.setName(true, StringUtils.capitalize(property.getName(true)));
						}
					}
				}
			}
			
			// Verbose: list the model's beans.
			if (getVerbose())
			{
				getLog().info("Model Strategy: " + model.strategy);
				for ( Entry<NClass, CClassInfo> beanEntry : model.beans().entrySet() )
				{
					String beanKey = (beanEntry.getKey() != null ) ? beanEntry.getKey().toString() : "";
					String beanVal = (beanEntry.getValue() != null ) ? beanEntry.getValue().toString() : "";
					if ( beanKey.equals(beanVal) )
						getLog().info("JAXB Bean: [" + beanVal + "]");
					else
						getLog().info("JAXB Bean: [key=" + beanEntry.getKey() + ", value=" + beanEntry.getValue() + "]");
				}
			}
		}
		else
			throw new MojoExecutionException("Unable to parse input schema(s). Error messages should have been provided.");
		
		return model;
	}

	protected Outline generateCode(final Model model)
			throws MojoExecutionException {
		if (getVerbose()) {
			getLog().info("Compiling input schema(s)...");
		}

		if ( getLog().isDebugEnabled() )
		{
			for ( Plugin plugin : model.options.activePlugins )
				getLog().debug("Active plugin: "+plugin.getClass().getName());
		}

		final Outline outline = model.generateCode(model.options,
				new LoggingErrorReceiver("Error while generating code.",
						getLog(), getVerbose()));
		if (outline == null) {
			throw new MojoExecutionException(
					"Failed to compile input schema(s)! Error messages should have been provided.");
		} else {
			return outline;
		}
	}

	protected void writeCode(Outline outline) throws MojoExecutionException {

		if (getWriteCode()) {
			final Model model = outline.getModel();
			final JCodeModel codeModel = model.codeModel;
			final File targetDirectory = model.options.targetDir;
			if (getVerbose()) {
				getLog().info(
						MessageFormat.format("Writing output to [{0}].",
								targetDirectory.getAbsolutePath()));
			}
			try {
				if (getCleanPackageDirectories()) {
					if (getVerbose()) {
						getLog().info("Cleaning package directories.");
					}
					cleanPackageDirectories(targetDirectory, codeModel);
				}
				final CodeWriter codeWriter = new LoggingCodeWriter(
						model.options.createCodeWriter(), getLog(),
						getVerbose());
				codeModel.build(codeWriter);
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to write files: "
						+ e.getMessage(), e);
			}
		} else {
			getLog().info(
					"The [writeCode] setting is set to false, the code will not be written.");
		}
	}

	private void cleanPackageDirectories(File targetDirectory,
			JCodeModel codeModel) {
		for (Iterator<JPackage> packages = codeModel.packages(); packages
				.hasNext();) {
			final JPackage _package = packages.next();
			final File packageDirectory;
			if (_package.isUnnamed()) {
				packageDirectory = targetDirectory;
			} else {
				packageDirectory = new File(targetDirectory, _package.name()
						.replace('.', File.separatorChar));
			}
			if (packageDirectory.isDirectory()) {
				if (isRelevantPackage(_package)) {
					if (getVerbose()) {
						getLog().info(
								MessageFormat
										.format("Cleaning directory [{0}] of the package [{1}].",
												packageDirectory
														.getAbsolutePath(),
												_package.name()));
					}
					cleanPackageDirectory(packageDirectory);
				} else {
					if (getVerbose()) {
						getLog().info(
								MessageFormat
										.format("Skipping directory [{0}] of the package [{1}] as it does not contain generated classes or resources.",
												packageDirectory
														.getAbsolutePath(),
												_package.name()));
					}
				}
			}
		}
	}

	private boolean isRelevantPackage(JPackage _package) {
		if ( "META-INF".equals(_package.name()) ) {
			return false;
		}
		if (_package.propertyFiles().hasNext()) {
			return true;
		}
		Iterator<JDefinedClass> classes = _package.classes();
		for (; classes.hasNext();) {
			JDefinedClass _class = (JDefinedClass) classes.next();
			if (!_class.isHidden()) {
				return true;
			}
		}
		return false;
	}
}
