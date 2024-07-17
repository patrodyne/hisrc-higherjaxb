package org.jvnet.higherjaxb.mojo.resolver;

/**
 * An enum representing possible values of the resolve property
 */
public enum ResolveType
{
	UNKNOWN("unknown"),
	STRICT("strict"),		// CatalogFeatures.RESOLVE_STRICT
	CONTINUE("continue"),	// CatalogFeatures.RESOLVE_CONTINUE
	IGNORE("ignore");		// CatalogFeatures.RESOLVE_IGNORE

	// Represents the literal resolution type.
	final String literal;

	// Construct with the literal resolution type.
	ResolveType(String literal)
	{
		this.literal = literal;
	}

	/**
	 * Get a ResolveType enum instance for the given literal value.
	 * 
	 * @param type A ResolveType literal value.
	 * 
	 * @return A matching ResolveType or null.
	 */
	public static ResolveType getType(String type)
	{
		for (ResolveType resolveType : ResolveType.values())
		{
			if (resolveType.isType(type))
				return resolveType;
		}
		return null;
	}

	/**
	 * Does this enum instance's literal type equal the given type.
	 * 
	 * @param type A ResolveType literal value.
	 * 
	 * @return True when the given type matches this enum's literal value;
	 *         otherwise, false.
	 */
	public boolean isType(String type)
	{
		return literal.equals(type);
	}
}
