package org.patrodyne.jvnet.higherjaxb.ex001;

import static java.lang.Integer.toHexString;
import static java.lang.System.identityHashCode;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.patrodyne.jvnet.higherjaxb.ex001.model.Action.BUY;
import static org.patrodyne.jvnet.higherjaxb.ex001.model.Bind.newXMLGregorianCalendar;
import static org.jvnet.jaxb2_commons.test.Bogus.alpha;
import static org.jvnet.jaxb2_commons.test.Bogus.digit;
import static org.jvnet.jaxb2_commons.test.Bogus.randomEnum;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.patrodyne.jvnet.basicjaxb.explore.AbstractExplorer;
import org.patrodyne.jvnet.basicjaxb.validation.SchemaOutputDomResolver;
import org.patrodyne.jvnet.basicjaxb.validation.SchemaOutputStringResolver;
import org.patrodyne.jvnet.higherjaxb.ex001.model.Action;
import org.patrodyne.jvnet.higherjaxb.ex001.model.Completed;
import org.patrodyne.jvnet.higherjaxb.ex001.model.Duration;
import org.patrodyne.jvnet.higherjaxb.ex001.model.LimitPrice;
import org.patrodyne.jvnet.higherjaxb.ex001.model.ObjectFactory;
import org.patrodyne.jvnet.higherjaxb.ex001.model.Quantity;
import org.patrodyne.jvnet.higherjaxb.ex001.model.StopPrice;
import org.patrodyne.jvnet.higherjaxb.ex001.model.Trade;
import org.patrodyne.jvnet.higherjaxb.ex001.model.TransactionBatch;
import org.patrodyne.jvnet.higherjaxb.ex001.model.Transfer;
import org.jvnet.jaxb2_commons.test.Bogus;
import org.xml.sax.SAXException;

/**
 * A Swing application to explore features of the HiSrc HigherJAXB Runtime library.
 * 
 * Projects create their own custom Explorer by extending AbstractExplorer and
 * providing an HTML lesson page then adding JMenuItem(s) to trigger exploratory code.
 * 
 * The AbstractExplorer displays three panels: an HTML lesson, a print console and an
 * error console. The lesson file is read as a resource relative to this class. Text
 * is sent to the print console by calling 'println(text)' and error messages are
 * sent to the error console by calling 'errorln(msg)'. Also, 'standard out' /
 * 'standard error' streams are sent to their respective consoles.
 * 
 * @author Rick O'Sullivan
 */
@SuppressWarnings("serial")
public class Explorer extends AbstractExplorer
{
	private static final String WINDOW_TITLE = "HiSrc HigherJAXB Ex001 TradeDTD";
	private static final String EXPLORER_HTML = "Explorer.html";
	private static final int TRADE_LIST_SIZE = 50;
	private static final int TRANSFER_LIST_SIZE = 50;

	private ObjectFactory objectFactory;
	public ObjectFactory getObjectFactory() { return objectFactory; }
	public void setObjectFactory(ObjectFactory objectFactory) { this.objectFactory = objectFactory; }

	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() { return jaxbContext; }
	public void setJaxbContext(JAXBContext jaxbContext) { this.jaxbContext = jaxbContext; }

	private Marshaller marshaller;
	public Marshaller getMarshaller() { return marshaller; }
	public void setMarshaller(Marshaller marshaller) { this.marshaller = marshaller; }

	private Unmarshaller unmarshaller;
	public Unmarshaller getUnmarshaller() { return unmarshaller; }
	public void setUnmarshaller(Unmarshaller unmarshaller) { this.unmarshaller = unmarshaller; }
	
	private Trade trade1;
	public Trade getTrade1() { return trade1; }
	public void setTrade1(Trade trade1) { this.trade1 = trade1; }

	private Trade trade2;
	public Trade getTrade2() { return trade2; }
	public void setTrade2(Trade trade2) { this.trade2 = trade2; }

	private Trade trade3;
	public Trade getTrade3() { return trade3; }
	public void setTrade3(Trade trade3) { this.trade3 = trade3; }

	private List<Trade> tradeList;
	public List<Trade> getTradeList() { return tradeList; }
	public void setTradeList(List<Trade> tradeList) { this.tradeList = tradeList; }
	
	private List<Transfer> transferList;
	public List<Transfer> getTransferList() { return transferList; }
	public void setTransferList(List<Transfer> transferList) { this.transferList = transferList; }
	
	/**
	 * Main entry point for command line invocation.
	 * @param args CLI arguments
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(() -> {
			JFrame frame = new Explorer(EXPLORER_HTML);
			frame.setVisible(true);
		});
	}

	/**
	 * Construct application with an HTML lesson.
	 */
	public Explorer(String htmlName)
	{
		super(htmlName);
		setTitle(WINDOW_TITLE);
		try
		{
			setJMenuBar(createMenuBar());
			modifyToolBar();
			initializeLesson();
		}
		catch (Exception ex)
		{
			errorln(ex);
		}
	}

	public void generateXmlSchemaFromString()
	{
		try
		{
			SchemaOutputStringResolver sosr = new SchemaOutputStringResolver();
			getJaxbContext().generateSchema(sosr);
			println("Xml Schema from String:\n\n" + sosr.getSchemaString());
		}
		catch ( IOException ex )
		{
			errorln(ex);
		}
	}
	
	public void generateXmlSchemaFromDom()
	{
		try
		{
			SchemaOutputDomResolver sodr = new SchemaOutputDomResolver();
			getJaxbContext().generateSchema(sodr);
			println("Xml Schema from DOM:\n\n" + sodr.getSchemaDomNodeString());
		}
		catch ( IOException | TransformerException ex )
		{
			errorln(ex);
		}
	}
	
	public void generateXmlSchemaValidatorFromDom()
	{
		try
		{
			if ( (getMarshaller() != null) && (getUnmarshaller() != null) )
			{
				// Generate a Schema Validator from given the JAXB context.
				SchemaOutputDomResolver sodr = new SchemaOutputDomResolver();
				getJaxbContext().generateSchema(sodr);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
				Schema schemaValidator = schemaFactory.newSchema(sodr.getDomSource());
				
				// Configure Marshaller / unmarshaller to use validator.
				getMarshaller().setSchema(schemaValidator);
				getUnmarshaller().setSchema(schemaValidator);
				
				getValidateButton().setSelected(true);
				println("Schema Validation is ON.");
			}
			else
				errorln("Please create marshaller and unmarshaller!");
		}
		catch ( IOException | SAXException ex )
		{
			errorln(ex);
		}
	}
	
	public void marshalTrade1()
	{
		marshal("Trade1", getTrade1());
	}
	
	public void marshalTrade2()
	{
		marshal("Trade2", getTrade2());
	}
	
	public void marshalTrade3()
	{
		marshal("Trade3", getTrade3());
	}

	public void marshalTrades()
	{
		TransactionBatch batch = new TransactionBatch();
		batch.getTradeOrTransfer().addAll(getTradeList());
		marshal("Trades", batch);
	}

	public void marshalTransfers()
	{
		TransactionBatch batch = new TransactionBatch();
		batch.getTradeOrTransfer().addAll(getTransferList());
		marshal("Transfers", batch);
	}
	
	public void compareHashCodes()
	{
		String trade1HashCode = toHexString(getTrade1().hashCode());
		String trade2HashCode = toHexString(getTrade2().hashCode());
		String trade3HashCode = toHexString(getTrade3().hashCode());

		String trade1IdentityHashCode = toHexString(identityHashCode(getTrade1()));
		String trade2IdentityHashCode = toHexString(identityHashCode(getTrade2()));
		String trade3IdentityHashCode = toHexString(identityHashCode(getTrade3()));
		
		println("Compare Hash Codes\n");
		println("Trade1 hashCode: " + trade1HashCode + "; identityHashCode: " + trade1IdentityHashCode);
		println("Trade2 hashCode: " + trade2HashCode + "; identityHashCode: " + trade2IdentityHashCode);
		println("Trade3 hashCode: " + trade3HashCode + "; identityHashCode: " + trade3IdentityHashCode);
		println();
	}
	
	public void compareEquality()
	{
		println("Compare Equality\n");
		println("Trade1 vs Trade2: " + (getTrade1().equals(getTrade2()) ? "EQUAL" : "UNEQUAL"));
		println("Trade1 vs Trade3: " + (getTrade1().equals(getTrade3()) ? "EQUAL" : "UNEQUAL"));
		println();
	}

	public void compareToString()
	{
		println("Compare ToString\n");
		println("Trade1 toString: " + getTrade1().toString());
		println("Trade2 toString: " + getTrade2().toString());
		println("Trade3 toString: " + getTrade3().toString());
		println();
	}

	public void roundtripValid()
	{
		Trade trade1A = getTrade1();
		String trade1AXml = marshalToString(trade1A);
		Trade trade1B = unmarshalFromString(trade1AXml, Trade.class);
		println("Trade1A vs Trade1B: " + (trade1A.equals(trade1B) ? "EQUAL" : "UNEQUAL"));
		println();
	}

	public void roundtripInvalid()
	{
		// trade1BXml is made intentionally invalid!
		Trade trade1A = getTrade1();
		String trade1AXml = marshalToString(trade1A);
		String trade1BXml = trade1AXml.replaceAll("symbol", "simbol");
		Trade trade1B = unmarshalFromString(trade1BXml, Trade.class);
		if ( trade1A.equals(trade1B) )
			println("Trade1A vs Trade1B: EQUAL");
		else
		{
			println("Trade1A vs Trade1B: UNEQUAL");
			println();
			println("[BEFORE] Trade1A XML:\n" + trade1AXml);
			println("[BEFORE] Trade1B XML:\n" + trade1BXml);
			println();
			println("[AFTER] Trade1A XML:\n" + marshalToString(trade1A));
			println("[AFTER] Trade1B XML:\n" + marshalToString(trade1B));
		}
		println();
	}

	public void searchTrades()
	{
		Trade trade1 = Bogus.randomItem(getTradeList());
		Trade trade2 = unmarshalFromString(marshalToString(trade1), Trade.class);
		int index = getTradeList().indexOf(trade2);
		println("Search: " + trade2);
		println("Found.: " + getTradeList().get(index));
		println("Index.: " + index);
		println();
	}

	public void searchTransfers()
	{
		Transfer transfer1 = Bogus.randomItem(getTransferList());
		Transfer transfer2 = unmarshalFromString(marshalToString(transfer1), Transfer.class);
		int index = getTransferList().indexOf(transfer2);
		println("Search: " + transfer2);
		println("Found.: " + getTransferList().get(index));
		println("Index.: " + index);
		println();
	}
	
	/**
	 * Dispatch hyperlinks from the lesson to local method invocations.
	 * The markdown for hyperlinks in the lesson is declared like this:
	 * 
	 *   [description](!hyperLink)
	 */
	@Override
	public void dispatchHyperLink(String hyperLink)
	{
		switch ( hyperLink )
		{
			case "generateXmlSchemaFromString": generateXmlSchemaFromString(); break;
			case "generateXmlSchemaFromDom": generateXmlSchemaFromDom(); break;
			case "generateXmlSchemaValidatorFromDom": generateXmlSchemaValidatorFromDom(); break;
			case "marshalTrade1": marshalTrade1(); break;
			case "marshalTrade2": marshalTrade2(); break;
			case "marshalTrade3": marshalTrade3(); break;
			case "marshalTrades": marshalTrades(); break;
			case "marshalTransfers": marshalTransfers(); break;
			case "compareHashCodes": compareHashCodes(); break;
			case "compareEquality": compareEquality(); break;
			case "compareToString": compareToString(); break;
			case "roundtripValid": roundtripValid(); break;
			case "roundtripInvalid": roundtripInvalid(); break;
			case "searchTrades": searchTrades(); break;
			case "searchTransfers": searchTransfers(); break;
		}
	}

	/*
	 * Create a JMenuBar to display JMenuItem(s) to trigger
	 * your own exploratory methods.
	 */
	public JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		
		// Context Menu
		{
			JMenu contextMenu = new JMenu("Context");
			// Context: Generate XML Schema from String
			{
				JMenuItem menuItem = new JMenuItem("Generate XML Schema from String");
				menuItem.addActionListener((event) -> generateXmlSchemaFromString());
				contextMenu.add(menuItem);
			}
			// Context: Generate XML Schema from DOM
			{
				JMenuItem menuItem = new JMenuItem("Generate XML Schema from DOM");
				menuItem.addActionListener((event) -> generateXmlSchemaFromDom());
				contextMenu.add(menuItem);
			}
			// Context: Generate XML Schema Validator from DOM
			{
				JMenuItem menuItem = new JMenuItem("Generate XML Schema Validator from DOM");
				menuItem.addActionListener((event) -> generateXmlSchemaValidatorFromDom());
				contextMenu.add(menuItem);
			}
			menuBar.add(contextMenu);
		}

		// Marshal Menu
		{
			JMenu marshalMenu = new JMenu("Marshal");
			// Marshal: Trade1
			{
				JMenuItem menuItem = new JMenuItem("Trade1");
				menuItem.addActionListener((event) -> marshalTrade1());
				marshalMenu.add(menuItem);
			}
			// Marshal: Trade2
			{
				JMenuItem menuItem = new JMenuItem("Trade2");
				menuItem.addActionListener((event) -> marshalTrade2());
				marshalMenu.add(menuItem);
			}
			// Marshal: Trade3
			{
				JMenuItem menuItem = new JMenuItem("Trade3");
				menuItem.addActionListener((event) -> marshalTrade3());
				marshalMenu.add(menuItem);
			}
			// Marshal: Trades
			{
				JMenuItem menuItem = new JMenuItem("Trades");
				menuItem.addActionListener((event) -> marshalTrades());
				marshalMenu.add(menuItem);
			}
			// Marshal: Transfers
			{
				JMenuItem menuItem = new JMenuItem("Transfers");
				menuItem.addActionListener((event) -> marshalTransfers());
				marshalMenu.add(menuItem);
			}
			menuBar.add(marshalMenu);
		}

		// Compare Menu
		{
			JMenu compareMenu = new JMenu("Compare");
			// Compare: HashCodes
			{
				JMenuItem menuItem = new JMenuItem("HashCodes");
				menuItem.addActionListener((event) -> compareHashCodes());
				compareMenu.add(menuItem);
			}
			// Compare: Equality
			{
				JMenuItem menuItem = new JMenuItem("Equality");
				menuItem.addActionListener((event) -> compareEquality());
				compareMenu.add(menuItem);
			}
			// Compare: ToString
			{
				JMenuItem menuItem = new JMenuItem("ToString");
				menuItem.addActionListener((event) -> compareToString());
				compareMenu.add(menuItem);
			}
			menuBar.add(compareMenu);
		}

		// Roundtrip Menu
		{
			JMenu roundtripMenu = new JMenu("Roundtrip");
			// Roundtrip: Valid
			{
				JMenuItem menuItem = new JMenuItem("Valid");
				menuItem.addActionListener((event) -> roundtripValid());
				roundtripMenu.add(menuItem);
			}
			// Roundtrip: Invalid
			{
				JMenuItem menuItem = new JMenuItem("Invalid");
				menuItem.addActionListener((event) -> roundtripInvalid());
				roundtripMenu.add(menuItem);
			}
			menuBar.add(roundtripMenu);
		}

		// Search Menu
		{
			JMenu searchMenu = new JMenu("Search");
			// Search: Trades
			{
				JMenuItem menuItem = new JMenuItem("Trades");
				menuItem.addActionListener((event) -> searchTrades());
				searchMenu.add(menuItem);
			}
			// Search: Transfers
			{
				JMenuItem menuItem = new JMenuItem("Transfers");
				menuItem.addActionListener((event) -> searchTransfers());
				searchMenu.add(menuItem);
			}
			menuBar.add(searchMenu);
		}

		return menuBar;
	}
	
	private JToggleButton validateButton;
	public JToggleButton getValidateButton() { return validateButton; }
	public void setValidateButton(JToggleButton validateButton) { this.validateButton = validateButton; }

	public void modifyToolBar()
	{
		getToolBar().addSeparator();
		String validateOffPath = OILPATH+"/actions/flag-red.png";
		String validateOnPath = OILPATH+"/actions/flag-green.png";
		setValidateButton(createImageToggleButton(Explorer.class, validateOffPath, validateOnPath));
		getValidateButton().addActionListener((event) -> toggleValidateSchema(event));
		getValidateButton().setToolTipText("Toggle schema validation");
		getToolBar().add(getValidateButton());
	}
	
	private void toggleValidateSchema(ActionEvent event)
	{
		JToggleButton toggleButton = (JToggleButton) event.getSource();
		if ( toggleButton.isSelected() )
			generateXmlSchemaValidatorFromDom();
		else
		{
			setMarshaller(createMarshaller(getJaxbContext()));
			setUnmarshaller(createUnmarshaller(getJaxbContext()));
			println("Schema Validation is OFF.");
		}
	}

	public void initializeLesson() throws Exception
	{
		setObjectFactory(new ObjectFactory());
		setJaxbContext(createJAXBContext());
		setMarshaller(createMarshaller(getJaxbContext()));
		setUnmarshaller(createUnmarshaller(getJaxbContext()));
		
		XMLGregorianCalendar created = newXMLGregorianCalendar("-P1D");
		XMLGregorianCalendar completed = newXMLGregorianCalendar();
		
		setTrade1(createTrade(1001, Action.BUY, "ACME", 100, created, completed));
		setTrade2(createTrade(1001, Action.BUY, "ACME", 100, created, completed));
		setTrade3(createTrade(1002, Action.SELL, "ACME", 50, created, completed));
		
		setTradeList(createTradeList());
		setTransferList(createTransferList());
	}
	
	public Trade createTrade(int account, Action action, String symbol, int qty,
		XMLGregorianCalendar created, XMLGregorianCalendar completed)
	{
		Quantity quantity = getObjectFactory().createQuantity(qty);
		Trade trade = getObjectFactory().createTrade(account, action, symbol, quantity);
		trade.setCreated(created);
		trade.setCompleted(getObjectFactory().createCompleted(completed));
		
		if ( action == Action.BUY )
		{
			trade.setStopPrice(getObjectFactory().createStopPrice(BigDecimal.ONE));
			trade.setLimitPrice(getObjectFactory().createLimitPrice(BigDecimal.TEN));
		}
		
		return trade;
	}
	
	public List<Trade> createTradeList()
	{
		XMLGregorianCalendar created = newXMLGregorianCalendar("-P1D");
		XMLGregorianCalendar completed = newXMLGregorianCalendar();
		Map<Integer, Trade> trades = new HashMap<>(TRADE_LIST_SIZE);
		do
		{
			Trade trade = new Trade();
			trade.setCreated(created);
			trade.setAccount(digit(5));
			trade.setSymbol(alpha(4).toUpperCase());
			trade.setAction(randomEnum(Action.class));
			trade.setDuration(randomEnum(Duration.class));
			trade.setQuantity(new Quantity(digit(3)));
			if ( trade.getAction() == BUY )
			{
				BigDecimal stop = (new BigDecimal(digit(2)).setScale(2));
				BigDecimal limit = stop.add(new BigDecimal(digit(1)).setScale(2));
				trade.setStopPrice(new StopPrice(stop));
				trade.setLimitPrice(new LimitPrice(limit));
			}
			trade.setCompleted(new Completed(completed));
			// Duplicate accounts are replaced, intentionally.
			trades.put(trade.getAccount(), trade);
		} while ( trades.size() < TRADE_LIST_SIZE );
		return new ArrayList<Trade>(trades.values());
	}
	
	public List<Transfer> createTransferList()
	{
		XMLGregorianCalendar created = newXMLGregorianCalendar("-P1D");
		XMLGregorianCalendar completed = newXMLGregorianCalendar();
		Map<Integer[], Transfer> transfers = new HashMap<>(TRANSFER_LIST_SIZE);
		do
		{
			Trade trade1 = Bogus.randomItem(getTradeList());
			Trade trade2 = Bogus.randomItem(getTradeList());
			if ( !trade1.getAccount().equals(trade2.getAccount()) )
			{
				Transfer transfer = new Transfer();
				transfer.setCreated(created);
				transfer.setAccount(trade1.getAccount());
				transfer.setToAccount(trade2.getAccount());
				transfer.setSymbol(trade1.getSymbol());
				transfer.setQuantity(new Quantity(digit(2)));
				transfer.setCompleted(new Completed(completed));
				// Duplicate transfers are replaced, intentionally.
				Integer[] fromTo = { transfer.getAccount(), transfer.getToAccount() };
				transfers.put(fromTo, transfer);
			}
		} while ( transfers.size() < TRANSFER_LIST_SIZE );
		return new ArrayList<Transfer>(transfers.values());
	}
	
	private JAXBContext createJAXBContext()
	{
		JAXBContext jaxbContext = null;
		try
		{
			Class<?>[] classesToBeBound =
				{ ObjectFactory.class, Trade.class, Transfer.class,
				  TransactionBatch.class };
			jaxbContext = JAXBContext.newInstance(classesToBeBound);
		}
		catch ( JAXBException ex)
		{
			errorln(ex);
		}
		return jaxbContext;
	}

	private Marshaller createMarshaller(JAXBContext jaxbContext)
	{
		Marshaller marshaller = null;
		try
		{
			if ( jaxbContext != null )
			{
				marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
			}
			else
				errorln("Cannot create marshaller because JAXB context is null!");
		}
		catch ( JAXBException ex )
		{
			errorln(ex);
		}
		return marshaller;
	}
	
	private Unmarshaller createUnmarshaller(JAXBContext jaxbContext)
	{
		Unmarshaller unmarshaller = null;
		try
		{
			if ( jaxbContext != null )
			{
				unmarshaller = jaxbContext.createUnmarshaller();
			}
			else
				errorln("Cannot create unmarshaller because JAXB context is null!");
		}
		catch ( JAXBException ex )
		{
			errorln(ex);
		}
		return unmarshaller;
	}

	private void marshal(String label, Serializable instance)
	{
		String ehc = toHexString(instance.hashCode());
		String ihc = toHexString(identityHashCode(instance));
		String tradeXml = marshalToString(instance);
		// Element Hash vs Object Hash
		println(label + " XML: (E#=" + ehc + ", O#=" + ihc + ")\n\n" +tradeXml);
		println();
	}
	
	private String marshalToString(Object instance)
	{
		String xml = null;
		if ( instance != null)
		{
			try ( StringWriter writer = new StringWriter() )
			{
				getMarshaller().marshal(instance, writer);
				xml = writer.toString();
			}
			catch (JAXBException | IOException ex)
			{
				errorln(ex);
			}
		}
		return xml;
	}

	@SuppressWarnings("unchecked")
	private <T> T unmarshalFromString(String xml, Class<?> clazz)
	{
		T instance = null;
		try ( StringReader reader = new StringReader(xml) )
		{
			instance = (T) getUnmarshaller().unmarshal(new StreamSource(reader), clazz).getValue();
		}
		catch (JAXBException ex)
		{
			errorln(ex);
		}
		return instance;
	}
}
