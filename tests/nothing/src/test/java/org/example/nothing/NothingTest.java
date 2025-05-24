package org.example.nothing;

import static org.example.nothing.Main.CONTEXT;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

public class NothingTest extends AbstractSamplesTest
{
	@Override
	public JAXBContext createContext()
		throws JAXBException
	{
		return CONTEXT.getJaxbContext();
	}

	@Override
	protected void checkSample(File sampleFile)
		throws Exception
	{
		Object sample = getUnmarshaller().unmarshal(sampleFile);
		assertInstanceOf(Nothings.class, sample);
		Nothings nothings = (Nothings) sample;

		for ( Nothing nothing : nothings.getNothing() )
		{
			getLogger().debug("Id: {}", nothing.getId());
			getLogger().debug("\tisSetOptionalNillableString..: {}; {}",
				nothing.isSetOptionalNillableString(),
				toValue(nothing.getOptionalNillableString()));
			getLogger().debug("\tisSetRequiredNillableString..: {}; {}",
				nothing.isSetRequiredNillableString(),
				toValue(nothing.getRequiredNillableString()));
			getLogger().debug("\tisSetOptionalNillableStrings.: {}; {}",
				nothing.isSetOptionalNillableStrings(),
				toValue(nothing.getOptionalNillableStrings()));
				for ( int index=0; index < nothing.getOptionalNillableStrings().size(); ++index )
				{
					Object onsItem = nothing.getOptionalNillableStrings().get(index);
					getLogger().debug("\t\toptionalNillableString.....: [{}]; {}",
						index,
						toValue(onsItem));
				}
			getLogger().debug("\tisSetRequiredNillableStrings.: {}; {}",
				nothing.isSetRequiredNillableStrings(),
				toValue(nothing.getRequiredNillableStrings()));
				for ( int index=0; index < nothing.getRequiredNillableStrings().size(); ++index )
				{
					Object onsItem = nothing.getRequiredNillableStrings().get(index);
					getLogger().debug("\t\trequiredNillableString.....: [{}]; {}",
						index,
						toValue(onsItem));
				}
		}

		String xmlNothings = marshalToString(nothings);
		getLogger().debug("Nothings:\n{}\n", xmlNothings);
	}

	private String toValue(Object obj)
	{
		String value = "null";
		if ( obj instanceof JAXBElement )
		{
			JAXBElement<?> je = (JAXBElement<?>) obj;
			value = je.isNil() ? "nil" : toValue(je.getValue());
			value += " (JE isNill="+je.isNil()+")";
		}
		else if ( obj instanceof String )
			value = "\"" + obj + "\"";
		else if ( obj != null )
			value = obj.toString();
		return value;
	}

}
