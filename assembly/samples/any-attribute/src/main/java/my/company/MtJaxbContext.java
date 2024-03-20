package my.company;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.File;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.jvnet.higherjaxb.w3c.xmlschema.v1_0.Schema;
import org.jvnet.higherjaxb.w3c.xmlschema.v1_0.TopLevelSimpleType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import my.company.v2.MyTypeWrapper;

/**
 * MyType JAXB Context methods.
 */
public class MtJaxbContext
{
	public static final QName MY_COMPANY_REFERENCE_QNAME = new QName("http://www.mycompany.no/xsd", "reference");
	public static final String MY_TYPE_WRAPPER_XSD = "classpath:MyTypeWrapper.xsd";

	public MtJaxbContext()
	{
		this(null);
	}
	
	public MtJaxbContext(JAXBContext jaxbContext)
	{
		setJaxbContext(jaxbContext);
	}
	
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(JAXBContext.newInstance(MyTypeWrapper.class));
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
	
	public MyTypeWrapper execute(String path) throws JAXBException
	{
		MyTypeWrapper myTypeWrapper = null;
		// MyTypeWrapper is not an XmlRootElement, it is an XmlType;
		// thus, we need to unmarshal a Source with the declared type.
		File xmlFile = new File(path);
		StreamSource source = new StreamSource(xmlFile);
		Object root = getUnmarshaller().unmarshal(source, MyTypeWrapper.class);
		
		if ( root instanceof JAXBElement )
		{
			@SuppressWarnings("unchecked")
			JAXBElement<MyTypeWrapper> myTypeWrapperJE = (JAXBElement<MyTypeWrapper>) root;
	        myTypeWrapper = myTypeWrapperJE.getValue();
		}
		else if ( root instanceof MyTypeWrapper )
			myTypeWrapper = (MyTypeWrapper) root;
        
		return myTypeWrapper;
	}
	
	public String findMyCompanyReference(Schema xsSchema)
	{
		String myCompanyReference = null;
		Optional<TopLevelSimpleType> xsMyType = xsSchema.getSimpleType().stream()
			.filter(p -> "MyType".equals(p.getName())).findFirst();
		if ( xsMyType.isPresent() )
		{
			// println("MyType: " + xsMyType.get());
			myCompanyReference = xsMyType.get().getOtherAttributes()
				.get(MY_COMPANY_REFERENCE_QNAME);
		}
		return myCompanyReference;
	}
	
	public Boolean validateMyCompanyReference(String myCompanyReference, String value)
	{
		Boolean valid = null;
		switch ( myCompanyReference )
		{
			case "https://example.com/valid_values.xml":
				if ( "SomeValidValue".equals(value) )
					valid = true;
				else
					valid = false;
				break;
			default:
				valid = null;
				break;
		}
		return valid;
	}
}
