package org.jvnet.higherjaxb.mojo;

import static java.lang.String.format;

/**
 * Mojo options configuration used to create the System options.
 */
public class SystemOptionsConfiguration
{
	private final String accessExternalSchema;
	public String getAccessExternalSchema() { return accessExternalSchema; }

	private final String accessExternalDTD;
	public String getAccessExternalDTD() { return accessExternalDTD; }
	
	private final Boolean enableExternalEntityProcessing;
	public Boolean isEnableExternalEntityProcessing() { return enableExternalEntityProcessing; }
	
	public SystemOptionsConfiguration
	(
		String accessExternalSchema,
		String accessExternalDTD,
		boolean enableExternalEntityProcessing
	)
	{
		super();
		this.accessExternalSchema = accessExternalSchema;
		this.accessExternalDTD = accessExternalDTD;
		this.enableExternalEntityProcessing = enableExternalEntityProcessing;
	}

	@Override
	public String toString()
	{
		return format
		(
			"SystemOptionsConfiguration [\n" +
				"accessExternalSchema=%s\n " +
				"accessExternalDTD=%s\n " +
				"enableExternalEntityProcessing=%s" +
			"]",
			accessExternalSchema,
			accessExternalDTD,
			enableExternalEntityProcessing
		);

	}

}
