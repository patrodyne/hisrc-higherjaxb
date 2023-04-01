package h_spamfilter_p.it_test_base;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static jakarta.xml.bind.Marshaller.JAXB_FRAGMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.jvnet.basicjaxb.test.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file.
 */
public class RequestBaseTest extends AbstractSamplesTest
{
	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() { return jaxbContext; }
	public void setJaxbContext(JAXBContext jaxbContext) { this.jaxbContext = jaxbContext; }

	private Unmarshaller unmarshaller;
	public Unmarshaller getUnmarshaller() { return unmarshaller; }
	public void setUnmarshaller(Unmarshaller unmarshaller) { this.unmarshaller = unmarshaller; }
	
	private Marshaller marshaller;
	public Marshaller getMarshaller() { return marshaller; }
	public void setMarshaller(Marshaller marshaller) { this.marshaller = marshaller; }

	/**
	 * Configure the JAXB context path in AbstractSamplesTest
	 * to include all classes from two packages.
	 */
	@Override
	protected String getContextPath()
	{
		return OBJECT_FACTORY.getClass().getPackageName();
	}
	
	@BeforeEach
	public void beforeEach() throws JAXBException
	{
		setJaxbContext(createContext());
		setUnmarshaller(getJaxbContext().createUnmarshaller());
		setMarshaller(getJaxbContext().createMarshaller());
		getMarshaller().setProperty(JAXB_FORMATTED_OUTPUT, true);
		getMarshaller().setProperty(JAXB_FRAGMENT, false);

		// Enable XML Schema Validation
		//generateXmlSchemaValidatorFromDom();
	}

	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		// RequestBase is not an XmlRootElement, it is an XmlType;
		// thus, we need to unmarshal a Source with a declared type.
		StreamSource source = new StreamSource(sample);
		Object root = getUnmarshaller().unmarshal(source, RequestBase.class);
		
		RequestBase requestBase = null;
		if ( root instanceof JAXBElement )
		{
			@SuppressWarnings("unchecked")
			JAXBElement<RequestBase> requestBaseJE = (JAXBElement<RequestBase>) root;
			requestBase = requestBaseJE.getValue();
		}
		else if ( root instanceof RequestBase )
			requestBase = (RequestBase) root;
		else
			fail("Object is not instance of RequestBase: " + root);

		String requestBaseXml = marshal(getMarshaller(), root);
		getLogger().debug("RequestBase:\n\n" + requestBaseXml);

		assertEquals("OK", requestBase.getErrorMSG());
	}

	// Marshall a JAXB object into a String for logging, etc.
	private String marshal(Marshaller marshaller, Object jaxbObject)
		throws JAXBException, IOException
	{
		String xml;
		try ( StringWriter sw = new StringWriter() )
		{
			marshaller.marshal(jaxbObject, sw);
			xml = sw.toString();
		}
		return xml;
	}
}
