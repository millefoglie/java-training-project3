package depositprocessor;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import deposit.DepositsDB;

/**
 * JAXB demo. Unmarshal the deposits XML file and marshal it to the copy file. 
 */
public class DepositJAXBDemo {
    
    private DepositJAXBDemo() {}
    
    public static void main(String[] args) {
	File in = new File("xml/deposits.xml");
	File out = new File("xml/deposits-copy.xml");

	try {
	    JAXBContext jaxbContext = JAXBContext.newInstance(DepositsDB.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	    
	    DepositsDB deposits = (DepositsDB) jaxbUnmarshaller.unmarshal(in);

	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	    jaxbMarshaller.marshal(deposits, out);
	    jaxbMarshaller.marshal(deposits, System.out);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

}
