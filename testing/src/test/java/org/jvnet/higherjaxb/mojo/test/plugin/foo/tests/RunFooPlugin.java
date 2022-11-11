package org.jvnet.higherjaxb.mojo.test.plugin.foo.tests;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

public class RunFooPlugin extends RunHigherjaxbMojo {

	@Override
	public File getSchemaDirectory() {
		return getDirectory("src/test/resources");
	}

	@Override
	public List<String> getArgs() {
		return Collections.singletonList("-Xfoo");
	}

}
