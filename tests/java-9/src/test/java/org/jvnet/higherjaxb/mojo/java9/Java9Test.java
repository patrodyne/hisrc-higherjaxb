//Copyright (c) 2017 by Disy Informationssysteme GmbH
package org.jvnet.higherjaxb.mojo.java9;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

// NOT_PUBLISHED
public class Java9Test
{
	@Test
	public void packageBindingRespected()
		throws Exception
	{
		Object o = Class.forName("with_pack.SimpleClassWithPackage").getDeclaredConstructor().newInstance();
		assertNotNull(o);
	}

	@Test
	public void classNameBindingRespected()
		throws Exception
	{
		Object o = Class.forName("class_name.SimpleClassWithRightName").getDeclaredConstructor().newInstance();
		assertNotNull(o);
	}
}
