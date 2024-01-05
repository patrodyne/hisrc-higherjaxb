package org.jvnet.higherjaxb.mojo.testing;

import static java.lang.String.valueOf;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;

/**
 * Implement Maven's {@link Log} using an instance of SLF4J's {@link Logger}.
 */
public class SLF4JLogger implements Log
{
	private Logger logger;
	public Logger getLogger() { return logger; }
	public void setLogger(Logger logger) { this.logger = logger; }

	/**
	 * Construct with a SLF4J {@link Logger}.
	 * 
	 * @param logger The SLF4J {@link Logger}
	 */
	public SLF4JLogger(Logger logger)
	{
		setLogger(logger);
	}
	
	@Override
	public boolean isDebugEnabled()
	{
		return getLogger().isDebugEnabled();
	}

	@Override
	public void debug(CharSequence content)
	{
		getLogger().debug(valueOf(content));
	}

	@Override
	public void debug(CharSequence content, Throwable error)
	{
		getLogger().debug(valueOf(content), error);
	}

	@Override
	public void debug(Throwable error)
	{
		getLogger().debug(error.getMessage(), error);
	}

	@Override
	public boolean isInfoEnabled()
	{
		return getLogger().isDebugEnabled();
	}

	@Override
	public void info(CharSequence content)
	{
		getLogger().info(valueOf(content));
	}

	@Override
	public void info(CharSequence content, Throwable error)
	{
		getLogger().info(valueOf(content), error);
	}

	@Override
	public void info(Throwable error)
	{
		getLogger().info(error.getMessage(), error);
	}

	@Override
	public boolean isWarnEnabled()
	{
		return getLogger().isWarnEnabled();
	}

	@Override
	public void warn(CharSequence content)
	{
		getLogger().warn(valueOf(content));
	}

	@Override
	public void warn(CharSequence content, Throwable error)
	{
		getLogger().warn(valueOf(content), error);
	}

	@Override
	public void warn(Throwable error)
	{
		getLogger().warn(error.getMessage(), error);
	}

	@Override
	public boolean isErrorEnabled()
	{
		return getLogger().isErrorEnabled();
	}

	@Override
	public void error(CharSequence content)
	{
		getLogger().error(valueOf(content));
	}

	@Override
	public void error(CharSequence content, Throwable error)
	{
		getLogger().error(valueOf(content), error);
	}

	@Override
	public void error(Throwable error)
	{
		getLogger().error(error.getMessage(), error);
	}
}
