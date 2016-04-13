package depositparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import deposit.Deposit;
import deposit.DepositsDB;
import deposit.ISO3166CountryCode;
import deposit.Type;

/**
 * A simple deposits StAX parser.
 */
public class DepositStAXParser extends AbstractDepositParser {
    
    /** The logger. */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /** Types of content (used for proper parsing of characters content) */
    private enum ContentType {
        NONE, DEPOSITOR, ACCOUNTID, AMOUNT, INTEREST, TIME
    }

    /**
     * Instantiates a new deposit stax parser.
     * @param xmlFile the XML file
     */
    public DepositStAXParser(File xmlFile) {
        super(xmlFile);
    }

    /* (non-Javadoc)
     * @see depositparser.DepositParser#parse()
     */
    @Override
    public DepositsDB parse() {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

        try {
            XMLEventReader reader = inputFactory.createXMLEventReader(
                    new FileReader(xmlFile));
            XMLEvent event;

            String bankNS = "www.example.org/xmlns/bank";
            String finNS = "www.example.org/xmlns/financial";

            DepositsDB depositsDB = new DepositsDB();
            Deposit deposit = null;
            
            // get reference to the empty deposits list
            List<Deposit> list = depositsDB.getDeposits();
            
            ContentType flag = ContentType.NONE;

            QName qnId = new QName("id");
            QName qnName = new QName(bankNS, "name", "bank");
            QName qnType = new QName("type");
            QName qnCountry = new QName("country");

            String qName;
            String ns;

            while (reader.hasNext()) {
                event = reader.nextEvent();

                switch (event.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    StartElement se = event.asStartElement();
                    qName = se.getName().getLocalPart();
                    ns = se.getName().getNamespaceURI();

                    switch (qName) {
                    case "deposit":
                        deposit = new Deposit();
                        deposit.setId(se.getAttributeByName(qnId).getValue());
                        deposit.setName(se.getAttributeByName(qnName).getValue());
                        deposit.setType(Type.fromValue(
                                se.getAttributeByName(qnType).getValue()));
                        deposit.setCountry(ISO3166CountryCode.fromValue(
                                se.getAttributeByName(qnCountry).getValue()));
                        break;
                    case "depositor":
                        flag = ContentType.DEPOSITOR;
                        break;
                    case "account-id":
                        flag = ContentType.ACCOUNTID;
                        break;
                    case "amount-on-deposit":
                        if (ns.equals(finNS)) {
                            flag = ContentType.AMOUNT;
                        }

                        break;
                    case "interest":
                        if (ns.equals(finNS)) {
                            flag = ContentType.INTEREST;
                        }

                        break;
                    case "time-constraint":
                        flag = ContentType.TIME;
                        break;
                    }

                    break;
                case XMLEvent.END_ELEMENT:
                    EndElement ee = event.asEndElement();
                    qName = ee.getName().toString();

                    if ("deposit".equals(qName) && (deposit != null)) {
                        list.add(deposit);
                    }
                 
                    flag = ContentType.NONE;
                    break;
                case XMLEvent.CHARACTERS:
                    Characters ch = event.asCharacters();

                    switch (flag) {
                    case DEPOSITOR:
                        deposit.setDepositor(ch.getData().trim());
                        break;
                    case ACCOUNTID:
                        deposit.setAccountId(
                        	Integer.parseInt(ch.getData().trim()));
                        break;
                    case AMOUNT:
                        deposit.setAmountOnDeposit(
                        	new BigDecimal(ch.getData().trim()));
                        break;
                    case INTEREST:
                        deposit.setInterest(
                        	new BigDecimal(ch.getData().trim()));
                        break;
                    case TIME:
                        deposit.setTimeConstraint(
                        	Long.parseLong(ch.getData().trim()));
                        break;
                    default:
                    }
                }
            }

            return depositsDB;

        } catch (XMLStreamException | FileNotFoundException e) {
            LOGGER.error(e);
        }

        return null;
    }
}
