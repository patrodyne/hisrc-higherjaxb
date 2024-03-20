package my.company;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.IOException;
import java.io.Reader;

import org.jvnet.basicjaxb.config.LocatorInputFactory;
import org.jvnet.higherjaxb.w3c.xmlschema.v1_0.Schema;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * XmlSchema JAXB Context methods.
 */
public class XsJaxbContext
{
	public XsJaxbContext()
	{
		this(null);
	}
	
	public XsJaxbContext(JAXBContext jaxbContext)
	{
		setJaxbContext(jaxbContext);
	}
	
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(JAXBContext.newInstance(Schema.class));
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
			Marshaller marshaller = getJaxbContext().createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
		}
		return marshaller;
	}
	public void setMarshaller(Marshaller marshaller)
	{
		this.marshaller = marshaller;
	}
	
	public Schema execute(String path)
		throws JAXBException, IOException
	{
		Schema xsSchema = null;
		try ( Reader reader = LocatorInputFactory.createReader(path) )
		{
			Object root = getUnmarshaller().unmarshal(reader);
			if ( root instanceof JAXBElement )
			{
				@SuppressWarnings("unchecked")
				JAXBElement<Schema> xsSchemaJE = (JAXBElement<Schema>) root;
		        xsSchema = xsSchemaJE.getValue();
			}
			else if ( root instanceof Schema )
				xsSchema = (Schema) root;
		}
		return xsSchema;
	}
}
