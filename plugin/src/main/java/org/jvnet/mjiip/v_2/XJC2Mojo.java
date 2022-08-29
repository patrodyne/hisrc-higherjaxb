package org.jvnet.mjiip.v_2;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jvnet.higherjaxb.mojo.XJC30Mojo;

import com.sun.tools.xjc.Options;

/**
 * HigherJAXB Mojo.
 * 
 * @author Aleksei Valikov (valikov@gmx.net)
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE, threadSafe = true)
public class XJC2Mojo extends XJC30Mojo {

	private final org.jvnet.jaxb2.maven2.OptionsFactory<Options> optionsFactory = new OptionsFactory();

	@Override
	protected org.jvnet.jaxb2.maven2.OptionsFactory<Options> getOptionsFactory() {
		return optionsFactory;
	}
}
