package org.jvnet.higherjaxb.mojo.util;

import static org.apache.maven.RepositoryUtils.toArtifact;
import static org.apache.maven.RepositoryUtils.toDependency;
import static org.eclipse.aether.util.filter.DependencyFilterUtils.classpathFilter;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.jvnet.higherjaxb.mojo.util.CollectionUtils.Function;

public class ArtifactUtils
{
	// Seal this utility class
	private ArtifactUtils()
	{
	}

	public static Collection<Artifact> resolve
	(
		final Dependency[] dependencies,
		final RepositorySystem repoSystem,
		final RepositorySystemSession repoSession,
		final List<RemoteRepository> remoteRepos
	)
		throws MojoExecutionException
	{
		final Set<Artifact> resolvedArtifacts = new HashSet<>();
		
		if ( dependencies != null )
		{
			for ( Dependency dependency : dependencies )
			{
				org.eclipse.aether.graph.Dependency aetherDependency =
					toDependency(dependency, repoSession.getArtifactTypeRegistry());
				
		        ArtifactRequest request = new ArtifactRequest();
		        request.setArtifact(aetherDependency.getArtifact());
		        request.setRepositories(remoteRepos);
		        
	            try
				{
	                ArtifactResult artifactResult =
	                	repoSystem.resolveArtifact(repoSession, request);
	                
	                if ( artifactResult.isResolved() )
	                {
		    			Artifact mavenArtifact = toArtifact(artifactResult.getArtifact());
		                resolvedArtifacts.add(mavenArtifact);
	                }
	                else
	                {
		                throw new MojoExecutionException("Artifact result is not resolved for " + artifactResult);
	                }
				}
				catch (org.eclipse.aether.resolution.ArtifactResolutionException ex)
				{
	                throw new MojoExecutionException(ex.getMessage(), ex);
				}
			}
		}
		
		return resolvedArtifacts;
	}

	public static Collection<Artifact> resolveTransitively
	(
		final Dependency[] dependencies,
		final RepositorySystem repoSystem,
		final RepositorySystemSession repoSession,
		final List<RemoteRepository> remoteRepos
	)
		throws MojoExecutionException
	{
		final Set<Artifact> resolvedArtifacts = new HashSet<>();
		
		if ( dependencies != null )
		{
	        // Select dependencies with "runtime" scope .
	        String scope = Artifact.SCOPE_RUNTIME;
	        DependencyFilter classpathFlter = classpathFilter(scope);

			for ( Dependency dependency : dependencies )
			{
				org.eclipse.aether.graph.Dependency aetherDependency =
					toDependency(dependency, repoSession.getArtifactTypeRegistry());
				
	            CollectRequest collectRequest = new CollectRequest();
	            collectRequest.setRoot(aetherDependency);
	            collectRequest.setRepositories(remoteRepos);
	            
	            DependencyRequest dependencyRequest =
	            	new DependencyRequest(collectRequest, classpathFlter);
	            
	    		try
	    		{
	                List<ArtifactResult> artifactResults = repoSystem
	    				.resolveDependencies
	    				(
	    					repoSession,
	    					dependencyRequest
	    				)
	    				.getArtifactResults();
	        		
	        		for ( ArtifactResult artifactResult : artifactResults )
	        		{
	        			if ( artifactResult.isResolved() )
	        			{

		        			Artifact mavenArtifact = toArtifact(artifactResult.getArtifact());
		        			resolvedArtifacts.add(mavenArtifact);
	        			}
		                else
		                {
			                throw new MojoExecutionException("Artifact result is not resolved for " + artifactResult);
		                }
	        		}
	    		}
	    		catch (DependencyResolutionException ex)
	    		{
	                throw new MojoExecutionException(ex.getMessage(), ex);
	    		}
			}
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
		if (dep.getSystemPath() == null && def.getSystemPath() != null)
		{
			dep.setScope(Artifact.SCOPE_SYSTEM);
			dep.setSystemPath(def.getSystemPath());
		}
		
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
