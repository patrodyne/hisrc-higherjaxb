package org.jvnet.higherjaxb.mojo.v40;

import org.apache.maven.plugin.logging.Log;
import org.xml.sax.SAXParseException;

import com.sun.tools.xjc.ErrorReceiver;

public class LoggingErrorReceiver extends ErrorReceiver
{
	private final Log log;
	private final boolean verbose;
	private final String messagePrefix;

	public LoggingErrorReceiver(String messagePrefix, Log log, boolean verbose)
	{
		this.log = log;
		this.verbose = verbose;
		this.messagePrefix = messagePrefix;
	}

	public void warning(SAXParseException saxex)
	{
		if (verbose)
			log.warn(getMessage(saxex), saxex);
		else
		{
			if ( saxex.getMessage().contains("experimental") )
				log.warn("Current configuration is experimental!");
			else
			{
				log.warn(saxex.getMessage());
				log.warn(getMessage(saxex));
			}
		}
	}

	public void error(SAXParseException saxex)
	{
		log.error(getMessage(saxex), saxex);
	}

	public void fatalError(SAXParseException saxex)
	{
		log.error(getMessage(saxex), saxex);
	}

	public void info(SAXParseException saxex)
	{
		if (verbose)
			log.info(getMessage(saxex));
	}

	private String getMessage(SAXParseException ex)
	{
		final int row = ex.getLineNumber();
		final int col = ex.getColumnNumber();
		final String sys = ex.getSystemId();
		final String pub = ex.getPublicId();

		return messagePrefix + " Location [" + (sys != null ? " " + sys : "")
			+ (pub != null ? " " + pub : "")
			+ (row > 0 ? "{" + row + (col > 0 ? "," + col : "") + "}" : "")
			+ " ].";
	}
}
