package org.jvnet.higherjaxb.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.sun.tools.xjc.Options;

import org.jvnet.higherjaxb.mojo.v40.Higherjaxb40Mojo;

/**
 * This higherjaxb mojo provides the current version implementation.
 * 
 * @author Aleksei Valikov (valikov@gmx.net)
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE, threadSafe = true)
public class HigherjaxbMojo extends Higherjaxb40Mojo
{
	private final CoreOptionsFactory<Options> optionsFactory = new OptionsFactory();

	@Override
	protected org.jvnet.higherjaxb.mojo.CoreOptionsFactory<Options> getOptionsFactory()
	{
		return optionsFactory;
	}
}
