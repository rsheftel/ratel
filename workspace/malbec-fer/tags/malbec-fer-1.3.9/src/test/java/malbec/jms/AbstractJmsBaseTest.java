package malbec.jms;

import malbec.AbstractBaseTest;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.LastImageSubscriptionRecoveryPolicy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.testng.TestException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * A new base class for the JMS tests.  
 * 
 */
public class AbstractJmsBaseTest extends AbstractBaseTest {

    protected static final String BROKER_URL = "vm://localhost";

    private BrokerService broker;
    
    @BeforeMethod(groups = { "unittest-new" })
    public void init() {

        System.err.println("Starting embeded broker");
        try {
            broker = new BrokerService();
            // configure the broker
            broker.setBrokerName("UnitTestBroker");
            broker.addConnector(BROKER_URL);
            broker.setUseJmx(true);
            // Since I cannot figure out how to turn this off, just delete everything
            broker.getPersistenceAdapter().deleteAllMessages();
            broker.setDestinationPolicy(new PolicyMap());

            PolicyEntry pe = new PolicyEntry();
            pe.setSubscriptionRecoveryPolicy(new LastImageSubscriptionRecoveryPolicy());
            broker.getDestinationPolicy().setDefaultEntry(new PolicyEntry());

            broker.start();
            System.err.println("Started embeded broker");
        } catch (Exception e) {
            throw new TestException("Unable to start embedded broker. " + e.getMessage());
        }
    }

    @AfterMethod(groups = { "unittest-new" })
    public void shutdown() {
        System.err.println("Stopping embeded broker");
        try {
            broker.stop();
            System.err.println("Stopped embeded broker");
        } catch (Exception e) {
            throw new TestException("Unable to stop embedded broker");
        }
    }

}
