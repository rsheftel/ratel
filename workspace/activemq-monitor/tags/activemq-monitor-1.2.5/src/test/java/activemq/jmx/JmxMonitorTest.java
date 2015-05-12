package activemq.jmx;

import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Topic;

import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.testng.annotations.Test;

import util.IObserver;
import util.Monitoring;
import activemq.ActiveMQTest;
import activemq.jms.TopicMonitor;

import static org.testng.Assert.*;

/**
 * 
 */
public class JmxMonitorTest extends ActiveMQTest {

    private static final class TopicListFeedback implements IObserver<Integer> {
        int lastUpdate;

        @Override
        public void onUpdate(Integer progress) {
           if (lastUpdate != progress) {
                lastUpdate = progress;
            }
        }
    }

    @Test(groups = { "unittest", "debug" })
    public void listAllTopics() throws Exception {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assertTrue(monitor.isConnected(), "Monitor is not connected");

        TopicMonitor topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assertTrue(topicMonitor.isConnected(), "TopicMonitor is not connected");

        topicMonitor.publish("UnitTest.Temp.TopicList.Topic", "List me!");

        List<DestinationViewMBean> topics = monitor.listTopics();

        assertTrue(topics.size() > 1, "No topics found on test broker");

        // List the topics with a callback for progress
        TopicListFeedback feedback = new TopicListFeedback();
        List<DestinationViewMBean> topicsWithFeedback = monitor.listTopics(feedback);
        
        assertTrue(topicsWithFeedback.size() > 0, "No topics found");
        assertTrue(feedback.lastUpdate == 100, "Failed to receive feedback");
        
        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void filterTopics() throws Exception {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assertTrue(monitor.isConnected(), "Monitor is not connected");

        TopicMonitor topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assertTrue(topicMonitor.isConnected(), "TopicMonitor is not connected");
        topicMonitor.publish("UnitTest.Temp.TopicFilter.Topic", "Filter me!");

        List<TopicViewMBean> topics = monitor.filterTopics("ActiveMQ.Advisory.Producer.Topic*");
        assertTrue(topics.size() > 1, "No topics found on test broker");

        List<TopicViewMBean> noTopics = monitor.filterTopics("TestTopics*");
        assertTrue(noTopics.size() == 0, "Found topics on test broker");

        monitor.shutdown();
    }

    @Test(groups = { "delete-topics" })
    public void deleteTopics() throws Exception {
/*
        //JmxMonitor monitor = new JmxMonitor(JMX_URL);
        JmxMonitor monitor = new JmxMonitor("service:jmx:rmi:///jndi/rmi://nysrv61:9393/jmxrmi");
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        List<TopicViewMBean> topics = monitor.filterTopics("UnitTest.*");
        assert topics.size() > 1 : "No topics found on test broker";
System.out.println("Topic count=" + topics.size());
        for (TopicViewMBean bean : topics) {
            monitor.removeTopic(bean.getName());
        }
        monitor.shutdown();
        */
    }
    
    @Test(groups = { "unittest" })
    public void updatedPublishCount() throws Exception {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assertTrue(monitor.isConnected(), "Monitor is not connected");

        TopicMonitor topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assertTrue(topicMonitor.isConnected(), "TopicMonitor is not connected");

        topicMonitor.publish("UnitTest.Temp.TopicPubishCount.Topic", "Count me!");

        List<TopicViewMBean> topics = monitor.filterTopics("ActiveMQ.Advisory.Producer.Topic*");
        assertTrue(topics.size() > 1, "No topics found on test broker");

        List<TopicViewMBean> noTopics = monitor.filterTopics("TestTopics*");
        assertTrue(noTopics.size() == 0, "Found topics on test broker");

        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void removeTopicNameTest() throws JMSException {
        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assertTrue(monitor.isConnected(), "Monitor is not connected");

        TopicMonitor topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assertTrue(topicMonitor.isConnected(), "TopicMonitor is not connected");

        int topicCount = monitor.getTopicCount();
        assertTrue(0 < topicCount, "No topics found");

        monitor.removeTopic("My.NonExistent.Topic." + System.nanoTime());
        int newTopicCount = monitor.getTopicCount();

        assertTrue(topicCount == newTopicCount, "Removed a topic that should not exist");
        assertTrue(monitor.getTopicCount() == topicCount, "Removed a topic that should not have existed");

        Topic tempTopic = topicMonitor.publish("UnitTest.Temp.Topic.Remove.Test:", "Test Message");
        topicCount = monitor.getTopicCount();

        monitor.removeTopic(tempTopic.getTopicName());

        newTopicCount = monitor.getTopicCount();
        assertTrue(newTopicCount + 1 == topicCount, "Removed non or more than the specified topic");
        assertTrue(monitor.getTopicCount() == topicCount - 1, "Failed to remove topic");
        
        monitor.shutdown();
        System.out.println("Finished removeTopicNameTest");
    }

    @Test(groups = { "unittest" })
    public void removeTopicsTest() throws JMSException {
        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assertTrue(monitor.isConnected(), "Monitor is not connected");

        TopicMonitor topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();
        assertTrue(topicMonitor.isConnected(), "TopicMonitor is not connected");

        int topicCount = monitor.getTopicCount();
        assertTrue(0 < topicCount, "No topics found");

        assertTrue(!monitor.removeTopics("My.NonExistent.Topic." + System.nanoTime()), "Huh?");
        assertTrue(monitor.getTopicCount() == topicCount, "Removed a topic that should not have existed");

        Topic tempTopic = topicMonitor.publish("UnitTest.Temp.Topic.Remove.Test", "Test Message");
        topicCount = monitor.getTopicCount();

        assertTrue(monitor.removeTopics(tempTopic.getTopicName()), "Failed to remove topic");
        assertTrue(monitor.getTopicCount() == topicCount - 1, "Failed to remove topic");
        
        monitor.shutdown();
    }

    /**
     * This test can only be run when the JMX server is brought up completely, otherwise it will fail, as the
     * memory module is not loaded.
     */
    @Test(groups = { "unittest-eclipse" })
    public void memoryManagementTest() {
        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        MemoryUsage memUsage = null;
        try {
            memUsage = monitor.getHeapMemoryUsage();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assert memUsage != null : "Failed to get heap memory usage";
        // System.out.println(memUsage.getMax());
        System.out.println(memUsage.getUsed());

        long initialUsed = memUsage.getUsed();

        assert memUsage.getMax() > memUsage.getUsed() : "Using more memory than allocated";

        List<String> testList = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            testList.add("Test object: " + i);
        }

        memUsage = monitor.getHeapMemoryUsage();

        // System.out.println(memUsage.getMax());
        System.out.println(memUsage.getUsed());

        long allocatedUsed = memUsage.getUsed();

        Monitoring.sleep(10000);
        // memUsage = monitor.getHeapMemoryUsage();
        // System.out.println(memUsage.getMax());
        System.out.println(memUsage.getUsed());

        assert allocatedUsed > initialUsed : "Used memory did not change after allocation";

        monitor.shutdown();

    }

    @Test(groups = { "unittest-eclipse" })
    public void threadCountTest() {
        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        ThreadCountInfo threadCountInfo = null;
        try {
            threadCountInfo = monitor.getThreadCountInfo();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        assert threadCountInfo != null : "Unable to get thread counts";
        
        assert threadCountInfo.peakThreadCount >= threadCountInfo.threadCount : "Peak is less than current";
        
    }

}
