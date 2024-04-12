package org.example.employee_map;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jvnet.basicjaxb.testing.AbstractSamplesTest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file.
 */
public class EmployeeTest extends AbstractSamplesTest
{
	private static final ObjectFactory OF = new ObjectFactory();
	
	@Override
	protected void checkSample(File sample)
		throws Exception
	{
		setFailFast(true);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(OF.getClass());
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object root = unmarshaller.unmarshal(sample);
		
		// Unmarshal Employee
		Employee employee = null;
        if ( root instanceof JAXBElement )
        {
            @SuppressWarnings("unchecked")
            JAXBElement<Employee> employeeJE = (JAXBElement<Employee>) root;
            employee = employeeJE.getValue();
        }
        else if ( root instanceof Employee )
            employee = (Employee) root;
        else
            fail("Object is not instance of Employee: " + root);
        
        assertNotNull(employee);
        assertNotNull(employee.getValue());
        assertNotNull(employee.getDataMap());
        assertFalse(employee.getDataMap().isEmpty());

		// Marshal Employee		
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
		String employeeXml = null;
		employeeXml = marshalToString(employee, marshaller);
		getLogger().debug("Employee:\n\n" + employeeXml);
	}

	private String marshalToString(Employee employee, Marshaller marshaller)
		throws JAXBException, IOException, XMLStreamException, FactoryConfigurationError
	{
		String employeeXml;
		try ( StringWriter sw = new StringWriter() )
		{
	        XMLStreamWriter xw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
	        NamespaceStrippingXMLStreamWriter nsxsw = new NamespaceStrippingXMLStreamWriter(xw);
			marshaller.marshal(OF.createEmployee(employee), nsxsw);
			employeeXml = sw.toString();
		}
		return employeeXml;
	}
}
