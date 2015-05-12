package activemq.jmx;

import java.util.List;

import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.testng.annotations.Test;

import activemq.ActiveMQTest;

/**
 * 
 */
public class JmxMonitorTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void listAllTopics() {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        List<TopicViewMBean> topics = monitor.listTopics();

        assert topics.size() > 1 : "No topics found on test broker";

        // for (TopicViewMBean topicView : topics) {
        // System.out.println(topicView.getName() + ".consumerCount:" +
        // topicView.getConsumerCount());
        // System.out.println(topicView.getName() + ".enqueueCount:" +
        // topicView.getEnqueueCount());
        // System.out.println(topicView.getName() + ".dequeueCount:" +
        // topicView.getDequeueCount());
        // }

        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void filterTopics() {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        List<TopicViewMBean> topics = monitor.filterTopics("ActiveMQ.Advisory.Producer.Topic*");
//        for (TopicViewMBean topicView : topics) {
//            System.out.println(topicView.getName() + ".consumerCount:" + topicView.getConsumerCount());
//            System.out.println(topicView.getName() + ".enqueueCount:" + topicView.getEnqueueCount());
//            System.out.println(topicView.getName() + ".dequeueCount:" + topicView.getDequeueCount());
//        }

        assert topics.size() > 1 : "No topics found on test broker";

        List<TopicViewMBean> noTopics = monitor.filterTopics("TestTopics*");
//        for (TopicViewMBean topicView : noTopics) {
//            System.out.println(topicView.getName() + ".consumerCount:" + topicView.getConsumerCount());
//            System.out.println(topicView.getName() + ".enqueueCount:" + topicView.getEnqueueCount());
//            System.out.println(topicView.getName() + ".dequeueCount:" + topicView.getDequeueCount());
//        }

        
        assert noTopics.size() == 0 : "Found topics on test broker";

        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void updatedPublishCount() {

        JmxMonitor monitor = new JmxMonitor(JMX_URL);
        monitor.startup();
        assert monitor.isConnected() : "Monitor is not connected";

        List<TopicViewMBean> topics = monitor.filterTopics("ActiveMQ.Advisory.Producer.Topic*");
//        for (TopicViewMBean topicView : topics) {
//            System.out.println(topicView.getName() + ".consumerCount:" + topicView.getConsumerCount());
//            System.out.println(topicView.getName() + ".enqueueCount:" + topicView.getEnqueueCount());
//            System.out.println(topicView.getName() + ".dequeueCount:" + topicView.getDequeueCount());
//        }

        assert topics.size() > 1 : "No topics found on test broker";

        List<TopicViewMBean> noTopics = monitor.filterTopics("TestTopics*");
//        for (TopicViewMBean topicView : noTopics) {
//            System.out.println(topicView.getName() + ".consumerCount:" + topicView.getConsumerCount());
//            System.out.println(topicView.getName() + ".enqueueCount:" + topicView.getEnqueueCount());
//            System.out.println(topicView.getName() + ".dequeueCount:" + topicView.getDequeueCount());
//        }

        
        assert noTopics.size() == 0 : "Found topics on test broker";

        monitor.shutdown();
    }
}
