package activemq;

import static util.Monitoring.sleep;

import org.apache.activemq.broker.BrokerService;
import org.testng.TestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class ActiveMQTest {

    protected static final int TCP_PORT = 60616;
    protected static final int JMX_PORT = 11199;

    protected static final String HOST_NAME = "localhost";
    
    protected static final String BROKER_URL = "tcp://" + HOST_NAME +":" + TCP_PORT;
    protected static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://"+HOST_NAME+":" + JMX_PORT + "/jmxrmi";

    private BrokerService broker;

    public ActiveMQTest() {
        super();
    }

    @BeforeClass(groups = { "unittest" })
    protected void init() {

        System.setProperty("com.sun.management.jmxremote.port", String.valueOf(JMX_PORT));
        try {
            broker = new BrokerService();
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

    @AfterClass(groups = { "unittest" })
    public void shutdown() {
        try {
            System.out.println("Stopping broker");
            broker.stop();
        } catch (Exception e) {
            throw new TestException("Unable to stop embedded broker");
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