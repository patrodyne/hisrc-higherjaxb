package org.example.nothing;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * JAXB context for {@link org.example.nothing.ObjectFactory}
 */
public class Context
{
	public static final ObjectFactory OF =	new ObjectFactory();

	private static Logger logger = LoggerFactory.getLogger(Context.class);
	public static Logger getLogger() { return logger; }

    // Represents a {@link DocumentBuilderFactory}.
    private DocumentBuilderFactory documentBuilderFactory = null;
    public DocumentBuilderFactory getDocumentBuilderFactory()
    {
        if ( documentBuilderFactory == null )
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            setDocumentBuilderFactory(dbf);
        }
        return documentBuilderFactory;
    }
    public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory)
    {
        this.documentBuilderFactory = documentBuilderFactory;
    }

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

	// JAXB Context Properties

	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(JAXBContext.newInstance(OF.getClass()));
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
			getMarshaller().setProperty("org.glassfish.jaxb.namespacePrefixMapper", NPM);
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
	public <T> T unmarshal(File xmlFile, Class<T> clazz) throws JAXBException
	{
		try
		{
			Document doc = parse(xmlFile);
			return unmarshal(doc, clazz);
		}
		catch (Exception ex)
		{
			throw new JAXBException("unmarshal: "+xmlFile, ex);
		}
	}

	/**
	 * Use the current {@link Unmarshaller} to unmarshal the given {@link File}
	 * and {@link Class} declaration to a Java object.
	 *
	 * <p>This implementation uses a {@link DocumentBuilderFactory} with
	 * support for an OASIS catalog file.</p>
	 *
	 * @param <T> The type of the declared class.
	 * @param doc The DOM Document object.
	 * @param clazz The declared class type expected.
	 *
	 * @return An instance representaion of the contents of the given {@link File}.
	 *
	 * @throws JAXBException When the instance cannot be marshaled to a {@link File}.
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal(Document doc, Class<T> clazz) throws JAXBException
	{
		try
		{
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
			throw new JAXBException("unmarshal: "+doc.getNodeName(), ex);
		}
	}

	/**
	 * Parse the content of the given file as an XML document
     * and return a new DOM {@link Document} object.
     *
	 * @param xmlFileName The file name to be read for unmarshalling.
	 *
	 * @return A new DOM Document object.
	 *
	 * @throws JAXBException If any I/O or parse error occurs.
	 */
	public Document parse(String xmlFileName) throws JAXBException
	{
		return parse(new File(xmlFileName));
	}

	/**
	 * Parse the content of the given file as an XML document
     * and return a new DOM {@link Document} object.
     *
	 * @param xmlFile The {@link File} to be read for unmarshalling.
	 *
	 * @return A new DOM Document object.
	 *
	 * @throws JAXBException If any I/O or parse error occurs.
	 */
	public Document parse(File xmlFile) throws JAXBException
	{
		try
		{
			DocumentBuilder db = getDocumentBuilderFactory().newDocumentBuilder();
			return db.parse(xmlFile);
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

    public static final NamespacePrefixMapper NPM = new NamespacePrefixMapper()
    {
    	private final Map<String, String> namespaceMap = new HashMap<>();
    	public Map<String, String> getNamespaceMap()
    	{
    		if ( namespaceMap.isEmpty() )
    		{
    			namespaceMap.put("urn:example.org:nothing", "no");
    		}
    		return namespaceMap;
    	}

        @Override
    	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
        {
    		return getNamespaceMap().getOrDefault(namespaceUri, suggestion);
    	}
    };

}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
