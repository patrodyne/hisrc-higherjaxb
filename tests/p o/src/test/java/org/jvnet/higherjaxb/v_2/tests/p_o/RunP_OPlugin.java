package org.jvnet.higherjaxb.v_2.tests.p_o;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

public class RunP_OPlugin extends RunHigherjaxbMojo {

	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo mojo) {
		super.configureMojo(mojo);
//		mojo.setExtension(true);
//		mojo.setForceRegenerate(true);
	}
}
