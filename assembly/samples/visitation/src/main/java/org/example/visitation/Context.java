package org.example.visitation;

import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * JAXB context for {@link org.example.visitation.ObjectFactory}
 */
public class Context
{
	public static final in.mynet.mmdslservices.ObjectFactory MMD =
		new in.mynet.mmdslservices.ObjectFactory();
	public static final visitation.mynet.in.ObjectFactory VIS =
		new visitation.mynet.in.ObjectFactory();

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
			setJaxbContext(JAXBContext.newInstance(MMD.getClass(), VIS.getClass()));
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
			// JAXB 2.3
			getMarshaller().setProperty("com.sun.xml.bind.namespacePrefixMapper", NPM);
			// JAXB 4+
			// getMarshaller().setProperty("org.glassfish.jaxb.namespacePrefixMapper", NPM);
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

    public static final NamespacePrefixMapper NPM = new NamespacePrefixMapper()
    {
    	private final Map<String, String> namespaceMap = new HashMap<>();
    	public Map<String, String> getNamespaceMap()
    	{
    		if ( namespaceMap.isEmpty() )
    		{
    			namespaceMap.put("http://mynet.in/MMDSLServices", "");
    			namespaceMap.put("http://in.mynet.visitation", "vis");
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
