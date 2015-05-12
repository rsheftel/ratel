package activemq.advisory;

import org.testng.annotations.Test;

import activemq.IWaitFor;

public class ListAdvisoryMessageTest extends AdvisoryMonitorTest {

    public ListAdvisoryMessageTest() {
        super();
    }
    
    @Test(groups = { "unittest" })
    public void listBrokerProducers() {
        monitor.setMonitorProducer(true);
        monitor.startup();
        
        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.listProducers().size() : "Found producers on test broker: " + monitor.listProducers().size();

        monitor.createTempTopicProducer();
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.listProducers().size();
            }

        }, 1000);
        assert 1 == monitor.listProducers().size() : "No producer found on broker";

        monitor.createTempTopicProducer();

        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.listProducers().size();
            }

        }, 2, 1000);
        assert 2 == monitor.listProducers().size() : "Expecting to have 2 producers";
        
        monitor.shutdown();
        monitor.setMonitorProducer(false);
        monitor.clearLists();
    }

    @Test(groups = { "unittest" })
    public void listBrokerConsumers() {
        monitor.setMonitorConsumer(true);
        monitor.startup();
        

        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.listConsumers().size() : "Found consumers on test broker";

        monitor.createTempTopicConsumer();
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.listConsumers().size();
            }

        }, 1, 1000);

        assert 1 <= monitor.listConsumers().size() : "No consumer found on broker";

        monitor.createTempTopicConsumer();

        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.listConsumers().size();
            }

        }, 2, 1000);
        assert 2 <= monitor.listConsumers().size() : "Expecting to have 2 consumers";
        
        monitor.shutdown();
        monitor.clearLists();
        monitor.setMonitorConsumer(false);
    }
    
    @Test(groups = { "unittest" })
    public void listDestinations() throws Exception {
        monitor.setMonitorDestinations(true);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        int startingCount = monitor.listDestinations().size();
        monitor.publish("NewTestTopicTest", "Test message on test topic");
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.listDestinations().size();
            }

        }, 1, 10000);
        
        assert startingCount < monitor.listDestinations().size() : "No destinations found on broker";
        sleepFor(1000);
        monitor.shutdown();
        monitor.setMonitorDestinations(false);
        monitor.clearLists();
    }
    
    @Test(groups = { "unittest-todo" })
    public void listTopicsWithoutConsumers() {

        monitor.startup();

        assert monitor.isConnected() : "Monitor is not connected";
        assert 0 == monitor.listNoConsumerTopics().size() : "Found topics without consumers";

        monitor.createTempTopicProducer();
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.listNoConsumerTopics().size();
            }

        }, 1000);
        assert 1 == monitor.listNoConsumerTopics().size() : "No topics without consumer found on broker";

        monitor.createTempTopicConsumer();

        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.listNoConsumerTopics().size();
            }

        }, 2, 1000);
        assert 2 == monitor.listNoConsumerTopics().size() : "Expecting to have 2 topics";
        
        monitor.shutdown();
    }
   
}
