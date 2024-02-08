package com.workday.bsvo;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import com.workday.Root;

/**
 * Example of unmarshalling a PutAbsenceInputRequest
 */
public class Main
{
	public static final String SAMPLE_WD_FILE = "src/test/samples/workday-01.xml";
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }
	
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(JAXBContext.newInstance(com.workday.ObjectFactory.class));
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

	// Represents the unmarshalled PutAbsenceInputRequest.
	private PutAbsenceInputRequest putAbsenceInputRequest = null;
	public PutAbsenceInputRequest getPutAbsenceInputRequest() { return putAbsenceInputRequest; }
	public void setPutAbsenceInputRequest(PutAbsenceInputRequest putAbsenceInputRequest) { this.putAbsenceInputRequest = putAbsenceInputRequest; }

	/**
	 * Command Line Invocation.
	 * @param args CLI argument(s)
	 */
	public static void main(String[] args) throws Exception
	{
		String path = args.length > 0 ? args[0] : SAMPLE_WD_FILE;
		Main main = new Main();
		main.execute(path);
	}
	
	private void execute(String path) throws JAXBException
	{
		File xmlFile = new File(path);
		Root root = (Root) getUnmarshaller().unmarshal(xmlFile);
        setPutAbsenceInputRequest(root.getPutAbsenceInputRequest());
		println("PutAbsenceInputRequest: "
			+ getPutAbsenceInputRequest().getAbsenceInputData().getBatchID() + "\n\n"
			+ getPutAbsenceInputRequest() + "\n");
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
