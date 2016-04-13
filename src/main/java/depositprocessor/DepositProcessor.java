package depositprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import deposit.Deposit;
import deposit.DepositsDB;
import depositparser.DepositDOMParser;
import depositparser.DepositSAXParser;
import depositparser.DepositStAXParser;

/**
 * A class that provides functionality for processing XML files that contain
 * deposits information. The included methods allow loading, validating,
 * parsing and transforming such XML files. Moreover, there are methods for
 * sorting and printing of deposits list contained 
 * in a {@code DepositDB} object.
 */
public class DepositProcessor {
    
    /** The logger. */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /** The SAX parser option. */
    public static final byte SAX = 0;
    
    /** The DOM parser option. */
    public static final byte DOM = 1;
    
    /** The StAX parser option. */
    public static final byte STAX = 2;

    /** The 'sort by depositor' option. */
    public static final byte DEPOSITOR = 100;
    
    /** The 'sort by amount' option. */
    public static final byte AMOUNT = 101;
    
    /** The 'sort by time constraint' option. */
    public static final byte TIME = 102;

    /** An XML file. */
    private File xmlFile;
    
    /** An XSD schema file. */
    private File schemaFile;
    
    /** An XSL stylesheet file. */
    private File stylesheetFile;

    /** A deposits db (deposits list). */
    private DepositsDB depositsDB;

    /**
     * Instantiates a new deposit processor.
     */
    public DepositProcessor() {}

    /**
     * Set the XSD file to be used for validation.
     * @param pathToXsd Path to XSD file.
     */
    public void setSchema(String pathToXsd) {
        this.schemaFile = new File(pathToXsd);

        if (!schemaFile.exists()) {
            LOGGER.error("Could not set schema: no schema file found.");
        }
    }

    /**
     * Set XSL file to be used for transformation.
     * @param pathToXsl path to XSL file.
     */
    public void setStylesheet(String pathToXsl) {
        this.stylesheetFile = new File(pathToXsl);

        if (!stylesheetFile.exists()) {
            LOGGER.error(
                    "Could not set stylesheet: no stylesheet file found.");
        }
    }

    /**
     * Open an XML file.
     * @param pathToXML path to an XML file.
     */
    public void open(String pathToXML) {
        this.xmlFile = new File(pathToXML);

        if (!xmlFile.exists()) {
            LOGGER.error("Could not open: no xml file found.");
        }
    }

    /**
     * Validate the XML file.
     * @return returns true if XML file validates against the schema.
     */
    public boolean validate() {
        if (xmlFile == null) {
            LOGGER.error("Could not validate: no xml file opened.");
            return false;
        }

        if (schemaFile == null) {
            LOGGER.error("Could not validate: no schema is set.");
            return false;
        }

        Source source = new StreamSource(xmlFile);

        SchemaFactory sf = SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        try {
            Schema schema = sf.newSchema(schemaFile);
            Validator validator = schema.newValidator();

            validator.validate(source);
        } catch (SAXException | IOException e) {
            LOGGER.error(e);
            return false;
        }

        return true;
    }

    /**
     * Read all deposit entries from the XML file.
     * @param parserType type of parser to be used (SAX, DOM or StAX).
     */
    public void readDeposits(byte parserType) {
        if (xmlFile == null) {
            LOGGER.error("Could not read: no xml file opened.");
            return;
        }

        switch (parserType) {
        case SAX:
            depositsDB = new DepositSAXParser(xmlFile).parse();
            break;
        case DOM:
            depositsDB = new DepositDOMParser(xmlFile).parse();
            break;
        case STAX:
            depositsDB = new DepositStAXParser(xmlFile).parse();
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid parser choice parameter");
        }
    }

    /**
     * Print loaded deposits to the console.
     */
    public void printDeposits() {
        if (depositsDB == null) {
            System.out.println("Could not print: No deposits loaded.");
            return;
        }

        depositsDB.getDeposits().stream()
                .forEach(System.out::println);
    }

    /**
     * Sort deposits contained in the {@code DepositsDB} object.
     * @param how sorting key (depositor's name, amount on account, etc.)
     */
    public void sortDeposits(byte how) {
        if (depositsDB == null) {
            System.out.println("Could not sort: no deposits loaded.");
            return;
        }

        switch (how) {
        case DEPOSITOR:
            Collections.sort(depositsDB.getDeposits(),
                             (Deposit a, Deposit b) 
                             -> a.getDepositor().compareTo(b.getDepositor()));
            break;
        case AMOUNT:
            Collections.sort(depositsDB.getDeposits(),
                             (Deposit a, Deposit b) 
                             -> a.getAmountOnDeposit()
                             .compareTo(b.getAmountOnDeposit()));
            break;
        case TIME:
            Collections.sort(depositsDB.getDeposits(),
                             (Deposit a, Deposit b) 
                             -> Long.compare(a.getTimeConstraint(), 
                        	     b.getTimeConstraint()));
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid sorting choice parameter");
        }
    }

    /**
     * Transform the XML file to an HTML file using XSL.
     */
    public void generateHTML() {
        if (stylesheetFile == null) {
            LOGGER.error("Could not output: no stylesheet loaded.");
            return;
        }

        File output = new File("xml/deposits.html");
        StreamSource xmlSource = new StreamSource(xmlFile);
        StreamSource xslSource = new StreamSource(stylesheetFile);
        TransformerFactory tf = TransformerFactory.newInstance();
        
        tf.setAttribute("indent-number", 4);

        try {
            Transformer t = tf.newTransformer(xslSource);
            
            // using just file doesn't produce indents in HTML
	    Result result = new StreamResult(new OutputStreamWriter(
		    new FileOutputStream(output), "UTF-8"));
            
            t.transform(xmlSource, result);
        } catch (TransformerException | UnsupportedEncodingException 
        	| FileNotFoundException e) {
	    LOGGER.error(e);
	}
    }
}
