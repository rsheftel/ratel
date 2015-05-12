package activemq;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class JmxConnectionTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void connectToBrokerDirectly() {

        JmxConnectionTask connectionTask = new JmxConnectionTask(JMX_URL);

        connectionTask.connect();

        assertTrue(connectionTask.isConnected(), "Did not connect to broker via JMX");
    }

}
