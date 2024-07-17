package org.jvnet.higherjaxb.mojo.resolver.tools;

import java.io.IOException;
import java.io.StringReader;

public class ResetableStringReader extends StringReader
{
	public ResetableStringReader(String s)
	{
		super(s);
	}
	
	@Override
	public void close()
	{
		try
		{
			reset();
		}
		catch (IOException e)
		{
			// Ignore
		}
	}
}
