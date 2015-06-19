package depositparser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import deposit.Deposit;
import deposit.DepositsDB;
import deposit.ISO3166CountryCode;
import deposit.Type;

/**
 * A simple deposits SAX parser.
 */
public class DepositSAXParser extends AbstractDepositParser {
    
    /** The logger. */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Instantiates a new deposit SAX parser.
     * @param xmlFile an XML file to be parsed
     */
    public DepositSAXParser(File xmlFile) {
        super(xmlFile);
    }

    /* (non-Javadoc)
     * @see depositparser.DepositParser#parse()
     */
    @Override
    public DepositsDB parse() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        
        try {
            SAXParser sp = spf.newSAXParser();
            XMLReader xmlReader = sp.getXMLReader();
            DepositContentHandler dch = new DepositContentHandler();
            
            xmlReader.setContentHandler(dch);
            xmlReader.parse(xmlFile.getAbsolutePath());

            return dch.getDepositsDB();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error(e);
        }

        return null;
    }
}

/**
 * A deposit content handler for deposit SAX parser.
 */
class DepositContentHandler extends DefaultHandler {
    
    /** Types of content (used for proper parsing of characters content) */
    private enum ContentType {
        NONE, DEPOSITOR, ACCOUNTID, AMOUNT, INTEREST, TIME
    }

    /** A deposit. */
    private Deposit deposit;
    
    /** A content type flag. */
    private ContentType flag = ContentType.NONE;
    
    /** A {@code DepositsDB} object. */
    private DepositsDB depositsDB;

    /**
     * Instantiates a new deposit content handler.
     */
    public DepositContentHandler() {
        this.depositsDB = new DepositsDB();
    }

    /**
     * Gets the {@code DepositsDB} object.
     * @return the {@code DepositsDB} object.
     */
    public DepositsDB getDepositsDB() {
        return depositsDB;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes)
            throws SAXException {
        switch (qName) {
        case "deposit":
            deposit = new Deposit();
            deposit.setId(attributes.getValue("id"));
            deposit.setName(attributes.getValue("bank:name"));
            deposit.setType(Type.fromValue(attributes.getValue("type")));
            deposit.setCountry(ISO3166CountryCode.fromValue(
                    attributes.getValue("country")));
            break;
        case "depositor":
            flag = ContentType.DEPOSITOR;
            break;
        case "account-id":
            flag = ContentType.ACCOUNTID;
            break;
        case "fin:amount-on-deposit":
            flag = ContentType.AMOUNT;
            break;
        case "fin:interest":
            flag = ContentType.INTEREST;
            break;
        case "time-constraint":
            flag = ContentType.TIME;
            break;
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("deposit".equals(qName)) {
            depositsDB.getDeposit().add(deposit);
        }
        
        flag = ContentType.NONE;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        String str = new String(ch, start, length).trim();

        switch (flag) {
        case DEPOSITOR:
            deposit.setDepositor(str);
            break;
        case ACCOUNTID:
            deposit.setAccountId(Integer.parseInt(str));
            break;
        case AMOUNT:
            deposit.setAmountOnDeposit(new BigDecimal(str));
            break;
        case INTEREST:
            deposit.setInterest(new BigDecimal(str));
            break;
        case TIME:
            deposit.setTimeConstraint(Long.parseLong(str));
            break;
        default:
        }
    }
}
