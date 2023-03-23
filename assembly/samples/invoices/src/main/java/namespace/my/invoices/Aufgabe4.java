package namespace.my.invoices;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import jakarta.xml.bind.Unmarshaller;

/**
 * Example of unmarshalling an InvoiceList.
 */
public class Aufgabe4
{
	public static final String SAMPLE_INVOICE_FILE = "src/test/samples/Invoices01-1.xml";
	
	private static Logger logger = LoggerFactory.getLogger(Aufgabe4.class);
	public static Logger getLogger() { return logger; }
	
	// Represents the unmarshalled InvoiceList.
	private InvoiceList invoices = null;
	public InvoiceList getInvoices() { return invoices; }
	public void setInvoices(InvoiceList invoices) { this.invoices = invoices; }

	/**
	 * Construct with path to an InvoiceList XML file.
	 * 
	 * @param path an InvoiceList XML file path.
	 * 
	 * @throws JAXBException When the XML file cannot be unmarshalled.
	 */
	public Aufgabe4(String path) throws JAXBException
	{
        File f = new File(path);
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InvoiceList invoiceList = (InvoiceList) JAXBIntrospector.getValue( unmarshaller.unmarshal(f));
        setInvoices(invoiceList);
	}

	/**
	 * Command Line Invocation.
	 * @param args CLI argument(s)
	 */
	public static void main(String[] args) throws Exception
	{
		String path = args.length > 0 ? args[0] : SAMPLE_INVOICE_FILE;
		Aufgabe4 aufgabe4 = new Aufgabe4(path);
		for ( Invoice invoice : aufgabe4.getInvoices().getInvoice() )
			println("Invoice: " + invoice.getInvoiceIdentifier() + "\n\n" + invoice + "\n");
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
