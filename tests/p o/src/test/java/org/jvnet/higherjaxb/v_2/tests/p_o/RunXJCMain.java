package org.jvnet.higherjaxb.v_2.tests.p_o;

import com.sun.tools.xjc.Driver;

public class RunXJCMain
{
	public static void main(String args[]) throws Exception
	{
		Driver.main(new String[]
		{
		 	"src/main/resources/purchaseorder.xsd",
			"-b", "src/main/resources/binding.xjb",
			"-d", "target/generated-sources/xjc"
		});
	}
}
