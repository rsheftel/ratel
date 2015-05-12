package activemq;

import org.testng.annotations.Test;

public class JmxConnectionTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void connectToBrokerDirectly() {

        JmxConnectionTask connectionTask = new JmxConnectionTask(JMX_URL);

        connectionTask.connect();

        assert connectionTask.isConnected() : "Did not connect to broker via JMX";
    }

}
