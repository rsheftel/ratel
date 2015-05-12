/**
 * 
 */
package util;

import java.util.List;
import java.util.Map;

import javax.jms.InvalidSelectorException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.apache.activemq.broker.jmx.TopicViewMBean;

public class TestTopicViewMBean implements TopicViewMBean {
    private String name;

    public TestTopicViewMBean(String name) {
        this.name = name;
    }

    @Override public CompositeData[] browse() throws OpenDataException {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override public CompositeData[] browse(String selector) throws OpenDataException,
    InvalidSelectorException {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override public TabularData browseAsTable() throws OpenDataException {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override public TabularData browseAsTable(String selector) throws OpenDataException,
    InvalidSelectorException {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override public List<?> browseMessages() throws InvalidSelectorException {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override public List<?> browseMessages(String selector) throws InvalidSelectorException {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override public double getAverageEnqueueTime() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getConsumerCount() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getDequeueCount() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getDispatchCount() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getEnqueueCount() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getInFlightCount() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public int getMaxAuditDepth() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getMaxEnqueueTime() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public int getMaxProducersToAudit() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getMemoryLimit() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public int getMemoryPercentUsage() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public float getMemoryUsagePortion() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getMinEnqueueTime() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public String getName() {
        return name;
    }

    @Override public long getProducerCount() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public long getQueueSize() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }

    @Override public boolean isProducerFlowControl() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return false;
    }

    @Override public void resetStatistics() {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
    }

    @Override public String sendTextMessage(String body) throws Exception {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override public String sendTextMessage(Map headers, String body) throws Exception {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override public void setMaxAuditDepth(int maxAuditDepth) {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
    }

    @Override public void setMaxProducersToAudit(int maxProducersToAudit) {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
    }

    @Override public void setMemoryLimit(long limit) {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
    }

    @Override public void setMemoryUsagePortion(float value) {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
    }

    @Override public void setProducerFlowControl(boolean producerFlowControl) {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
    }
}