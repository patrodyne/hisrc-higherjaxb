package org.example.model;

import static java.lang.String.format;

import java.io.Serializable;
public class Version
    implements Serializable
{
    private static final long serialVersionUID = 20230701L;
    
	private String major;
	public String getMajor() { return major; }
	public void setMajor(String major) { this.major = major; }

	private String minor;
	public String getMinor() { return minor; }
	public void setMinor(String minor) { this.minor = minor; }
	
	/**
	 * Construct with properties
	 * @param major The major version number.
	 * @param minor The minor version number.
	 */
	public Version(String major, String minor)
	{
		setMajor(major);
		setMinor(minor);
	}
	
	/**
	 * Construct by parsing version.
	 * 
	 * @param version in {@code major.minor} format.
	 */
	public Version(String version)
	{
		if ( version != null )
		{
			String[] parts = version.split("\\.");
			setMajor(parts[0]);
			if ( parts.length > 1 )
				setMinor(parts[1]);
		}
	}
	
	/**
	 * Default constructor
	 */
	public Version()
	{
		// default properties
	}
	
	public String toString()
	{
		return format("%s.%s",
			((getMajor() != null) ? getMajor() : "0"),
			((getMinor() != null) ? getMinor() : "0")
		);
	}
}
