package org.jvnet.higherjaxb.mojo.testing.foo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

public class FooMojo extends AbstractMojo
{
	public static File DEFAULT_USER_LOCAL_REPO = org.apache.maven.repository.RepositorySystem.defaultUserLocalRepository;
	public static String DEFAULT_REMOTE_REPO_ID = org.apache.maven.repository.RepositorySystem.DEFAULT_REMOTE_REPO_ID;
	public static String DEFAULT_REMOTE_REPO_URL = org.apache.maven.repository.RepositorySystem.DEFAULT_REMOTE_REPO_URL;
	public static String DEFAULT_REMOTE_REPO_TYPE = "default";

	/**
     * The entry point to Maven Artifact Resolver, i.e. the component doing all the work.
     */
    @Component
    private RepositorySystem repoSystem;
    public RepositorySystem getRepoSystem() { return repoSystem; }
	public void setRepoSystem(RepositorySystem repoSystem) { this.repoSystem = repoSystem; }

    /**
     * The project's remote repositories to use for the resolution.
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;
	public List<RemoteRepository> getRemoteRepos()
	{
		if ( remoteRepos == null )
			remoteRepos = new ArrayList<RemoteRepository>();
		return remoteRepos;
	}
	
	/**
     * The current repository/network configuration of Maven.
     */
    @Parameter(defaultValue = "${REPOSITORY_SYSTEM_SESSION}", readonly = true)
    private RepositorySystemSession repoSession;
	public RepositorySystemSession getRepoSession() { return repoSession; }
	public void setRepoSession(RepositorySystemSession repoSession) { this.repoSession = repoSession; }
	
    @Parameter( defaultValue = "${project}", readonly = true )
	private MavenProject project;
	public MavenProject getProject()
	{
		if ( project == null )
			setProject((MavenProject) getPluginContext().get("project"));
		return project;
	}
    public void setProject(MavenProject project) { this.project = project; }
	
	@Override
	public void execute()
		throws MojoExecutionException
	{
		getLog().info("HELLO FROM FOO");
	}
}
