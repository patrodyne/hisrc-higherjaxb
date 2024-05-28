package org.example.checkbook;

import static jakarta.xml.bind.JAXBContext.newInstance;
import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * Checkbook JAXB Context methods.
 */
public class CBContext
{
	public CBContext()
	{
		this(null);
	}
	
	public CBContext(JAXBContext jaxbContext)
	{
		setJaxbContext(jaxbContext);
	}
	
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(newInstance(Checkbook.class, CheckbookBalance.class));
		return jaxbContext;
	}
	public void setJaxbContext(JAXBContext jaxbContext)
	{
		this.jaxbContext = jaxbContext;
	}
	
	private Unmarshaller unmarshaller = null;
	public Unmarshaller getUnmarshaller() throws JAXBException
	{
		if ( unmarshaller == null )
			setUnmarshaller(getJaxbContext().createUnmarshaller());
		return unmarshaller;
	}
	public void setUnmarshaller(Unmarshaller unmarshaller)
	{
		this.unmarshaller = unmarshaller;
	}
	
	private Marshaller marshaller = null;
	public Marshaller getMarshaller() throws JAXBException
	{
		if ( marshaller == null )
		{
			marshaller = getJaxbContext().createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
		}
		return marshaller;
	}
	public void setMarshaller(Marshaller marshaller)
	{
		this.marshaller = marshaller;
	}
}
