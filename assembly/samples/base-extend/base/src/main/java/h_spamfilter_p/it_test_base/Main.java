package h_spamfilter_p.it_test_base;

import java.io.File;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Example of unmarshalling a RequestBase
 */
public class Main
{
	public static final String SAMPLE_FILE = "src/test/samples/RequestBase.xml";
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }
	
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

	// Represents the unmarshalled RequestBase.
	private RequestBase requestBase = null;
	public RequestBase getRequestBase() { return requestBase; }
	public void setRequestBase(RequestBase requestBase) { this.requestBase = requestBase; }

	/**
	 * Command Line Invocation.
	 * @param args CLI argument(s)
	 */
	public static void main(String[] args) throws Exception
	{
		String path = args.length > 0 ? args[0] : SAMPLE_FILE;
		Main main = new Main();
		main.execute(path);
	}
	
	private void execute(String path) throws JAXBException
	{
		// RequestBase is not an XmlRootElement, it is an XmlType;
		// thus, we need to unmarshal a Source with the declared type.
		File xmlFile = new File(path);
		StreamSource source = new StreamSource(xmlFile);
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
		
        setRequestBase(requestBase);
        
		println("RequestBase:\n\n" + getRequestBase() + "\n");
	}

	/**
	 * Print a text string.
	 * @param text The text to be printed.
	 */
    public static void println(String text)
	{
		getLogger().info(text);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
