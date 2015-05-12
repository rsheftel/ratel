package activemq;

import static util.Monitoring.sleep;

import org.apache.activemq.broker.BrokerService;
import org.testng.TestException;
import org.testng.annotations.BeforeSuite;

public class ActiveMQTest {

    private static final int TCP_PORT = 60606;
    private static final int JMX_PORT = 11099;
    
    protected static final String BROKER_URL = "tcp://localhost:"+TCP_PORT;
    protected static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:"+JMX_PORT+"/jmxrmi";

    public ActiveMQTest() {
        super();
    }

    @BeforeSuite(groups = { "unittest" })
    protected void init() {
    
        try {
            BrokerService broker = new BrokerService();
            // configure the broker
            broker.setBrokerName("UnitTestBroker");
            broker.addConnector(BROKER_URL);
            broker.setUseJmx(true);
            broker.getManagementContext().setConnectorPort(JMX_PORT);
            broker.start();
            
        } catch (Exception e) {
            throw new TestException("Unable to start embedded broker");
        }
    }

    protected void waitForValue(IWaitFor<Integer> waitingFor, int expectedValue) {
        waitForValue(waitingFor, expectedValue, 1000);
    }

    protected void waitForValue(IWaitFor<Integer> waitingFor, int expectedValue, long waitDuration) {
        long startedWaitingAt = System.currentTimeMillis();
    
        while (waitingFor.waitFor() <= expectedValue
                && (startedWaitingAt + waitDuration > System.currentTimeMillis())) {
            sleep(10);
        }
    }

    protected void waitForValue(IWaitFor<Boolean> waitingFor, boolean expectedValue, long waitDuration) {
        long startedWaitingAt = System.currentTimeMillis();
    
        while (waitingFor.waitFor() != expectedValue
                && (startedWaitingAt + waitDuration > System.currentTimeMillis())) {
            sleep(10);
        }
    }

    protected void sleepFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}