package org.jvnet.higherjaxb.mojo.tests.res;

import java.io.File;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.DependencyResource;
import org.jvnet.higherjaxb.mojo.ResourceEntry;
import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

public class RunResMojo extends RunHigherjaxbMojo {

	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo mojo) {
		super.configureMojo(mojo);
		
		mojo.setCatalog(new File(getBaseDir(),"src/main/jaxb/catalog.cat"));
		mojo.setExtension(true);
		final ResourceEntry purchaseorder_xsd = new ResourceEntry();
		final DependencyResource purchaseorder_xsd_dependencyResource = new DependencyResource();
		purchaseorder_xsd.setDependencyResource(purchaseorder_xsd_dependencyResource);
		purchaseorder_xsd.getDependencyResource().setGroupId("org.jvnet.higherjaxb.mojo");
		purchaseorder_xsd.getDependencyResource().setArtifactId("hisrc-higherjaxb-maven-plugin-tests-po");
		purchaseorder_xsd.getDependencyResource().setResource("purchaseorder.xsd");
		mojo.setSchemas(new ResourceEntry[]{
				purchaseorder_xsd
				
		});
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
