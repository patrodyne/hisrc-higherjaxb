package org.jvnet.higherjaxb.mojo.net;

import static java.lang.String.format;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;

import org.apache.maven.plugin.logging.Log;

/**
 * A {@link File} {@link URI} last modified resolver.
 */
public class FileURILastModifiedResolver
	extends AbstractSchemeAwareURILastModifiedResolver
{
	public static final String SCHEME = "file";

	public FileURILastModifiedResolver(Log logger)
	{
		super(SCHEME, logger);
	}

	@Override
	protected Long getLastModifiedForScheme(final URI uri)
	{
		long lastModified = 0;
		try
		{
			final File file = new File(uri);
			if ( file.exists() )
			{
				lastModified = file.lastModified();
				if ( lastModified != 0 )
				{
					getLogger().debug(MessageFormat.format(
						"Last modification timestamp of the file URI [{0}] is [{1,date,yyyy-MM-dd HH:mm:ss.SSS}].", uri,
						lastModified));
				}
				else
				{
					getLogger().error(
						format("Could not retrieve the last modification of the file [%s] .", file.getAbsolutePath()));
				}
			}
			else
				getLogger().error(format("File [%s] does not exist.", file.getAbsolutePath()));
		}
		catch (Exception ex)
		{
			getLogger().error(format("Could not retrieve the last modification of the URI [%s] .", uri), ex);
		}
		
		if ( lastModified == 0 )
			getLogger().warn(format("Last modification of the URI [%s] is not known.", uri));
		
		return lastModified;
	}
}
