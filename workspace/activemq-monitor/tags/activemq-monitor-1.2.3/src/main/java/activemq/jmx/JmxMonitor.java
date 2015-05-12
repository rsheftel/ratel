package activemq.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.IObserver;

import activemq.JmxConnectionTask;
import activemq.JmxException;

/**
 * 
 * TODO Make this class observable so that we can have a nicer user experience
 * when loading the topics.
 */
public class JmxMonitor {
    final Logger log = LoggerFactory.getLogger(getClass().getName());

    private JmxConnectionTask connection;

    private BrokerViewMBean brokerView;

    private MemoryMXBean memoryBean;
    
    private ThreadMXBean threadBean;

    public JmxMonitor(String jmxUrl) {
        connection = new JmxConnectionTask(jmxUrl);
    }

    public void startup() {
        connection.connect();
        refreshBroker();
    }

    public boolean isConnected() {
        return (connection != null && connection.isConnected());
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

    public List<TopicViewMBean> listTopics(IObserver<Integer> observer) {
        try {
            ObjectName[] topics = brokerView.getTopics();
            ObjectName pattern = new ObjectName("org.apache.activemq:*");

            return this.<TopicViewMBean> getManagedObjects(topics, TopicViewMBean.class, pattern, observer);
        } catch (MalformedObjectNameException e) {
            log.error("Unable to list topics", e);
            throw new JmxException("Unable to list topics", e);
        }
        
    }



    public List<TopicViewMBean> listTopics() {
        return listTopics(null);
    }

    protected <T> List<T> getManagedObjects(ObjectName[] names, Class<T> type, ObjectName pattern, IObserver<Integer> observer) {
        MBeanServerConnection mBeanConnection = connection.getMBeanConnection();

        List<T> managedObjects = new ArrayList<T>();
        if (mBeanConnection != null) {
            for (int i = 0; i < names.length; i++) {
                if (observer != null) {
                    observer.onUpdate(calculateProgress(i+1, names.length));
                }
                ObjectName name = names[i];
                if (pattern == null || pattern.apply(name)) {
                    T value = (T) MBeanServerInvocationHandler.newProxyInstance(mBeanConnection, name, type, true);
                    if (value != null) {
                        log.info("adding MBean " + name);
                        managedObjects.add(value);
                    }
                }
            }
        }
        return managedObjects;
    }

    private int calculateProgress(int numInt, int denumInt) {
        float num = numInt;
        float denum = denumInt;
        
        return (int)(num / denum * 100f);
    }

    protected <T> List<T> getManagedObjects(String[] names, Class<T> type, String patternStr) {
        MBeanServerConnection mBeanConnection = connection.getMBeanConnection();

        ObjectName pattern = null;
        
        if (patternStr != null && patternStr.trim().length() > 0) {
            try {
                pattern = new ObjectName(patternStr);
            } catch (MalformedObjectNameException e) {
                log.error("Invalid JMX object pattern - " + patternStr, e);
            }
        }
        List<T> managedObjects = new ArrayList<T>();
        if (mBeanConnection != null) {
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                
                try {
                    if (pattern == null || pattern.apply(new ObjectName(name))) {
                        T value = ManagementFactory.newPlatformMXBeanProxy(mBeanConnection, name, type);
                        if (value != null) {
                            log.info("adding MBean " + name);
                            managedObjects.add(value);
                        }
                    }
                } catch (MalformedObjectNameException e) {
                    log.error("Invalid MBean name.", e);
                } catch (IOException e) {
                    log.error("Unable to connect to remote JMX server", e);
                }
            }
        }
        return managedObjects;
    }

    public List<TopicViewMBean> filterTopics(String regex) {
        try {
            ObjectName[] topics = brokerView.getTopics();
            // org.apache.activemq:BrokerName=UnitTestBroker,Type=Topic,Destination=ActiveMQ.Advisory.Consumer.Topic.ID_nyws802-3674-1213637862116-2_3_1
            ObjectName pattern = new ObjectName("org.apache.activemq:*,Destination=" + regex);

            return this.<TopicViewMBean> getManagedObjects(topics, TopicViewMBean.class, pattern, null);
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

    /**
     * Return the heap usage.
     * 
     *  Heap usage is obtained from the:
     *  <ul><li>Eden space</li><li>Survivor space</li><li>Tenured space</li></ul>
     *  
     *  
     * @return
     */
    public MemoryUsage getHeapMemoryUsage() {
        if (memoryBean == null) {
            String[] objectNames = new String[1];
            objectNames[0] = ManagementFactory.MEMORY_MXBEAN_NAME;
            List<MemoryMXBean> memoryBeans = this.<MemoryMXBean> getManagedObjects(objectNames, MemoryMXBean.class, null);
            memoryBean = memoryBeans.get(0);
        }

        return memoryBean.getHeapMemoryUsage();
    }

    /**
     * Sets the threshold for the Tenured Heap space and assign the listener.
     * 
     * @param threshold
     * @param listener
     */
    public void setTenuredThreshold(int threshold, Object listener) {
        String[] objectNames = new String[5];
        // Non-heap
        objectNames[0] = ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name=Code Cache";
        objectNames[1] = ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name=Perm Gen";
        
        // Heap
        // Does not support limit
        objectNames[2] = ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name=Eden Space";
        // Does not support limit
        objectNames[3] = ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name=Survivor Space";
        
        objectNames[4] = ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name=Tenured Gen";
        
        List<MemoryPoolMXBean> memoryPoolBeans = this.<MemoryPoolMXBean> getManagedObjects(objectNames, MemoryPoolMXBean.class, null);
        
        for (MemoryPoolMXBean bean : memoryPoolBeans) {
            
            System.out.println(bean.getName() +":"+bean.isUsageThresholdSupported());
        }
        
    }

    public ThreadCountInfo getThreadCountInfo() {
        if (threadBean == null) {
            String[] objectNames = new String[1];
            objectNames[0] = ManagementFactory.THREAD_MXBEAN_NAME;
            List<ThreadMXBean> threadBeans = this.<ThreadMXBean> getManagedObjects(objectNames, ThreadMXBean.class, null);
            threadBean = threadBeans.get(0);
        }

        ThreadCountInfo tci = new ThreadCountInfo();
        tci.peakThreadCount = threadBean.getPeakThreadCount();
        tci.threadCount = threadBean.getThreadCount();

        return tci;
    }

}
