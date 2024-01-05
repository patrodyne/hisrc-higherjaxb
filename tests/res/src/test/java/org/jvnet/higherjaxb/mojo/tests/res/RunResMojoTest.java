package org.jvnet.higherjaxb.mojo.tests.res;

import static java.lang.String.format;
import static org.apache.maven.artifact.Artifact.SCOPE_SYSTEM;

import java.util.ArrayList;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.FileSet;
import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.DependencyResource;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.ResourceEntry;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

public class RunResMojoTest extends AbstractMojoTest
{
	@Test
	public void testExecute() throws Exception
	{
		// Dependencies
		
		final Dependency basicjaxb = new Dependency();
		basicjaxb.setGroupId("org.patrodyne.jvnet");
		basicjaxb.setArtifactId("hisrc-basicjaxb-plugins");
		basicjaxb.setVersion(DEFAULT_BASICJAXB_VERSION);

		final Dependency po_dep = new Dependency();
		po_dep.setGroupId("org.patrodyne.jvnet");
		po_dep.setArtifactId("hisrc-higherjaxb-maven-plugin-tests-po");
		po_dep.setVersion(getProjectVersion());
		po_dep.setSystemPath(format("../po/target/%s-%s.jar", po_dep.getArtifactId(), po_dep.getVersion()));
		po_dep.setScope(SCOPE_SYSTEM);

		// Resource Entries
		
		final FileSet schemas_fs = new FileSet();
		schemas_fs.setDirectory("src/main/schemas");
		schemas_fs.addInclude("*.*");
		schemas_fs.addExclude("*.xs");
		final ResourceEntry schemas_re = new ResourceEntry(schemas_fs);
		
        final ResourceEntry og_re =
        	new ResourceEntry("http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd");

		final DependencyResource po_dr = new DependencyResource(po_dep, "purchaseorder.xsd");
		po_dr.setVersion(null);
		final ResourceEntry po_re = new ResourceEntry(po_dr);

		final HigherjaxbMojo mojo = new HigherjaxbMojo();
		mojo.setLog(new SLF4JLogger(getLogger()));

		mojo.getRemoteRepos().add(REMOTE_REPOSITORY);
		mojo.setRepoSession(REPOSITORY_SYSTEM_SESSION);
		mojo.setRepoSystem(repositorySystem);
		
		mojo.setProject(createMavenProject());
		mojo.getProject().getDependencyManagement().addDependency(po_dep);
		
		mojo.setStrict(false);
		mojo.setCatalog(fullpath("src/main/resources/catalog.cat"));
		
		mojo.setSchemas(new ResourceEntry[] { schemas_re, og_re, po_re });
		
		mojo.setSchemaDirectory(fullpath("src/main/resources"));
		mojo.setSchemaIncludes(new String[] { "*.xsd" });
		mojo.setGenerateDirectory(fullpath("target/generated-sources/xjc")); 
		
		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setForceRegenerate(true);
		mojo.setNoFileHeader(true);
		mojo.setExtension(true);
		
		mojo.setArgs(new ArrayList<>());
		mojo.getArgs().add("-Xinheritance");
		
		mojo.setPlugins(new Dependency[] { basicjaxb });
		
		mojo.execute();
	}
}
