package org.jvnet.higherjaxb.mojo.testing;

import static org.apache.maven.repository.internal.MavenRepositorySystemUtils.newSession;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo.DEFAULT_REMOTE_REPO_ID;
import static org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo.DEFAULT_REMOTE_REPO_TYPE;
import static org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo.DEFAULT_REMOTE_REPO_URL;
import static org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo.DEFAULT_USER_LOCAL_REPO;
import static org.jvnet.higherjaxb.mojo.util.IOUtils.getMavenProjectDir;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.testing.PlexusTest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PlexusTest
abstract public class AbstractMojoTest
{
	public static final String DEFAULT_PROJECT_VERSION = "2.1.2-SNAPSHOT";
	public static final String DEFAULT_BASICJAXB_VERSION = "2.1.1";
	public static final String BUILD_PROPERTIES_PATH = "target/build.properties";
	public static final Properties BUILD_PROPERTIES = new Properties();
	public static final LocalRepository LOCAL_REPOSITORY = new LocalRepository(DEFAULT_USER_LOCAL_REPO);
	public static final DefaultRepositorySystemSession REPOSITORY_SYSTEM_SESSION = newSession();
	public static final RemoteRepository REMOTE_REPOSITORY =
		new Builder(DEFAULT_REMOTE_REPO_ID, DEFAULT_REMOTE_REPO_TYPE, DEFAULT_REMOTE_REPO_URL).build();
	
	@Inject
	public RepositorySystem repositorySystem;
	
	@BeforeAll
	public static void beforeAll()
	{
		BUILD_PROPERTIES.clear();
		try ( Reader reader = new FileReader(BUILD_PROPERTIES_PATH) )
		{
			BUILD_PROPERTIES.load(reader);
		}
		catch (IOException ex)
		{
			fail("Not found: " + BUILD_PROPERTIES_PATH, ex);
		}
	}
	
	@BeforeEach
    public void beforeEach() throws Exception
    {
		assertNotNull(LOCAL_REPOSITORY, "Aether Local Repository");
		assertNotNull(REMOTE_REPOSITORY, "Aether Remote Repository");
		assertNotNull(REPOSITORY_SYSTEM_SESSION, "Aether Repository System Session");
		assertNotNull(repositorySystem, "Aether Repository System");

		if ( REPOSITORY_SYSTEM_SESSION.getLocalRepositoryManager() == null )
		{
			LocalRepositoryManager localRepositoryManager =
				repositorySystem.newLocalRepositoryManager(REPOSITORY_SYSTEM_SESSION, LOCAL_REPOSITORY);
			REPOSITORY_SYSTEM_SESSION.setLocalRepositoryManager(localRepositoryManager);
			assertNotNull(localRepositoryManager, "Aether Local Repository Manager");
		}
		
		if ( getLogger().isDebugEnabled() )
		{
			getLogger().debug("Aether Local Repository .........: {}", LOCAL_REPOSITORY);
			getLogger().debug("Aether Local Repository Manager..: {}", REPOSITORY_SYSTEM_SESSION.getLocalRepositoryManager());
			getLogger().debug("Aether Remote Repository ........: {}", REMOTE_REPOSITORY);
			getLogger().debug("Aether Repository System Session.: {}", REPOSITORY_SYSTEM_SESSION);
			getLogger().debug("Aether Repository System.........: {}", repositorySystem);
		}
    }
	
	private Logger logger = LoggerFactory.getLogger(getClass());
    public Logger getLogger() { return logger; }
	public void setLogger(Logger logger) { this.logger = logger; }

	private File projectDir;
	protected File getProjectDir()
	{
		try
		{
			if ( projectDir == null )
				projectDir = getMavenProjectDir(getClass());
			return projectDir;
		}
		catch (Exception ex)
		{
			throw new AssertionError(ex);
		}
	}
	
	protected String getProjectVersion()
	{
		return BUILD_PROPERTIES.getProperty("project.version", DEFAULT_PROJECT_VERSION);
	}

	protected MavenProject createMavenProject()
	{
		MavenProject mp = new MavenProject();
		mp.getModel().setDependencyManagement(new DependencyManagement());
		return mp;
	}

	protected File fullpath(String path)
	{
		return new File(getProjectDir(), path);
	}
}
