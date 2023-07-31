package org.example.notype;

import static jakarta.xml.bind.JAXBContext.newInstance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

import org.example.model.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of unmarshalling a ParentElement
 */
public class Main
{
	public static final String SAMPLE_PO_FILE = "src/test/samples/ParentElement.xml";
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }
	
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(newInstance(ObjectFactory.class, Version.class));
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

	// Represents the unmarshalled ParentElement.
	private ParentElement parentElement = null;
	public ParentElement getParentElement() { return parentElement; }
	public void setParentElement(ParentElement parentElement) { this.parentElement = parentElement; }

	/**
	 * Command Line Invocation.
	 * @param args CLI argument(s)
	 */
	public static void main(String[] args) throws Exception
	{
		String path = args.length > 0 ? args[0] : SAMPLE_PO_FILE;
		Main main = new Main();
		main.execute(path);
	}
	
	private void execute(String path) throws JAXBException
	{
		File xmlFile = new File(path);
        setParentElement((ParentElement) getUnmarshaller().unmarshal(xmlFile));
		println("ParentElement Version: " + getParentElement().getVersion());
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
