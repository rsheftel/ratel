package activemq.advisory;

import org.apache.activemq.command.ActiveMQMessage;
import org.testng.annotations.Test;

import activemq.IJmsObserver;
import activemq.IWaitFor;

public class AdvisoryMessageObserverTest extends AdvisoryMonitorTest {

    private static final class AMObserver implements IJmsObserver<ActiveMQMessage> {
        private int messageCount;

        @Override
        public void onUpdate(ActiveMQMessage message) {
            messageCount++;
        }
    }

    @Test(groups = { "unittest" })
    public void observeProducers() {
        monitor.setMonitorProducer(true);
        monitor.startup();

        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.observerCount() : "Found observers on test broker";

        final AMObserver observer = new AMObserver();
        monitor.addObserver(observer);

        assert 1 == monitor.observerCount() : "No observer found for producer";

        monitor.createTempTopicProducer();
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return observer.messageCount;
            }
        }, 1000);

        assert 1 <= observer.messageCount : "Did not receive message";

        monitor.removeObserver(observer);

        assert 0 == monitor.observerCount() : "Observer found";
        sleepFor(1000);  // try to get all the messages off the topics
        monitor.shutdown();
        monitor.setMonitorProducer(false);
        monitor.clearLists();
    }

    @Test(groups = { "unittest" })
    public void observeConsumers() {
        monitor.setMonitorConsumer(true);
        monitor.startup();

        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.observerCount() : "Found observers on test broker";

        final AMObserver observer = new AMObserver();
        monitor.addObserver(observer);

        assert 1 == monitor.observerCount() : "No observer found for consumer";

        monitor.createTempTopicConsumer();
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return observer.messageCount;
            }
        }, 1000);

        assert 1 <= observer.messageCount : "Did not receive message";

        monitor.removeObserver(observer);

        assert 0 == monitor.observerCount() : "Observer found";
        monitor.shutdown();
        monitor.setMonitorConsumer(false);
    }
}
