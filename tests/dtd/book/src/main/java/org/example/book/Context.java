package org.example.book;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.catalog.CatalogFeatures.Feature.FILES;
import static org.jvnet.basicjaxb.config.LocatorInputFactory.resolveLocator;

import java.io.File;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.patrodyne.jvnet.basicjaxb.validation.SchemaOutputDomResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * JAXB context for {@link org.example.book.ObjectFactory}
 */
public class Context
{
	private static Logger logger = LoggerFactory.getLogger(Context.class);
	public static Logger getLogger() { return logger; }
	
	// Represents OASIS DTD Entity Resolution XML Catalog V1.0
	public static final String CATALOG_NAME = "classpath:/catalog.xml";
	
	/**
	 * Default Constructor
	 */
	public Context()
	{
		this(null);
	}
	
	/**
	 * Construct with an existing {@link JAXBContext} instance.
	 * 
	 * @param jaxbContext An existing {@link JAXBContext} instance.
	 */
	public Context(JAXBContext jaxbContext)
	{
		setJaxbContext(jaxbContext);
		// accessExternalDTD: [ file | http | all ]
		System.setProperty("javax.xml.accessExternalDTD", "file");
	}

	// Represents a {@link DocumentBuilderFactory} with support for catalogs.
	private DocumentBuilderFactory documentBuilderFactory = null;
	public DocumentBuilderFactory getDocumentBuilderFactory()
		throws JAXBException
	{
		if ( documentBuilderFactory == null )
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    dbf.setNamespaceAware(true);
			if ( getCatalogURI() != null )
				   dbf.setAttribute(FILES.getPropertyName(), getCatalogURI().toString());
			setDocumentBuilderFactory(dbf);
		}
		return documentBuilderFactory;
	}
	public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory)
	{
		this.documentBuilderFactory = documentBuilderFactory;
	}
	
	// Enable OASIS DTD Entity Resolution XML Catalog
	private URI catalogURI;
	public URI getCatalogURI()
		throws JAXBException
	{
		try
		{
			if ( catalogURI == null )
				setCatalogURI(resolveLocator(CATALOG_NAME, null));
			return catalogURI;
		}
		catch (Exception ex)
		{
			throw new JAXBException("getCatalogURI: ", ex);
		}
	}
	public void setCatalogURI(URI catalogURI)
	{
		this.catalogURI = catalogURI;
	}

	// JAXB Context Properties
	
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(JAXBContext.newInstance(ObjectFactory.class));
		return jaxbContext;
	}
	public void setJaxbContext(JAXBContext jaxbContext)
	{
		this.jaxbContext = jaxbContext;
	}

	private Unmarshaller unmarshaller = null;
	protected Unmarshaller getUnmarshaller() throws JAXBException
	{
		if ( unmarshaller == null )
			setUnmarshaller(getJaxbContext().createUnmarshaller());
		return unmarshaller;
	}
	protected void setUnmarshaller(Unmarshaller unmarshaller)
	{
		this.unmarshaller = unmarshaller;
	}

	private Marshaller marshaller = null;
	public Marshaller getMarshaller() throws JAXBException
	{
		if ( marshaller == null )
		{
			setMarshaller(getJaxbContext().createMarshaller());
			getMarshaller().setProperty(JAXB_FORMATTED_OUTPUT, true);
		}
		return marshaller;
	}
	public void setMarshaller(Marshaller marshaller)
	{
		this.marshaller = marshaller;
	}
	
	// JAXB Context Methods
	
	/**
	 * Use the current {@link Unmarshaller} to unmarshal the given {@link File}.
	 * 
	 * @param <T> The type of the declared class.
	 * @param xmlFileName The file name to be read for unmarshalling.
	 * 
	 * @return An instance representaion of the contents of the given {@link File}.
	 * 
	 * @throws JAXBException When the instance cannot be marshaled to a {@link File}.
	 */
	public <T> T unmarshal(String xmlFileName) throws JAXBException
	{
		File xmlFile = new File(xmlFileName);
		return unmarshal(xmlFile);
	}
	
	/**
	 * Use the current {@link Unmarshaller} to unmarshal the given {@link File}.
	 * 
	 * @param <T> The type of the declared class.
	 * @param xmlFile The {@link File} to be read for unmarshalling.
	 * 
	 * @return An instance representaion of the contents of the given {@link File}.
	 * 
	 * @throws JAXBException When the instance cannot be marshaled to a {@link File}.
	 */
	public <T> T unmarshal(File xmlFile) throws JAXBException
	{
		return unmarshal(xmlFile, null);
	}
	
	/**
	 * Use the current {@link Unmarshaller} to unmarshal the given {@link File}
	 * and {@link Class} declaration to a Java object.
	 * 
	 * @param <T> The type of the declared class.
	 * @param xmlFileName The file name to be read for unmarshalling.
	 * @param clazz The declared class type expected.
	 * 
	 * @return An instance representaion of the contents of the given {@link File}.
	 * 
	 * @throws JAXBException When the instance cannot be marshaled to a {@link File}.
	 */
	public <T> T unmarshal(String xmlFileName, Class<T> clazz) throws JAXBException
	{
		File xmlFile = new File(xmlFileName);
		return unmarshal(xmlFile, clazz);
	}
	
	/**
	 * Use the current {@link Unmarshaller} to unmarshal the given {@link File}
	 * and {@link Class} declaration to a Java object.
	 * 
	 * <p>This implementation uses a {@link DocumentBuilderFactory} with
	 * support for an OASIS catalog file.</p>
	 * 
	 * @param <T> The type of the declared class.
	 * @param xmlFile The {@link File} to be read for unmarshalling.
	 * @param clazz The declared class type expected.
	 * 
	 * @return An instance representaion of the contents of the given {@link File}.
	 * 
	 * @throws JAXBException When the instance cannot be marshaled to a {@link File}.
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal(File xmlFile, Class<T> clazz) throws JAXBException
	{
		try
		{
			DocumentBuilder db = getDocumentBuilderFactory().newDocumentBuilder();
			Document doc = db.parse(xmlFile);

			Object result = null;
			if ( clazz != null )
				result = getUnmarshaller().unmarshal(doc, clazz);
			else
				result = getUnmarshaller().unmarshal(doc);
			
			if ( result instanceof JAXBElement )
				return ((JAXBElement<T>) result).getValue();
			else
				return (T) result;
		}
		catch (Exception ex)
		{
			throw new JAXBException("unmarshal: "+xmlFile, ex);
		}
	}
	
	/**
     * Use the current {@link Marshaller} to marshal the given instance
     * to a {@link File} with the given name.
	 * 
     * @param instance The object to be marshaled.
	 * @param xmlFileName The name of the file to be created.
	 * 
	 * @throws JAXBException When the instance cannot be marshaled to a {@link File}.
	 */
	public void marshal(Object instance, String xmlFileName) throws JAXBException
    {
		File xmlFile = new File(xmlFileName);
    	try
    	{
	        if ( instance != null)
                getMarshaller().marshal(instance, xmlFile);
    	}
		catch (Exception ex)
		{
			throw new JAXBException("marshal: " + xmlFileName, ex);
		}
    }

    /**
     * Use the current {@link Marshaller} to marshal the given instance
     * to a {@link String}.
     * 
     * @param instance The object to be marshaled.
     * 
     * @return An XML {@link String} representation of the instance.
     * 
	 * @throws JAXBException When the instance cannot be marshaled to a {@link String}.
     */
	public String marshalToString(Object instance) throws JAXBException
    {
    	try
    	{
	        String xml = null;
	        if ( instance != null)
	        {
	            try ( StringWriter writer = new StringWriter() )
	            {
	                getMarshaller().marshal(instance, writer);
	                xml = writer.toString();
	            }
	        }
	        return xml;
    	}
		catch (Exception ex)
		{
			throw new JAXBException("marshalToString: "+instance, ex);
		}
    }

	/**
	 * Generate an XML Schema Validator from the DOM and wire the
	 * current {@link Unmarshaller} and {@link Marshaler} to use validation..
	 * 
	 * @throws JAXBException When the {@link Unmarshaller} and {@link Marshaler}
	 *                       cannot be configured for validation.
	 */
	public void generateXmlSchemaValidatorFromDom() throws JAXBException
	{
		try
		{
			if ( (getMarshaller() != null) && (getUnmarshaller() != null) )
			{
				// Generate a Schema Validator from given the JAXB context.
				SchemaOutputDomResolver sodr = new SchemaOutputDomResolver();
				getJaxbContext().generateSchema(sodr);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
	
				// Produce schema from the factory.
				Schema schema = schemaFactory.newSchema(sodr.getDomSource());
				
				// Configure Marshaller / unmarshaller to use validator.
				getMarshaller().setSchema(schema);
				getUnmarshaller().setSchema(schema);
			}
			else
				getLogger().error("Please create marshaller and unmarshaller!");
		}
		catch (Exception ex)
		{
			throw new JAXBException("generateXmlSchemaValidatorFromDom: ", ex);
		}
    }
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
