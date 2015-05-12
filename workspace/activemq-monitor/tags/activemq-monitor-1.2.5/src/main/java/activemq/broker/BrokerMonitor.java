package activemq.broker;

import java.util.List;

import javax.jms.Message;

import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.apache.activemq.command.ActiveMQMessage;

import util.IObserver;
import activemq.IJmsObserver;
import activemq.advisory.AdvisoryMessageMonitor;
import activemq.jms.TopicMonitor;
import activemq.jmx.JmxMonitor;

public class BrokerMonitor {

    private BrokerConfiguration config;

    private TopicMonitor topicMonitor;

    private JmxMonitor jmxMonitor;

    private AdvisoryMessageMonitor advisoryMonitor;

    private int topicCount; 
    
    private boolean started;

    public BrokerMonitor(BrokerConfiguration configuration) {
        this.config = configuration;
        jmxMonitor = new JmxMonitor(config.getJmxUrl());

        topicMonitor = new TopicMonitor(config.getJmsUrl());
        // share the JMS connection
        advisoryMonitor = new AdvisoryMessageMonitor(topicMonitor.getConnectionTask());
        // only interested in new destinations
        advisoryMonitor.setMonitorDestinations(true);
    }

    public BrokerConfiguration getConfig() {
        return config;
    }

    public synchronized void startAll() {
        if (!started) {
            jmxMonitor.startup();
            topicMonitor.startup();
            advisoryMonitor.startup();
            started = true;
        }
    }

    public synchronized void shutdownAll() {
        jmxMonitor.shutdown();
        topicMonitor.shutdown();
        advisoryMonitor.shutdown();

        started = false;
    }

    public boolean isConnectedToTopics() {
        return topicMonitor.isConnected();
    }

    public boolean isConnectedToAdvisory() {
        return advisoryMonitor.isConnected();
    }

    public boolean isConnectedToJmx() {
        return jmxMonitor.isConnected();
    }

    public String getServerName() {
        return config.getServerName();
    }

    public synchronized boolean isStarted() {
        return started;
    }

    public List<DestinationViewMBean> listTopics(IObserver<Integer> progressObserver) {
        return jmxMonitor.listTopics(progressObserver);
        //return jmxMonitor.listQueues(progressObserver);
    }

    public void removeAdvisoryObserver(IJmsObserver<ActiveMQMessage> observer) {
        advisoryMonitor.removeObserver(observer);
    }

    public void addAdvisoryObserver(IJmsObserver<ActiveMQMessage> observer) {
        advisoryMonitor.addObserver(observer);
    }

    public void removeTopic(String topicName) {
        jmxMonitor.removeTopic(topicName);
    }

    public void addTopicObserver(IJmsObserver<Message> observer) {
        topicMonitor.addObserver(observer);
    }

    public void listenTo(String topicName, boolean withRetro) {
        topicMonitor.listenTo(topicName, withRetro);
    }

    public void stopListeningTo(String topicName, boolean withRetro) {
        topicMonitor.stopListeningTo(topicName, withRetro);
    }

    public void setTopicCount(int count) {
        this.topicCount = count;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public List<TopicViewMBean> filterTopics(String newTopicName) {
        return jmxMonitor.filterTopics(newTopicName);
    }

}
