package org.example.nothing;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static org.jvnet.basicjaxb.dom.DOMUtils.logNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jakarta.xml.bind.Binder;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

/**
 * Example of unmarshalling and marshaling a Nothing.
 */
public class Main
{
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }

	// Represents the JAXBContext for this instance.
	public static final Context CONTEXT = new Context();

	// Represents a sample XML file name.
	public static final String SAMPLE_FILE = "src/test/samples/nothings01.xml";

	/**
	 * Command Line Invocation.
	 *
	 * @param args CLI argument(s)
	 */
	public static void main(String[] args)
	{
		String xmlFileName = args.length > 0 ? args[0] : SAMPLE_FILE;
		try
		{
			Main main = new Main();
			main.execute(xmlFileName);
		}
		catch (JAXBException ex)
		{
			getLogger().error("Aborting " + Main.class.getName(), ex);
		}
	}

	/**
	 * Execute the JAXB actions.
	 *
	 * @param xmlFileName The path to an XML data file.
	 *
	 * @throws JAXBException When a JAXB action fails.
	 */
	public void execute(String xmlFileName) throws JAXBException
	{
		// JAXB: parse an XML file by path name to a DOM document.
		Document dom = CONTEXT.parse(xmlFileName);

		if ( getLogger().isTraceEnabled() )
			logNode(getLogger(), "Main:", dom);

		Binder<Node> binder = CONTEXT.getJaxbContext().createBinder();
		JAXBElement<Nothings> jeNothings = binder.unmarshal(dom, Nothings.class);
		Nothings nothings = jeNothings.getValue();
		for ( Nothing nothing : nothings.getNothing() )
		{
			Element element = (Element) binder.getXMLNode(nothing);
			for ( int index=0; index < element.getChildNodes().getLength(); ++index )
			{
				Node child = element.getChildNodes().item(index);
				if ( child instanceof Element )
				{
					Element childElement = (Element) child;
					String nil = childElement.getAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil");
					if ( "true".equals(nil) )
						getLogger().info("{}: {}", nothing.id, childElement.getLocalName());
				}
			}
			element = null;
		}

		// JAXB: unmarshal XML file by path name.
		nothings = CONTEXT.unmarshal(dom, Nothings.class);
		if ( nothings != null )
			getLogger().info("Nothings: {}", nothings);
		else
			getLogger().warn("Nothings not unmarshaled!");

		// JAXB: marshal Nothings.
		String xmlNothings = CONTEXT.marshalToString(nothings);
		getLogger().info("Nothings:\n\n{}", xmlNothings);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
