package depositprocessor;

/**
 * A Deposit Processor demo class.
 */
public class DepositProcessorDemo {
    
    private DepositProcessorDemo() {}

    public static void main(String[] args) {
        DepositProcessor dp = new DepositProcessor();
        
        dp.setSchema("xml/xsd/deposits.xsd");
        dp.setStylesheet("xml/xsl/deposits.xsl");
        dp.open("xml/deposits.xml");

        if (!dp.validate()) {
            System.out.println("XML validation failed");
            return;
        }

        System.out.println("XML validation passed");
        dp.readDeposits(DepositProcessor.STAX);
        System.out.println("Sorted by depositor name: \n");
        dp.sortDeposits(DepositProcessor.DEPOSITOR);
        dp.printDeposits();

        dp.generateHTML();
    }
}
