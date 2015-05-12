package activemq.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.JmxConnectionTask;
import activemq.JmxException;

public class JmxMonitor {
    final Logger log = LoggerFactory.getLogger(getClass().getName());

    private JmxConnectionTask connection;

    private BrokerViewMBean brokerView;

    public JmxMonitor(String jmxUrl) {
        connection = new JmxConnectionTask(jmxUrl);
    }

    public void startup() {
        connection.connect();
        refreshBroker();
    }

    public boolean isConnected() {
        return (connection != null);
    }

    public void shutdown() {
        if (connection != null) {
            connection.stop();
        }
    }

    private BrokerViewMBean createBrokerViewMBean() {
        return (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(connection
                .getMBeanConnection(), connection.getFirstBroker(), BrokerViewMBean.class, true);
    }

    @SuppressWarnings("unchecked")
    public List<TopicViewMBean> listTopics() {
        try {
            ObjectName[] topics = brokerView.getTopics();
            ObjectName pattern = new ObjectName("org.apache.activemq:*");

            return (List<TopicViewMBean>) getManagedObjects(topics, TopicViewMBean.class, pattern);
        } catch (MalformedObjectNameException e) {
            log.error("Unable to list topics", e);
            throw new JmxException("Unable to list topics", e);
        }
    }

    @SuppressWarnings("unchecked") 
    protected List<? extends DestinationViewMBean> getManagedObjects(ObjectName[] names, Class type, ObjectName pattern) {
        MBeanServerConnection mBeanConnection = connection.getMBeanConnection();

        List managedObjects = new ArrayList();
        if (mBeanConnection != null) {
            for (int i = 0; i < names.length; i++) {
                ObjectName name = names[i];
                if (pattern == null || pattern.apply(name)) {
                    Object value = MBeanServerInvocationHandler.newProxyInstance(mBeanConnection, name, type,
                            true);
                    if (value != null) {
                        managedObjects.add(value);
                    }
                }
            }
        }
        return managedObjects;
    }

    @SuppressWarnings("unchecked")
    public List<TopicViewMBean> filterTopics(String regex) {
        try {
            ObjectName[] topics = brokerView.getTopics();
            // org.apache.activemq:BrokerName=UnitTestBroker,Type=Topic,Destination=ActiveMQ.Advisory.Consumer.Topic.ID_nyws802-3674-1213637862116-2_3_1
            ObjectName pattern = new ObjectName("org.apache.activemq:*,Destination=" + regex);

            return (List<TopicViewMBean>) getManagedObjects(topics, TopicViewMBean.class, pattern);
        } catch (MalformedObjectNameException e) {
            log.error("Unable to list topics", e);
            throw new JmxException("Unable to list topics", e);
        }
    }

    public int getTopicCount() {
        return brokerView.getTopics().length;
    }

    /**
     * Remove a topic based on the name.
     * 
     * @param topicName
     * @return
     */
    public boolean removeTopics(String topicNames) {
        List<TopicViewMBean> topics = filterTopics(topicNames);

        boolean removedAtLeastOne = false;

        for (TopicViewMBean topic : topics) {
            try {
                brokerView.removeTopic(topic.getName());
                removedAtLeastOne = true;
            } catch (Exception e) {
                log.error("Failed to remove topic: " + topic.getName(), e);
            }
        }
        return removedAtLeastOne;
    }

    /**
     * Removes the topic if it exists.
     * 
     * @param topicName
     */
    public void removeTopic(String topicName) {
        try {
            brokerView.removeTopic(topicName);
        } catch (Exception e) {
            log.error("Failed to remove topic: " + topicName, e);
        }
    }
    
    private void refreshBroker() {
        synchronized (connection) {
            brokerView = createBrokerViewMBean();
        }
    }
}
