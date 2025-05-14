package org.example.visitation;

import static org.example.visitation.Context.VIS;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import visitation.mynet.in.GetNodeAnalysisListType;

/**
 * Example of unmarshalling and marshaling a visitation.
 */
public class Main
{
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }

	public static final String SAMPLE_FILE = "src/test/samples/visitation01.xml";

	// Represents the JAXBContext for this instance.
	public static final Context CONTEXT = new Context();

    // Represents the unmarshalled GetNodeAnalysisListType.
    private GetNodeAnalysisListType visitaion = null;
    public GetNodeAnalysisListType getVisitation() { return visitaion; }
    public void setVisitation (GetNodeAnalysisListType visitaion) { this.visitaion = visitaion; }

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
		// JAXB: unmarshal XML file by path name.
		setVisitation(CONTEXT.unmarshal(xmlFileName, GetNodeAnalysisListType.class));
		if ( getVisitation() != null )
			getLogger().info("Visitation: {}", getVisitation());
		else
			getLogger().warn("Visitation not unmarshaled!");

		JAXBElement<GetNodeAnalysisListType> jaxbVis =
			VIS.createGetNodeAnalysisList(getVisitation());

		// JAXB: marshal GetNodeAnalysisListType.
		String xmlVisitation = CONTEXT.marshalToString(jaxbVis);
		getLogger().info("Visitation:\n\n{}", xmlVisitation);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
