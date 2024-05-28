package org.example.book;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;

/**
 * Example of unmarshalling and marshaling a DTD generated Book.
 */
public class Main
{
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }

	public static final String SAMPLE_FILE = "src/test/samples/Book01.xml";
	
	// Represents the Book JAXBContext for this instance.
	public static final Context CONTEXT = new Context(); 
	
    // Represents the unmarshalled Book.
    private Book book = null;
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
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
	 * @throws IOException When an I/O actions fails.
	 * @throws SAXException When XmlSchemaValidator cannot be generated from DOM.
	 * @throws URISyntaxException When the catalog URI cannot be parsed.
	 * @throws XMLStreamException 
	 * @throws IllegalArgumentException 
	 */
	public void execute(String xmlFileName) throws JAXBException
	{
		// Enable JAXB schema validation on XML instances.
		CONTEXT.generateXmlSchemaValidatorFromDom();

		// JAXB: unmarshal XML file by path name.
		setBook(CONTEXT.unmarshal(xmlFileName, Book.class));
		if ( getBook() != null )
		{
			if ( getBook().getTitlepage() != null )
				getLogger().info("Book: {}\n\n{}\n", getBook().getTitlepage().getTitle(), getBook());
			else
				getLogger().warn("Book.Titlepage not unmarshaled!");
		}
		else
			getLogger().warn("Book not unmarshaled!");

		// JAXB: marshal Book.
		String xmlBook = CONTEXT.marshalToString(book);
		getLogger().info("Book:\n\n{}", xmlBook);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
