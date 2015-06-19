package depositparser;

import deposit.DepositsDB;

import java.io.File;

/**
 * An abstract class for parsing XML files with deposits data.
 */
public abstract class AbstractDepositParser {

    /** An XML file to be parsed. */
    protected File xmlFile;

    /**
     * Instantiates a new deposit parser.
     * @param xmlFile an XML file that contains deposits
     */
    public AbstractDepositParser(File xmlFile) {
	this.xmlFile = xmlFile;
    }

    /**
     * Gets the XML file.
     * @return the XML file
     */
    public File getXmlFile() {
	return xmlFile;
    }

    /**
     * Sets the XML file.
     * @param xmlFile a new XML file
     */
    public void setXmlFile(File xmlFile) {
	this.xmlFile = xmlFile;
    }

    /**
     * Parses the XML file.
     * @return the {@code DepositsDB} object
     */
    public abstract DepositsDB parse();
}
