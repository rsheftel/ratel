package activemq;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import util.IObserver;

public class ConnectionTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void connectToBrokerDirectlyTest() {

        ConnectionTask connectionTask = new ConnectionTask(BROKER_URL);

        connectionTask.connect();
        assertTrue(connectionTask.isConnected(), "Did not connect to broker");

        connectionTask.stop();
        assertFalse(connectionTask.isConnected(), "Did not disconnect from broker");
    }

    @Test(groups = { "unittest" })
    public void observerTest() {

        ConnectionTask connectionTask = new ConnectionTask(BROKER_URL);
        ConnectionObserver co = new ConnectionObserver();
        connectionTask.addObserver(co);
        
        connectionTask.connect();
        assertTrue(connectionTask.isConnected(), "Did not connect to broker");
        assertNotNull(co.event, "Did not receive ConnectionEvent");
        assertEquals(co.event.getEventType(), ConnectionEventType.Connected);
        
        connectionTask.stop();
        assertFalse(connectionTask.isConnected(), "Did not disconnect from broker");
    }

    
    @Test(groups = { "unittest" })
    public void connectAsTaskTest() {
        ConnectionTask connectionTask = new ConnectionTask(BROKER_URL);
        final ConnectionObserver co = new ConnectionObserver();
        connectionTask.addObserver(co);
        connectionTask.connectAsTask();
        
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return co.connected;
            }
        }, true, 1000);
        
        assertTrue(connectionTask.isConnected(), "Received connection event, but not connected");
        
        connectionTask.stop();
        
        assertFalse(connectionTask.isConnected(), "Still connected after stop");
    }
    
    private final static class ConnectionObserver implements IObserver<ConnectionEvent> {
        private ConnectionEvent event;
        
        boolean connected;

        @Override
        public void onUpdate(ConnectionEvent notificationObject) {
            this.event = notificationObject;
            if (event.getEventType() == ConnectionEventType.Connected) {
                connected = true;
            }
        }
    }
}
