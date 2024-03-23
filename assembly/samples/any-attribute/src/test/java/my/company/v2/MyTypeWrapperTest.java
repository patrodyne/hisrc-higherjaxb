package my.company.v2;

import static my.company.MtJaxbContext.MY_TYPE_WRAPPER_XSD;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringWriter;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;
import org.jvnet.higherjaxb.w3c.xmlschema.v1_0.Schema;

import jakarta.xml.bind.JAXBElement;
import my.company.MtJaxbContext;
import my.company.XsJaxbContext;

/**
 * A JUnit test to check a sample XML file.
 */
public class MyTypeWrapperTest extends AbstractSamplesTest
{
	private static final ObjectFactory OF = new ObjectFactory();
	
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		setFailFast(true);
		
		MtJaxbContext mtJaxbContext = new MtJaxbContext(createContext());
		Object root = mtJaxbContext.getUnmarshaller().unmarshal(sample);
		
		// Unmarshal MyTypeWrapper sample
		MyTypeWrapper myTypeWrapper = null;
		if ( root instanceof MyTypeWrapper )
			myTypeWrapper = (MyTypeWrapper) root;
		else if ( root instanceof JAXBElement )
		{
			@SuppressWarnings("unchecked")
			JAXBElement<MyTypeWrapper> myTypeWrapperJE = (JAXBElement<MyTypeWrapper>) root;
			myTypeWrapper = myTypeWrapperJE.getValue();
		}
		else
			fail("Object is not instance of MyTypeWrapper: " + root);
		
		// Marshal MyTypeWrapper sample
		if ( myTypeWrapper != null )
		{
			JAXBElement<MyTypeWrapper> myTypeWrapperJE =
				OF.createMyTypeWrapper(myTypeWrapper);
			
			String myTypeWrapperXml = null;
			try ( StringWriter sw = new StringWriter() )
			{
				mtJaxbContext.getMarshaller().marshal(myTypeWrapperJE, sw);
				myTypeWrapperXml = sw.toString();
			}
			getLogger().debug("MyTypeWrapper:\n\n" + myTypeWrapperXml);
		}

		XsJaxbContext xsJaxbContext = new XsJaxbContext();
		Schema xsSchema = xsJaxbContext.execute(MY_TYPE_WRAPPER_XSD);
		assertNotNull(xsSchema, "MyTypeWrapper XSD Schema Instance");
		assertFalse(xsSchema.getSimpleType().isEmpty(), "Expect MyType simple type");
		getLogger().info(xsSchema.getSimpleType().get(0).toString());
		
		String myCompanyReference =
			mtJaxbContext.findMyCompanyReference(xsSchema);
		assertNotNull(myCompanyReference, "MyType Company Reference");
		
		String mtValue = myTypeWrapper.getMt();
		Boolean valid =
			mtJaxbContext.validateMyCompanyReference(myCompanyReference, mtValue);
		assertTrue(valid, "Expect SomeValidValue: " + mtValue);
	}
}
