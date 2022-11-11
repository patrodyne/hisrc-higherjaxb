package org.jvnet.higherjaxb.mojo.tests.jvnet_issue_77;

import java.io.File;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

public class RunJvnetIssue77Mojo extends RunHigherjaxbMojo {

	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo mojo) {
		super.configureMojo(mojo);
		
		mojo.setSchemaDirectory(new File(getBaseDir(), "src/main/resources/META-INF/project/schemas"));
		mojo.setGeneratePackage("com.company.project.service.types");
		mojo.setCatalog(new File(getBaseDir(),"src/main/jaxb/catalog.cat"));
		mojo.setCatalogResolver("org.jvnet.higherjaxb.mojo.resolver.tools.ClasspathCatalogResolver");
		mojo.setExtension(true);
//		mojo.
//
//		final ResourceEntry a_xsd = new ResourceEntry();
//		a_xsd.setUrl("http://www.ab.org/a.xsd");
//		mojo.setStrict(false);
//		mojo.setSchemaIncludes(new String[] {});
//		mojo.setSchemas(new ResourceEntry[] { a_xsd });
//		mojo.setCatalog(new File(getBaseDir(), "src/main/resources/catalog.cat"));
		mojo.setForceRegenerate(true);
	}

}
