package org.jvnet.higherjaxb.mojo;

import static java.lang.String.format;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.jvnet.higherjaxb.mojo.resolver.tools.MavenCatalogResolver;
import org.jvnet.higherjaxb.mojo.util.StringUtils;

/**
 * Represents a Maven dependency with a system-id.
 * 
 * <p>The {@code <dependency>} element contains information about
 * a dependency in a Maven project.</p>
 */
public class DependencyResource extends Dependency
{
	private static final long serialVersionUID = -7680130645800522100L;
	
	private String resource;
	public String getResource() { return resource; }
	public void setResource(String resource) { this.resource = resource; }

	/**
	 * Construct with a runtime Maven artifact scope.
	 */
	public DependencyResource()
	{
		setScope(Artifact.SCOPE_RUNTIME);
	}
	
	/**
	 * Construct with a Maven model {@link Dependency}.
	 * 
	 * @param dep A Maven model {@link Dependency}.
	 */
	public DependencyResource(Dependency dep)
	{
		setGroupId(dep.getGroupId());
		setArtifactId(dep.getArtifactId());
		setVersion(dep.getVersion());
		setSystemPath(dep.getSystemPath());
		setScope(dep.getScope());
	}

	/**
	 * Construct with a Maven model {@link Dependency} and a resource.
	 * 
	 * @param dep A Maven model {@link Dependency}.
	 * @param resource The resource path.
	 */
	public DependencyResource(Dependency dep, String resource)
	{
		this(dep);
		setResource(resource);
	}

	@Override
	public String toString()
	{
		return "Dependency {groupId=" + getGroupId() + ", artifactId=" + getArtifactId() + ", version=" + getVersion()
				+ ", type=" + getType() + ", classifier=" + getClassifier() + ", resource=" + getResource() + "}";
	}

	private String systemId;
	public String getSystemId()
	{
		if (this.systemId != null)
			return this.systemId;
		else
		{
			// maven:groupId:artifactId:type:classifier:version!/resource/path/in/jar/schema.xsd
			StringBuilder sb = new StringBuilder();
			sb.append(MavenCatalogResolver.URI_SCHEME_MAVEN).append(':');
			sb.append(getGroupId()).append(':');
			sb.append(getArtifactId()).append(':');
			sb.append(getType() == null ? "" : getType()).append(':');
			sb.append(getClassifier() == null ? "" : getClassifier()).append(':');
			sb.append(getVersion() == null ? "" : getVersion());
			sb.append("!/");
			sb.append(getResource());
			return sb.toString();
		}
	}
	public void setSystemId(String systemId)
	{
		this.systemId = systemId;
	}

	public static DependencyResource valueOf(String value)
		throws IllegalArgumentException
	{
		final String resourceDelimiter = "!/";
		final int resourceDelimiterPosition = value.indexOf(resourceDelimiter);
		final String dependencyPart;
		final String resource;
		
		if (resourceDelimiterPosition == -1)
		{
			dependencyPart = value;
			resource = "";
		}
		else
		{
			dependencyPart = value.substring(0, resourceDelimiterPosition);
			resource = value.substring(resourceDelimiterPosition + resourceDelimiter.length());
		}
		
		final String[] dependencyParts = StringUtils.split(dependencyPart, ':', true);
		
		if (dependencyParts.length < 2)
		{
			throw new IllegalArgumentException(format(
				"Error parsing dependency descriptor [%s], both groupId and artifactId must be specified.",
				dependencyPart));
		}
		
		if (dependencyParts.length > 5)
		{
			throw new IllegalArgumentException(format("Error parsing dependency descriptor [%s], it contains too many parts.",
				dependencyPart));
		}
		
		final String groupId = dependencyParts[0];
		final String artifactId = dependencyParts[1];
		final String version;
		final String type;
		
		if (dependencyParts.length > 2)
			type = (dependencyParts[2] == null || dependencyParts[2].length() == 0) ? null : dependencyParts[2];
		else
			type = null;
		
		final String classifier;
		if (dependencyParts.length > 3)
			classifier = (dependencyParts[3] == null || dependencyParts[3].length() == 0) ? null : dependencyParts[3];
		else
			classifier = null;
		
		if (dependencyParts.length > 4)
			version = (dependencyParts[4] == null || dependencyParts[4].length() == 0) ? null : dependencyParts[4];
		else
			version = null;
		
		final DependencyResource dependencyResource = new DependencyResource();
		dependencyResource.setGroupId(groupId);
		dependencyResource.setArtifactId(artifactId);
		
		if (version != null)
			dependencyResource.setVersion(version);
		
		if (type != null)
			dependencyResource.setType(type);
		
		if (classifier != null)
			dependencyResource.setClassifier(classifier);
		
		if (resource != null)
			dependencyResource.setResource(resource);
		
		dependencyResource.setSystemId(value);
		
		return dependencyResource;
	}
}
