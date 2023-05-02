package org.jvnet.higherjaxb.mojo.v30;

import org.apache.maven.plugin.logging.Log;
import org.xml.sax.SAXParseException;

import com.sun.tools.xjc.ErrorReceiver;

public class LoggingErrorReceiver extends ErrorReceiver
{
	private Log logger;
	public Log getLogger() { return logger; }
	public void setLogger(Log logger) { this.logger = logger; }

	private boolean verbose = false;
    public boolean isVerbose() { return verbose; }
	public void setVerbose(boolean verbose) { this.verbose = verbose; }
	
	private String messagePrefix = "ERROR";
	public String getMessagePrefix() { return messagePrefix; }
	public void setMessagePrefix(String messagePrefix) { this.messagePrefix = messagePrefix; }

	public LoggingErrorReceiver(String messagePrefix, Log logger, boolean verbose)
	{
		setMessagePrefix(messagePrefix);
		setLogger(logger);
		setVerbose(verbose);
	}

	public void warning(SAXParseException saxex)
	{
		if (isVerbose())
			getLogger().warn(getMessage(saxex), saxex);
		else
		{
			if ( saxex.getMessage().contains("experimental") )
				getLogger().warn("Current configuration is experimental!");
			else
			{
				getLogger().warn(saxex.getMessage());
				getLogger().warn(getMessage(saxex));
			}
		}
	}

	public void error(SAXParseException saxex)
	{
		getLogger().error(getMessage(saxex), saxex);
	}

	public void fatalError(SAXParseException saxex)
	{
		getLogger().error(getMessage(saxex), saxex);
	}

	public void info(SAXParseException saxex)
	{
		if (isVerbose())
			getLogger().info(getMessage(saxex));
	}

	private String getMessage(SAXParseException ex)
	{
		final int row = ex.getLineNumber();
		final int col = ex.getColumnNumber();
		final String sys = ex.getSystemId();
		final String pub = ex.getPublicId();

		return getMessagePrefix() + " Location [" + (sys != null ? " " + sys : "")
			+ (pub != null ? " " + pub : "")
			+ (row > 0 ? "{" + row + (col > 0 ? "," + col : "") + "}" : "")
			+ " ].";
	}
}
