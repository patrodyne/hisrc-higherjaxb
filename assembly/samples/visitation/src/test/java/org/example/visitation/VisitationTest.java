package org.example.visitation;

import static org.example.visitation.Context.VIS;
import static org.example.visitation.Main.CONTEXT;
import static org.example.visitation.Main.SAMPLE_FILE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import visitation.mynet.in.GetNodeAnalysisListType;

/**
 * A JUnit test to check a sample XML file.
 */
public class VisitationTest
{
	private static Logger logger = LoggerFactory.getLogger(VisitationTest.class);
	public static Logger getLogger() { return logger; }

	@Test
	public void testVisitation() throws JAXBException
	{
		GetNodeAnalysisListType visitation =
			CONTEXT.unmarshal(SAMPLE_FILE, GetNodeAnalysisListType.class);
		assertNotNull(visitation, "Unmarshal visitaion");

		JAXBElement<GetNodeAnalysisListType> jaxbVis =
			VIS.createGetNodeAnalysisList(visitation);

		String xmlVisitation = CONTEXT.marshalToString(jaxbVis);
		getLogger().info("Visitation:\n\n{}", xmlVisitation);
	}
}
