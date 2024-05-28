package org.jvnet.higherjaxb.mojo;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Interface to configure <code>OptionsFactory</code> in each XJC version.
 * 
 * @param <O> Global options generic type.
 */
public interface CoreOptionsFactory<O>
{
	/**
	 * Set any/all system properties prior to MOJO execution.
	 * 
	 * @param systemOptionsConfiguration Mojo options configuration used to create the XJC options.
	 */
	public void setSystemOptions(SystemOptionsConfiguration systemOptionsConfiguration);
	
	/**
	 * Create the XJC options using the MOJO {@link OptionsConfiguration}.
	 * 
	 * @param optionsConfiguration MOJO options used to create the XJC options.
	 * 
	 * @return The specific XJC version options.
	 * 
	 * @throws MojoExecutionException When the MOJO options cannot be converted.
	 */
	public O createOptions(OptionsConfiguration optionsConfiguration)
		throws MojoExecutionException;
}
