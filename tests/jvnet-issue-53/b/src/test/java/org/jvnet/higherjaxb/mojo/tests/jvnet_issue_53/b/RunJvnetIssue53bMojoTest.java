package org.jvnet.higherjaxb.mojo.tests.jvnet_issue_53.b;

import static java.lang.String.format;
import static org.apache.maven.artifact.Artifact.SCOPE_SYSTEM;

import java.util.ArrayList;

import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.resolver.tools.MavenCatalogResolver;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

public class RunJvnetIssue53bMojoTest extends AbstractMojoTest
{
	@Test
	public void testExecute() throws Exception
	{
		// An "META-INF/sun-jaxb.episode" file is generated by the XJC (XML
		// Schema to Java) compiler. It is a schema bindings that associates
		// schema types with existing classes. It is useful when you have one
		// XML schema that is imported by other schemas, as it prevents the
		// model from being regenerated. XJC will scan JARs for '*.episode
		// files', then use them as binding files automatically.
		final Dependency episode = new Dependency();
		episode.setGroupId("org.patrodyne.jvnet");
		episode.setArtifactId("hisrc-higherjaxb-maven-plugin-tests-jvnet-issue-53-a");
		episode.setVersion(getProjectVersion());
		episode.setSystemPath(format("../a/target/%s-%s.jar", episode.getArtifactId(), episode.getVersion()));
		episode.setScope(SCOPE_SYSTEM);
		
		HigherjaxbMojo mojo = new HigherjaxbMojo();
		mojo.setLog(new SLF4JLogger(getLogger()));

		mojo.getRemoteRepos().add(REMOTE_REPOSITORY);
		mojo.setRepoSession(REPOSITORY_SYSTEM_SESSION);
		mojo.setRepoSystem(repositorySystem);
		
		mojo.setProject(createMavenProject());
		
		mojo.setCatalogResolver(MavenCatalogResolver.class.getName());
		mojo.setCatalog(fullpath("src/main/resources/catalog.cat"));
		mojo.setStrict(false);
		
		mojo.setEpisodes(new Dependency[] { episode });
		
		mojo.setSchemaDirectory(fullpath("src/main/resources"));
		mojo.setGenerateDirectory(fullpath("target/generated-sources/xjc")); 
		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setForceRegenerate(true);
		mojo.setNoFileHeader(true);

		// Enable Schema Component Designator (SCD) support, etc.
		mojo.setExtension(true);

		mojo.setArgs(new ArrayList<>());
		
		mojo.execute();
	}
}
