package activemq.advisory;

import org.testng.annotations.Test;

import activemq.IWaitFor;

public class ListProducerConsumerTest extends AdvisoryMonitorTest {

    public ListProducerConsumerTest() {
        super();
    }
    
    @Test(groups = { "unittest" })
    public void listBrokerProducers() {

        monitor.startup();
        
        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.listProducers().size() : "Found producers on test broker: " + monitor.listProducers().size();

        monitor.createTempTopicProducer();
        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listProducers().size();
            }

        }, 1000);
        assert 1 == monitor.listProducers().size() : "No producer found on broker";

        monitor.createTempTopicProducer();

        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listProducers().size();
            }

        }, 2, 1000);
        assert 2 == monitor.listProducers().size() : "Expecting to have 2 producers";
        
        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void listBrokerConsumers() {

        monitor.startup();

        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.listConsumers().size() : "Found consumers on test broker";

        monitor.createTempTopicConsumer();
        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listConsumers().size();
            }

        }, 1000);
        assert 1 == monitor.listConsumers().size() : "No consumer found on broker";

        monitor.createTempTopicConsumer();

        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listConsumers().size();
            }

        }, 2, 1000);
        assert 2 == monitor.listConsumers().size() : "Expecting to have 2 consumers";
        
        monitor.shutdown();
    }
    
    @Test(groups = { "unittest-todo" })
    public void listDestinations() {

        monitor.startup();

        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.listDestinations().size() : "Found destinations on test broker";

        
        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listDestinations().size();
            }

        }, 1000);
        assert 1 == monitor.listDestinations().size() : "No destinations found on broker";

        monitor.createTempTopicConsumer();

        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listDestinations().size();
            }

        }, 2, 1000);
        assert 2 == monitor.listDestinations().size() : "Expecting to have 2 destinations";
        
        monitor.shutdown();
    }
    
    @Test(groups = { "unittest-todo" })
    public void listTopicsWithoutConsumers() {

        monitor.startup();

        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.listNoConsumerTopics().size() : "Found topics without consumers";

        monitor.createTempTopicProducer();
        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listNoConsumerTopics().size();
            }

        }, 1000);
        assert 1 == monitor.listNoConsumerTopics().size() : "No topics without consumer found on broker";

        monitor.createTempTopicConsumer();

        waitForValue(new IWaitFor() {
            @Override
            public int waitFor() {
                return monitor.listNoConsumerTopics().size();
            }

        }, 2, 1000);
        assert 2 == monitor.listNoConsumerTopics().size() : "Expecting to have 2 topics";
        
        monitor.shutdown();
    }
}
