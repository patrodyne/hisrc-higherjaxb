package org.jvnet.higherjaxb.w3c.xmlschema.v1_0;

import static java.math.BigInteger.ZERO;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.jvnet.basicjaxb.lang.ContextUtils.createUnmarshaller;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.BOOLEAN;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.DATE;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.DECIMAL;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.DOUBLE;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.NMTOKEN;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.POSITIVEINTEGER;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.STRING;
import static org.jvnet.higherjaxb.w3c.xmlschema.v1_0.FormChoice.QUALIFIED;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.jvnet.basicjaxb.lang.ContextUtils;
import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

@Order(1)
public class RunMarshalXmlSchemaTest extends AbstractSamplesTest
{
	private static final ObjectFactory OF = new ObjectFactory();
    private static final String PO_PREFIX = "po";
    private static final String PO_URI = "urn:po";
	
	@Override
	protected String getContextPath()
	{
		return ContextUtils.getContextPath(OF.getClass());
	}
	
	@Override
	protected File[] getSampleFiles()
	{
		return new File[] { new File("src/test/resources/po.xsd") };
	}

	@Override
	protected void checkSample(File sample) throws Exception
	{
		getLogger().debug("Sample: {}", sample);
		setFailFast(true);
		
		Unmarshaller schemaUnmarshaller = createUnmarshaller(getJaxbContext());
		Object root = schemaUnmarshaller.unmarshal(sample);
		if ( root instanceof Schema )
		{
			Schema pos1 = (Schema) root;
			Schema pos2 = createSchema();
			
			assertEquals(pos1.simpleType, pos2.simpleType, "pos1.simpleType vs pos2.simpleType");
			assertEquals(pos1.complexType, pos2.complexType, "pos1.complexType vs pos2.complexType");
			assertEquals(pos1.element, pos2.element, "pos1.element vs pos2.element");
			assertEquals(pos1.annotation.size(), pos2.annotation.size(), "pos1.annotation.size vs pos2.annotation.size");
			for ( int annidx=0; annidx < pos1.annotation.size(); ++annidx)
			{
				Annotation pos1ann = pos1.annotation.get(annidx);
				Annotation pos2ann = pos2.annotation.get(annidx);
				assertEquals(pos1ann.documentation.size(), pos2ann.documentation.size(), "pos1ann.documentation.size vs pos2ann.documentation.size");
				for ( int docidx=0; docidx < pos1ann.documentation.size(); ++docidx)
				{
					Documentation pos1doc = pos1ann.documentation.get(docidx);
					Documentation pos2doc = pos2ann.documentation.get(docidx);
					assertEquals(pos1doc.content.size(), pos2doc.content.size(), "pos1doc.content.size vs pos2doc.content.size");
					for ( int conidx=0; conidx < pos1doc.content.size(); ++conidx)
					{
						Serializable pos1con = pos1doc.content.get(conidx);
						Serializable pos2con = pos2doc.content.get(conidx);
						assertEquals(pos1con, pos2con, "pos1con vs pos2con");
					}
					assertEquals(pos1doc, pos2doc, "pos1doc vs pos2doc");
				}
				assertEquals(pos1ann, pos2ann, "pos1ann vs pos2ann");
			}
			assertEquals(pos1.annotation, pos2.annotation, "pos1.annotation vs pos2.annotation");
			assertEquals(pos1, pos2, "Unmarshalled PO Schema must equal fluent PO Schema.");
		}
		else
			fail(sample + " is not a Schema instance");
	}
	
	@Test
	public void testMarshalXmlSchema() throws JAXBException
	{
		Schema pos = createSchema();
		getMarshaller().setProperty("org.glassfish.jaxb.namespacePrefixMapper", NPM);
		String poxsd = marshalToString(pos);
		assertNotNull(poxsd);
		getLogger().debug("po.xsd:\n{}", poxsd);
	}
	
	private static final NamespacePrefixMapper NPM = new NamespacePrefixMapper()
	{
	    @Override
	    public String[] getPreDeclaredNamespaceUris()
	    {
	        return new String[] { PO_URI };
	    }
	    
		@Override
		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
		{
			String prefix = null;
			switch ( namespaceUri )
			{
				case PO_URI: prefix = PO_PREFIX; break;
				case W3C_XML_SCHEMA_NS_URI: prefix = "xsd"; break;
				default: prefix = suggestion; break;
			}
			return prefix;
		}
	};

	private Schema createSchema()
	{
		QName qnComment = new QName(PO_URI, "comment", PO_PREFIX);
		QName qnCredit = new QName(PO_URI, "Credit", PO_PREFIX);
		QName qnItems = new QName(PO_URI, "Items", PO_PREFIX);
		QName qnSKU = new QName(PO_URI, "SKU", PO_PREFIX);
		QName qnUSAddress = new QName(PO_URI, "USAddress", PO_PREFIX);
		
		Schema pos = OF.createSchema()
			.useElementFormDefault(QUALIFIED)
			.useTargetNamespace("urn:po")
			.useSimpleType(OF.createTopLevelSimpleType()
				.useName(qnSKU.getLocalPart())
				.useRestriction(OF.createRestriction()
					.useBase(STRING)
					.usePattern(OF.createPattern()
						.useValue("\\d{3}-[A-Z]{2}"))))
			.useComplexType(OF.createTopLevelComplexType()
				.useName(qnCredit.getLocalPart())
				.useAttribute(OF.createAttribute()
					.useName("amount")
					.useType(DOUBLE))
				.useAttribute(OF.createAttribute()
					.useName("reason")
					.useType(STRING)))
			.useComplexType(OF.createTopLevelComplexType()
				.useName(qnUSAddress.getLocalPart())
				.useSequence(OF.createExplicitGroup()
					.useElement(OF.createLocalElement()
						.useName("name")
						.useType(STRING))
					.useElement(OF.createLocalElement()
						.useName("street")
						.useType(STRING))
					.useElement(OF.createLocalElement()
						.useName("city")
						.useType(STRING))
					.useElement(OF.createLocalElement()
						.useName("state")
						.useType(STRING))
					.useElement(OF.createLocalElement()
						.useName("zip")
						.useType(DECIMAL)))
				.useAttribute(OF.createAttribute()
					.useName("country")
					.useType(NMTOKEN)
					.useFixed("US")))
			.useComplexType(OF.createTopLevelComplexType()
				.useName(qnItems.getLocalPart())
				.useSequence(OF.createExplicitGroup()
					.useElement(OF.createLocalElement()
						.useName("item")
						.useMinOccurs(ZERO)
						.useMaxOccurs("unbounded")
						.useComplexType(OF.createLocalComplexType()
							.useSequence(OF.createExplicitGroup()
								.useElement(OF.createLocalElement()
									.useName("productName")
									.useType(STRING))
								.useElement(OF.createLocalElement()
									.useName("quantity")
									.useSimpleType(OF.createLocalSimpleType()
										.useRestriction(OF.createRestriction()
											.useBase(POSITIVEINTEGER)
											.useMaxExclusive(OF.createFacet()
												.useValue("100")))))
								.useElement(OF.createLocalElement()
									.useName("USPrice")
									.useType(DECIMAL))
								.useElement(OF.createLocalElement()
									.useRef(qnComment)
									.useMinOccurs(ZERO))
								.useElement(OF.createLocalElement()
									.useName("shipDate")
									.useType(DATE)
									.useMinOccurs(ZERO)))
							.useAttribute(OF.createAttribute()
								.useName("partNum")
								.useType(qnSKU)
								.useUse("required"))
							))))
			.useElement(OF.createTopLevelElement()
				.useName(qnComment.getLocalPart())
				.useType(STRING))
			.useElement(OF.createTopLevelElement()
				.useName("purchaseOrder")
				.useComplexType(OF.createLocalComplexType()
					.useSequence(OF.createExplicitGroup()
						.useElement(OF.createLocalElement()
							.useName("shipTo")
							.useType(qnUSAddress))
						.useElement(OF.createLocalElement()
							.useName("billTo")
							.useType(qnUSAddress))
						.useElement(OF.createLocalElement()
							.useRef(qnComment)
							.useMinOccurs(ZERO))
						.useElement(OF.createLocalElement()
							.useName("items")
							.useType(qnItems))
						.useElement(OF.createLocalElement()
							.useName("payments")
							.useComplexType(OF.createLocalComplexType()
								.useSequence(OF.createExplicitGroup()
									.useElement(OF.createLocalElement()
										.useName("payment")
										.useMinOccurs(ZERO)
										.useMaxOccurs("unbounded")
										.useComplexType(OF.createLocalComplexType()
											.useSimpleContent(OF.createSimpleContent()
												.useExtension(OF.createSimpleExtensionType()
													.useBase(DECIMAL)
													.useAttribute(OF.createAttribute()
														.useName("paymentDate")
														.useType(DATE)))))))))
						.useElement(OF.createLocalElement()
							.useName("credits")
							.useType(qnCredit)
							.useMinOccurs(ZERO)
							.useMaxOccurs("unbounded")
							))
					.useAttribute(OF.createAttribute()
						.useName("orderDate")
						.useType(DATE))
					.useAttribute(OF.createAttribute()
						.useName("complete")
						.useType(BOOLEAN))
					))
			.useAnnotation(OF.createAnnotation()
				.useDocumentation(OF.createDocumentation()
					.useLang(Locale.ENGLISH.getLanguage())
					.useContent
					(
						"\n\t\t\tPurchase order schema for Example.com." +
						"\n\t\t\tCopyright 2000 Example.com. All rights reserved." +
						"\n\t\t"
					)))
			;
		return pos;
	}
}
