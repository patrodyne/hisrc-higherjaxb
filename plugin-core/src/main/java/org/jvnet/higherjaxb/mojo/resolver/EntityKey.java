package org.jvnet.higherjaxb.mojo.resolver;

/**
 * Represent an XML Entity public and system id.
 */
public class EntityKey
{
	private String publicId;
	private String systemId;

	/**
	 * Gets the value of the publicId property.
	 * 
	 * @return Possible object is {@link String }
	 */
	public String getPublicId() 
	{
		return publicId;
	}
	/**
	 * Sets the value of the publicId property.
	 * 
	 * @param value Allowed object is {@link String }
	 *	   
	 */
	public void setPublicId(String value)
	{
		this.publicId = value;
	}

	/**
	 * Gets the value of the systemId property.
	 * 
	 * @return Possible object is {@link String }
	 *	   
	 */
	public String getSystemId()
	{
		return systemId;
	}
	/**
	 * Sets the value of the systemId property.
	 * 
	 * @param value Allowed object is {@link String }
	 *	   
	 */
	public void setSystemId(String value)
	{
		this.systemId = value;
	}

	/**
	 * Construct with a publicId and a systemId.
	 * 
	 * @param publicId The XML entity public identifier.
	 * @param systemId The XML entity system identifier
	 */
	public EntityKey(String publicId, String systemId)
	{
		setPublicId(publicId);
		setSystemId(systemId);
	}
	
	@Override
	public int hashCode()
	{
		int currentHashCode = 1;

		{
			currentHashCode = (currentHashCode* 31);
			String thePublicId = this.getPublicId();
			if (this.publicId!= null)
				currentHashCode += thePublicId.hashCode();
		}

		{
			currentHashCode = (currentHashCode* 31);
			String theSystemId = this.getSystemId();
			if (this.systemId!= null)
				currentHashCode += theSystemId.hashCode();
		}

		return currentHashCode;
	}

	@Override
	public boolean equals(Object object)
	{
		if ((object == null)||(this.getClass()!= object.getClass()))
			return false;
		
		if (this == object)
			return true;
		
		final EntityKey that = ((EntityKey) object);

		{
			String lhsPublicId = this.getPublicId();
			String rhsPublicId = that.getPublicId();
			if (this.publicId!= null)
			{
				if (that.publicId!= null)
				{
					if (!lhsPublicId.equals(rhsPublicId))
						return false;
				}
				else
					return false;
			}
			else
			{
				if (that.publicId!= null)
					return false;
			}
		}

		{
			String lhsSystemId = this.getSystemId();
			String rhsSystemId = that.getSystemId();
			if (this.systemId!= null)
			{
				if (that.systemId!= null)
				{
					if (!lhsSystemId.equals(rhsSystemId))
						return false;
				}
				else
					return false;
			}
			else
			{
				if (that.systemId!= null)
					return false;
			}
		}
		return true;
	}

	protected void toStringFields(StringBuilder stringBuilder)
	{
		{
			String thePublicId = this.getPublicId();
			stringBuilder.append(thePublicId);
		}

		{
			String theSystemId = this.getSystemId();
			stringBuilder.append(", ");
			stringBuilder.append(theSystemId);
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.getClass().getSimpleName());
		stringBuilder.append('@');
		stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
		stringBuilder.append("[");
		toStringFields(stringBuilder);
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
}
