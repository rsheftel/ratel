package activemq;

import org.testng.annotations.Test;

public class ConnectionTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void connectToBrokerDirectly() {

        ConnectionTask connectionTask = new ConnectionTask(BROKER_URL);

        connectionTask.connect();
        assert connectionTask.isConnected() : "Did not connect to broker";
        
        connectionTask.stop();
        assert !connectionTask.isConnected() : "Did not disconnect from broker";
        
    }

}
