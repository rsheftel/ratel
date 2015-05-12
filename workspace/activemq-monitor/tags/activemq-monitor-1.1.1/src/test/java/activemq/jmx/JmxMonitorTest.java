package activemq.jmx;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.testng.annotations.Test;

import activemq.AbstractJmsMonitor;
import activemq.ActiveMQTest;
import activemq.IJmsObserver;
import activemq.jms.TopicMonitor;

/**
 * 
 */
public class JmxMonitorTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void listAllTopics() throws Exception {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        AbstractJmsMonitor<IJmsObserver<Message>, Message> topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assert topicMonitor.isConnected() : "TopicMonitor is not connected";

        topicMonitor.publish("UnitTest.Temp.TopicList.Topic", "List me!");
        
        List<TopicViewMBean> topics = monitor.listTopics();

        assert topics.size() > 1 : "No topics found on test broker";

        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void filterTopics() throws Exception {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        AbstractJmsMonitor<IJmsObserver<Message>, Message> topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assert topicMonitor.isConnected() : "TopicMonitor is not connected";
        topicMonitor.publish("UnitTest.Temp.TopicFilter.Topic", "Filter me!");

        List<TopicViewMBean> topics = monitor.filterTopics("ActiveMQ.Advisory.Producer.Topic*");
        assert topics.size() > 1 : "No topics found on test broker";

        List<TopicViewMBean> noTopics = monitor.filterTopics("TestTopics*");
        assert noTopics.size() == 0 : "Found topics on test broker";

        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void updatedPublishCount() throws Exception{

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        AbstractJmsMonitor<IJmsObserver<Message>, Message> topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assert topicMonitor.isConnected() : "TopicMonitor is not connected";

        topicMonitor.publish("UnitTest.Temp.TopicPubishCount.Topic", "Count me!");

        
        List<TopicViewMBean> topics = monitor.filterTopics("ActiveMQ.Advisory.Producer.Topic*");
        assert topics.size() > 1 : "No topics found on test broker";

        List<TopicViewMBean> noTopics = monitor.filterTopics("TestTopics*");
        assert noTopics.size() == 0 : "Found topics on test broker";

        monitor.shutdown();
    }
    
    @Test(groups = { "unittest" })
    public void removeTopicNameTest() throws JMSException {
        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        AbstractJmsMonitor<IJmsObserver<Message>, Message> topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assert topicMonitor.isConnected() : "TopicMonitor is not connected";
        
        int topicCount = monitor.getTopicCount();
        assert 0 < topicCount : "No topics found";
        
        monitor.removeTopic("My.NonExistent.Topic."+ System.nanoTime());
        int newTopicCount = monitor.getTopicCount();
        
        assert topicCount==newTopicCount : "Removed a topic that should not exist";
        assert monitor.getTopicCount() == topicCount : "Removed a topic that should not have existed";
        
        Topic tempTopic = topicMonitor.publish("UnitTest.Temp.Topic.Remove.Test:", "Test Message");
        topicCount = monitor.getTopicCount();
        
        monitor.removeTopic(tempTopic.getTopicName());
        
        newTopicCount = monitor.getTopicCount();
        assert newTopicCount +1 == topicCount : "Removed non or more than the specified topic";
        assert monitor.getTopicCount() == topicCount -1 : "Failed to remove topic";
    }
    
    @Test(groups = { "unittest" })
    public void removeTopicsTest() throws JMSException {
        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        AbstractJmsMonitor<IJmsObserver<Message>, Message> topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assert topicMonitor.isConnected() : "TopicMonitor is not connected";
        
        int topicCount = monitor.getTopicCount();
        assert 0 < topicCount : "No topics found";
        
        assert !monitor.removeTopics("My.NonExistent.Topic."+ System.nanoTime());
        assert monitor.getTopicCount() == topicCount : "Removed a topic that should not have existed";
        
        Topic tempTopic = topicMonitor.publish("UnitTest.Temp.Topic.Remove.Test", "Test Message");
        topicCount = monitor.getTopicCount();
        
        assert monitor.removeTopics(tempTopic.getTopicName());
        assert monitor.getTopicCount() == topicCount -1 : "Failed to remove topic";
    }
    
    @Test(groups = { "unittest" })
    public void wrapperOperationTest() {
        
    }
    
}
