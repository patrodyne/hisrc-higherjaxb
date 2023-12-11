package org.jvnet.higherjaxb.mojo;

import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * An interface to resolve Maven dependency resource (i.e. an XML schema from a JAR). This may
 * be implemented on the custom {@link org.apache.maven.plugin.AbstractMojo} implementation.
 * 
 * <p>
 * Note: This is typically use to resolve <a href="https://en.wikipedia.org/wiki/XML_catalog">XML catalog</a>
 * mappings.
 * </p>
 */
public interface DependencyResourceResolver
{
	/**
	 * Resolve a resource from a Maven dependency such as an XML schema from a JAR artifact.
	 * 
	 * @param dependencyResource A resource to resolve.
	 * 
	 * @return An URL representing a dependency resource.
	 * 
	 * @throws MojoExecutionException when the resource cannot be resolved.
	 */
	public URL resolveDependencyResource(DependencyResource dependencyResource)
		throws MojoExecutionException;
}
