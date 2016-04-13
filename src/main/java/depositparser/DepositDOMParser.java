package depositparser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import deposit.Deposit;
import deposit.DepositsDB;
import deposit.ISO3166CountryCode;
import deposit.Type;

/**
 * A simple DOM parser for parsing a deposits XML file.
 */
public class DepositDOMParser extends AbstractDepositParser {
    
    /** The logger. */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Instantiates a new deposit DOM parser.
     * @param xmlFile an XML to be parsed
     */
    public DepositDOMParser(File xmlFile) {
        super(xmlFile);
    }

    /* (non-Javadoc)
     * @see depositparser.DepositParser#parse()
     */
    @Override
    public DepositsDB parse() {
        DepositsDB depositsDB = new DepositsDB();
        
        // get reference to the empty deposits list
        List<Deposit> list = depositsDB.getDeposits();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            NodeList depositNodes = doc.getElementsByTagName("deposit");
            Element depositElement;
            Deposit deposit;
            
            String id;
            String type;
            String name;
            String country;
            String depositor;
            String accountId;
            String amountOnDeposit;
            String interest;
            String timeConstraint;

            // traverse all 'deposit' nodes
            for (int i = 0, endi = depositNodes.getLength(); i < endi; i++) {
                deposit = new Deposit();
                depositElement = (Element) depositNodes.item(i);

                id = depositElement.getAttributes()
                        .getNamedItem("id").getTextContent().trim();
                type = depositElement.getAttributes()
                        .getNamedItem("type").getTextContent().trim();
                name = depositElement.getAttributes()
                        .getNamedItem("bank:name").getTextContent().trim();
                country = depositElement.getAttributes()
                        .getNamedItem("country").getTextContent().trim();
                depositor = depositElement.getElementsByTagName("depositor")
                	.item(0).getTextContent().trim();
                accountId = depositElement.getElementsByTagName("account-id")
                	.item(0).getTextContent().trim();
                amountOnDeposit = depositElement.getElementsByTagName("fin:amount-on-deposit")
                	.item(0).getTextContent().trim();
                interest = depositElement.getElementsByTagName("fin:interest")
                	.item(0).getTextContent().trim();
                timeConstraint = depositElement.getElementsByTagName("time-constraint")
                	.item(0).getTextContent().trim();
                
                deposit.setId(id);
                deposit.setType(Type.fromValue(type));
                deposit.setName(name);
                deposit.setCountry(ISO3166CountryCode.fromValue(country));
                deposit.setDepositor(depositor);
                deposit.setAccountId(Integer.parseInt(accountId));
                deposit.setAmountOnDeposit(new BigDecimal(amountOnDeposit));
                deposit.setInterest(new BigDecimal(interest));
                deposit.setTimeConstraint(Long.parseLong(timeConstraint));

                list.add(deposit);
            }

            return depositsDB;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error(e);
        }
        
        return null;
    }
}
