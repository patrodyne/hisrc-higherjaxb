package org.jvnet.higherjaxb.w3c.xmlschema.v1_0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.jvnet.basicjaxb.lang.ValueUtils.toBigDecimal;
import static org.jvnet.basicjaxb.lang.ValueUtils.toXMLGregorianCalendar;

import java.io.File;
import java.math.BigDecimal;

import org.junit.jupiter.api.Order;
import org.jvnet.basicjaxb.lang.ContextUtils;
import org.jvnet.basicjaxb.testing.AbstractSamplesTest;
import org.jvnet.higherjaxb.w3c.po.ObjectFactory;
import org.jvnet.higherjaxb.w3c.po.PurchaseOrder;

@Order(3)
public class RunFluentAPITest extends AbstractSamplesTest
{
	private static final ObjectFactory OF = new ObjectFactory();
	
	@Override
	protected String getContextPath()
	{
		return ContextUtils.getContextPath(OF.getClass());
	}
	
	@Override
	protected void checkSample(File sample) throws Exception
	{
		setFailFast(true);
		final Object object = createContext().createUnmarshaller().unmarshal(sample);
		if ( object instanceof PurchaseOrder )
		{
			PurchaseOrder po1 = (PurchaseOrder) object;
			PurchaseOrder po2 = OF.createPurchaseOrder()
				.useOrderDate(toXMLGregorianCalendar("1999-05-20"))
				.useComplete(false)
				.useComment("Hurry, my lawn is going wild!")
				.useShipTo(OF.createUSAddress()
					.useName("Alice Smith")
					.useStreet("123 Maple Street")
					.useCity("Mill Valley")
					.useState("CA")
					.useZip(toBigDecimal("90952")))
				.useBillTo(OF.createUSAddress()
					.useName("Robert Smith")
					.useStreet("8 Oak Avenue")
					.useCity("Old Town")
					.useState("PA")
					.useZip(toBigDecimal("95819")))
				.useItems(OF.createItems()
					.useItem(OF.createItem()
						.usePartNum("872-AA")
						.useProductName("Lawnmower")
						.useQuantity(1)
						.useUSPrice(toBigDecimal("148.95"))
						.useComment("Confirm this is electric"))
					.useItem(OF.createItem()
						.usePartNum("926-AA")
						.useProductName("Baby Monitor")
						.useQuantity(1)
						.useUSPrice(new BigDecimal("39.98"))
						.useShipDate(toXMLGregorianCalendar("1999-10-21")))
				)
				.usePayments(OF.createPayments()
					.usePayment(
						OF.createPayment().usePaymentDate(toXMLGregorianCalendar("1999-10-21")).useValue(toBigDecimal("5.00")),
						OF.createPayment().usePaymentDate(toXMLGregorianCalendar("1999-10-22")).useValue(toBigDecimal("10.00")),
						OF.createPayment().usePaymentDate(toXMLGregorianCalendar("1999-10-23")).useValue(toBigDecimal("24.98"))
					)
				)
				.useCredits
				(
					OF.createCredit().useAmount(5.00).useReason("Promo: MAY99"),
					OF.createCredit().useAmount(10.00).useReason("Promo: OCT99")
				);
			
			getLogger().debug("PO1: {}", po1);
			getLogger().debug("PO2: {}", po2);
			
			assertEquals(po1, po2, "Unmarshaled and Fluent POs are equal.");
		}
	}
}
