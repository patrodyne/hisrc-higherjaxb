package org.jvnet.higherjaxb.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jvnet.higherjaxb.mojo.CoreOptionsFactory;
import org.jvnet.higherjaxb.mojo.v30.Higherjaxb30Mojo;

import com.sun.tools.xjc.Options;

/**
 * This higherjaxb mojo provides the current version implementation.
 * 
 * @author Aleksei Valikov (valikov@gmx.net)
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE, threadSafe = true)
public class HigherjaxbMojo extends Higherjaxb30Mojo
{
	private final CoreOptionsFactory<Options> optionsFactory = new OptionsFactory();

	@Override
	protected org.jvnet.higherjaxb.mojo.CoreOptionsFactory<Options> getOptionsFactory()
	{
		return optionsFactory;
	}
}
