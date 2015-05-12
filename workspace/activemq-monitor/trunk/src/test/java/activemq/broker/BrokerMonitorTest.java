package activemq.broker;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import activemq.ActiveMQTest;

public class BrokerMonitorTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void testBrokerMonitorStartup() {
        BrokerConfiguration bc = new BrokerConfiguration(HOST_NAME, TCP_PORT, JMX_PORT);
        
        BrokerMonitor bm = new BrokerMonitor(bc);
        
        bm.startAll();
        
        assertTrue(bm.isConnectedToTopics(), "Failed to connect topic monitoring");
        assertTrue(bm.isConnectedToAdvisory(), "Failed to connect to advisory monitoring");
        assertTrue(bm.isConnectedToJmx(), "Failed to connect to JMX monitoring");
        
        bm.shutdownAll();
    }
}
