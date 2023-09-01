package org.example.employee;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.jvnet.basicjaxb.test.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file.
 */
public class EmployeeTest extends AbstractSamplesTest
{
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(Employee.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object root = unmarshaller.unmarshal(sample);
		if ( root instanceof Employee )
		{
			Employee employee = (Employee) root;

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

			String employeeXml = null;
			employeeXml = marshalToString(employee, marshaller);
			getLogger().debug("Employee:\n\n" + employeeXml);
		}
		else
			fail("Object is not instance of Employee: " + root);
	}
	
	@Test
	public void testEmployees() throws Exception
	{
		// Unmarshal Employees
		List<Employee> employeeList = new ArrayList<>();
		JAXBContext jaxbContext = JAXBContext.newInstance(Employee.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Decoder decoder = Base64.getDecoder();
		File employeesB64Source = new File("src/test/samples/Employees.b64");
		// Read Source
		try ( FileReader fr = new FileReader(employeesB64Source) )
		{
			LineNumberReader lnr = new LineNumberReader(fr);
			String employeeB64 = null;
			while ( (employeeB64 = lnr.readLine()) != null )
			{
				String employeeXml = new String(decoder.decode(employeeB64), UTF_8);
				Object root = unmarshaller.unmarshal(new StringReader(employeeXml));
				if ( root instanceof Employee )
					employeeList.add((Employee) root);
			}
		}
		assertFalse(employeeList.isEmpty(), "Employee list should not be empty.");
		
		// Marshal Employees
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
		Encoder encoder = Base64.getEncoder();
		String nl = System.getProperty("line.separator");
		File employeesB64Target = new File("target/Employees.b64");
		// Write Target
		try ( FileWriter fw = new FileWriter(employeesB64Target) )
		{
			for ( Employee employee : employeeList )
			{
				String employeeXml = null;
				employeeXml = marshalToString(employee, marshaller);
				String employeeB64 = encoder.encodeToString(employeeXml.getBytes(UTF_8));
				fw.write(employeeB64 + nl);
			}
		}
	}

	private String marshalToString(Employee employee, Marshaller marshaller)
		throws JAXBException, IOException
	{
		String employeeXml;
		try ( StringWriter sw = new StringWriter() )
		{
			marshaller.marshal(employee, sw);
			employeeXml = sw.toString();
		}
		return employeeXml;
	}
}
