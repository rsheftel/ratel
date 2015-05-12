package activemq.jms;

import static org.testng.Assert.assertTrue;

import javax.jms.Message;

import org.testng.annotations.Test;

import activemq.ActiveMQTest;
import activemq.IJmsObserver;
import activemq.IWaitFor;

public class TopicMonitorTest extends ActiveMQTest {

    private static final class TopicObserver implements IJmsObserver<Message> {
        private int messageCount;

        @Override
        public void onUpdate(Object source, Message message) {
            messageCount++;
        }
    }
    
    @Test(groups = { "unittest" })
    public void monitorTopic() throws Exception {
        
        TopicMonitor monitor = new TopicMonitor(BROKER_URL);
        monitor.startup();
        
        assertTrue(monitor.isConnected(), "Monitor is not connected");
        final TopicObserver observer = new TopicObserver();
        
        monitor.addObserver(observer);
        assertTrue(monitor.observerCount() > 0, "Observer did not get registered");
        
        monitor.listenTo("Topic.Monitor.Test.>");
        assertTrue(1 == monitor.listenerCount(), "Incorrect number of listeners");
        monitor.publish("Topic.Monitor.Test.Message", "Hello");
        
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return observer.messageCount;
            }
        }, 1, 1000);
        
        assertTrue(observer.messageCount > 0, "Failed to receive message");
        
        monitor.removeObserver(observer);
        assertTrue(monitor.observerCount() == 0, "Observer did not un-register");
        
        monitor.stopListeningTo("Topic.Monitor.Test.>");
        
        assertTrue(0 == monitor.listenerCount(), "Still listening to topics");
        
        monitor.shutdown();
    }
}
