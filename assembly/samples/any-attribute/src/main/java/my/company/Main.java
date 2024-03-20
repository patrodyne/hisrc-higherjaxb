package my.company;

import static my.company.MtJaxbContext.MY_COMPANY_REFERENCE_QNAME;
import static my.company.MtJaxbContext.MY_TYPE_WRAPPER_XSD;

import org.jvnet.higherjaxb.w3c.xmlschema.v1_0.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import my.company.v2.MyTypeWrapper;

/**
 * Example of unmarshalling a MyTypeWrapper.xml and a MyTypeWrapper.xsd.
 */
public class Main
{
	public static final String SAMPLE_FILE = "src/test/samples/MyTypeWrapper.xml";
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }
	
	// Represents the unmarshalled MyTypeWrapper.
	private MyTypeWrapper myTypeWrapper = null;
	public MyTypeWrapper getMyTypeWrapper() { return myTypeWrapper; }
	public void setMyTypeWrapper(MyTypeWrapper myTypeWrapper) { this.myTypeWrapper = myTypeWrapper; }

	// Represents the unmarshalled XML Schema.
	private Schema xsSchema = null;
	public Schema getXsSchema() { return xsSchema; }
	public void setXsSchema(Schema xsSchema) { this.xsSchema = xsSchema; }

	/**
	 * Command Line Invocation.
	 * @param args CLI argument(s)
	 */
	public static void main(String[] args) throws Exception
	{
		String path = args.length > 0 ? args[0] : SAMPLE_FILE;
		
		XsJaxbContext xsJaxbContext = new XsJaxbContext();
		MtJaxbContext mtJaxbContext = new MtJaxbContext();
		
		Main main = new Main();
		main.setXsSchema(xsJaxbContext.execute(MY_TYPE_WRAPPER_XSD));
		main.setMyTypeWrapper(mtJaxbContext.execute(path));
		main.validateValue(mtJaxbContext);
	}

	private void validateValue(MtJaxbContext mtJaxbContext)
	{
		println("MyTypeWrapper: " + getMyTypeWrapper());
		String myCompanyReference =
			mtJaxbContext.findMyCompanyReference(getXsSchema());
		if ( myCompanyReference != null )
		{
			String mtValue = getMyTypeWrapper().getMt();
			println(MY_COMPANY_REFERENCE_QNAME + " = " + myCompanyReference);
			Boolean valid =
				mtJaxbContext.validateMyCompanyReference(myCompanyReference, mtValue);
			if ( valid == null )
				errorln("Cannot resolve " + myCompanyReference);
			if ( valid )
				println("VALID: " + mtValue);
			else
				errorln("INVALID: " + mtValue);
		}
		else
			errorln(MY_COMPANY_REFERENCE_QNAME + " not found");
	}
	
	/**
	 * Print a text string.
	 * @param text The text to be printed.
	 */
    public static void println(String text)
	{
		getLogger().info(text);
	}

	/**
	 * Print a error string.
	 * @param error The text to be printed.
	 */
    public static void errorln(String error)
	{
		getLogger().error(error);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
