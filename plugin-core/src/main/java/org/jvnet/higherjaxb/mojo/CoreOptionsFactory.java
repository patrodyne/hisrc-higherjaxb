package org.jvnet.higherjaxb.mojo;

import org.apache.maven.plugin.MojoExecutionException;

public interface CoreOptionsFactory<O> {

	public O createOptions(OptionsConfiguration optionsConfiguration)
			throws MojoExecutionException;

}
