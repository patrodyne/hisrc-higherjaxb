package org.jvnet.higherjaxb.mojo.util;

import static java.util.Arrays.asList;
import static org.apache.maven.project.artifact.MavenMetadataSource.createArtifacts;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.jvnet.higherjaxb.mojo.util.CollectionUtils.Function;

@SuppressWarnings("deprecation")
public class ArtifactUtils
{
	// Seal this utility class
	private ArtifactUtils()
	{
	}

	public static Collection<Artifact> resolve
	(
		final ArtifactFactory artifactFactory,
		final ArtifactResolver artifactResolver,
		final ArtifactRepository localRepository,
		final ArtifactMetadataSource artifactMetadataSource,
		final Dependency[] dependencies,
		final MavenProject project
	)
		throws InvalidDependencyVersionException, ArtifactResolutionException, ArtifactNotFoundException
	{
		Set<Artifact> resolvedArtifacts = new HashSet<>();
		
		if (dependencies != null)
		{
			if (artifactFactory != null)
			{
				resolvedArtifacts = createArtifacts
				(
					artifactFactory,
					asList(dependencies),
					"runtime",
					null,
					project
				);
				
				for (Artifact artifact : resolvedArtifacts)
				{
					artifactResolver.resolve
					(
						artifact,
						project.getRemoteArtifactRepositories(),
						localRepository
					);
				}
				return resolvedArtifacts;
			}
			else
			{
				throw new IllegalArgumentException("Missing ArtifactFactory instance!");
			}
		}

		return resolvedArtifacts;
	}

	public static Collection<Artifact> resolveTransitively
	(
		final ArtifactFactory artifactFactory,
		final ArtifactResolver artifactResolver,
		final ArtifactRepository localRepository,
		final ArtifactMetadataSource artifactMetadataSource,
		final Dependency[] dependencies,
		final MavenProject project
	)
		throws InvalidDependencyVersionException, ArtifactResolutionException, ArtifactNotFoundException
	{
		Set<Artifact> resolvedArtifacts = new HashSet<>();

		if (dependencies != null)
		{
			final Set<Artifact> artifacts =
				createArtifacts
				(
					artifactFactory,
					asList(dependencies),
					"runtime",
					null,
					project
				);
			
			final ArtifactResolutionResult artifactResolutionResult = artifactResolver
				.resolveTransitively
				(
					artifacts,
					project.getArtifact(),
					project.getRemoteArtifactRepositories(),
					localRepository,
					artifactMetadataSource
				);
			
			resolvedArtifacts = artifactResolutionResult.getArtifacts();
		}

		return resolvedArtifacts;
	}

	public static final Function<Artifact, File> GET_FILE = new Function<Artifact, File>()
	{
		@Override
		public File eval(Artifact argument)
		{
			return argument.getFile();
		}
	};

	public static final Collection<File> getFiles(Collection<Artifact> artifacts)
	{
		return CollectionUtils.apply(artifacts, ArtifactUtils.GET_FILE);
	}

	public static void mergeDependencyWithDefaults(Dependency dep, Dependency def)
	{
		if (dep.getScope() == null && def.getScope() != null)
		{
			dep.setScope(def.getScope());
			dep.setSystemPath(def.getSystemPath());
		}
		
		if (dep.getVersion() == null && def.getVersion() != null)
			dep.setVersion(def.getVersion());

		if (dep.getClassifier() == null && def.getClassifier() != null)
			dep.setClassifier(def.getClassifier());

		if (dep.getType() == null && def.getType() != null)
			dep.setType(def.getType());

		@SuppressWarnings("rawtypes")
		List exclusions = dep.getExclusions();
		if (exclusions == null || exclusions.isEmpty())
			dep.setExclusions(def.getExclusions());
	}
}
