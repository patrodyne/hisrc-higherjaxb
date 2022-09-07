package org.jvnet.jaxb2.maven2;

import java.io.File;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.junit.jupiter.api.Test;
import org.jvnet.mjiip.v_2.XJC2Mojo;

public abstract class JAXBGenerateTest extends AbstractMojoTestCase {

	static {
		System.setProperty("basedir", getBaseDir().getAbsolutePath());
	}

	protected MavenProjectBuilder mavenProjectBuilder;

	protected void setUp() throws Exception {
		super.setUp();

//		mavenProjectBuilder = (MavenProjectBuilder) getContainer().lookup(
//				MavenProjectBuilder.ROLE);
		
		// KLUDGE - Rick - 2022-07-19
		mavenProjectBuilder = (MavenProjectBuilder) getContainer().lookup(
			"ROLE");
	}

	protected static File getBaseDir() {
		try {
			return (new File(JAXBGenerateTest.class.getProtectionDomain()
					.getCodeSource().getLocation().getFile())).getParentFile()
					.getParentFile().getAbsoluteFile();
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * Validate the generation of a java files from purchaseorder.xsd.
	 * 
	 * @throws MojoExecutionException
	 */
	@Test
	public void testExecute() throws Exception {

		final File pom = new File(getBaseDir(),
		"src/test/resources/test-pom.xml");
		
        final ArtifactRepository localRepository = new DefaultArtifactRepository( "local", 
        		
        		new File(getBaseDir(), "target/test-repository").toURI().toURL().toString()        		, new DefaultRepositoryLayout());
		
		
		final MavenProject mavenProject = mavenProjectBuilder.build(pom, localRepository, null);
		

		final XJC2Mojo generator = (XJC2Mojo) lookupMojo("generate", pom);
		generator.setProject(mavenProject);
		generator.setLocalRepository(localRepository);
		generator.setSchemaDirectory(new File(getBaseDir(),"src/test/resources/"));
		generator.setSchemaIncludes(new String[] { "*.xsd" });
		generator.setBindingIncludes(new String[] { "*.xjb" });
		generator.setGenerateDirectory(new File(getBaseDir(), "target/test/generated-sources"));
		generator.setVerbose(true);
		generator.setGeneratePackage("unittest");
		generator.setRemoveOldOutput(false);
		
		generator.execute();
	}

	public static void main(String[] args) throws Exception {
		//	new JAXBGenerateTest().testExecute();
	}
}
